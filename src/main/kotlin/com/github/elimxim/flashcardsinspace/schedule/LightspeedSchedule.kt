/*
 * Copyright (c) 2025 Maksim Eliseev. All rights reserved.
 *
 * This file is part of Flashcards in Space.
 *
 * This software is proprietary and confidential. Unauthorized copying,
 * distribution, or use is strictly prohibited.
 *
 * For licensing information, see LICENSE file in the project root.
 */

package com.github.elimxim.flashcardsinspace.schedule

import com.github.elimxim.flashcardsinspace.entity.FlashcardStage
import com.github.elimxim.flashcardsinspace.entity.FlashcardStage.*
import com.github.elimxim.flashcardsinspace.entity.TERMINAL_STAGE_NAME

val lightspeedCfg = mapOf(
    S1 to interval(delay = 0, gap = 1),
    S2 to interval(delay = 1, gap = 2),
    S3 to interval(delay = 2, gap = 4),
    S4 to interval(delay = 4, gap = 8),
    S5 to interval(delay = 0, gap = 16),
    S6 to interval(delay = 8, gap = 32),
    S7 to interval(delay = -8, gap = 64)
)

val wynerCfg = mapOf(
    S1 to interval(delay = 0, gap = 1),
    S2 to interval(delay = -1, gap = 2),
    S3 to interval(delay = -2, gap = 4),
    S4 to interval(delay = -3, gap = gaps(7, 9)),
    S5 to interval(delay = -4, gap = 16),
    S6 to interval(delay = -5, gap = gaps(29, 35)),
    S7 to interval(delay = -8, gap = 64)
)

interface StudySchedule {
    fun days(): List<Day>
}

class WynerSchedule(capacity: Int) : StudySchedule by ConfigurableSchedule(capacity, wynerCfg)
class LightspeedSchedule(capacity: Int) : StudySchedule by ConfigurableSchedule(capacity, lightspeedCfg)

class ConfigurableSchedule(capacity: Int, cfg: Map<FlashcardStage, IntervalDefinition>) : StudySchedule {
    private val days = ArrayList<Day>(capacity)

    init {
        val stageResolver = StageResolver(cfg = cfg.entries.associate { it.key to Interval(it.value) })
        for (day in 1..capacity) {
            days.add(Day(number = day, stages = stageResolver.stages(day)))
        }
    }

    override fun days() = days
}

data class Day(val number: Int, val stages: List<FlashcardStage>)

class StageResolver(private val cfg: Map<FlashcardStage, Interval>) {
    private val stages = FlashcardStage.entries
        .filter { it.name != TERMINAL_STAGE_NAME }
        .toTypedArray()
        .sortedByDescending { stage -> stage.ordinal }

    private val stageMap: MutableMap<FlashcardStage, Int> = FlashcardStage.entries
        .filter { it.name != TERMINAL_STAGE_NAME }
        .associateWith { stage -> cfg.getValue(stage).next() }
        .toMutableMap()

    fun stages(day: Int): List<FlashcardStage> {
        return stages.filter { s ->
            val interval = stageMap.getValue(s)
            if (interval - day == 0) {
                stageMap[s] = interval + cfg.getValue(s).next()
                true
            } else {
                false
            }
        }
    }
}

fun interval(delay: Int, gap: DynamicGapDefinition) = IntervalDefinition(delay, gap)
fun interval(delay: Int, gap: Int) = IntervalDefinition(delay, StaticGapDefinition(gap))
fun gaps(vararg gaps: Int) = DynamicGapDefinition(gaps)

class IntervalDefinition(val delay: Int, val gap: GapDefinition)
interface GapDefinition
class StaticGapDefinition(val gap: Int) : GapDefinition
class DynamicGapDefinition(val gaps: IntArray) : GapDefinition

fun mapGapDefinition(def: GapDefinition): Gap {
    return when (def) {
        is StaticGapDefinition -> StaticGap(def.gap)
        is DynamicGapDefinition -> DynamicGap(def.gaps)
        else -> throw IllegalArgumentException("Unknown gap definition type: ${def::class.simpleName}")
    }
}

class Interval(private val delay: Int, private val gap: Gap) {
    constructor(def: IntervalDefinition) : this(def.delay, mapGapDefinition(def.gap))

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

class DynamicGap(private val values: IntArray) : Gap {
    private val lastIdx = values.size - 1
    private var currIdx = 0

    override fun value(): Int {
        if (currIdx > lastIdx) currIdx = 0
        return values[currIdx++]
    }
}

class PrintableSchedule(private val schedule: StudySchedule) : StudySchedule by schedule {
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
    print("\n\n")
    lightspeedSchedule.print(rows = 16, columns = 4)
}
