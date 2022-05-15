package aeropresscipe.divinelink.aeropress.base;


import java.io.Serializable;

import aeropresscipe.divinelink.aeropress.generaterecipe.DiceUI;

public interface HomeView extends Serializable {

    void addSavedRecipesFragment();

    void startTimerActivity(DiceUI diceUI);

    void addGenerateRecipeFragment();

    void addHistoryFragment();

}
