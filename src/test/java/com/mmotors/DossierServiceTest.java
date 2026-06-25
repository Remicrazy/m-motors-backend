package com.mmotors;

import com.mmotors.entity.*;
import com.mmotors.repository.*;
import com.mmotors.service.DossierService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DossierService — Tests unitaires")
class DossierServiceTest {

    @Mock DossierRepository dossierRepository;
    @Mock VehicleRepository vehicleRepository;
    @Mock UserRepository userRepository;
    @InjectMocks DossierService dossierService;

    private User clientTest;
    private Vehicle vehicleTest;
    private Dossier dossierTest;

    @BeforeEach
    void setUp() {
        clientTest = User.builder()
                .id("user-1")
                .nom("Dupont").prenom("Jean")
                .email("jean@test.fr")
                .role(User.Role.CLIENT)
                .build();

        vehicleTest = Vehicle.builder()
                .id("v-1")
                .marque("Renault").modele("Clio")
                .prix(new BigDecimal("13500"))
                .type(Vehicle.Type.ACHAT)
                .statut(Vehicle.Statut.DISPONIBLE)
                .build();

        dossierTest = Dossier.builder()
                .id("d-1")
                .client(clientTest)
                .vehicle(vehicleTest)
                .type(Dossier.Type.ACHAT)
                .statut(Dossier.Statut.EN_ATTENTE)
                .documents(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("create — crée dossier et passe le véhicule EN_COURS")
    void create_success_vehicleSetToEnCours() {
        when(userRepository.findByEmail("jean@test.fr")).thenReturn(Optional.of(clientTest));
        when(vehicleRepository.findById("v-1")).thenReturn(Optional.of(vehicleTest));
        when(dossierRepository.save(any(Dossier.class))).thenAnswer(i -> i.getArgument(0));

        var result = dossierService.create("jean@test.fr", "v-1", Dossier.Type.ACHAT, null);

        assertThat(result.getStatut()).isEqualTo(Dossier.Statut.EN_ATTENTE);
        assertThat(result.getType()).isEqualTo(Dossier.Type.ACHAT);
        assertThat(vehicleTest.getStatut()).isEqualTo(Vehicle.Statut.EN_COURS);
        verify(vehicleRepository).save(vehicleTest);
    }

    @Test
    @DisplayName("create — lève exception si véhicule introuvable")
    void create_vehicleNotFound_throwsException() {
        when(userRepository.findByEmail("jean@test.fr")).thenReturn(Optional.of(clientTest));
        when(vehicleRepository.findById("v-inexistant")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                dossierService.create("jean@test.fr", "v-inexistant", Dossier.Type.ACHAT, null))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Véhicule introuvable");
    }

    @Test
    @DisplayName("create — dossier LLD avec durée souhaitée")
    void create_lldDossier_withDuree() {
        when(userRepository.findByEmail("jean@test.fr")).thenReturn(Optional.of(clientTest));
        when(vehicleRepository.findById("v-1")).thenReturn(Optional.of(vehicleTest));
        when(dossierRepository.save(any(Dossier.class))).thenAnswer(i -> i.getArgument(0));

        var result = dossierService.create("jean@test.fr", "v-1", Dossier.Type.LOCATION, 24);

        assertThat(result.getDureeSouhaitee()).isEqualTo(24);
        assertThat(result.getType()).isEqualTo(Dossier.Type.LOCATION);
    }

    @Test
    @DisplayName("findByClient — retourne dossiers du client connecté")
    void findByClient_returnsClientDossiers() {
        when(userRepository.findByEmail("jean@test.fr")).thenReturn(Optional.of(clientTest));
        when(dossierRepository.findByClientIdOrderByCreatedAtDesc("user-1"))
                .thenReturn(List.of(dossierTest));

        var result = dossierService.findByClient("jean@test.fr");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(Dossier.Type.ACHAT);
    }

    @Test
    @DisplayName("findAll — admin voit tous les dossiers")
    void findAll_returnsAllDossiers() {
        when(dossierRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(dossierTest));

        var result = dossierService.findAll();

        assertThat(result).hasSize(1);
        verify(dossierRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("findById — client accède à son propre dossier")
    void findById_clientAccessesOwnDossier() {
        when(dossierRepository.findById("d-1")).thenReturn(Optional.of(dossierTest));

        var result = dossierService.findById("d-1", "jean@test.fr", false);

        assertThat(result.getId()).isEqualTo("d-1");
    }

    @Test
    @DisplayName("findById — client refusé sur dossier d'un autre")
    void findById_clientAccessesOtherDossier_throwsForbidden() {
        when(dossierRepository.findById("d-1")).thenReturn(Optional.of(dossierTest));

        assertThatThrownBy(() ->
                dossierService.findById("d-1", "autre@test.fr", false))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("findById — admin accède à tout dossier")
    void findById_adminAccessesAnyDossier() {
        when(dossierRepository.findById("d-1")).thenReturn(Optional.of(dossierTest));

        var result = dossierService.findById("d-1", "admin@mmotors.fr", true);

        assertThat(result.getId()).isEqualTo("d-1");
    }

    @Test
    @DisplayName("updateStatut — VALIDE passe le véhicule en VENDU")
    void updateStatut_valide_vehicleSetToVendu() {
        when(dossierRepository.findById("d-1")).thenReturn(Optional.of(dossierTest));
        when(dossierRepository.save(any(Dossier.class))).thenAnswer(i -> i.getArgument(0));

        var result = dossierService.updateStatut("d-1", Dossier.Statut.VALIDE, null);

        assertThat(result.getStatut()).isEqualTo(Dossier.Statut.VALIDE);
        assertThat(vehicleTest.getStatut()).isEqualTo(Vehicle.Statut.VENDU);
        verify(vehicleRepository).save(vehicleTest);
    }

    @Test
    @DisplayName("updateStatut — REFUSE avec motif remet le véhicule DISPONIBLE")
    void updateStatut_refuse_vehicleSetToDisponible() {
        vehicleTest.setStatut(Vehicle.Statut.EN_COURS);
        when(dossierRepository.findById("d-1")).thenReturn(Optional.of(dossierTest));
        when(dossierRepository.save(any(Dossier.class))).thenAnswer(i -> i.getArgument(0));

        var result = dossierService.updateStatut("d-1", Dossier.Statut.REFUSE, "Revenus insuffisants");

        assertThat(result.getStatut()).isEqualTo(Dossier.Statut.REFUSE);
        assertThat(result.getMotifRefus()).isEqualTo("Revenus insuffisants");
        assertThat(vehicleTest.getStatut()).isEqualTo(Vehicle.Statut.DISPONIBLE);
    }

    @Test
    @DisplayName("updateStatut — dossier introuvable lève exception")
    void updateStatut_notFound_throwsException() {
        when(dossierRepository.findById("inexistant")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                dossierService.updateStatut("inexistant", Dossier.Statut.VALIDE, null))
                .isInstanceOf(NoSuchElementException.class);
    }
}
