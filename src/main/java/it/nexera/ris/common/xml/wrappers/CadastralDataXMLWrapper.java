package it.nexera.ris.common.xml.wrappers;

import it.nexera.ris.common.enums.CadastralDataXMLElements;
import it.nexera.ris.common.enums.XMLElements;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.CadastralData;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import java.util.ArrayList;
import java.util.List;

public class CadastralDataXMLWrapper extends BaseXMLWrapper<CadastralData> {

    private String section;

    private String sheet;

    private String particle;

    private String sub;

    private Long propertyTypeId;

    @Override
    public CadastralData toEntity()
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        CadastralData data = new CadastralData();

        data.setCreateDate(getCreateDate());
        data.setCreateUserId(getCreateUserId());
        data.setUpdateDate(getUpdateDate());
        data.setUpdateUserId(getUpdateUserId());
        data.setVersion(getVersion());
        data.setParticle(getParticle());
        data.setSection(getSection());
        data.setSheet(getSheet());
        data.setSub(getSub());

        return data;
    }

    @Override
    public void setField(XMLElements element, String value) {
        if (!ValidationHelper.isNullOrEmpty(value)) {
            switch ((CadastralDataXMLElements) element) {
                case PARTICLE:
                    setParticle(value);
                    break;

                case SECTION:
                    setSection(value);
                    break;

                case SHEET:
                    setSheet(value);
                    break;

                case SUB:
                    setSub(value);
                    break;

                default:
                    break;
            }
        }
    }

    public List<CadastralData> entityFromDB(Session session)
            throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {

        List<Criterion> restrictions = new ArrayList<>();

        if (!ValidationHelper.isNullOrEmpty(getParticle())) {
            restrictions.add(Restrictions.eq("particle", getParticle()));
        } else {
            restrictions.add(Restrictions.eq("particle", ""));
        }

        if (!ValidationHelper.isNullOrEmpty(getSection())) {
            restrictions.add(Restrictions.eq("section", getSection()));
        } else {
            restrictions.add(Restrictions.or(
                    Restrictions.isNull("section"),
                    Restrictions.eq("section", "")));
        }

        if (!ValidationHelper.isNullOrEmpty(getSheet())) {
            restrictions.add(Restrictions.eq("sheet", getSheet()));
        } else {
            restrictions.add(Restrictions.eq("sheet", ""));
        }

        if (!ValidationHelper.isNullOrEmpty(getSub())) {
            restrictions.add(Restrictions.eq("sub", getSub()));
        } else {
            restrictions.add(Restrictions.eq("sub", ""));
        }

        return ConnectionManager.load(CadastralData.class,
                restrictions.toArray(new Criterion[0]), session);
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getSheet() {
        return sheet;
    }

    public void setSheet(String sheet) {
        this.sheet = sheet;
    }

    public String getParticle() {
        return particle;
    }

    public void setParticle(String particle) {
        this.particle = particle;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public Long getPropertyTypeId() {
        return propertyTypeId;
    }

    public void setPropertyTypeId(Long propertyTypeId) {
        this.propertyTypeId = propertyTypeId;
    }

}
