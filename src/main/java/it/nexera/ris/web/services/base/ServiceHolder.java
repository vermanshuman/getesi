package it.nexera.ris.web.services.base;

import it.nexera.ris.common.helpers.ImportOmiXlsxService;
import it.nexera.ris.common.helpers.ImportXLSXService;
import it.nexera.ris.web.services.*;

public class ServiceHolder {
    private KeepAliveService keepAliveService;

    private ImportPropertyService importPropertyService;

    private ImportEstateFormalityService importEstateFormalityService;

    private ImportFormalityService importFormalityService;

    private ImportReportFormalitySubjectService importReportFormalitySubjectService;

    private ImportVisureRTFService importVisureRTFService;

    private ImportVisureDHService importVisureDHService;

    private ImportRequestOLDService importRequestOLDService;

    private TempRemoverService tempRemoverService;

    private ImportXLSXService importXLSXService;

    private ImportXLSXService importXLSXUpdateFormalityImported;

    private ImportOmiXlsxService importOmiXlsxService;

    private RemoveSubjectsService removeSubjectsService;

    private static ServiceHolder instance;

    public static synchronized ServiceHolder getInstance() {
        if (instance == null) {
            instance = new ServiceHolder();
        }
        return instance;
    }

    private ServiceHolder() {
    }

    public KeepAliveService getKeepAliveService() {
        return keepAliveService;
    }

    public void setKeepAliveService(KeepAliveService keepAliveService) {
        this.keepAliveService = keepAliveService;
    }

    public TempRemoverService getTempRemoverService() {
        return tempRemoverService;
    }

    public void setTempRemoverService(TempRemoverService tempRemoverService) {
        this.tempRemoverService = tempRemoverService;
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

	public void setImportXLSXService(ImportXLSXService importXLSXService) {
        this.importXLSXService = importXLSXService;
    }

    public ImportXLSXService getImportXLSXService() {
        return importXLSXService;
    }

    public ImportXLSXService getImportXLSXUpdateFormalityImported() {
        return importXLSXUpdateFormalityImported;
    }

    public void setImportXLSXUpdateFormalityImported(ImportXLSXService importXLSXUpdateFormalityImported) {
        this.importXLSXUpdateFormalityImported = importXLSXUpdateFormalityImported;
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
