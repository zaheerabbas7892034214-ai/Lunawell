package com.zaheer.lunawell.domain.usecase

import com.zaheer.lunawell.data.repository.CycleRepository
import com.zaheer.lunawell.data.repository.SymptomLogRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlin.math.pow
import kotlin.math.sqrt

data class PCOSInsights(
    val hasIrregularCycles: Boolean,
    val irregularityScore: Float,
    val commonSymptoms: List<String>,
    val disclaimer: String = "These are patterns only. Consult healthcare provider for diagnosis."
)

class DetectPCOSPatternsUseCase(
    private val cycleRepository: CycleRepository,
    private val symptomLogRepository: SymptomLogRepository
) {
    companion object {
        private const val VARIANCE_THRESHOLD = 7f
        private val PCOS_RELATED_SYMPTOMS = setOf(
            "acne",
            "weight gain",
            "mood swings",
            "irregular bleeding",
            "fatigue",
            "hair loss",
            "excessive hair growth"
        )
    }
    
    suspend operator fun invoke(profileId: Long): PCOSInsights {
        val cycles = cycleRepository.getCyclesByProfile(profileId).firstOrNull() ?: emptyList()
        
        val variance = if (cycles.size > 1) {
            val cycleLengths = cycles.map { it.cycleLength }
            val mean = cycleLengths.average()
            val sumOfSquares = cycleLengths.sumOf { (it - mean).pow(2) }
            sqrt(sumOfSquares / cycleLengths.size).toFloat()
        } else {
            0f
        }
        
        val hasIrregularCycles = variance > VARIANCE_THRESHOLD
        
        val irregularityScore = when {
            cycles.isEmpty() -> 0f
            variance > 14f -> 1f
            variance > 10f -> 0.75f
            variance > 7f -> 0.5f
            variance > 5f -> 0.25f
            else -> 0f
        }
        
        val symptoms = symptomLogRepository.getSymptomsByProfile(profileId).firstOrNull() ?: emptyList()
        val pcosSymptoms = symptoms
            .filter { it.symptomType.lowercase() in PCOS_RELATED_SYMPTOMS }
            .groupingBy { it.symptomType }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .map { it.key }
        
        return PCOSInsights(
            hasIrregularCycles = hasIrregularCycles,
            irregularityScore = irregularityScore,
            commonSymptoms = pcosSymptoms
        )
    }
}
