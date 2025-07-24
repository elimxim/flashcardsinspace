package com.github.elimxim.flashcardsinspace

import com.github.elimxim.flashcardsinspace.entity.Stage
import kotlin.collections.joinToString

val lightspeedCfg = mapOf(
    Stage.S1 to Interval(delay = 0, gap = 1),
    Stage.S2 to Interval(delay = 1, gap = 2),
    Stage.S3 to Interval(delay = 2, gap = 4),
    Stage.S4 to Interval(delay = 4, gap = 8),
    Stage.S5 to Interval(delay = 0, gap = 16),
    Stage.S6 to Interval(delay = 8, gap = 32),
    Stage.S7 to Interval(delay = -8, gap = 64)
)

val wynerCfg = mapOf(
    Stage.S1 to Interval(delay = 0, gap = 1),
    Stage.S2 to Interval(delay = -1, gap = 2),
    Stage.S3 to Interval(delay = -2, gap = 4),
    Stage.S4 to Interval(delay = -3, gap = intArrayOf(7, 9)),
    Stage.S5 to Interval(delay = -4, gap = 16),
    Stage.S6 to Interval(delay = -5, gap = intArrayOf(29, 35)),
    Stage.S7 to Interval(delay = -8, gap = 64)
)

interface StudySchedule {
    fun days(): List<Day>
}

class WynerSchedule(capacity: Int) : StudySchedule by ConfigurableSchedule(capacity, wynerCfg)
class LightspeedSchedule(capacity: Int) : StudySchedule by ConfigurableSchedule(capacity, lightspeedCfg)

class ConfigurableSchedule(capacity: Int, cfg: Map<Stage, Interval>) : StudySchedule {
    private val days = ArrayList<Day>(capacity)

    init {
        val stageResolver = StageResolver(cfg)
        for (day in 1..capacity) {
            days.add(Day(number = day, stages = stageResolver.stages(day)))
        }
    }

    override fun days() = days
}

data class Day(val number: Int, val stages: List<Stage>)

class StageResolver(private val cfg: Map<Stage, Interval>) {
    private val stages = Stage.entries.toTypedArray()
        .sortedByDescending { stage -> stage.ordinal }

    private val stageDay: MutableMap<Stage, Int> = Stage.entries
        .associateWith { stage -> cfg.getValue(stage).next() }
        .toMutableMap()

    fun stages(day: Int): List<Stage> {
        return stages.filter { s ->
            val interval = stageDay.getValue(s)
            if (interval - day == 0) {
                stageDay[s] = interval + cfg.getValue(s).next()
                true
            } else {
                false
            }
        }
    }
}

class Interval(private val delay: Int, private val gap: Gap) {
    constructor(delay: Int, gap: Int) : this(delay, StaticGap(gap))
    constructor(delay: Int, gap: IntArray) : this(delay, gap = DynamicGap(*gap))

    private var delayed = false
    fun next(): Int {
        return if (!delayed) {
            delayed = true
            gap.value() + delay
        } else {
            gap.value()
        }
    }
}

interface Gap {
    fun value(): Int
}

class StaticGap(val value: Int) : Gap {
    override fun value() = value
}

class DynamicGap(vararg values: Int) : Gap {
    private val values: Array<Int> = values.toTypedArray()
    private val lastIdx = values.size - 1
    private var currIdx = 0

    override fun value(): Int {
        if (currIdx > lastIdx) currIdx = 0
        return values[currIdx++]
    }
}

class PrintableSchedule(private val schedule: StudySchedule): StudySchedule by schedule {
    fun print(rows: Int, columns: Int) {
        for (i in 0..<rows) {
            var idx = i
            for (j in 0..<columns) {
                if (idx >= schedule.days().size) continue
                val day = schedule.days()[idx]

                val fDay = day.number.toString()
                    .padEnd(3, ' ')

                val fStages = day.stages
                    .map { s -> s.ordinal + 1 }
                    .joinToString(separator = ", ")
                    .padEnd(10, ' ')

                print("Day $fDay: Stage $fStages")
                idx += rows
            }
            println()
        }
    }
}

fun main() {
    val wynerSchedule = PrintableSchedule(WynerSchedule(64))
    val lightspeedSchedule = PrintableSchedule(LightspeedSchedule(64))

    wynerSchedule.print(rows = 16, columns = 4)
    println()
    println()
    lightspeedSchedule.print(rows = 16, columns = 4)
}