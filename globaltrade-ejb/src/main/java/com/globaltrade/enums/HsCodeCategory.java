package com.globaltrade.enums;

public enum HsCodeCategory {
    MEDICAL_INSTRUMENTS("9018.90", "Medical & Surgical Diagnostic Instruments", 5.0),
    POWER_TRANSFORMERS("8504.40", "Electrical Power Transformers & Converters", 7.5),
    PORTABLE_COMPUTERS("8471.30", "Portable Laptops & Data Processors", 0.0),
    PHARMACEUTICALS("3004.90", "Pharmaceutical Medicaments & Vaccines", 0.0),
    MOTOR_VEHICLES("8703.23", "Motor Vehicles & Fleet Transports", 15.0);

    private final String code;
    private final String description;
    private final double dutyRate;

    HsCodeCategory(String code, String description, double dutyRate) {
        this.code = code;
        this.description = description;
        this.dutyRate = dutyRate;
    }

    public String getCode() { return code; }
    public String getDescription() { return description; }
    public double getDutyRate() { return dutyRate; }
}
