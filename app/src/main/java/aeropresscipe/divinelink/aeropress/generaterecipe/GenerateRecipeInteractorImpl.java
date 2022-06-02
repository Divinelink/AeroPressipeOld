package aeropresscipe.divinelink.aeropress.generaterecipe;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;

import aeropresscipe.divinelink.aeropress.base.BaseApplication;
import aeropresscipe.divinelink.aeropress.base.HomeDatabase;

public class GenerateRecipeInteractorImpl implements GenerateRecipeInteractor, SharedPrefManager {
    //We only want to edit out data in the interactor, before we pass them into the Presenter
    // otherwise we break the MVP rules


    @Override
    public void getNewRecipe(final OnGenerateRecipeFinishListener listener, final Context ctx, boolean letGenerate) {
        // Executed By Pressing "New Recipe Button"

        // Check whether to show generate a new recipe or not.
        // If there's a recipe running already, show a toast that asks the user to long press on the button in order to get a new recipe.
        // When long-pressing, letGenerate is true, and isBrewing becomes false, then the model passes the new recipe on the presenter etc.
        boolean isBrewing = IsItBrewingAlready(ctx);

        if (letGenerate)
            isBrewing = false;

        if (isBrewing) {
            listener.isAlreadyBrewing();
        } else {
            AsyncTask.execute(() -> {
                final RecipeDao recipeDao = HomeDatabase.getDatabase(ctx).recipeDao();

                final DiceDomain newRecipe = new GenerateRecipe().getFinalRecipe();

                // When Using AsyncTask, we also need to use .runOnUiThread(new Runnable()) where we set the adapter
                // That updates the UI. In this case, on the fragment, on showRecipe() method.

                // Stuff that updates the UI
                Log.d("getNewRecipe", "Get New Recipe Button, updates DB and creates new recipe");
                AsyncTask.execute(() -> {
                    recipeDao.updateRecipe(newRecipe);
                    listener.onSuccessNewRecipe(newRecipe);
                });

                BaseApplication.sharedPreferences.setBrewing(false);
            });
        }
    }

    @Override
    public void getRecipe(final OnGenerateRecipeFinishListener listener, final Context ctx) {

        final boolean isBrewing = IsItBrewingAlready(ctx);

        AsyncTask.execute(() -> {
            final RecipeDao recipeDao = HomeDatabase.getDatabase(ctx).recipeDao();
            final DiceDomain recipe = recipeDao.getSingleRecipe();

            if (recipe == null) {
                // If it's the first time we run the app, there's no recipe. We generate a new one using the getRandomRecipe() method

                final DiceDomain newRecipe = new GenerateRecipe().getFinalRecipe();

                // When DB is empty, meaning it has no recipe, it automatically saves the current recipe on the DB
                // And we show it on the fragment.
                AsyncTask.execute(() -> {
                    Log.d("getRecipe", "Creates New Recipe when app starts and there's no recipe available.");
                    recipeDao.updateRecipe(newRecipe);
                    listener.onSuccessAppStarts(newRecipe);
                });
            } else {
                Log.d("Show Recipe", "Show already existing recipe");
                AsyncTask.execute(() -> {
                    if (isBrewing) // When there's a timer active and app is starting
                        listener.onSuccess(recipe);
                    else // When there's no timer active and app is starting
                        listener.onSuccessAppStarts(recipe);
                });
            }
        });
    }


    @Override
    public boolean IsItBrewingAlready(Context ctx) {
        return BaseApplication.sharedPreferences.isBrewing();
    }
}
