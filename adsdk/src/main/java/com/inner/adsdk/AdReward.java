package com.inner.adsdk;

/**
 * Created by Administrator on 2018/6/28.
 */

public class AdReward {
    private int amount;
    private String type;

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "AdReward{" +
                "amount=" + amount +
                ", type='" + type + '\'' +
                '}';
    }
}
