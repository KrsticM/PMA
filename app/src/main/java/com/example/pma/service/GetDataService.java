package com.example.pma.service;

import com.example.pma.model.Route;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface GetDataService {

    @GET("/route")
    Call<List<Route>> getAllRoutes();
}
