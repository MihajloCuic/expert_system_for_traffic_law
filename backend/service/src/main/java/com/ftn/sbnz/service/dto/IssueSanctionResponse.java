package com.ftn.sbnz.service.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.ftn.sbnz.model.sanctions.ActivePointsSnapshot;
import com.ftn.sbnz.model.sanctions.Driver;
import com.ftn.sbnz.model.sanctions.DrivingLicense;
import com.ftn.sbnz.model.sanctions.LicenseCategory;
import com.ftn.sbnz.model.sanctions.LicenseRevocation;
import com.ftn.sbnz.model.sanctions.LicenseType;
import com.ftn.sbnz.model.sanctions.Sanction;
import com.ftn.sbnz.model.sanctions.SanctionSummary;
import com.ftn.sbnz.model.sanctions.Severity;

/**
 * End-to-end result of POST /api/module1/issue-sanction. Carries every piece
 * of information the police officer / UI needs:
 *   - the driver and (optional) license,
 *   - each individual Sanction produced by Layer 1-4,
 *   - the per-driver SanctionSummary produced by Layer 5 (sticaj prekrsaja),
 *   - the CEP active-points snapshot (Layer 7),
 *   - the LicenseRevocation if Layer 8 fired.
 */
public class IssueSanctionResponse {

    private DriverDto driver;
    private boolean probationary;

    private List<SanctionDto> sanctions = new ArrayList<>();
    private SummaryDto summary;
    private ActivePointsDto activePoints;
    private RevocationDto revocation;

    public IssueSanctionResponse() {}

    public DriverDto getDriver() { return driver; }
    public void setDriver(DriverDto driver) { this.driver = driver; }

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

    // ============================================================
    // Nested DTOs
    // ============================================================

    public static class DriverDto {
        public String id;
        public String firstName;
        public String lastName;
        public String fullName;
        public LocalDate dateOfBirth;
        public LicenseDto license;

        public static DriverDto from(Driver d) {
            DriverDto dto = new DriverDto();
            dto.id = d.getId();
            dto.firstName = d.getFirstName();
            dto.lastName = d.getLastName();
            dto.fullName = d.getFullName();
            dto.dateOfBirth = d.getDateOfBirth();
            dto.license = d.getLicense() == null ? null : LicenseDto.from(d.getLicense());
            return dto;
        }
    }

    public static class LicenseDto {
        public String licenseNumber;
        public LocalDate issuedAt;
        public LicenseType type;
        public Set<LicenseCategory> categories;

        public static LicenseDto from(DrivingLicense l) {
            LicenseDto dto = new LicenseDto();
            dto.licenseNumber = l.getLicenseNumber();
            dto.issuedAt = l.getIssuedAt();
            dto.type = l.getType();
            dto.categories = l.getCategories();
            return dto;
        }
    }

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
