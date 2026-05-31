import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map, of, catchError } from 'rxjs';

import {
  DriverRecord, LicenceType, Range, ViolationResult, ViolationSubmission,
} from '../../shared/types';
import { parseSerbianDecimal, SPEEDING_OPTIONS } from '../../shared/catalogues';

/**
 * The actual HTTP shapes returned by the Spring backend
 * (see backend `IssueSanctionResponse`). Adapters in this file translate
 * to/from the CLAUDE.md-defined frontend shapes so the components stay
 * domain-language-clean.
 */
interface BackendIssueSanctionResponse {
  driver: {
    id: string;
    firstName: string;
    lastName: string;
    fullName: string;
    dateOfBirth: string | null;
    license: BackendLicense | null;
  };
  probationary: boolean;
  sanctions: BackendSanction[];
  summary: BackendSummary | null;
  activePoints: { activePoints: number; computedAt: string } | null;
  revocation: BackendRevocation | null;
}

interface BackendLicense {
  licenseNumber: string;
  issuedAt: string | null;
  type: 'PERMANENT' | 'PROBATIONARY';
  categories: string[];
}

interface BackendSanction {
  lawArticle: string;
  severity: string;
  minFine: number;
  maxFine: number;
  finalFine: number;
  basePoints: number;
  finalPoints: number;
  drivingBanDays: number;
  prisonDaysMin: number;
  prisonDaysMax: number;
  alternativePenalty: boolean;
  explanation: string[];
}

interface BackendSummary {
  totalFineMin: number;
  totalFineMax: number;
  totalPoints: number;
  drivingBanMinDays: number;
  drivingBanMaxDays: number;
  prisonMinDays: number;
  prisonMaxDays: number;
  overallExplanation: string[];
}

interface BackendRevocation {
  revokedAt: string;
  activePointsAtRevocation: number;
  probationary: boolean;
  reason: string;
}

/* ============================================================
   Service
   ============================================================ */
@Injectable({ providedIn: 'root' })
export class SankcijeService {
  private readonly http = inject(HttpClient);

  /**
   * Roadside lookup. Calls `GET /api/module1/driver/by-license/{number}`
   * and adapts the backend response to the CLAUDE.md `DriverRecord` shape.
   * Returns null on HTTP 404.
   */
  getDriver(licNum: string): Observable<DriverRecord | null> {
    const trimmed = licNum.trim();
    return this.http
      .get<BackendIssueSanctionResponse>(`http://localhost:8080/api/module1/driver/by-license/${encodeURIComponent(trimmed)}`)
      .pipe(
        map(resp => toDriverRecord(resp)),
        catchError(err => {
          // HTTP 404 = unknown licence; surface as null and let the
          // component render the not-found state.
          if (err?.status === 404) return of(null);
          throw err;
        }),
      );
  }

  /**
   * Submit a violation report. Translates the CLAUDE.md `ViolationSubmission`
   * into the backend's `IssueSanctionRequest` payload, posts to
   * `http://localhost:8080/api/module1/issue-sanction`, and translates the response back into
   * `ViolationResult`.
   */
  submitViolation(s: ViolationSubmission): Observable<ViolationResult> {
    const payload = {
      driver: {
        id: s.jmbg,
        firstName: s.ime,
        lastName: s.prezime,
        dateOfBirth: null,
        license: s.hasLicence
          ? {
              licenseNumber: s.licNum ?? '',
              issuedAt: s.licIssued ?? null,
              type: s.licType === 'PROBNA' ? 'PROBATIONARY' : 'PERMANENT',
              categories: s.cats ?? [],
            }
          : null,
      },
      vehicle: { licensePlate: s.plate, category: s.vehicleType },
      bacAtStop: numericOrNull(parseSerbianDecimal(s.bac)),
      causedAccident: s.causedAccident,
      violations: explicitViolations(s),
    };

    return this.http
      .post<BackendIssueSanctionResponse>('http://localhost:8080/api/module1/issue-sanction', payload)
      .pipe(map(resp => toViolationResult(resp, s)));
  }
}

/* ============================================================
   Adapters
   ============================================================ */

function toDriverRecord(resp: BackendIssueSanctionResponse): DriverRecord | null {
  const d = resp.driver;
  if (!d?.license) {
    // Driver exists but has no licence record → not a "roadside hit"
    // for the brza-provera screen. Treat as not found, the form is the
    // proper place to log a NO_LICENSE violation.
    return null;
  }
  return {
    ime:     d.firstName,
    prezime: d.lastName,
    jmbg:    d.id,
    rodj:    d.dateOfBirth ?? '',
    dozvola: {
      broj:        d.license.licenseNumber,
      tip:         d.license.type === 'PROBATIONARY' ? 'PROBNA' : 'STALNA',
      izdato:      d.license.issuedAt ?? '',
      vazi:        validUntil(d.license.issuedAt),
      kategorije:  d.license.categories,
    },
    aktivniPoeni: resp.activePoints?.activePoints ?? 0,
  };
}

function toViolationResult(
  resp: BackendIssueSanctionResponse,
  source: ViolationSubmission,
): ViolationResult {
  // Each Sanction the backend produced is mapped 1:1 to the frontend's
  // Violation shape; the row labels come from the lawArticle string,
  // which the table renders verbatim.
  const sanctions = resp.sanctions.map(s => ({
    code:    `S-${s.lawArticle.replace(/\s+/g, '_').substring(0, 24)}`,
    opis:    deriveOpis(s),
    clan:    s.lawArticle,
    poeni:   s.finalPoints,
    kaznaRsd: [s.minFine, s.maxFine] as [number, number],
    zatvorDana: s.prisonDaysMax > 0 ? [s.prisonDaysMin, s.prisonDaysMax] as [number, number] : undefined,
    zabranaMeseci: s.drivingBanDays > 0
      ? [Math.round(s.drivingBanDays / 30), Math.round(s.drivingBanDays / 30)] as [number, number]
      : undefined,
  }));

  const cap = (source.hasLicence && source.licType === 'PROBNA') ? 9 : 18;
  const newPoints = resp.summary?.totalPoints ?? 0;
  const totalPoints = resp.activePoints?.activePoints ?? newPoints;
  const priorPoints = Math.max(0, totalPoints - newPoints);

  // Was the driver ALREADY above the revocation threshold before this
  // submission? If yes, the LicenseRevocation row is pre-existing — this
  // submission did NOT trigger a new revocation, it just added more points
  // to an already-revoked driver. Using point counts instead of revokedAt
  // dates avoids the "revoked earlier today" edge case.
  const previouslyRevoked = !!resp.revocation && priorPoints >= cap;

  const licType: LicenceType | undefined = source.licType;

  // Use the backend's Layer 5 SanctionSummary directly — it has the
  // sticaj-prekršaja cap already applied. Falling back to bare sums only
  // when the summary is missing (single-sanction case).
  const totals = {
    fine:   [resp.summary?.totalFineMin ?? 0,    resp.summary?.totalFineMax ?? 0] as Range,
    points: resp.summary?.totalPoints ?? newPoints,
    prison: [resp.summary?.prisonMinDays ?? 0,   resp.summary?.prisonMaxDays ?? 0] as Range,
    ban:    [resp.summary?.drivingBanMinDays ?? 0, resp.summary?.drivingBanMaxDays ?? 0] as Range,
    work:   [0, 0] as Range,
  };

  return {
    driver: {
      ime: resp.driver.firstName,
      prezime: resp.driver.lastName,
      jmbg: resp.driver.id,
      licNum: resp.driver.license?.licenseNumber,
      licType,
      plate: source.plate,
    },
    priorPoints,
    newPoints,
    totalPoints,
    revoked: !!resp.revocation,
    previouslyRevoked,
    cap,
    sanctions,
    totals,
  };
}

/* ----- Small mapping helpers ----- */

type BackendViolationInput = {
  type: string;
  location?: string;
  speedOverLimitKmH?: number;
};

function explicitViolations(s: ViolationSubmission): BackendViolationInput[] {
  const list: BackendViolationInput[] = [];

  // NO_LICENSE: surfaced when the toggle is OFF
  if (!s.hasLicence) list.push({ type: 'NO_LICENSE' });

  // Officer-selected violations (codes already match backend ViolationType)
  for (const code of s.violations) list.push({ type: code });

  // SPEEDING: officer selected a tier from SPEEDING_OPTIONS dropdown.
  // The chosen code maps to a (location, speedKmH) pair that matches exactly
  // one row of speeding_by_location.csv on the backend, so the corresponding
  // template-generated rule fires.
  if (s.speeding) {
    const opt = SPEEDING_OPTIONS.find(o => o.code === s.speeding);
    if (opt) {
      list.push({
        type: 'SPEEDING',
        location: opt.location,
        speedOverLimitKmH: opt.speedKmH,
      });
    }
  }

  return list;
}

function numericOrNull(n: number): number | null {
  return Number.isFinite(n) ? n : null;
}

/**
 * Serbian B-category licences run 10 years from the issue date. The
 * backend does not track an expiry column, so we synthesise it here.
 */
function validUntil(issuedAt: string | null | undefined): string {
  if (!issuedAt) return '';
  const d = new Date(issuedAt);
  if (Number.isNaN(d.getTime())) return '';
  d.setFullYear(d.getFullYear() + 10);
  return d.toISOString().substring(0, 10);
}

/** Strip the "[L1] foo bar" prefix from the first explanation line. */
function deriveOpis(s: BackendSanction): string {
  const first = s.explanation?.[0] ?? '';
  return first
    .replace(/^\[[^\]]+\]\s*/, '')
    .replace(/\s*->\s*[\d.,–\-kRSD\s].*/, '')
    .trim()
    || s.lawArticle;
}
