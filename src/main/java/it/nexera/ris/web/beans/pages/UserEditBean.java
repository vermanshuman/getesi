package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.*;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.common.security.crypto.MD5;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Client;
import it.nexera.ris.persistence.beans.entities.domain.Role;
import it.nexera.ris.persistence.beans.entities.domain.User;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Area;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Office;
import it.nexera.ris.persistence.view.ClientView;
import it.nexera.ris.web.beans.EntityEditPageBean;
import it.nexera.ris.web.beans.wrappers.logic.UserWrapper;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.primefaces.event.FileUploadEvent;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@ManagedBean(name = "userEditBean")
@ViewScoped
public class UserEditBean extends EntityEditPageBean<User>
        implements Serializable {

    private static final long serialVersionUID = -2909406163733552007L;

    private List<SelectItem> roles;

    private List<SelectItem> statuses;

    private List<SelectItem> categories;

    private List<SelectItem> areas;

    private List<SelectItem> offices;

    private List<SelectItem> types;

    private List<SelectItem> levels;

    private UserStatuses status;

    private UserCategories category;

    private Long selectedArea;

    private Long selectedOffice;

    private String pwd;

    private String confirmPwd;

    private List<String> selectedRoles;

    private List<SelectItem> sectors;

    private String document;

    private List<SelectItem> clients;

    private Long selectedClientId;

    private boolean showClientList;

    private boolean externalUser;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, IllegalAccessException {
        if (!getEntity().isNew() && !isPostback()) {
            setSelectedRoles(getEntity().getRoles().stream().map(Role::getStrId).collect(Collectors.toList()));
            setStatus(getEntity().getStatus());
            if (!ValidationHelper.isNullOrEmpty(getEntity().getClient())) {
                setSelectedClientId(getEntity().getClient().getId());
            }
            handleRoleSelect();
        }
        if (getEntity().isNew() || getEntity().getType() == null) {
            getEntity().setType(UserType.PHYSICAL);
        }

        if(!ValidationHelper.isNullOrEmpty(getEntity().getArea())) {
            setSelectedArea(getEntity().getArea().getId());
            if(!ValidationHelper.isNullOrEmpty(getEntity().getOffice())) {
                handleAreaSelect();
                setSelectedOffice(getEntity().getOffice().getId());
            }
        }
         handleCategorySelect();
        setStatuses(ComboboxHelper.fillList(UserStatuses.values()));
        setCategories(ComboboxHelper.fillList(UserCategories.values()));
        setTypes(ComboboxHelper.fillList(UserType.class, false, false));
        setLevels(ComboboxHelper.fillList(UserLevel.class));
    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        if (this.getEntity().getNotDeletable() == null) {
            this.getEntity().setNotDeletable(Boolean.FALSE);
        }

        this.getEntity().setStatus(this.getStatus());

        if(!ValidationHelper.isNullOrEmpty(getSelectedArea())) {
            Area area = DaoManager.get(Area.class, new Criterion[]{
                    Restrictions.eq("id", Long.valueOf(getSelectedArea()))
            });
            this.getEntity().setArea(area);
            if(!ValidationHelper.isNullOrEmpty(getSelectedOffice())) {
                Office office = DaoManager.get(Office.class, new Criterion[]{
                        Restrictions.eq("id", Long.valueOf(getSelectedOffice()))
                });
                this.getEntity().setOffice(office);
            }
        }

        List<Long> ids = new ArrayList<Long>();
        for (String str : this.getSelectedRoles()) {
            ids.add(Long.parseLong(str));
        }

        this.getEntity().setRoles(DaoManager.load(Role.class, new Criterion[]{
                Restrictions.in("id", ids.toArray(new Long[0]))
        }));
        if (isShowClientList()) {
            getEntity().setClient(DaoManager.get(Client.class, getSelectedClientId()));
        } else {
            getEntity().setClient(null);
        }

        if (!ValidationHelper.isNullOrEmpty(this.getPwd())) {
            this.getEntity().setPasswordChangeDate(new Date());
            this.getEntity().setPassword(MD5.encodeString(this.getPwd(), null));
            this.getContext().getExternalContext().getApplicationMap()
                    .put("reload_users", Boolean.TRUE);
        }

        if (!ValidationHelper.isNullOrEmpty(getDocument())) {
            this.getEntity().setPhotoPath(getDocument());
        }

        DaoManager.save(this.getEntity());

        if (this.getEntity().getId().equals(this.getCurrentUser().getId())) {
            UserHolder.getInstance().setCurrentUser(
                    UserWrapper.wrap(getEntity(), DaoManager.getSession()));
        }
    }

    public void saveDocument(FileUploadEvent event) {
        try {
            if (event.getFile() != null) {
                String sb = FileHelper.getDocumentSavePath() +
                        DateTimeHelper.ToFilePathString(new Date()) +
                        UserHolder.getInstance().getCurrentUser().getId() +
                        "\\";

                File filePath = new File(sb);

                String fileName = FileHelper.writeFileToFolder(event.getFile().getFileName(),
                        filePath, event.getFile().getContents());

                setDocument(fileName);
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see it.nexera.web.beans.EntityEditPageBean#validate()
     */
    @Override
    public void onValidate() throws PersistenceBeanException {
        if (ValidationHelper.isNullOrEmpty(getEntity().getCategory())) {
            addRequiredFieldException("form:category");
        }
        if (ValidationHelper.isNullOrEmpty(getStatus())) {
            addRequiredFieldException("form:status");
        }

        if (!ValidationHelper.isNullOrEmpty(getEntity().getEmail())
                && !ValidationHelper.checkMailCorrectFormat(getEntity().getEmail())) {
            addFieldException("form:email", "emailWrongFormat");
        }

        if (!ValidationHelper.isNullOrEmpty(getEntity().getEmail())
                && !ValidationHelper.isUnique(User.class, "email", getEntity().getEmail(), getEntityId())) {
            addFieldException("form:email", "emailAlreadyInUse");
        }
        if (getEntity().isPhysical()) {
            if (ValidationHelper.isNullOrEmpty(getEntity().getLastName())) {
                addRequiredFieldException("form:lastname");
            }

            if (ValidationHelper.isNullOrEmpty(getEntity().getFirstName())) {
                addRequiredFieldException("form:firstname");
            }
        } else {
            if (ValidationHelper.isNullOrEmpty(getEntity().getBusinessName())) {
                addRequiredFieldException("form:businessName");
            }
        }

        if (ValidationHelper.isNullOrEmpty(getEntity().getLogin())) {
            addRequiredFieldException("form:login");
        }

        if (!ValidationHelper.isUnique(User.class, "login", getEntity().getLogin(), getEntityId())) {
            addFieldException("form:login", "loginAlreadyInUse");
        }

        if (ValidationHelper.isNullOrEmpty(getSelectedRoles())) {
            addRequiredFieldException("form:role");
        }

        if (isShowClientList() && ValidationHelper.isNullOrEmpty(getSelectedClientId())) {
            addRequiredFieldException("form:client");
        }

        if (getEntity().isNew() && ValidationHelper.isNullOrEmpty(getPwd().trim())) {
            addRequiredFieldException("form:password");
        }

        if (getEntity().isNew() && ValidationHelper.isNullOrEmpty(getConfirmPwd().trim())) {
            addRequiredFieldException("form:c_password");
        }

        if (!ValidationHelper.isNullOrEmpty(getPwd().trim())
                && ValidationHelper.isNullOrEmpty(getConfirmPwd().trim())) {
            addFieldException("form:password", "passwordMissmatch");
            addFieldException("form:c_password", "passwordMissmatch", Boolean.FALSE);
        } else if (!ValidationHelper.isNullOrEmpty(getConfirmPwd().trim())
                && !ValidationHelper.isNullOrEmpty(getPwd().trim())
                && !getConfirmPwd().trim().equals(getPwd().trim())) {
            addFieldException("form:password", "passwordMissmatch");
            addFieldException("form:c_password", "passwordMissmatch", Boolean.FALSE);
        } else {
            if (!ValidationHelper.isNullOrEmpty(getPwd().trim())
                    && !ValidationHelper.checkFieldLength(getPwd().trim(), 8, 14)) {
                addFieldException("form:password", "passwordFormatError");
            }

            if (!ValidationHelper.isNullOrEmpty(getConfirmPwd().trim())
                    && !ValidationHelper.checkFieldLength(getConfirmPwd().trim(), 8, 14)) {
                addFieldException("form:c_password", "passwordFormatError", Boolean.FALSE);
            }
        }

        if (!ValidationHelper.isNullOrEmpty(getEntity().getFiscalCode())
                && !((getEntity().getFiscalCode().length() == 11
                && ValidationHelper.checkCorrectFormatByExpression("^[0-9]+$", getEntity().getFiscalCode()))
                || (getEntity().getFiscalCode().length() == 16
                && ValidationHelper.checkCorrectFormatByExpression(
                "^[a-zA-Z]{6}[0-9]{2}[a-zA-Z]{1}[0-9]{2}[a-zA-Z]{1}[0-9]{3}[a-zA-Z]{1}$",
                getEntity().getFiscalCode()))
                || (getEntity().getFiscalCode().length() == 16
                && ValidationHelper.checkCorrectFormatByExpression("^(STP|ENI)[0-9]{13}$",
                getEntity().getFiscalCode())))) {
            addFieldException("fiscalCode", "fiscalCodeWrongFormat");
        } else if (!ValidationHelper.isNullOrEmpty(getEntity().getFiscalCode())
                && !ValidationHelper.isUnique(User.class, "fiscalCode",
                getEntity().getFiscalCode(), getEntity().getId())) {
            addFieldException("fiscalCode", "fiscalCodeAlreadyInUse");
        }
    }

    @Override
    public void afterSave() {
        try {
            UserHolder.getInstance()
                    .setCurrentUser(UserWrapper.wrap(
                            DaoManager.get(User.class,
                                    UserHolder.getInstance().getCurrentUser()
                                            .getId()),
                            DaoManager.getSession()));
        } catch (HibernateException | InstantiationException
                | IllegalAccessException | PersistenceBeanException e) {
            LogHelper.log(log, e);
        }
        super.afterSave();
    }

    public void handleCategorySelect() throws PersistenceBeanException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(getEntity().getCategory()) && UserCategories.ESTERNO.name()
                .equals(getEntity().getCategory().name())) {
            setExternalUser(true);
            setRoles(ComboboxHelper.fillList(Role.class, Order.asc("name"), new Criterion[]{
                    Restrictions.or(
                            Restrictions.eq("type", RoleTypes.ADMINISTRATOR),
                            Restrictions.eq("type", RoleTypes.EXTERNAL)
                    )
            }, false));

            setAreas(ComboboxHelper.fillList(Area.class, Order.asc("description"), true));

        } else if(!ValidationHelper.isNullOrEmpty(getEntity().getCategory()) && UserCategories.INTERNO.name()
                .equals(getEntity().getCategory().name())) {
            setExternalUser(false);
            setRoles(ComboboxHelper.fillList(Role.class, Order.asc("name"), new Criterion[]{
                    Restrictions.or(
                            Restrictions.isNull("type"),
                            Restrictions.ne("type", RoleTypes.EXTERNAL)
                    )
            }, false));
        } else {
            setExternalUser(false);
            setRoles(new ArrayList<>(Collections.emptyList()));
        }
    }

    public void handleAreaSelect() throws PersistenceBeanException, IllegalAccessException {
        if(!ValidationHelper.isNullOrEmpty(getSelectedArea())) {
            setOffices(ComboboxHelper.fillListDictionary(Office.class, Order.asc("code"), new Criterion[]{
                    Restrictions.eq("area.id", Long.valueOf(getSelectedArea()))
            }, true));
        } else {
            setOffices(new ArrayList<>(Collections.emptyList()));
        }
    }

    public void handleRoleSelect() throws PersistenceBeanException, IllegalAccessException {
        List<Long> roles = DaoManager.loadIds(Role.class, new Criterion[]{
                Restrictions.eq("type", RoleTypes.EXTERNAL)
        });
        if (!ValidationHelper.isNullOrEmpty(getSelectedRoles()) && !ValidationHelper.isNullOrEmpty(roles)
                && roles.stream().anyMatch(l -> getSelectedRoles().stream().anyMatch(str -> l.equals(Long.parseLong(str))))) {
            setShowClientList(true);
            setExternalUser(true);
            if (ValidationHelper.isNullOrEmpty(getClients())) {
                setClients(ComboboxHelper.fillList(ClientView.class, Order.asc("name"), new Criterion[]{
                        Restrictions.or(
                                Restrictions.eq("isDeleted", Boolean.FALSE),
                                Restrictions.isNull("isDeleted"))
                }));
                if (!ValidationHelper.isNullOrEmpty(getSelectedClientId())) {
                    SelectItemHelper.addItemToListIfItIsNotInIt(getClients(), getEntity().getClient());
                }
            }
        } else {
            setShowClientList(false);
            setExternalUser(false);
        }
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getPwd() {
        return pwd;
    }

    public void setConfirmPwd(String confirmPwd) {
        this.confirmPwd = confirmPwd;
    }

    public String getConfirmPwd() {
        return confirmPwd;
    }

    public List<SelectItem> getRoles() {
        return roles;
    }

    public void setRoles(List<SelectItem> roles) {
        this.roles = roles;
    }

    public List<SelectItem> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<SelectItem> statuses) {
        this.statuses = statuses;
    }

    public List<String> getSelectedRoles() {
        return selectedRoles;
    }

    public void setSelectedRoles(List<String> selectedRoles) {
        this.selectedRoles = selectedRoles;
    }

    public List<SelectItem> getSectors() {
        return sectors;
    }

    public void setSectors(List<SelectItem> sectors) {
        this.sectors = sectors;
    }

    public UserStatuses getStatus() {
        return status;
    }

    public void setStatus(UserStatuses status) {
        this.status = status;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public List<SelectItem> getTypes() {
        return types;
    }

    public void setTypes(List<SelectItem> types) {
        this.types = types;
    }

    public List<SelectItem> getLevels() {
        return levels;
    }

    public void setLevels(List<SelectItem> levels) {
        this.levels = levels;
    }

    public List<SelectItem> getClients() {
        return clients;
    }

    public void setClients(List<SelectItem> clients) {
        this.clients = clients;
    }

    public Long getSelectedClientId() {
        return selectedClientId;
    }

    public void setSelectedClientId(Long selectedClientId) {
        this.selectedClientId = selectedClientId;
    }

    public boolean isShowClientList() {
        return showClientList;
    }

    public void setShowClientList(boolean showClientList) {
        this.showClientList = showClientList;
    }

    public boolean isExternalUser() {
        return externalUser;
    }

    public void setExternalUser(boolean externalUser) {
        this.externalUser = externalUser;
    }

    public List<SelectItem> getCategories() {
        return categories;
    }

    public void setCategories(List<SelectItem> categories) {
        this.categories = categories;
    }

    public UserCategories getCategory() {
        return category;
    }

    public void setCategory(UserCategories category) {
        this.category = category;
    }

    public List<SelectItem> getAreas() {
        return areas;
    }

    public void setAreas(List<SelectItem> areas) {
        this.areas = areas;
    }

    public List<SelectItem> getOffices() {
        return offices;
    }

    public void setOffices(List<SelectItem> offices) {
        this.offices = offices;
    }

    public Long getSelectedArea() {
        return selectedArea;
    }

    public void setSelectedArea(Long selectedArea) {
        this.selectedArea = selectedArea;
    }

    public Long getSelectedOffice() {
        return selectedOffice;
    }

    public void setSelectedOffice(Long selectedOffice) {
        this.selectedOffice = selectedOffice;
    }
}
