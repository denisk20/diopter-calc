package org.endmyopia.calc.data

/**
 * @author denisk
 * @since 2019-07-17.
 */
enum class MeasurementMode(val value: Int) {
    LEFT(0),
    RIGHT(1),
    BOTH(2);

    companion object {
        fun byValue(value: Int): MeasurementMode {
            values().filter {
                it.value == value
            }.also {
                return if (it.size == 1) it[0] else throw IllegalArgumentException("Can't find value for $value")
            }
        }
    }
}