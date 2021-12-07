package it.nexera.ris.common.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class APICall {

    public CloseableHttpResponse apiCall(String xmlString, String apiUrl, String apiKey)
            throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(apiUrl);
        StringEntity entity = null;

        try {
            entity = new StringEntity(xmlString);
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }

        httpPost.setEntity(entity);
        httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
        httpPost.setHeader("apiKey", apiKey);
        CloseableHttpResponse response = null;

        System.out.println("Hitting API");
        response = client.execute(httpPost);
        System.out.println("Response received :: status code :: " + response.getStatusLine().getStatusCode());
        System.out.println("Response received :: status description :: " + response.getStatusLine().getReasonPhrase());

        return response;
    }

}