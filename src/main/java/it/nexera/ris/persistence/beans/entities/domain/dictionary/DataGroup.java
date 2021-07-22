package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.DataGroupInputCard;

@Entity
@Table(name = "data_group")
public class DataGroup extends IndexedEntity {

	private static final long serialVersionUID = 1408720145673891487L;

	@Column
    private String name;

    @Column
    private String description;

    @OneToMany(mappedBy = "dataGroup")
    private List<DataGroupInputCard> inputCardList;
    
    @Column(name = "is_deleted")
    private Boolean isDeleted;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<DataGroupInputCard> getInputCardList() {
        return inputCardList;
    }

    public void setInputCardList(List<DataGroupInputCard> inputCardList) {
        this.inputCardList = inputCardList;
    }
    
    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    @Override
    public String toString() {
        return getName();
    }
}
