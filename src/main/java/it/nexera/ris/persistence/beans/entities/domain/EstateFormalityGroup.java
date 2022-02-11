package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "estate_formality_group")
public class EstateFormalityGroup extends IndexedEntity {

    private static final long serialVersionUID = 2039792535025073735L;

    @Column(name = "conservation_date")
    private Date conservationDate;

    @OneToMany(mappedBy = "estateFormalityGroup")
    private List<EstateFormality> estateFormalityList;

    public Date getConservationDate() {
        return conservationDate;
    }

    public void setConservationDate(Date conservationDate) {
        this.conservationDate = conservationDate;
    }

    public List<EstateFormality> getEstateFormalityList() {
        return estateFormalityList;
    }

    public void setEstateFormalityList(List<EstateFormality> estateFormalityList) {
        this.estateFormalityList = estateFormalityList;
    }
}
