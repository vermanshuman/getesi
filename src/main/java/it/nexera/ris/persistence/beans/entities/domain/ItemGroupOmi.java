package it.nexera.ris.persistence.beans.entities.domain;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.GroupOmi;

@Entity
@Table(name = "item_group_omi")
public class ItemGroupOmi extends IndexedEntity {
    private static final long serialVersionUID = -313949433177778123L;

    @Column(name = "position")
    private Integer position;

    @ManyToOne
    @JoinColumn(name = "group_omi_id")
    private GroupOmi groupOmi;

    @OneToMany(mappedBy = "itemGroupOmi", cascade = CascadeType.REMOVE)
    private List<CategoryItemGroupOmi> categoryItemGroupOmis;
    
    public Integer getPosition() {
        return position;
    }

    public GroupOmi getGroupOmi() {
        return groupOmi;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public void setGroupOmi(GroupOmi groupOmi) {
        this.groupOmi = groupOmi;
    }

    public List<CategoryItemGroupOmi> getCategoryItemGroupOmis() {
        return categoryItemGroupOmis;
    }

    public void setCategoryItemGroupOmis(List<CategoryItemGroupOmi> categoryItemGroupOmis) {
        this.categoryItemGroupOmis = categoryItemGroupOmis;
    }
}
