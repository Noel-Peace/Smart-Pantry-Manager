package com.smartpantry.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.smartpantry.app.model.PantryItem;
import com.smartpantry.app.model.Recipe;
import com.smartpantry.app.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Local SQLite database (via SQLiteOpenHelper).
 *
 * <ul>
 *   <li>pantry_item        - what the user owns (full CRUD from the UI)</li>
 *   <li>recipe             - the seeded recipe collection</li>
 *   <li>recipe_ingredient  - the ingredients of each recipe (many rows per recipe)</li>
 * </ul>
 *
 * The recipes are inserted once, in {@link #onCreate}, i.e. on the very first run.
 * Data lives in a file on the device, so it is still there after the app is closed and reopened.
 */
public class PantryDbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "smart_pantry.db";
    public static final int DB_VERSION = 1;

    // table + column names
    public static final String T_PANTRY = "pantry_item";
    public static final String T_RECIPE = "recipe";
    public static final String T_INGREDIENT = "recipe_ingredient";

    public static final String C_ID = "_id";
    public static final String C_NAME = "name";
    public static final String C_QUANTITY = "quantity";
    public static final String C_UNIT = "unit";
    public static final String C_EXPIRY = "expiry_date";
    public static final String C_STEPS = "steps";
    public static final String C_RECIPE_ID = "recipe_id";

    private static PantryDbHelper instance;

    /** One shared helper for the whole app (avoids several helpers fighting over one file). */
    public static synchronized PantryDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new PantryDbHelper(context.getApplicationContext());
        }
        return instance;
    }

    private PantryDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true); // makes ON DELETE CASCADE work
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_PANTRY + " ("
                + C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + C_NAME + " TEXT NOT NULL, "
                + C_QUANTITY + " REAL NOT NULL, "
                + C_UNIT + " TEXT NOT NULL, "
                + C_EXPIRY + " TEXT)");
        createRecipeTables(db);
        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Recipes are reference data: rebuild them, but never throw away the user's pantry.
        db.execSQL("DROP TABLE IF EXISTS " + T_INGREDIENT);
        db.execSQL("DROP TABLE IF EXISTS " + T_RECIPE);
        createRecipeTables(db);
        seedRecipes(db);
    }

    private void createRecipeTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_RECIPE + " ("
                + C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + C_NAME + " TEXT NOT NULL, "
                + C_STEPS + " TEXT NOT NULL)");
        db.execSQL("CREATE TABLE " + T_INGREDIENT + " ("
                + C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + C_RECIPE_ID + " INTEGER NOT NULL, "
                + C_NAME + " TEXT NOT NULL, "
                + C_QUANTITY + " REAL NOT NULL, "
                + C_UNIT + " TEXT NOT NULL, "
                + "FOREIGN KEY(" + C_RECIPE_ID + ") REFERENCES " + T_RECIPE + "(" + C_ID
                + ") ON DELETE CASCADE)");
    }

    /** Inserts the pre-loaded recipe collection (see {@link SeedData}). */
    private void seedRecipes(SQLiteDatabase db) {
        for (Recipe recipe : SeedData.recipes()) {
            ContentValues rv = new ContentValues();
            rv.put(C_NAME, recipe.getName());
            rv.put(C_STEPS, recipe.getSteps());
            long recipeId = db.insert(T_RECIPE, null, rv);

            for (RecipeIngredient ingredient : recipe.getIngredients()) {
                ContentValues iv = new ContentValues();
                iv.put(C_RECIPE_ID, recipeId);
                iv.put(C_NAME, ingredient.getName());
                iv.put(C_QUANTITY, ingredient.getQuantity());
                iv.put(C_UNIT, ingredient.getUnit());
                db.insert(T_INGREDIENT, null, iv);
            }
        }
    }

    // ------------------------------------------------------------------ pantry CRUD

    /** CREATE: returns the new row id, or -1 on failure. */
    public long addPantryItem(PantryItem item) {
        return getWritableDatabase().insert(T_PANTRY, null, toValues(item));
    }

    /** UPDATE: returns the number of rows changed (1 on success). */
    public int updatePantryItem(PantryItem item) {
        return getWritableDatabase().update(T_PANTRY, toValues(item),
                C_ID + " = ?", new String[]{String.valueOf(item.getId())});
    }

    /** DELETE: returns the number of rows removed. */
    public int deletePantryItem(long id) {
        return getWritableDatabase().delete(T_PANTRY,
                C_ID + " = ?", new String[]{String.valueOf(id)});
    }

    /** Removes every pantry item (used by Settings > Clear pantry). */
    public void deleteAllPantryItems() {
        getWritableDatabase().delete(T_PANTRY, null, null);
    }

    /** READ (list): all pantry items, alphabetical. */
    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(T_PANTRY, null, null, null, null, null,
                C_NAME + " COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                items.add(cursorToItem(c));
            }
        }
        return items;
    }

    /** READ (single): null if no item has that id. */
    public PantryItem getPantryItem(long id) {
        try (Cursor c = getReadableDatabase().query(T_PANTRY, null,
                C_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? cursorToItem(c) : null;
        }
    }

    private ContentValues toValues(PantryItem item) {
        ContentValues values = new ContentValues();
        values.put(C_NAME, item.getName());
        values.put(C_QUANTITY, item.getQuantity());
        values.put(C_UNIT, item.getUnit());
        String expiry = item.getExpiryDate();
        values.put(C_EXPIRY, expiry); // null clears the date
        return values;
    }

    private PantryItem cursorToItem(Cursor c) {
        int expiryIndex = c.getColumnIndexOrThrow(C_EXPIRY);
        return new PantryItem(
                c.getLong(c.getColumnIndexOrThrow(C_ID)),
                c.getString(c.getColumnIndexOrThrow(C_NAME)),
                c.getDouble(c.getColumnIndexOrThrow(C_QUANTITY)),
                c.getString(c.getColumnIndexOrThrow(C_UNIT)),
                c.isNull(expiryIndex) ? null : c.getString(expiryIndex));
    }

    // ------------------------------------------------------------------ recipes (read only)

    /** All recipes with their ingredients (2 queries in total, not one per recipe). */
    public List<Recipe> getAllRecipes() {
        SQLiteDatabase db = getReadableDatabase();

        Map<Long, List<RecipeIngredient>> ingredientsByRecipe = new HashMap<>();
        try (Cursor c = db.query(T_INGREDIENT, null, null, null, null, null, C_ID + " ASC")) {
            while (c.moveToNext()) {
                long recipeId = c.getLong(c.getColumnIndexOrThrow(C_RECIPE_ID));
                List<RecipeIngredient> list = ingredientsByRecipe.get(recipeId);
                if (list == null) {
                    list = new ArrayList<>();
                    ingredientsByRecipe.put(recipeId, list);
                }
                list.add(cursorToIngredient(c));
            }
        }

        List<Recipe> recipes = new ArrayList<>();
        try (Cursor c = db.query(T_RECIPE, null, null, null, null, null,
                C_NAME + " COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                recipes.add(cursorToRecipe(c, ingredientsByRecipe));
            }
        }
        return recipes;
    }

    /** One recipe with its ingredients, or null if the id does not exist. */
    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        List<RecipeIngredient> ingredients = new ArrayList<>();
        try (Cursor c = db.query(T_INGREDIENT, null, C_RECIPE_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, C_ID + " ASC")) {
            while (c.moveToNext()) {
                ingredients.add(cursorToIngredient(c));
            }
        }
        try (Cursor c = db.query(T_RECIPE, null, C_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            if (!c.moveToFirst()) {
                return null;
            }
            return new Recipe(
                    c.getLong(c.getColumnIndexOrThrow(C_ID)),
                    c.getString(c.getColumnIndexOrThrow(C_NAME)),
                    c.getString(c.getColumnIndexOrThrow(C_STEPS)),
                    ingredients);
        }
    }

    private Recipe cursorToRecipe(Cursor c, Map<Long, List<RecipeIngredient>> ingredientsByRecipe) {
        long id = c.getLong(c.getColumnIndexOrThrow(C_ID));
        List<RecipeIngredient> ingredients = ingredientsByRecipe.get(id);
        if (ingredients == null) {
            ingredients = new ArrayList<>();
        }
        return new Recipe(id,
                c.getString(c.getColumnIndexOrThrow(C_NAME)),
                c.getString(c.getColumnIndexOrThrow(C_STEPS)),
                ingredients);
    }

    private RecipeIngredient cursorToIngredient(Cursor c) {
        return new RecipeIngredient(
                c.getString(c.getColumnIndexOrThrow(C_NAME)),
                c.getDouble(c.getColumnIndexOrThrow(C_QUANTITY)),
                c.getString(c.getColumnIndexOrThrow(C_UNIT)));
    }
}
