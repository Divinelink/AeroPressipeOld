package aeropresscipe.divinelink.aeropress.timer;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.List;

import aeropresscipe.divinelink.aeropress.base.HomeDatabase;
import aeropresscipe.divinelink.aeropress.generaterecipe.DiceDomain;
import aeropresscipe.divinelink.aeropress.generaterecipe.RecipeDao;

public class TimerInteractorImpl implements TimerInteractor {

    @Override
    public void saveValues(OnStartTimerFinishListener listener, Context ctx, int bloomTime, int brewTime, boolean isBloomTimer) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = preferences.edit();

        long endTimeBloom = System.currentTimeMillis() + bloomTime * 1000;
        long endTimeBrew = System.currentTimeMillis() + brewTime * 1000;

        editor.putLong("endTimeBloom", endTimeBloom);
        editor.putLong("endTimeBrew", endTimeBrew);
        editor.putBoolean("isBloomTimer", isBloomTimer);

        //Test for resuming brew
        editor.putBoolean("isBrewing", true);

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
                    //TODO add message to ask user either to go back or "Possible Feature": Add recipe to Liked Recipes.
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

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final RecipeDao recipeDao = HomeDatabase.getDatabase(ctx).recipeDao();
                final DiceDomain recipe = recipeDao.getRecipe();

                final LikedRecipeDao likedRecipeDao = HomeDatabase.getDatabase(ctx).likedRecipeDao();

                List<LikedRecipeDomain> myData = likedRecipeDao.getLikedRecipes();

                System.out.println(myData.size());

                LikedRecipeDomain currentRecipe = new LikedRecipeDomain(
                        recipe.getDiceTemperature(),
                        recipe.getGroundSize(),
                        recipe.getBrewTime(),
                        recipe.getBrewingMethod(),
                        recipe.getBloomTime(),
                        recipe.getBloomWater(),
                        recipe.getBrewWaterAmount(),
                        recipe.getCoffeeAmount());

                if (!checkIfRecipeExistsInDatabase(myData, currentRecipe)) {
                    // Add current recipe to LikedRecipes DB
                    likedRecipeDao.insertLikedRecipe(currentRecipe);
                    Log.d("Inserted", currentRecipe.toString());
                    listener.onSuccessSave(true);
                }
                else
                {
                    likedRecipeDao.deleteById( recipe.getDiceTemperature(),
                            recipe.getGroundSize(),
                            recipe.getBrewTime(),
                            recipe.getBrewingMethod(),
                            recipe.getBloomTime(),
                            recipe.getBloomWater(),
                            recipe.getBrewWaterAmount(),
                            recipe.getCoffeeAmount());
                    Log.d("Deleted", currentRecipe.toString());
                    listener.onSuccessSave(false);
                }
            }
        });
    }

    @Override
    public void checkIfRecipeExists(final OnSaveLikedRecipeFinishListener listener, final Context ctx) {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final RecipeDao recipeDao = HomeDatabase.getDatabase(ctx).recipeDao();
                final DiceDomain recipe = recipeDao.getRecipe();

                final LikedRecipeDao likedRecipeDao = HomeDatabase.getDatabase(ctx).likedRecipeDao();
                List<LikedRecipeDomain> myData = likedRecipeDao.getLikedRecipes();

                LikedRecipeDomain currentRecipe = new LikedRecipeDomain(
                        recipe.getDiceTemperature(),
                        recipe.getGroundSize(),
                        recipe.getBrewTime(),
                        recipe.getBrewingMethod(),
                        recipe.getBloomTime(),
                        recipe.getBloomWater(),
                        recipe.getBrewWaterAmount(),
                        recipe.getCoffeeAmount());

                if (checkIfRecipeExistsInDatabase(myData, currentRecipe)) {
                    listener.onRecipeFound(true);
                    Log.d("Recipe Exists in DB?", "ΥΕΣ!!!");
                } else {
                    listener.onRecipeFound(false);
                    Log.d("Recipe Exists in DB?", "ANLAKI!!!");
                }
            }
        });
    }

    public boolean checkIfRecipeExistsInDatabase(List<LikedRecipeDomain> myData, LikedRecipeDomain currentRecipe) {
        boolean recipeFound = false;
        for (LikedRecipeDomain dataInDB : myData) {
             recipeFound = dataInDB.getDiceTemperature() == currentRecipe.getDiceTemperature() &&
                    dataInDB.getGroundSize().equals(currentRecipe.getGroundSize()) &&
                    dataInDB.getBrewTime() == currentRecipe.getBrewTime() &&
                    dataInDB.getBrewingMethod().equals(currentRecipe.getBrewingMethod()) &&
                    dataInDB.getBloomTime() == currentRecipe.getBloomTime() &&
                    dataInDB.getBloomWater() == currentRecipe.getBloomWater() &&
                    dataInDB.getBrewWaterAmount() == currentRecipe.getBrewWaterAmount() &&
                    dataInDB.getCoffeeAmount() == currentRecipe.getCoffeeAmount();
             if (recipeFound)
                 break;
        }
        return recipeFound;
    }


}
