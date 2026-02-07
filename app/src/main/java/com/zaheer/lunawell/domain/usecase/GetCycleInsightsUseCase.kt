package com.zaheer.lunawell.domain.usecase

import com.zaheer.lunawell.data.repository.CycleRepository
import com.zaheer.lunawell.data.repository.SymptomLogRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlin.math.pow
import kotlin.math.sqrt

data class CycleInsights(
    val averageCycleLength: Float,
    val shortestCycle: Int,
    val longestCycle: Int,
    val cycleVariance: Float,
    val mostCommonSymptoms: List<String>
)

class GetCycleInsightsUseCase(
    private val cycleRepository: CycleRepository,
    private val symptomLogRepository: SymptomLogRepository
) {
    suspend operator fun invoke(profileId: Long, limitToCycles: Int? = null): CycleInsights {
        val cycles = if (limitToCycles != null) {
            cycleRepository.getRecentCycles(profileId, limitToCycles).firstOrNull() ?: emptyList()
        } else {
            cycleRepository.getCyclesByProfile(profileId).firstOrNull() ?: emptyList()
        }
        
        if (cycles.isEmpty()) {
            return CycleInsights(
                averageCycleLength = 28f,
                shortestCycle = 0,
                longestCycle = 0,
                cycleVariance = 0f,
                mostCommonSymptoms = emptyList()
            )
        }
        
        val cycleLengths = cycles.map { it.cycleLength }
        val averageLength = cycleLengths.average().toFloat()
        val shortestCycle = cycleLengths.minOrNull() ?: 0
        val longestCycle = cycleLengths.maxOrNull() ?: 0
        
        val variance = if (cycleLengths.size > 1) {
            val mean = cycleLengths.average()
            val sumOfSquares = cycleLengths.sumOf { (it - mean).pow(2) }
            sqrt(sumOfSquares / cycleLengths.size).toFloat()
        } else {
            0f
        }
        
        val symptoms = symptomLogRepository.getSymptomsByProfile(profileId).firstOrNull() ?: emptyList()
        val symptomCounts = symptoms.groupingBy { it.symptomType }.eachCount()
        val mostCommonSymptoms = symptomCounts.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { it.key }
        
        return CycleInsights(
            averageCycleLength = averageLength,
            shortestCycle = shortestCycle,
            longestCycle = longestCycle,
            cycleVariance = variance,
            mostCommonSymptoms = mostCommonSymptoms
        )
    }
}
