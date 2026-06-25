package com.mmotors;

import com.mmotors.entity.Dossier;
import com.mmotors.entity.LldOption;
import com.mmotors.entity.Vehicle;
import com.mmotors.repository.VehicleRepository;
import com.mmotors.service.VehicleService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VehicleService — Tests unitaires")
class VehicleServiceTest {

    @Mock VehicleRepository vehicleRepository;
    @InjectMocks VehicleService vehicleService;

    private Vehicle vehicleAchat;
    private Vehicle vehicleLocation;

    @BeforeEach
    void setUp() {
        vehicleAchat = Vehicle.builder()
                .id("v-achat-1")
                .marque("Renault").modele("Clio").annee(2021)
                .km(35000).prix(new BigDecimal("13500"))
                .type(Vehicle.Type.ACHAT)
                .statut(Vehicle.Statut.DISPONIBLE)
                .dossiers(new ArrayList<>())
                .build();

        vehicleLocation = Vehicle.builder()
                .id("v-loc-1")
                .marque("Toyota").modele("Yaris").annee(2023)
                .km(8000).prix(new BigDecimal("320"))
                .type(Vehicle.Type.LOCATION)
                .statut(Vehicle.Statut.DISPONIBLE)
                .dossiers(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("findAll — retourne la liste des véhicules disponibles")
    void findAll_returnsAvailableVehicles() {
        when(vehicleRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(vehicleAchat, vehicleLocation));

        var result = vehicleService.findAll(null, null, null, null, null);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Vehicle::getMarque)
                .containsExactlyInAnyOrder("Renault", "Toyota");
    }

    @Test
    @DisplayName("findAll — retourne liste vide si aucun véhicule")
    void findAll_empty_returnsEmptyList() {
        when(vehicleRepository.findAll(any(Specification.class))).thenReturn(List.of());

        var result = vehicleService.findAll(Vehicle.Type.ACHAT, null, null, null, null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findById — succès avec ID existant")
    void findById_success() {
        when(vehicleRepository.findById("v-achat-1")).thenReturn(Optional.of(vehicleAchat));

        var result = vehicleService.findById("v-achat-1");

        assertThat(result).isNotNull();
        assertThat(result.getMarque()).isEqualTo("Renault");
        assertThat(result.getPrix()).isEqualByComparingTo("13500");
    }

    @Test
    @DisplayName("findById — lève exception si ID inexistant")
    void findById_notFound_throwsException() {
        when(vehicleRepository.findById("id-inexistant")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.findById("id-inexistant"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Véhicule introuvable");
    }

    @Test
    @DisplayName("create — sauvegarde et retourne le véhicule")
    void create_savesAndReturnsVehicle() {
        when(vehicleRepository.save(vehicleAchat)).thenReturn(vehicleAchat);

        var result = vehicleService.create(vehicleAchat);

        assertThat(result.getMarque()).isEqualTo("Renault");
        assertThat(result.getType()).isEqualTo(Vehicle.Type.ACHAT);
        verify(vehicleRepository).save(vehicleAchat);
    }

    @Test
    @DisplayName("toggleType — bascule ACHAT vers LOCATION")
    void toggleType_achatToLocation() {
        when(vehicleRepository.findById("v-achat-1")).thenReturn(Optional.of(vehicleAchat));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(i -> i.getArgument(0));

        var result = vehicleService.toggleType("v-achat-1");

        assertThat(result.getType()).isEqualTo(Vehicle.Type.LOCATION);
    }

    @Test
    @DisplayName("toggleType — bascule LOCATION vers ACHAT")
    void toggleType_locationToAchat() {
        when(vehicleRepository.findById("v-loc-1")).thenReturn(Optional.of(vehicleLocation));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(i -> i.getArgument(0));

        var result = vehicleService.toggleType("v-loc-1");

        assertThat(result.getType()).isEqualTo(Vehicle.Type.ACHAT);
    }

    @Test
    @DisplayName("toggleType — bloqué si dossier actif en cours")
    void toggleType_withActiveDossier_throwsException() {
        Dossier dossierActif = Dossier.builder()
                .statut(Dossier.Statut.EN_ATTENTE)
                .build();
        vehicleAchat.setDossiers(List.of(dossierActif));

        when(vehicleRepository.findById("v-achat-1")).thenReturn(Optional.of(vehicleAchat));

        assertThatThrownBy(() -> vehicleService.toggleType("v-achat-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dossier en cours");
    }

    @Test
    @DisplayName("update — modifie les champs fournis")
    void update_modifiesProvidedFields() {
        Vehicle updates = Vehicle.builder()
                .prix(new BigDecimal("12000"))
                .description("Nouvelle description")
                .build();

        when(vehicleRepository.findById("v-achat-1")).thenReturn(Optional.of(vehicleAchat));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(i -> i.getArgument(0));

        var result = vehicleService.update("v-achat-1", updates);

        assertThat(result.getPrix()).isEqualByComparingTo("12000");
        assertThat(result.getDescription()).isEqualTo("Nouvelle description");
        assertThat(result.getMarque()).isEqualTo("Renault"); // inchangé
    }
}
