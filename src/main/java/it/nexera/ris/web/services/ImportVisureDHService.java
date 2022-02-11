package it.nexera.ris.web.services;

import it.nexera.ris.common.enums.ImportSettingsType;
import it.nexera.ris.common.enums.SessionNames;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.entities.domain.VisureDH;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.LandChargesRegistry;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
import java.util.List;

public class ImportVisureDHService extends ImportVisureService {

    private static final long serialVersionUID = 4831006195379396678L;

    public ImportVisureDHService() {
        super(SessionNames.ImportVisureDH, ImportSettingsType.VISURE_DH);
    }


    @Override
    protected void everyElementLogic(Session session, Row row) {
        VisureDH visureDH = new VisureDH();

        fillVisureDH(session, row, visureDH);
        saveVisureDH(visureDH, session);
    }

    private void saveVisureDH(VisureDH visureDH, Session session) {

        List<VisureDH> visureDHList = ConnectionManager.load(VisureDH.class, getCriterions(visureDH), session);

        if (ValidationHelper.isNullOrEmpty(visureDHList)) {
            ConnectionManager.saveObject(visureDH, true, session);
        }
    }

    private void fillVisureDH(Session session, Row row, VisureDH visureDH) {
        Cell cell = row.getCell(0);
        if (cell != null) {
            visureDH.setName(cell.getStringCellValue().trim());
        }

        cell = row.getCell(2);
        if (cell != null) {
            try {
                visureDH.setFiscalCodeVat(cell.getStringCellValue());
            } catch (IllegalStateException e) {
                visureDH.setFiscalCodeVat("" + new Double(cell.getNumericCellValue()).longValue());
            }
        }

        cell = row.getCell(1);
        if (cell != null) {
            try {
                visureDH.setNumberPractice(Long.valueOf(cell.getStringCellValue()));
            } catch (IllegalStateException e) {
                visureDH.setNumberPractice((long) cell.getNumericCellValue());
            }
        }

        cell = row.getCell(3);
        if (cell != null) {

            List<City> cityList = ConnectionManager.load(City.class, new Criterion[]{
                    Restrictions.eq("cfis", cell.getStringCellValue()),
                    Restrictions.isNotNull("province"),
                    Restrictions.isNotNull("description"),
            }, session);

            if (!ValidationHelper.isNullOrEmpty(cityList)) {
                List<LandChargesRegistry> landChargesRegistries = ConnectionManager.load(LandChargesRegistry.class, new Criterion[]{
                        Restrictions.eq("name", cityList.get(0).getDescription())
                }, session);

                if (!ValidationHelper.isNullOrEmpty(landChargesRegistries)) {
                    visureDH.setLandChargesRegistry(landChargesRegistries.get(0));
                }
            }

        }

        cell = row.getCell(8);
        if (cell != null) {
            visureDH.setType(cell.getStringCellValue());
        }

        cell = row.getCell(20);
        if (cell != null) {
            try {
                if (!ValidationHelper.isNullOrEmpty(cell.getStringCellValue())) {
                }
            } catch (IllegalStateException e) {
                visureDH.setUpdateDate(cell.getDateCellValue());
            }
        }

        Double numFormality = 0d;

        cell = row.getCell(18);
        if (cell != null) {
            numFormality += cell.getNumericCellValue();
        }

        cell = row.getCell(19);
        if (cell != null) {
            numFormality += cell.getNumericCellValue();
        }
        visureDH.setNumFormality(numFormality.longValue());
    }

    private Criterion[] getCriterions(VisureDH visureDH) {
        List<Criterion> restrictionsList = new ArrayList<>();

        if (!ValidationHelper.isNullOrEmpty(visureDH.getName())) {
            restrictionsList.add(Restrictions.eq("name", visureDH.getName()));
        }
        if (!ValidationHelper.isNullOrEmpty(visureDH.getFiscalCodeVat())) {
            restrictionsList.add(Restrictions.eq("fiscalCodeVat", visureDH.getFiscalCodeVat()));
        }
        if (!ValidationHelper.isNullOrEmpty(visureDH.getNumberPractice())) {
            restrictionsList.add(Restrictions.eq("numberPractice", visureDH.getNumberPractice()));
        }
        if (!ValidationHelper.isNullOrEmpty(visureDH.getLandChargesRegistry())) {
            restrictionsList.add(Restrictions.eq("landChargesRegistry", visureDH.getLandChargesRegistry()));
        }
        if (!ValidationHelper.isNullOrEmpty(visureDH.getType())) {
            restrictionsList.add(Restrictions.eq("type", visureDH.getType()));
        }
        if (!ValidationHelper.isNullOrEmpty(visureDH.getUpdateDate())) {
            restrictionsList.add(Restrictions.eq("updateDate", visureDH.getUpdateDate()));
        }
        if (!ValidationHelper.isNullOrEmpty(visureDH.getNumFormality())) {
            restrictionsList.add(Restrictions.eq("numFormality", visureDH.getNumFormality()));
        }

        return restrictionsList.toArray(new Criterion[0]);
    }
}
