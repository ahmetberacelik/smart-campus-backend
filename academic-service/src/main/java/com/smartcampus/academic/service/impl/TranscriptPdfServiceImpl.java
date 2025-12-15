package com.smartcampus.academic.service.impl;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.smartcampus.academic.dto.response.TranscriptResponse;
import com.smartcampus.academic.service.TranscriptPdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class TranscriptPdfServiceImpl implements TranscriptPdfService {

    private static final DeviceRgb HEADER_COLOR = new DeviceRgb(41, 128, 185);
    private static final DeviceRgb LIGHT_GRAY = new DeviceRgb(245, 245, 245);

    @Override
    public byte[] generateTranscriptPdf(TranscriptResponse transcript) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            addHeader(document, transcript);
            addStudentInfo(document, transcript);

            for (TranscriptResponse.SemesterRecord semester : transcript.getSemesters()) {
                addSemesterSection(document, semester);
            }

            addSummary(document, transcript);
            addFooter(document);

            document.close();
            log.info("Transkript PDF oluşturuldu: {}", transcript.getStudentNumber());
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("PDF oluşturma hatası: {}", e.getMessage());
            throw new RuntimeException("Transkript PDF oluşturulamadı", e);
        }
    }

    private void addHeader(Document document, TranscriptResponse transcript) {
        Paragraph title = new Paragraph("SMART CAMPUS ÜNİVERSİTESİ")
                .setBold()
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(HEADER_COLOR);
        document.add(title);

        Paragraph subtitle = new Paragraph("RESMİ TRANSKRİPT")
                .setBold()
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(subtitle);
    }

    private void addStudentInfo(Document document, TranscriptResponse transcript) {
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 2, 1, 2}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        addInfoRow(infoTable, "Öğrenci No:", transcript.getStudentNumber());
        addInfoRow(infoTable, "Ad Soyad:", transcript.getStudentName());
        addInfoRow(infoTable, "Bölüm:", transcript.getDepartmentName());
        addInfoRow(infoTable, "CGPA:", transcript.getCgpa().toString());

        document.add(infoTable);
    }

    private void addInfoRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setBold()).setBorder(null));
        table.addCell(new Cell().add(new Paragraph(value)).setBorder(null));
    }

    private void addSemesterSection(Document document, TranscriptResponse.SemesterRecord semester) {
        Paragraph semesterTitle = new Paragraph(semester.getSemester() + " " + semester.getYear())
                .setBold()
                .setFontSize(12)
                .setBackgroundColor(LIGHT_GRAY)
                .setPadding(5)
                .setMarginTop(10);
        document.add(semesterTitle);

        Table coursesTable = new Table(UnitValue.createPercentArray(new float[]{2, 4, 1, 1, 1}))
                .setWidth(UnitValue.createPercentValue(100));

        coursesTable.addHeaderCell(createHeaderCell("Ders Kodu"));
        coursesTable.addHeaderCell(createHeaderCell("Ders Adı"));
        coursesTable.addHeaderCell(createHeaderCell("Kredi"));
        coursesTable.addHeaderCell(createHeaderCell("Harf Notu"));
        coursesTable.addHeaderCell(createHeaderCell("Puan"));

        for (TranscriptResponse.CourseRecord course : semester.getCourses()) {
            coursesTable.addCell(createDataCell(course.getCourseCode()));
            coursesTable.addCell(createDataCell(course.getCourseName()));
            coursesTable.addCell(createDataCell(String.valueOf(course.getCredits())));
            coursesTable.addCell(createDataCell(course.getLetterGrade() != null ? course.getLetterGrade() : "-"));
            coursesTable.addCell(createDataCell(course.getGradePoint() != null ? course.getGradePoint().toString() : "-"));
        }

        document.add(coursesTable);

        Paragraph gpaLine = new Paragraph("Dönem GPA: " + semester.getGpa() + " | Dönem Kredi: " + semester.getCredits())
                .setItalic()
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(10);
        document.add(gpaLine);
    }

    private Cell createHeaderCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold())
                .setBackgroundColor(HEADER_COLOR)
                .setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(5);
    }

    private Cell createDataCell(String text) {
        return new Cell()
                .add(new Paragraph(text))
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(3);
    }

    private void addSummary(Document document, TranscriptResponse transcript) {
        Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                .setWidth(UnitValue.createPercentValue(60))
                .setMarginTop(20);

        summaryTable.addCell(createSummaryCell("Genel CGPA", transcript.getCgpa().toString()));
        summaryTable.addCell(createSummaryCell("Toplam Kredi", String.valueOf(transcript.getTotalCredits())));
        summaryTable.addCell(createSummaryCell("Tamamlanan Kredi", String.valueOf(transcript.getCompletedCredits())));

        document.add(summaryTable);
    }

    private Cell createSummaryCell(String label, String value) {
        return new Cell()
                .add(new Paragraph(label).setBold().setFontSize(10))
                .add(new Paragraph(value).setFontSize(14))
                .setTextAlignment(TextAlignment.CENTER)
                .setBackgroundColor(LIGHT_GRAY)
                .setPadding(10);
    }

    private void addFooter(Document document) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        Paragraph footer = new Paragraph("Oluşturulma Tarihi: " + date)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(30)
                .setFontColor(ColorConstants.GRAY);
        document.add(footer);

        Paragraph disclaimer = new Paragraph("Bu belge elektronik olarak oluşturulmuştur ve Smart Campus Üniversitesi tarafından onaylanmıştır.")
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY);
        document.add(disclaimer);
    }
}
