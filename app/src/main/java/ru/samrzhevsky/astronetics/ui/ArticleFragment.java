package ru.samrzhevsky.astronetics.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import ru.samrzhevsky.astronetics.databinding.FragmentArticleBinding;

public class ArticleFragment extends Fragment {
    private FragmentArticleBinding binding;

    @SuppressLint("SetJavaScriptEnabled")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentArticleBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        WebView webView = binding.articleWebview;

        String resourceName = requireArguments().getString("resource_name");
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/articles/" + resourceName + ".html");

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}