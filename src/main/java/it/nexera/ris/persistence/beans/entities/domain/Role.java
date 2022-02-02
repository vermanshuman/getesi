package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.enums.RoleTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@javax.persistence.Entity
@Table(name = "acl_role")
public class Role extends IndexedEntity {
    private static final long serialVersionUID = -4263868053757011096L;

    @Column
    @Enumerated(EnumType.STRING)
    private RoleTypes type;

    @Column(name = "name")
    private String name;

    @ManyToMany
    @JoinTable(name = "acl_role_spec_permission", joinColumns = {
            @JoinColumn(name = "role_id", table = "acl_role")
    }, inverseJoinColumns = {
            @JoinColumn(name = "permission_id", table = "acl_spec_permission")
    })
    private List<SpecialPermission> specPermissions = new ArrayList<SpecialPermission>();

    @ManyToMany(mappedBy = "roles")
    private List<User> users;

    @OneToMany(mappedBy = "role", cascade = CascadeType.REMOVE)
    private List<Permission> permissions;

    /* (non-Javadoc)
     * @see it.nexera.persistence.beans.entities.Entity#getIsCanDelete()
     */
    @Override
    public boolean getDeletable() throws HibernateException,
            PersistenceBeanException, IllegalAccessException {
        return DaoManager.load(User.class,
                new CriteriaAlias("roles", "r", JoinType.LEFT_OUTER_JOIN),
                new Criterion[]{
                        Restrictions.eq("r.id", this.getId())
                }).isEmpty();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public RoleTypes getType() {
        return type;
    }

    public void setType(RoleTypes type) {
        this.type = type;
    }

    public List<SpecialPermission> getSpecPermissions() {
        return specPermissions;
    }

    public void setSpecPermissions(List<SpecialPermission> specPermissions) {
        this.specPermissions = specPermissions;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }

}
