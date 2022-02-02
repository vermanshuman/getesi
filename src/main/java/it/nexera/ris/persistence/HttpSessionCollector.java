package it.nexera.ris.persistence;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.SessionHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.wrappers.logic.UserWrapper;
import org.primefaces.push.EventBus;
import org.primefaces.push.EventBusFactory;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class HttpSessionCollector implements HttpSessionListener {
    private static final List<HttpSession> sessions = new CopyOnWriteArrayList<HttpSession>();

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        sessions.add(event.getSession());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        sessions.remove(event.getSession());
    }

    public static synchronized void checkUniqueUser(Long id) {
        if (!ValidationHelper.isNullOrEmpty(id)
                && !ValidationHelper.isNullOrEmpty(sessions)) {
            for (HttpSession session : sessions) {
                UserWrapper user = (UserWrapper) session
                        .getAttribute("USER_HOLDER_ATTRIBUTE");

                if (user != null && id.equals(user.getId())) {
                    StringBuffer sb = new StringBuffer();

                    sb.append(user.getId());
                    sb.append("__separator__");
                    sb.append(SessionHelper.get("my_session_id"));

                    EventBus eventBus = EventBusFactory.getDefault().eventBus();
                    eventBus.publish("/notify", sb.toString());
                }
            }
        }
    }

    public static synchronized void checkEmptySession() {
        if (!ValidationHelper.isNullOrEmpty(sessions)) {
            for (HttpSession session : sessions) {
                if (DateTimeHelper.differenceInMinutes(
                        new Date(session.getLastAccessedTime()),
                        new Date()) > loadTimeout()
                        && session
                        .getAttribute("ON_DOCUMENT_GENERATION") != null
                        && ((Boolean) session
                        .getAttribute("ON_DOCUMENT_GENERATION"))) {
                    session.invalidate();
                }
            }
        }
    }

    private static int loadTimeout() {
        if (!ValidationHelper
                .isNullOrEmpty(ApplicationSettingsHolder.getInstance()
                        .getByKey(ApplicationSettingsKeys.SESSION_TIMEOUT))
                && !ValidationHelper
                .isNullOrEmpty(ApplicationSettingsHolder.getInstance()
                        .getByKey(
                                ApplicationSettingsKeys.SESSION_TIMEOUT)
                        .getValue())) {
            return Integer.parseInt(ApplicationSettingsHolder.getInstance()
                    .getByKey(ApplicationSettingsKeys.SESSION_TIMEOUT)
                    .getValue());
        }

        return 120;
    }

}
