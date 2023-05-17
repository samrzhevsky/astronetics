package ru.samrzhevsky.astronetics.data;

import java.util.ArrayList;

public class Constants {
    public static final int QUESTIONS_IN_TEST = 10;
    public static final int PASSING_SCORE = 6;
    public static final String API_URL = "https://astronetics.local/api.php";

    public static final ArrayList<Category> CATEGORIES = new ArrayList<Category>() {{
            add(new Category(1, "Планеты солнечной системы"));
            add(new Category(2, "Метеориты и метеоры"));
            add(new Category(3, "Космические явления"));
            add(new Category(4, "Звёзды и созвездия"));
            add(new Category(5, "Чёрные дыры"));
            add(new Category(6, "Астрономические объекты"));
    }};

    public static final ArrayList<Article> ARTICLES = new ArrayList<Article>() {{
        add(new Article(2, "Метеориты", "Общие сведения о метеоритах и их свойствах", "article_2_1", "article_2_1_meteorite"));
        add(new Article(2, "Метеориты - камни с небес", "Как много метеоритов падает на Землю? Для чего используют метеориты?", "article_2_2", "article_2_1_meteorite"));
        add(new Article(2, "Опасность метеоритов", "Что происходит при падении метеорита?", "article_2_3", "article_2_1_meteorite"));
        add(new Article(2, "Состав метеорита", "Химический и минеральный состав метеоритов", "article_2_4", "chemistry"));
        add(new Article(2, "Метеоры", "Общие сведения о метеорах. Историческая справка. Методы исследования метеоров", "article_2_5", "meteor"));

        add(new Article(4, "Звёзды", "Общие сведения о звёздах: что это такое и как они формируются", "article_4_1", "star"));
        add(new Article(4, "Виды звёзд", "Какие бывают звёзды?", "article_4_2", "star"));
        add(new Article(4, "Характеристики звёзд", "Чтобы в полной мере описать звезды, люди пользуются определенными характеристиками", "article_4_3", "star"));
        add(new Article(4, "Созвездия", "Общие сведения о созвездиях и их истории", "article_4_4", "constellation"));
    }};
}
