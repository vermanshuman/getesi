package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.common.enums.RoleTypes;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.PersistenceSession;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Permission;
import it.nexera.ris.persistence.beans.entities.domain.Role;
import it.nexera.ris.persistence.beans.entities.domain.SpecialPermission;
import it.nexera.ris.persistence.beans.entities.domain.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import javax.faces.context.FacesContext;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class UserWrapper implements Serializable {

    private static final long serialVersionUID = 7770139502939393406L;

    public transient final Log log = LogFactory
            .getLog(UserWrapper.class);

    private String email;

    private String firstName;

    private String lastName;

    private String login;

    private String password;

    private Date passwordChangeDate;

    private Long clientId;

    private Long id;

    private transient Map<String, PermissionWrapper> permissions;

    private List<RoleWrapper> roles;

    private List<SpecialPermissionWrapper> specialPermissions;

    private List<Long> diagnostics;

    private String sectorsDescriptions;

    private String photoPath;

    public UserWrapper(User u, Session session) {
        this.setEmail(u.getEmail());
        this.setFirstName(u.getFirstName());
        this.setId(u.getId());
        this.setLastName(u.getLastName());
        this.setLogin(u.getLogin());
        this.setPassword(u.getPassword());
        this.setPasswordChangeDate(u.getPasswordChangeDate());
        if (!ValidationHelper.isNullOrEmpty(u.getClient())) {
            this.setClientId(u.getClient().getId());
        }
        this.setPermissions(this.loadPermissions());
        this.setRoles(this.loadRoles(session));
        this.setSpecialPermissions(this.loadSpecialPermisions(session));
        this.setPhotoPath(u.getPhotoPath());
    }

    private Map<String, PermissionWrapper> loadPermissions() {
        Map<String, PermissionWrapper> listPermissions = new HashMap<String, PermissionWrapper>();

        List<Role> roles = null;
        List<Permission> permissions = null;
        try {
            if (FacesContext.getCurrentInstance() != null) {
                roles = DaoManager.load(Role.class, new CriteriaAlias[]
                        {
                                new CriteriaAlias("users", "u", JoinType.INNER_JOIN)
                        }, new Criterion[]
                        {
                                Restrictions.eq("u.id", this.getId())
                        });

                if (roles == null) {
                    return listPermissions;
                }

                permissions = DaoManager.load(Permission.class, new Criterion[]
                        {
                                Restrictions.in("role", roles)
                        });
            } else {
                Session session = null;
                try {
                    session = PersistenceSession.createSession();

                    roles = ConnectionManager.load(Role.class,
                            new CriteriaAlias[]
                                    {
                                            new CriteriaAlias("users", "u",
                                                    JoinType.INNER_JOIN)
                                    }, new Criterion[]
                                    {
                                            Restrictions.eq("u.id", this.getId())
                                    }, session);

                    if (roles == null) {
                        return listPermissions;
                    }

                    permissions = ConnectionManager.load(Permission.class,
                            new Criterion[]
                                    {
                                            Restrictions.in("role", roles)
                                    }, session);
                } catch (Exception e) {
                    throw e;
                } finally {
                    if (session != null) {
                        session.clear();
                        session.close();
                    }
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        try {
            if (!ValidationHelper.isNullOrEmpty(permissions)) {
                for (Permission p : permissions) {
                    PermissionWrapper per = listPermissions
                            .get(p.getModule().getCode());
                    if (per == null) {
                        per = new PermissionWrapper();
                    }

                    if (p.getCanCreate() != null
                            && p.getCanCreate().booleanValue()) {
                        per.setCanCreate(true);
                    }
                    if (p.getCanDelete() != null
                            && p.getCanDelete().booleanValue()) {
                        per.setCanDelete(true);
                    }
                    if (p.getCanEdit() != null && p.getCanEdit().booleanValue()) {
                        per.setCanEdit(true);
                    }
                    if (p.getCanView() != null && p.getCanView().booleanValue()) {
                        per.setCanView(true);
                    }
                    if (p.getCanList() != null && p.getCanList().booleanValue()) {
                        per.setCanList(true);
                    }
                    if (p.getCanListCreatedByUser() != null && p.getCanListCreatedByUser()) {
                        per.setCanListCreatedByUser(true);
                    }
                    if (p.getModule() != null) {
                        per.setIdModule(p.getModule().getId());
                        per.setParent(p.getModule().getParent() == null);
                        listPermissions.put(p.getModule().getCode(), per);
                    }
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return listPermissions;
    }

    private List<RoleWrapper> loadRoles(Session session) {
        List<RoleWrapper> rolesWrapper = new ArrayList<RoleWrapper>();

        List<Role> roles = null;
        try {
            roles = ConnectionManager.load(Role.class, new CriteriaAlias[]
                    {
                            new CriteriaAlias("users", "u", JoinType.INNER_JOIN)
                    }, new Criterion[]
                    {
                            Restrictions.eq("u.id", this.getId())
                    }, session);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        if (roles == null) {
            return rolesWrapper;
        }

        try {
            for (Role r : roles) {
                rolesWrapper.add(new RoleWrapper(r));
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return rolesWrapper;
    }

    private List<SpecialPermissionWrapper> loadSpecialPermisions(Session session) {
        List<SpecialPermissionWrapper> listPermissions = new ArrayList<SpecialPermissionWrapper>();

        List<Long> rolesIdList = null;
        try {
            rolesIdList = ConnectionManager.loadIds(Role.class, new CriteriaAlias[]
                    {
                            new CriteriaAlias("users", "u", JoinType.INNER_JOIN)
                    }, new Criterion[]
                    {
                            Restrictions.eq("u.id", this.getId())
                    }, session);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        if (rolesIdList == null) {
            return listPermissions;
        }
        LogHelper.log(log, "user ID: " + getId());
        LogHelper.log(log, rolesIdList.toString());
        LogHelper.log(log, rolesIdList.stream().map(l -> "" + l).collect(Collectors.joining(" ")));
        try {
            for (SpecialPermission p : ConnectionManager.load(SpecialPermission.class,
                    new CriteriaAlias[]
                            {
                                    new CriteriaAlias("roles", "r", JoinType.INNER_JOIN)
                            }, new Criterion[]
                            {
                                    Restrictions.in("r.id", rolesIdList)
                            }, session)) {
                listPermissions.add(new SpecialPermissionWrapper(p));
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return listPermissions;
    }

    public boolean isAdmin() {
        if (!ValidationHelper.isNullOrEmpty(this.getRoles())) {
            for (RoleWrapper role : this.getRoles()) {
                if (RoleTypes.ADMINISTRATOR.equals(role.getType())) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isExternal() {
        if (!ValidationHelper.isNullOrEmpty(this.getRoles())) {
            for (RoleWrapper role : this.getRoles()) {
                if (RoleTypes.EXTERNAL.equals(role.getType())) {
                    return true;
                }
            }
        }

        return false;
    }

    public static UserWrapper wrap(User u, Session session) {
        return new UserWrapper(u, session);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getFullname() {
        return String.format("%s %s",
                this.getLastName() == null ? "" : this.getLastName(),
                this.getFirstName() == null ? "" : this.getFirstName());
    }

    public String getFullReverseName() {
        return String.format("%s %s",
                this.getFirstName() == null ? "" : this.getFirstName(),
                this.getLastName() == null ? "" : this.getLastName());
    }

    public String getLastName() {
        return lastName;

    }

    public StreamedContent getPhoto() throws FileNotFoundException {
        if (!ValidationHelper.isNullOrEmpty(getPhotoPath())) {
            File initialFile = new File(getPhotoPath());
            InputStream stream = new FileInputStream(initialFile);

            return new DefaultStreamedContent(stream, "image/jpeg");
        } else {
            return null;
        }
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.getFullname();
    }

    public Map<String, PermissionWrapper> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, PermissionWrapper> listPermissions) {
        this.permissions = listPermissions;
    }

    public List<RoleWrapper> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleWrapper> roles) {
        this.roles = roles;
    }

    public Date getPasswordChangeDate() {
        return passwordChangeDate;
    }

    public void setPasswordChangeDate(Date passwordChangeDate) {
        this.passwordChangeDate = passwordChangeDate;
    }

    public List<SpecialPermissionWrapper> getSpecialPermissions() {
        return specialPermissions;
    }

    public void setSpecialPermissions(
            List<SpecialPermissionWrapper> specialPermissions) {
        this.specialPermissions = specialPermissions;
    }

    public List<Long> getDiagnostics() {
        return diagnostics;
    }

    public void setDiagnostics(List<Long> diagnostics) {
        this.diagnostics = diagnostics;
    }

    public String getSectorsDescriptions() {
        return sectorsDescriptions;
    }

    public void setSectorsDescriptions(String sectorsDescriptions) {
        this.sectorsDescriptions = sectorsDescriptions;
    }

    public String getDateString() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

        long smallValue = Long.parseLong(formatter.format(new Date()))
                - 15328593282712l;

        String stringValue = String.valueOf(smallValue);

        StringBuilder builder = new StringBuilder();
        for (char c : stringValue.toCharArray()) {

            switch (c) {
                case '0':
                    builder.append('a');
                    break;
                case '1':
                    builder.append('c');
                    break;
                case '2':
                    builder.append('e');
                    break;
                case '3':
                    builder.append('g');
                    break;
                case '4':
                    builder.append('i');
                    break;
                case '5':
                    builder.append('k');
                    break;
                case '6':
                    builder.append('m');
                    break;
                case '7':
                    builder.append('o');
                    break;
                case '8':
                    builder.append('q');
                    break;
                case '9':
                    builder.append('s');
                    break;
                default:
                    break;
            }
        }
        return builder.toString();
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }
}
