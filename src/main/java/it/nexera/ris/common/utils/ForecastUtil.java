package it.nexera.ris.common.utils;

import it.nexera.ris.common.enums.WeatherCodes;

public class ForecastUtil {

    private String date;

    private String day;

    private WeatherCodes code;

    private Integer temp;

    private Integer maxTemp;

    private Integer minTemp;

    private Integer humidity;

    private String windSpeed;

    public WeatherCodes getCode() {
        return code;
    }

    public void setCode(WeatherCodes code) {
        this.code = code;
    }

    public Integer getTemp() {
        return temp;
    }

    public void setTemp(Integer temp) {
        this.temp = temp;
    }

    public Integer getMaxTemp() {
        return maxTemp;
    }

    public void setMaxTemp(Integer maxTemp) {
        this.maxTemp = maxTemp;
    }

    public Integer getMinTemp() {
        return minTemp;
    }

    public void setMinTemp(Integer minTemp) {
        this.minTemp = minTemp;
    }

    public Integer getHumidity() {
        return humidity;
    }

    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(String windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }
}
