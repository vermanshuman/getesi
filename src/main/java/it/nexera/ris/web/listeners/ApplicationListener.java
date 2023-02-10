package it.nexera.ris.web.listeners;

import com.mchange.v2.c3p0.C3P0Registry;
import com.mchange.v2.c3p0.PooledDataSource;
import it.nexera.ris.common.enums.LandChargesRegistryType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.HibernateUtil;
import it.nexera.ris.persistence.IConnectionListner;
import it.nexera.ris.persistence.PersistenceSession;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.entities.Entity;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.*;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.services.*;
import it.nexera.ris.web.services.base.ServiceHolder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.*;
import java.lang.reflect.Method;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class ApplicationListener implements ServletContextListener, IConnectionListner {
    public transient final Log log = LogFactory.getLog(getClass());

    private final Properties projectProperties = new Properties();

    private static final String PROJECT_PROPERTIES_FILE_NAME = "project.properties";

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        LogHelper.debugInfo(log, "GETESI VERSION : 2.1.71.16");
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Locale.setDefault(Locale.ITALIAN);

        initializeProps();

        FileHelper.setRealPath(servletContextEvent.getServletContext().getRealPath("/"));
        HibernateUtil.addConnectionListener(this);
        HibernateUtil.getSessionFactory(false);
    }

    private void initializeProps() {
        System.out.println("Start reading project properties...");

        try {
            InputStream is = new FileInputStream(new File("./"
                    + PROJECT_PROPERTIES_FILE_NAME));
            projectProperties.load(is);
        } catch (FileNotFoundException e) {
            InputStream is = ApplicationListener.class.getResourceAsStream("/"
                    + PROJECT_PROPERTIES_FILE_NAME);
            try {
                projectProperties.load(is);
            } catch (IOException e1) {
                LogHelper.log(log, "Project properties hasn't been read.");
                return;
            }
        } catch (IOException e) {
            InputStream is = ApplicationListener.class.getResourceAsStream("/"
                    + PROJECT_PROPERTIES_FILE_NAME);
            try {
                projectProperties.load(is);
            } catch (IOException e1) {
                LogHelper.log(log, "Project properties hasn't been read.");
                return;
            }
        }

        ApplicationSettingsHolder.setRIS_ADDRESS(projectProperties.getProperty("ris_address"));

        System.out.println("Reading project properties ended.");
    }

    @Override
    public void fireConnetionEstablished() {
        tryStopServices();

        startServices();

        try {
            initData();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private void initData() throws PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        try {
            PersistenceSession ps = null;
            Transaction tr = null;
            try {
                ps = new PersistenceSession();
                tr = ps.getSession().beginTransaction();
                DBFiller.createViews(ps.getSession());
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
                if (tr != null && tr.isActive()) {
                    tr.rollback();
                }
            } finally {
                if (tr != null && tr.isActive() && !tr.wasRolledBack()) {
                    tr.commit();
                }
                if (ps != null) {
                    ps.closeSession();
                }
            }

            try {
                ps = new PersistenceSession();
                tr = ps.getSession().beginTransaction();

                List<AggregationLandChargesRegistry> aggregationLandChargesRegistries =
                        ConnectionManager.load(AggregationLandChargesRegistry.class,
                                new CriteriaAlias[]{
                                        new CriteriaAlias("landChargesRegistries", "l", JoinType.LEFT_OUTER_JOIN)
                                },
                                new Criterion[]{
                                Restrictions.or(
                                        Restrictions.eq("isDeleted", Boolean.FALSE),
                                        Restrictions.isNull("isDeleted"))
                }, ps.getSession());

                for (AggregationLandChargesRegistry aggregationLandChargesRegistry : aggregationLandChargesRegistries) {
                    String typeValue = null;
                    if (!ValidationHelper.isNullOrEmpty(aggregationLandChargesRegistry.getLandChargesRegistries())) {
                        boolean allConservatory = aggregationLandChargesRegistry.getLandChargesRegistries()
                                .stream()
                                .filter(lcr -> !ValidationHelper.isNullOrEmpty(lcr.getType()))
                                .allMatch(x -> x.getType().equals(LandChargesRegistryType.CONSERVATORY));
                        boolean allTavolare = aggregationLandChargesRegistry.getLandChargesRegistries()
                                .stream()
                                .filter(lcr -> !ValidationHelper.isNullOrEmpty(lcr.getType()))
                                .allMatch(lcr -> lcr.getType().equals(LandChargesRegistryType.TAVOLARE));

                        if (allConservatory)
                            typeValue = "C";
                        else if (allTavolare)
                            typeValue = "T";
                    }
                    aggregationLandChargesRegistry.setType(typeValue);
                    ConnectionManager.save(aggregationLandChargesRegistry, ps.getSession());
                }

                if (DBFiller.needFillEntity(ps.getSession(), Role.class)) {
                    for (Role item : DBFiller.fillRoles()) {
                        ConnectionManager.save(item, ps.getSession());
                    }
                }
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
                if (tr != null && tr.isActive()) {
                    tr.rollback();
                }
            } finally {
                if (tr != null && tr.isActive() && !tr.wasRolledBack()) {
                    tr.commit();
                }
                if (ps != null) {
                    ps.closeSession();
                }
            }

            try {
                ps = new PersistenceSession();
                tr = ps.getSession().beginTransaction();
                DBFiller.updateTable(ps.getSession());
                if (DBFiller.needFillUsers(ps.getSession())) {
                    for (User item : DBFiller.fillUsers(ps.getSession())) {
                        ConnectionManager.save(item, ps.getSession());
                    }
                }
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
                if (tr != null && tr.isActive()) {
                    tr.rollback();
                }
            } finally {
                if (tr != null && tr.isActive() && !tr.wasRolledBack()) {
                    tr.commit();
                }
                if (ps != null) {
                    ps.closeSession();
                }
            }

            try {
                ps = new PersistenceSession();
                tr = ps.getSession().beginTransaction();

                if (DBFiller.needFillEntity(ps.getSession(), WLGFolder.class)) {
                    for (WLGFolder item : DBFiller.fillFolders(ps.getSession())) {
                        ConnectionManager.save(item, ps.getSession());
                    }
                }
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
                if (tr != null && tr.isActive()) {
                    tr.rollback();
                }
            } finally {
                if (tr != null && tr.isActive() && !tr.wasRolledBack()) {
                    tr.commit();
                }
                if (ps != null) {
                    ps.closeSession();
                }
            }

            fillFromXml(ps, tr, Asl.class, "fillAsls", "getCode", "code");
            fillFromXml(ps, tr, AslRegion.class, "fillAslRegions", "getCode", "code");
            fillFromXml(ps, tr, Province.class, "fillProvinces", "getCode", "code");

            // need be after asl, aslregion and province
            fillFromXml(ps, tr, City.class, "fillCities", "getCode", "code");

            // need be after aslregion
            fillFromXml(ps, tr, Nationality.class, "fillNationalities", "getCode", "code");

            fillFromXml(ps, tr, Country.class, "fillContries", "getCode", "code");

            fillFromXml(ps, tr, Module.class, "fillModuleNonEnums", "getCode", "code");

            fillFromXml(ps, tr, ModulePage.class, "fillModulePages", "getPage_type", "page_type");

            fillFromXml(ps, tr, Permission.class, "fillPermissions", "getModule_code", "module.code");

            fillFromXml(ps, tr, TypeAct.class, "fillTypeActs");

            fillFromXml(ps, tr, TypeFormality.class, "fillTypeFormalities", "getCode", "code");

            fillFromXml(ps, tr, TypeFormality.class, "TypeFormalityAdditionalData.xml",
                    "fillTypeFormalityAdditional", null, null,
                    false, false);

            fillFromXml(ps, tr, CadastralTopology.class, "fillCadastralTopologies", "getDescription", "description");

            fillFromXml(ps, tr, LandChargesRegistry.class, "fillLandChargesRegistries");

            fillFromXml(ps, tr, LandChargesRegistry.class, "LandChargesRegistrySymbolData.xml",
                    "fillLandChargesRegistrySymbols", null, null,
                    false, false);
            updateClients();
        } finally {
            HibernateUtil.removeConnectionListener(this);
            HibernateUtil.shutdown();
            HibernateUtil.getSessionFactory(true);
            HibernateUtil.addConnectionListener(this);
        }
    }
    
    private void updateClients() {
        PersistenceSession ps = null;
        Transaction tr = null;
            try {
                ps = new PersistenceSession();
                Session session = ps.getSession();
                tr = ps.getSession().beginTransaction();
                
                List<Client> clients = ConnectionManager.load(Client.class, new Criterion[]{
                        Restrictions.isNull("clientName")
                }, session);
                
                for (Client client : clients) {
                    if(StringUtils.isBlank(client.getClientName())) {
                        client.setClientName(client.toString().toLowerCase());
                        ConnectionManager.save(client, session);
                    }
                }
                
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
                if (tr != null && tr.isActive()) {
                    tr.rollback();
                }
            } finally {
                if (tr != null && tr.isActive() && !tr.wasRolledBack()) {
                    try {
                        tr.commit();
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        if (tr.isActive()) {
                            tr.rollback();
                        }
                    }
                }
                if (ps != null) {
                    ps.closeSession();
                }
            }
            
            
    }

    private void fillFromXml(PersistenceSession ps, Transaction tr,
                             Class<? extends Entity> entity, String methodName,
                             String checkedMethodName, String checkedFieldName) {
        fillFromXml(ps, tr, entity, null, methodName, checkedMethodName, checkedFieldName, true, true);
    }

    private void fillFromXml(PersistenceSession ps, Transaction tr,
                             Class<? extends Entity> entity, String methodName) {
        fillFromXml(ps, tr, entity, null, methodName, null, null, false, true);
    }

    private void fillFromXml(PersistenceSession ps, Transaction tr,
                             Class<? extends Entity> entity, String dataFileName, String methodName,
                             String checkedMethodName, String checkedFieldName, boolean needCheck, boolean needCheckFillEntity) {
        try {
            ps = new PersistenceSession();
            tr = ps.getSession().beginTransaction();

            if (needCheckFillEntity && !DBFiller.needFillEntity(ps.getSession(), entity)) {
                return;
            }
            if (dataFileName == null) {
                dataFileName = String.format("/WEB-INF/classes/data/%sData.xml", entity.getSimpleName());
            } else {
                dataFileName = String.format("/WEB-INF/classes/data/%s", dataFileName);
            }
            File file = new File(FileHelper.getLocalDir(), dataFileName);
            LogHelper.log(log, String.format("fillFromXml file name : %s %s", file.getName(), file.exists()));
            Method method = null;
            try {
                LogHelper.log(log, "fillFromXml methodName : " + methodName);
                method = DBFiller.class.getDeclaredMethod(methodName, File.class, Session.class);
            } catch (Exception e) {
                LogHelper.log(log, e);
            }

            if (method == null) {
                return;
            }
            @SuppressWarnings("unchecked")
            List<? extends Entity> list = (List<? extends Entity>) (method.invoke(DBFiller.class, file, ps.getSession()));

            if (ValidationHelper.isNullOrEmpty(list)) {
                return;
            }
            try {
                Method checkedMethod = null;
                if (needCheck) {
                    checkedMethod = getCheckedMethod(entity, checkedMethodName);
                    if (checkedMethod == null) {
                        return;
                    }
                }
                for (Entity item : list) {
                    if (needCheck) {
                        Object checkedValue = checkedMethod.invoke(item, new Class[0]);

                        if (checkedValue != null) {
                            if (!(item instanceof Permission) && 0 != ConnectionManager.getCount(entity,
                                    "id", new Criterion[]{
                                            Restrictions.eq(checkedFieldName, checkedValue)
                                    }, ps.getSession())
                                    || (item instanceof Permission) && 0 != ConnectionManager.getCount(entity,
                                    "id",
                                    new Criterion[]{
                                            Restrictions.eq("module.id", ((Permission) item).getModule().getId()),
                                            Restrictions.eq("role.id", ((Permission) item).getRole_id())
                                    }, ps.getSession())) {
                                continue;
                            }
                        }
                        if (item instanceof Module && ((Module) item).getParent() != null) {
                            Module parrent = ConnectionManager.get(Module.class, new Criterion[]{
                                    Restrictions.eq("code", ((Module) item).getParent().getCode())
                            }, ps.getSession());

                            if (parrent != null) {
                                ((Module) item).setParent(parrent);
                            }
                        }
                    }

                    if (item instanceof LandChargesRegistry) {
                        LandChargesRegistry registry = (LandChargesRegistry) item;
                        registry.setProvinces(registry.getCities().stream().map(City::getProvince)
                                .distinct().collect(Collectors.toList()));
                        ConnectionManager.save(registry, ps.getSession());
                        AggregationLandChargesRegistry aggregationRegistry = registry.getAggregationLandChargesRegistries().get(0);
                        aggregationRegistry.setLandChargesRegistries(new LinkedList<>());
                        aggregationRegistry.getLandChargesRegistries().add(registry);
                        ConnectionManager.save(aggregationRegistry, ps.getSession());
                    } else {
                        ConnectionManager.save(item, ps.getSession());
                    }
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
        } finally {
            if (tr != null && tr.isActive() && !tr.wasRolledBack()) {
                tr.commit();
            }
            if (ps != null) {
                ps.closeSession();
            }
        }
    }

    private Method getCheckedMethod(Class<? extends Entity> entity, String checkedMethodName) {
        Method method = null;

        try {
            method = entity.getDeclaredMethod(checkedMethodName);
        } catch (NoSuchMethodException e) {
            try {
                method = entity.getSuperclass().getDeclaredMethod(checkedMethodName);
            } catch (Exception e1) {
                LogHelper.log(log, e1);
            }
        }

        return method;
    }

    private void startServices() {
        ServiceHolder.getInstance().setShowMemoryService(new ShowMemoryService());
        if (Boolean.TRUE.toString().equals(projectProperties.getProperty("showMemoryService"))) {
            System.out.println("Running ShowMemoryService...");
            ServiceHolder.getInstance().getShowMemoryService().start();
        }

        ServiceHolder.getInstance().setKeepAliveService(new KeepAliveService());
        if (Boolean.TRUE.toString().equals(projectProperties.getProperty("keepAliveService"))) {
            System.out.println("Running KeepAliveService...");
            ServiceHolder.getInstance().getKeepAliveService().start();
        }

        ServiceHolder.getInstance().setImportPropertyService(new ImportPropertyService());

        if (Boolean.TRUE.toString().equals(projectProperties.getProperty("importPropertyService"))) {
            System.out.println("Running ImportPropertyService...");
            ServiceHolder.getInstance().getImportPropertyService().start();
        }

        ServiceHolder.getInstance().setImportEstateFormalityService(new ImportEstateFormalityService());

        if (Boolean.TRUE.toString().equals(projectProperties.getProperty("importEstateFormalityService"))) {
            System.out.println("Running ImportEstateFormalityService...");
            ServiceHolder.getInstance().getImportEstateFormalityService().start();
        }

        ServiceHolder.getInstance().setImportFormalityService(new ImportFormalityService());

        if (Boolean.TRUE.toString().equals(projectProperties.getProperty("importFormalityService"))) {
            System.out.println("Running ImportFormalityService...");
            ServiceHolder.getInstance().getImportFormalityService().start();
        }
        
        ServiceHolder.getInstance().setImportReportFormalitySubjectService(
        		new ImportReportFormalitySubjectService());

        if (Boolean.TRUE.toString().equals(projectProperties.getProperty("importReportFormalitySubjectService"))) {
            System.out.println("Running ImportReportFormalitySubjectService...");
            ServiceHolder.getInstance().getImportReportFormalitySubjectService().start();
        }

        ServiceHolder.getInstance().setImportVisureRTFService(new ImportVisureRTFService());

        if (Boolean.TRUE.toString().equals(projectProperties.getProperty("importVisureRTFService"))) {
            System.out.println("Running importVisureRTFService...");
            ServiceHolder.getInstance().getImportVisureRTFService().start();
        }

        ServiceHolder.getInstance().setImportVisureDHService(new ImportVisureDHService());

        if (Boolean.TRUE.toString().equals(projectProperties.getProperty("importVisureDHService"))) {
            System.out.println("Running importVisureDHService...");
            ServiceHolder.getInstance().getImportVisureDHService().start();
        }

        ServiceHolder.getInstance().setImportRequestOLDService(new ImportRequestOLDService());

        if (Boolean.TRUE.toString().equals(projectProperties.getProperty("importRequestOLDService"))) {
            System.out.println("Running importRequestOLDService...");
            ServiceHolder.getInstance().getImportRequestOLDService().start();
        }

        ServiceHolder.getInstance().setTempRemoverService(
                new TempRemoverService(new String[]{
                        FileHelper.getTempDir(),
                        FileHelper.getCustomFolderDir("/resources/reports/temp/")
                }));

        if (Boolean.TRUE.toString().equals(projectProperties.getProperty("tempRemoverService"))) {
            System.out.println("Running TempRemoverService...");
            ServiceHolder.getInstance().getTempRemoverService().start();
        }

        ServiceHolder.getInstance().setImportXLSXService(new ImportXLSXService(false));
        ServiceHolder.getInstance().setImportXLSXUpdateFormalityImported(new ImportXLSXService(true));
        ServiceHolder.getInstance().setImportOmiXlsxService(new ImportOmiXlsxService());
        ServiceHolder.getInstance().setRemoveSubjectsService(new RemoveSubjectsService());
    }

    private void tryStopServices() {
        try {
            if (ServiceHolder.getInstance().getKeepAliveService() != null) {
                System.out.println("Stopping KeepAliveService...");
                ServiceHolder.getInstance().getKeepAliveService().stop();
                ServiceHolder.getInstance().setKeepAliveService(null);
            }

            if (ServiceHolder.getInstance().getImportPropertyService() != null) {
                System.out.println("Stopping ImportPropertyService...");
                ServiceHolder.getInstance().getImportPropertyService().stop();
                ServiceHolder.getInstance().setImportPropertyService(null);
            }

            if (ServiceHolder.getInstance().getImportEstateFormalityService() != null) {
                System.out.println("Stopping ImportEstateFormalityService...");
                ServiceHolder.getInstance().getImportEstateFormalityService().stop();
                ServiceHolder.getInstance().setImportEstateFormalityService(null);
            }

            if (ServiceHolder.getInstance().getImportFormalityService() != null) {
                System.out.println("Stopping ImportFormalityService...");
                ServiceHolder.getInstance().getImportFormalityService().stop();
                ServiceHolder.getInstance().setImportFormalityService(null);
            }
            
            if (ServiceHolder.getInstance().getImportReportFormalitySubjectService() != null) {
                System.out.println("Stopping ImportReportFormalitySubjectService...");
                ServiceHolder.getInstance().getImportReportFormalitySubjectService().stop();
                ServiceHolder.getInstance().setImportReportFormalitySubjectService(null);
            }

            if (ServiceHolder.getInstance().getImportVisureRTFService() != null) {
                System.out.println("Stopping ImportVisureRTFService...");
                ServiceHolder.getInstance().getImportVisureRTFService().stop();
                ServiceHolder.getInstance().setImportVisureRTFService(null);
            }

            if (ServiceHolder.getInstance().getImportVisureDHService() != null) {
                System.out.println("Stopping ImportVisureDHService...");
                ServiceHolder.getInstance().getImportVisureDHService().stop();
                ServiceHolder.getInstance().setImportVisureDHService(null);
            }

            if (ServiceHolder.getInstance().getImportRequestOLDService() != null) {
                System.out.println("Stopping ImportRequestOLDService...");
                ServiceHolder.getInstance().getImportRequestOLDService().stop();
                ServiceHolder.getInstance().setImportRequestOLDService(null);
            }

            if (ServiceHolder.getInstance().getTempRemoverService() != null) {
                System.out.println("Stopping TempRemoverService...");
                ServiceHolder.getInstance().getTempRemoverService().stop();
                ServiceHolder.getInstance().setTempRemoverService(null);
            }

            if (ServiceHolder.getInstance().getImportOmiXlsxService() != null) {
                System.out.println("Stopping ImportOmiXlsxService...");
                ServiceHolder.getInstance().getImportOmiXlsxService().stop();
                ServiceHolder.getInstance().setImportOmiXlsxService(null);
            }
            
            if (ServiceHolder.getInstance().getRemoveSubjectsService() != null) {
                System.out.println("Stopping RemoveSubjectsService...");
                ServiceHolder.getInstance().getRemoveSubjectsService().stop();
                ServiceHolder.getInstance().setRemoveSubjectsService(null);
            }

            if (ServiceHolder.getInstance().getShowMemoryService() != null) {
                System.out.println("Stopping ShowMemoryService...");
                ServiceHolder.getInstance().getShowMemoryService().stop();
                ServiceHolder.getInstance().setShowMemoryService(null);
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    @Override
    public void fireConnetionResufed() {
        tryStopServices();
    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        tryStopServices();
        HibernateUtil.shutdown();

        for (Object o : C3P0Registry.getPooledDataSources()) {
            try {
                ((PooledDataSource) o).close();
            } catch (Exception e) {
                // oh well, let tomcat do the complaing for us.
            }
        }

        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                log.info(String.format("deregistering jdbc driver: %s", driver));
            } catch (SQLException e) {
                log.fatal(String.format("Error deregistering driver %s", driver), e);
            }
        }
    }
}
