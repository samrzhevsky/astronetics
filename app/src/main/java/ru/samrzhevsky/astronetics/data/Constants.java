package ru.samrzhevsky.astronetics.data;

import java.util.ArrayList;

public class Constants {
    public static final int QUESTIONS_IN_TEST = 10;
    public static final int PASSING_SCORE = 6;
    public static final String API_URL = "https://astronetics.local/api.php";

    public static final ArrayList<Category> CATEGORIES = new ArrayList<Category>() {{
            add(new Category(1, "Планеты Солнечной системы"));
            add(new Category(2, "Метеориты и метеоры"));
            add(new Category(3, "Космические явления"));
            add(new Category(4, "Звёзды и созвездия"));
            add(new Category(5, "Чёрные дыры"));
    }};

    public static final ArrayList<Article> ARTICLES = new ArrayList<Article>() {{
        add(new Article(1, "Меркурий", "Наименьшая планета Солнечной системы и самая близкая к Солнцу", "article_1_1", "mercury"));
        add(new Article(1, "Венера", "Ближайшая к Земле планета, вторая по удалённости от Солнца и шестая по размеру планета Солнечной системы", "article_1_2", "venus"));
        add(new Article(1, "Земля", "Единственное известное в настоящее время тело во Вселенной, населённое живыми организмами", "article_1_3", "earth"));
        add(new Article(1, "Марс", "Четвёртая по удалённости от Солнца и седьмая по размеру планета Солнечной системы", "article_1_4", "mars"));
        add(new Article(1, "Юпитер", "Крупнейшая планета Солнечной системы, газовый гигант", "article_1_5", "jupiter"));
        add(new Article(1, "Сатурн", "Шестая планета по удалённости от Солнца, обладает заметной системой колец", "article_1_6", "saturn"));
        add(new Article(1, "Уран", "Седьмая планета по удалённости от Солнца, третья по диаметру и четвёртая по массе", "article_1_7", "uranus"));
        add(new Article(1, "Нептун", "Восьмая и самая дальняя от Солнца планета Солнечной системы", "article_1_8", "neptune"));

        add(new Article(2, "Метеориты", "Общие сведения о метеоритах и их свойствах", "article_2_1", "article_2_1_meteorite"));
        add(new Article(2, "Метеориты - камни с небес", "Как много метеоритов падает на Землю? Для чего используют метеориты?", "article_2_2", "article_2_1_meteorite"));
        add(new Article(2, "Опасность метеоритов", "Что происходит при падении метеорита?", "article_2_3", "article_2_1_meteorite"));
        add(new Article(2, "Состав метеорита", "Химический и минеральный состав метеоритов", "article_2_4", "chemistry"));
        add(new Article(2, "Метеоры", "Общие сведения о метеорах. Историческая справка. Методы исследования метеоров", "article_2_5", "meteor"));

        add(new Article(3, "Затмения", "Явления, возникающие когда один объект заслоняет собой свет от другого объекта", "article_3_1", "eclipse"));
        add(new Article(3, "Великий аттрактор", "Гравитационная аномалия, находящаяся на расстоянии около 250 млн. световых лет от Земли", "article_3_2", "attractor"));
        add(new Article(3, "Космические пустоты (войды)", "Области между галактическими нитями, в которых отсутствуют или почти отсутствуют галактики и скопления", "article_3_3", "boovoid"));
        add(new Article(3, "Туманность Бумеранг", "Протопланетарная туманность, самый холодный объект во Вселенной", "article_3_4", "boomerang"));
        add(new Article(3, "Квазары", "Класс астрономических объектов, являющихся одними из самых ярких в видимой Вселенной", "article_3_5", "quasar"));
        add(new Article(3, "Солнечные вспышки", "Процесс высвобождения огромного количества энергии и частиц Солнцем", "article_3_6", "solarflare"));

        add(new Article(4, "Звёзды", "Общие сведения о звёздах: что это такое и как они формируются", "article_4_1", "star"));
        add(new Article(4, "Виды звёзд", "Какие бывают звёзды?", "article_4_2", "star"));
        add(new Article(4, "Характеристики звёзд", "Чтобы в полной мере описать звезды, люди пользуются определенными характеристиками", "article_4_3", "star"));
        add(new Article(4, "Созвездия", "Общие сведения о созвездиях и их истории", "article_4_4", "constellation"));

        add(new Article(5, "Общие сведения", "Что такое чёрная дыра? Как они появляются? Как исчезают?", "article_5_1", "blackhole"));
        add(new Article(5, "История", "Почему чёрные дыры так называются?", "article_5_2", "blackhole"));
        add(new Article(5, "Характеристики", "Структура и физика черных дыр", "article_5_3", "blackhole"));
        add(new Article(5, "Виды чёрных дыр", "Четыре вида черных дыр, обладающих определенными особенностями", "article_5_4", "blackhole"));
        add(new Article(5, "Сингулярность", "Область, где кривизна пространства-времени становится бесконечной", "article_5_5", "blackhole"));
        add(new Article(5, "Белая дыра", "Полная противоположность чёрной", "article_5_6", "blackhole"));
    }};
}
