package aeropresscipe.divinelink.aeropress.generaterecipe

import java.io.Serializable

class DiceUI(
    var bloomTime: Int,
    var brewTime: Int,
    var bloomWater: Int,
    var remainingBrewWater: Int,
) : Serializable {

    var isNewRecipe = false
    private var recipeHadBloom = false

    fun recipeHadBloom(): Boolean {
        return recipeHadBloom
    }

    fun setRecipeHadBloom(recipeHadBloom: Boolean) {
        this.recipeHadBloom = recipeHadBloom
    }
}