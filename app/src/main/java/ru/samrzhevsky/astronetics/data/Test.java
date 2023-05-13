package ru.samrzhevsky.astronetics.data;

import org.json.JSONException;
import org.json.JSONObject;

public class Test {
    private final int id;
    private final int categoryId;
    private final boolean completed;
    private final int result;
    private final int lockedUntil;

    private Test(int id, int categoryId, boolean completed, int result, int lockedUntil) {
        this.id = id;
        this.categoryId = categoryId;
        this.completed = completed;
        this.result = result;
        this.lockedUntil = lockedUntil;
    }

    public static Test fromJSON(JSONObject json) throws JSONException {
        return new Test(
                json.getInt("id"),
                json.getInt("category"),
                json.getBoolean("completed"),
                json.getInt("result"),
                json.getInt("locked_until")
        );
    }

    public int getId() {
        return id;
    }

    public Category getCategory() {
        for (int i = 0; i < Constants.CATEGORIES.size(); i++) {
            Category category = Constants.CATEGORIES.get(i);

            if (category.getId() == categoryId) {
                return category;
            }
        }

        return null;
    }

    public int getResult() {
        return result;
    }

    public boolean isCompleted() {
        return completed;
    }

    public int getLockTime() {
        return lockedUntil == -1 ? -1 : lockedUntil - Math.round(System.currentTimeMillis() / 1000f);
    }
}
