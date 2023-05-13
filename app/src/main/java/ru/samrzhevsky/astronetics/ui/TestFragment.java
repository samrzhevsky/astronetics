package ru.samrzhevsky.astronetics.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.Navigation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import ru.samrzhevsky.astronetics.R;
import ru.samrzhevsky.astronetics.Utils;
import ru.samrzhevsky.astronetics.data.Test;
import ru.samrzhevsky.astronetics.databinding.FragmentTestBinding;

public class TestFragment extends Fragment {
    private FragmentTestBinding binding;

    private FragmentActivity activity;

    private Context context;

    private View root;

    private ProgressDialog progressDialog;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = requireActivity();
        context = requireContext();
        binding = FragmentTestBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(() -> {
            try {
                final int testId = requireArguments().getInt("test_id");

                String apiResponse = Utils.apiGetRequest(context, "getTestById", "test_id=" + testId);
                JSONObject json = new JSONObject(apiResponse);

                if (json.getInt("status") == 1) {
                    Test test = Test.fromJSON(json.getJSONObject("test"));
                    JSONArray questions = json.getJSONArray("questions");
                    int[] questionIds = new int[questions.length()];
                    HashMap<Integer, ArrayList<Integer>> answersIds = new HashMap<>();

                    for (int i = 0; i < questions.length(); i++) {
                        JSONObject question = questions.getJSONObject(i);
                        JSONArray answers = question.getJSONArray("answers");
                        final boolean isCorrect = question.getBoolean("correct");
                        final int questionViewId = View.generateViewId();

                        questionIds[i] = questionViewId;
                        answersIds.put(questionViewId, new ArrayList<>());

                        TextView questionText = new TextView(context);
                        questionText.setTextColor(ContextCompat.getColor(context, R.color.black));
                        questionText.setText(question.getString("question"));

                        RadioGroup questionRadioGroup = new RadioGroup(context);
                        questionRadioGroup.setId(questionIds[i]);
                        questionRadioGroup.setPadding(
                                Utils.dp2px(context, 10),
                                Utils.dp2px(context, 10),
                                Utils.dp2px(context, 10),
                                Utils.dp2px(context, 10)
                        );
                        questionRadioGroup.addView(questionText);

                        // rounded background
                        ShapeDrawable shapeDrawable = new ShapeDrawable(
                                new RoundRectShape(
                                        new float[] { 20, 20, 20, 20, 20, 20, 20, 20 },
                                        null,
                                        null
                                )
                        );
                        shapeDrawable.getPaint().setColor(ContextCompat.getColor(context, R.color.white1));
                        questionRadioGroup.setBackground(shapeDrawable);

                        RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                                RadioGroup.LayoutParams.MATCH_PARENT,
                                RadioGroup.LayoutParams.WRAP_CONTENT
                        );
                        params.bottomMargin = Utils.dp2px(context, 10);

                        for (int j = 0; j < answers.length(); j++) {
                            JSONObject answer = answers.getJSONObject(j);
                            final int answerViewId = View.generateViewId();

                            Objects.requireNonNull(answersIds.get(questionViewId)).add(answerViewId);

                            RadioButton answerBtn = new RadioButton(context);
                            answerBtn.setId(answerViewId);
                            answerBtn.setText(answer.getString("answer"));
                            answerBtn.setChecked(answer.getBoolean("selected"));
                            answerBtn.setClickable(!test.isCompleted());

                            if (test.isCompleted()) {
                                answerBtn.setButtonTintList(
                                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grey)));
                            } else {
                                answerBtn.setButtonTintList(
                                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.purple_200)));
                            }

                            questionRadioGroup.addView(answerBtn);
                        }

                        if (test.isCompleted()) {
                            TextView resultText = new TextView(context);
                            resultText.setTypeface(resultText.getTypeface(), Typeface.BOLD);

                            if (isCorrect) {
                                resultText.setTextColor(ContextCompat.getColor(context, R.color.green));
                                resultText.setText(R.string.test_answer_is_correct);
                            } else {
                                resultText.setTextColor(ContextCompat.getColor(context, R.color.red));
                                resultText.setText(R.string.test_answer_is_wrong);
                            }

                            questionRadioGroup.addView(resultText);
                        }

                        activity.runOnUiThread(() -> binding.testLayout.addView(questionRadioGroup, params));
                    }

                    activity.runOnUiThread(() -> {
                        progressDialog.cancel();

                        Button submitBtn = new Button(context);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                RadioGroup.LayoutParams.MATCH_PARENT,
                                RadioGroup.LayoutParams.WRAP_CONTENT
                        );
                        params.bottomMargin = Utils.dp2px(context, 10);

                        if (test.isCompleted()) {
                            int lockTime = test.getLockTime();
                            submitBtn.setClickable(false);

                            if (lockTime == -1) {
                                submitBtn.setText(R.string.test_repass_unavaiable);
                            } else if (lockTime > 0) {
                                submitBtn.setText(context.getString(R.string.test_repass_avaiable_time, Utils.timeToString(lockTime)));
                            } else {
                                submitBtn.setClickable(true);
                                submitBtn.setText(R.string.test_repass);
                                submitBtn.setOnClickListener(view -> new AlertDialog.Builder(context)
                                        .setTitle(R.string.confirmation)
                                        .setMessage(R.string.test_repass_confirmation)
                                        .setNegativeButton(R.string.no, (dialog, id) -> dialog.cancel())
                                        .setPositiveButton(R.string.yes, (dialog, id) -> {
                                            dialog.cancel();
                                            this.repassTest(view, testId);
                                        })
                                        .show());
                            }
                        } else {
                            submitBtn.setText(R.string.test_submit);
                            submitBtn.setOnClickListener(view -> new AlertDialog.Builder(context)
                                    .setTitle(R.string.confirmation)
                                    .setMessage(R.string.test_submit_confirmation)
                                    .setNegativeButton(R.string.no, (dialog, id) -> dialog.cancel())
                                    .setPositiveButton(R.string.yes, (dialog, id) -> {
                                        dialog.cancel();
                                        this.submitTest(view, testId, questionIds, answersIds);
                                    })
                                    .show());
                        }

                        binding.testLayout.addView(submitBtn, params);
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

        return root;
    }

    private void submitTest(View view, int testId, int[] questionIds, HashMap<Integer, ArrayList<Integer>> answersIds) {
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
                JSONObject requestBody = new JSONObject();
                JSONArray answers = new JSONArray();

                for (int i = 0; i < questionIds.length; i++) {
                    RadioGroup question = root.findViewById(questionIds[i]);
                    int checkedBtnId = question.getCheckedRadioButtonId();

                    if (checkedBtnId == -1) {
                        activity.runOnUiThread(() -> {
                            progressDialog.cancel();

                            new AlertDialog.Builder(context)
                                    .setTitle(R.string.error)
                                    .setMessage(R.string.test_answer_all_questions)
                                    .setPositiveButton(R.string.ok, (dialog, id) -> dialog.cancel())
                                    .show();
                        });
                        return;
                    } else {
                        int answer = Objects.requireNonNull(answersIds.get(questionIds[i])).indexOf(checkedBtnId);
                        answers.put(i, answer);
                    }
                }

                requestBody.put("test_id", testId);
                requestBody.put("answers", answers);

                String response = Utils.apiPostRequest(context, "checkAnswers", null, requestBody.toString());
                JSONObject json = new JSONObject(response);

                if (json.getInt("status") == 1) {
                    JSONArray result = json.getJSONArray("result");
                    boolean canGetCertificate = json.getBoolean("cert");
                    boolean needToFillProfile = json.getBoolean("need_fill_profile");

                    for (int i = 0; i < result.length(); i++) {
                        TextView resultText = new TextView(context);
                        resultText.setTypeface(resultText.getTypeface(), Typeface.BOLD);

                        if (result.getBoolean(i)) {
                            resultText.setTextColor(ContextCompat.getColor(context, R.color.green));
                            resultText.setText(R.string.test_answer_is_correct);
                        } else {
                            resultText.setTextColor(ContextCompat.getColor(context, R.color.red));
                            resultText.setText(R.string.test_answer_is_wrong);
                        }

                        RadioGroup questionRadioGroup = root.findViewById(questionIds[i]);
                        final int countOfAnswersInQuestion = Objects.requireNonNull(answersIds.get(questionIds[i])).size();
                        activity.runOnUiThread(() -> {
                            view.setVisibility(View.GONE);

                            for (int j = 1; j < countOfAnswersInQuestion + 1; j++) {
                                RadioButton answerBtn = (RadioButton) questionRadioGroup.getChildAt(j);
                                answerBtn.setClickable(false);
                                answerBtn.setButtonTintList(
                                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grey)));
                            }

                            questionRadioGroup.addView(resultText);
                        });
                    }

                    activity.runOnUiThread(() -> {
                        progressDialog.cancel();

                        if (canGetCertificate) {
                            AlertDialog.Builder alert = new AlertDialog.Builder(context)
                                    .setTitle(R.string.congrats)
                                    .setNegativeButton(R.string.close, (dialog, id) -> dialog.cancel());

                            if (needToFillProfile) {
                                alert.setMessage(R.string.test_you_can_get_cert_profile);
                                alert.setPositiveButton(R.string.go_to_profile, (dialog, id) -> {
                                    dialog.cancel();
                                    Navigation.findNavController(view).navigate(R.id.action_nav_test_to_nav_settings);
                                });
                            } else {
                                alert.setMessage(R.string.test_you_can_get_cert);
                                alert.setPositiveButton(R.string.get, (dialog, id) -> {
                                    dialog.cancel();
                                    Navigation.findNavController(view).navigate(R.id.action_nav_test_to_nav_rating);
                                });
                            }

                            alert.show();
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

    private void repassTest(View view, int testId) {
        if (Utils.isNetworkUnavailable(context)) {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.error)
                    .setMessage(R.string.error_network)
                    .setPositiveButton(R.string.ok, (dialog, which) -> dialog.cancel())
                    .show();
            return;
        }

        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.show();

        new Thread(() -> {
            try {
                String apiResponse = Utils.apiGetRequest(context, "regenerateTest", "test_id=" + testId);
                JSONObject json = new JSONObject(apiResponse);

                if (json.getInt("status") == 1) {
                    activity.runOnUiThread(() -> {
                        progressDialog.cancel();

                        Bundle bundle = new Bundle();
                        bundle.putInt("test_id", testId);

                        Navigation.findNavController(view).navigate(R.id.action_nav_test_self, bundle);
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
