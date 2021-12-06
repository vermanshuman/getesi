package it.nexera.ris.web.services;

import it.nexera.ris.common.enums.ImportSettingsType;
import it.nexera.ris.common.enums.SessionNames;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.entities.domain.RequestOLD;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.LandChargesRegistry;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ImportRequestOLDService extends ImportVisureService {

    private static final long serialVersionUID = -4885670776679920238L;

    public ImportRequestOLDService() {
        super(SessionNames.ImportRequestOLD, ImportSettingsType.REQUEST_OLD);
    }


    @Override
    protected void everyElementLogic(Session session, Row row) {
        RequestOLD requestOLD = new RequestOLD();

        fillRequestOLD(session, row, requestOLD);
        saveRequestOLD(requestOLD, session);
    }

    private void saveRequestOLD(RequestOLD requestOLD, Session session) {

/*        List<RequestOLD> requestOLDList = ConnectionManager.load(RequestOLD.class, getCriterions(requestOLD), session);

        if (ValidationHelper.isNullOrEmpty(requestOLDList))*/
            ConnectionManager.saveObject(requestOLD, true, session);
    }

    private void fillRequestOLD(Session session, Row row, RequestOLD requestOLD) {
        Cell cell = row.getCell(24);
        if (cell != null) {
            requestOLD.setName(cell.getStringCellValue().trim().replaceAll("\\s+", " "));
        }

        cell = row.getCell(25);
        if (cell != null) {
            try {
                requestOLD.setFiscalCodeVat(cell.getStringCellValue().trim());
            } catch (IllegalStateException e) {
                requestOLD.setFiscalCodeVat("" + new Double(cell.getNumericCellValue()).longValue());
            }
        }

        cell = row.getCell(3);
        if (cell != null) {
            requestOLD.setClient(cell.getStringCellValue().trim());
        }

        cell = row.getCell(13);
        if (cell != null) {

            List<LandChargesRegistry> landChargesRegistries = ConnectionManager.load(LandChargesRegistry.class, new Criterion[]{
                    Restrictions.eq("name", cell.getStringCellValue().trim())
            }, session);

            if (!ValidationHelper.isNullOrEmpty(landChargesRegistries)) {
                requestOLD.setLandChargesRegistry(landChargesRegistries.get(0));
            }
        }

        cell = row.getCell(6);
        if (cell != null) {
            requestOLD.setType(cell.getStringCellValue().trim());
        }

        cell = row.getCell(29);
        if (cell != null) {
            try {
                if (!ValidationHelper.isNullOrEmpty(cell.getStringCellValue())) {
                    Date d = DateTimeHelper.fromString(cell.getStringCellValue(), DateTimeHelper.getMySQLDatePattern());
                    requestOLD.setEvasionDate(d);
                }
            } catch (IllegalStateException e) {
                requestOLD.setEvasionDate(cell.getDateCellValue());
            }
        }

        cell = row.getCell(1);
        if (cell != null) {
            try {
                if (!ValidationHelper.isNullOrEmpty(cell.getStringCellValue())) {
                    Date d = DateTimeHelper.fromString(cell.getStringCellValue(), DateTimeHelper.getMySQLDatePattern());
                    requestOLD.setRequestDate(d);
                }
            } catch (IllegalStateException e) {
                requestOLD.setRequestDate(cell.getDateCellValue());
            }
        }

        cell = row.getCell(39);
        if (cell != null) {
            requestOLD.setNumFormality((long) cell.getNumericCellValue());
        }
    }

    private Criterion[] getCriterions(RequestOLD requestOLD) {
        List<Criterion> restrictionsList = new ArrayList<>();

        if (!ValidationHelper.isNullOrEmpty(requestOLD.getName())) {
            restrictionsList.add(Restrictions.eq("name", requestOLD.getName()));
        }
        if (!ValidationHelper.isNullOrEmpty(requestOLD.getFiscalCodeVat())) {
            restrictionsList.add(Restrictions.eq("fiscalCodeVat", requestOLD.getFiscalCodeVat()));
        }
        if (!ValidationHelper.isNullOrEmpty(requestOLD.getClient())) {
            restrictionsList.add(Restrictions.eq("client", requestOLD.getClient()));
        }
        if (!ValidationHelper.isNullOrEmpty(requestOLD.getLandChargesRegistry())) {
            restrictionsList.add(Restrictions.eq("landChargesRegistry", requestOLD.getLandChargesRegistry()));
        }
        if (!ValidationHelper.isNullOrEmpty(requestOLD.getType())) {
            restrictionsList.add(Restrictions.eq("type", requestOLD.getType()));
        }
        if (!ValidationHelper.isNullOrEmpty(requestOLD.getEvasionDate())) {
            restrictionsList.add(Restrictions.eq("evasionDate", requestOLD.getEvasionDate()));
        }
        if (!ValidationHelper.isNullOrEmpty(requestOLD.getRequestDate())) {
            restrictionsList.add(Restrictions.eq("requestDate", requestOLD.getRequestDate()));
        }
        if (!ValidationHelper.isNullOrEmpty(requestOLD.getNumFormality())) {
            restrictionsList.add(Restrictions.eq("numFormality", requestOLD.getNumFormality()));
        }

        return restrictionsList.toArray(new Criterion[0]);
    }
}
