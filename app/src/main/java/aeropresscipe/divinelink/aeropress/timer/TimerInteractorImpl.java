package aeropresscipe.divinelink.aeropress.timer;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

import aeropresscipe.divinelink.aeropress.base.BaseApplication;
import aeropresscipe.divinelink.aeropress.base.HomeDatabase;
import aeropresscipe.divinelink.aeropress.generaterecipe.DiceDomain;
import aeropresscipe.divinelink.aeropress.generaterecipe.RecipeDao;
import aeropresscipe.divinelink.aeropress.history.HistoryDao;
import aeropresscipe.divinelink.aeropress.history.HistoryDomain;
import aeropresscipe.divinelink.aeropress.savedrecipes.SavedRecipeDao;
import aeropresscipe.divinelink.aeropress.savedrecipes.SavedRecipeDomain;

public class TimerInteractorImpl implements TimerInteractor {

    @Override
    public void saveValues(OnStartTimerFinishListener listener, Context ctx, int bloomTime, int brewTime, boolean isBloomTimer) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = preferences.edit();

        long endTimeBloom = System.currentTimeMillis() + bloomTime * 1000L;
        long endTimeBrew = System.currentTimeMillis() + brewTime * 1000L;

        editor.putLong("endTimeBloom", endTimeBloom);
        editor.putLong("endTimeBrew", endTimeBrew);
        editor.putBoolean("isBloomTimer", isBloomTimer);

        //Test for resuming brew
        BaseApplication.sharedPreferences.setBrewing(true);

        editor.apply();
    }

    @Override
    public void returnValues(OnStartTimerFinishListener listener, Context ctx) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);

        boolean isBloomTimer = preferences.getBoolean("isBloomTimer", true);
        long bloomTimeLeftInMillis = preferences.getLong("endTimeBloom", 0);
        long brewTimeLeftInMillis = preferences.getLong("endTimeBrew", 0);

        long minutes = (bloomTimeLeftInMillis - System.currentTimeMillis()) / 1000 / 60;
        long seconds = (bloomTimeLeftInMillis - System.currentTimeMillis()) / 1000 % 60;

        int endTime = (int) minutes * 60 + (int) seconds;


        if (endTime < 0) {
            if (isBloomTimer) {
                isBloomTimer = false;

                minutes = (brewTimeLeftInMillis - System.currentTimeMillis()) / 1000 / 60;
                seconds = (brewTimeLeftInMillis - System.currentTimeMillis()) / 1000 % 60;

                endTime = (int) minutes * 60 + (int) seconds;
                if (endTime < 0) // Brew Also Finished before resuming
                    listener.onBrewFinished();
                else
                    listener.onSuccess(endTime, isBloomTimer);
            } else
                listener.onBrewFinished();
        } else {
            listener.onSuccess(endTime, isBloomTimer);
        }

    }

    @Override
    public void saveLikedRecipe(final OnSaveLikedRecipeFinishListener listener, final Context ctx) {
        AsyncTask.execute(() -> {
            final RecipeDao recipeDao = HomeDatabase.getDatabase(ctx).recipeDao();
            final SavedRecipeDao savedRecipeDao = HomeDatabase.getDatabase(ctx).savedRecipeDao();
            final HistoryDao historyDao = HomeDatabase.getDatabase(ctx).historyDao();

            final DiceDomain recipe = recipeDao.getSingleRecipe();
            final SavedRecipeDomain currentRecipe = new SavedRecipeDomain(recipe, getCurrentDate());

            final boolean recipeExists = savedRecipeDao.recipeExists(recipe.getId());

            if (!recipeExists) {
                Log.d("Inserted", currentRecipe.toString());
                historyDao.updateRecipe(new HistoryDomain(recipe, getCurrentDate(), true)); // Updates History's corresponding entry's Like Button
                savedRecipeDao.insertLikedRecipe(currentRecipe);
                listener.onSavedRecipe(true);
            } else {
                Log.d("Deleted", currentRecipe.toString());
                historyDao.updateRecipe(new HistoryDomain(recipe, getCurrentDate(), false));
                savedRecipeDao.delete(currentRecipe);
                listener.onSavedRecipe(false);
            }
        });
    }

    @Override
    public void checkIfRecipeIsLikedAndSavedOnDB(final OnSaveLikedRecipeFinishListener listener, final Context ctx) {

        AsyncTask.execute(() -> {
            final RecipeDao recipeDao = HomeDatabase.getDatabase(ctx).recipeDao();
            final SavedRecipeDao savedRecipeDao = HomeDatabase.getDatabase(ctx).savedRecipeDao();
            final HistoryDao historyDao = HomeDatabase.getDatabase(ctx).historyDao();

            final DiceDomain recipe = recipeDao.getSingleRecipe();
            final SavedRecipeDomain currentRecipe = new SavedRecipeDomain(recipe, getCurrentDate());
            final boolean recipeExists = savedRecipeDao.recipeExists(recipe.getId()); //Checks whether current recipe exists on DB

            historyDao.updateRecipe(new HistoryDomain(recipe, getCurrentDate(), recipeExists)); // Updates History's Like Button Status

            Log.d("Current Recipe: ", currentRecipe.toString());
            listener.onSavedRecipe(recipeExists);
        });
    }

    @Override
    public void addRecipeToHistory(final Context ctx) {
        AsyncTask.execute(() -> {
            final HistoryDao historyDao = HomeDatabase.getDatabase(ctx).historyDao();
            final RecipeDao recipeDao = HomeDatabase.getDatabase(ctx).recipeDao();
            final SavedRecipeDao savedRecipeDao = HomeDatabase.getDatabase(ctx).savedRecipeDao();

            final DiceDomain recipe = recipeDao.getSingleRecipe();
            final boolean recipeExists = savedRecipeDao.recipeExists(recipe.getId()); //Checks whether current recipe exists on DB

            final HistoryDomain currentRecipe = new HistoryDomain(recipe, getCurrentDate(), recipeExists);

            historyDao.insertRecipeToHistory(currentRecipe);
        });
    }

    public String getCurrentDate() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("d MMMM yyyy");

        return formatter.format(date.getTime());
    }

}
