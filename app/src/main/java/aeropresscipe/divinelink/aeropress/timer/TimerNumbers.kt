package aeropresscipe.divinelink.aeropress.timer

abstract class TimerNumbers {
    abstract var time: Int
    abstract var phase: Boolean // true if phase == Bloom
}

internal class BloomPhase(override var time: Int) : TimerNumbers() {
    override var phase: Boolean = true
}

internal class BrewPhase(override var time: Int) : TimerNumbers() {
    override var phase: Boolean = false
}

internal class GetPhaseFactory {
    fun findPhase(bloomTime: Int = 0, brewTime: Int = 0): TimerNumbers? {
        if (bloomTime != null && brewTime != null) {
            if (bloomTime == 0) {
                return BrewPhase(brewTime)
            } else if (bloomTime > 0) {
                return BloomPhase(bloomTime)
            }
        }
        return null
    }

    fun getMaxTime(bloomTime: Int?, brewTime: Int?): Int {
        return if (bloomTime == 0)
            brewTime ?: 0
        else
            bloomTime ?: 0
    }
}