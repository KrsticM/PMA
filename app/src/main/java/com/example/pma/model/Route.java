package com.example.pma.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Route {

    @SerializedName("id")
    private Integer id;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("city")
    private String city;

    @SerializedName("busStops")
    private List<BusStop> busStops = new ArrayList<>();

    public Route() {

    }

    public Route(Integer id, String name, String description, String city) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.city = city;

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public  void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public List<BusStop> getBusStops() {
        return busStops;
    }

    public void setBusStops(List<BusStop> busStops) {
        this.busStops = busStops;
    }

    @Override
    public String toString() {
        return name + " " + description;
    }
}
