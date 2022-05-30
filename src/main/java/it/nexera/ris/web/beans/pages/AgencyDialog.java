package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.AgencyType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.SessionHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Agency;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;
import it.nexera.ris.web.beans.EntityEditPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.primefaces.context.RequestContext;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "agencyDialogBean")
@ViewScoped
public class AgencyDialog extends EntityEditPageBean<Agency>
        implements Serializable {

    private static final long serialVersionUID = 5549229004964590271L;

    private String agencyName;

    private String numberVAT;

    private String fiscalCode;

    private String addressStreet;

    private String addressHouseNumber;

    private String addressPostalCode;

    private String phone;

    private String fax;

    private String mail;

    private String mailPEC;

    private String agencyDirector;

    private String mobileNumber;

    private Long addressProvinceId;

    private Long addressCityId;

    private boolean onlyView;

    private List<SelectItem> provinces;

    private List<SelectItem> addressCities;

    private Agency agency;

    private Boolean automaticInvoice;

    private Boolean requestInvoice;

    private Boolean monthlyInvoice;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        if (SessionHelper.get("isNewAgency") != null
                && (Boolean) SessionHelper.get("isNewAgency")) {
            setAgency(new Agency());
            SessionHelper.removeObject("isNewAgency");
        } else if (SessionHelper.get("editAgency") != null) {
            setAgency((Agency) SessionHelper.get("editAgency"));
            SessionHelper.removeObject("editAgency");
            fillFields();
        }

        if (SessionHelper.get("isOnlyViewAgency") != null
                && (Boolean) SessionHelper.get("isOnlyViewAgency")) {
            this.setOnlyView(true);
            SessionHelper.removeObject("isOnlyViewAgency");
        }

        setProvinces(ComboboxHelper.fillList(Province.class,
                Order.asc("description")));
        this.setAddressCities(
                ComboboxHelper.fillList(new ArrayList<City>(), true));

        if (!ValidationHelper.isNullOrEmpty(this.getAddressProvinceId())) {
            handleAddressProvinceChange();
        }
    }

    @Override
    public void onValidate() throws PersistenceBeanException,
            HibernateException, IllegalAccessException {
        if (this.getAgency().isNew()) {
            checkSameEntity();
        }

        if (ValidationHelper.isNullOrEmpty(this.getAgencyName())) {
            addRequiredFieldException("form:nameOfTheCompany");
        }

        if (ValidationHelper.isNullOrEmpty(this.getNumberVAT())
                && ValidationHelper.isNullOrEmpty(this.getFiscalCode())) {
            addRequiredFieldException("form:numberVAT");
        }

        if (!ValidationHelper.isNullOrEmpty(this.getFiscalCode())
                && !(this.getFiscalCode().length() == 11
                || this.getFiscalCode().length() == 16)) {
            addFieldException("form:fiscalCode", "fiscalCodeWrongFormat");
        }

        if (ValidationHelper.isNullOrEmpty(this.getMail())) {
            addRequiredFieldException("form:mail");
        } else if (!ValidationHelper.checkMailCorrectFormat(this.getMail())) {
            addFieldException("form:mail", "emailWrongFormat");
        }

        if (!ValidationHelper.isNullOrEmpty(this.getMailPEC())
                && !ValidationHelper.checkMailCorrectFormat(this.getMailPEC())) {
            addRequiredFieldException("form:mailPEC", "emailWrongFormat");
        }

        if (ValidationHelper.isNullOrEmpty(this.getPhone())) {
            addRequiredFieldException("form:phone");
        }

        if (ValidationHelper.isNullOrEmpty(this.getAddressStreet())) {
            addRequiredFieldException("form:addressStreet");
        }

        if (ValidationHelper.isNullOrEmpty(this.getAddressHouseNumber())) {
            addRequiredFieldException("form:addressHouseNumber");
        }

        if (ValidationHelper.isNullOrEmpty(this.getAddressPostalCode())) {
            addRequiredFieldException("form:addressPostalCode");
        }

        if (ValidationHelper.isNullOrEmpty(this.getAddressProvinceId())) {
            addRequiredFieldException("form:province");
        }

        if (ValidationHelper.isNullOrEmpty(this.getAddressCityId())) {
            addRequiredFieldException("form:city");
        }
    }

    private void checkSameEntity() {
        try {
            if (!ValidationHelper.isNullOrEmpty(this.getAgencyName())) {
                Agency agency = DaoManager.get(Agency.class, new Criterion[]
                        {
                                Restrictions.eq("agencyName", this.getAgencyName())
                        });

                if (agency != null) {
                    addRequiredFieldException("form:nameOfTheCompany",
                            "agencyAlreadyPresent");
                }
            }
            if (!ValidationHelper.isNullOrEmpty(this.getNumberVAT())) {
                Agency agency = DaoManager.get(Agency.class, new Criterion[]
                        {
                                Restrictions.eq("numberVAT", this.getNumberVAT())
                        });

                if (agency != null) {
                    addRequiredFieldException("form:numberVAT",
                            "agencyAlreadyPresent");
                }
            }
            if (!ValidationHelper.isNullOrEmpty(this.getFiscalCode())) {
                Agency agency = DaoManager.get(Agency.class, new Criterion[]
                        {
                                Restrictions.eq("fiscalCode", this.getFiscalCode())
                        });

                if (agency != null) {
                    addRequiredFieldException("form:fiscalCode",
                            "agencyAlreadyPresent");
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        this.getAgency().setAgencyDirector(this.getAgencyDirector());
        this.getAgency().setAgencyName(this.getAgencyName());
        this.getAgency().setAddressHouseNumber(this.getAddressHouseNumber());
        this.getAgency().setAddressPostalCode(this.getAddressPostalCode());
        this.getAgency().setAddressStreet(this.getAddressStreet());
        this.getAgency().setFax(this.getFax());
        this.getAgency().setFiscalCode(this.getFiscalCode());
        this.getAgency().setMail(this.getMail());
        this.getAgency().setMailPEC(this.getMailPEC());
        this.getAgency().setNumberVAT(this.getNumberVAT());
        this.getAgency().setPhone(this.getPhone());
        this.getAgency().setMobileNumber(this.getMobileNumber());
        this.getAgency().setAutomaticInvoice(this.getAutomaticInvoice());
        this.getAgency().setRequestInvoice(this.getRequestInvoice());
        this.getAgency().setMonthlyInvoice(this.getMonthlyInvoice());

        if (SessionHelper.get("typeAgency") != null) {
            if ((Boolean) SessionHelper.get("typeAgency")) {
                this.getAgency().setAgencyType(AgencyType.FILIAL);
            } else {
                this.getAgency().setAgencyType(AgencyType.OFFICE);
            }
            SessionHelper.removeObject("typeAgency");
        }
        if (!ValidationHelper.isNullOrEmpty(this.getAddressCityId())) {
            this.getAgency().setAddressCityId(
                    DaoManager.get(City.class, this.getAddressCityId()));
        } else {
            this.getAgency().setAddressCityId(null);
        }

        if (!ValidationHelper.isNullOrEmpty(this.getAddressProvinceId())) {
            this.getAgency().setAddressProvinceId(DaoManager.get(Province.class,
                    this.getAddressProvinceId()));
        } else {
            this.getAgency().setAddressProvinceId(null);
        }

        closeDialog();
    }

    public void editAgency() {
        this.setOnlyView(false);
    }

    public void closeDialog() {
        RequestContext.getCurrentInstance().closeDialog(this.getAgency());
    }

    public void notSaveAgency() {
        RequestContext.getCurrentInstance().closeDialog(null);
    }

    public void handleAddressProvinceChange() throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        this.setAddressCities(ComboboxHelper.fillList(City.class,
                Order.asc("description"),
                new Criterion[]{
                        Restrictions.eq("province.id", this.getAddressProvinceId()),
                        Restrictions.eq("external", Boolean.TRUE)
                }
                ));
    }

    private void fillFields() {
        this.setAgencyDirector(this.getAgency().getAgencyDirector());
        this.setAgencyName(this.getAgency().getAgencyName());
        this.setNumberVAT(this.getAgency().getNumberVAT());
        this.setFiscalCode(this.getAgency().getFiscalCode());
        this.setAddressStreet(this.getAgency().getAddressStreet());
        this.setAddressHouseNumber(this.getAgency().getAddressHouseNumber());
        this.setAddressPostalCode(this.getAgency().getAddressPostalCode());
        this.setPhone(this.getAgency().getPhone());
        this.setFax(this.getAgency().getFax());
        this.setMail(this.getAgency().getMail());
        this.setMailPEC(this.getAgency().getMailPEC());
        this.setMobileNumber(this.getAgency().getMobileNumber());

        this.setAddressProvinceId(
                this.getAgency().getAddressProvinceId() != null
                        ? this.getAgency().getAddressProvinceId().getId()
                        : null);
        this.setAddressCityId(this.getAgency().getAddressCityId() != null
                ? this.getAgency().getAddressCityId().getId() : null);
    }

    public String getCityDescription() {
        return this.loadDescriptionById(City.class, this.getAddressCityId());
    }

    public String getProvinceDescription() {
        return this.loadDescriptionById(Province.class,
                this.getAddressProvinceId());
    }

    public boolean isFilial() {
        return getEntity().getAgencyType() == AgencyType.FILIAL;
    }

    public String getAgencyName() {
        return agencyName;
    }

    public void setAgencyName(String agencyName) {
        this.agencyName = agencyName;
    }

    public String getNumberVAT() {
        return numberVAT;
    }

    public void setNumberVAT(String numberVAT) {
        this.numberVAT = numberVAT;
    }

    public String getFiscalCode() {
        return fiscalCode;
    }

    public void setFiscalCode(String fiscalCode) {
        this.fiscalCode = fiscalCode;
    }

    public String getAddressStreet() {
        return addressStreet;
    }

    public void setAddressStreet(String addressStreet) {
        this.addressStreet = addressStreet;
    }

    public String getAddressHouseNumber() {
        return addressHouseNumber;
    }

    public void setAddressHouseNumber(String addressHouseNumber) {
        this.addressHouseNumber = addressHouseNumber;
    }

    public String getAddressPostalCode() {
        return addressPostalCode;
    }

    public void setAddressPostalCode(String addressPostalCode) {
        this.addressPostalCode = addressPostalCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getMailPEC() {
        return mailPEC;
    }

    public void setMailPEC(String mailPEC) {
        this.mailPEC = mailPEC;
    }

    public String getAgencyDirector() {
        return agencyDirector;
    }

    public void setAgencyDirector(String agencyDirector) {
        this.agencyDirector = agencyDirector;
    }

    public Long getAddressProvinceId() {
        return addressProvinceId;
    }

    public void setAddressProvinceId(Long addressProvinceId) {
        this.addressProvinceId = addressProvinceId;
    }

    public Long getAddressCityId() {
        return addressCityId;
    }

    public void setAddressCityId(Long addressCityId) {
        this.addressCityId = addressCityId;
    }

    public boolean isOnlyView() {
        return onlyView;
    }

    public void setOnlyView(boolean onlyView) {
        this.onlyView = onlyView;
    }

    public List<SelectItem> getProvinces() {
        return provinces;
    }

    public void setProvinces(List<SelectItem> provinces) {
        this.provinces = provinces;
    }

    public List<SelectItem> getAddressCities() {
        return addressCities;
    }

    public void setAddressCities(List<SelectItem> addressCities) {
        this.addressCities = addressCities;
    }

    public Agency getAgency() {
        return agency;
    }

    public void setAgency(Agency agency) {
        this.agency = agency;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public Boolean getAutomaticInvoice() {
        return automaticInvoice;
    }

    public void setAutomaticInvoice(Boolean automaticInvoice) {
        this.automaticInvoice = automaticInvoice;
    }

    public Boolean getRequestInvoice() {
        return requestInvoice;
    }

    public void setRequestInvoice(Boolean requestInvoice) {
        this.requestInvoice = requestInvoice;
    }

    public Boolean getMonthlyInvoice() {
        return monthlyInvoice;
    }

    public void setMonthlyInvoice(Boolean monthlyInvoice) {
        this.monthlyInvoice = monthlyInvoice;
    }
}
