package com.ftn.sbnz.model;

import java.io.Serializable;

public class AccidentScene implements Serializable {

    private static final long serialVersionUID = 1L;

    private SignalizationType signalization;
    private Visibility visibility;
    private WeatherCondition weather;
    private RoadCondition roadCondition;
    private int speedLimitKmH;
    private boolean intersection;

    public AccidentScene() {}

    public SignalizationType getSignalization() { return signalization; }
    public void setSignalization(SignalizationType signalization) { this.signalization = signalization; }

    public Visibility getVisibility() { return visibility; }
    public void setVisibility(Visibility visibility) { this.visibility = visibility; }

    public WeatherCondition getWeather() { return weather; }
    public void setWeather(WeatherCondition weather) { this.weather = weather; }

    public RoadCondition getRoadCondition() { return roadCondition; }
    public void setRoadCondition(RoadCondition roadCondition) { this.roadCondition = roadCondition; }

    public int getSpeedLimitKmH() { return speedLimitKmH; }
    public void setSpeedLimitKmH(int speedLimitKmH) { this.speedLimitKmH = speedLimitKmH; }

    public boolean isIntersection() { return intersection; }
    public void setIntersection(boolean intersection) { this.intersection = intersection; }
}
