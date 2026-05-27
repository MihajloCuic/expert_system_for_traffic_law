package com.ftn.sbnz.model.sanctions;

/**
 * Violation severity, mapped to ZOBS articles (Glava XVIII - Kaznene odredbe).
 * Lower ordinal = more severe.
 */
public enum Severity {

    VIOLENT, //329
    SEVERE, //330
    MAJOR, //331
    MODERATE, //332
    PETTY, //332a
    MINOR, //333
    MINIMAL //334
}
