package it.nexera.ris.persistence;

import it.nexera.ris.common.helpers.LogHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.reflections.Reflections;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class HibernateUtil implements IConnectionManager {
    private static transient final Log log;

    private static SessionFactory sessionFactory;

    private static ServiceRegistry serviceRegistry;

    private static List<IConnectionListner> connectionListners = new ArrayList<IConnectionListner>();

    private static String username;

    static {
        log = LogFactory.getLog(HibernateUtil.class);
    }

    public static void addAnnotatedClasses(Configuration config,
                                           boolean addViews) {
        Reflections reflections = new Reflections(
                "it.nexera.ris.persistence.beans.entities.domain");

        Set<Class<?>> annotated = reflections
                .getTypesAnnotatedWith((Class<? extends Annotation>) Entity.class);
        for (Class<?> clazz : annotated) {
            config.addAnnotatedClass(clazz);
        }

        Set<Class<?>> annotatedPk = reflections
                .getTypesAnnotatedWith((Class<? extends Annotation>) Embeddable.class);
        for (Class<?> clazz : annotatedPk) {
            config.addAnnotatedClass(clazz);
        }

        if (addViews) {
            Reflections reflectionsView = new Reflections(
                    "it.nexera.ris.persistence.view");

            Set<Class<?>> annotatedView = reflectionsView
                    .getTypesAnnotatedWith((Class<? extends Annotation>) javax.persistence.Entity.class);
            for (Class<?> clazz : annotatedView) {
                config.addAnnotatedClass(clazz);
            }
        }
    }

    public static void addConnectionListener(IConnectionListner listener) {
        connectionListners.add(listener);
    }

    public static void removeConnectionListener(IConnectionListner listener) {
        connectionListners.remove(listener);
    }

    public static synchronized SessionFactory getSessionFactory(boolean addView) {
        if (sessionFactory == null) {
            createSessionFactory(addView);
        }
        return sessionFactory;
    }

    private static void createSessionFactory(boolean addViews)
            throws ExceptionInInitializerError {
        try {
            Date d1 = new Date();
            System.out.println("HibernateUtil: Opening DB connection.");
            Configuration config = new Configuration();
            addAnnotatedClasses(config, addViews);
            config.configure();
            /*config.setProperty("hibernate.default_schema",
                    config.getProperty("hibernate.connection.username"));*/

            System.out.println("CONNECTION_URL: "
                    + config.getProperty("hibernate.connection.url"));
            System.out.println("USER: "
                    + config.getProperty("hibernate.connection.username"));
            username = config.getProperty("hibernate.connection.username");
            System.out.println("PASSWORD: "
                    + config.getProperty("hibernate.connection.password"));

            System.out.println("DB schema change: "
                    + config.getProperty("hbm2ddl.auto"));

            serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(config.getProperties()).build();

            sessionFactory = config.buildSessionFactory(serviceRegistry);

            checkConnection(d1);
        } catch (Exception ex) {
            onConnectionFail();
            System.err.println("Failed to create sessionFactory object." + ex);
            LogHelper.log(log, ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static void checkConnection() throws Exception {
        Session s = sessionFactory.openSession();
        s.beginTransaction().commit();
        SessionTracker.getInstance().sessionOpening("HibernateUtil");
        s.close();
        SessionTracker.getInstance().sessionClosing("HibernateUtil");
        s = null;
    }

    private static void checkConnection(Date d1) {
        try {
            checkConnection();
        } catch (Exception e) {
            System.out.println("Opening a DB connection... Failed");
            System.out.println(e);
            onConnectionFail();
            return;
        }

        System.out.println(String.format(
                "Connection was successfully opened in %d seconds",
                ((new Date().getTime() - d1.getTime()) / 1000)));
        onSuccessConnection();
    }

    private static void onSuccessConnection() {
        if (connectionListners == null) {
            return;
        }
        for (IConnectionListner item : connectionListners) {
            item.fireConnetionEstablished();
        }
    }

    private static void onConnectionFail() {
        if (connectionListners == null) {
            return;
        }
        for (IConnectionListner item : connectionListners) {
            item.fireConnetionResufed();
        }
    }

    public static void shutdown() {
        onConnectionFail();
        // Close caches and connection pools
        if (sessionFactory != null) {
            sessionFactory.close();
            sessionFactory = null;
        }
    }

    @Override
    public void handleConfigFileChange() {
        HibernateUtil.shutdown();
        createSessionFactory(true);
        System.out.println("SessionFactory recreated");
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        HibernateUtil.username = username;
    }

}
