package com.thermaflow.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.thermaflow.model.DailySchedule;
import com.thermaflow.model.InfusionSlot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

/**
 * Service for generating PDF exports of daily schedules.
 * Uses OpenPDF and runs on Virtual Threads for optimal I/O performance.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PdfExportService {
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
    
    /**
     * Generates a PDF for a daily schedule asynchronously using Virtual Threads.
     * 
     * @param schedule The daily schedule to export
     * @return CompletableFuture containing the PDF as byte array
     */
    @Async("virtualThreadExecutor")
    public CompletableFuture<byte[]> generateDailySchedulePdf(DailySchedule schedule) {
        log.info("Starting PDF generation for schedule on {}", schedule.getDate());
        
        try {
            byte[] pdfBytes = createPdf(schedule);
            log.info("PDF generation completed for schedule on {}", schedule.getDate());
            return CompletableFuture.completedFuture(pdfBytes);
        } catch (Exception e) {
            log.error("Error generating PDF for schedule on {}", schedule.getDate(), e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * Creates the PDF document.
     */
    private byte[] createPdf(DailySchedule schedule) throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(document, baos);
        
        document.open();
        
        // Add title
        addTitle(document, schedule);
        
        // Add schedule table
        addScheduleTable(document, schedule);
        
        // Add footer
        addFooter(document);
        
        document.close();
        
        return baos.toByteArray();
    }
    
    /**
     * Adds the title section to the PDF.
     */
    private void addTitle(Document document, DailySchedule schedule) throws DocumentException {
        Font titleFont = new Font(Font.HELVETICA, 24, Font.BOLD);
        Font dateFont = new Font(Font.HELVETICA, 14, Font.NORMAL, java.awt.Color.GRAY);
        
        Paragraph title = new Paragraph("ThermaFlow Daily Infusion Schedule", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);
        
        Paragraph date = new Paragraph(schedule.getDate().format(DATE_FORMATTER), dateFont);
        date.setAlignment(Element.ALIGN_CENTER);
        date.setSpacingAfter(20);
        document.add(date);
    }
    
    /**
     * Adds the schedule table to the PDF.
     */
    private void addScheduleTable(Document document, DailySchedule schedule) throws DocumentException {
        // Create table with 6 columns: Time, Location, Theme, Intensity, Scent, QR Code
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.2f, 1.5f, 2f, 1f, 1.5f, 1f});
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);
        
        // Add header row
        addTableHeader(table);
        
        // Add data rows
        for (InfusionSlot slot : schedule.getSlots()) {
            if (!slot.getCancelled()) {
                addSlotRow(table, slot);
            }
        }
        
        document.add(table);
    }
    
    /**
     * Adds the header row to the table.
     */
    private void addTableHeader(PdfPTable table) {
        Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, java.awt.Color.WHITE);
        java.awt.Color headerColor = new java.awt.Color(41, 128, 185); // Blue
        
        String[] headers = {"Time", "Location", "Theme/Name", "Intensity", "Scent Info", "Rating"};
        
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(headerColor);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(8);
            table.addCell(cell);
        }
    }
    
    /**
     * Adds a slot row to the table.
     */
    private void addSlotRow(PdfPTable table, InfusionSlot slot) {
        Font normalFont = new Font(Font.HELVETICA, 9, Font.NORMAL);
        Font boldFont = new Font(Font.HELVETICA, 9, Font.BOLD);
        
        // Time
        String timeRange = String.format("%s - %s",
                slot.getStartTime().format(TIME_FORMATTER),
                slot.getEndTime().format(TIME_FORMATTER));
        table.addCell(createCell(timeRange, normalFont));
        
        // Location
        table.addCell(createCell(slot.getRoom().getName(), boldFont));
        
        // Theme/Name
        String themeName = slot.getRecipe().getTheme() != null 
                ? slot.getRecipe().getTheme() 
                : slot.getRecipe().getName();
        table.addCell(createCell(themeName, normalFont));
        
        // Intensity (visual indicator)
        String intensityIndicator = getIntensityIndicator(slot.getAverageHeatIntensity());
        PdfPCell intensityCell = createCell(intensityIndicator, normalFont);
        intensityCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(intensityCell);
        
        // Scent Info
        String scentInfo = slot.getRecipe().getSteps().stream()
                .filter(step -> step.getIngredient() != null)
                .map(step -> step.getIngredient().getScentProfile().toString())
                .distinct()
                .limit(2)
                .reduce((a, b) -> a + ", " + b)
                .orElse("N/A");
        table.addCell(createCell(scentInfo, normalFont));
        
        // QR Code placeholder
        PdfPCell qrCell = new PdfPCell(new Phrase("[QR]", normalFont));
        qrCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        qrCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        qrCell.setPadding(5);
        table.addCell(qrCell);
    }
    
    /**
     * Creates a table cell with specified text and font.
     */
    private PdfPCell createCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }
    
    /**
     * Generates visual intensity indicator based on heat level.
     * Returns flame emojis/symbols representing intensity (1-3).
     */
    private String getIntensityIndicator(double averageIntensity) {
        if (averageIntensity <= 3.0) {
            return "🔥";  // Low intensity
        } else if (averageIntensity <= 6.0) {
            return "🔥🔥";  // Medium intensity
        } else {
            return "🔥🔥🔥";  // High intensity
        }
    }
    
    /**
     * Adds footer to the PDF.
     */
    private void addFooter(Document document) throws DocumentException {
        Font footerFont = new Font(Font.HELVETICA, 8, Font.ITALIC, java.awt.Color.GRAY);
        
        Paragraph footer = new Paragraph(
                "Scan the QR codes to rate your experience and provide feedback. Enjoy your wellness journey!",
                footerFont
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(20);
        document.add(footer);
    }
}
