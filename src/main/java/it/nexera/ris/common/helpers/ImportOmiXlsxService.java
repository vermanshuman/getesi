package it.nexera.ris.common.helpers;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.helpers.create.xls.XlsxHelper;
import it.nexera.ris.common.utils.ProcessMonitor;
import it.nexera.ris.persistence.PersistenceSession;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.OmiValue;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.services.base.BaseService;
import it.nexera.ris.web.services.base.ThreadFactoryEx;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.primefaces.push.EventBus;
import org.primefaces.push.EventBusFactory;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;

@Setter
@Getter
public class ImportOmiXlsxService extends BaseService {

    private static final String COMUNE_AMM_COLUMN_IN_SECURITIES_XSLX = "Comune_amm";
    private static final String CITY_DESCRIPTION_COLUMN_IN_SECURITIES_XSLX = "Comune_descrizione";
    private static final String ZONA_COLUMN_IN_SECURITIES_XSLX = "Zona";
    private static final String COD_TIP_COLUMN_IN_SECURITIES_XSLX = "Cod_Tip";
    private static final String COMPR_MIN_COLUMN_IN_SECURITIES_XSLX = "Compr_min";
    private static final String COMPR_MAX_COLUMN_IN_SECURITIES_XSLX = "Compr_max";
    private static final String STATE_COLUMN_IN_SECURITIES_XSLX = "Stato";

    private static final int MAX_NUMBER_OF_ELEMENTS_BEFORE_COMMIT = 1000;

    private ProcessMonitor processMonitor;

    private boolean showProgressPanel;

    public ImportOmiXlsxService() {
        super("ImportOmiXlsxService");
    }

    @Override
    public void start() {
        setExecutorService(Executors.newSingleThreadExecutor(new ThreadFactoryEx(this.name)));
        setStopFlag(false);
        setShowProgressPanel(true);
        setNotWaitBeforeStop(true);
        getExecutorService().execute(this);
        socketPush();
    }

    @Override
    protected void routineFuncInternal() {
        PersistenceSession ps = null;
        String filePath = ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.OMI_SECURITIES_FILE).getValue();
        try {
            LogHelper.log(log, "file path");
            LogHelper.log(log, filePath);

            File file = new File(filePath);
            LogHelper.log(log, "process file " + file.getName());

            getProcessMonitor().setStatusStr(String.format("[%s] %s", file.getName(), "Parse excel ..."));
            socketPush();
            Sheet sheet = XlsxHelper.readSheet(file.getPath());

            getProcessMonitor().setEndValue(sheet.getLastRowNum() * 2);
            getProcessMonitor().setStatusStr("Save entities from excel...");

            Set<OmiValue> omiValues = loadEntities(sheet);
            if (!ValidationHelper.isNullOrEmpty(omiValues)) {
                ps = new PersistenceSession();
                saveEntities(ps.getSession(), omiValues, ps);
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        } finally {
            if (ps != null) {
                ps.closeSession();
            }
            getProcessMonitor().resetCounters();
            postRoutineFunc();
            stop();
        }
    }

    private Set<OmiValue> loadEntities(Sheet sheet) {
        Set<OmiValue> result = new HashSet<>();
        Iterator<Row> rowIterator = sheet.rowIterator();

        int cityColIndex = -1;
        int cityDescriptionIndex = -1;
        int zoneColIndex = -1;
        int codeColIndex = -1;
        int comprMinColIndex = -1;
        int comprMaxColIndex = -1;
        int statoColIndex = -1;
        
        Row row;
        while (rowIterator.hasNext()) {
            row = rowIterator.next();
            cityColIndex = XlsxHelper.findColumnIndexByName(row, COMUNE_AMM_COLUMN_IN_SECURITIES_XSLX);
            cityDescriptionIndex = XlsxHelper.findColumnIndexByName(row, CITY_DESCRIPTION_COLUMN_IN_SECURITIES_XSLX);
            zoneColIndex = XlsxHelper.findColumnIndexByName(row, ZONA_COLUMN_IN_SECURITIES_XSLX);
            codeColIndex = XlsxHelper.findColumnIndexByName(row, COD_TIP_COLUMN_IN_SECURITIES_XSLX);
            comprMinColIndex = XlsxHelper.findColumnIndexByName(row, COMPR_MIN_COLUMN_IN_SECURITIES_XSLX);
            comprMaxColIndex = XlsxHelper.findColumnIndexByName(row, COMPR_MAX_COLUMN_IN_SECURITIES_XSLX);
            statoColIndex = XlsxHelper.findColumnIndexByName(row, STATE_COLUMN_IN_SECURITIES_XSLX);
            if (cityColIndex != -1) {
                break;
            }
        }

        int step = sheet.getLastRowNum() / 100;
        if (step == 0) {
            step = 1;
        }
        int i = 0;

        while (rowIterator.hasNext()) {
            row = rowIterator.next();
            Cell cityCell = row.getCell(cityColIndex);
            Cell cityDescriptionCell = row.getCell(cityDescriptionIndex);
            Cell zoneCell = row.getCell(zoneColIndex);
            Cell codeCell = row.getCell(codeColIndex);
            Cell comprMinCell = row.getCell(comprMinColIndex);
            Cell comprMaxCell = row.getCell(comprMaxColIndex);
            Cell stateCell = row.getCell(statoColIndex);
            
            OmiValue omiValue = new OmiValue();
            omiValue.setCityCfis(cityCell.getStringCellValue());
            omiValue.setCategoryCode((long) codeCell.getNumericCellValue());
            omiValue.setCityDescription(cityDescriptionCell.getStringCellValue());
            omiValue.setZone(zoneCell.getStringCellValue());
            if(stateCell != null)
            	omiValue.setState(stateCell.getStringCellValue());
            if(!ValidationHelper.isNullOrEmpty(comprMinCell) && !ValidationHelper.isNullOrEmpty(comprMaxCell.getNumericCellValue())){
                omiValue.setComprMin((long) comprMinCell.getNumericCellValue());
                omiValue.setComprMax((long) comprMaxCell.getNumericCellValue());
                result.add(omiValue);
            }
            getProcessMonitor().setStartValue(++i);
            if (i % step == 0) {
                socketPush();
            }
        }
        return result;
    }

    private void saveEntities(Session session, Set<OmiValue> omiValues, PersistenceSession ps) {
        int numberOfElements = 0;
        Transaction tr = session.beginTransaction();
        int step = omiValues.size() / 100;
        if (step == 0) {
            step = 1;
        }

        int i = getProcessMonitor().getStartValue().intValue();
        try {
            for (OmiValue omiValue : omiValues) {
                getProcessMonitor().setStartValue(++i);
                if (i % step == 0) {
                    socketPush();
                }
                
                /*Long countOfPresumableEntitiesInDb = ConnectionManager.getCount(OmiValue.class, "id",
                        new Criterion[]{
                                Restrictions.eq("zone", omiValue.getZone()),
                                Restrictions.eq("cityCfis", omiValue.getCityCfis()),
                                Restrictions.eq("categoryCode", omiValue.getCategoryCode()),
                                Restrictions.eq("state", omiValue.getState())
                        }, session);

                if (ValidationHelper.isNullOrEmpty(countOfPresumableEntitiesInDb)) {
                    ConnectionManager.save(omiValue, session);*/
                
                OmiValue omiValueDb = ConnectionManager.get(OmiValue.class, 
                        new Criterion[]{
                                Restrictions.eq("zone", omiValue.getZone()),
                                Restrictions.eq("cityCfis", omiValue.getCityCfis()),
                                Restrictions.eq("categoryCode", omiValue.getCategoryCode()),
                                Restrictions.eq("state", omiValue.getState())
                        }, session);

                if (ValidationHelper.isNullOrEmpty(omiValueDb)) {
                    ConnectionManager.save(omiValue, session);
                } else {
                	omiValueDb.setManual(null);
                	omiValueDb.setComprMax(omiValue.getComprMax());
                	omiValueDb.setComprMin(omiValue.getComprMin());
                	ConnectionManager.save(omiValueDb, session);
                }
                
                if (numberOfElements++ > MAX_NUMBER_OF_ELEMENTS_BEFORE_COMMIT) {
                    if (tr != null && !tr.wasCommitted())
                        tr.commit();

                    if (ps != null)
                        ps.closeSession();

                    ps = new PersistenceSession();
                    session = ps.getSession();

                    tr = session.beginTransaction();

                    numberOfElements = 0;
                }
                //}
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
        } finally {
            if (tr != null && tr.isActive() && !tr.wasRolledBack()) {
                tr.commit();
            }
        }
    }

    @Override
    protected int getPollTimeKey() {
        return 0;
    }

    @Override
    protected void postRoutineFunc() {
        super.postRoutineFunc();
        setShowProgressPanel(false);
        socketPush();
    }

    private void socketPush() {
        EventBus eventBus = EventBusFactory.getDefault().eventBus();
        eventBus.publish("/notify", "");
    }

}
