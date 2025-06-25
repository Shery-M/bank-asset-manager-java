package com.shrouk;

public class ZakatCalculator {
    private IZakatStrategy strategy;

    public ZakatCalculator(IZakatStrategy strategy) {
        this.strategy = strategy;
    }

    public double calculateZakat(double totalAssets) {
        return strategy.calculateZakat(totalAssets);
    }

    public double getNisab() {
        return strategy.getNisab();
    }
}