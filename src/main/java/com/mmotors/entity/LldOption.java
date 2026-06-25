package com.mmotors.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "lld_options")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LldOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Builder.Default private boolean assurance = false;
    @Builder.Default private boolean assistance = false;
    @Builder.Default private boolean entretien = false;
    @Builder.Default private boolean controleTechnique = false;

    @Column(precision = 10, scale = 2)
    private BigDecimal prixMensuel12;

    @Column(precision = 10, scale = 2)
    private BigDecimal prixMensuel24;

    @Column(precision = 10, scale = 2)
    private BigDecimal prixMensuel36;
}
