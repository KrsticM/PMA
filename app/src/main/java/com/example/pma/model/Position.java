package com.example.pma.model;

import com.google.gson.annotations.SerializedName;

public class Position {
    @SerializedName("x")
    private Double x;

    @SerializedName("y")
    private Double y;

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }
}
