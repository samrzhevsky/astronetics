package ru.samrzhevsky.astronetics.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;

import ru.samrzhevsky.astronetics.Utils;
import ru.samrzhevsky.astronetics.data.Test;
import ru.samrzhevsky.astronetics.R;
import ru.samrzhevsky.astronetics.data.Constants;

public class TestsListAdapter extends ArrayAdapter<Test> {
    private final ArrayList<Test> testsList;
    private final Context context;

    public TestsListAdapter(Context context, ArrayList<Test> testsList) {
        super(context, R.layout.tests_item, testsList);
        this.testsList = testsList;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.tests_item, parent, false);
        }

        Test test = testsList.get(position);

        TextView testName = convertView.findViewById(R.id.tests_item_name);
        testName.setText(test.getCategory().getName());

        ImageView testDone = convertView.findViewById(R.id.tests_item_check);
        TextView testResultText = convertView.findViewById(R.id.tests_item_result);

        if (test.isCompleted()) {
            int testResult = test.getResult();
            testResultText.setText(context.getString(R.string.test_result, testResult, Constants.QUESTIONS_IN_TEST));

            if (testResult >= Constants.PASSING_SCORE) {
                testDone.setImageResource(R.drawable.baseline_check_box_24);
                testDone.setColorFilter(ContextCompat.getColor(context, R.color.green));
            } else {
                testDone.setImageResource(R.drawable.outline_disabled_by_default_24);
                testDone.setColorFilter(ContextCompat.getColor(context, R.color.red));
            }

            int lockTime = test.getLockTime();
            TextView testRePass = convertView.findViewById(R.id.test_repass);
            if (lockTime == -1) {
                testRePass.setText(R.string.test_repass_unavaiable);
            } else if (lockTime > 0) {
                testRePass.setText(context.getString(R.string.test_repass_avaiable_time, Utils.timeToString(lockTime)));
            } else {
                testRePass.setTextColor(ContextCompat.getColor(context, R.color.green));
                testRePass.setText(R.string.test_repass_avaiable);
            }
        } else {
            testResultText.setText(R.string.test_not_passed_yet);

            testDone.setImageResource(R.drawable.outline_check_box_outline_blank_24);
            testDone.setColorFilter(ContextCompat.getColor(context, R.color.grey));
        }

        return convertView;
    }
}