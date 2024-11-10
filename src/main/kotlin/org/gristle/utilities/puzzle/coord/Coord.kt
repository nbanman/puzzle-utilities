@file:Suppress("UNUSED")
package org.gristle.utilities.puzzle.coord

import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.sign

interface MCoord  {
    val coordinates: List<Int>
    fun asIndex(dimensions: List<Int>): Int
    operator fun get(n: Int): Int
    operator fun unaryMinus(): MCoord
    operator fun plus(other: MCoord): MCoord
    operator fun minus(other: MCoord): MCoord
    operator fun times(other: MCoord): MCoord
    operator fun div(other: MCoord): MCoord
    operator fun rem(other: MCoord): MCoord
    fun mod(other: MCoord): MCoord
    fun max(other: MCoord): MCoord
    fun min(other: MCoord): MCoord
    fun measure(): Int
    fun lineTo(other: MCoord): List<MCoord>
    fun manhattanDistance(other: MCoord): Int
    fun chebyshevDistance(other: MCoord): Int
    fun getNeighbors(): List<MCoord>
}

open class MCoordImpl internal constructor(override val coordinates: List<Int>) : MCoord {
    override fun asIndex(dimensions: List<Int>): Int {
        val multiplier = dimensions.runningFold(1, Int::times)
        return coordinates.zip(multiplier, Int::times).sum()
    }

    override fun get(n: Int): Int = coordinates[n]

    override fun unaryMinus(): MCoord {
        return MCoord(coordinates.map { -it })
    }

    override fun plus(other: MCoord): MCoord {
        return MCoord(coordinates.zip(other.coordinates, Int::plus))
    }

    override fun minus(other: MCoord): MCoord {
        return MCoord(coordinates.zip(other.coordinates, Int::minus))
    }

    override fun times(other: MCoord): MCoord {
        return MCoord(coordinates.zip(other.coordinates, Int::times))
    }

    override fun div(other: MCoord): MCoord {
        return MCoord(coordinates.zip(other.coordinates, Int::div))
    }

    override fun rem(other: MCoord): MCoord {
        return MCoord(coordinates.zip(other.coordinates, Int::rem))
    }

    override fun mod(other: MCoord): MCoord {
        return MCoord(coordinates.zip(other.coordinates, Int::mod))
    }

    override fun max(other: MCoord): MCoord {
        return MCoord(coordinates.zip(other.coordinates, { a, b -> kotlin.math.max(a, b) }) )
    }

    override fun min(other: MCoord): MCoord {
        return MCoord(coordinates.zip(other.coordinates, { a, b -> kotlin.math.min(a, b) }) )
    }

    override fun measure(): Int = coordinates.reduce(Int::times)

    override fun lineTo(other: MCoord): List<MCoord> {
        val deltas: List<Int> = coordinates.zip(other.coordinates) { a, b -> (b - a).sign }
        val steps: Int = coordinates
            .zip(other.coordinates) { a, b -> (b - a).absoluteValue }
            .max()
        return (1..steps).runningFold(this) { last: MCoord, _ -> MCoord(last.coordinates.zip(deltas, Int::plus)) }
    }

    override fun manhattanDistance(other: MCoord): Int {
        return (coordinates zip other.coordinates).sumOf { (a, b) -> abs(a - b) }
    }

    override fun chebyshevDistance(other: MCoord): Int {
        return coordinates.zip(other.coordinates) { a, b -> abs(a - b) }.max()
    }

    private fun changeInOneDimension(dimension: Int, newValue: Int): MCoord {
        return MCoord(coordinates.toMutableList().also { it[dimension] = newValue })
    }

    override fun getNeighbors(): List<MCoord> {
        return (0 until coordinates.size).fold(listOf(this)) { acc: List<MCoord>, dim ->
            val left = acc.map { this.changeInOneDimension(dim, it.coordinates[dim] - 1) }
            val right = acc.map { this.changeInOneDimension(dim, it.coordinates[dim] + 1) }
            acc + left + right
        }.drop(1)
    }

    operator fun component1() = coordinates[0]
    operator fun component2() = coordinates[1]
    operator fun component3() = coordinates[2]
    operator fun component4() = coordinates[3]
    operator fun component5() = coordinates[4]

    override fun toString(): String = "(${coordinates.joinToString()})"
}

fun MCoord(coordinates: List<Int>): MCoord = MCoordImpl(coordinates)

interface Coord : MCoord {
    val x: Int
    val y: Int

    override fun unaryMinus(): Coord
    operator fun plus(other: Coord): Coord
    operator fun minus(other: Coord): Coord
    operator fun times(other: Coord): Coord
    operator fun div(other: Coord): Coord
    operator fun rem(other: Coord): Coord
    fun mod(other: Coord): Coord
    fun max(other: Coord): Coord
    fun min(other: Coord): Coord
    fun lineTo(other: Coord): List<Coord>
    override fun getNeighbors(): List<Coord>

    fun area(): Int

    companion object {
        val ORIGIN = Coord(0, 0)

        val CROSS = listOf(
            Coord(0, -1),
            Coord(-1, 0),
            Coord(0, 0),
            Coord(1, 0),
            Coord(0, 1),
        )

        /**
         * Positions above, left, below, and right of origin.
         */
        val NESW = listOf(
            Coord(0, -1),
            Coord(1, 0),
            Coord(0, 1),
            Coord(-1, 0),
        )

        /**
         * All positions adjacent to origin, including diagonals.
         */
        val ALL_ADJACENT = listOf(
            Coord(-1, -1),
            Coord(0, -1),
            Coord(1, -1),
            Coord(1, 0),
            Coord(-1, 0),
            Coord(-1, 1),
            Coord(0, 1),
            Coord(1, 1),
        )

        fun fromIndex(n: Int, width: Int) = Coord(n % width, n / width)

        inline fun forRectangle(tl: Coord, br: Coord, action: (coord: Coord) -> Unit) {
            for (y in tl.y..br.y) for (x in tl.x..br.x) action(Coord(x, y))
        }

        inline fun forRectangle(xRange: IntRange, yRange: IntRange, action: (coord: Coord) -> Unit) {
            for (y in yRange) for (x in xRange) action(Coord(x, y))
        }

        inline fun forRectangle(minMaxRange: Pair<IntRange, IntRange>, action: (coord: Coord) -> Unit) =
            forRectangle(minMaxRange.first, minMaxRange.second, action)

        fun rectangleFrom(tl: Coord, br: Coord): List<Coord> = buildList {
            for (y in tl.y..br.y) for (x in tl.x..br.x) add(Coord(x, y))
        }
    }
}

class CoordImpl internal constructor(private val b: MCoordImpl) : Coord, MCoord by b {
    override val x: Int
        get() = coordinates[0]
    override val y: Int
        get() = coordinates[1]

    override fun unaryMinus(): Coord = CoordImpl(b.unaryMinus() as MCoordImpl)
    override fun plus(other: Coord): Coord = CoordImpl(b.plus(other) as MCoordImpl)
    override fun minus(other: Coord): Coord = CoordImpl(b.minus(other) as MCoordImpl)
    override fun times(other: Coord): Coord = CoordImpl(b.times(other) as MCoordImpl)
    override fun div(other: Coord): Coord = CoordImpl(b.div(other) as MCoordImpl)
    override fun rem(other: Coord): Coord = CoordImpl(b.rem(other) as MCoordImpl)
    override fun mod(other: Coord): Coord = CoordImpl(b.mod(other) as MCoordImpl)
    override fun max(other: Coord): Coord = CoordImpl(b.max(other) as MCoordImpl)
    override fun min(other: Coord): Coord = CoordImpl(b.min(other) as MCoordImpl)
    override fun lineTo(other: Coord): List<Coord> = b.lineTo(other)
        .map { mCoord -> CoordImpl(mCoord as MCoordImpl) }
    override fun getNeighbors(): List<Coord>  = b.getNeighbors()
        .map { mCoord -> CoordImpl(mCoord as MCoordImpl) }

    override fun area(): Int = b.measure()
}

fun Coord(x: Int, y: Int): Coord {
    return CoordImpl(MCoordImpl(listOf(x, y)))
}