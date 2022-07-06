package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.RoleTypes;
import it.nexera.ris.common.enums.SpecialPermissionTypes;
import it.nexera.ris.common.enums.SpecialPermissionTypes.PermissionType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.Module;
import it.nexera.ris.persistence.beans.entities.domain.Permission;
import it.nexera.ris.web.beans.EntityEditPageBean;
import it.nexera.ris.web.beans.wrappers.logic.UserWrapper;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@ManagedBean(name = "roleEditBean")
@ViewScoped
public class RoleEditBean extends EntityEditPageBean<Role> implements
        Serializable {
    private static final long serialVersionUID = -5674681405879724423L;

    private List<SelectItem> types;

    private List<Permission> permissionsFiltered;

    private List<Permission> permissions;

    private List<SpecialPermission> specPermissionsAction;

    private List<SpecialPermission> specPermissionsPageAvailability;

    private SpecialPermission selectedSpecPermition;

    private boolean deselectAllPermissions;

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityEditPageBean#pageLoad()
     */
    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        if (!this.isPostback()) {
            setTypes(ComboboxHelper.fillList(RoleTypes.values()));
            List<Permission> tempList = new ArrayList<Permission>();

            List<Module> modules = DaoManager.load(Module.class,
                    new Criterion[]{
                            Restrictions.eq("isTemplate", "true")
                    });

            if (!ValidationHelper.isNullOrEmpty(modules)) {
                for (Module module : modules) {
                    Permission p = new Permission();
                    p.setCanCreate(Boolean.TRUE);
                    p.setCanDelete(Boolean.TRUE);
                    p.setCanEdit(Boolean.TRUE);
                    p.setCanView(Boolean.TRUE);
                    p.setCanList(Boolean.TRUE);
                    p.setCanListCreatedByUser(Boolean.TRUE);
                    p.setModule(module);
                    tempList.add(p);
                }
            }

            List<Permission> loadedList = new ArrayList<Permission>();

            if (!this.getEntity().isNew()) {
                loadedList = DaoManager.load(Permission.class,
                        new CriteriaAlias[]{
                                new CriteriaAlias("module", "m",
                                        JoinType.LEFT_OUTER_JOIN)
                        }, new Criterion[]{
                                Restrictions.eq("role.id", this.getEntityId()),
                                Restrictions.eq("m.isTemplate", "true")
                        });
            }

            this.setPermissions(new ArrayList<Permission>());

            for (Permission p : tempList) {
                if (p.getModule().getParent() == null
                        && p.getModule().getIsTemplate().equals("true")) {
                    Permission item = findInList(loadedList, p.getModule());
                    if (item != null) {
                        p.setCanCreate(item.getCanCreate());
                        p.setCanDelete(item.getCanDelete());
                        p.setCanEdit(item.getCanEdit());
                        p.setCanView(item.getCanView());
                        p.setCanList(item.getCanList());
                        p.setCanListCreatedByUser(item.getCanListCreatedByUser());
                    }

                    this.getPermissions().add(p);

                    for (Permission child : tempList) {
                        if (p.getModule().equals(child.getModule().getParent())) {
                            item = findInList(loadedList, child.getModule());
                            if (item != null) {
                                processParentAsChild(p, item);
                                child.setCanCreate(item.getCanCreate());
                                child.setCanDelete(item.getCanDelete());
                                child.setCanEdit(item.getCanEdit());
                                child.setCanView(item.getCanView());
                                child.setCanList(item.getCanList());
                                child.setCanListCreatedByUser(item.getCanListCreatedByUser());
                            }

                            this.getPermissions().add(child);
                        }
                    }
                }
            }

            fillActionsAndAvailability();
        }

        fillSpecialPermissionsFromEntity();
        setDeselectAllPermissions(false);
    }

    private void fillSpecialPermissionsFromEntity() {
        if (!ValidationHelper.isNullOrEmpty(this.getEntity()
                .getSpecPermissions())) {
            List<SpecialPermissionTypes> specPermitions = new ArrayList<SpecialPermissionTypes>();

            for (SpecialPermission sp : this.getEntity().getSpecPermissions()) {
                specPermitions.add(sp.getSpecialPermissionType());
            }

            List<SpecialPermission> allPermissions = new ArrayList<SpecialPermission>();

            allPermissions.addAll(this.getSpecPermissionsAction());
            allPermissions.addAll(this.getSpecPermissionsPageAvailability());

            if (!ValidationHelper.isNullOrEmpty(allPermissions)) {
                for (SpecialPermission sp : allPermissions) {
                    if (!specPermitions.contains(sp.getSpecialPermissionType())) {
                        sp.setSelected(Boolean.FALSE);
                    } else {
                        sp.setSelected(Boolean.TRUE);
                    }
                }
            }
        }
    }

    public void selectOrDeselectAllPermissions() {
        for (Permission p : this.getPermissions()) {
            p.setCanCreate(!isDeselectAllPermissions());
            p.setCanDelete(!isDeselectAllPermissions());
            p.setCanEdit(!isDeselectAllPermissions());
            p.setCanView(!isDeselectAllPermissions());
            p.setCanList(!isDeselectAllPermissions());
            p.setCanListCreatedByUser(!isDeselectAllPermissions());
        }
    }

    private void fillActionsAndAvailability() {
        this.setSpecPermissionsAction(new ArrayList<SpecialPermission>());
        this.setSpecPermissionsPageAvailability(new ArrayList<SpecialPermission>());
        for (SpecialPermissionTypes spt : SpecialPermissionTypes.values()) {
            SpecialPermission p = new SpecialPermission();
            p.setSpecialPermissionType(spt);

            if (PermissionType.ACTION_AVAILABILITY.equals(spt
                    .getPermissionType())) {
                this.getSpecPermissionsAction().add(p);
            } else if (PermissionType.PAGE_ACCESS.equals(spt.getPermissionType())) {
                this.getSpecPermissionsPageAvailability().add(p);
            }
        }
    }

    private void processParentAsChild(Permission parent, Permission child) {
        if (Boolean.FALSE.equals(child.getCanCreate())) {
            parent.setCanCreate(Boolean.FALSE);
        }
        if (Boolean.FALSE.equals(child.getCanDelete())) {
            parent.setCanDelete(Boolean.FALSE);
        }
        if (Boolean.FALSE.equals(child.getCanEdit())) {
            parent.setCanEdit(Boolean.FALSE);
        }
        if (Boolean.FALSE.equals(child.getCanList())) {
            parent.setCanList(Boolean.FALSE);
        }
        if (Boolean.FALSE.equals(child.getCanView())) {
            parent.setCanView(Boolean.FALSE);
        }
        if (Boolean.FALSE.equals(child.getCanListCreatedByUser())) {
            parent.setCanListCreatedByUser(Boolean.FALSE);
        }
    }

    private SpecialPermission findInList(List<SpecialPermission> list,
                                         SpecialPermission specialPermission) {
        for (SpecialPermission p : list) {
            if (p.getSpecialPermissionType().equals(
                    specialPermission.getSpecialPermissionType())) {
                return p;
            }
        }

        return null;
    }

    private Permission findInList(List<Permission> list, Module mod) {
        for (Permission p : list) {
            if (p.getModule().getCode().equals(mod.getCode())) {
                return p;
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityEditPageBean#validate()
     */
    @Override
    public void onValidate() throws PersistenceBeanException {
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getType())) {
            this.addRequiredFieldException("tabs:type");
        }
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getName())) {
            this.addRequiredFieldException("tabs:name");
        } else if (!ValidationHelper.isUnique(Role.class, "name", this
                .getEntity().getName(), this.getEntityId())) {
            this.addFieldException("tabs:name", "roleNameAlreadyInUse");
        }
    }

    /* (non-Javadoc)
     * @see it.nexera.web.beans.EntityEditPageBean#save()
     */
    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        boolean wasNew = this.getEntity().isNew();

        DaoManager.save(this.getEntity());

        List<SpecialPermission> allPermissions = new ArrayList<SpecialPermission>();

        allPermissions.addAll(this.getSpecPermissionsAction());
        allPermissions.addAll(this.getSpecPermissionsPageAvailability());

        List<SpecialPermission> selectedSpecialPermitions = new ArrayList<SpecialPermission>();

        if (!ValidationHelper.isNullOrEmpty(allPermissions)) {
            for (SpecialPermission sp : allPermissions) {
                if (sp.getSelected().booleanValue()) {
                    selectedSpecialPermitions.add(sp);
                }
            }
        }

        if (wasNew) {
            for (SpecialPermission spermission : selectedSpecialPermitions) {
                List<SpecialPermission> spPermissions = DaoManager
                        .load(SpecialPermission.class);
                SpecialPermission permissionExpected = findInList(
                        spPermissions, spermission);
                if (permissionExpected == null) {
                    DaoManager.save(permissionExpected);
                }
                this.getEntity().getSpecPermissions().add(permissionExpected);
            }
        } else {
            Iterator<SpecialPermission> iterator = this.getEntity()
                    .getSpecPermissions().iterator();
            while (iterator.hasNext()) {
                SpecialPermission specialPermission = iterator.next();
                if (findInList(selectedSpecialPermitions, specialPermission) == null) {
                    removeSpecialPermition(specialPermission);
                    iterator.remove();
                }
            }

            for (SpecialPermission specialPermission : selectedSpecialPermitions) {
                SpecialPermission temp = DaoManager.get(
                        SpecialPermission.class, new Criterion[]{
                                Restrictions.eq("specialPermissionType",
                                        specialPermission
                                                .getSpecialPermissionType())
                        });

                if (temp == null) {
                    DaoManager.save(specialPermission);
                    this.getEntity().getSpecPermissions()
                            .add(specialPermission);
                } else {
                    if (!checkEntityHasPermission(temp)) {
                        this.getEntity().getSpecPermissions().add(temp);
                    }
                }
            }
        }
        if (wasNew) {
            for (Permission p : getPermissions()) {
                p.setRole(getEntity());
                DaoManager.save(p);
            }
            for (Module m : DaoManager.load(Module.class, new Criterion[]{
                    Restrictions.eq("isTemplate", "false")
            })) {
                Permission permission = new Permission();
                permission.setModule(m);
                permission.setCanCreate(Boolean.FALSE);
                permission.setCanDelete(Boolean.FALSE);
                permission.setCanEdit(Boolean.FALSE);
                permission.setCanList(Boolean.FALSE);
                permission.setCanView(Boolean.FALSE);
                permission.setCanListCreatedByUser(Boolean.FALSE);
                permission.setRole(DaoManager.get(Role.class, getEntity()
                        .getId()));
                DaoManager.save(permission);
            }
        } else {
            List<Permission> savedList = DaoManager.load(Permission.class,
                    new CriteriaAlias[]{
                            new CriteriaAlias("module", "m",
                                    JoinType.LEFT_OUTER_JOIN)
                    }, new Criterion[]{
                            Restrictions.eq("role.id", this.getEntityId()),
                            Restrictions.eq("m.isTemplate", "true")
                    });
            for (Permission p : this.getPermissions()) {
                if (p.getModule().getParent() != null) {
                    Permission item = findInList(savedList, p.getModule());
                    if (item == null) {
                        item = new Permission();
                        item.setModule(p.getModule());
                    }

                    item.setCanCreate(p.getCanCreate());
                    item.setCanDelete(p.getCanDelete());
                    item.setCanEdit(p.getCanEdit());
                    item.setCanView(p.getCanView());
                    item.setCanList(p.getCanList());
                    item.setCanListCreatedByUser(p.getCanListCreatedByUser());

                    item.setRole(getEntity());
                    DaoManager.save(item);
                }
            }
        }
    }

    private boolean checkEntityHasPermission(SpecialPermission permission) {
        for (SpecialPermission perm : this.getEntity().getSpecPermissions()) {
            if (perm.getSpecialPermissionType().equals(
                    permission.getSpecialPermissionType())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void afterSave() {
        try {
            UserHolder.getInstance().setCurrentUser(
                    UserWrapper.wrap(
                            DaoManager.get(User.class, UserHolder.getInstance()
                                    .getCurrentUser().getId()),
                            DaoManager.getSession()));
        } catch (HibernateException | InstantiationException
                | IllegalAccessException | PersistenceBeanException e) {
            LogHelper.log(log, e);
        }
        super.afterSave();
    }

    private void removeSpecialPermition(SpecialPermission specialPermission)
            throws PersistenceBeanException {
        saveSpecialPermition(specialPermission, null);
    }

    private void saveSpecialPermition(SpecialPermission specialPermission,
                                      Role role) throws PersistenceBeanException {
        if (ValidationHelper.isNullOrEmpty(specialPermission.getRoles())) {
            specialPermission.setRoles(new ArrayList<Role>());
        }
        specialPermission.getRoles().add(role);
        DaoManager.save(specialPermission);
    }

    public void setSelectAllSpecPermitionAction(Boolean selectAllSpecPermition) {
        if (!ValidationHelper.isNullOrEmpty(this.getSpecPermissionsAction())) {
            for (SpecialPermission sp : this.getSpecPermissionsAction()) {
                sp.setSelected(selectAllSpecPermition);
            }
        }
    }

    public Boolean getSelectAllSpecPermitionAction() {
        if (!ValidationHelper.isNullOrEmpty(this.getSpecPermissionsAction())) {
            for (SpecialPermission sp : this.getSpecPermissionsAction()) {
                if (!sp.getSelected().booleanValue()) {
                    return Boolean.FALSE;
                }
            }
        }
        return Boolean.TRUE;
    }

    public void setSelectAllSpecPermitionAccess(Boolean selectAllSpecPermition) {
        if (!ValidationHelper.isNullOrEmpty(this
                .getSpecPermissionsPageAvailability())) {
            for (SpecialPermission sp : this
                    .getSpecPermissionsPageAvailability()) {
                sp.setSelected(selectAllSpecPermition);
            }
        }
    }

    public Boolean getSelectAllSpecPermitionAccess() {
        if (!ValidationHelper.isNullOrEmpty(this
                .getSpecPermissionsPageAvailability())) {
            for (SpecialPermission sp : this
                    .getSpecPermissionsPageAvailability()) {
                if (!sp.getSelected().booleanValue()) {
                    return Boolean.FALSE;
                }
            }
        }
        return Boolean.TRUE;
    }

    public void selectOne() {
        List<SpecialPermission> allPermissions = new ArrayList<SpecialPermission>();

        allPermissions.addAll(this.getSpecPermissionsAction());
        allPermissions.addAll(this.getSpecPermissionsPageAvailability());

        if (!ValidationHelper.isNullOrEmpty(allPermissions)
                && this.getSelectedSpecPermition() != null
                && !ValidationHelper.isNullOrEmpty(this
                .getSelectedSpecPermition().getSpecialPermissionType())) {
            for (SpecialPermission sp : allPermissions) {
                if (!ValidationHelper.isNullOrEmpty(sp
                        .getSpecialPermissionType()))
                    if (sp.getSpecialPermissionType().equals(
                            this.getSelectedSpecPermition()
                                    .getSpecialPermissionType())) {
                        sp.setSelected(!sp.getSelected().booleanValue());
                    }
            }
        }
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }

    public List<Permission> getPermissionsFiltered() {
        return permissionsFiltered;
    }

    public void setPermissionsFiltered(List<Permission> permissionsFiltered) {
        this.permissionsFiltered = permissionsFiltered;
    }

    public List<SpecialPermission> getSpecPermissionsAction() {
        return specPermissionsAction;
    }

    public void setSpecPermissionsAction(List<SpecialPermission> specPermissions) {
        this.specPermissionsAction = specPermissions;
    }

    public SpecialPermission getSelectedSpecPermition() {
        return selectedSpecPermition;
    }

    public void setSelectedSpecPermition(SpecialPermission selectedSpecPermition) {
        this.selectedSpecPermition = selectedSpecPermition;
    }

    public List<SpecialPermission> getSpecPermissionsPageAvailability() {
        return specPermissionsPageAvailability;
    }

    public void setSpecPermissionsPageAvailability(
            List<SpecialPermission> specPermissionsPageAvailability) {
        this.specPermissionsPageAvailability = specPermissionsPageAvailability;
    }

    public List<SelectItem> getTypes() {
        return types;
    }

    public void setTypes(List<SelectItem> types) {
        this.types = types;
    }

    public boolean isDeselectAllPermissions() {
        return deselectAllPermissions;
    }

    public void setDeselectAllPermissions(boolean deselectAllPermissions) {
        this.deselectAllPermissions = deselectAllPermissions;
    }
}
