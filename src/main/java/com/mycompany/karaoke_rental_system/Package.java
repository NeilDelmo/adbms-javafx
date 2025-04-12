
package com.mycompany.karaoke_rental_system;

public class Package {
    private int packageId;
    private String name;
    private String description;
    private double bundlePrice;

    // Constructor
    public Package(int packageId, String name, String description, double bundlePrice) {
        this.packageId = packageId;
        this.name = name;
        this.description = description;
        this.bundlePrice = bundlePrice;
    }

    // Getters and Setters
    public int getPackageId() {
        return packageId;
    }

    public void setPackageId(int packageId) {
        this.packageId = packageId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getBundlePrice() {
        return bundlePrice;
    }

    public void setBundlePrice(double bundlePrice) {
        this.bundlePrice = bundlePrice;
    }
    
}