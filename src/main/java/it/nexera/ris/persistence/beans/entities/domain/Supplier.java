package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "supplier")
@Getter
@Setter
public class Supplier extends IndexedEntity implements Serializable {

    private static final long serialVersionUID = -261925222083499514L;

    private String name;

    private String address;

    private String email;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    private Boolean getesi;

    private Boolean brexa;

    @Override
    public String toString() {
        return this.getName();
    }
}
