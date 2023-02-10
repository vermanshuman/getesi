package it.nexera.ris.web.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.nexera.ris.common.helpers.APIHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.common.utils.Constants;
import it.nexera.ris.persistence.PersistenceSession;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.entities.domain.WLGInbox;
import it.nexera.ris.web.dto.GetInboxByIdRequestDTO;
import it.nexera.ris.web.dto.GetInboxByIdResponseDTO;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class GetInboxByIdServlet extends HttpServlet {

    private static final long serialVersionUID = 1182163125732166041L;
    protected transient final Log log = LogFactory.getLog(GetInboxByIdServlet.class);
    private static final Base64 CODER = new Base64();

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        PersistenceSession ps = null;
        GetInboxByIdResponseDTO responseDTO = new GetInboxByIdResponseDTO();
        Gson gson = new GsonBuilder().serializeNulls().create();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            ps = new PersistenceSession();
            Session session = ps.getSession();
            APIHelper apiHelper = new APIHelper();
            String authorization = request.getHeader(Constants.AUTHORIZATION_HEADER_NAME);
            LogHelper.debugInfo(log, "Request received for API (LOGIN)" + authorization);
            if (authorization == null || authorization.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                responseDTO.setResultCode(Constants.API_FAILURE_CODE);
                responseDTO.setResultDescription(Constants.API_CREDENTIALS_FAILURE);
            } else {
                String base64Credentials = authorization.substring("Basic".length()).trim();
                byte[] credDecoded = CODER.decode(base64Credentials);
                String credentials = new String(credDecoded, Charset.forName("UTF-8"));
                final String[] values = credentials.split(":", 2);
                String userName = values[0];
                String password = values[1];
                String validationResponse = apiHelper.loginValidate(userName, password, session);
                if (StringUtils.isNotBlank(validationResponse)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    responseDTO.setResultCode(Constants.API_FAILURE_CODE);
                    responseDTO.setResultDescription(validationResponse);
                } else {
                    BufferedReader reader = request.getReader();
                    GetInboxByIdRequestDTO inputData = gson.fromJson(reader, GetInboxByIdRequestDTO.class);
                    if (inputData == null || inputData.getWlgInboxId() == null || inputData.getWlgInboxId() <= 0) {
                        response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
                        responseDTO.setResultCode(Constants.API_FAILURE_MISSING_PARAMETER);
                        responseDTO.setResultDescription(Constants.API_GET_DOCUMENT_BY_ID_MISSING_DATA);
                    } else {
                        Long wlgInboxId = inputData.getWlgInboxId();

                        boolean mailFound = false;
                        File messageFile = null;

                        WLGInbox wlgInbox = ConnectionManager.get(WLGInbox.class, new Criterion[]{
                                Restrictions.eq("id", wlgInboxId)}, session);
                        if (!ValidationHelper.isNullOrEmpty(wlgInbox)
                                && !ValidationHelper.isNullOrEmpty(wlgInbox.getPath())) {
                            messageFile = new File(wlgInbox.getPath());
                        }
                        if (messageFile != null && messageFile.exists()) {
                            mailFound = true;
                            byte[] fileContent = FileUtils.readFileToByteArray(messageFile);
                            responseDTO.setMailContent(CODER.encodeAsString(fileContent));
                            responseDTO.setFileName(messageFile.getName());
                            response.setStatus(HttpServletResponse.SC_OK);
                            responseDTO.setResultCode(Constants.API_SUCCESS_CODE);
                            responseDTO.setResultDescription(Constants.API_GET_DOCUMENT_BY_ID_SUCCESS);
                        }
                        if (!mailFound) {
                            response.setStatus(HttpServletResponse.SC_OK);
                            responseDTO.setResultCode(Constants.API_FAILURE_CODE);
                            responseDTO.setResultDescription(Constants.API_GET_DOCUMENT_BY_ID_MISSING);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            LogHelper.log(log, ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseDTO.setResultCode(Constants.API_FAILURE_CODE);
            responseDTO.setResultDescription(ex.getMessage());
        } finally {
            if (ps != null) {
                ps.closeSession();
            }
        }
        response.getWriter().write(gson.toJson(responseDTO));
    }
}
