package com.ftn.sbnz.model;

/**
 * All distinct violation types currently supported by Module 1.
 * Grouped by category for readability.
 */
public enum ViolationType {

    // === Speeding ===
    /** Speeding (rules differentiate by location and by vehicle category). */
    SPEEDING,

    // === Alcohol & substance ===
    /** Driving under the influence of alcohol (rule differentiates by blood level). */
    ALCOHOL,
    /** Driver refused to take the alcohol test (rule 24). */
    REFUSING_ALCO_TEST,

    // === Signalization & right of way ===
    RED_LIGHT,
    STOP_SIGN_IGNORED,
    YIELD_SIGN_IGNORED,
    RIGHT_OF_WAY_AT_INTERSECTION,
    RIGHT_OF_WAY_FOR_PEDESTRIAN,

    // === Overtaking & lane control ===
    OVERTAKING_SOLID_LINE,
    OVERTAKING_AT_CROSSING,
    OVERTAKING_HILLTOP,
    CROSSING_SOLID_LINE,

    // === Direction of travel ===
    WRONG_WAY_ONE_WAY,
    FORBIDDEN_U_TURN,
    DRIVING_IN_REVERSE_DANGEROUS,
    AGGRESSIVE_LANE_CHANGE,
    IMPROPER_DISTANCE,

    // === Driver / passenger safety ===
    PHONE_USAGE,
    NO_SEATBELT,
    CHILD_IN_FRONT_SEAT,
    PEDESTRIAN_NEGLIGENCE,

    // === Lights & visibility ===
    NO_DAY_LIGHTS,
    IMPROPER_NIGHT_LIGHTS,
    ONLY_POSITION_LIGHTS_AT_NIGHT,
    NO_LIGHTS_IN_FOG,
    NIGHT_LIGHTS_IN_FOG,

    // === License ===
    NO_LICENSE,
    WRONG_LICENSE_CATEGORY,
    /** Driving while the driver's license is currently revoked / suspended (ZOBS art. 330 pt. 8). */
    DRIVING_DURING_BAN,

    // === Accident-related ===
    FLEEING_SCENE,
    FAILURE_TO_REPORT_ACCIDENT,

    // === Parking ===
    PARKING_ON_PEDESTRIAN_CROSSING,
    PARKING_ON_DISABLED_SPOT,
    PARKING_BLOCKING_TRAFFIC,
    IMPROPER_PARKING,

    // === Misc ===
    NO_HIGH_VISIBILITY_VEST,
    NOT_REMOVING_ROAD_HAZARD,
    IMPROPER_HORN_USE
}
