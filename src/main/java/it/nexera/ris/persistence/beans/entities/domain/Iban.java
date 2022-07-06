package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "iban")
@Getter
@Setter
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

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Override
    public String toString() {
        return getDescription();
    }
}
