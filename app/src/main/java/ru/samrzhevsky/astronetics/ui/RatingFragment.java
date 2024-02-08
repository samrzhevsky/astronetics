package ru.samrzhevsky.astronetics.ui;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import org.json.JSONException;
import org.json.JSONObject;

import ru.samrzhevsky.astronetics.Utils;
import ru.samrzhevsky.astronetics.R;
import ru.samrzhevsky.astronetics.VkAuth;
import ru.samrzhevsky.astronetics.databinding.FragmentRatingBinding;

public class RatingFragment extends Fragment {
    private FragmentRatingBinding binding;
    private Context context;

    private ActivityResultLauncher<String> requestPermissionLauncher;

    private String certDownloadUrl;

    private ProgressDialog progressDialog;

    private FragmentActivity activity;

    private void tryDownloadCert() {
        // request permission
        int permissionCheck = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            downloadCert();
        }
    }

    private void downloadCert() {
        Uri uri = Uri.parse(certDownloadUrl);
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri)
                .setMimeType("image/png")
                .setTitle("certificate.png")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "certificate.png");
        manager.enqueue(request);
    }

    @SuppressWarnings("deprecation")
    private void resetProgress(View view) {
        if (Utils.isNetworkUnavailable(context)) {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.error)
                    .setMessage(R.string.error_network)
                    .setPositiveButton(R.string.ok, (dialog, which) -> dialog.cancel())
                    .show();
            return;
        }

        progressDialog.setMessage(getString(R.string.test_sending_answers));
        progressDialog.show();

        new Thread(() -> {
            try {
                String apiResponse = Utils.apiPostRequest(context, "resetProgress", null, "");
                JSONObject json = new JSONObject(apiResponse);

                if (json.getInt("status") == 1) {
                    activity.runOnUiThread(() -> {
                        progressDialog.cancel();

                        new AlertDialog.Builder(context)
                                .setTitle(R.string.success)
                                .setMessage(R.string.rating_reset_progress_success)
                                .setPositiveButton(R.string.ok, (dialog, id) -> dialog.cancel())
                                .show();

                        Navigation.findNavController(view).navigate(R.id.action_nav_rating_to_nav_settings);
                    });
                } else {
                    activity.runOnUiThread(() -> {
                        progressDialog.cancel();

                        try {
                            new AlertDialog.Builder(context)
                                    .setTitle(R.string.error)
                                    .setMessage(json.getString("error"))
                                    .setPositiveButton(R.string.ok, (dialog, id) -> dialog.cancel())
                                    .show();
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            } catch (Exception e) {
                activity.runOnUiThread(() -> {
                    progressDialog.cancel();

                    new AlertDialog.Builder(context)
                            .setTitle(R.string.error)
                            .setMessage(R.string.error_fatal)
                            .setPositiveButton(R.string.ok, (dialog, id) -> dialog.cancel())
                            .show();
                });

                e.printStackTrace();
            }
        }).start();
    }

    @SuppressWarnings("deprecation")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRatingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        context = requireContext();
        activity = requireActivity();
        NavController navController = NavHostFragment.findNavController(this);

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // Permission granted
                        downloadCert();
                    } else {
                        // Permission denied
                        new AlertDialog.Builder(context)
                                .setTitle(R.string.error)
                                .setMessage(R.string.error_permissions)
                                .setPositiveButton(R.string.ok, (dialog, id) -> dialog.cancel())
                                .show();
                    }
                }
        );

        // rounded background
        ShapeDrawable shapeDrawable = new ShapeDrawable(
                new RoundRectShape(
                        new float[] { 20, 20, 20, 20, 20, 20, 20, 20 },
                        null,
                        null
                )
        );
        shapeDrawable.getPaint().setColor(ContextCompat.getColor(context, R.color.white1));
        binding.ratingCard1.setBackground(shapeDrawable);
        binding.ratingCard2.setBackground(shapeDrawable);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        if (Utils.isNetworkUnavailable(context)) {
            progressDialog.cancel();
            navController.popBackStack();

            new AlertDialog.Builder(context)
                    .setTitle(R.string.error)
                    .setMessage(R.string.error_network)
                    .setPositiveButton(R.string.ok, (dialog, id) -> dialog.cancel())
                    .show();
        } else if (VkAuth.getUserToken(context) == null) {
            // display login button if token is null
            binding.btnRatingVkLogin.setOnClickListener(view -> VkAuth.startNewIntent(context));
            progressDialog.cancel();
        } else {
            new Thread(() -> {
                try {
                    String apiResponse = Utils.apiGetRequest(context, "getRating", null);
                    JSONObject json = new JSONObject(apiResponse);

                    if (json.getInt("status") == 1) {
                        String fullName = json.getString("full_name");
                        certDownloadUrl = json.getString("cert_url");
                        boolean isCertSaved = json.getBoolean("cert_saved");
                        boolean isProfileFilled = json.getBoolean("profile_filled");

                        int score = json.getInt("score");
                        String scoreStr = Utils.declension(
                                score,
                                getString(R.string.rating_score_1, score),
                                getString(R.string.rating_score_3, score),
                                getString(R.string.rating_score_5, score)
                        );
                        String betterStr = getString(R.string.rating_better, json.getInt("better"));

                        int passedTests = json.getInt("passed_tests");
                        int totalTests = json.getInt("total_tests");
                        int progress = Math.round(((float) passedTests / (float) totalTests) * 100);
                        String progressStr = getString(R.string.rating_cert_progress, passedTests, totalTests);

                        activity.runOnUiThread(() -> {
                            if (!certDownloadUrl.equals("")) {
                                binding.btnDownloadCert.setVisibility(View.VISIBLE);
                                binding.btnResetProgress.setVisibility(View.VISIBLE);

                                // Download certificate
                                if (isProfileFilled) {
                                    binding.btnDownloadCert.setOnClickListener(view -> {
                                        if (isCertSaved) {
                                            tryDownloadCert();
                                        } else {
                                            new AlertDialog.Builder(context)
                                                    .setTitle(R.string.confirmation)
                                                    .setMessage(getString(R.string.rating_download_cert_confirm, fullName))
                                                    .setNegativeButton(R.string.no, (dialog, id) -> dialog.cancel())
                                                    .setPositiveButton(R.string.yes, (dialog, id) -> {
                                                        dialog.cancel();
                                                        tryDownloadCert();
                                                        binding.btnDownloadCert.setOnClickListener(_view -> tryDownloadCert());
                                                    })
                                                    .show();
                                        }
                                    });
                                } else {
                                    binding.btnDownloadCert.setOnClickListener(view -> new AlertDialog.Builder(context)
                                            .setTitle(R.string.error)
                                            .setMessage(R.string.rating_need_to_fill_profile)
                                            .setNegativeButton(R.string.ok, (dialog, id) -> dialog.cancel())
                                            .setPositiveButton(R.string.go_to_profile, (dialog, id) -> {
                                                dialog.cancel();
                                                Navigation.findNavController(view).navigate(R.id.action_nav_rating_to_nav_settings);
                                            })
                                            .show());
                                }

                                // Reset progress
                                binding.btnResetProgress.setOnClickListener(view -> new AlertDialog.Builder(context)
                                        .setTitle(R.string.confirmation)
                                        .setMessage(R.string.rating_reset_progress_confirm)
                                        .setNegativeButton(R.string.no, (dialog, id) -> dialog.cancel())
                                        .setPositiveButton(R.string.yes, (dialog, id) -> {
                                            dialog.cancel();
                                            resetProgress(view);
                                        })
                                        .show());
                            }

                            binding.ratingScore.setText(scoreStr);
                            binding.ratingBetter.setText(betterStr);
                            binding.ratingProgress.setProgress(progress);
                            binding.ratingProgressText.setText(progressStr);
                            binding.ratingCard1.setVisibility(View.VISIBLE);
                            binding.ratingCard2.setVisibility(View.VISIBLE);
                            binding.btnRatingVkLogin.setVisibility(View.GONE);

                            progressDialog.cancel();
                        });
                    } else {
                        activity.runOnUiThread(() -> {
                            progressDialog.cancel();
                            navController.popBackStack();

                            try {
                                new AlertDialog.Builder(context)
                                        .setTitle(R.string.error)
                                        .setMessage(json.getString("error"))
                                        .setPositiveButton(R.string.ok, (dialog, id) -> dialog.cancel())
                                        .show();
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                } catch (Exception e) {
                    activity.runOnUiThread(() -> {
                        progressDialog.cancel();
                        navController.popBackStack();

                        new AlertDialog.Builder(context)
                                .setTitle(R.string.error)
                                .setMessage(R.string.error_fatal)
                                .setPositiveButton(R.string.ok, (dialog, id) -> dialog.cancel())
                                .show();
                    });
                    e.printStackTrace();
                }
            }).start();
        }

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Block height reset fix
        ShapeDrawable shapeDrawable = new ShapeDrawable(
                new RoundRectShape(
                        new float[] { 20, 20, 20, 20, 20, 20, 20, 20 },
                        null,
                        null
                )
        );
        shapeDrawable.getPaint().setColor(ContextCompat.getColor(context, R.color.white1));
        binding.ratingCard1.setBackground(shapeDrawable);
        binding.ratingCard2.setBackground(shapeDrawable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}