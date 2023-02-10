package it.nexera.ris.persistence.view;

import it.nexera.ris.common.annotations.View;
import it.nexera.ris.persistence.beans.entities.IndexedView;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Objects;

@javax.persistence.Entity
@Immutable
@Table(name = "client_view")
@View(sql = ClientView.QUERY)
public class ClientView extends IndexedView {

    private static final long serialVersionUID = -6525117422538879596L;

    public static final String CREATE_PART = "CREATE OR REPLACE VIEW client_view ";

    public static final String FROM_PART = " FROM client client ";

    public static final String FIELDS = "client.ID ID,  "
            + "client.is_deleted is_deleted, "
            + "client.CREATE_DATE CREATE_DATE, "
            + "client.CREATE_USER_ID CREATE_USER_ID, "
            + "client.UPDATE_DATE UPDATE_DATE, "
            + "client.UPDATE_USER_ID UPDATE_USER_ID, "
            + "client.number_vat number_vat, "
            + "client.fiscal_code fiscal_code, "
            + "client.address_street address_street, "
            + "client.external external, "
            + "client.visible visible, "
            + "client.manager manager, "
            + "client.fiduciary fiduciary, "
            + "client.brexa brexa ";

    public static final String SELECT_PART = "AS SELECT "
            + "client.name_of_the_company name, "
            + FIELDS
            + FROM_PART
            + "where client.type_id=2"

            + " UNION ALL "

            + "SELECT "
            + "client.name_professional name, "
            + FIELDS
            + FROM_PART
            + "where client.type_id=1";

    protected static final String QUERY = CREATE_PART + SELECT_PART;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "name")
    private String name;

    @Column(name = "number_vat")
    private String numberVAT;

    @Column(name = "fiscal_code")
    private String fiscalCode;

    @Column(name = "address_street")
    private String addressStreet;

    @Column(name = "external")
    private Boolean external;
    
    @Column(name = "manager")
    private Boolean manager;
    
    @Column(name = "fiduciary")
    private Boolean fiduciary;

    @Column(name = "brexa")
    private Boolean brexa;

    private Boolean visible;

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientView that = (ClientView) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Boolean getExternal() { return external; }

    public void setExternal(Boolean external) { this.external = external; }

	public Boolean getManager() {
		return manager;
	}

	public Boolean getFiduciary() {
		return fiduciary;
	}

	public void setManager(Boolean manager) {
		this.manager = manager;
	}

	public void setFiduciary(Boolean fiduciary) {
		this.fiduciary = fiduciary;
	}

    public Boolean getBrexa() {
        return brexa;
    }

    public void setBrexa(Boolean brexa) {
        this.brexa = brexa;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }
}
