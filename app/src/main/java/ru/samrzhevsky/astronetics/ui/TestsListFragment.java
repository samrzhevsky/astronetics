package ru.samrzhevsky.astronetics.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.Navigation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ru.samrzhevsky.astronetics.Utils;
import ru.samrzhevsky.astronetics.VkAuth;
import ru.samrzhevsky.astronetics.data.Test;
import ru.samrzhevsky.astronetics.R;
import ru.samrzhevsky.astronetics.adapters.TestsListAdapter;
import ru.samrzhevsky.astronetics.databinding.FragmentTestsListBinding;

public class TestsListFragment extends Fragment {
    private FragmentTestsListBinding binding;

    private FragmentActivity activity;

    private Context context;

    private ProgressDialog progressDialog;

    private final Runnable gettingTestsFromApi = () -> {
        activity.runOnUiThread(() -> {
            binding.testsListBtnReload.setVisibility(View.GONE);
            progressDialog.show();
        });

        try {
            String apiResponse = Utils.apiGetRequest(context,"getTests",null);
            JSONObject json = new JSONObject(apiResponse);

            if (json.getInt("status") == 1) {
                ArrayList<Test> testsArr = new ArrayList<>();
                JSONArray testsArrJson = json.getJSONArray("tests");
                for (int i = 0; i < testsArrJson.length(); i++) {
                    JSONObject test = testsArrJson.getJSONObject(i);
                    testsArr.add(Test.fromJSON(test));
                }

                activity.runOnUiThread(() -> {
                    progressDialog.cancel();

                    ListView testsListView = binding.testsList;
                    TestsListAdapter adapter = new TestsListAdapter(context, testsArr);
                    testsListView.setAdapter(adapter);
                    testsListView.setOnItemClickListener((adapterView, view, position, id) -> {
                        if (Utils.isNetworkUnavailable(context)) {
                            new AlertDialog.Builder(context)
                                    .setTitle(R.string.error)
                                    .setMessage(R.string.error_network)
                                    .setPositiveButton(R.string.ok, (dialog, which) -> dialog.cancel())
                                    .show();
                            return;
                        }

                        Test t = adapter.getItem(position);
                        if (t == null) {
                            return;
                        }

                        Bundle bundle = new Bundle();
                        bundle.putInt("test_id", t.getId());

                        Navigation.findNavController(view).navigate(R.id.action_nav_tests_list_to_nav_test, bundle);
                    });
                    testsListView.setVisibility(View.VISIBLE);
                });
            } else {
                activity.runOnUiThread(() -> {
                    progressDialog.cancel();
                    binding.testsListBtnReload.setVisibility(View.VISIBLE);

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
                binding.testsListBtnReload.setVisibility(View.VISIBLE);

                new AlertDialog.Builder(context)
                        .setTitle(R.string.error)
                        .setMessage(R.string.error_fatal)
                        .setPositiveButton(R.string.ok, (dialog, id) -> dialog.cancel())
                        .show();
            });

            e.printStackTrace();
        }
    };

    private void getTests() {
        if (Utils.isNetworkUnavailable(context)) {
            progressDialog.cancel();
            binding.testsListBtnReload.setVisibility(View.VISIBLE);

            new AlertDialog.Builder(context)
                    .setTitle(R.string.error)
                    .setMessage(R.string.error_network)
                    .setPositiveButton(R.string.ok, (dialog, which) -> dialog.cancel())
                    .show();

            return;
        }

        new Thread(gettingTestsFromApi).start();
    }

    @SuppressWarnings("deprecation")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = requireActivity();
        context = requireContext();
        binding = FragmentTestsListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        binding.testsListBtnReload.setOnClickListener(view -> getTests());

        if (VkAuth.getUserToken(context) == null) {
            // display login button if token is null
            binding.testsListBtnLogin.setOnClickListener(view -> VkAuth.startNewIntent(context));
            progressDialog.cancel();
        } else {
            binding.testsListBtnLogin.setVisibility(View.GONE);
            getTests();
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}