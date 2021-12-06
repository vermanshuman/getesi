package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.persistence.beans.entities.IEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.LandChargesRegistry;
import lombok.Data;
import org.hibernate.HibernateException;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "visureDH")
@Data
public class VisureDH implements IEntity {

    private static final long serialVersionUID = -4196841213176453969L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "fiscalcode_vat")
    private String fiscalCodeVat;

    @Column(name = "number_practice")
    private Long numberPractice;

    @OneToOne
    @JoinColumn(name = "id_land_registry")
    private LandChargesRegistry landChargesRegistry;

    @Column(name = "type")
    private String type;

    @Column(name = "update_date")
    private Date updateDate;

    @Column(name = "num_formality")
    private Long numFormality;

    public String getUpdateDateString() {
        return DateTimeHelper.toString(getUpdateDate());
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
}