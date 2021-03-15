package com.example.weatherapp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiInterface {
    @GET("weather")
    public Call<WeatherMap> getWeatherMap(@Query("q") String cityName, @Query("appid") String apiKey);
}
