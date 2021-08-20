package it.nexera.ris.web.beans.pages;

import com.google.gson.Gson;
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
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.DayPhrase;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RequestType;
import it.nexera.ris.web.beans.BaseValidationPageBean;
import it.nexera.ris.web.beans.wrappers.ChartDataWrapper;
import it.nexera.ris.web.beans.wrappers.ChartWrapper;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
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
import java.time.LocalDate;
import java.util.*;

@ManagedBean(name = "homeBean")
@ViewScoped
@Getter
@Setter
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

    private boolean nextSlide = false;

    private String chartData;

    @Override
    protected void onConstruct() {
        try {
            createDashboardChart();
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


    public void createDashboardChart() throws PersistenceBeanException, IllegalAccessException {

        ChartWrapper chartWrapper = new ChartWrapper();
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();

        chartWrapper.setType("line");
        chartWrapper.setLabels(new ArrayList<>());
        for(int m = 1 ; m <= month; m++){
            chartWrapper.getLabels().add(DateTimeHelper.getMonth(m));
        }
        Random randomObject = new Random();
        List<ChartDataWrapper> dataSets = new ArrayList<>();
        List<RequestType> requestTypes = DaoManager.load(RequestType.class, new Criterion[]{Restrictions.isNotNull("name")});
        for(RequestType requestType : requestTypes) {
            List<Long> data = new ArrayList<>();
            for(int m = 1 ; m <= month; m++){
                Long requestCount = DaoManager.getCount(Request.class,"id",
                        new Criterion[]{
                                Restrictions.eq("requestType", requestType),
                                Restrictions.ge("evasionDate", DateTimeHelper.getMonthStart(m)),
                                Restrictions.le("evasionDate", DateTimeHelper.getMonthEnd(m))
                        }
                );
                if(requestCount > 0)
                    data.add(requestCount);
            }

            if(data.size() > 0){
                int rand_num = randomObject.nextInt(0xffffff + 1);
                String colorCode = String.format("#%06x", rand_num);
                int pointRadius = randomObject.nextInt((6 - 3) + 1) + 3;
                ChartDataWrapper dataSet = ChartDataWrapper.builder()
                        .label(requestType.getName())
                        .data(data)
                        .borderColor(colorCode)
                        .borderWidth(3)
                        .fill(false)
                        .pointRadius(pointRadius)
                        .build();
                dataSets.add(dataSet);
            }
        }
        chartWrapper.setDatasets(dataSets);
        setChartData(new Gson().toJson(chartWrapper));
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
}
