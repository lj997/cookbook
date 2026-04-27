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
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
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
                .setMarginBottom(20);
        document.add(title);

        if (recipe.getDescription() != null && !recipe.getDescription().isEmpty()) {
            Paragraph desc = new Paragraph(recipe.getDescription())
                    .setFont(font)
                    .setFontSize(12)
                    .setFontColor(new DeviceRgb(100, 100, 100))
                    .setMarginBottom(20);
            document.add(desc);
        }

        float[] infoColumnWidths = {1, 1, 1, 1};
        Table infoTable = new Table(infoColumnWidths);
        infoTable.setMarginBottom(20);
        
        addTableHeaderCell(infoTable, "菜系", font);
        addTableCell(infoTable, recipe.getCuisine(), font);
        addTableHeaderCell(infoTable, "难度", font);
        addTableCell(infoTable, recipe.getDifficulty(), font);
        addTableHeaderCell(infoTable, "烹饪时间", font);
        addTableCell(infoTable, recipe.getCookingTime() + "分钟", font);
        addTableHeaderCell(infoTable, "食材数量", font);
        addTableCell(infoTable, String.valueOf(recipe.getIngredients() != null ? recipe.getIngredients().size() : 0), font);
        
        document.add(infoTable);

        if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
            Paragraph ingredientsTitle = new Paragraph("食材清单")
                    .setFont(font)
                    .setFontSize(16)
                    .setBold()
                    .setMarginBottom(10)
                    .setMarginTop(10);
            document.add(ingredientsTitle);

            float[] ingColumnWidths = {1, 1};
            Table ingredientsTable = new Table(ingColumnWidths);
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
                .add(new Paragraph(text).setFont(font).setFontSize(11));
        table.addCell(cell);
    }

    private void addTableHeaderCell(Table table, String text, PdfFont font) {
        Cell cell = new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(11).setBold())
                .setBackgroundColor(new DeviceRgb(240, 240, 240));
        table.addCell(cell);
    }
}
