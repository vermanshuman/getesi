package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.LandChargesRegistry;
import lombok.Data;

import org.apache.commons.io.FilenameUtils;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.persistence.*;

import java.io.File;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "visureRTF")
@Data
public class VisureRTF implements IEntity {

    private static final long serialVersionUID = 740853503733684233L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "business_name")
    private String businessName;

    @Column(name = "birth_date")
    private Date birthDate;

    @Column(name = "fiscalcode_vat")
    private String fiscalCodeVat;

    @OneToOne
    @JoinColumn(name = "id_land_registry")
    private LandChargesRegistry landChargesRegistry;

    @Column(name = "num_dir")
    private String numDir;

    @Column(name = "num_text")
    private String numText;

    @Column(name = "update_date")
    private Date updateDate;

    @Column(name = "num_formality")
    private Long numFormality;

    @Column(name = "ndg")
    private String ndg;

    @Column(name = "reference")
    private String reference;

    public String getUpdateDateString() {
        return DateTimeHelper.toString(getUpdateDate());
    }

    public String getFullName() {
        if (!ValidationHelper.isNullOrEmpty(getLastName()) && !ValidationHelper.isNullOrEmpty(getFirstName())) {
            return String.format("%s %s", getLastName(), getFirstName());
        } else if (!ValidationHelper.isNullOrEmpty(getBusinessName())) {
            return getBusinessName();
        }

        return "";
    }

    public boolean getShowToggler() throws PersistenceBeanException, IllegalAccessException {
        return DaoManager.getCount(VisureRTFUpload.class, "id", new Criterion[]{
                Restrictions.eq("visureRTF.id", getId())}) > 0;
    }

    public List<VisureRTFUpload> getUploadedVisureRTFList() throws PersistenceBeanException, IllegalAccessException {
        return DaoManager.load(VisureRTFUpload.class, new Criterion[]{
                Restrictions.eq("visureRTF.id", getId())});
    }

    @Override
    public boolean isNew() {
        return false;
    }

    @Override
    public boolean isCustomId() {
        return false;
    }

    @Override
    public boolean getDeletable() throws HibernateException, PersistenceBeanException, IllegalAccessException {
        return false;
    }

    @Override
    public boolean getEditable() throws HibernateException, PersistenceBeanException, IllegalAccessException {
        return false;
    }

	public String getNumDir() {
		return numDir;
	}

	public void setNumDir(String numDir) {
		this.numDir = numDir;
	}

	public String getNumText() {
		return numText;
	}

	public void setNumText(String numText) {
		this.numText = numText;
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	public String getFiscalCodeVat() {
		return fiscalCodeVat;
	}

	public void setFiscalCodeVat(String fiscalCodeVat) {
		this.fiscalCodeVat = fiscalCodeVat;
	}

	public LandChargesRegistry getLandChargesRegistry() {
		return landChargesRegistry;
	}

	public void setLandChargesRegistry(LandChargesRegistry landChargesRegistry) {
		this.landChargesRegistry = landChargesRegistry;
	}
}