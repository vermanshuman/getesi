package it.nexera.ris.web.beans.pages;

import com.google.gson.*;
import it.nexera.ris.common.enums.RequestState;
import it.nexera.ris.common.enums.UserCategories;
import it.nexera.ris.common.enums.UserStatuses;
import it.nexera.ris.common.enums.WeatherCodes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.common.utils.ForecastUtil;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Event;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.persistence.beans.entities.domain.User;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.DayPhrase;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.DayPhrase;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RequestType;
import it.nexera.ris.web.beans.BaseValidationPageBean;
import it.nexera.ris.web.beans.wrappers.ChartDataWrapper;
import it.nexera.ris.web.beans.wrappers.ChartWrapper;
import it.nexera.ris.web.beans.wrappers.WorkLoadWrapper;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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

    private List<String> colorCodes;

    private Long selectedUserId;

    private List<SelectItem> users;

    private List<WorkLoadWrapper> workLoadWrappers;

    private String currentDayString;

    @Override
    protected void onConstruct() {
        try {
            colorCodes = new LinkedList<>();
            colorCodes.add("#003f5c");
            colorCodes.add("#488f31");
            colorCodes.add("#665191");
            colorCodes.add("#42A5F5");
            colorCodes.add("#f95d6a");
            colorCodes.add("#ff7c43");
            colorCodes.add("#ffa600");
            colorCodes.add("#83af70");
            colorCodes.add("#5383a1");
            colorCodes.add("#abd2ec");

            colorCodes.add("#8b4500");
            colorCodes.add("#8b0000");
            colorCodes.add("#4876ff");
            colorCodes.add("#ffff00");
            colorCodes.add("#00868b");
            setCurrentDayString(
                    DateTimeHelper.toFormatedString(new Date(), DateTimeHelper.getMonthWordDatePattert()).toUpperCase());
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
            generateWorkload();

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
        int colorIndex = 0;
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
                int pointRadius = randomObject.nextInt((6 - 3) + 1) + 3;
                ChartDataWrapper dataSet = ChartDataWrapper.builder()
                        .label(requestType.getName())
                        .data(data)
                        .borderColor(colorCodes.get(colorIndex++))
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
            JsonArray dataseries = getForecastJson("lon=14.268120&lat=40.851799").get("dataseries").getAsJsonArray();
            setFutureForecast(fillForecastWeek(dataseries));
            dataseries = getForecastJsonToday("lon=14.268120&lat=40.851799").get("dataseries").getAsJsonArray();
            setForecastToday(fillForecastToday(dataseries));
            updateFutureForcast(dataseries);
        } catch (Exception e) {
            e.printStackTrace();
            setForecastToday(null);
        }
    }

    private List<ForecastUtil> fillForecastWeek(JsonArray channel) {
        List<ForecastUtil> list = new LinkedList<>();
        for (int i = 0; i < channel.size() && i < SHOW_NEXT_WEATHER_DAYS; i++) {
            ForecastUtil util = new ForecastUtil();
            JsonObject itemObj = channel.get(i).getAsJsonObject();
            util.setCode(WeatherCodes.getByString(itemObj.get("weather").getAsString()));
            Date date = DateTimeHelper.fromString(itemObj.get("date").getAsString(), "yyyyMMdd", Locale.ITALY);
            util.setDate(DateTimeHelper.toString(date));

            Calendar cal = Calendar.getInstance(Locale.ITALY);
            cal.setTime(date);
            Integer day = cal.get(Calendar.DAY_OF_MONTH);

            util.setDay(day.toString());
            JsonObject temperature = itemObj.get("temp2m").getAsJsonObject();

            util.setMaxTemp(temperature.get("max").getAsInt());
            util.setMinTemp(temperature.get("min").getAsInt());
            if(i == 0) {
                setForecastToday(util);
            } else {
                list.add(util);
            }
        }
        return list;
    }

    private ForecastUtil fillForecastToday(JsonArray channel) {
        JsonObject itemObj = channel.get(0).getAsJsonObject();
        String temp = itemObj.get("temp2m").getAsString();
        ForecastUtil util = getForecastToday();
        util.setCode(WeatherCodes.getByCode(itemObj.get("cloudcover").getAsInt()));
        util.setTemp(Integer.parseInt(temp));
        util.setHumidity(itemObj.get("rh2m").getAsString());
        util.setWindSpeed(itemObj.get("wind10m").getAsJsonObject().get("speed").getAsString() + " km/h");
        return util;
    }

    private JsonObject getForecastJson(String location) throws IOException {
        URL feedSource = new URL("https://www.7timer.info/bin/civillight.php?"+location+"&unit=metric&output=json&tzshift=0");
        HttpURLConnection connection = (HttpURLConnection) feedSource.openConnection();
        connection.connect();
        JsonElement o = new JsonParser().parse(new InputStreamReader((InputStream) connection.getContent()));
        return o.getAsJsonObject();
    }

    private JsonObject getForecastJsonToday(String location) throws IOException {
        URL feedSource = new URL("https://www.7timer.info/bin/civil.php?"+location+"0&unit=metric&output=json&tzshift=0");
        HttpURLConnection connection = (HttpURLConnection) feedSource.openConnection();
        connection.connect();
        JsonElement o = new JsonParser().parse(new InputStreamReader((InputStream) connection.getContent()));
        return o.getAsJsonObject();
    }

    private void updateFutureForcast(JsonArray channel) {
        for (int i = 0; i < channel.size() && i < getFutureForecast().size(); i++) {
            ForecastUtil util = getFutureForecast().get(i);
            JsonObject itemObj = channel.get(i).getAsJsonObject();
            util.setCode(WeatherCodes.getByCode(itemObj.get("cloudcover").getAsInt()));
            util.setHumidity(itemObj.get("rh2m").getAsString());
            util.setWindSpeed(itemObj.get("wind10m").getAsJsonObject().get("speed").getAsString() + " km/h");
        }
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

    public void generateWorkload() throws PersistenceBeanException, IllegalAccessException {
        setWorkLoadWrappers(new ArrayList<>());
        if(getCurrentUser().isAdmin()){
            setUsers(ComboboxHelper.fillList(User.class, Order.asc("createDate"), new Criterion[]{
                    Restrictions.and(
                            Restrictions.or(
                                    Restrictions.eq("category", UserCategories.INTERNO),
                                    Restrictions.isNull("category")
                            ),
                            Restrictions.eq("status", UserStatuses.ACTIVE)
                    )}));
        }
        List<Criterion> restrictions = new ArrayList<>();
        if(!ValidationHelper.isNullOrEmpty(getSelectedUserId())){
            restrictions.add(Restrictions.eq("user.id",getSelectedUserId()));
        }
        List<Long> stateIds = new ArrayList<>();
        stateIds.add(RequestState.INSERTED.getId());
        stateIds.add(RequestState.IN_WORK.getId());

        restrictions.add(Restrictions.in("stateId",stateIds));
        restrictions.add(
                Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                        Restrictions.isNull("isDeleted")));

        List<Request> requests = DaoManager.load(Request.class,restrictions.toArray(new Criterion[0]));

        Map<RequestType, List<Request>> groupedByRequestTypes = requests.stream()
                    .collect(Collectors.groupingBy(Request::getRequestType));

        for (Map.Entry<RequestType, List<Request>> entry : groupedByRequestTypes.entrySet()) {
            WorkLoadWrapper workLoadWrapper = new WorkLoadWrapper();
            workLoadWrapper.setName(entry.getKey().getName());
            if(entry.getKey().getIcon().startsWith("fa-")) {
                workLoadWrapper.setStyle("font-size: 2em !important");
                workLoadWrapper.setIcon("fa " + entry.getKey().getIcon());
            }else
                workLoadWrapper.setIcon(entry.getKey().getIcon());
            List<Request> groupedRequests = entry.getValue();
            if(!ValidationHelper.isNullOrEmpty(groupedRequests)){
                Long numberUnclosedRequestsInWork = groupedRequests
                        .stream()
                        .filter(r -> r.getStateId().equals(RequestState.IN_WORK.getId()))
                        .count();
                Long numberNewRequests = groupedRequests
                        .stream()
                        .filter(r -> r.getStateId().equals(RequestState.INSERTED.getId()))
                        .count();
                workLoadWrapper.setNumberUnclosedRequestsInWork(numberUnclosedRequestsInWork);
                if(numberNewRequests != null && numberNewRequests > 0){
                    Double percentage = (numberUnclosedRequestsInWork*1.0/(numberNewRequests + numberUnclosedRequestsInWork))*100;
                    workLoadWrapper.setPercentage(percentage.intValue());
                }
            }
            getWorkLoadWrappers().add(workLoadWrapper);
        }

    }
}
