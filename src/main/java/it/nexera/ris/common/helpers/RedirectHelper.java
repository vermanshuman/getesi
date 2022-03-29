package it.nexera.ris.common.helpers;

import it.nexera.ris.common.enums.MailEditType;
import it.nexera.ris.common.enums.PageTypes;
import org.apache.commons.io.IOUtils;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RedirectHelper extends BaseHelper {

    public static final String ID_PARAMETER = "id";

    public static final String EDIT_ID = "editId";

    public static final String PARENT_ID_PARAMETER = "refferentId";

    public static final String FROM_PARAMETER = "from";

    public static final String ONLY_VIEW = "onlyView";

    public static final String MAIL = "mail";

    public static final String MAIL_ID = "mail_id";

    public static final String ARCHIVE_MAIL = "archive_mail";

    public static final String REPLY_MAIL = "reply_mail";

    public static final String REPLY_TO_ALL_MAIL = "reply_to_all_mail";

    public static final String FORWARD_MAIL = "forward_mail";

    public static final String TABLE_PAGE = "page";

    public static final String MULTIPLE = "multiple";

    public static final String MULTIPLE_SERVICE_REQUEST_TYPES = "multipleServiceRequestTypes";

    public static final String REQUEST_ID = "request_id";
    
    public static final String DAYS_PARAMETER = "days";

    public static final String SELECTED = "selected";

    public static final String SALES = "sales";

    public static final String TAB = "tab";

    public static final String REQUEST_TYPE_PARAM = "type";

    public static void goTo(PageTypes type) {
        try {
            if (type != null) {
                sendRedirect(type.getPagesContext());
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static void goToMultiple(PageTypes type) {
        try {
            sendRedirect(type.getPagesContext() + "?" + MULTIPLE + "=" + true);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static void goToMultipleServiceRequestTypes(PageTypes type) {
        try {
            sendRedirect(type.getPagesContext() + "?" + MULTIPLE_SERVICE_REQUEST_TYPES + "=" + true);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static void goTo(PageTypes type, HttpServletRequest request,
                            HttpServletResponse response) {
        try {
            if (type != null) {
                sendRedirect(type.getPagesContext(), request, response);
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static void goTo(PageTypes type, Serializable id) {
        try {
            sendRedirect(type.getPagesContext() + "?" + ID_PARAMETER + "="
                    + (id == null ? "" : id.toString()));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static void goToSavePage(PageTypes type, Serializable id, Serializable pageNum) {
        try {
            sendRedirect(type.getPagesContext() + "?" + (id == null ? "" : ID_PARAMETER + "=" + id.toString() + "&")
                    + TABLE_PAGE + "=" + pageNum);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static void goTo(PageTypes type, Serializable id, boolean newTab) {
        try {
            sendRedirect(type.getPagesContext() + "?" + ID_PARAMETER + "="
                    + (id == null ? "" : id.toString()), newTab);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static void goTo(PageTypes type, Serializable parentId,
                            Serializable id) {
        try {
            sendRedirect(type.getPagesContext() + "?" + PARENT_ID_PARAMETER
                    + "=" + parentId + "&" + ID_PARAMETER + "="
                    + (id == null ? "" : id.toString()));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static void goTo(PageTypes type, Serializable parentId,
                            Serializable id,
                            Serializable editId) {
        try {
            sendRedirect(type.getPagesContext() + "?" + PARENT_ID_PARAMETER
                    + "=" + parentId + "&" + ID_PARAMETER + "="
                    + (id == null ? "" : id.toString()) + "&" + EDIT_ID + "=" + editId);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static void goToOnlyView(PageTypes type, Serializable parentId,
                                    Serializable id, Boolean onlyView) {
        try {
            sendRedirect(type.getPagesContext() + "?" + PARENT_ID_PARAMETER
                    + "=" + parentId + "&" + ID_PARAMETER + "="
                    + (id == null ? "" : id.toString()) + "&" + ONLY_VIEW + "="
                    + onlyView);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static void goToOnlyView(PageTypes type, Serializable id) {
        try {
            sendRedirect(type.getPagesContext() + "?" + ID_PARAMETER + "="
                    + (id == null ? "" : id.toString()) + "&" + ONLY_VIEW + "="
                    + "true");
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static void goToMailEdit(Serializable id, MailEditType type) {
        try {
            sendRedirect(PageTypes.MAIL_MANAGER_EDIT.getPagesContext() + "?" + ID_PARAMETER + "="
                    + (id == null ? "" : id.toString()) + "&" + MAIL + "=" + type.name());
            log.info("after sendRedirect " + type);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static void goToMailEditRequest(Serializable requestId, MailEditType type) {
        try {
            sendRedirect(PageTypes.MAIL_MANAGER_EDIT.getPagesContext() + "?" + REQUEST_ID + "="
                    + (requestId == null ? "" : requestId.toString()) + "&" + MAIL + "=" + type.name());
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static void goToMailEditRequestToSent(Serializable requestId, Long mailId, MailEditType type) {
        try {
            sendRedirect(PageTypes.MAIL_MANAGER_EDIT.getPagesContext() + "?" + REQUEST_ID + "="
                    + (requestId == null ? "" : requestId.toString()) + "&" + MAIL_ID + "=" + mailId  + "&" + MAIL + "=" + type.name());
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static void goToMailEditRequestToSent(Serializable requestId, Long mailId, MailEditType type, List<Long> selectedIds) {
        try {
            sendRedirect(PageTypes.MAIL_MANAGER_EDIT.getPagesContext() + "?" + REQUEST_ID + "="
                    + (requestId == null ? "" : requestId.toString()) + "&" + MAIL_ID + "=" + mailId  + "&" +
                    SELECTED + "=" + selectedIds.toString().replaceAll("[^0-9,]", "") +  "&" + MAIL + "=" + type.name());
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static void goToCreateRequestFromMail(Serializable id) {
        goToCreateRequestFromMail(id, false, false);
    }

    public static void goToCreateRequestFromMail(Serializable id, boolean needArchive, boolean isMultipleCreate) {
        goToCreateRequestFromMail(id, "", needArchive, isMultipleCreate);
    }

    public static void goToCreateRequestFromMail(Serializable id, Serializable requestId, boolean needArchive, boolean isMultipleCreate) {
        try {
            sendRedirect(PageTypes.REQUEST_EDIT.getPagesContext() + "?"
                    + ID_PARAMETER + "=" + requestId + "&"
                    + (needArchive ? ARCHIVE_MAIL : MAIL) + "=" + id
                    + (isMultipleCreate ? "&" + MULTIPLE + "=true" : ""));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static void replyMail(Serializable id) {
        try {
            sendRedirect(PageTypes.MAIL_MANAGER_EDIT.getPagesContext() + "?"
                    + REPLY_MAIL + "=" + id);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static void replyToAllMail(Serializable id) {
        try {
            sendRedirect(PageTypes.MAIL_MANAGER_EDIT.getPagesContext() + "?"
                    + REPLY_TO_ALL_MAIL + "=" + id);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static void forwardMail(Serializable id) {
        try {
            sendRedirect(PageTypes.MAIL_MANAGER_EDIT.getPagesContext() + "?"
                    + FORWARD_MAIL + "=" + id);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    /**
     * Sends the redirect to another page.
     *
     * @param url
     * @throws IOException
     */
    public static void sendRedirect(String url, HttpServletRequest request,
                                    HttpServletResponse response) throws IOException {
        String createdUrl = createUrl(request, url, true);
        response.sendRedirect(createdUrl);
    }

    /**
     * Sends the redirect to another page. newTab only for POST requests
     *
     * @param url
     * @param newTab
     * @throws IOException
     */
    public static void sendRedirect(String url, boolean newTab)
            throws IOException {
        String createdUrl = createUrl(null, url, true);

        if (newTab) {
            PFRequestContextHelper.executeJS(
                    String.format("window.open('%s', '_newtab')", createdUrl));
        } else {
            FacesContext.getCurrentInstance().getExternalContext()
                    .redirect(createdUrl);
        }
    }

    public static void sendExternalRedirect(String url) {
        if (!ValidationHelper.isNullOrEmpty(url)) {
            LogHelper.debugInfo(log, "single import " + url);
            PFRequestContextHelper
                    .executeJS(String.format("window.open('%s', '_blank')",
                            url.replaceAll("\\\\", "/")));
        }
    }

    public static void sendExternalUrl(String url) {
//        if (!ValidationHelper.isNullOrEmpty(url)) {
//            PFRequestContextHelper
//                    .executeJS(String.format("newWindow = window.open('%s', '_blank')",
//                            url.replaceAll("\\\\", "/")));
//            PFRequestContextHelper
//                    .executeJS(String.format("setTimeout(function(){\n" +
//                            "    newWindow.close();\n" +
//                            "}, 3000);"));
//        }
        try {
            URL feedSource = new URL(url.replaceAll("\\\\", "/"));
            LogHelper.debugInfo(log, feedSource.toString());
//            URL feedSource = new URL(new String("http://192.168.1.167:8888/pdfxml/pdfconverter.php?url=C:/test/ris_framework/Third_Part_App/2018/06/25/1/2017_1065256403.pdf&docid=3").replaceAll("\\\\", "/"));
            HttpURLConnection connection = (HttpURLConnection) feedSource.openConnection();
            connection.connect();
            InputStream in = connection.getInputStream();
            String encoding = connection.getContentEncoding();
            encoding = encoding == null ? "UTF-8" : encoding;
            String body = IOUtils.toString(in, encoding);
            if (!ValidationHelper.isNullOrEmpty(body)) {
                Pattern pattern = Pattern.compile("<iframe .*></iframe>");
                Matcher matcher = pattern.matcher(body);
                String subsrt = matcher.find() ? matcher.group() : "";
                int index = subsrt.indexOf("\\\"");
                subsrt = subsrt.substring(index + 2, subsrt.length());
                index = subsrt.indexOf("\\\"");
                subsrt = subsrt.substring(0, index);
                URL feedSource2 = new URL(subsrt.replaceAll("\\\\", "/"));
                LogHelper.debugInfo(log, "inner iframe connection");
                LogHelper.debugInfo(log, feedSource2.toString());
                HttpURLConnection connection2 = (HttpURLConnection) feedSource2.openConnection();
                connection2.connect();
                InputStream in2 = connection2.getInputStream();
                String encoding2 = connection2.getContentEncoding();
                encoding2 = encoding2 == null ? "UTF-8" : encoding2;
                String body2 = IOUtils.toString(in2, encoding2);
                if (!ValidationHelper.isNullOrEmpty(body2)) {
                    LogHelper.debugInfo(log, "has response from inner iframe connection");
                } else {
                    LogHelper.debugInfo(log, "WARN no response from inner iframe connection");
                }
            }
        } catch (IOException e) {
            LogHelper.log(log, e);
            e.printStackTrace();
        }
    }

    public static void sendRedirect(String url) throws IOException {
        String createdUrl = createUrl(null, url, true);

        FacesContext facesContext = FacesContext.getCurrentInstance();

        if (facesContext.getResponseComplete()) {
            return;
        }

        facesContext.getExternalContext().redirect(createdUrl);
    }

    public static void sendRedirectWithoutAppContextname(String url)
            throws IOException {
        String createdUrl = createUrl(null, url, false);
        FacesContext.getCurrentInstance().getExternalContext()
                .redirect(createdUrl);
    }

    public static String createUrl(HttpServletRequest request, String url,
                                   boolean withApplicationContextName) {
        if (request == null) {
            request = (HttpServletRequest) FacesContext.getCurrentInstance()
                    .getExternalContext().getRequest();
        }
        StringBuilder sb = new StringBuilder("");

        sb.append(request.getRequestURL().substring(0,
                request.getRequestURL().indexOf(request.getContextPath())));

        if (withApplicationContextName) {
            sb.append(request.getContextPath());
        }

        if (!url.startsWith("/")) {
            sb.append("/");
        }

        sb.append(url);
        return sb.toString();
    }

    public static void addRequestError(boolean isTms) {
        if (FacesContext.getCurrentInstance() != null) {
            HttpServletRequest request = (HttpServletRequest) FacesContext
                    .getCurrentInstance().getExternalContext().getRequest();
            request.setAttribute(isTms ? "tmsError" : "dbError",
                    ResourcesHelper.getString(isTms ? "loginCouldNotConnect"
                            : "dbCouldNotConnect"));
        }
    }

    public static void goToCreateClientFromMail(Serializable id) {
        goToCreateClientFromMail(id, "");
    }

    public static void goToCreateClientFromMail(Serializable id, Serializable clientId) {
        try {
            sendRedirect(PageTypes.CLIENT_CREATE.getPagesContext() + "?"
                    + MAIL_ID + "=" + id, true);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static void goToMailViewFromClient(Serializable mailId) {
        try {
            sendRedirect(PageTypes.MAIL_MANAGER_VIEW.getPagesContext() + "?"
                    + ID_PARAMETER + "=" + mailId + "&page=0");
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static void goToSalesDevelopment(PageTypes type, Serializable parentId,
                            Serializable id) {
        try {
            sendRedirect(type.getPagesContext() + "?" + PARENT_ID_PARAMETER
                    + "=" + parentId + "&" + ID_PARAMETER + "="
                    + (id == null ? "" : id.toString())  + "&" + SALES + "=" + true);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static void goToMultiple(PageTypes type,String queryParameter) {
        try {
            sendRedirect(type.getPagesContext() + "?" + MULTIPLE + "=" + true + "&"+ queryParameter);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static void goToCreateMultipleRequestFromMail(Serializable id, boolean needArchive, boolean isMultipleCreate, Integer requestType) {
        goToCreateMultipleRequestFromMail(id, "", needArchive, isMultipleCreate, requestType);
    }

    public static void goToCreateMultipleRequestFromMail(Serializable id, Serializable requestId, boolean needArchive, boolean isMultipleCreate, Integer requestType) {
        try {
            sendRedirect(PageTypes.REQUEST_EDIT.getPagesContext() + "?"
                    + ID_PARAMETER + "=" + requestId + "&"
                    + (needArchive ? ARCHIVE_MAIL : MAIL) + "=" + id + "&"
                    + REQUEST_TYPE_PARAM + "=" + requestType + "&"
                    + (isMultipleCreate ? "&" + MULTIPLE + "=true" : "") + "&"+ RedirectHelper.FROM_PARAMETER + "=RICHESTE_MULTIPLE");
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }
}
