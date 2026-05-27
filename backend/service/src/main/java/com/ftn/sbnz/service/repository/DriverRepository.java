package com.ftn.sbnz.service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ftn.sbnz.model.sanctions.Driver;

@Repository
public interface DriverRepository extends JpaRepository<Driver, String> {

    /**
     * Look up a driver by their license number. Used by the roadside
     * lookup endpoint - the officer types the license number printed on
     * the physical card and the system answers with the full driver record
     * (or 404 if unknown).
     */
    Optional<Driver> findByLicense_LicenseNumber(String licenseNumber);
}
