package com.cookbook.service;

import com.cookbook.entity.Ingredient;
import com.cookbook.entity.Recipe;
import com.cookbook.entity.Step;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class PdfService {

    @Value("${app.upload.path}")
    private String uploadPath;

    @Value("${app.upload.url-prefix}")
    private String uploadUrlPrefix;

    public byte[] generateRecipePdf(Recipe recipe) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);

        PdfFont font = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H");
        
        Paragraph title = new Paragraph(recipe.getName())
                .setFont(font)
                .setFontSize(24)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(title);

        if (recipe.getDescription() != null && !recipe.getDescription().isEmpty()) {
            Paragraph desc = new Paragraph(recipe.getDescription())
                    .setFont(font)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(new DeviceRgb(100, 100, 100))
                    .setMarginBottom(20);
            document.add(desc);
        }

        Table infoTable = new Table(UnitValue.createPercentArray(4)).useAllAvailableWidth();
        infoTable.setMarginBottom(20);
        
        addTableCell(infoTable, "菜系", recipe.getCuisine(), font);
        addTableCell(infoTable, "难度", recipe.getDifficulty(), font);
        addTableCell(infoTable, "烹饪时间", recipe.getCookingTime() + "分钟", font);
        addTableCell(infoTable, "食材数量", String.valueOf(recipe.getIngredients() != null ? recipe.getIngredients().size() : 0), font);
        
        document.add(infoTable);

        if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
            Paragraph ingredientsTitle = new Paragraph("食材清单")
                    .setFont(font)
                    .setFontSize(16)
                    .setBold()
                    .setMarginBottom(10)
                    .setMarginTop(10);
            document.add(ingredientsTitle);

            Table ingredientsTable = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();
            ingredientsTable.setMarginBottom(20);
            
            addTableHeaderCell(ingredientsTable, "食材名称", font);
            addTableHeaderCell(ingredientsTable, "用量", font);
            
            for (Ingredient ingredient : recipe.getIngredients()) {
                addTableCell(ingredientsTable, ingredient.getName(), font);
                addTableCell(ingredientsTable, ingredient.getQuantity(), font);
            }
            
            document.add(ingredientsTable);
        }

        if (recipe.getSteps() != null && !recipe.getSteps().isEmpty()) {
            Paragraph stepsTitle = new Paragraph("烹饪步骤")
                    .setFont(font)
                    .setFontSize(16)
                    .setBold()
                    .setMarginBottom(10)
                    .setMarginTop(10);
            document.add(stepsTitle);

            for (Step step : recipe.getSteps()) {
                Paragraph stepTitle = new Paragraph("步骤 " + step.getStepNumber())
                        .setFont(font)
                        .setFontSize(12)
                        .setBold()
                        .setMarginTop(10)
                        .setMarginBottom(5);
                document.add(stepTitle);
                
                Paragraph stepDesc = new Paragraph(step.getDescription())
                        .setFont(font)
                        .setFontSize(11)
                        .setMarginLeft(20);
                document.add(stepDesc);
            }
        }

        document.close();
        return baos.toByteArray();
    }

    private void addTableCell(Table table, String text, PdfFont font) {
        Cell cell = new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(11))
                .setTextAlignment(TextAlignment.CENTER);
        table.addCell(cell);
    }

    private void addTableHeaderCell(Table table, String text, PdfFont font) {
        Cell cell = new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(11).setBold())
                .setTextAlignment(TextAlignment.CENTER)
                .setBackgroundColor(new DeviceRgb(240, 240, 240));
        table.addCell(cell);
    }
}
