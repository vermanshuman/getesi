package it.nexera.ris.web.services;

import it.nexera.ris.common.enums.ImportSettingsType;
import it.nexera.ris.common.enums.LandChargesRegistryType;
import it.nexera.ris.common.enums.SessionNames;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.common.utils.ProcessMonitor;
import it.nexera.ris.persistence.PersistenceSession;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.LandChargesRegistry;
import it.nexera.ris.web.services.base.BaseImportService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.primefaces.push.EventBus;
import org.primefaces.push.EventBusFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;

public abstract class ImportVisureService extends BaseImportService {

    private static final long serialVersionUID = 1364667886781723169L;

    private ProcessMonitor processMonitor;

    private Boolean showProgressPanel;

    public ImportVisureService(SessionNames name, ImportSettingsType type) {
        super(name, type);
        setNotWaitBeforeStop(true);
    }

    @Override
    protected void importFunction(File file) {
        PersistenceSession ps = null;
        try {
            setShowProgressPanel(true);
            ps = new PersistenceSession();
            Session session = ps.getSession();

            Sheet sheet = readSheetXlsx(file.getPath());

            if (ValidationHelper.isNullOrEmpty(sheet)) {
                return;
            }

            Iterator<Row> iterator = sheet.iterator();
            if (iterator.hasNext()) {
                iterator.next();
            }
            getProcessMonitor().setEndValue(sheet.getLastRowNum());
            int i = 0;
            while (iterator.hasNext() && !stopFlag) {
                Row row = iterator.next();
                getProcessMonitor().setStartValue(i++);
                if (i % 10 == 0) {
                    socketPush();
                }
                if (row != null) {

                    everyElementLogic(session, row);
                }
            }
        } catch (Exception e) {
            log.info(log, e);
        } finally {
            if (ps != null) {
                ps.closeSession();
            }
            getProcessMonitor().resetCounters();
            postRoutineFunc();
            stop();
        }
    }

    protected abstract void everyElementLogic(Session session, Row row) throws IllegalAccessException, PersistenceBeanException, InstantiationException;

    @Override
    protected void postRoutineFunc() {
        super.postRoutineFunc();
        setShowProgressPanel(false);
        socketPush();
    }

    @Override
    protected void postRoutineFuncInternal() {
    }

    protected LandChargesRegistry createNewLandChargesRegistry(Session session, Cell cell) {
        LandChargesRegistry newLandChargesRegistry = new LandChargesRegistry();
        newLandChargesRegistry.setType(LandChargesRegistryType.CONSERVATORY);
        newLandChargesRegistry.setName(cell.getStringCellValue());
        ConnectionManager.save(newLandChargesRegistry, session);
        return newLandChargesRegistry;
    }

    protected Sheet readSheetXlsx(String filePath) {
        try {
            FileInputStream fis = new FileInputStream(filePath);
            XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(fis);
            fis.close();
            return workbook.getSheetAt(0);

        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return null;
    }

    protected void socketPush() {
        EventBus eventBus = EventBusFactory.getDefault().eventBus();
        eventBus.publish("/notify", "");
    }

    public ProcessMonitor getProcessMonitor() {
        return processMonitor;
    }

    public void setProcessMonitor(ProcessMonitor processMonitor) {
        this.processMonitor = processMonitor;
    }

    public Boolean getShowProgressPanel() {
        return showProgressPanel;
    }

    public void setShowProgressPanel(Boolean showProgressPanel) {
        this.showProgressPanel = showProgressPanel;
    }
}
