package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.ItemGroupOmi;
import it.nexera.ris.web.beans.wrappers.logic.CategoryGroupItemWrapper;

@Entity
@Table(name = "group_omi")
public class GroupOmi extends IndexedEntity {
    private static final long serialVersionUID = 7151061027368056824L;

    @Column
    private String name;

    @Column(name = "step_value")
    private Integer stepValue;

    @Column(name = "is_deleted")
    private Boolean isDeleted;
    
    @OneToMany(mappedBy = "groupOmi", cascade = CascadeType.REMOVE)
    private List<ItemGroupOmi> itemGroupOmi;
    
    @Transient
    private List<CategoryGroupItemWrapper> categoryGroupItems;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Integer getStepValue() {
        return stepValue;
    }

    public void setStepValue(Integer stepValue) {
        this.stepValue = stepValue;
    }

    public List<CategoryGroupItemWrapper> getCategoryGroupItems() {
        return categoryGroupItems;
    }

    public void setCategoryGroupItems(List<CategoryGroupItemWrapper> categoryGroupItems) {
        this.categoryGroupItems = categoryGroupItems;
    }

    public List<ItemGroupOmi> getItemGroupOmi() {
        return itemGroupOmi;
    }

    public void setItemGroupOmi(List<ItemGroupOmi> itemGroupOmi) {
        this.itemGroupOmi = itemGroupOmi;
    }
}