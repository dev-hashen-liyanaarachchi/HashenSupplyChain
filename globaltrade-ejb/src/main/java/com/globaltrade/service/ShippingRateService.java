package com.globaltrade.service;

import jakarta.ejb.Stateless;

@Stateless
public class ShippingRateService {

    public static class ShippingQuote {
        private boolean domestic;
        private String shipmentType;      // "DOMESTIC" or "INTERNATIONAL"
        private String carrierName;       // "DHL Express International" or "Local Express Courier"
        private double totalWeightKg;
        private double shippingFee;
        private String weightTierLabel;
        private String deliveryEstimate;

        public ShippingQuote() {}

        public ShippingQuote(boolean domestic, String shipmentType, String carrierName, double totalWeightKg, double shippingFee, String weightTierLabel, String deliveryEstimate) {
            this.domestic = domestic;
            this.shipmentType = shipmentType;
            this.carrierName = carrierName;
            this.totalWeightKg = totalWeightKg;
            this.shippingFee = shippingFee;
            this.weightTierLabel = weightTierLabel;
            this.deliveryEstimate = deliveryEstimate;
        }

        public boolean isDomestic() { return domestic; }
        public void setDomestic(boolean domestic) { this.domestic = domestic; }

        public String getShipmentType() { return shipmentType; }
        public void setShipmentType(String shipmentType) { this.shipmentType = shipmentType; }

        public String getCarrierName() { return carrierName; }
        public void setCarrierName(String carrierName) { this.carrierName = carrierName; }

        public double getTotalWeightKg() { return totalWeightKg; }
        public void setTotalWeightKg(double totalWeightKg) { this.totalWeightKg = totalWeightKg; }

        public double getShippingFee() { return shippingFee; }
        public void setShippingFee(double shippingFee) { this.shippingFee = shippingFee; }

        public String getWeightTierLabel() { return weightTierLabel; }
        public void setWeightTierLabel(String weightTierLabel) { this.weightTierLabel = weightTierLabel; }

        public String getDeliveryEstimate() { return deliveryEstimate; }
        public void setDeliveryEstimate(String deliveryEstimate) { this.deliveryEstimate = deliveryEstimate; }
    }

    public ShippingQuote calculateShippingQuote(String originCountryCode, String destinationCountryCode, double totalWeightKg) {
        String origin = (originCountryCode != null && !originCountryCode.isBlank()) ? originCountryCode.toUpperCase() : "DE";
        String dest = (destinationCountryCode != null && !destinationCountryCode.isBlank()) ? destinationCountryCode.toUpperCase() : "LK";

        double weight = totalWeightKg <= 0 ? 1.0 : totalWeightKg;
        boolean isDomestic = origin.equalsIgnoreCase(dest);

        String shipmentType;
        String carrierName;
        String deliveryEstimate;
        double fee;
        String weightTierLabel;

        if (isDomestic) {
            shipmentType = "DOMESTIC";
            carrierName = "Local Express Ground Courier";
            deliveryEstimate = "1-2 Business Days";

            if (weight <= 1.0) {
                fee = 5.00;
                weightTierLabel = "0–1 kg Tier ($5.00)";
            } else if (weight <= 3.0) {
                fee = 10.00;
                weightTierLabel = "1–3 kg Tier ($10.00)";
            } else if (weight <= 5.0) {
                fee = 15.00;
                weightTierLabel = "3–5 kg Tier ($15.00)";
            } else {
                double extraKg = Math.ceil(weight - 5.0);
                fee = 15.00 + (extraKg * 3.00);
                weightTierLabel = ">5 kg Tier ($15.00 + $3.00/kg)";
            }
        } else {
            shipmentType = "INTERNATIONAL";
            carrierName = "DHL Express Air & Sea Freight";
            deliveryEstimate = "3-5 Business Days (Includes Customs Inspection & Clearance)";

            if (weight <= 1.0) {
                fee = 15.00;
                weightTierLabel = "0–1 kg Tier ($15.00)";
            } else if (weight <= 3.0) {
                fee = 25.00;
                weightTierLabel = "1–3 kg Tier ($25.00)";
            } else if (weight <= 5.0) {
                fee = 40.00;
                weightTierLabel = "3–5 kg Tier ($40.00)";
            } else {
                double extraKg = Math.ceil(weight - 5.0);
                fee = 40.00 + (extraKg * 8.00);
                weightTierLabel = ">5 kg Tier ($40.00 + $8.00/kg)";
            }
        }

        fee = Math.round(fee * 100.0) / 100.0;
        return new ShippingQuote(isDomestic, shipmentType, carrierName, Math.round(weight * 100.0) / 100.0, fee, weightTierLabel, deliveryEstimate);
    }
}
