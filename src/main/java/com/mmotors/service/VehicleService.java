package com.mmotors.service;

import com.mmotors.entity.*;
import com.mmotors.repository.VehicleRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public List<Vehicle> findAll(Vehicle.Type type, String marque, String modele,
                                  BigDecimal prixMax, Integer kmMax) {
        return vehicleRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("statut"), Vehicle.Statut.DISPONIBLE));

            if (type != null)    predicates.add(cb.equal(root.get("type"), type));
            if (marque != null)  predicates.add(cb.like(cb.lower(root.get("marque")), "%" + marque.toLowerCase() + "%"));
            if (modele != null)  predicates.add(cb.like(cb.lower(root.get("modele")), "%" + modele.toLowerCase() + "%"));
            if (prixMax != null) predicates.add(cb.lessThanOrEqualTo(root.get("prix"), prixMax));
            if (kmMax != null)   predicates.add(cb.lessThanOrEqualTo(root.get("km"), kmMax));

            query.orderBy(cb.desc(root.get("createdAt")));
            return cb.and(predicates.toArray(new Predicate[0]));
        });
    }

    public Vehicle findById(String id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Véhicule introuvable : " + id));
    }

    public Vehicle create(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    public Vehicle update(String id, Vehicle updates) {
        Vehicle existing = findById(id);
        if (updates.getMarque() != null)      existing.setMarque(updates.getMarque());
        if (updates.getModele() != null)      existing.setModele(updates.getModele());
        if (updates.getPrix() != null)        existing.setPrix(updates.getPrix());
        if (updates.getStatut() != null)      existing.setStatut(updates.getStatut());
        if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
        return vehicleRepository.save(existing);
    }

    public Vehicle toggleType(String id) {
        Vehicle vehicle = findById(id);
        boolean hasActiveDossier = vehicle.getDossiers() != null && vehicle.getDossiers().stream()
                .anyMatch(d -> d.getStatut() == Dossier.Statut.EN_ATTENTE
                            || d.getStatut() == Dossier.Statut.EN_COURS);

        if (hasActiveDossier) {
            throw new IllegalStateException("Ce véhicule a un dossier en cours et ne peut pas être basculé");
        }

        vehicle.setType(vehicle.getType() == Vehicle.Type.ACHAT
                ? Vehicle.Type.LOCATION : Vehicle.Type.ACHAT);
        return vehicleRepository.save(vehicle);
    }
}
