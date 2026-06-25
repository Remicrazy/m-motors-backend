package com.mmotors.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.mmotors.entity.Document;
import com.mmotors.entity.Dossier;
import com.mmotors.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DossierRepository dossierRepository;
    private final Cloudinary cloudinary;

    public Document upload(String dossierId, MultipartFile file) throws IOException {
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new NoSuchElementException("Dossier introuvable"));

        Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("folder", "m-motors/dossiers/" + dossierId,
                                  "resource_type", "auto"));

        Document doc = Document.builder()
                .dossier(dossier)
                .nom(file.getOriginalFilename())
                .type(file.getContentType())
                .url((String) result.get("secure_url"))
                .publicId((String) result.get("public_id"))
                .build();

        return documentRepository.save(doc);
    }

    public List<Document> findByDossier(String dossierId) {
        return documentRepository.findByDossierId(dossierId);
    }
}
