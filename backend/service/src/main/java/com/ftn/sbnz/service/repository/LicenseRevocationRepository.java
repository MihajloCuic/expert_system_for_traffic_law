package com.ftn.sbnz.service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ftn.sbnz.model.LicenseRevocation;

@Repository
public interface LicenseRevocationRepository extends JpaRepository<LicenseRevocation, Long> {

    /**
     * Returns the active (most recent) revocation for the driver, if any.
     * A driver should have at most one revocation in this simple model.
     */
    Optional<LicenseRevocation> findFirstByDriverIdOrderByRevokedAtDesc(String driverId);
}
