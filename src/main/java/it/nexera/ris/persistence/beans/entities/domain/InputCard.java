package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "input_card")
public class InputCard extends IndexedEntity {

    private static final long serialVersionUID = -1766339742931163732L;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "inputCard")
    private List<InputCardManageField> fields;

    @OneToMany(mappedBy = "inputCard")
    private List<DataGroupInputCard> dataGroupInputCardList;
    
    @Column(name = "is_deleted")
    private Boolean isDeleted;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<InputCardManageField> getFields() {
        return fields;
    }

    public void setFields(List<InputCardManageField> fields) {
        this.fields = fields;
    }

    public List<DataGroupInputCard> getDataGroupInputCardList() {
        return dataGroupInputCardList;
    }

    public void setDataGroupInputCardList(List<DataGroupInputCard> dataGroupInputCardList) {
        this.dataGroupInputCardList = dataGroupInputCardList;
    }
    
    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
