package it.nexera.ris.common.helpers.omi;

import it.nexera.ris.common.helpers.BaseHelper;
import it.nexera.ris.common.helpers.FileHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

public class GeocodeHelper extends BaseHelper {
    private static final String SERVICE_URL = "https://geocoder.ls.hereapi.com/6.2/geocode.json";
    private static final String API_KEY = "?apiKey=" + getApiKey();
    private static final String SEARCH_PARAMETER = "&searchtext=";

    private static String getApiKey() {
        return FileHelper.getApplicationProperties().getProperty("geocodeApiKey");
    }

    private static String getRequest(String url) throws IOException {
        final URL obj = new URL(url);
        final HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("GET");

        if (con.getResponseCode() != 200) {
            return null;
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    public static List<Pair<Double, Double>> getCoordinates(String address) throws Exception {

        StringBuffer query;
        if(address.contains("`"))
            address = address.replaceAll("`", "'");
        String[] split = address.split(" ");
        String queryResult = null;

        query = new StringBuffer();
        query.append(SERVICE_URL).append(API_KEY).append(SEARCH_PARAMETER);

        if (split.length == 0) {
            return null;
        }

        for (int i = 0; i < split.length; i++) {
            query.append(split[i]);
            if (i < (split.length - 1)) {
                query.append("+");
            }
        }

        log.info("Query:" + query);

        try {
            queryResult = getRequest(query.toString());
        } catch (Exception e) {
            throw new Exception("Error when trying to get data with the following query " + query + ", try again", e);
        }

        if (queryResult == null) {
            throw new Exception("Address was not found");
        }

        Object obj = JSONValue.parse(queryResult);

        if (obj instanceof JSONObject) {
            return parseJsonResponse((JSONObject) obj);
        } else {
            return null;
        }
    }

    private static List<Pair<Double, Double>> parseJsonResponse(JSONObject jsonObject) {
        List<Pair<Double, Double>> foundCoordinates = new LinkedList<>();
        JSONObject response = (JSONObject) jsonObject.get("Response");
        JSONArray view = (JSONArray) response.get("View");
        for (Object o : view) {
            JSONArray result = (JSONArray) ((JSONObject) o).get("Result");
            for (Object entry : result) {
                JSONObject location = (JSONObject) ((JSONObject) entry).get("Location");
                JSONObject displayPosition = (JSONObject) location.get("DisplayPosition");
                double latitude = (double) displayPosition.get("Latitude");
                double longitude = (double) displayPosition.get("Longitude");
                foundCoordinates.add(Pair.of(longitude, latitude));
            }
        }
        return foundCoordinates;
    }

}
