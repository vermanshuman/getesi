package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "iban")
public class Iban extends IndexedEntity implements Serializable {

    private static final long serialVersionUID = -1710213345125196694L;

    @Column(name = "description")
    private String description;

	@Column(name = "bankName")
    private String bankName;
    
    @Column(name = "address")
    private String address;

    @OneToMany(mappedBy = "iban")
    private List<Client> client;

	@Override
	public String toString() {
		return getDescription();
	}
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

    public List<Client> getClient() {
        return client;
    }

    public void setClient(List<Client> client) {
        this.client = client;
    }
}
