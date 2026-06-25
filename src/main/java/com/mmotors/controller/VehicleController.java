package com.mmotors.controller;

import com.mmotors.entity.Vehicle;
import com.mmotors.service.VehicleService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@Tag(name = "Vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    public List<Vehicle> findAll(
            @RequestParam(required = false) Vehicle.Type type,
            @RequestParam(required = false) String marque,
            @RequestParam(required = false) String modele,
            @RequestParam(required = false) BigDecimal prixMax,
            @RequestParam(required = false) Integer kmMax) {
        return vehicleService.findAll(type, marque, modele, prixMax, kmMax);
    }

    @GetMapping("/{id}")
    public Vehicle findOne(@PathVariable String id) {
        return vehicleService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Vehicle> create(@RequestBody Vehicle vehicle) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.create(vehicle));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public Vehicle update(@PathVariable String id, @RequestBody Vehicle updates) {
        return vehicleService.update(id, updates);
    }

    @PatchMapping("/{id}/toggle-type")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public Vehicle toggleType(@PathVariable String id) {
        return vehicleService.toggleType(id);
    }
}
