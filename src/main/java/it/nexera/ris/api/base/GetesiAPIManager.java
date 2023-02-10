package it.nexera.ris.api.base;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class GetesiAPIManager {

    private static transient final Log log = LogFactory.getLog(GetesiAPIManager.class);

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON_UTF_8 = "application/json;utf-8";
    public static final String ACCEPT = "Accept";
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER_TOKEN = "Bearer 1|ysTHb53VjqySAlHmFDwLAAmQtOpC81afkOxIAnOu";

    public static final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

   /* public static Object getAZRequest(Map<String, String> params, Map<String, String> header) throws IOException {
        String urlString = ApplicationSettingsHolder.getInstance().getByKey(
                ApplicationSettingsKeys.API_URL).getValue().trim();
        if(urlString.endsWith("/"))
            urlString = urlString + "getdata";
        else
            urlString = urlString + "/getdata";

        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        setHeader(con, header);
        con.setDoOutput(true);
        String formData = getParamsString(params);
        log.info("Get AZ API Form data " + formData );
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = formData.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        int status = con.getResponseCode();
        if (status != 200) {
            con.disconnect();
            return null;
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();
        return content.toString();
    }*/


    /*public static void main(String[] args) throws IOException {
        Map<String, String> params = new HashMap<>();
        Map<String, String> header = new HashMap<>();
        params.put("ticketID", "1");

//        params.put("servizio", "TS1305");
//        params.put("codice_fiscale", "GHJGHJ66A13F839R");
//        params.put("custom_code", "[1678, 378, 83]");

        header.put(ACCEPT, APPLICATION_JSON_UTF_8);
        header.put(AUTHORIZATION, "Bearer 1|ysTHb53VjqySAlHmFDwLAAmQtOpC81afkOxIAnOu");

        String  responseJsonString = (String) postAZRequest("getdata", params, header);
        log.info("AZ API response " + responseJsonString);
    }*/

    public static Object postAZRequest(String path, Map<String, String> params, Map<String, String> header) throws IOException {
        String urlString = ApplicationSettingsHolder.getInstance().getByKey(
                ApplicationSettingsKeys.API_URL).getValue().trim();
        if(urlString.endsWith("/"))
            urlString = urlString + path;
        else
            urlString = urlString + "/" + path;

        if (params == null)
            params = new HashMap<>();
        URL url = new URL(urlString);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        setHeader(con, header);
        con.setDoOutput(true);
        String formData = getDataString(params);
        log.info("AZ API Form data " + formData );
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = formData.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        int status = con.getResponseCode();
        if (status != 200) {
            con.disconnect();
            return null;
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        in.close();
        con.disconnect();
        return content.toString();
    }

    private static String getDataString(Map<String, String> params) throws UnsupportedEncodingException{
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return result.toString();
    }

    private static void setHeader(URLConnection con, Map<String, String> header) {
        for (Map.Entry<String, String> entry : header.entrySet())
            con.setRequestProperty(entry.getKey(), entry.getValue());
    }

    protected static Map<String, Object> parseJson(String jsonString) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<>();
        try {
            map = mapper.readValue(jsonString, Map.class);

        } catch (Exception e) {
            LogHelper.log(log, e);
            e.printStackTrace();
        }
        return map;
    }

    private static String getParamsString(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        if (params == null)
            return "";

        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            result.append("&");
        }

        String resultString = result.toString();
        return resultString.length() > 0
                ? '?' + resultString.substring(0, resultString.length() - 1)
                : resultString;
    }
}
