package com.mmotors;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.mmotors.entity.Document;
import com.mmotors.entity.Dossier;
import com.mmotors.repository.DocumentRepository;
import com.mmotors.repository.DossierRepository;
import com.mmotors.service.DocumentService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentService — Tests unitaires")
class DocumentServiceTest {

    @Mock DocumentRepository documentRepository;
    @Mock DossierRepository dossierRepository;
    @Mock Cloudinary cloudinary;
    @Mock Uploader uploader;

    @InjectMocks DocumentService documentService;

    private Dossier dossierTest;

    @BeforeEach
    void setUp() {
        dossierTest = Dossier.builder()
                .id("dossier-1")
                .type(Dossier.Type.ACHAT)
                .statut(Dossier.Statut.EN_ATTENTE)
                .build();
    }

    @Test
    @DisplayName("upload — succès : document sauvegardé avec URL Cloudinary")
    void upload_success_returnsDocumentWithUrl() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "identite.pdf", "application/pdf", "contenu-pdf".getBytes());

        Map<String, String> cloudinaryResult = Map.of(
                "secure_url", "https://res.cloudinary.com/test/identite.pdf",
                "public_id", "m-motors/dossiers/dossier-1/identite"
        );

        when(dossierRepository.findById("dossier-1")).thenReturn(Optional.of(dossierTest));
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(cloudinaryResult);
        when(documentRepository.save(any(Document.class))).thenAnswer(i -> i.getArgument(0));

        Document result = documentService.upload("dossier-1", file);

        assertThat(result.getNom()).isEqualTo("identite.pdf");
        assertThat(result.getType()).isEqualTo("application/pdf");
        assertThat(result.getUrl()).isEqualTo("https://res.cloudinary.com/test/identite.pdf");
        assertThat(result.getPublicId()).isEqualTo("m-motors/dossiers/dossier-1/identite");
        verify(documentRepository).save(any(Document.class));
    }

    @Test
    @DisplayName("upload — dossier introuvable lève exception")
    void upload_dossierNotFound_throwsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", "data".getBytes());

        when(dossierRepository.findById("inexistant")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.upload("inexistant", file))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Dossier introuvable");
    }

    @Test
    @DisplayName("upload — erreur Cloudinary propage l'exception")
    void upload_cloudinaryError_throwsIOException() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.jpg", "image/jpeg", "data".getBytes());

        when(dossierRepository.findById("dossier-1")).thenReturn(Optional.of(dossierTest));
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap()))
                .thenThrow(new IOException("Cloudinary unavailable"));

        assertThatThrownBy(() -> documentService.upload("dossier-1", file))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Cloudinary unavailable");
    }

    @Test
    @DisplayName("findByDossier — retourne les documents du dossier")
    void findByDossier_returnsDocuments() {
        Document doc1 = Document.builder().id("d1").nom("identite.pdf").url("https://url1").build();
        Document doc2 = Document.builder().id("d2").nom("salaire.pdf").url("https://url2").build();

        when(documentRepository.findByDossierId("dossier-1")).thenReturn(List.of(doc1, doc2));

        List<Document> result = documentService.findByDossier("dossier-1");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Document::getNom)
                .containsExactlyInAnyOrder("identite.pdf", "salaire.pdf");
    }

    @Test
    @DisplayName("findByDossier — retourne liste vide si aucun document")
    void findByDossier_noDocuments_returnsEmpty() {
        when(documentRepository.findByDossierId("dossier-vide")).thenReturn(List.of());

        List<Document> result = documentService.findByDossier("dossier-vide");

        assertThat(result).isEmpty();
    }
}
