package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.enums.*;
import it.nexera.ris.common.helpers.ListHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.SessionManager;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Area;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Office;
import it.nexera.ris.web.beans.wrappers.logic.RoleWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@javax.persistence.Entity
@Table(name = "acl_user")
public class User extends IndexedEntity {

    private static final long serialVersionUID = -8713862973623645229L;

    public transient final Log log = LogFactory.getLog(User.class);

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private UserType type;

    @Column(name = "email")
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "fiscal_code")
    private String fiscalCode;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "business_name")
    private String businessName;

    @Enumerated(EnumType.STRING)
    @Column(name = "business_level")
    private UserLevel level;

    @Column(name = "user_code")
    private String userCode;

    @Column(name = "confirm_code")
    private String confirmCode;

    @Column(name = "login", nullable = false, length = 255)
    private String login;

    @Column(name = "not_deletable", columnDefinition = "NUMERIC(1) DEFAULT 0")
    private Boolean notDeletable;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "password_changed", columnDefinition = "NUMERIC(1) DEFAULT 0")
    private Boolean passwordChanged;
    
    @ManyToMany
    @JoinTable(name = "acl_user_role", joinColumns = {
            @JoinColumn(name = "user_id", table = "acl_user")
    }, inverseJoinColumns = {
            @JoinColumn(name = "role_id", table = "acl_role")
    })
    private List<Role> roles = new ArrayList<Role>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private UserStatuses status;

    @Column(name = "password_change_date")
    private Date passwordChangeDate;

    @Column(name = "photo_path")
    private String photoPath;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @Transient
    private List<Role> userRoles;

    @Column(name = "notification_state")
    private Boolean notificationState;

    @Column(name = "notification_evasion")
    private Boolean notificationEvasion;

    @Column(name = "notification_output")
    private Boolean notificationOutput;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private UserCategories category;

    @ManyToOne
    @JoinColumn(name = "area_id")
    private Area area;

    @ManyToOne
    @JoinColumn(name = "office_id")
    private Office office;


    public boolean isPhysical() {
        return getType() != null && getType() == UserType.PHYSICAL;
    }

    public String getBankName() {
        if (getLevel() == null) {
            return getBusinessName();
        } else {
            return String.format("%s (%s)", getBusinessName(), getLevel().toString());
        }
    }

    public List<Role> getUserRoles() {
        if (userRoles == null) {
            try {
                userRoles = DaoManager.load(Role.class, new CriteriaAlias[]{
                        new CriteriaAlias("users", "users", JoinType.INNER_JOIN)
                }, new Criterion[]{
                        Restrictions.eq("users.id", this.getId())
                });
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        return userRoles;
    }

    @Override
    public boolean getDeletable() {
        if (SessionManager.getInstance() != null
                && SessionManager.getInstance().getSessionBean() != null
                && UserHolder.getInstance().getCurrentUser() != null
                && UserHolder.getInstance().getCurrentUser().getId() != null
                && UserHolder.getInstance().getCurrentUser().getId()
                .equals(this.getId())) {
            return false;
        }
        return getNotDeletable() == null || !getNotDeletable();
    }

    @Transient
    public String getFullname() {
        return String.format("%s %s",
                this.getLastName() == null ? "" : this.getLastName(),
                this.getFirstName() == null ? "" : this.getFirstName());
    }

    public Boolean getNotDeletable() {
        if (getId() == null || notDeletable == null) {
            return null;
        }
        return !this.getId()
                .equals(UserHolder.getInstance().getCurrentUser().getId())
                && notDeletable;
    }

    @Transient
    public boolean isAdmin() {
        if(!Hibernate.isInitialized(this.getRoles()))
        if (!ValidationHelper.isNullOrEmpty(this.getRoles())) {
            for (Role role : this.getRoles()) {
                if (RoleTypes.ADMINISTRATOR.equals(role.getType())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Transient
    public String getRolesExport() {
        return ListHelper.toString(this.getUserRoles());
    }

    @Override
    public String toString() {
        return this.getFullname();
    }

    public UserType getType() {
        return type;
    }

    public void setType(UserType type) {
        this.type = type;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFiscalCode() {
        return fiscalCode;
    }

    public void setFiscalCode(String fiscalCode) {
        this.fiscalCode = fiscalCode;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public UserLevel getLevel() {
        return level;
    }

    public void setLevel(UserLevel level) {
        this.level = level;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getConfirmCode() {
        return confirmCode;
    }

    public void setConfirmCode(String confirmCode) {
        this.confirmCode = confirmCode;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setNotDeletable(Boolean notDeletable) {
        this.notDeletable = notDeletable;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public UserStatuses getStatus() {
        return status;
    }

    public void setStatus(UserStatuses status) {
        this.status = status;
    }

    public Date getPasswordChangeDate() {
        return passwordChangeDate;
    }

    public void setPasswordChangeDate(Date passwordChangeDate) {
        this.passwordChangeDate = passwordChangeDate;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public void setUserRoles(List<Role> userRoles) {
        this.userRoles = userRoles;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Boolean getNotificationState() {
        return notificationState;
    }

    public void setNotificationState(Boolean notificationState) {
        this.notificationState = notificationState;
    }

    public Boolean getNotificationEvasion() {
        return notificationEvasion;
    }

    public void setNotificationEvasion(Boolean notificationEvasion) {
        this.notificationEvasion = notificationEvasion;
    }

    public Boolean getNotificationOutput() {
        return notificationOutput;
    }

    public void setNotificationOutput(Boolean notificationOutput) {
        this.notificationOutput = notificationOutput;
    }

    public UserCategories getCategory() {
        return category;
    }

    public void setCategory(UserCategories category) {
        this.category = category;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public Office getOffice() {
        return office;
    }

    public void setOffice(Office office) {
        this.office = office;
    }

	public Boolean getPasswordChanged() {
		return passwordChanged;
	}

	public void setPasswordChanged(Boolean passwordChanged) {
		this.passwordChanged = passwordChanged;
	}
}
