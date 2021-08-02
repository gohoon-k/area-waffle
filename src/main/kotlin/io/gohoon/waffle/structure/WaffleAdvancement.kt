package io.gohoon.waffle.structure

data class WaffleAdvancement(
    val name: String,
    val canRepeat: Boolean = false,
    val isChallenge: Boolean = false,
    val requirements: Array<Requirements>
) {
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WaffleAdvancement

        if (name != other.name) return false
        if (canRepeat != other.canRepeat) return false
        if (isChallenge != other.isChallenge) return false
        if (!requirements.contentEquals(other.requirements)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + canRepeat.hashCode()
        result = 31 * result + isChallenge.hashCode()
        result = 31 * result + requirements.contentHashCode()
        return result
    }
    
}
