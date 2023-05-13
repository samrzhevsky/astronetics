package ru.samrzhevsky.astronetics.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.fragment.NavHostFragment;

import org.json.JSONException;
import org.json.JSONObject;

import ru.samrzhevsky.astronetics.Utils;
import ru.samrzhevsky.astronetics.R;
import ru.samrzhevsky.astronetics.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private Context context;
    private FragmentActivity activity;
    private ProgressDialog progressDialog;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        context = requireContext();
        activity = requireActivity();

        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        loadProfile();

        binding.settingsSave.setOnClickListener(this::saveProfile);
        binding.settingsEditUnavailable.setVisibility(View.GONE);

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
