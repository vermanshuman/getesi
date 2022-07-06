package it.nexera.ris.web.services;

import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.common.enums.ImportSettingsType;
import it.nexera.ris.common.enums.SessionNames;
import it.nexera.ris.common.helpers.GeneralFunctionsHelper;
import it.nexera.ris.web.beans.wrappers.logic.UploadDocumentWrapper;
import it.nexera.ris.web.services.base.BaseImportService;

import java.io.File;
import java.util.Date;

public class ImportPropertyService extends BaseImportService {

    private static final long serialVersionUID = 1285541161707388375L;

    public ImportPropertyService() {
        super(SessionNames.ImportPropertyService, ImportSettingsType.PROPERTY);
    }

    @Override
    protected void importFunction(File file) throws Exception {
        UploadDocumentWrapper wrapper = GeneralFunctionsHelper.handleFileUpload(file.getPath(),
                DocumentType.CADASTRAL.getId(), file.getPath(), new Date(),
                null, null, false, getSession());
    }
}
