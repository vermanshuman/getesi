package it.nexera.ris.web.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.common.utils.Constants;
import it.nexera.ris.persistence.PersistenceSession;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.entities.domain.Document;
import it.nexera.ris.persistence.beans.entities.domain.User;
import it.nexera.ris.web.dto.GetDocumentByIdRequestDTO;
import it.nexera.ris.web.dto.ResponseDto;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

public class UpdateDocumentByIdServlet extends HttpServlet {

    private static final long serialVersionUID = 921790723004153333L;
    protected transient final Log log = LogFactory.getLog(UpdateDocumentByIdServlet.class);
    private static final Base64 CODER = new Base64();

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        PersistenceSession ps = null;
        ResponseDto responseDTO = new ResponseDto();
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
                    GetDocumentByIdRequestDTO inputData = gson.fromJson(reader, GetDocumentByIdRequestDTO.class);
                    if (inputData == null || inputData.getDocumentId() == null || inputData.getDocumentId() <= 0) {
                        response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
                        responseDTO.setResultCode(Constants.API_FAILURE_MISSING_PARAMETER);
                        responseDTO.setResultDescription(Constants.API_GET_DOCUMENT_BY_ID_MISSING_DATA);
                    }else if (StringUtils.isBlank(inputData.getDocument())) {
                        response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
                        responseDTO.setResultCode(Constants.API_FAILURE_MISSING_PARAMETER);
                        responseDTO.setResultDescription(Constants.API_UPDATE_DOCUMENT_BY_ID_MISSING_DOCUMENT);
                    }else if (StringUtils.isBlank(inputData.getFileName())) {
                        response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
                        responseDTO.setResultCode(Constants.API_FAILURE_MISSING_PARAMETER);
                        responseDTO.setResultDescription(Constants.API_UPDATE_DOCUMENT_BY_ID_MISSING_FILE_NAME);
                    } else {
                        Long documentId = inputData.getDocumentId();
                        String documentBase64 = inputData.getDocument();
                        Document document = ConnectionManager.get(Document.class, new Criterion[]{
                                Restrictions.eq("id", documentId)}, session);
                        User user = ConnectionManager.get(User.class, new Criterion[]{
                                Restrictions.eq("login", userName)}, session);
                        if (!ValidationHelper.isNullOrEmpty(document)) {
                            byte [] documentData = CODER.decode(documentBase64);
                            String sb = FileHelper.getDocumentSavePath() +
                                    DateTimeHelper.ToFilePathString(new Date()) + user.getId() + File.separator;
                            Path documentPath = Paths.get(sb);
                            if(Files.notExists(documentPath))
                                Files.createDirectories(documentPath);
                            document.setPath(FileHelper.writeFileToFolder(inputData.getFileName(), documentPath.toFile(),
                                    documentData));
                            document.setTitle(FilenameUtils.getBaseName(inputData.getFileName()));
                            ConnectionManager.save(document, true, session);
                            response.setStatus(HttpServletResponse.SC_OK);
                            responseDTO.setResultCode(Constants.API_SUCCESS_CODE);
                            responseDTO.setResultDescription(Constants.API_GET_DOCUMENT_BY_ID_SUCCESS);
                        } else {
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
