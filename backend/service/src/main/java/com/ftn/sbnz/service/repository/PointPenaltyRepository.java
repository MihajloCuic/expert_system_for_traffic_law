package com.ftn.sbnz.service.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ftn.sbnz.model.PointPenalty;

@Repository
public interface PointPenaltyRepository extends JpaRepository<PointPenalty, Long> {

    /**
     * All penalty points issued to a driver that are still within the 24-month
     * window (i.e. issued on or after {@code cutoff}).
     */
    @Query("SELECT p FROM PointPenalty p " +
           "WHERE p.driver.id = :driverId AND p.issuedAt >= :cutoff " +
           "ORDER BY p.issuedAt ASC")
    List<PointPenalty> findActiveForDriver(@Param("driverId") String driverId,
                                           @Param("cutoff") Date cutoff);

    List<PointPenalty> findByDriverId(String driverId);
}
