package com.cookbook.controller;

import com.cookbook.entity.Recipe;
import com.cookbook.service.PdfService;
import com.cookbook.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    @Autowired
    private PdfService pdfService;

    @Autowired
    private RecipeService recipeService;

    @GetMapping("/recipe/{id}/pdf")
    public ResponseEntity<byte[]> exportRecipeToPdf(@PathVariable Long id) {
        Recipe recipe = recipeService.findById(id)
                .orElse(null);
        
        if (recipe == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        try {
            byte[] pdfBytes = pdfService.generateRecipePdf(recipe);
            
            String filename = URLEncoder.encode(recipe.getName() + ".pdf", StandardCharsets.UTF_8)
                    .replace("+", "%20");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfBytes.length);
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/recipe/{id}/preview")
    public ResponseEntity<byte[]> previewRecipePdf(@PathVariable Long id) {
        Recipe recipe = recipeService.findById(id)
                .orElse(null);
        
        if (recipe == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        try {
            byte[] pdfBytes = pdfService.generateRecipePdf(recipe);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", recipe.getName() + ".pdf");
            headers.setContentLength(pdfBytes.length);
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
