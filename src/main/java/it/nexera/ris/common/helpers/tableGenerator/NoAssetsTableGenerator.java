package it.nexera.ris.common.helpers.tableGenerator;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.exceptions.TypeFormalityNotConfigureException;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import org.hibernate.HibernateException;

public class NoAssetsTableGenerator extends TagTableGenerator {

    public NoAssetsTableGenerator(Request request) {
        super(request);
    }

    @Override
    void addBeginning() {
        getJoiner().add(getCell("", BORDER) +
                getCell("", BORDER) +
                getCell("", BORDER) +
                (!ValidationHelper.isNullOrEmpty(getRequest().getSubject()) && getRequest().getSubject().getTypeIsPhysicalPerson() ?
                        getCell(ResourcesHelper.getString("noAssetsTableSubjectHeader"), COLUMN_PADDING + BORDER) :
                        getCell(ResourcesHelper.getString("noAssetsTableSubjectHeaderLegal"), COLUMN_PADDING + BORDER)
                ));
    }

    @Override
    void fillTagTableList() {
        addEmptyRow(7);
    }

    @Override
    void addBeforeEstateFormality() throws HibernateException {

    }
}
