package it.nexera.ris.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

public class APICall {

    public CloseableHttpResponse apiCall(String xmlString, String apiUrl, String apiKey, Log log)
            throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(apiUrl);
        List<NameValuePair> form = new ArrayList<>();
        form.add(new BasicNameValuePair("apiKey", apiKey));
        form.add(new BasicNameValuePair("xml", xmlString));

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, Consts.UTF_8);

        httpPost.setEntity(entity);
        httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
        httpPost.setHeader("apiKey", apiKey);
        CloseableHttpResponse response = null;

        log.info("Hitting API");
        response = client.execute(httpPost);
        log.info("Response received :: status code :: " + response.getStatusLine().getStatusCode());
        log.info("Response received :: status description :: " + response.getStatusLine().getReasonPhrase());

        return response;
    }

}