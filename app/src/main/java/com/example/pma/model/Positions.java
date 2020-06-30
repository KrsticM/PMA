package com.example.pma.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Positions {

    @SerializedName("positions")
    private List<Position> positions;

    public List<Position> getPositions() {
        return positions;
    }

    public void setPositions(List<Position> positions) {
        this.positions = positions;
    }
}
