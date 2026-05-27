package com.ftn.sbnz.model.sanctions;

public enum Location {

    /** Inside a populated area (limit usually 50 km/h). */
    URBAN,

    /** Open road outside populated areas (limit usually 80 km/h). */
    OPEN_ROAD,

    /** School zone (limit usually 30 km/h, also treated as aggravating context). */
    SCHOOL_ZONE,

    /** Used when the violation is location-independent (alcohol, no license, fleeing, parking, etc.). */
    ANY
}
