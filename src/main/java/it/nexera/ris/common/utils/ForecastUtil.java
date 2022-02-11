package it.nexera.ris.common.utils;

import it.nexera.ris.common.enums.WeatherCodes;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForecastUtil {

    private String date;

    private String day;

    private WeatherCodes code;

    private Integer temp;

    private Integer maxTemp;

    private Integer minTemp;

    private String humidity;

    private String windSpeed;
}
