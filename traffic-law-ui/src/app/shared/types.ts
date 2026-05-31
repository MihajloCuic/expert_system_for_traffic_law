/**
 * Canonical TypeScript contracts from CLAUDE.md §3.
 * All sanction values are RANGES; render with format helpers.
 */

/** Inclusive `[min, max]`. When `min === max` render as a single value. */
export type Range = [number, number];

export type LicenceType = 'STALNA' | 'PROBNA';

export type Violation = {
  code: string;
  opis: string;
  /** Article reference, e.g. "332/5". */
  clan: string;
  /** Kazneni poeni — single integer, do NOT range. */
  poeni: number;
  /** Novčana kazna (RSD). */
  kaznaRsd: Range;
  /** Kazna zatvora (days). */
  zatvorDana?: Range;
  /** Zabrana upravljanja motornim vozilom (months). */
  zabranaMeseci?: Range;
  /** Društveno koristan rad (hours). */
  koristanRadSati?: Range;
};

export type ViolationSubmission = {
  ime: string;
  prezime: string;
  jmbg: string;                          // 13 digits
  hasLicence: boolean;
  licNum?: string;                       // 9 digits — only when hasLicence
  licType?: LicenceType;
  cats?: string[];                       // subset of category codes
  licIssued?: string;                    // ISO YYYY-MM-DD
  vehicleType: string;                   // see VEHICLE_TYPES
  plate: string;
  bac: string;                           // Serbian decimal "0,42"
  /** When true, every sanction the backend produces is escalated by the
   *  Layer 4 (accident_escalations) rules. */
  causedAccident: boolean;
  violations: string[];                  // array of violation codes
  /** Optional SPEEDING tier — single code from SPEEDING_OPTIONS (catalogues.ts).
   *  Empty / undefined means "no speeding violation in this submission". */
  speeding?: string;
};

export type ViolationResult = {
  driver: {
    ime: string;
    prezime: string;
    jmbg: string;
    licNum?: string;
    licType?: LicenceType;
    plate: string;
  };
  priorPoints: number;
  newPoints: number;
  totalPoints: number;
  revoked: boolean;
  /** True when the LicenseRevocation row existed BEFORE this submission
   *  (i.e. the driver was already under a ban). Used to switch the banner
   *  from "newly revoked" to "still revoked from earlier" wording. */
  previouslyRevoked: boolean;
  cap: number;                           // 18 (STALNA) or 9 (PROBNA)
  sanctions: Violation[];
  /** Authoritative totals from the backend's Layer 5 SanctionSummary —
   *  these already have the sticaj-prekršaja cap applied. Frontend MUST
   *  use these rather than re-summing kaznaRsd across rows, which would
   *  ignore the cap. */
  totals: {
    fine: Range;
    points: number;
    prison: Range;
    ban: Range;
    work: Range;
  };
};

/** Returned by the roadside lookup. */
export type DriverRecord = {
  ime: string;
  prezime: string;
  jmbg: string;
  rodj: string;                          // ISO YYYY-MM-DD
  dozvola: {
    broj: string;
    tip: LicenceType;
    izdato: string;
    vazi: string;
    kategorije: string[];
  };
  aktivniPoeni: number;
};
