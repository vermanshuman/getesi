package it.nexera.ris.web.beans.wrappers.logic.editInTable;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import it.nexera.ris.common.enums.PropertyTypeEnum;
import it.nexera.ris.common.enums.RelationshipType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Property;
import it.nexera.ris.persistence.beans.entities.domain.Relationship;
import it.nexera.ris.persistence.beans.entities.domain.Subject;

public class RelationshipEditInTableWrapper {

    private Long id;

    private boolean edited;

    private String quote1;

    private String quote2;

    private PropertyTypeEnum type;

    private boolean toDelete;

    private Subject subject;

    public RelationshipEditInTableWrapper(Relationship relationship) {
        this.id = relationship.getId();
        if (!ValidationHelper.isNullOrEmpty(relationship.getQuote())) {
            String[] quote = relationship.getQuote().split("/");
            this.quote1 = quote[0];
            this.quote2 = quote[1];
        }
        this.type = PropertyTypeEnum.getByDescription(relationship.getPropertyType());
        this.subject = relationship.getSubject();
    }

    public RelationshipEditInTableWrapper(String quote1, String quote2, PropertyTypeEnum type, Subject subject) {
        this.quote1 = quote1;
        this.quote2 = quote2;
        this.type = type;
        this.subject = subject;
        this.edited = true;
    }

    public void save(Property property) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (edited) {
            Relationship relationship;
            if (getId() != null && isToDelete()) {
                relationship = DaoManager.get(Relationship.class, new Criterion[]{
                        Restrictions.eq("id", getId())});
                if(relationship != null)
                    DaoManager.remove(Relationship.class, getId(), true);
            }else if (getId() != null && !isToDelete()) {
                relationship = DaoManager.get(Relationship.class, new Criterion[]{
                                Restrictions.eq("id", getId())});
                relationship.setRelationshipTypeId(RelationshipType.MANUAL_ENTRY.getId());
                relationship.setQuote(String.format("%s/%s", getQuote1(), getQuote2()));
                relationship.setPropertyType(getType().getDescription());
                relationship.setProperty(property);
                relationship.setSubject(getSubject());
                DaoManager.save(relationship, true);
            } else if (getId() == null && !isToDelete()) {
                relationship = new Relationship();
                relationship.setRelationshipTypeId(RelationshipType.MANUAL_ENTRY.getId());
                relationship.setQuote(String.format("%s/%s", getQuote1(), getQuote2()));
                relationship.setPropertyType(getType().getDescription());
                relationship.setProperty(property);
                relationship.setSubject(getSubject());
                DaoManager.save(relationship, true);
            }
        }
    }

    public void setQuote1(String quote1) {
        this.quote1 = quote1;
        this.edited = true;
    }

    public void setQuote2(String quote2) {
        this.quote2 = quote2;
        this.edited = true;
    }

    public void setType(PropertyTypeEnum type) {
        this.type = type;
        this.edited = true;
    }

    public void setToDelete(boolean toDelete) {
        this.toDelete = toDelete;
        this.edited = true;
    }

    public Long getId() {
        return id;
    }

    public boolean isEdited() {
        return edited;
    }

    public String getQuote1() {
        return quote1;
    }

    public String getQuote2() {
        return quote2;
    }

    public PropertyTypeEnum getType() {
        return type;
    }

    public boolean isToDelete() {
        return toDelete;
    }

    public Subject getSubject() {
        return subject;
    }
}
