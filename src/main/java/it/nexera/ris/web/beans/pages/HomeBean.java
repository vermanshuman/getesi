package it.nexera.ris.web.beans.pages;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.nexera.ris.common.enums.WeatherCodes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.common.utils.ForecastUtil;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Event;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.DayPhrase;
import it.nexera.ris.web.beans.BaseValidationPageBean;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

@ManagedBean(name = "homeBean")
@ViewScoped
public class HomeBean extends BaseValidationPageBean implements Serializable {

    private static final long serialVersionUID = 4618457741093417637L;

    private static final long SHOW_NEXT_WEATHER_DAYS = 3L;

    private Long numberTotalRequests;

    private Long numberDBRecords;

    private ScheduleModel eventModel;

    private String phrase;

    private ForecastUtil forecastToday;

    private List<ForecastUtil> futureForecast;

    private ScheduleEvent eventSelect;

    @Override
    protected void onConstruct() {
        try {
//            (actually we may set this value as 0)
            setNumberTotalRequests(0L);
            setNumberDBRecords(0L);
            setEventModel(new DefaultScheduleModel());
            List<Event> eventList = DaoManager.load(Event.class, new Criterion[]{
                    Restrictions.eq("createUserId", getCurrentUser().getId())});
            for (Event ev : eventList) {
                getEventModel().addEvent(new DefaultScheduleEvent(ev.getText(), ev.getStartDate(), ev.getEndDate()));
            }
            generatePhrase();
            generateForecast();
        } catch (InstantiationException | IllegalAccessException | PersistenceBeanException e) {
            LogHelper.log(log, e);
        }
    }

    public void generateForecast() {
        try {
            JsonObject channel = getForecastJson().get("query").getAsJsonObject()
                    .get("results").getAsJsonObject()
                    .get("channel").getAsJsonObject();
            setFutureForecast(fillForecastWeek(channel));
            setForecastToday(fillForecastToday(channel));
        } catch (Exception e) {
            setForecastToday(null);
        }
    }

    private List<ForecastUtil> fillForecastWeek(JsonObject channel) {
        List<ForecastUtil> list = new LinkedList<>();
        JsonArray forecast = channel.get("item").getAsJsonObject().get("forecast").getAsJsonArray();
        for (int i = 0; i < forecast.size() && i < SHOW_NEXT_WEATHER_DAYS; i++) {
            ForecastUtil util = new ForecastUtil();
            JsonObject itemObj = forecast.get(i).getAsJsonObject();
            util.setCode(WeatherCodes.getByCode(itemObj.get("code").getAsInt()));
            String dateStr = DateTimeHelper.fromStringFormater(itemObj.get("date").getAsString(), "dd MMM yyyy", Locale.ITALY);
            String dayStr = DateTimeHelper.fromStringFormater(itemObj.get("date").getAsString(), "EEE", Locale.ITALY);
            util.setDate(dateStr.toUpperCase());
            util.setDay(dayStr.substring(0, 1).toUpperCase() + dayStr.substring(1));
            util.setMaxTemp(itemObj.get("high").getAsInt());
            util.setMinTemp(itemObj.get("low").getAsInt());
            list.add(util);
        }
        return list;
    }

    private ForecastUtil fillForecastToday(JsonObject channel) {
        JsonObject condition = channel.get("item").getAsJsonObject().get("condition").getAsJsonObject();
        String speedUnit = channel.get("units").getAsJsonObject().get("speed").getAsString();
        ForecastUtil util = new ForecastUtil();
        util.setCode(WeatherCodes.getByCode(condition.get("code").getAsInt()));
        util.setDate(condition.get("date").getAsString());
        util.setTemp(Integer.parseInt(condition.get("temp").getAsString()));
        util.setHumidity(channel.get("atmosphere").getAsJsonObject().get("humidity").getAsInt());
        util.setWindSpeed(channel.get("wind").getAsJsonObject().get("speed").getAsString() + " " + speedUnit);
        return util;
    }

    private JsonObject getForecastJson() throws IOException {
        URL feedSource = new URL("https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%3D719258%20and%20u%20%3D%20%27c%27&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys");
        HttpURLConnection connection = (HttpURLConnection) feedSource.openConnection();
        connection.connect();
        return new JsonParser().parse(new InputStreamReader((InputStream) connection.getContent())).getAsJsonObject();
    }

    private void generatePhrase() throws InstantiationException, PersistenceBeanException, IllegalAccessException {
        List<Long> phraseIds = DaoManager.loadIds(DayPhrase.class, new Criterion[]{});
        if (ValidationHelper.isNullOrEmpty(phraseIds)) {
            setPhrase("");
            return;
        }
        Long max = phraseIds.stream().max(Comparator.naturalOrder()).get();
        Long min = phraseIds.stream().min(Comparator.naturalOrder()).get();
        while (true) {
            Long random = min + (long) (Math.random() * (max - min));
            if (phraseIds.contains(random)) {
                setPhrase(DaoManager.get(DayPhrase.class, random).getPhrase());
                break;
            }
        }
    }

    public void onEventSelect(SelectEvent selectEvent) {
        eventSelect = (ScheduleEvent) selectEvent.getObject();
    }

    public Long getNumberTotalRequests() {
        return numberTotalRequests;
    }

    public void setNumberTotalRequests(Long numberTotalRequests) {
        this.numberTotalRequests = numberTotalRequests;
    }

    public Long getNumberDBRecords() {
        return numberDBRecords;
    }

    public void setNumberDBRecords(Long numberDBRecords) {
        this.numberDBRecords = numberDBRecords;
    }

    public ScheduleModel getEventModel() {
        return eventModel;
    }

    public void setEventModel(ScheduleModel eventModel) {
        this.eventModel = eventModel;
    }

    public String getPhrase() {
        return phrase;
    }

    public void setPhrase(String phrase) {
        this.phrase = phrase;
    }

    public ForecastUtil getForecastToday() {
        return forecastToday;
    }

    public void setForecastToday(ForecastUtil forecastToday) {
        this.forecastToday = forecastToday;
    }

    public List<ForecastUtil> getFutureForecast() {
        return futureForecast;
    }

    public void setFutureForecast(List<ForecastUtil> futureForecast) {
        this.futureForecast = futureForecast;
    }

    public ScheduleEvent getEventSelect() {
        return eventSelect;
    }

    public void setEventSelect(ScheduleEvent eventSelect) {
        this.eventSelect = eventSelect;
    }
}
