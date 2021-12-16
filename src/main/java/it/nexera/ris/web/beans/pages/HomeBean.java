package it.nexera.ris.web.beans.pages;

import com.google.gson.*;
import it.nexera.ris.common.enums.*;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.common.utils.ForecastUtil;
import it.nexera.ris.common.xml.wrappers.CitySelectItem;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Event;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.persistence.beans.entities.domain.User;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.DayPhrase;
import it.nexera.ris.web.beans.BaseValidationPageBean;
import it.nexera.ris.web.beans.wrappers.ChartDataWrapper;
import it.nexera.ris.web.beans.wrappers.ChartWrapper;
import it.nexera.ris.web.beans.wrappers.MixChartDataWrapper;
import it.nexera.ris.web.beans.wrappers.WorkLoadWrapper;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.primefaces.model.*;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.*;
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

    private List<String> colors;

    private Long selectedUserId;

    private List<SelectItem> users;

    private List<WorkLoadWrapper> workLoadWrappers;

    private String currentDayString;

    private String photo;

    //private Long selectedChartUserId;

    private List<SelectItem> expirationFilters;

    private Long selectedExpirationId;

    @Override
    protected void onConstruct() {
        try {
            if (getCurrentUser().isAdmin()) {
                setUsers(ComboboxHelper.fillList(User.class, Order.asc("createDate"), new Criterion[]{
                        Restrictions.and(
                                Restrictions.or(
                                        Restrictions.eq("category", UserCategories.INTERNO),
                                        Restrictions.isNull("category")
                                ),
                                Restrictions.eq("status", UserStatuses.ACTIVE)
                        )}, Boolean.FALSE));
            } else {
                setSelectedUserId(getCurrentUser().getId());
            }

            setExpirationFilters(ComboboxHelper.fillList(ExpirationFilter.class, false));

            colors = new LinkedList<>();
            colors.add("rgb(0, 63, 92");
            colors.add("rgb(72, 143, 49");
            colors.add("rgb(102, 81, 145");
            colors.add("rgb(66, 165, 245");
            colors.add("rgb(249, 93, 106");
            colors.add("rgb(255, 124, 67");
            colors.add("rgb(255, 166, 0");
            colors.add("rgb(131, 175, 112");
            colors.add("rgb(83, 131, 161");
            colors.add("rgb(0, 134, 139");
            setCurrentDayString(
                    DateTimeHelper.toFormatedString(new Date(), DateTimeHelper.getMonthWordDatePattert()).toUpperCase());
            createDashboardBarChart();
            setNumberTotalRequests(0L);
            setNumberDBRecords(0L);
            setEventModel(new DefaultScheduleModel());
            List<Event> eventList = DaoManager.load(Event.class, new Criterion[]{
                    Restrictions.eq("createUserId", getCurrentUser().getId())});
            for (Event ev : eventList) {
                getEventModel().addEvent(new DefaultScheduleEvent(ev.getText(), ev.getStartDate(), ev.getEndDate()));
            }
            generatePhrase();
            // generateForecast();
            generateWorkload();

        } catch (InstantiationException | IllegalAccessException | PersistenceBeanException e) {
            LogHelper.log(log, e);
        }
    }


    private static String cleanTextContent(String text) {
        if (!ValidationHelper.isNullOrEmpty(text)) {
            text = text.replaceAll("\"", " ").replaceAll("'", " ");
            return text.trim();
        }
        return text;
    }

    public void createDashboardBarChart() throws PersistenceBeanException, IllegalAccessException, InstantiationException {

        ChartWrapper chartWrapper = new ChartWrapper();
        chartWrapper.setType("bar");
        List<RequestType> requestTypes = DaoManager.load(RequestType.class, new Criterion[]{Restrictions.isNotNull("name")});
        // List<String> allRequestTypes = requestTypes.stream().map(RequestType::getName).distinct().collect(Collectors.toList());
        LinkedList<String> chartXAxisData = new LinkedList<>();

        chartWrapper.setXLabel(ResourcesHelper.getString("requestListService"));
        chartWrapper.setYLabel(ResourcesHelper.getString("permissionRequest"));


        List<MixChartDataWrapper> dataSets = new ArrayList<>();
        LinkedList<Integer> data = new LinkedList<>();
        Collections.shuffle(colors);
        LinkedList<List<String>> tooltips = new LinkedList<>();
        List<Long> stateIds = new ArrayList<>();
        stateIds.add(RequestState.INSERTED.getId());
        stateIds.add(RequestState.IN_WORK.getId());
        LinkedList<BarGraph> barGraphs = new LinkedList<>();
        for (RequestType requestType : requestTypes) {
            List<String> tooltip = new ArrayList<>();
            List<Criterion> restrictions = new ArrayList<>();
            if (!ValidationHelper.isNullOrEmpty(getSelectedUserId())) {
                restrictions.add(Restrictions.eq("user.id", getSelectedUserId()));
            }
            restrictions.add(Restrictions.eq("requestType", requestType));
            restrictions.add(Restrictions.in("stateId", stateIds));
            Date now = DateTimeHelper.getNow();
            if(!ValidationHelper.isNullOrEmpty(getSelectedExpirationId())
                    && getSelectedExpirationId().equals(ExpirationFilter.EXPIRED.getId())){
                restrictions.add(Restrictions.le("expirationDate", now));
            }else if(!ValidationHelper.isNullOrEmpty(getSelectedExpirationId())
                    && getSelectedExpirationId().equals(ExpirationFilter.TRA1TO3DAYS.getId())){
                restrictions.add(Restrictions.ge("expirationDate", now));
                restrictions.add(Restrictions.le("expirationDate", DateTimeHelper.addDays(now,3)));
            }else if(!ValidationHelper.isNullOrEmpty(getSelectedExpirationId())
                    && getSelectedExpirationId().equals(ExpirationFilter.TRA4TO10DAYS.getId())){
                restrictions.add(Restrictions.ge("expirationDate", DateTimeHelper.addDays(now,4)));
                restrictions.add(Restrictions.le("expirationDate", DateTimeHelper.addDays(now,10)));
            }
            restrictions.add(
                    Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                            Restrictions.isNull("isDeleted")));
            List<Request> requests = DaoManager.load(Request.class, restrictions.toArray(new Criterion[0]));
            requests.sort(Comparator.comparing(Request::getExpirationDate, Comparator.nullsFirst(Comparator.naturalOrder())));

            for (Request request : requests) {
                StringBuilder sb = new StringBuilder();
                if (!ValidationHelper.isNullOrEmpty(request.getExpirationDate())) {
                    sb.append(DateTimeHelper.toFormatedString(request.getExpirationDate(), DateTimeHelper.getDatePattern()));
                }
                if (!ValidationHelper.isNullOrEmpty(request.getService())) {
                    sb.append(" ");
                    sb.append(cleanTextContent(request.getService().toString()));
                } else if (!ValidationHelper.isNullOrEmpty(request.getMultipleServices())) {
                    List<String> serviceNames = request.getMultipleServices().stream().map(Service::getName).distinct().collect(Collectors.toList());
                    sb.append(" ");
                    sb.append(cleanTextContent(serviceNames.stream()
                            .collect(Collectors.joining(","))));
                }
                if (!ValidationHelper.isNullOrEmpty(request.getSubject())) {
                    sb.append(" ");
                    sb.append(cleanTextContent(request.getSubject().getFullName()));
                }
                if (!ValidationHelper.isNullOrEmpty(request.getUser()))
                    sb.append(String.format("%s %s %s", " (",
                            request.getUser().getFirstName() == null ? "" : request.getUser().getFirstName(),
                            request.getUser().getLastName() == null ? "" : request.getUser().getLastName()) + ")");

                tooltip.add(sb.toString());
            }
            BarGraph barGraph = new BarGraph();
            if (!ValidationHelper.isNullOrEmpty(tooltip)) {
                barGraph.setTooltip(tooltip);
            }
            
            Integer requestCount = requests.size();
            if (requestCount > 0) {
                barGraph.setChartXAxisData(requestType.getName());
                barGraph.setData(requestCount);
                barGraphs.add(barGraph);
            }
        }

        Collections.sort(barGraphs, Comparator.comparingInt(BarGraph::getData).reversed());
        for(BarGraph barGraph : barGraphs) {
        	tooltips.add(barGraph.getTooltip());
        	chartXAxisData.add(barGraph.getChartXAxisData());
        	data.add(barGraph.getData());
        }
//        List<String> missingRequestTypes  = allRequestTypes.stream()
//                .filter(e -> !chartXAxisData.contains(e))
//                .collect(Collectors.toList());
//
        if (data.size() > 0) {
            List<String> borderColors = new ArrayList<>();
            List<String> backgroundColors = new ArrayList<>();
            for (int c = 0; c < chartXAxisData.size(); c++) {
                borderColors.add(colors.get(c) + ")");
                backgroundColors.add(colors.get(c) + ", 0.2)");
            }
            MixChartDataWrapper dataSet = MixChartDataWrapper.builder()
                    .label(ResourcesHelper.getString("requestListService"))
                    .data(data)
                    .backgroundColor(backgroundColors)
                    .borderColor(borderColors)
                    .borderWidth(1)
                    .tooltip(tooltips)
                    .build();
            dataSets.add(dataSet);
        }
        chartWrapper.setLabels(chartXAxisData);
        chartWrapper.setDatasets(dataSets);
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        setChartData(gson.toJson(chartWrapper));
        //log.info("CHart data " + getChartData());
    }

    /*
    public void createDashboardChart() throws PersistenceBeanException, IllegalAccessException {
        ChartWrapper chartWrapper = new ChartWrapper();
        chartWrapper.setType("line");
        chartWrapper.setLabels(new ArrayList<>());
        Random randomObject = new Random();
        List<ChartDataWrapper> dataSets = new ArrayList<>();
        List<RequestType> requestTypes = DaoManager.load(RequestType.class, new Criterion[]{Restrictions.isNotNull("name")});

        List<String> chartXAxisData = requestTypes.stream().map(RequestType::getName).distinct().collect(Collectors.toList());
        chartWrapper.getLabels().addAll(chartXAxisData);

        int colorIndex = 0;
        List<Long> data = new ArrayList<>();
        for(RequestType requestType : requestTypes) {
            Long requestCount = DaoManager.getCount(Request.class,"id",
                    new Criterion[]{
                            Restrictions.eq("requestType", requestType)
                    });
            if(requestCount > 0)
                data.add(requestCount);

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
*/
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
            if (i == 0) {
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
        URL feedSource = new URL("https://www.7timer.info/bin/civillight.php?" + location + "&unit=metric&output=json&tzshift=0");
        HttpURLConnection connection = (HttpURLConnection) feedSource.openConnection();
        connection.connect();
        JsonElement o = new JsonParser().parse(new InputStreamReader((InputStream) connection.getContent()));
        return o.getAsJsonObject();
    }

    private JsonObject getForecastJsonToday(String location) throws IOException {
        URL feedSource = new URL("https://www.7timer.info/bin/civil.php?" + location + "0&unit=metric&output=json&tzshift=0");
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
        if (getCurrentUser().isAdmin()) {
            setUsers(ComboboxHelper.fillList(User.class, Order.asc("createDate"), new Criterion[]{
                    Restrictions.and(
                            Restrictions.or(
                                    Restrictions.eq("category", UserCategories.INTERNO),
                                    Restrictions.isNull("category")
                            ),
                            Restrictions.eq("status", UserStatuses.ACTIVE)
                    )}, Boolean.FALSE));
        }
        List<Criterion> restrictions = new ArrayList<>();
        if (!ValidationHelper.isNullOrEmpty(getSelectedUserId())) {
            restrictions.add(Restrictions.eq("user.id", getSelectedUserId()));
        }
        List<Long> stateIds = new ArrayList<>();
        stateIds.add(RequestState.INSERTED.getId());
        stateIds.add(RequestState.IN_WORK.getId());

        restrictions.add(Restrictions.in("stateId", stateIds));
        restrictions.add(
                Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                        Restrictions.isNull("isDeleted")));

        List<Request> requests = DaoManager.load(Request.class, restrictions.toArray(new Criterion[0]));

        Map<RequestType, List<Request>> groupedByRequestTypes = requests.stream()
                .collect(Collectors.groupingBy(Request::getRequestType));

        for (Map.Entry<RequestType, List<Request>> entry : groupedByRequestTypes.entrySet()) {
            WorkLoadWrapper workLoadWrapper = new WorkLoadWrapper();
            workLoadWrapper.setName(entry.getKey().getName());
            if (entry.getKey().getIcon().startsWith("fa-")) {
                workLoadWrapper.setStyle("font-size: 2em !important");
                workLoadWrapper.setIcon("fa " + entry.getKey().getIcon());
            } else
                workLoadWrapper.setIcon(entry.getKey().getIcon());
            List<Request> groupedRequests = entry.getValue();
            if (!ValidationHelper.isNullOrEmpty(groupedRequests)) {
                Long numberUnclosedRequestsInWork = groupedRequests
                        .stream()
                        .filter(r -> r.getStateId().equals(RequestState.IN_WORK.getId()))
                        .count();
                Long numberNewRequests = groupedRequests
                        .stream()
                        .filter(r -> r.getStateId().equals(RequestState.INSERTED.getId()))
                        .count();
                workLoadWrapper.setNumberUnclosedRequestsInWork(numberUnclosedRequestsInWork);
                workLoadWrapper.setTotal(numberUnclosedRequestsInWork + numberNewRequests);
                if (numberNewRequests != null && numberNewRequests > 0) {
                    Double percentage = (numberUnclosedRequestsInWork * 1.0 / (numberNewRequests + numberUnclosedRequestsInWork)) * 100;
                    workLoadWrapper.setPercentage(percentage.intValue());
                }
            }
            getWorkLoadWrappers().add(workLoadWrapper);
        }

    }

    public void handleUserChange() throws HibernateException, PersistenceBeanException, IllegalAccessException, InstantiationException {
        generateWorkload();
        try {
            getPhoto();
        } catch (Exception e) {
            e.printStackTrace();
        }
        createDashboardBarChart();
    }

    public void handleExpirationFilter() throws HibernateException, PersistenceBeanException, IllegalAccessException, InstantiationException {
        createDashboardBarChart();
    }

    public String getPhoto() throws IOException, PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        if (getCurrentUser().isAdmin()) {
            if (!ValidationHelper.isNullOrEmpty(getSelectedUserId())) {
                User user = DaoManager.get(User.class, getSelectedUserId());
                if (!ValidationHelper.isNullOrEmpty(user.getPhotoPath())) {
                    try {
                        File initialFile = new File(user.getPhotoPath());
                        byte[] fileContent = FileUtils.readFileToByteArray(initialFile);
                        String encodedString = Base64.getEncoder().encodeToString(fileContent);
                        return "data:image/jpg;base64," + encodedString;
                    } catch (Exception e) {
                        LogHelper.log(log, e);
                        e.printStackTrace();
                    }
                }
            }
            return null;
        } else {
            if(!ValidationHelper.isNullOrEmpty(getCurrentUser().getPhotoPath())){
                File initialFile = new File(getCurrentUser().getPhotoPath());
                byte[] fileContent = FileUtils.readFileToByteArray(initialFile);
                String encodedString = Base64.getEncoder().encodeToString(fileContent);
                return "data:image/jpg;base64," + encodedString;
            }

        }
        return null;
    }

    public String getToolTipData() {
        System.out.println(">>>>>>>>>>>>>>");
        return "";
    }
    public void openMailList() {
        String value = Arrays.toString(new Long[]{MailManagerStatuses.NEW.getId()});
        setSessionValue("KEY_MAIL_TYPE_SESSION_KEY_NOT_COPY",value);
        RedirectHelper.goTo(PageTypes.MAIL_MANAGER_LIST);
        
    }
    
    public void openRequestList() {
        setSessionValue("REQUEST_LIST_FILTER_BY",RequestState.INSERTED.name());
        RedirectHelper.goTo(PageTypes.REQUEST_LIST);
        
    }
    
    private void setSessionValue(String key, String value) {
        SessionHelper.put(key, value);
        HttpSessionHelper.put(key, value);
    }
}
