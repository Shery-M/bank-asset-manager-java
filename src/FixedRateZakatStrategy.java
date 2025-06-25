package com.shrouk;

public class FixedRateZakatStrategy implements IZakatStrategy {
    private static final double NISAB = 595 * 3.75;
    private static final double ZAKAT_RATE = 0.025;

    @Override
    public double calculateZakat(double totalAssets) {
        return totalAssets >= NISAB ? totalAssets * ZAKAT_RATE : 0;
    }

    @Override
    public double getNisab() {
        return NISAB;
    }
}