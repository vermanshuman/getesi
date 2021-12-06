package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ImportOmiXlsxService;
import it.nexera.ris.common.helpers.ImportXLSXService;
import it.nexera.ris.common.utils.ProcessMonitor;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.web.beans.BaseEntityPageBean;
import it.nexera.ris.web.services.*;
import it.nexera.ris.web.services.base.ServiceHolder;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import java.io.Serializable;
import java.net.InetAddress;

@ManagedBean(name = "monitoringBean")
@ApplicationScoped
public class MonitoringBean extends BaseEntityPageBean implements Serializable {

    private static final long serialVersionUID = -9198755620346683397L;

    private TempRemoverService tempRemoverService;

    private KeepAliveService keepAliveService;

    private ImportPropertyService importPropertyService;

    private ImportEstateFormalityService importEstateFormalityService;

    private ImportFormalityService importFormalityService;

    private ImportReportFormalitySubjectService importReportFormalitySubjectService;

    private ImportVisureRTFService importVisureRTFService;

    private ImportVisureDHService importVisureDHService;

    private ImportRequestOLDService importRequestOLDService;

    private ImportXLSXService importXLSXService;

    private ImportXLSXService importXLSXUpdateFormalityImported;

    private ImportOmiXlsxService importOmiXlsxService;

    private RemoveSubjectsService removeSubjectsService;

    private ProcessMonitor processMonitor;

    private boolean xlsImporting;

    @Override
    protected void onConstruct() {
        tempRemoverService = ServiceHolder.getInstance().getTempRemoverService();
        keepAliveService = ServiceHolder.getInstance().getKeepAliveService();
        importPropertyService = ServiceHolder.getInstance().getImportPropertyService();
        importEstateFormalityService = ServiceHolder.getInstance().getImportEstateFormalityService();
        importFormalityService = ServiceHolder.getInstance().getImportFormalityService();
        setImportReportFormalitySubjectService(ServiceHolder.getInstance().getImportReportFormalitySubjectService());
        importXLSXService = ServiceHolder.getInstance().getImportXLSXService();
        importXLSXUpdateFormalityImported = ServiceHolder.getInstance().getImportXLSXUpdateFormalityImported();
        importVisureRTFService = ServiceHolder.getInstance().getImportVisureRTFService();
        importVisureDHService = ServiceHolder.getInstance().getImportVisureDHService();
        importRequestOLDService = ServiceHolder.getInstance().getImportRequestOLDService();
        importOmiXlsxService = ServiceHolder.getInstance().getImportOmiXlsxService();
        removeSubjectsService = ServiceHolder.getInstance().getRemoveSubjectsService();

        if (importXLSXService.getProcessMonitor() == null) {
            importXLSXService.setProcessMonitor(new ProcessMonitor());
        }
        if (importXLSXUpdateFormalityImported.getProcessMonitor() == null) {
            importXLSXUpdateFormalityImported.setProcessMonitor(new ProcessMonitor());
        }
        if (importVisureRTFService.getProcessMonitor() == null) {
            importVisureRTFService.setProcessMonitor(new ProcessMonitor());
        }
        if (importVisureDHService.getProcessMonitor() == null) {
            importVisureDHService.setProcessMonitor(new ProcessMonitor());
        }
        if (importRequestOLDService.getProcessMonitor() == null) {
            importRequestOLDService.setProcessMonitor(new ProcessMonitor());
        }
        if (importOmiXlsxService.getProcessMonitor() == null) {
            importOmiXlsxService.setProcessMonitor(new ProcessMonitor());
        }
        if (importReportFormalitySubjectService.getProcessMonitor() == null) {
            setProcessMonitor(new ProcessMonitor());
            importReportFormalitySubjectService.setProcessMonitor(getProcessMonitor());
        }

        if (removeSubjectsService.getProcessMonitor() == null) {
            removeSubjectsService.setProcessMonitor(new ProcessMonitor());
        }

        try {
            updateXlsService();
        } catch (IllegalAccessException | InstantiationException | PersistenceBeanException e) {
            log.error(e.getMessage(), e);
        }
    }

    public String getIPAddress() {
        InetAddress thisIp = null;
        try {
            thisIp = InetAddress.getLocalHost();
        } catch (Exception e) {

        }
        return thisIp == null ? "" : thisIp.getHostAddress();
    }

    public void updateXlsService() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        ApplicationSettingsValue permission = DaoManager.get(ApplicationSettingsValue.class, new Criterion[]{
                Restrictions.eq("key", ApplicationSettingsKeys.PERMISSION_IMPORT_FORMALITY)});
        if (permission != null && !Boolean.valueOf(permission.getValue())) {
            setXlsImporting(true);
        } else {
            setXlsImporting(false);
        }
    }

    public TempRemoverService getTempRemoverService() {
        return tempRemoverService;
    }

    public void setTempRemoverService(TempRemoverService tempRemoverService) {
        this.tempRemoverService = tempRemoverService;
    }

    public KeepAliveService getKeepAliveService() {
        return keepAliveService;
    }

    public void setKeepAliveService(KeepAliveService keepAliveService) {
        this.keepAliveService = keepAliveService;
    }

    public ImportPropertyService getImportPropertyService() {
        return importPropertyService;
    }

    public void setImportPropertyService(ImportPropertyService importPropertyService) {
        this.importPropertyService = importPropertyService;
    }

    public ImportEstateFormalityService getImportEstateFormalityService() {
        return importEstateFormalityService;
    }

    public void setImportEstateFormalityService(ImportEstateFormalityService importEstateFormalityService) {
        this.importEstateFormalityService = importEstateFormalityService;
    }

    public ImportFormalityService getImportFormalityService() {
        return importFormalityService;
    }

    public void setImportFormalityService(ImportFormalityService importFormalityService) {
        this.importFormalityService = importFormalityService;
    }

    public ImportReportFormalitySubjectService getImportReportFormalitySubjectService() {
        return importReportFormalitySubjectService;
    }

    public void setImportReportFormalitySubjectService(ImportReportFormalitySubjectService importReportFormalitySubjectService) {
        this.importReportFormalitySubjectService = importReportFormalitySubjectService;
    }

    public boolean isXlsImporting() {
        return xlsImporting;
    }

    public void setXlsImporting(boolean xlsImporting) {
        this.xlsImporting = xlsImporting;
    }

    public ImportXLSXService getImportXLSXService() {
        return importXLSXService;
    }

    public void setImportXLSXService(ImportXLSXService importXLSXService) {
        this.importXLSXService = importXLSXService;
    }

    public ImportXLSXService getImportXLSXUpdateFormalityImported() {
        return importXLSXUpdateFormalityImported;
    }

    public void setImportXLSXUpdateFormalityImported(ImportXLSXService importXLSXUpdateFormalityImported) {
        this.importXLSXUpdateFormalityImported = importXLSXUpdateFormalityImported;
    }

    public void setProcessMonitor(ProcessMonitor processMonitor) {
        this.processMonitor = processMonitor;
    }

    public ProcessMonitor getProcessMonitor() {
        return processMonitor;
    }

    public ImportVisureRTFService getImportVisureRTFService() {
        return importVisureRTFService;
    }

    public void setImportVisureRTFService(ImportVisureRTFService importVisureRTFService) {
        this.importVisureRTFService = importVisureRTFService;
    }

    public ImportVisureDHService getImportVisureDHService() {
        return importVisureDHService;
    }

    public void setImportVisureDHService(ImportVisureDHService importVisureDHService) {
        this.importVisureDHService = importVisureDHService;
    }

    public ImportRequestOLDService getImportRequestOLDService() {
        return importRequestOLDService;
    }

    public void setImportRequestOLDService(ImportRequestOLDService importRequestOLDService) {
        this.importRequestOLDService = importRequestOLDService;
    }

    public ImportOmiXlsxService getImportOmiXlsxService() {
        return importOmiXlsxService;
    }

    public void setImportOmiXlsxService(ImportOmiXlsxService importOmiXlsxService) {
        this.importOmiXlsxService = importOmiXlsxService;
    }

    public RemoveSubjectsService getRemoveSubjectsService() {
        return removeSubjectsService;
    }

    public void setRemoveSubjectsService(RemoveSubjectsService removeSubjectsService) {
        this.removeSubjectsService = removeSubjectsService;
    }
}
