package com.cadify.cadifyWAS.model.entity.cart;

public enum Discount {

    NO_DISCOUNT(0, 10, 0.0),
    DISCOUNT_5(11, 50, 0.05),
    DISCOUNT_10(51, 100, 0.10),
    DISCOUNT_15(101, 200, 0.15),
    DISCOUNT_20(201, Integer.MAX_VALUE, 0.20);


    private final int minAmount;
    private final int maxAmount;
    private final double discountRate;

    Discount(int minAmount, int maxAmount, double discountRate) {
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.discountRate = discountRate;
    }

    public static double getDiscountRate(int amount) {
        for (Discount discount : values()) {
            if (amount >= discount.minAmount && amount <= discount.maxAmount) {
                return discount.discountRate;
            }
        }
        return 0.0;
    }
}
