package it.nexera.ris.common.xml.wrappers;

import it.nexera.ris.common.enums.EstateLocationsXMLElements;
import it.nexera.ris.common.enums.XMLElements;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.domain.EstateLocation;
import org.hibernate.HibernateException;

import javax.persistence.Column;

public class EstateLocationsXMLWrapper extends BaseXMLWrapper<EstateLocation> {

    @Column(name = "description")
    private String description;

    @Column(name = "common_code")
    private String commonCode;

    @Override
    public EstateLocation toEntity()
            throws InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        EstateLocation estateLocations = new EstateLocation();

        estateLocations.setId(getId());
        estateLocations.setCreateUserId(getCreateUserId());
        estateLocations.setUpdateUserId(getUpdateUserId());
        estateLocations.setCreateDate(getCreateDate());
        estateLocations.setUpdateDate(getUpdateDate());
        estateLocations.setVersion(getVersion());
        estateLocations.setDescription(getDescription());
        estateLocations.setCommonCode(getCommonCode());

        return estateLocations;
    }

    @Override
    public void setField(XMLElements element, String value) {
        if (!ValidationHelper.isNullOrEmpty(value)) {
            switch ((EstateLocationsXMLElements)element) {
                case COMMON_CODE:
                    setCommonCode(value);
                    break;

                case DESCRIPTION:
                    setDescription(value);
                    break;

                default:
                    break;

            }
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCommonCode() {
        return commonCode;
    }

    public void setCommonCode(String commonCode) {
        this.commonCode = commonCode;
    }
}
