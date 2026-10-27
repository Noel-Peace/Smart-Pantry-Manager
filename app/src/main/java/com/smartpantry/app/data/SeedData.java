package com.smartpantry.app.data;

import com.smartpantry.app.model.Recipe;
import com.smartpantry.app.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * The 22 recipes that are pre-loaded into the database the first time the app runs.
 * Pure Java (no Android classes) so the recipes can be unit-tested too.
 */
public final class SeedData {

    private SeedData() {
    }

    private static RecipeIngredient ing(String name, double quantity, String unit) {
        return new RecipeIngredient(name, quantity, unit);
    }

    private static Recipe recipe(String name, String steps, RecipeIngredient... ingredients) {
        List<RecipeIngredient> list = new ArrayList<>();
        for (RecipeIngredient i : ingredients) {
            list.add(i);
        }
        return new Recipe(-1, name, steps, list);
    }

    public static List<Recipe> recipes() {
        List<Recipe> r = new ArrayList<>();

        r.add(recipe("Scrambled Eggs on Toast",
                "Toast the bread.\n"
                        + "Crack the eggs into a bowl and whisk with a fork.\n"
                        + "Melt the butter in a pan over medium heat.\n"
                        + "Pour in the eggs and stir gently until just set.\n"
                        + "Pile onto the toast and serve immediately.",
                ing("eggs", 3, "piece"), ing("bread", 2, "slice"), ing("butter", 10, "g")));

        r.add(recipe("Cheese Omelette",
                "Whisk the eggs in a bowl.\n"
                        + "Melt the butter in a non-stick pan over medium heat.\n"
                        + "Pour in the eggs and let them set for about a minute.\n"
                        + "Sprinkle the cheese over one half of the omelette.\n"
                        + "Fold the omelette over, cook for 1 more minute and serve.",
                ing("eggs", 3, "piece"), ing("cheese", 40, "g"), ing("butter", 10, "g")));

        r.add(recipe("Tomato Garlic Pasta",
                "Boil the pasta in salted water until al dente, then drain.\n"
                        + "Chop the tomatoes and crush the garlic.\n"
                        + "Heat the olive oil and fry the garlic for 30 seconds.\n"
                        + "Add the tomatoes and the salt and simmer for 10 minutes.\n"
                        + "Toss the pasta through the sauce and serve.",
                ing("pasta", 200, "g"), ing("tomatoes", 4, "piece"), ing("garlic", 2, "clove"),
                ing("olive oil", 15, "ml"), ing("salt", 1, "tsp")));

        r.add(recipe("Egg Fried Rice",
                "Cook the rice and let it cool.\n"
                        + "Chop the onion finely.\n"
                        + "Heat the oil in a wok and fry the onion until soft.\n"
                        + "Push the onion aside, scramble the eggs in the wok.\n"
                        + "Add the rice and soy sauce and stir-fry for 3 minutes.",
                ing("rice", 200, "g"), ing("eggs", 2, "piece"), ing("onion", 1, "piece"),
                ing("soy sauce", 15, "ml"), ing("oil", 15, "ml")));

        r.add(recipe("Fluffy Pancakes",
                "Mix the flour and sugar in a bowl.\n"
                        + "Whisk in the milk and eggs until you have a smooth batter.\n"
                        + "Melt a little butter in a pan over medium heat.\n"
                        + "Pour in a ladle of batter and cook until bubbles appear.\n"
                        + "Flip, cook for another minute and repeat with the rest of the batter.",
                ing("flour", 200, "g"), ing("milk", 300, "ml"), ing("eggs", 2, "piece"),
                ing("sugar", 20, "g"), ing("butter", 20, "g")));

        r.add(recipe("Creamy Mashed Potatoes",
                "Peel the potatoes and cut them into chunks.\n"
                        + "Boil in salted water for 15-20 minutes until very soft.\n"
                        + "Drain well and return to the pot.\n"
                        + "Add the butter, warm milk and salt.\n"
                        + "Mash until smooth and creamy.",
                ing("potatoes", 500, "g"), ing("butter", 30, "g"), ing("milk", 60, "ml"),
                ing("salt", 1, "tsp")));

        r.add(recipe("Hearty Vegetable Soup",
                "Peel and chop the carrots, potatoes and onion.\n"
                        + "Soften the onion in a large pot for 5 minutes.\n"
                        + "Add the carrots, potatoes and vegetable stock.\n"
                        + "Simmer for 25 minutes until the vegetables are tender.\n"
                        + "Blend partly for a thicker soup, or serve chunky.",
                ing("carrots", 2, "piece"), ing("potatoes", 2, "piece"), ing("onion", 1, "piece"),
                ing("vegetable stock", 500, "ml")));

        r.add(recipe("Cheese Toastie",
                "Butter one side of each slice of bread.\n"
                        + "Place the cheese between the slices, buttered sides out.\n"
                        + "Toast in a hot pan for 2-3 minutes per side until golden.\n"
                        + "Let it cool for a minute, then cut in half.",
                ing("bread", 2, "slice"), ing("cheese", 50, "g"), ing("butter", 10, "g")));

        r.add(recipe("Banana Honey Porridge",
                "Combine the oats and milk in a small pot.\n"
                        + "Cook over medium heat for 5 minutes, stirring often.\n"
                        + "Slice the banana.\n"
                        + "Pour the porridge into a bowl and top with the banana.\n"
                        + "Drizzle with the honey.",
                ing("oats", 80, "g"), ing("milk", 250, "ml"), ing("banana", 1, "piece"),
                ing("honey", 15, "ml")));

        r.add(recipe("Chickpea Curry",
                "Chop the onion and crush the garlic.\n"
                        + "Fry the onion in a pot until soft, then add the garlic and curry powder.\n"
                        + "Add the chopped tomatoes and cook for 5 minutes.\n"
                        + "Stir in the drained chickpeas and simmer for 15 minutes.\n"
                        + "Serve with rice or bread.",
                ing("chickpeas", 400, "g"), ing("tomatoes", 2, "piece"), ing("onion", 1, "piece"),
                ing("garlic", 2, "clove"), ing("curry powder", 2, "tsp")));

        r.add(recipe("Lentil Stew",
                "Rinse the lentils.\n"
                        + "Chop the carrots and onion and crush the garlic.\n"
                        + "Fry the onion, carrots and garlic for 5 minutes.\n"
                        + "Add the lentils and vegetable stock.\n"
                        + "Simmer for 30 minutes until the lentils are soft.",
                ing("lentils", 200, "g"), ing("carrots", 2, "piece"), ing("onion", 1, "piece"),
                ing("garlic", 2, "clove"), ing("vegetable stock", 500, "ml")));

        r.add(recipe("Simple Guacamole",
                "Halve the avocados, remove the stones and scoop the flesh into a bowl.\n"
                        + "Mash with a fork, leaving it slightly chunky.\n"
                        + "Finely chop the onion and stir it in.\n"
                        + "Squeeze in the lemon juice and add the salt.\n"
                        + "Taste and adjust, then serve with bread or chips.",
                ing("avocados", 2, "piece"), ing("lemon", 1, "piece"), ing("onion", 0.5, "piece"),
                ing("salt", 1, "tsp")));

        r.add(recipe("Tuna Pasta Salad",
                "Cook the pasta, drain and rinse under cold water.\n"
                        + "Drain the tuna and flake it into a bowl.\n"
                        + "Add the cooled pasta and the mayonnaise.\n"
                        + "Mix well and chill for 10 minutes before serving.",
                ing("tuna", 1, "can"), ing("pasta", 150, "g"), ing("mayonnaise", 60, "g")));

        r.add(recipe("Chicken Stir-Fry",
                "Slice the chicken, bell pepper and onion into strips.\n"
                        + "Heat the oil in a wok until very hot.\n"
                        + "Fry the chicken for 5 minutes until golden and cooked through.\n"
                        + "Add the pepper and onion and stir-fry for 3 minutes.\n"
                        + "Pour in the soy sauce, toss and serve.",
                ing("chicken breast", 300, "g"), ing("bell pepper", 1, "piece"),
                ing("onion", 1, "piece"), ing("soy sauce", 30, "ml"), ing("oil", 15, "ml")));

        r.add(recipe("Spaghetti Bolognese",
                "Chop the onion and tomatoes and crush the garlic.\n"
                        + "Brown the ground beef in a pan, then add the onion and garlic.\n"
                        + "Add the tomatoes and simmer the sauce for 25 minutes.\n"
                        + "Cook the spaghetti until al dente and drain.\n"
                        + "Serve the sauce on top of the spaghetti.",
                ing("spaghetti", 200, "g"), ing("ground beef", 250, "g"), ing("tomatoes", 4, "piece"),
                ing("onion", 1, "piece"), ing("garlic", 2, "clove")));

        r.add(recipe("French Toast",
                "Whisk the eggs and milk together in a shallow dish.\n"
                        + "Soak each slice of bread in the mixture for 20 seconds per side.\n"
                        + "Melt the butter in a pan over medium heat.\n"
                        + "Fry the bread for 2-3 minutes per side until golden.",
                ing("bread", 4, "slice"), ing("eggs", 2, "piece"), ing("milk", 100, "ml"),
                ing("butter", 10, "g")));

        r.add(recipe("Loaded Baked Potato",
                "Heat the oven to 200 C.\n"
                        + "Prick the potatoes with a fork and bake for 60 minutes.\n"
                        + "Cut a cross in the top of each potato and squeeze open.\n"
                        + "Add the butter and grated cheese and return to the oven for 5 minutes.",
                ing("potatoes", 2, "piece"), ing("cheese", 60, "g"), ing("butter", 20, "g")));

        r.add(recipe("Peanut Butter Banana Sandwich",
                "Spread the peanut butter on both slices of bread.\n"
                        + "Slice the banana and arrange it on one slice.\n"
                        + "Close the sandwich, press down gently and cut in half.",
                ing("bread", 2, "slice"), ing("peanut butter", 30, "g"), ing("banana", 1, "piece")));

        r.add(recipe("Yoghurt Oat Parfait",
                "Spoon half of the yoghurt into a glass.\n"
                        + "Add half of the oats and drizzle with some honey.\n"
                        + "Slice the banana and add a layer.\n"
                        + "Repeat the layers with the remaining ingredients and serve.",
                ing("yoghurt", 250, "g"), ing("oats", 40, "g"), ing("honey", 15, "ml"),
                ing("banana", 1, "piece")));

        r.add(recipe("Mushroom Omelette",
                "Slice the mushrooms.\n"
                        + "Melt half of the butter and fry the mushrooms until golden, then set aside.\n"
                        + "Whisk the eggs and cook them in the rest of the butter.\n"
                        + "Add the mushrooms and cheese, fold over and serve.",
                ing("eggs", 3, "piece"), ing("mushrooms", 100, "g"), ing("butter", 10, "g"),
                ing("cheese", 30, "g")));

        r.add(recipe("Roasted Tomato Soup",
                "Chop the tomatoes and onion and crush the garlic.\n"
                        + "Melt the butter in a pot and soften the onion and garlic.\n"
                        + "Add the tomatoes and the vegetable stock.\n"
                        + "Simmer for 20 minutes, then blend until smooth.",
                ing("tomatoes", 6, "piece"), ing("onion", 1, "piece"), ing("garlic", 2, "clove"),
                ing("vegetable stock", 500, "ml"), ing("butter", 20, "g")));

        r.add(recipe("Garlic Butter Toast",
                "Soften the butter and mash it with the crushed garlic.\n"
                        + "Spread the garlic butter over the slices of bread.\n"
                        + "Bake at 200 C for 8 minutes until golden and crisp.",
                ing("bread", 4, "slice"), ing("butter", 40, "g"), ing("garlic", 3, "clove")));

        return r;
    }
}
