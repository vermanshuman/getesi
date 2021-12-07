package it.nexera.ris.web.services;

import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.common.enums.ImportSettingsType;
import it.nexera.ris.common.enums.SessionNames;
import it.nexera.ris.common.exceptions.TypeActNotConfigureException;
import it.nexera.ris.common.helpers.GeneralFunctionsHelper;
import it.nexera.ris.common.helpers.ImportXMLHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.web.beans.wrappers.logic.UploadDocumentWrapper;
import it.nexera.ris.web.services.base.BaseImportService;

import java.io.File;
import java.util.Date;

public class ImportEstateFormalityService extends BaseImportService{

    private static final long serialVersionUID = 4732530869919718076L;

    public ImportEstateFormalityService() {
        super(SessionNames.ImportEstateFormalityService, ImportSettingsType.ESTATE_FORMALITY);
    }

    @Override
    protected void importFunction(File file) throws Exception {
        try {
            UploadDocumentWrapper wrapper = GeneralFunctionsHelper.handleFileUpload(file.getPath(),
                    DocumentType.ESTATE_FORMALITY.getId(), file.getPath(),
                    new Date(), null, null, false, getSession());
            ImportXMLHelper.handleXMLTagsEstateFormality(file, null, wrapper.getDocument(), getSession());
        } catch (TypeActNotConfigureException e) {
            LogHelper.log(log, "Type act not found");
            LogHelper.log(log, "can not import estateFormality");
        }
    }
}
