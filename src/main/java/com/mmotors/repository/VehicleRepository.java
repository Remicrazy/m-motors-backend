package com.mmotors.repository;

import com.mmotors.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VehicleRepository extends JpaRepository<Vehicle, String>,
        JpaSpecificationExecutor<Vehicle> {
}
