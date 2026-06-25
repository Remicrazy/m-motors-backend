package com.mmotors.config;

import com.mmotors.entity.*;
import com.mmotors.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createAdminIfNotExists();
        createClientIfNotExists();
        createSampleVehiclesIfEmpty();
    }

    private void createAdminIfNotExists() {
        if (userRepository.existsByEmail("admin@mmotors.fr")) return;

        User admin = User.builder()
                .nom("Admin")
                .prenom("M-Motors")
                .email("admin@mmotors.fr")
                .telephone("0100000000")
                .password(passwordEncoder.encode("Admin123!"))
                .role(User.Role.ADMIN)
                .build();
        userRepository.save(admin);
        log.info("Compte admin créé : admin@mmotors.fr / Admin123!");
    }

    private void createClientIfNotExists() {
        if (userRepository.existsByEmail("client@test.fr")) return;

        User client = User.builder()
                .nom("Dupont")
                .prenom("Jean")
                .email("client@test.fr")
                .telephone("0612345678")
                .password(passwordEncoder.encode("Client123!"))
                .role(User.Role.CLIENT)
                .build();
        userRepository.save(client);
        log.info("Compte client créé : client@test.fr / Client123!");
    }

    private void createSampleVehiclesIfEmpty() {
        if (vehicleRepository.count() > 0) return;

        List<Vehicle> vehicles = List.of(
            Vehicle.builder()
                .marque("Renault").modele("Clio V").annee(2021)
                .km(35000).prix(new BigDecimal("13500"))
                .type(Vehicle.Type.ACHAT).statut(Vehicle.Statut.DISPONIBLE)
                .description("Renault Clio V 1.0 TCe 100ch — Climatisation, GPS, Bluetooth")
                .build(),

            Vehicle.builder()
                .marque("Peugeot").modele("308").annee(2020)
                .km(52000).prix(new BigDecimal("16900"))
                .type(Vehicle.Type.ACHAT).statut(Vehicle.Statut.DISPONIBLE)
                .description("Peugeot 308 1.5 BlueHDi 130ch — Toit panoramique, Camera de recul")
                .build(),

            Vehicle.builder()
                .marque("Volkswagen").modele("Golf VIII").annee(2022)
                .km(18000).prix(new BigDecimal("24500"))
                .type(Vehicle.Type.ACHAT).statut(Vehicle.Statut.DISPONIBLE)
                .description("VW Golf VIII 1.5 eTSI 150ch DSG — Full LED, Lane Assist")
                .build(),

            Vehicle.builder()
                .marque("Toyota").modele("Yaris").annee(2023)
                .km(8000).prix(new BigDecimal("320"))
                .type(Vehicle.Type.LOCATION).statut(Vehicle.Statut.DISPONIBLE)
                .description("Toyota Yaris Hybride — Parfaite en ville, très économique")
                .build(),

            Vehicle.builder()
                .marque("Renault").modele("Megane E-Tech").annee(2023)
                .km(5000).prix(new BigDecimal("450"))
                .type(Vehicle.Type.LOCATION).statut(Vehicle.Statut.DISPONIBLE)
                .description("Renault Megane E-Tech 100% électrique — Autonomie 450km")
                .build()
        );

        vehicleRepository.saveAll(vehicles);

        // Ajouter les options LLD aux véhicules de location
        vehicles.stream()
            .filter(v -> v.getType() == Vehicle.Type.LOCATION)
            .forEach(v -> {
                LldOption lld = LldOption.builder()
                        .vehicle(v)
                        .assurance(true)
                        .assistance(true)
                        .entretien(true)
                        .controleTechnique(true)
                        .prixMensuel12(v.getPrix().multiply(new BigDecimal("1.15")))
                        .prixMensuel24(v.getPrix())
                        .prixMensuel36(v.getPrix().multiply(new BigDecimal("0.90")))
                        .build();
                v.setLldOption(lld);
                vehicleRepository.save(v);
            });

        log.info("5 véhicules de démonstration créés (3 achat + 2 location LLD)");
    }
}
