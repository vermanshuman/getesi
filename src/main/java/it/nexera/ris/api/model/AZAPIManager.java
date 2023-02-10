package it.nexera.ris.api.model;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import it.nexera.ris.api.base.GetesiAPIManager;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.web.dto.AZSendRequestDTO;
import it.nexera.ris.web.dto.AZSendRequestResponseDTO;
import it.nexera.ris.web.dto.GetInboxByIdRequestDTO;
import it.nexera.ris.web.dto.GetInboxByIdResponseDTO;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class AZAPIManager extends GetesiAPIManager {
    private static transient final Log log = LogFactory.getLog(AZAPIManager.class);

    public static AZSendRequestResponseDTO sendAZRequest(AZSendRequestDTO requestDTO) {

        log.info("Send request sendAZRequest");
        Map<String, String> params = new HashMap<>();
        Map<String, String> header = new HashMap<>();

        if (StringUtils.isNotBlank(requestDTO.getService()))
            params.put("servizio", requestDTO.getService());

        if (StringUtils.isNotBlank(requestDTO.getFiscalCode()))
            params.put("codice_fiscale", requestDTO.getFiscalCode());

        if (requestDTO.getCustomCode() != null && !requestDTO.getCustomCode().isEmpty())
            params.put("custom_code", JSONArray.toJSONString(requestDTO.getCustomCode()));

        header.put(ACCEPT, APPLICATION_JSON_UTF_8);
        header.put(AUTHORIZATION, BEARER_TOKEN);
        try {
            String  responseJsonString = (String) postAZRequest("request", params, header);
            log.info("AZ API response " + responseJsonString);
            if(StringUtils.isNotBlank(responseJsonString))
                return mapper.readValue(responseJsonString, AZSendRequestResponseDTO.class);
        } catch (Exception e) {
            LogHelper.log(log, e);
            e.printStackTrace();
        }
        return null;
    }

    public static AZSendRequestResponseDTO getAZData(String ticketId) {
        log.info("Send request to get AZ Data " + ticketId);

        Map<String, String> params = new HashMap<>();
        Map<String, String> header = new HashMap<>();
        if (StringUtils.isNotBlank(ticketId))
            params.put("ticketID", ticketId);

        header.put(ACCEPT, APPLICATION_JSON_UTF_8);
        header.put(AUTHORIZATION, BEARER_TOKEN);

        try {
            String  responseJsonString = (String) postAZRequest("getdata", params, header);
            log.info("AZ Get API response " + responseJsonString);
            if(StringUtils.isNotBlank(responseJsonString))
                return mapper.readValue(responseJsonString, AZSendRequestResponseDTO.class);
        } catch (Exception e) {
            LogHelper.log(log, e);
            e.printStackTrace();
        }
        return null;
    }
}

