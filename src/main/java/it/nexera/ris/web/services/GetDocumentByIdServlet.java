package it.nexera.ris.web.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.nexera.ris.common.helpers.APIHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.common.utils.Constants;
import it.nexera.ris.persistence.PersistenceSession;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.CadastralData;
import it.nexera.ris.persistence.beans.entities.domain.Document;
import it.nexera.ris.persistence.beans.entities.domain.Formality;
import it.nexera.ris.persistence.beans.entities.domain.WLGExport;
import it.nexera.ris.web.dto.GetDocumentByIdRequestDTO;
import it.nexera.ris.web.dto.GetDocumentByIdResponseDTO;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

public class GetDocumentByIdServlet extends HttpServlet {

    private static final long serialVersionUID = 424746583004774093L;
    protected transient final Log log = LogFactory.getLog(GetDocumentByIdServlet.class);
    private static final Base64 CODER = new Base64();

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        PersistenceSession ps = null;
        GetDocumentByIdResponseDTO responseDTO = new GetDocumentByIdResponseDTO();
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
                    } else {
                        Long documentId = inputData.getDocumentId();
                        boolean documentFound = false;
                        File documentFile = null;
                        if(StringUtils.isBlank(inputData.getType())
                                || inputData.getType().equalsIgnoreCase("document")){
                            Document document = ConnectionManager.get(Document.class, new Criterion[]{
                                    Restrictions.eq("id", documentId)}, session);
                            if (!ValidationHelper.isNullOrEmpty(document)
                                    && !ValidationHelper.isNullOrEmpty(document.getDocumentPath())) {
                                documentFile = new File(document.getDocumentPath());
                            }
                        }else if(inputData.getType().equalsIgnoreCase("attachment")){
                            WLGExport attachment = ConnectionManager.get(WLGExport.class,
                                    new Criterion[]{
                                    Restrictions.eq("id", documentId)
                            }, session);
                            if (!ValidationHelper.isNullOrEmpty(attachment)
                                    && !ValidationHelper.isNullOrEmpty(attachment.getDestinationPath())) {
                                documentFile = new File(attachment.getDestinationPath());
                            }
                        }
                        if (documentFile != null && documentFile.exists()) {
                            documentFound = true;
                            byte[] fileContent = FileUtils.readFileToByteArray(documentFile);
                            responseDTO.setDocument(CODER.encodeAsString(fileContent));
                            responseDTO.setFileName(documentFile.getName());
                            responseDTO.setContentType(Files.probeContentType(documentFile.toPath()));
                            if(StringUtils.isBlank(responseDTO.getContentType())){
                                FileNameMap fileNameMap
                                        = URLConnection.getFileNameMap();
                                responseDTO.setContentType(fileNameMap.getContentTypeFor(documentFile.getName()));
                            }
                            response.setStatus(HttpServletResponse.SC_OK);
                            responseDTO.setResultCode(Constants.API_SUCCESS_CODE);
                            responseDTO.setResultDescription(Constants.API_GET_DOCUMENT_BY_ID_SUCCESS);
                        }
                        if (!documentFound){
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
