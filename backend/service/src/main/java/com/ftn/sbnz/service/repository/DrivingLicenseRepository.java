package com.ftn.sbnz.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ftn.sbnz.model.sanctions.DrivingLicense;

@Repository
public interface DrivingLicenseRepository extends JpaRepository<DrivingLicense, String> {
}
