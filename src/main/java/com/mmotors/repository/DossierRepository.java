package com.mmotors.repository;

import com.mmotors.entity.Dossier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface DossierRepository extends JpaRepository<Dossier, String> {

    List<Dossier> findByClientIdOrderByCreatedAtDesc(String clientId);

    List<Dossier> findAllByOrderByCreatedAtDesc();

    @Query("SELECT COUNT(d) > 0 FROM Dossier d WHERE d.vehicle.id = :vehicleId " +
           "AND d.statut IN (com.mmotors.entity.Dossier.Statut.EN_ATTENTE, com.mmotors.entity.Dossier.Statut.EN_COURS)")
    boolean existsActiveByVehicleId(String vehicleId);
}
