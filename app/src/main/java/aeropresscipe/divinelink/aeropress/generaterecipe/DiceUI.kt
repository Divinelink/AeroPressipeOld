package aeropresscipe.divinelink.aeropress.generaterecipe

import android.os.Parcelable
import android.os.Parcel
import aeropresscipe.divinelink.aeropress.generaterecipe.DiceUI

class DiceUI : Parcelable {
    var bloomTime: Int
    var brewTime: Int
    var bloomWater: Int
    var remainingBrewWater: Int
    var isNewRecipe = false
    private var recipeHadBloom = false

    constructor(bloomTime: Int, brewTime: Int, bloomWater: Int, remainingBrewWater: Int) {
        this.bloomTime = bloomTime
        this.brewTime = brewTime
        this.bloomWater = bloomWater
        this.remainingBrewWater = remainingBrewWater
    }

    fun recipeHadBloom(): Boolean {
        return recipeHadBloom
    }

    fun setRecipeHadBloom(recipeHadBloom: Boolean) {
        this.recipeHadBloom = recipeHadBloom
    }

    override fun describeContents(): Int {
        return 0
    }

    protected constructor(`in`: Parcel) {
        bloomTime = `in`.readInt()
        brewTime = `in`.readInt()
        bloomWater = `in`.readInt()
        remainingBrewWater = `in`.readInt()
        isNewRecipe = `in`.readInt() == 1
        recipeHadBloom = `in`.readInt() == 1
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeInt(bloomTime)
        parcel.writeInt(brewTime)
        parcel.writeInt(bloomWater)
        parcel.writeInt(remainingBrewWater)
        parcel.writeInt(if (isNewRecipe) 1 else 0)
        parcel.writeInt(if (recipeHadBloom) 1 else 0)
    }

    companion object {
//        val CREATOR: Parcelable.Creator<DiceUI> = object : Parcelable.Creator<DiceUI?> {
//            override fun createFromParcel(`in`: Parcel): DiceUI? {
//                return DiceUI(`in`)
//            }
//
//            override fun newArray(size: Int): Array<DiceUI?> {
//                return arrayOfNulls(size)
//            }
//        }
    }
}