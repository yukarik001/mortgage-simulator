package com.example.demo;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.List;

import org.springframework.stereotype.Service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.font.FontProvider;
import com.itextpdf.layout.properties.UnitValue;

@Service
public class PdfExportService {

    public byte[] generatePdf(List<MonthlyResult> results) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // ✅ フォントプロバイダーだけで日本語対応（フォントファイル指定なし）
        FontProvider fontProvider = new FontProvider();
        fontProvider.addSystemFonts(); // Windowsの日本語フォントも含まれる
        document.setFontProvider(fontProvider);
        document.setFontFamily("MS Gothic"); // ← ここは環境に応じて "Meiryo" や "Yu Gothic" でもOK

        // タイトル
        document.add(new Paragraph("月別返済表"));

        // 表の作成
        Table table = new Table(6);
        table.setWidth(UnitValue.createPercentValue(100));

        // 見出し
        table.addCell(new Cell().add(new Paragraph("回")));
        table.addCell(new Cell().add(new Paragraph("金利(%)")));
        table.addCell(new Cell().add(new Paragraph("返済額")));
        table.addCell(new Cell().add(new Paragraph("うち元金")));
        table.addCell(new Cell().add(new Paragraph("うち利息")));
        table.addCell(new Cell().add(new Paragraph("ローン残高")));

        // データ行
        for (MonthlyResult result : results) {
            table.addCell(new Cell().add(new Paragraph(String.valueOf(result.getMonth()))));
            table.addCell(new Cell().add(new Paragraph(formatRate(result.getRate()))));
            table.addCell(new Cell().add(new Paragraph(formatNumber(result.getGaku()))));
            table.addCell(new Cell().add(new Paragraph(formatNumber(result.getGan()))));
            table.addCell(new Cell().add(new Paragraph(formatNumber(result.getRisoku()))));
            table.addCell(new Cell().add(new Paragraph(formatNumber(result.getZan()))));
        }

        document.add(table);
        document.close();
        return out.toByteArray();
    }

    private String formatNumber(double value) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(value);
    }

    private String formatRate(double rate) {
        return rate + "%"; // ← そのまま表示して % をつけるだけ
    }

}