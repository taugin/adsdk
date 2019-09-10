package com.simple.mpsdk;

/**
 * Created by Administrator on 2018/6/28.
 */

public class RewardItem {
    private String amount;
    private String type;

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
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
        return "RewardItem{" +
                "amount=" + amount +
                ", type='" + type + '\'' +
                '}';
    }
}
