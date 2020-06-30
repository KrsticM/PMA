package com.example.pma.service;

import com.example.pma.model.DatabaseVersion;
import com.example.pma.model.News;
import com.example.pma.model.Position;
import com.example.pma.model.Positions;
import com.example.pma.model.Route;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface GetDataService {

    @GET("/route")
    Call<List<Route>> getAllRoutes();

    @GET("/news")
    Call<List<News>> getAllNews();

    @GET("/bus/position/4")
    Call<Positions> getPosition4();

    @GET("/bus/position/7")
    Call<Positions> getPosition7();

    @GET("/version")
    Call<DatabaseVersion> getVersion();
}
