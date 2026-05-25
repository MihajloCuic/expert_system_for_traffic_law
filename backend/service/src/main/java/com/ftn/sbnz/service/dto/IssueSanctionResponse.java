package com.ftn.sbnz.service.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.ftn.sbnz.model.ActivePointsSnapshot;
import com.ftn.sbnz.model.LicenseRevocation;
import com.ftn.sbnz.model.Sanction;
import com.ftn.sbnz.model.SanctionSummary;
import com.ftn.sbnz.model.Severity;

/**
 * End-to-end result of POST /api/module1/issue-sanction. Carries every piece
 * of information the police officer / UI needs:
 *   - each individual Sanction produced by Layer 1-2,
 *   - the per-driver SanctionSummary produced by Layer 3 (sticaj prekrsaja),
 *   - the CEP active-points snapshot (Layer 4),
 *   - the LicenseRevocation if Layer 5 fired.
 */
public class IssueSanctionResponse {

    private String driverId;
    private String driverFullName;
    private boolean probationary;

    private List<SanctionDto> sanctions = new ArrayList<>();
    private SummaryDto summary;
    private ActivePointsDto activePoints;
    private RevocationDto revocation;

    public IssueSanctionResponse() {}

    // === Getters / setters ===

    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }

    public String getDriverFullName() { return driverFullName; }
    public void setDriverFullName(String driverFullName) { this.driverFullName = driverFullName; }

    public boolean isProbationary() { return probationary; }
    public void setProbationary(boolean probationary) { this.probationary = probationary; }

    public List<SanctionDto> getSanctions() { return sanctions; }
    public void setSanctions(List<SanctionDto> sanctions) { this.sanctions = sanctions; }

    public SummaryDto getSummary() { return summary; }
    public void setSummary(SummaryDto summary) { this.summary = summary; }

    public ActivePointsDto getActivePoints() { return activePoints; }
    public void setActivePoints(ActivePointsDto activePoints) { this.activePoints = activePoints; }

    public RevocationDto getRevocation() { return revocation; }
    public void setRevocation(RevocationDto revocation) { this.revocation = revocation; }

    // === Nested DTOs ===

    public static class SanctionDto {
        public String lawArticle;
        public Severity severity;
        public int minFine;
        public int maxFine;
        public int finalFine;
        public int basePoints;
        public int finalPoints;
        public int drivingBanDays;
        public int prisonDaysMin;
        public int prisonDaysMax;
        public boolean alternativePenalty;
        public List<String> explanation;

        public static SanctionDto from(Sanction s) {
            SanctionDto d = new SanctionDto();
            d.lawArticle = s.getLawArticle();
            d.severity = s.getSeverity();
            d.minFine = s.getMinFine();
            d.maxFine = s.getMaxFine();
            d.finalFine = s.getFinalFine();
            d.basePoints = s.getBasePoints();
            d.finalPoints = s.getFinalPoints();
            d.drivingBanDays = s.getDrivingBanDays();
            d.prisonDaysMin = s.getPrisonDaysMin();
            d.prisonDaysMax = s.getPrisonDaysMax();
            d.alternativePenalty = s.isAlternativePenalty();
            d.explanation = new ArrayList<>(s.getExplanation());
            return d;
        }
    }

    public static class SummaryDto {
        public int totalFineMin;
        public int totalFineMax;
        public int totalPoints;
        public int drivingBanMinDays;
        public int drivingBanMaxDays;
        public int prisonMinDays;
        public int prisonMaxDays;
        public List<String> overallExplanation;

        public static SummaryDto from(SanctionSummary s) {
            SummaryDto d = new SummaryDto();
            d.totalFineMin = s.getTotalFineMin();
            d.totalFineMax = s.getTotalFineMax();
            d.totalPoints = s.getTotalPoints();
            d.drivingBanMinDays = s.getDrivingBanMinDays();
            d.drivingBanMaxDays = s.getDrivingBanMaxDays();
            d.prisonMinDays = s.getPrisonMinDays();
            d.prisonMaxDays = s.getPrisonMaxDays();
            d.overallExplanation = new ArrayList<>(s.getOverallExplanation());
            return d;
        }
    }

    public static class ActivePointsDto {
        public int activePoints;
        public LocalDate computedAt;

        public static ActivePointsDto from(ActivePointsSnapshot a) {
            ActivePointsDto d = new ActivePointsDto();
            d.activePoints = a.getActivePoints();
            d.computedAt = a.getComputedAt();
            return d;
        }
    }

    public static class RevocationDto {
        public LocalDate revokedAt;
        public int activePointsAtRevocation;
        public boolean probationary;
        public String reason;

        public static RevocationDto from(LicenseRevocation r) {
            RevocationDto d = new RevocationDto();
            d.revokedAt = r.getRevokedAt();
            d.activePointsAtRevocation = r.getActivePointsAtRevocation();
            d.probationary = r.isProbationary();
            d.reason = r.getReason();
            return d;
        }
    }
}
