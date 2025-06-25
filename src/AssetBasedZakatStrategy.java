package com.shrouk;


public class AssetBasedZakatStrategy implements IZakatStrategy {
    private static final double NISAB = 595 * 3.75;

    @Override
    public double calculateZakat(double totalAssets) {
        if (totalAssets < NISAB) return 0;
        return totalAssets * 0.025;
    }

    @Override
    public double getNisab() {
        return NISAB;
    }
}

