package ru.samrzhevsky.astronetics.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import ru.samrzhevsky.astronetics.adapters.CategoryExpandableListAdapter;
import ru.samrzhevsky.astronetics.data.Article;
import ru.samrzhevsky.astronetics.R;
import ru.samrzhevsky.astronetics.data.Constants;
import ru.samrzhevsky.astronetics.databinding.FragmentTheoreticalBinding;

public class TheoreticalFragment extends Fragment {
    private FragmentTheoreticalBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTheoreticalBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        ExpandableListView categoriesList = binding.categoriesList;
        CategoryExpandableListAdapter adapter = new CategoryExpandableListAdapter(
                requireContext(),
                Constants.CATEGORIES,
                Constants.ARTICLES
        );
        categoriesList.setAdapter(adapter);
        categoriesList.setOnChildClickListener((parent, view, groupPosition, childPosition, id) -> {
            Article t = (Article) adapter.getChild(groupPosition, childPosition);

            Bundle bundle = new Bundle();
            bundle.putString("resource_name", t.getResourceName());

            Navigation.findNavController(view).navigate(R.id.action_nav_to_theoretical_article, bundle);

            return true;
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}