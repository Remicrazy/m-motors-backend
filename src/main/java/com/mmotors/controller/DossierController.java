package com.mmotors.controller;

import com.mmotors.entity.Dossier;
import com.mmotors.service.DossierService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dossiers")
@Tag(name = "Dossiers")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class DossierController {

    private final DossierService dossierService;

    @PostMapping
    public ResponseEntity<Dossier> create(@AuthenticationPrincipal UserDetails user,
                                           @RequestBody CreateDossierRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dossierService.create(user.getUsername(), req.vehicleId(), req.type(), req.dureeSouhaitee()));
    }

    @GetMapping("/mes-dossiers")
    public List<Dossier> findMine(@AuthenticationPrincipal UserDetails user) {
        return dossierService.findByClient(user.getUsername());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Dossier> findAll() {
        return dossierService.findAll();
    }

    @GetMapping("/{id}")
    public Dossier findOne(@PathVariable String id,
                            @AuthenticationPrincipal UserDetails user) {
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return dossierService.findById(id, user.getUsername(), isAdmin);
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasRole('ADMIN')")
    public Dossier updateStatut(@PathVariable String id,
                                 @RequestBody Map<String, String> body) {
        return dossierService.updateStatut(id,
                Dossier.Statut.valueOf(body.get("statut")),
                body.get("motifRefus"));
    }

    record CreateDossierRequest(String vehicleId, Dossier.Type type, Integer dureeSouhaitee) {}
}
