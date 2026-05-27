package com.ftn.sbnz.model.sanctions;

import com.ftn.sbnz.model.shared.VehicleCategory;
/**
 * Serbian driving license categories (per ZOBS art. 178).
 * A single {@link DrivingLicense} can hold multiple categories.
 *
 * Each category maps to the broad {@link VehicleCategory} used inside the
 * rules engine. The mapping lets us answer "does this license cover this
 * vehicle?" without baking Serbian letter names into the Drools rules.
 */
public enum LicenseCategory {

    // Motorcycle family
    AM(VehicleCategory.MOTORCYCLE),  // mopeds (50cc)
    A1(VehicleCategory.MOTORCYCLE),  // light motorcycles
    A2(VehicleCategory.MOTORCYCLE),  // medium motorcycles
    A (VehicleCategory.MOTORCYCLE),  // unrestricted motorcycles

    // Car family
    B1(VehicleCategory.CAR),         // quadricycles
    B (VehicleCategory.CAR),         // passenger car (default for most drivers)
    BE(VehicleCategory.CAR),         // car + heavy trailer

    // Truck family
    C1(VehicleCategory.TRUCK),       // medium trucks
    C (VehicleCategory.TRUCK),       // heavy trucks
    CE(VehicleCategory.TRUCK),       // truck + trailer

    // Bus family
    D1(VehicleCategory.BUS),         // small buses
    D (VehicleCategory.BUS),         // full-size buses
    DE(VehicleCategory.BUS);         // bus + trailer

    private final VehicleCategory vehicleCategory;

    LicenseCategory(VehicleCategory vc) {
        this.vehicleCategory = vc;
    }

    /** Which broad vehicle category this license category authorises the holder to drive. */
    public VehicleCategory getVehicleCategory() {
        return vehicleCategory;
    }
}
