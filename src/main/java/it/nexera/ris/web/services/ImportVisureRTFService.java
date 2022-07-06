package it.nexera.ris.web.services;

import it.nexera.ris.common.enums.ImportSettingsType;
import it.nexera.ris.common.enums.SessionNames;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.entities.domain.VisureRTF;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.LandChargesRegistry;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ImportVisureRTFService extends ImportVisureService {

    private static final long serialVersionUID = -1602344297369834339L;

    public ImportVisureRTFService() {
        super(SessionNames.ImportVisureRTF, ImportSettingsType.VISURE_RTF);
    }


    @Override
    protected void everyElementLogic(Session session, Row row) throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        VisureRTF visureRTF = new VisureRTF();

        fillSubjectWrapper(row, visureRTF);
        fillVisureRTF(session, row, visureRTF);
        saveVisureRTF(visureRTF, session);
    }

    private void saveVisureRTF(VisureRTF visureRTF, Session session) {

        List<VisureRTF> visureRTFList = ConnectionManager.load(VisureRTF.class, getCriterions(visureRTF), session);

        if (ValidationHelper.isNullOrEmpty(visureRTFList)) {
            ConnectionManager.saveObject(visureRTF, true, session);
        }
    }

    private void fillVisureRTF(Session session, Row row, VisureRTF visureRTF) {
        Cell cell = row.getCell(0);
        if (cell != null) {
            List<LandChargesRegistry> landChargesRegistries = ConnectionManager.load(LandChargesRegistry.class, new Criterion[]{
                    Restrictions.eq("name", cell.getStringCellValue())
            }, session);

            if (!ValidationHelper.isNullOrEmpty(landChargesRegistries)) {
                visureRTF.setLandChargesRegistry(landChargesRegistries.get(0));
            } else {
                if (!cell.getStringCellValue().equals("")) {
                    LandChargesRegistry newLandChargesRegistry = createNewLandChargesRegistry(session, cell);
                    visureRTF.setLandChargesRegistry(newLandChargesRegistry);
                }
            }
        }

        cell = row.getCell(13);
        if (cell != null) {
            visureRTF.setNumDir(cell.getStringCellValue());
        }
        cell = row.getCell(14);
        if (cell != null) {
            visureRTF.setNumText(cell.getStringCellValue());
        }
        cell = row.getCell(12);
        if (cell != null) {
            try {
                if (!ValidationHelper.isNullOrEmpty(cell.getStringCellValue())) {
                    Date d = DateTimeHelper.fromString(cell.getStringCellValue(), DateTimeHelper.getMySQLDatePattern());
                    visureRTF.setUpdateDate(d);
                }
            } catch (IllegalStateException e) {
                visureRTF.setUpdateDate(cell.getDateCellValue());
            }
        }

        cell = row.getCell(9);
        if (cell != null) {
            try {
                visureRTF.setNumFormality((long) cell.getNumericCellValue());
            } catch (IllegalStateException | NumberFormatException e) {
            }
        }
        cell = row.getCell(22);
        if (cell != null) {
            try {
                visureRTF.setNdg(cell.getStringCellValue());
            } catch (IllegalStateException e) {
                visureRTF.setNdg("" + new Double(cell.getNumericCellValue()).longValue());
            }
        }
        cell = row.getCell(21);
        if (cell != null) {
            try {
                visureRTF.setReference(cell.getStringCellValue());
            } catch (IllegalStateException e) {
                visureRTF.setReference("" + new Double(cell.getNumericCellValue()).longValue());
            }
        }
    }

    private void fillSubjectWrapper(Row row, VisureRTF visureRTF) {
        Cell cell = row.getCell(6);
        if (cell != null) {
            try {
                visureRTF.setFiscalCodeVat(cell.getStringCellValue());
            } catch (IllegalStateException e) {
                visureRTF.setFiscalCodeVat("" + new Double(cell.getNumericCellValue()).longValue());
            }
        }

        cell = row.getCell(3);
        if (cell != null) {
            visureRTF.setBusinessName(cell.getStringCellValue());
        }
        cell = row.getCell(1);
        if (cell != null) {
            try {
                visureRTF.setLastName(cell.getStringCellValue());
            } catch (IllegalStateException e) {
                try {
                    visureRTF.setLastName(String.valueOf(cell.getNumericCellValue()));
                } catch (IllegalStateException e1) {
                    visureRTF.setLastName(String.valueOf(cell.getBooleanCellValue()));
                }
            }
            cell = row.getCell(2);
            if (cell != null) {
                visureRTF.setFirstName(cell.getStringCellValue());
            }
            cell = row.getCell(5);
            if (cell != null) {
                try {
                    if (!ValidationHelper.isNullOrEmpty(cell.getStringCellValue())) {
                        Date d = DateTimeHelper.fromString(cell.getStringCellValue(), DateTimeHelper.getMySQLDatePattern());
                        visureRTF.setBirthDate(d);
                    }
                } catch (IllegalStateException e) {
                    visureRTF.setBirthDate(cell.getDateCellValue());
                }
            }
        }
    }

    private Criterion[] getCriterions(VisureRTF visureRTF) {
        List<Criterion> restrictionsList = new ArrayList<>();

        if (!ValidationHelper.isNullOrEmpty(visureRTF.getFirstName())) {
            restrictionsList.add(Restrictions.eq("firstName", visureRTF.getFirstName()));
        }

        if (!ValidationHelper.isNullOrEmpty(visureRTF.getLastName())) {
            restrictionsList.add(Restrictions.eq("lastName", visureRTF.getLastName()));
        }

        if (!ValidationHelper.isNullOrEmpty(visureRTF.getBusinessName())) {
            restrictionsList.add(Restrictions.eq("businessName", visureRTF.getBusinessName()));
        }

        if (!ValidationHelper.isNullOrEmpty(visureRTF.getBirthDate())) {
            restrictionsList.add(Restrictions.eq("birthDate", visureRTF.getBirthDate()));
        }

        if (!ValidationHelper.isNullOrEmpty(visureRTF.getFiscalCodeVat())) {
            restrictionsList.add(Restrictions.eq("fiscalCodeVat", visureRTF.getFiscalCodeVat()));
        }

        if (!ValidationHelper.isNullOrEmpty(visureRTF.getLandChargesRegistry())) {
            restrictionsList.add(Restrictions.eq("landChargesRegistry", visureRTF.getLandChargesRegistry()));
        }

        if (!ValidationHelper.isNullOrEmpty(visureRTF.getNumDir())) {
            restrictionsList.add(Restrictions.eq("numDir", visureRTF.getNumDir()));
        }

        if (!ValidationHelper.isNullOrEmpty(visureRTF.getNumText())) {
            restrictionsList.add(Restrictions.eq("numText", visureRTF.getNumText()));
        }

        if (!ValidationHelper.isNullOrEmpty(visureRTF.getUpdateDate())) {
            restrictionsList.add(Restrictions.eq("updateDate", visureRTF.getUpdateDate()));
        }

        if (!ValidationHelper.isNullOrEmpty(visureRTF.getNumFormality())) {
            restrictionsList.add(Restrictions.eq("numFormality", visureRTF.getNumFormality()));
        }

        if (!ValidationHelper.isNullOrEmpty(visureRTF.getNdg())) {
            restrictionsList.add(Restrictions.eq("ndg", visureRTF.getNdg()));
        }

        if (!ValidationHelper.isNullOrEmpty(visureRTF.getReference())) {
            restrictionsList.add(Restrictions.eq("reference", visureRTF.getReference()));
        }

        return restrictionsList.toArray(new Criterion[0]);
    }
}
