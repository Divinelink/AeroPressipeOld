package aeropresscipe.divinelink.aeropress.base;

import android.content.Context;

import aeropresscipe.divinelink.aeropress.generaterecipe.DiceDomain;
import aeropresscipe.divinelink.aeropress.generaterecipe.RecipeDao;
import aeropresscipe.divinelink.aeropress.history.HistoryDao;
import aeropresscipe.divinelink.aeropress.history.HistoryDomain;
import aeropresscipe.divinelink.aeropress.savedrecipes.Converters;
import aeropresscipe.divinelink.aeropress.savedrecipes.SavedRecipeDao;
import aeropresscipe.divinelink.aeropress.savedrecipes.SavedRecipeDomain;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {DiceDomain.class, SavedRecipeDomain.class, HistoryDomain.class}, version = 10, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class HomeDatabase extends RoomDatabase {

    public abstract RecipeDao recipeDao();

    public abstract SavedRecipeDao savedRecipeDao();

    public abstract HistoryDao historyDao();

    private static HomeDatabase INSTANCE;

    public static HomeDatabase getDatabase(Context ctx) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(ctx.getApplicationContext(),
                    HomeDatabase.class, "Home_Database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }

}
