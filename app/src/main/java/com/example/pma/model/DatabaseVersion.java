package com.example.pma.model;

import com.google.gson.annotations.SerializedName;

public class DatabaseVersion {
    @SerializedName("id")
    private Integer id;

    @SerializedName("version")
    private Integer version;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
