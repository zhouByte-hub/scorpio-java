package com.zhoubyte.sse.dto;

public class WeatherResult {

    private String city;
    private String weather;
    private double temperature;
    private String unit;

    public WeatherResult(String city, String weather, double temperature, String unit) {
        this.city = city;
        this.weather = weather;
        this.temperature = temperature;
        this.unit = unit;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
