# Smart Pantry Manager

A Java Android application that suggests recipes based **strictly** on the ingredients
you already have at home, to help cut food waste. No shopping trip required: a recipe is
only suggested when every ingredient it needs is in your pantry, in at least the required quantity.

Mobile App Development 700 - Practical Assignment.

## Features

| Screen | What it does |
|---|---|
| **Pantry list** (`MainActivity`) | Shows all pantry items from SQLite in a `RecyclerView`. Tap an item to edit, tap *Delete* to remove, tap **+** to add. Highlights items that are expired or expiring within 3 days. |
| **Add / Edit Ingredient** (`AddEditItemActivity`) | Create, update and delete a pantry item (name, quantity, unit, optional expiry date) with full input validation. |
| **Suggested Recipes** (`SuggestedRecipesActivity`) | Runs the strict-matching algorithm. *Can cook now* lists only fully-matched recipes. A separate *Almost there* tab (bonus) lists recipes missing exactly one ingredient. Shows a friendly message when nothing matches. |
| **Recipe Detail** (`RecipeDetailActivity`) | Full ingredient list (ticked / crossed against your pantry) and the method. |
| **Settings** (`SettingsActivity`) | Expiring-soon alerts toggle, preferred units (metric / imperial) and a *Clear pantry* button. |

Navigation uses a bottom navigation bar plus explicit `Intent`s (with extras for the item / recipe id).

## Database choice: SQLite (`SQLiteOpenHelper`)

I chose **local SQLite** because:

* the app is single-user and fully offline - the pantry never needs to sync between devices;
* it needs no account, API key, server or internet connection, so it always runs and is easy to mark;
* it is built into Android and is the approach taught in the module's persistent-data chapter;
* a relational model suits the data (a recipe has many ingredient rows).

Tables: `pantry_item` (user data, full CRUD), `recipe` and `recipe_ingredient` (22 recipes seeded on the first run
in `PantryDbHelper.onCreate`). The database file lives in the app's private storage, so data persists after the app is closed and reopened.

## The strict-matching rule

`IngredientMatcher.strictMatches(recipes, pantry)` returns a recipe **only if every ingredient** is in the pantry
in at least the required quantity. A recipe needing 5 ingredients when you own 4 is never suggested.

To cope with real-world messiness it:

* normalises names (`NameNormalizer`): case, plurals (`tomato` = `tomatoes`, `berries` = `berry`), filler words
  (`fresh large eggs` = `egg`) and a small synonym list (`yoghurt` = `yogurt`, `scallion` = `spring onion`);
* converts units (`UnitConverter`): `1 kg` satisfies `500 g`, `2 cups flour` can be compared with grams, and `500 g tomato`
  can be compared with a count of tomatoes using average piece weights;
* adds up several pantry rows of the same ingredient (2 eggs + 3 eggs = 5 eggs);
* never guesses in the user's favour: if two units cannot be compared (e.g. grams of tuna vs. a can), the recipe is excluded.

This logic is plain Java with no Android dependencies, so it is covered by JVM unit tests.

## Project structure

```
app/src/main/java/com/smartpantry/app/
  model/   PantryItem, Recipe, RecipeIngredient
  data/    PantryDbHelper (SQLite), SeedData (22 recipes), AppPrefs (settings)
  logic/   NameNormalizer, UnitConverter, IngredientMatcher, MatchResult, IngredientCheck,
           QuantityFormatter, ExpiryUtils
  ui/      MainActivity, AddEditItemActivity, SuggestedRecipesActivity, RecipeDetailActivity,
           SettingsActivity, BaseActivity (bottom nav), PantryAdapter, RecipeAdapter
app/src/test/   JUnit tests for the logic and seed data
```

## Setup and run

Requirements: **Android Studio Hedgehog (2023.1.1) or newer** (bundled JDK 17), Android SDK 34, an emulator or phone running Android 7.0 (API 24) or newer.

1. Clone the repository: `git clone <your-repo-url>`
2. In Android Studio choose **File > Open** and select the `SmartPantryManager` folder.
3. Wait for the Gradle sync to finish (first sync downloads dependencies, internet required).
4. Pick an emulator / device and press **Run** (green triangle).
5. Run the unit tests: right-click `app/src/test` > **Run Tests**, or `./gradlew test` in a terminal.

No permissions are required. The app uses **no** maps, GPS or location services.

## Branching used during development

Development used three branches:

* `main` - project setup, theme and resources, data models, seeded recipes and the SQLite helper;
* `feature/matching-logic` - name normalisation, unit conversion and the strict-matching algorithm with unit tests;
* `feature/ui-screens` - the Add/Edit, Pantry list, Recipe detail, Suggested Recipes and Settings screens, bottom navigation and this README.

Each feature branch was merged back into `main` with a merge commit.
