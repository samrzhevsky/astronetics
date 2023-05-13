package ru.samrzhevsky.astronetics.data;

public class Article {
    private final int categoryId;
    private final String name;
    private final String description;
    private final String resourceName;
    private final String imageName;

    public Article(int categoryId, String name, String description, String resourceName, String imageName) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.resourceName = resourceName;
        this.imageName = imageName;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getImage() {
        return imageName;
    }
}
