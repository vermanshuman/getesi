package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "dic_request_type")
public class RequestType extends IndexedEntity {

    private static final long serialVersionUID = -8154092503014698523L;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "icon")
    private String icon;

    @Column(name = "is_deleted")
    private Boolean isDeleted;
    
    @Column(name = "multiselection")
    private Boolean multiselection;
    
    @ManyToOne
    @JoinColumn(name = "aggregation_land_char_reg_id")
    private AggregationLandChargesRegistry default_registry;
    
    @Transient
    private String styleClass;
    
    @Transient
    private String styleClassForSmallIcons;

    @Override
    public String toString() {
        return this.getName();
    }

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

    public Boolean getIsDeleted() {
        return isDeleted == null ? Boolean.FALSE : isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getIconStr() {
        return icon == null ? "fa-square-o" : icon;
    }

    public String getStyleClass() {
        if(!ValidationHelper.isNullOrEmpty(getIcon())) {
            if(getIcon().startsWith("fa-")) {
                styleClass = "fa " + getIcon() + " fa-3x";
            }else {
                styleClass = getIcon() + "-3x";
            }
        }
        return styleClass;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    public Boolean getMultiselection() {
        return multiselection;
    }

    public void setMultiselection(Boolean multiselection) {
        this.multiselection = multiselection;
    }

    public AggregationLandChargesRegistry getDefault_registry() {
        return default_registry;
    }

    public void setDefault_registry(AggregationLandChargesRegistry default_registry) {
        this.default_registry = default_registry;
    }

    /**
     * @return the styleClassForSmallIcons
     */
    public String getStyleClassForSmallIcons() {
         if(!ValidationHelper.isNullOrEmpty(getIcon())) {
            if(getIcon().startsWith("fa-")) {
                styleClassForSmallIcons = "fa " + getIcon() + " fa-2x";
            }else {
            styleClassForSmallIcons = getIcon();
            }
        }
        return styleClassForSmallIcons;
    }

    /**
     * @param styleClassForSmallIcons the styleClassForSmallIcons to set
     */
    public void setStyleClassForSmallIcons(String styleClassForSmallIcons) {
        this.styleClassForSmallIcons = styleClassForSmallIcons;
    }
}
