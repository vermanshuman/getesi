package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.enums.AgencyType;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "agency")
public class Agency extends IndexedEntity implements Serializable {

    private static final long serialVersionUID = -1710213345125196694L;

    @Column(name = "agency_name")
    private String agencyName;

    @Column(name = "number_vat")
    private String numberVAT;

    @Column(name = "fiscal_code")
    private String fiscalCode;

    @Column(name = "address_street")
    private String addressStreet;

    @Column(name = "address_house_number")
    private String addressHouseNumber;

    @Column(name = "address_postal_code")
    private String addressPostalCode;

    @Column(name = "phone")
    private String phone;

    @Column(name = "fax")
    private String fax;

    @Column(name = "mail")
    private String mail;

    @Column(name = "mail_pec")
    private String mailPEC;

    @Column(name = "agency_director")
    private String agencyDirector;

    @Column(name = "mobile_number", length = 50)
    private String mobileNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "agency_type")
    private AgencyType agencyType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_province_id")
    private Province addressProvinceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_city_id")
    private City addressCityId;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(name = "automatic_invoice")
    private Boolean automaticInvoice;

    @Column(name = "request_invoice")
    private Boolean requestInvoice;

    @Column(name = "monthly_invoice")
    private Boolean monthlyInvoice;

    @Transient
    private int tempId;

    @Override
    public String toString() {
        return agencyName;
    }

    public String getFullAddress() {
        return String.format("%s %s %s", this.getAddressStreet(),
                this.getAddressHouseNumber(),
                this.getAddressCityId().getDescription());
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

    public Province getAddressProvinceId() {
        return addressProvinceId;
    }

    public void setAddressProvinceId(Province addressProvinceId) {
        this.addressProvinceId = addressProvinceId;
    }

    public City getAddressCityId() {
        return addressCityId;
    }

    public void setAddressCityId(City addressCityId) {
        this.addressCityId = addressCityId;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public int getTempId() {
        return tempId;
    }

    public void setTempId(int tempId) {
        this.tempId = tempId;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public AgencyType getAgencyType() {
        return agencyType;
    }

    public void setAgencyType(AgencyType agencyType) {
        this.agencyType = agencyType;
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
