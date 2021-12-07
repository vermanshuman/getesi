package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;

import java.util.Arrays;

public enum WeatherCodes {
    Tornado(0),
    TropicalStorm(1),
    Hurricane(2),
    SevereThunderstorms(3),
    Thunderstorms(4),
    MixedRainSnow(5),
    MixedRainSleet(6),
    MixedSnowSleet(7),
    FreezingDrizzle(8),
    Drizzle(9),
    FreezingRain(10),
    ShowersNight(11),
    ShowersDay(12),
    SnowFlurries(13),
    LightSnowShowers(14),
    BlowingSnow(15),
    Snow(16),
    Hail(17),
    Sleet(18),
    Dust(19),
    Foggy(20),
    Haze(21),
    Smoky(22),
    Blustery(23),
    Windy(24),
    Cold(25),
    Cloudy(26),
    MostlyCloudyNight(27),
    MostlyCloudyDay(28),
    PartlyCloudyNight(29),
    PartlyCloudyDay(30),
    ClearNight(31),
    Sunny(32),
    FairNight(33),
    FairDay(34),
    MixedRainAndHail(35),
    Hot(36),
    IsolatedThunderstorms(37),
    ScatteredThunderstormsNight(38),
    ScatteredThunderstormsDay(39),
    ScatteredShowers(40),
    HeavySnowNight(41),
    ScatteredSnowShowers(42),
    HeavySnowDay(43),
    PartlyCloudy(44),
    Thundershowers(45),
    SnowShowers(46),
    IsolatedThundershowers(47),
    NotAvailable(3200);

    Integer code;

    static final String url = "http://tripad.ru/wikon/%s@2x.png";

    WeatherCodes(Integer code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }

    public String getImg() {
        return String.format(url, code);
    }

    public static WeatherCodes getByCode(Integer code) {
        return Arrays.stream(WeatherCodes.values())
                .filter(weather -> weather.getCode().equals(code))
                .findFirst().orElse(null);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
