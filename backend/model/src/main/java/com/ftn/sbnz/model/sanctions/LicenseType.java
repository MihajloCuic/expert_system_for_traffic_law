package com.ftn.sbnz.model.sanctions;

/**
 * Two flavours of Serbian driving licenses recognised by the rule engine.
 *
 *   PERMANENT     - regular license, 18-point revocation threshold
 *   PROBATIONARY  - "probna" license (first 2 years, or until 21 if obtained
 *                   before 19), 9-point revocation threshold per ZOBS
 */
public enum LicenseType {
    PERMANENT,
    PROBATIONARY
}
