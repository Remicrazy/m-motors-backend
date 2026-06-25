package com.mmotors.controller;

import com.mmotors.entity.Document;
import com.mmotors.service.DocumentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@Tag(name = "Documents")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload/{dossierId}")
    public ResponseEntity<Document> upload(@PathVariable String dossierId,
                                            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.upload(dossierId, file));
    }

    @GetMapping("/dossier/{dossierId}")
    public List<Document> findByDossier(@PathVariable String dossierId) {
        return documentService.findByDossier(dossierId);
    }
}
