package com.ftn.sbnz.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ftn.sbnz.model.Driver;

@Repository
public interface DriverRepository extends JpaRepository<Driver, String> {
}
