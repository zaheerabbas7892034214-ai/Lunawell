package com.zaheer.lunawell.export

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.zaheer.lunawell.data.repository.AppointmentRepository
import com.zaheer.lunawell.data.repository.BreastHealthRepository
import com.zaheer.lunawell.data.repository.CycleRepository
import com.zaheer.lunawell.data.repository.PregnancyRepository
import com.zaheer.lunawell.data.repository.ProfileRepository
import com.zaheer.lunawell.data.repository.SymptomLogRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PDFExporter(
    private val context: Context,
    private val profileRepository: ProfileRepository,
    private val cycleRepository: CycleRepository,
    private val symptomLogRepository: SymptomLogRepository,
    private val pregnancyRepository: PregnancyRepository,
    private val breastHealthRepository: BreastHealthRepository,
    private val appointmentRepository: AppointmentRepository
) {
    
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val PAGE_WIDTH = 595
    private val PAGE_HEIGHT = 842
    private val MARGIN = 40f
    private val LINE_HEIGHT = 20f
    
    suspend fun exportHealthSummary(profileId: Long, outputUri: Uri): Boolean {
        return try {
            val profile = profileRepository.getProfile(profileId).first() ?: return false
            val cycles = cycleRepository.getCyclesByProfile(profileId).first()
            val symptoms = symptomLogRepository.getSymptomsByProfile(profileId).first()
            val pregnancyLogs = pregnancyRepository.getPregnancyLogsByProfile(profileId).first()
            val breastLogs = breastHealthRepository.getBreastLogsByProfile(profileId).first()
            val appointments = appointmentRepository.getAppointmentsByProfile(profileId).first()
            
            val pdfDocument = PdfDocument()
            var pageNumber = 1
            var yPosition = MARGIN
            
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas
            
            val titlePaint = Paint().apply {
                textSize = 24f
                isFakeBoldText = true
            }
            val headerPaint = Paint().apply {
                textSize = 18f
                isFakeBoldText = true
            }
            val normalPaint = Paint().apply {
                textSize = 12f
            }
            
            // Title
            canvas.drawText("LunaWell Health Summary", MARGIN, yPosition, titlePaint)
            yPosition += LINE_HEIGHT * 2
            
            // Profile Info
            canvas.drawText("Profile Information", MARGIN, yPosition, headerPaint)
            yPosition += LINE_HEIGHT
            canvas.drawText("Name: ${profile.name}", MARGIN + 20, yPosition, normalPaint)
            yPosition += LINE_HEIGHT
            canvas.drawText("Date of Birth: ${dateFormat.format(Date(profile.dateOfBirth))}", MARGIN + 20, yPosition, normalPaint)
            yPosition += LINE_HEIGHT
            canvas.drawText("Mode: ${profile.mode}", MARGIN + 20, yPosition, normalPaint)
            yPosition += LINE_HEIGHT * 2
            
            // Cycle History
            canvas.drawText("Cycle History (Last 12 months)", MARGIN, yPosition, headerPaint)
            yPosition += LINE_HEIGHT
            val recentCycles = cycles.takeLast(12)
            if (recentCycles.isEmpty()) {
                canvas.drawText("No cycle data available", MARGIN + 20, yPosition, normalPaint)
                yPosition += LINE_HEIGHT
            } else {
                recentCycles.forEach { cycle ->
                    if (yPosition > PAGE_HEIGHT - MARGIN * 2) {
                        pdfDocument.finishPage(page)
                        pageNumber++
                        val newPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                        page = pdfDocument.startPage(newPageInfo)
                        canvas = page.canvas
                        yPosition = MARGIN
                    }
                    val startDate = dateFormat.format(Date(cycle.startDate))
                    val endDate = cycle.endDate?.let { dateFormat.format(Date(it)) } ?: "Ongoing"
                    canvas.drawText("Start: $startDate, End: $endDate, Length: ${cycle.cycleLength} days", 
                        MARGIN + 20, yPosition, normalPaint)
                    yPosition += LINE_HEIGHT
                }
            }
            yPosition += LINE_HEIGHT
            
            // Symptoms
            if (yPosition > PAGE_HEIGHT - MARGIN * 4) {
                pdfDocument.finishPage(page)
                pageNumber++
                val newPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = pdfDocument.startPage(newPageInfo)
                canvas = page.canvas
                yPosition = MARGIN
            }
            canvas.drawText("Symptom Logs", MARGIN, yPosition, headerPaint)
            yPosition += LINE_HEIGHT
            if (symptoms.isEmpty()) {
                canvas.drawText("No symptom data available", MARGIN + 20, yPosition, normalPaint)
                yPosition += LINE_HEIGHT
            } else {
                symptoms.take(20).forEach { symptom ->
                    if (yPosition > PAGE_HEIGHT - MARGIN * 2) {
                        pdfDocument.finishPage(page)
                        pageNumber++
                        val newPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                        page = pdfDocument.startPage(newPageInfo)
                        canvas = page.canvas
                        yPosition = MARGIN
                    }
                    val date = dateFormat.format(Date(symptom.date))
                    canvas.drawText("$date - ${symptom.symptomType}, Severity: ${symptom.severity}", 
                        MARGIN + 20, yPosition, normalPaint)
                    yPosition += LINE_HEIGHT
                }
            }
            yPosition += LINE_HEIGHT
            
            // Pregnancy Logs
            if (pregnancyLogs.isNotEmpty()) {
                if (yPosition > PAGE_HEIGHT - MARGIN * 4) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    val newPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    page = pdfDocument.startPage(newPageInfo)
                    canvas = page.canvas
                    yPosition = MARGIN
                }
                canvas.drawText("Pregnancy Logs", MARGIN, yPosition, headerPaint)
                yPosition += LINE_HEIGHT
                pregnancyLogs.take(10).forEach { log ->
                    if (yPosition > PAGE_HEIGHT - MARGIN * 2) {
                        pdfDocument.finishPage(page)
                        pageNumber++
                        val newPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                        page = pdfDocument.startPage(newPageInfo)
                        canvas = page.canvas
                        yPosition = MARGIN
                    }
                    canvas.drawText("Week ${log.currentWeek}, Day ${log.currentDay}", MARGIN + 20, yPosition, normalPaint)
                    yPosition += LINE_HEIGHT
                    log.weight?.let {
                        canvas.drawText("Weight: $it kg", MARGIN + 40, yPosition, normalPaint)
                        yPosition += LINE_HEIGHT
                    }
                }
                yPosition += LINE_HEIGHT
            }
            
            // Breast Health Logs
            if (breastLogs.isNotEmpty()) {
                if (yPosition > PAGE_HEIGHT - MARGIN * 4) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    val newPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    page = pdfDocument.startPage(newPageInfo)
                    canvas = page.canvas
                    yPosition = MARGIN
                }
                canvas.drawText("Breast Health Logs", MARGIN, yPosition, headerPaint)
                yPosition += LINE_HEIGHT
                breastLogs.take(10).forEach { log ->
                    if (yPosition > PAGE_HEIGHT - MARGIN * 2) {
                        pdfDocument.finishPage(page)
                        pageNumber++
                        val newPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                        page = pdfDocument.startPage(newPageInfo)
                        canvas = page.canvas
                        yPosition = MARGIN
                    }
                    val date = dateFormat.format(Date(log.date))
                    canvas.drawText("$date - ${log.symptomType}", MARGIN + 20, yPosition, normalPaint)
                    yPosition += LINE_HEIGHT
                }
                yPosition += LINE_HEIGHT
            }
            
            // Appointments
            if (yPosition > PAGE_HEIGHT - MARGIN * 4) {
                pdfDocument.finishPage(page)
                pageNumber++
                val newPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = pdfDocument.startPage(newPageInfo)
                canvas = page.canvas
                yPosition = MARGIN
            }
            canvas.drawText("Appointments", MARGIN, yPosition, headerPaint)
            yPosition += LINE_HEIGHT
            if (appointments.isEmpty()) {
                canvas.drawText("No appointments scheduled", MARGIN + 20, yPosition, normalPaint)
            } else {
                appointments.take(10).forEach { appointment ->
                    if (yPosition > PAGE_HEIGHT - MARGIN * 2) {
                        pdfDocument.finishPage(page)
                        pageNumber++
                        val newPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                        page = pdfDocument.startPage(newPageInfo)
                        canvas = page.canvas
                        yPosition = MARGIN
                    }
                    val date = dateFormat.format(Date(appointment.date))
                    canvas.drawText("$date - Dr. ${appointment.doctorName}", MARGIN + 20, yPosition, normalPaint)
                    yPosition += LINE_HEIGHT
                }
            }
            
            pdfDocument.finishPage(page)
            
            context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            pdfDocument.close()
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
