package com.mmotors.service;

import com.mmotors.entity.*;
import com.mmotors.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DossierService {

    private final DossierRepository dossierRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    @Transactional
    public Dossier create(String clientEmail, String vehicleId,
                          Dossier.Type type, Integer dureeSouhaitee) {
        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur introuvable"));
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new NoSuchElementException("Véhicule introuvable"));

        Dossier dossier = Dossier.builder()
                .client(client)
                .vehicle(vehicle)
                .type(type)
                .dureeSouhaitee(dureeSouhaitee)
                .statut(Dossier.Statut.EN_ATTENTE)
                .build();

        vehicle.setStatut(Vehicle.Statut.EN_COURS);
        vehicleRepository.save(vehicle);

        return dossierRepository.save(dossier);
    }

    public List<Dossier> findByClient(String clientEmail) {
        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur introuvable"));
        return dossierRepository.findByClientIdOrderByCreatedAtDesc(client.getId());
    }

    public List<Dossier> findAll() {
        return dossierRepository.findAllByOrderByCreatedAtDesc();
    }

    public Dossier findById(String id, String requesterEmail, boolean isAdmin) {
        Dossier dossier = dossierRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Dossier introuvable"));
        if (!isAdmin && !dossier.getClient().getEmail().equals(requesterEmail)) {
            throw new AccessDeniedException("Accès refusé");
        }
        return dossier;
    }

    @Transactional
    public Dossier updateStatut(String id, Dossier.Statut statut, String motifRefus) {
        Dossier dossier = dossierRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Dossier introuvable"));
        dossier.setStatut(statut);
        if (motifRefus != null) dossier.setMotifRefus(motifRefus);

        Vehicle vehicle = dossier.getVehicle();
        if (statut == Dossier.Statut.VALIDE) {
            vehicle.setStatut(Vehicle.Statut.VENDU);
        } else if (statut == Dossier.Statut.REFUSE) {
            vehicle.setStatut(Vehicle.Statut.DISPONIBLE);
        }
        vehicleRepository.save(vehicle);

        return dossierRepository.save(dossier);
    }
}
