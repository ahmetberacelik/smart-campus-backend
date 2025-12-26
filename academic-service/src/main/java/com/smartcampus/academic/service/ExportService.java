package com.smartcampus.academic.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.opencsv.CSVWriter;
import com.smartcampus.academic.dto.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportService {

    private final AnalyticsService analyticsService;

    /**
     * Dashboard istatistiklerini Excel formatında export et
     */
    public byte[] exportDashboardToExcel() {
        log.info("Dashboard Excel export başlatıldı");
        DashboardStatsResponse stats = analyticsService.getDashboardStats();

        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Dashboard İstatistikleri");

            // Başlık stili
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            int rowNum = 0;

            // Başlık
            Row titleRow = sheet.createRow(rowNum++);
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Smart Campus - Dashboard Raporu");
            titleCell.setCellStyle(headerStyle);

            rowNum++; // Boş satır

            // Kullanıcı İstatistikleri
            addSectionHeader(sheet, rowNum++, "Kullanıcı İstatistikleri", headerStyle);
            addDataRow(sheet, rowNum++, "Toplam Kullanıcı", stats.getTotalUsers());
            addDataRow(sheet, rowNum++, "Öğrenci", stats.getTotalStudents());
            addDataRow(sheet, rowNum++, "Öğretim Üyesi", stats.getTotalFaculty());
            addDataRow(sheet, rowNum++, "Admin", stats.getTotalAdmins());

            rowNum++;

            // Akademik İstatistikler
            addSectionHeader(sheet, rowNum++, "Akademik İstatistikler", headerStyle);
            addDataRow(sheet, rowNum++, "Bölüm", stats.getTotalDepartments());
            addDataRow(sheet, rowNum++, "Ders", stats.getTotalCourses());
            addDataRow(sheet, rowNum++, "Section", stats.getTotalSections());
            addDataRow(sheet, rowNum++, "Kayıt", stats.getTotalEnrollments());

            rowNum++;

            // Yoklama İstatistikleri
            addSectionHeader(sheet, rowNum++, "Yoklama İstatistikleri", headerStyle);
            addDataRow(sheet, rowNum++, "Oturum Sayısı", stats.getTotalAttendanceSessions());
            addDataRow(sheet, rowNum++, "Ortalama Katılım (%)", stats.getAverageAttendanceRate());

            rowNum++;

            // Yemek ve Etkinlik
            addSectionHeader(sheet, rowNum++, "Diğer İstatistikler", headerStyle);
            addDataRow(sheet, rowNum++, "Bugünkü Yemek Rezervasyonu", stats.getTotalMealReservationsToday());
            addDataRow(sheet, rowNum++, "Toplam Etkinlik", stats.getTotalEvents());
            addDataRow(sheet, rowNum++, "Yaklaşan Etkinlik", stats.getUpcomingEvents());

            // Kolon genişliği
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Excel export hatası: {}", e.getMessage());
            throw new RuntimeException("Excel export başarısız", e);
        }
    }

    /**
     * Dashboard istatistiklerini CSV formatında export et
     */
    public byte[] exportDashboardToCsv() {
        log.info("Dashboard CSV export başlatıldı");
        DashboardStatsResponse stats = analyticsService.getDashboardStats();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                CSVWriter writer = new CSVWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {

            // Header
            writer.writeNext(new String[] { "Metrik", "Değer" });

            // Veriler
            writer.writeNext(new String[] { "Toplam Kullanıcı", String.valueOf(stats.getTotalUsers()) });
            writer.writeNext(new String[] { "Öğrenci", String.valueOf(stats.getTotalStudents()) });
            writer.writeNext(new String[] { "Öğretim Üyesi", String.valueOf(stats.getTotalFaculty()) });
            writer.writeNext(new String[] { "Admin", String.valueOf(stats.getTotalAdmins()) });
            writer.writeNext(new String[] { "Bölüm", String.valueOf(stats.getTotalDepartments()) });
            writer.writeNext(new String[] { "Ders", String.valueOf(stats.getTotalCourses()) });
            writer.writeNext(new String[] { "Kayıt", String.valueOf(stats.getTotalEnrollments()) });
            writer.writeNext(new String[] { "Yoklama Oturumu", String.valueOf(stats.getTotalAttendanceSessions()) });
            writer.writeNext(
                    new String[] { "Ortalama Katılım (%)", String.format("%.2f", stats.getAverageAttendanceRate()) });
            writer.writeNext(new String[] { "Bugünkü Yemek", String.valueOf(stats.getTotalMealReservationsToday()) });
            writer.writeNext(new String[] { "Toplam Etkinlik", String.valueOf(stats.getTotalEvents()) });

            writer.flush();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("CSV export hatası: {}", e.getMessage());
            throw new RuntimeException("CSV export başarısız", e);
        }
    }

    /**
     * Dashboard istatistiklerini PDF formatında export et
     */
    public byte[] exportDashboardToPdf() {
        log.info("Dashboard PDF export başlatıldı");
        DashboardStatsResponse stats = analyticsService.getDashboardStats();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Başlık
            Paragraph title = new Paragraph("Smart Campus - Dashboard Raporu")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            // Tarih
            document.add(new Paragraph("Oluşturulma: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("\n"));

            // Tablo
            Table table = new Table(UnitValue.createPercentArray(new float[] { 60, 40 }))
                    .useAllAvailableWidth();

            // Header
            table.addHeaderCell(createHeaderCell("Metrik"));
            table.addHeaderCell(createHeaderCell("Değer"));

            // Veriler
            addTableRow(table, "Toplam Kullanıcı", stats.getTotalUsers());
            addTableRow(table, "Öğrenci", stats.getTotalStudents());
            addTableRow(table, "Öğretim Üyesi", stats.getTotalFaculty());
            addTableRow(table, "Admin", stats.getTotalAdmins());
            addTableRow(table, "Bölüm", stats.getTotalDepartments());
            addTableRow(table, "Ders", stats.getTotalCourses());
            addTableRow(table, "Kayıt", stats.getTotalEnrollments());
            addTableRow(table, "Yoklama Oturumu", stats.getTotalAttendanceSessions());
            addTableRow(table, "Ortalama Katılım (%)", String.format("%.2f", stats.getAverageAttendanceRate()));
            addTableRow(table, "Bugünkü Yemek Rezervasyonu", stats.getTotalMealReservationsToday());
            addTableRow(table, "Toplam Etkinlik", stats.getTotalEvents());
            addTableRow(table, "Yaklaşan Etkinlik", stats.getUpcomingEvents());

            document.add(table);
            document.close();

            return out.toByteArray();
        } catch (Exception e) {
            log.error("PDF export hatası: {}", e.getMessage());
            throw new RuntimeException("PDF export başarısız", e);
        }
    }

    /**
     * Akademik istatistikleri Excel formatında export et
     */
    public byte[] exportAcademicToExcel() {
        log.info("Academic Excel export başlatıldı");
        AcademicStatsResponse stats = analyticsService.getAcademicStats();

        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Akademik İstatistikler");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            int rowNum = 0;

            // Genel GPA
            addSectionHeader(sheet, rowNum++, "Genel GPA İstatistikleri", headerStyle);
            addDataRow(sheet, rowNum++, "Ortalama GPA", stats.getAverageGpa());
            addDataRow(sheet, rowNum++, "Ortalama CGPA", stats.getAverageCgpa());
            addDataRow(sheet, rowNum++, "En Yüksek GPA", stats.getHighestGpa());
            addDataRow(sheet, rowNum++, "En Düşük GPA", stats.getLowestGpa());

            rowNum++;

            // Geçme oranı
            addSectionHeader(sheet, rowNum++, "Başarı Oranları", headerStyle);
            addDataRow(sheet, rowNum++, "Geçme Oranı (%)", stats.getPassRate());
            addDataRow(sheet, rowNum++, "Kalma Oranı (%)", stats.getFailRate());

            rowNum++;

            // Not dağılımı
            addSectionHeader(sheet, rowNum++, "Not Dağılımı", headerStyle);
            for (Map.Entry<String, Double> entry : stats.getGradeDistribution().entrySet()) {
                addDataRow(sheet, rowNum++, entry.getKey() + " (%)", entry.getValue());
            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Academic Excel export hatası: {}", e.getMessage());
            throw new RuntimeException("Academic Excel export başarısız", e);
        }
    }

    // ========== Yardımcı metodlar ==========

    private void addSectionHeader(Sheet sheet, int rowNum, String text, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        org.apache.poi.ss.usermodel.Cell cell = row.createCell(0);
        cell.setCellValue(text);
        cell.setCellStyle(style);
    }

    private void addDataRow(Sheet sheet, int rowNum, String label, Object value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        if (value instanceof Number) {
            row.createCell(1).setCellValue(((Number) value).doubleValue());
        } else {
            row.createCell(1).setCellValue(String.valueOf(value));
        }
    }

    private Cell createHeaderCell(String text) {
        Cell cell = new Cell();
        cell.add(new Paragraph(text).setBold());
        cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
        return cell;
    }

    private void addTableRow(Table table, String label, Object value) {
        table.addCell(new Cell().add(new Paragraph(label)));
        table.addCell(new Cell().add(new Paragraph(String.valueOf(value))));
    }
}
