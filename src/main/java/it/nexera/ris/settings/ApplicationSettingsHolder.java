package it.nexera.ris.settings;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.MailHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.persistence.HibernateUtil;
import it.nexera.ris.persistence.SessionTracker;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.entities.domain.ApplicationSettingsValue;
import it.nexera.ris.web.beans.wrappers.logic.ApplicationSettingsValueWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationSettingsHolder {
    private final ConcurrentHashMap<ApplicationSettingsKeys, ApplicationSettingsValue> settings = new ConcurrentHashMap<ApplicationSettingsKeys, ApplicationSettingsValue>();

    private final ConcurrentHashMap<ApplicationSettingsKeys, Object> monitors = new ConcurrentHashMap<ApplicationSettingsKeys, Object>();

    private final static Log log = LogFactory.getLog(ApplicationSettingsHolder.class);

    private static volatile ApplicationSettingsHolder instance;

    private static volatile Session session;

    // Default settings
    private static final int DEFAULT_PASSWORD_EXPIRATION_PERIOD = 10;
    
    private static final String DEFAULT_SOFFICE_COMMAND = "soffice";
    
    private static final String DEFAULT_SOFFICE_TEMP_DIR_PREFIX = "/tmp/getesi-";

    private static String RIS_ADDRESS;

    public ApplicationSettingsHolder() {
        if (instance != null) {
            throw new IllegalStateException();
        }

        doInitialLoading();

        instance = this;
    }

    @SuppressWarnings("unchecked")
    private void doInitialLoading() {
        List<ApplicationSettingsValue> list = null;

        try {
            session = HibernateUtil.getSessionFactory(true).openSession();
            SessionTracker.getInstance().sessionOpening(
                    "ApplicationSettingsHolder");
            if (session == null || !session.isConnected() || !session.isOpen()) {
                log.error("Error creating session");
                return;
            }

            try {
                list = ConnectionManager.load(ApplicationSettingsValue.class, new Criterion[]{
                        Restrictions.in("key", ApplicationSettingsKeys.values())
                }, session);
            } catch (HibernateException e) {
                log.error("Failed to load application settings");
            }

            if (list == null) {
                return;
            }

            for (ApplicationSettingsValue val : list) {
                ApplicationSettingsKeys key = ApplicationSettingsKeys
                        .valueOf(val.getKey().name());

                if (key != null) {
                    settings.put(key, val);
                    monitors.put(key, new Object());
                }
            }

            createDefaultSettings();
        } catch (Exception e) {
            LogHelper.log(log, e);
        } finally {
            session.close();
            SessionTracker.getInstance().sessionClosing(
                    "ApplicationSettingsHolder");
            session = null;
        }
    }

    private void createDefaultSettings() {
        for (ApplicationSettingsKeys key : ApplicationSettingsKeys.values()) {
            if (!settings.containsKey(key) || key.isNeedToRefresh()) {
                ApplicationSettingsValue val = new ApplicationSettingsValue();
                val.setKey(key);
                val.setValue(getDefaultValueByKey(key));

                settings.put(key, val);
                monitors.put(key, new Object());

                applyNewValue(key, val.getValue());
            }
        }
    }

    public String getDefaultValueByKey(ApplicationSettingsKeys key) {
        if (key == null) {
            return null;
        }

        if (key.equals(ApplicationSettingsKeys.PASSWORD_EXPIRATION_PERIOD)) {
            return String.valueOf(DEFAULT_PASSWORD_EXPIRATION_PERIOD);
        }
        if (key.equals(ApplicationSettingsKeys.RECEIVED_SERVER_ID)) {
            Long serverId = MailHelper.mailReceiveServerId(session);
            return Long.toString(serverId == null ? 0L : serverId);
        }
        if (key.equals(ApplicationSettingsKeys.SENT_SERVER_ID)) {
            Long serverId = MailHelper.mailSendServerId(session);
            return Long.toString(serverId == null ? 0L : serverId);
        }
        
        if (key.equals(ApplicationSettingsKeys.SOFFICE_COMMAND))
        	return DEFAULT_SOFFICE_COMMAND;

        if (key.equals(ApplicationSettingsKeys.SOFFICE_TEMP_DIR_PREFIX))
        	return DEFAULT_SOFFICE_TEMP_DIR_PREFIX;
        
        if (key.equals(ApplicationSettingsKeys.DOCUMENT_CONVERSION_HEADER_IMAGE))
        	return ResourcesHelper.getString("dcsDefaultHeaderImage");
        
        if (key.equals(ApplicationSettingsKeys.DOCUMENT_CONVERSION_FOOTER_IMAGE))
        	return ResourcesHelper.getString("dcsDefaultFooterImage");
        
        return null;
    }

    public static ApplicationSettingsHolder getInstance() {
        if (instance == null) {
            synchronized (ApplicationSettingsHolder.class) {
                if (instance == null) {
                    instance = new ApplicationSettingsHolder();
                }
            }
        }

        return instance;
    }

    public ApplicationSettingsValueWrapper getByKey(ApplicationSettingsKeys key) {
        if (key == null) {
            return null;
        }

        if (settings.containsKey(key) && monitors.containsKey(key)) {
            ApplicationSettingsValueWrapper setting = null;
            Object monitor = monitors.get(key);
            synchronized (monitor) {
                setting = new ApplicationSettingsValueWrapper(key,
                        handleReadValue(key, settings.get(key).getValue()));
            }

            return setting;
        }

        return null;
    }

    public boolean applyNewValue(ApplicationSettingsKeys key, String value) {
        return applyNewValue(new ApplicationSettingsValueWrapper(key, value));
    }

    @SuppressWarnings("unchecked")
    public void refreshHolder() {
        List<ApplicationSettingsValue> list = null;
        try {
            session = HibernateUtil.getSessionFactory(true).openSession();
            SessionTracker.getInstance().sessionOpening(
                    "ApplicationSettingsHolder");
            if (session == null || !session.isConnected() || !session.isOpen()) {
                log.error("Error creating session");
                return;
            }

            try {
                list = session.createCriteria(ApplicationSettingsValue.class)
                        .list();
            } catch (HibernateException e) {
                log.error("Failed to load application settings");
            }

            if (list == null) {
                return;
            }

            for (ApplicationSettingsValue val : list) {
                if (ApplicationSettingsKeys.valueOf(val.getKey().name()) != null
                        && settings.containsKey(ApplicationSettingsKeys
                        .valueOf(val.getKey().name()))
                        && monitors.containsKey(ApplicationSettingsKeys
                        .valueOf(val.getKey().name()))) {
                    Object monitor = monitors.get(ApplicationSettingsKeys
                            .valueOf(val.getKey().name()));
                    synchronized (monitor) {
                        settings.replace(ApplicationSettingsKeys.valueOf(val
                                .getKey().name()), val);
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            session.close();
            SessionTracker.getInstance().sessionClosing(
                    "ApplicationSettingsHolder");
            session = null;
        }

    }

    public boolean applyNewValue(ApplicationSettingsValueWrapper setting) {
        if (setting == null) {
            return false;
        }

        if (setting.getKey() == null) {
            return false;
        }

        boolean isOpenedNewSession = false;

        if (settings.containsKey(setting.getKey())
                && monitors.containsKey(setting.getKey())) {
            Object monitor = monitors.get(setting.getKey());
            synchronized (monitor) {
                ApplicationSettingsValue val = settings.get(setting.getKey());
                val.setValue(handleNewValue(setting.getKey(),
                        setting.getValue()));

                try {
                    if (session == null || !session.isOpen()) {
                        session = HibernateUtil.getSessionFactory(true)
                                .openSession();
                        isOpenedNewSession = true;
                        SessionTracker.getInstance().sessionOpening(
                                "ApplicationSettingsHolder");
                    }

                    Transaction tr = null;
                    try {
                        tr = session.beginTransaction();

                        if (session.contains(val)) {
                            session.merge(val);
                        } else {
                            session.saveOrUpdate(val);
                        }
                    } catch (Exception e) {
                        if (tr != null) {
                            tr.rollback();
                        }
                        LogHelper.log(log, e);
                    } finally {
                        if (tr != null && !tr.wasRolledBack() && tr.isActive()) {
                            tr.commit();
                        }
                    }
                } catch (HibernateException e) {
                    return false;
                } finally {
                    if (isOpenedNewSession) {
                        session.close();
                        session = null;
                        SessionTracker.getInstance().sessionClosing(
                                "ApplicationSettingsHolder");
                    }
                }
            }
        } else {
            return false;
        }

        return true;
    }

    private String handleNewValue(ApplicationSettingsKeys key, String value) {
        if (key == null) {
            return value;
        }

        if (value == null || value.isEmpty()) {
            return value;
        }

        return value;
    }

    private String handleReadValue(ApplicationSettingsKeys key, String value) {
        return value;
    }

    public static String getRIS_ADDRESS() {
        return RIS_ADDRESS;
    }

    public static void setRIS_ADDRESS(String rIS_ADDRESS) {
        RIS_ADDRESS = rIS_ADDRESS;
    }

}
