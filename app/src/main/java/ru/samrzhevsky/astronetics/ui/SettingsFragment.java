package ru.samrzhevsky.astronetics.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

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

import ru.samrzhevsky.astronetics.BuildConfig;
import ru.samrzhevsky.astronetics.Utils;
import ru.samrzhevsky.astronetics.R;
import ru.samrzhevsky.astronetics.VkAuth;
import ru.samrzhevsky.astronetics.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private Context context;
    private FragmentActivity activity;
    private ProgressDialog progressDialog;

    private ActivityResultLauncher<String> requestPermissionLauncher;

    private String downloadUrl;

    @SuppressWarnings("deprecation")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        context = requireContext();
        activity = requireActivity();

        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        if (VkAuth.getUserToken(context) == null) {
            // display login button if token is null
            binding.btnSettingsVkLogin.setOnClickListener(view -> VkAuth.startNewIntent(context));
            progressDialog.cancel();
        } else {
            binding.settingsMainContent.setVisibility(View.VISIBLE);
            loadProfile();
        }

        binding.settingsSave.setOnClickListener(this::saveProfile);
        binding.settingsEditUnavailable.setVisibility(View.GONE);

        binding.settingsCheckForUpdates.setOnClickListener(this::checkForUpdates);

        binding.settingsLogout.setOnClickListener(view -> new AlertDialog.Builder(context)
                .setTitle(R.string.confirmation)
                .setMessage(R.string.logout_confirm)
                .setNegativeButton(R.string.no, (dialog, id) -> dialog.cancel())
                .setPositiveButton(R.string.yes, (dialog, id) -> {
                    dialog.cancel();
                    logout(view);
                })
                .show());

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // Permission granted
                        downloadUpdate();
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

        return root;
    }

    private void loadProfile() {
        if (Utils.isNetworkUnavailable(context)) {
            progressDialog.cancel();
            NavHostFragment.findNavController(this).popBackStack();
            new AlertDialog.Builder(context)
                    .setTitle(R.string.error)
                    .setMessage(R.string.error_network)
                    .setPositiveButton(R.string.ok, (dialog, id) -> dialog.cancel())
                    .show();
            return;
        }

        progressDialog.show();

        new Thread(() -> {
            try {
                String apiResponse = Utils.apiGetRequest(context,"getProfile",null);
                JSONObject json = new JSONObject(apiResponse);

                if (json.getInt("status") == 1) {
                    String firstname = json.getString("firstname");
                    String lastname = json.getString("lastname");
                    String midname = json.getString("midname");
                    boolean isCertSaved = json.getBoolean("cert_saved");

                    activity.runOnUiThread(() -> {
                        progressDialog.cancel();

                        binding.settingsFirstname.setText(firstname);
                        binding.settingsLastname.setText(lastname);
                        binding.settingsMidname.setText(midname);

                        if (isCertSaved) {
                            binding.settingsFirstname.setEnabled(false);
                            binding.settingsLastname.setEnabled(false);
                            binding.settingsMidname.setEnabled(false);
                            binding.settingsSave.setEnabled(false);
                            binding.settingsEditUnavailable.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                    activity.runOnUiThread(() -> {
                        progressDialog.cancel();

                        NavController navController = NavHostFragment.findNavController(this);
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

    private void saveProfile(View view) {
        EditText settingsFirstname = binding.settingsFirstname;
        EditText settingsLastname = binding.settingsLastname;
        EditText settingsMidname = binding.settingsMidname;

        settingsFirstname.clearFocus();
        settingsLastname.clearFocus();
        settingsMidname.clearFocus();

        if (Utils.isNetworkUnavailable(context)) {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.error)
                    .setMessage(R.string.error_network)
                    .setPositiveButton(R.string.ok, (dialog, which) -> dialog.cancel())
                    .show();
            return;
        }

        String firstname = settingsFirstname.getText().toString().trim();
        String lastname = settingsLastname.getText().toString().trim();
        String midname = settingsMidname.getText().toString().trim();

        if (firstname.equals("") || lastname.equals("")) {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.error)
                    .setMessage(R.string.settings_required_fields)
                    .setPositiveButton(R.string.ok, (dialog, which) -> dialog.cancel())
                    .show();
            return;
        }

        progressDialog.show();

        new Thread(() -> {
            try {
                JSONObject requestBody = new JSONObject();
                requestBody.put("firstname", firstname);
                requestBody.put("lastname", lastname);
                requestBody.put("midname", midname);

                String apiResponse = Utils.apiPostRequest(context,"editProfile",null, requestBody.toString());
                JSONObject json = new JSONObject(apiResponse);

                if (json.getInt("status") == 1) {
                    activity.runOnUiThread(() -> {
                        progressDialog.cancel();

                        new AlertDialog.Builder(context)
                                .setTitle(R.string.notification)
                                .setMessage(R.string.settings_successfully_saved)
                                .setPositiveButton(R.string.ok, (dialog, id) -> dialog.cancel())
                                .show();
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

    private void checkForUpdates(View view) {
        if (Utils.isNetworkUnavailable(context)) {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.error)
                    .setMessage(R.string.error_network)
                    .setPositiveButton(R.string.ok, (dialog, which) -> dialog.cancel())
                    .show();
            return;
        }

        progressDialog.show();

        new Thread(() -> {
            try {
                String apiResponse = Utils.apiGetRequest(context,"checkForUpdates", "current=" + BuildConfig.VERSION_CODE);
                JSONObject json = new JSONObject(apiResponse);

                if (json.getInt("status") == 1) {
                    boolean hasUpdates = json.getBoolean("has_updates");
                    downloadUrl = json.getString("download_url");

                    activity.runOnUiThread(() -> {
                        progressDialog.cancel();

                        if (hasUpdates) {
                            new AlertDialog.Builder(context)
                                    .setTitle(R.string.notification)
                                    .setMessage(R.string.settings_update_available)
                                    .setNegativeButton(R.string.ok, (dialog, id) -> dialog.cancel())
                                    .setPositiveButton(R.string.reload, (dialog, id) -> tryDownloadUpdate())
                                    .show();
                        } else {
                            new AlertDialog.Builder(context)
                                    .setTitle(R.string.notification)
                                    .setMessage(R.string.settings_installed_last_version)
                                    .setPositiveButton(R.string.ok, (dialog, id) -> dialog.cancel())
                                    .show();
                        }

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

    private void logout(View view) {
        if (Utils.isNetworkUnavailable(context)) {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.error)
                    .setMessage(R.string.error_network)
                    .setPositiveButton(R.string.ok, (dialog, which) -> dialog.cancel())
                    .show();
            return;
        }

        progressDialog.show();

        new Thread(() -> {
            try {
                String apiResponse = Utils.apiPostRequest(context, "logout", null, "");
                JSONObject json = new JSONObject(apiResponse);

                if (json.getInt("status") == 1) {
                    VkAuth.unsetUserToken(context);

                    activity.runOnUiThread(() -> {
                        progressDialog.cancel();
                        Navigation.findNavController(view).navigate(R.id.action_nav_settings_to_nav_theoretical_materials);
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

    private void tryDownloadUpdate() {
        // request permission
        int permissionCheck = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            downloadUpdate();
        }
    }

    private void downloadUpdate() {
        Uri uri = Uri.parse(downloadUrl);
        String fileName = getString(R.string.app_name).toLowerCase() + ".apk";
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri)
                .setMimeType("application/vnd.android.package-archive")
                .setTitle(fileName)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        manager.enqueue(request);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
