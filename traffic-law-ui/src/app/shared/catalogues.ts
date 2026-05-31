/** Driving licence categories per CLAUDE.md §3.3. */
export const CATEGORIES = [
  'AM', 'A1', 'A2', 'A', 'B1', 'B', 'BE', 'C1', 'C1E', 'C', 'CE', 'D1', 'D1E', 'D', 'DE', 'F', 'M',
] as const;
export type LicenceCategory = (typeof CATEGORIES)[number];

/**
 * Vehicle categories aligned with the backend `VehicleCategory` enum.
 * The backend has 4 broad buckets; the frontend renders Serbian labels.
 */
export const VEHICLE_TYPES: Array<{ code: string; label: string }> = [
  { code: 'CAR',        label: 'Putnički automobil' },
  { code: 'TRUCK',      label: 'Teretno vozilo' },
  { code: 'BUS',        label: 'Autobus' },
  { code: 'MOTORCYCLE', label: 'Motocikl' },
];

/**
 * Violations the officer can pick from the multiselect. Codes mirror the
 * backend `ViolationType` enum 1:1, so submission needs no further mapping.
 *
 * SPEEDING and ALCOHOL are intentionally NOT in this list:
 *   - ALCOHOL is auto-added by the backend when `bacAtStop > 0.20`.
 *   - SPEEDING requires location + speed-over-limit parameters that
 *     CLAUDE.md §2.3 multi-select cannot capture. Add as a dedicated
 *     section in a future iteration.
 */
export const VIOLATIONS: Array<{ code: string; label: string; clan?: string }> = [
  // Signalisation & right of way
  { code: 'RED_LIGHT',                   label: 'Prolazak kroz crveno svetlo',                clan: '312/1' },
  { code: 'STOP_SIGN_IGNORED',           label: 'Nepostupanje po znaku STOP' },
  { code: 'YIELD_SIGN_IGNORED',          label: 'Nepostupanje po znaku ukrštanja sa putem' },
  { code: 'RIGHT_OF_WAY_AT_INTERSECTION',label: 'Neprvenstvo prolaza na raskrsnici' },
  { code: 'RIGHT_OF_WAY_FOR_PEDESTRIAN', label: 'Nedavanje prvenstva pešaku' },

  // Overtaking & lane control
  { code: 'OVERTAKING_SOLID_LINE',       label: 'Pretpcanje preko pune linije' },
  { code: 'CROSSING_SOLID_LINE',         label: 'Prelazenje preko pune linije' },
  { code: 'WRONG_WAY_ONE_WAY',           label: 'Vožnja u suprotnom smeru jednosmerne ulice' },
  { code: 'FORBIDDEN_U_TURN',            label: 'Polukružno okretanje na zabranjenom mestu' },
  { code: 'AGGRESSIVE_LANE_CHANGE',      label: 'Naglo i opasno menjanje saobraćajne trake' },
  { code: 'IMPROPER_DISTANCE',           label: 'Nepropisno odstojanje od vozila ispred' },

  // Driver & passenger safety
  { code: 'PHONE_USAGE',                 label: 'Upotreba mobilnog telefona u vožnji' },
  { code: 'NO_SEATBELT',                 label: 'Nekorišćenje sigurnosnog pojasa' },
  { code: 'CHILD_IN_FRONT_SEAT',         label: 'Dete bez sedišta na prednjem sedištu' },

  // Lights & visibility
  { code: 'NO_DAY_LIGHTS',               label: 'Vožnja bez dnevnih svetala' },
  { code: 'NO_LIGHTS_IN_FOG',            label: 'Vožnja bez svetala u magli' },

  // Licence-related (NO_LICENSE handled by the form toggle; DRIVING_DURING_BAN auto-derived by rules)
  { code: 'WRONG_LICENSE_CATEGORY',      label: 'Vožnja vozila pogrešne kategorije',          clan: '178/1' },

  // Accident-related
  { code: 'FLEEING_SCENE',               label: 'Napuštanje mesta saobraćajne nezgode',       clan: '297/3' },
  { code: 'FAILURE_TO_REPORT_ACCIDENT',  label: 'Nepružanje pomoći / prijavljivanja nezgode' },

  // Alcohol-test refusal (different from ALCOHOL — captured separately)
  { code: 'REFUSING_ALCO_TEST',          label: 'Odbijanje alkotesta' },

  // Parking
  { code: 'PARKING_ON_PEDESTRIAN_CROSSING', label: 'Parkiranje na pešačkom prelazu' },
  { code: 'PARKING_ON_DISABLED_SPOT',    label: 'Parkiranje na mestu za invalide' },
  { code: 'PARKING_BLOCKING_TRAFFIC',    label: 'Parkiranje koje ometa saobraćaj' },
  { code: 'IMPROPER_PARKING',            label: 'Nepropisno parkiranje' },

  // Misc
  { code: 'NO_HIGH_VISIBILITY_VEST',     label: 'Nepostavljanje sigurnosnog prsluka' },
  { code: 'NOT_REMOVING_ROAD_HAZARD',    label: 'Neuklanjanje opasnosti sa puta' },
  { code: 'IMPROPER_HORN_USE',           label: 'Nepropisna upotreba sirene' },
];

/**
 * Speeding sub-types. Officer ne unosi sirovu brzinu — bira jedan od unapred
 * definisanih opsega koji 1:1 odgovaraju redovima u backend CSV-u
 * `speeding_by_location.csv` (tarifna tabela ZOBS čl. 43, 44, 45 + članovi
 * 329-333). `speedKmH` je reprezentativna vrednost iz opsega koja se šalje
 * backendu kao `speedOverLimitKmH` — bilo koja vrednost iz opsega bi
 * aktivirala isto template-generisano pravilo.
 *
 * BUS-specifične tarife (speeding_by_vehicle.csv) se NE biraju iz dropdown-a;
 * one se automatski aktiviraju kad je `Vehicle.category == BUS` jer template
 * `speeding_by_vehicle.drt` ne uslovljava `location`.
 */
export type SpeedingLocation = 'URBAN' | 'OPEN_ROAD' | 'SCHOOL_ZONE';
export type SpeedingOption = {
  code: string;
  label: string;
  location: SpeedingLocation;
  /** Reprezentativna brzina iz opsega — šalje se backendu. */
  speedKmH: number;
};

export const SPEEDING_OPTIONS: SpeedingOption[] = [
  // U naselju (limit 50 km/h)
  { code: 'URBAN_10_20',    label: 'U naselju — 10–20 km/h preko ograničenja',          location: 'URBAN',       speedKmH: 15 },
  { code: 'URBAN_21_30',    label: 'U naselju — 21–30 km/h preko ograničenja',          location: 'URBAN',       speedKmH: 25 },
  { code: 'URBAN_31_50',    label: 'U naselju — 31–50 km/h preko ograničenja',          location: 'URBAN',       speedKmH: 40 },
  { code: 'URBAN_51_70',    label: 'U naselju — 51–70 km/h preko ograničenja',          location: 'URBAN',       speedKmH: 60 },
  { code: 'URBAN_71_90',    label: 'U naselju — 71–90 km/h preko ograničenja',          location: 'URBAN',       speedKmH: 80 },
  { code: 'URBAN_OVER_90',  label: 'U naselju — preko 90 km/h (bezobzirna vožnja)',     location: 'URBAN',       speedKmH: 100 },
  // Van naselja (limit 80 km/h)
  { code: 'OPEN_10_20',     label: 'Van naselja — 10–20 km/h preko ograničenja',        location: 'OPEN_ROAD',   speedKmH: 15 },
  { code: 'OPEN_21_40',     label: 'Van naselja — 21–40 km/h preko ograničenja',        location: 'OPEN_ROAD',   speedKmH: 30 },
  { code: 'OPEN_41_60',     label: 'Van naselja — 41–60 km/h preko ograničenja',        location: 'OPEN_ROAD',   speedKmH: 50 },
  { code: 'OPEN_61_80',     label: 'Van naselja — 61–80 km/h preko ograničenja',        location: 'OPEN_ROAD',   speedKmH: 70 },
  { code: 'OPEN_81_100',    label: 'Van naselja — 81–100 km/h preko ograničenja',       location: 'OPEN_ROAD',   speedKmH: 90 },
  { code: 'OPEN_OVER_100',  label: 'Van naselja — preko 100 km/h (bezobzirna vožnja)',  location: 'OPEN_ROAD',   speedKmH: 110 },
  // Školska zona (limit 30 km/h)
  { code: 'SCHOOL_OVER_60', label: 'Školska zona — preko 60 km/h (otežavajuće)',        location: 'SCHOOL_ZONE', speedKmH: 65 },
];

/** BAC tone bands per CLAUDE.md §2.3 Section 4. */
export type BacTone = 'green' | 'amber' | 'red';
export type BacBand = { max: number; tone: BacTone; label: string };

export const BAC_BANDS: BacBand[] = [
  { max: 0.20, tone: 'green', label: 'Trezan' },
  { max: 0.50, tone: 'amber', label: 'Lakši stepen' },
  { max: 0.80, tone: 'amber', label: 'Srednji stepen' },
  { max: 1.20, tone: 'red',   label: 'Teški stepen' },
  { max: 1.60, tone: 'red',   label: 'Veoma teški stepen' },
  { max: Infinity, tone: 'red', label: 'Potpuno nesposoban' },
];

export function classifyBac(value: number): BacBand {
  for (const band of BAC_BANDS) if (value <= band.max) return band;
  return BAC_BANDS[BAC_BANDS.length - 1];
}

/** Parse a Serbian-decimal string ("0,42") into a number. Returns NaN if blank. */
export function parseSerbianDecimal(raw: string): number {
  const cleaned = raw.replace(/\s/g, '').replace(',', '.');
  if (cleaned === '') return NaN;
  return Number(cleaned);
}
