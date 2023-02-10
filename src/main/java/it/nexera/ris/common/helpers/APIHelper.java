package it.nexera.ris.common.helpers;

import it.nexera.ris.common.enums.UserStatuses;
import it.nexera.ris.common.security.crypto.MD5;
import it.nexera.ris.common.utils.Constants;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.entities.domain.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

public class APIHelper {

    protected transient final Log log = LogFactory.getLog(APIHelper.class);

    private static String token = "eyJhbGciOiJIUzI1NiJ9.eyJSb2xlIjoiQWRtaW4iLCJJc3N1ZXIiOiJJc3N1ZXIiLCJVc2VybmFtZSI6IkphdmFJblVzZSIsImV4cCI6MTY3MDk5Mzc3NSwiaWF0IjoxNjcwOTkzNzc1fQ.XI4wO4UrUM3cJToNkIb8na9tjsIKiHMQ0u_N1vi2WVs";

    public String loginValidate(String username, String password, Session session) {
        if (!ValidationHelper.isNullOrEmpty(username)
                && !ValidationHelper.isNullOrEmpty(password)) {
            User user = null;
            try {
                user = ConnectionManager.get(
                        User.class,
                        new Criterion[]
                                {
                                        Restrictions.eq("login", username),
                                        Restrictions.eq("password", APIHelper.isValidMD5(password) ? password :
                                                MD5.encodeString(password, null))}, session);
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
            if (user == null) {
                return Constants.API_CREDENTIALS_FAILURE;
            }
            if (user != null
                    && (user.getStatus() == null || user.getStatus().equals(
                    UserStatuses.INACTIVE))) {
                return Constants.API_CREDENTIALS_WRONG_STATUS;
            }
        }
        return null;
    }

    public static boolean isValidMD5(String s) {
        return s.matches("[a-fA-F0-9]{32}");}

    public static String getToken() {
        return token;
    }
}
