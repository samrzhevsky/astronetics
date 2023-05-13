package ru.samrzhevsky.astronetics.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ru.samrzhevsky.astronetics.R;
import ru.samrzhevsky.astronetics.Utils;
import ru.samrzhevsky.astronetics.data.Category;
import ru.samrzhevsky.astronetics.data.Article;

public class CategoryExpandableListAdapter extends BaseExpandableListAdapter {
    private final Context context;
    private final ArrayList<Category> categoryArrayList;
    private final ArrayList<Article> articleArrayList;

    public CategoryExpandableListAdapter(Context context, ArrayList<Category> categoryArrayList, ArrayList<Article> articleArrayList) {
        this.context = context;
        this.categoryArrayList = categoryArrayList;
        this.articleArrayList = articleArrayList;
    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        Category category = categoryArrayList.get(listPosition);
        ArrayList<Article> tmp = new ArrayList<>();

        for (Article article : articleArrayList) {
            if (article.getCategoryId() == category.getId()) {
                tmp.add(article);
            }
        }

        return tmp.get(expandedListPosition);
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @SuppressLint("DiscouragedApi")
    @Override
    public View getChildView(int listPosition, int expandedListPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.theoretical_item, parent, false);
        }

        Article article = (Article) getChild(listPosition, expandedListPosition);

        TextView itemName = convertView.findViewById(R.id.theoretical_item_name);
        itemName.setText(article.getName());

        TextView itemDescription = convertView.findViewById(R.id.theoretical_item_description);
        itemDescription.setText(article.getDescription());

        ImageView itemImage = convertView.findViewById(R.id.theoretical_item_image);
        itemImage.setImageResource(context.getResources().getIdentifier(article.getImage(), "drawable", context.getPackageName()));

        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        int count = 0;
        Category category = categoryArrayList.get(listPosition);

        for (Article article : articleArrayList) {
            if (article.getCategoryId() == category.getId()) {
                count++;
            }
        }

        return count;
    }

    @Override
    public Object getGroup(int listPosition) {
        return categoryArrayList.get(listPosition);
    }

    @Override
    public int getGroupCount() {
        return categoryArrayList.size();
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.theoretical_category_list, parent, false);
        }

        Category category = (Category) getGroup(listPosition);

        TextView title = convertView.findViewById(R.id.categories_listTitle);
        title.setTypeface(null, Typeface.BOLD);
        title.setText(category.getName());

        int countOfThemes = getChildrenCount(listPosition);
        TextView countThemes = convertView.findViewById(R.id.categories_countThemes);
        countThemes.setText(
                Utils.declension(
                        countOfThemes,
                        context.getString(R.string.theme_count_1, countOfThemes),
                        context.getString(R.string.theme_count_3, countOfThemes),
                        context.getString(R.string.theme_count_5, countOfThemes)
                )
        );

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }
}
