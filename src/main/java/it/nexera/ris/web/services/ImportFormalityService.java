package it.nexera.ris.web.services;

import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.common.enums.ImportSettingsType;
import it.nexera.ris.common.enums.SessionNames;
import it.nexera.ris.common.helpers.FileHelper;
import it.nexera.ris.common.helpers.GeneralFunctionsHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.web.services.base.BaseImportService;

import java.io.File;
import java.util.Date;

public class ImportFormalityService extends BaseImportService {

    private static final long serialVersionUID = -5105074528537962031L;

    public ImportFormalityService() {
        super(SessionNames.ImportFormalityService, ImportSettingsType.FORMALITY);
    }

    @Override
    protected void importFunction(File file) throws Exception {
        if (!ValidationHelper.isNullOrEmpty(FileHelper.getFileExtension(file.getName()).replaceAll("\\.", ""))) {
            GeneralFunctionsHelper.handleFileUpload(file.getPath(), DocumentType.FORMALITY.getId(),
                    file.getName(), new Date(), null, null, true,
                    getSession());
        }
    }
}
