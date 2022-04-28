package it.nexera.ris.web.beans.pages;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.web.common.EntityLazyListModel;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.data.PageEvent;
import org.primefaces.model.LazyDataModel;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.enums.MailEditType;
import it.nexera.ris.common.enums.MailManagerStatuses;
import it.nexera.ris.common.enums.MailManagerTypes;
import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.enums.SpecialPermissionTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.HttpSessionHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.MailManagerHelper;
import it.nexera.ris.common.helpers.PermissionsHelper;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.common.helpers.SessionHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.readonly.UserShort;
import it.nexera.ris.persistence.beans.entities.domain.readonly.WLGInboxShort;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import it.nexera.ris.web.beans.base.AccessBean;
import it.nexera.ris.web.beans.wrappers.logic.ClientEmailWrapper;
import it.nexera.ris.web.beans.wrappers.logic.MailManagerTypeWrapper;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.SortOrder;

@ManagedBean(name = "mailManagerListBean")
@ViewScoped
public class MailManagerListBean extends EntityLazyListPageBean<WLGInboxShort> implements Serializable {

    private static final long serialVersionUID = 6636147075028479690L;

    private static final String KEY_DATE_FROM = "KEY_DATE_FROM_SESSION_KEY_NOT_COPY";

    private static final String KEY_DATE_TO = "KEY_DATE_TO_SESSION_KEY_NOT_COPY";

    private static final String KEY_MAIL_TYPE = "KEY_MAIL_TYPE_SESSION_KEY_NOT_COPY";

    private static final String KEY_KEY_WORD = "KEY_KEY_WORD_SESSION_KEY_NOT_COPY";

    private static final String KEY_EMAIL = "KEY_EMAIL_SESSION_KEY_NOT_COPY";

    private static final String KEY_SELECTED_TAB = "KEY_SELECTED_TAB_SESSION_KEY_NOT_COPY";

    private static final String KEY_SORT_ORDER = "KEY_SORT_ORDER_SESSION_KEY_NOT_COPY";

    private static final String KEY_SORT_COLUMN = "KEY_SORT_COLUMN_SESSION_KEY_NOT_COPY";

    private static final String KEY_EMAIL_FROM = "KEY_EMAIL_FROM_SESSION_KEY_NOT_COPY";

    private static final String KEY_EMAIL_TO = "KEY_EMAIL_TO_SESSION_KEY_NOT_COPY";

    private static final String KEY_EMAIL_SUBJECT = "KEY_EMAIL_SUBJECT_SESSION_KEY_NOT_COPY";

    private static final String KEY_EMAIL_BODY = "KEY_EMAIL_BODY_SESSION_KEY_NOT_COPY";

    private static final String KEY_EMAIL_FILE = "KEY_EMAIL_FILE_SESSION_KEY_NOT_COPY";

    private static final String KEY_ROWS_PER_PAGE = "KEY_MAIL_ROWS_PER_PAGE_SESSION_KEY_NOT_COPY";

    private static final String KEY_PAGE_NUMBER = "KEY_MAIL_PAGE_NUMBER_SESSION_KEY_NOT_COPY";

    private Date dateFrom;

    private Date dateTo;

    private List<SelectItem> allUsers;

    private List<SelectItem> availableStates;

    private List<SelectItem> searchStates;

    private List<SelectItem> mailTypes;

    private Integer activeTabIndex;

    private Long selectedUserId;

    private Long selectedStateId;

    private Long[] selectedSearchStateIds;

    private Long selectedRequestId;

    private MailManagerTypeWrapper defaultType;

    private List<WLGInboxShort> selectedInboxes;

    private Long selectedTypeId;

    private Boolean needShowEmailTo;

    private Boolean needShowEmailFrom;

    private Boolean needShowEmailUser;

    private Boolean needShowChangeStateUser;

    private String filterAll;

    private Long oldSize;

    private Long mailManagerButtonSelectedId;

    private Long emailDeleteId;

    private Integer tablePage;

    private String tableSortOrder;

    private String tableSortColumn;

    private Integer rowsPerPage;

    @Getter
    @Setter
    private String filterEmailFrom;

    @Getter
    @Setter
    private String filterEmailTo;

    @Getter
    @Setter
    private String filterEmailSubject;

    @Getter
    @Setter
    private String filterEmailBody;

    @Getter
    @Setter
    private String filterEmailFile;

    @Getter
    @Setter
    private String filterEmail;

    @Getter
    @Setter
    private String globalFilter;

    private String mailManagerSent;

    private String mailManagerStorage;

    private String mailManagerDraft;

    private String mailManagerReceived;

    @Getter
    @Setter
    private Integer rowCount;

    @Getter
    @Setter
    private Integer totalPages;

    @Getter
    @Setter
    private Integer currentPageNumber;

    @Getter
    @Setter
    private String pageNavigationStart;

    @Getter
    @Setter
    private String pageNavigationEnd;

    @Getter
    @Setter
    private String paginatorString;

    private String selectedIds;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException, IOException {

        if(ValidationHelper.isNullOrEmpty(SessionHelper.get("loadMailFilters"))){
            clearFilterValueFromSession();
        }
        setRowCount(10);
        setTotalPages(1);
        setCurrentPageNumber(1);
        setTableSortOrder("DESC");
        setTableSortColumn("sendDate");
        StringBuilder builder = new StringBuilder();
        builder.append("<a href=\"#\" class=\"ui-paginator-first ui-state-default ui-corner-all ui-state-disabled");
        builder.append(" tabindex=\"-1\">\n");
        builder.append("<span class=\"ui-icon ui-icon-seek-first\">F</span>\n</a>\n");
        builder.append("<a href=\"#\" onclick=\"previousPage()\"");
        builder.append(" class=\"ui-paginator-prev ui-corner-all ui-state-disabled\" tabindex=\"-1\">\n");
        builder.append("<span class=\"ui-icon ui-icon-seek-prev\">P</span>\n</a>\n");
        setPageNavigationStart(builder.toString());
        builder.setLength(0);
        builder.append("<a href=\"#\" class=\"ui-paginator-next ui-state-default ui-corner-all\"  onclick=\"nextPage()\"");
        builder.append(" tabindex=\"0\">\n");
        builder.append("<span class=\"ui-icon ui-icon-seek-next\">N</span>\n</a>\n");
        builder.append("<a href=\"#\"");
        builder.append(" class=\"ui-paginator-last ui-state-default ui-corner-all\" tabindex=\"-1\" onclick=\"lastPage()\">\n");
        builder.append("<span class=\"ui-icon ui-icon-seek-end\">E</span>\n</a>\n");
        setPageNavigationEnd(builder.toString());

        String tablePage = getRequestParameter(RedirectHelper.TABLE_PAGE);
        if (!ValidationHelper.isNullOrEmpty(tablePage) && !"null".equalsIgnoreCase(tablePage)) {
            setTablePage(Integer.parseInt(tablePage));
            setCurrentPageNumber(getTablePage());
        }
        setAvailableStates(new ArrayList<>());
        setSelectedInboxes(new ArrayList<>());
        setSearchStates(ComboboxHelper.fillList(MailManagerStatuses.class, false));

        for (MailManagerStatuses status : MailManagerStatuses.values()) {
            if (status.isShowAvailable()) {
                if (status != MailManagerStatuses.ARCHIVED || getHavePermissionArchive()) {
                    getAvailableStates().add(new SelectItem(status.getId(), status.toString().toUpperCase()));
                }
            }
        }
        loadFilterFromCookie();
        setDefaultType(new MailManagerTypeWrapper(MailManagerTypes.getById(getMailManagerButtonSelectedId())));
        filterTableFromPanel();



        setAllUsers(ComboboxHelper.fillList(UserShort.class));
        setMailTypes(ComboboxHelper.fillList(WLGFolder.class, Order.asc("name"), new Criterion[]{
                Restrictions.or(
                        Restrictions.isNull("defaultFolder"),
                        Restrictions.ne("defaultFolder", true))
        }));
        oldSize = DaoManager.getCount(WLGInbox.class, "id");
    }

    private String getSessionValue(String key) {
        String result = null;
        if (!ValidationHelper.isNullOrEmpty((String) SessionHelper.get(key))) {
            result = (String) SessionHelper.get(key);
            SessionHelper.removeObject(key);
            HttpSessionHelper.put(key, null);
        } else if (!ValidationHelper.isNullOrEmpty((String) HttpSessionHelper.get(key))) {
            result = (String) HttpSessionHelper.get(key);
            HttpSessionHelper.put(key, null);
        }
        return result;
    }

    private void setSessionValue(String key, String value) {
        SessionHelper.put(key, value);
        HttpSessionHelper.put(key, value);
    }

    private void loadFilterFromCookie() {
        String value = getSessionValue(KEY_DATE_FROM);
        if (!ValidationHelper.isNullOrEmpty(value)) {
            setDateFrom(new Date(Long.parseLong(value)));
        }
        value = getSessionValue(KEY_DATE_TO);
        if (!ValidationHelper.isNullOrEmpty(value)) {
            setDateTo(new Date(Long.parseLong(value)));
        }
        value = getSessionValue(KEY_MAIL_TYPE);
        if (!ValidationHelper.isNullOrEmpty(value)) {
            List<Long> longList = Stream.of(value.substring(1, value.length()-1).split(",")).map(String::trim).map(Long::valueOf).collect(Collectors.toList());
            setSelectedSearchStateIds(longList.toArray(new Long[longList.size()]));
        }

        value = getSessionValue(KEY_KEY_WORD);
        if (!ValidationHelper.isNullOrEmpty(value)) {
            setFilterAll(value);
        }

        value = getSessionValue(KEY_EMAIL);
        if (!ValidationHelper.isNullOrEmpty(value)) {
            setFilterEmail(value);
        }

        value = getSessionValue(KEY_EMAIL_FROM);
        if (!ValidationHelper.isNullOrEmpty(value)) {
            setFilterEmailFrom(value);
        }
        value = getSessionValue(KEY_EMAIL_TO);
        if (!ValidationHelper.isNullOrEmpty(value)) {
            setFilterEmailTo(value);
        }
        value = getSessionValue(KEY_EMAIL_SUBJECT);
        if (!ValidationHelper.isNullOrEmpty(value)) {
            setFilterEmailSubject(value);
        }
        value = getSessionValue(KEY_EMAIL_BODY);
        if (!ValidationHelper.isNullOrEmpty(value)) {
            setFilterEmailBody(value);
        }
        value = getSessionValue(KEY_EMAIL_FILE);
        if (!ValidationHelper.isNullOrEmpty(value)) {
            setFilterEmailFile(value);
        }
        value = getSessionValue(KEY_SELECTED_TAB);
        if (!ValidationHelper.isNullOrEmpty(value)) {
            setMailManagerButtonSelectedId(Long.parseLong(value));
        } else {
            setMailManagerButtonSelectedId(2L);
        }
        value = getSessionValue(KEY_SORT_COLUMN);
        if (!ValidationHelper.isNullOrEmpty(value)) {
            setTableSortColumn(value);
        } else {
            setTableSortColumn("sendDate");
        }
        value = getSessionValue(KEY_SORT_ORDER);
        if (!ValidationHelper.isNullOrEmpty(value)) {
            setTableSortOrder(value);
        } else {
            setTableSortOrder("UNSORTED");
        }

        value = getSessionValue(KEY_ROWS_PER_PAGE);
        if (!ValidationHelper.isNullOrEmpty(value)) {
            setRowsPerPage(Integer.parseInt(value));
        } else {
            setRowsPerPage(18);
        }
        if(getRowsPerPage() == 0)
            setRowsPerPage(18);

        if(ValidationHelper.isNullOrEmpty(getRequestParameter(RedirectHelper.TABLE_PAGE))){
           value = getSessionValue(KEY_PAGE_NUMBER);
        }else {
            String tablePage = getRequestParameter(RedirectHelper.TABLE_PAGE);
            if (!ValidationHelper.isNullOrEmpty(tablePage) && !"null".equalsIgnoreCase(tablePage)) {
                value = tablePage;
            }
        }
        if(!ValidationHelper.isNullOrEmpty(value))
            setTablePage(Integer.parseInt(value));
        else
            setTablePage(0);
//        executeJS("if (PF('tableWV').getPaginator() != null ) " +
//                "PF('tableWV').getPaginator().setPage(" + getTablePage() + ");");
    }

    private void clearFilterValueFromSession() {
        SessionHelper.removeObject(KEY_DATE_FROM);
        HttpSessionHelper.put(KEY_DATE_FROM, null);
        SessionHelper.removeObject(KEY_DATE_TO);
        HttpSessionHelper.put(KEY_DATE_TO, null);
        SessionHelper.removeObject(KEY_MAIL_TYPE);
        HttpSessionHelper.put(KEY_MAIL_TYPE, null);
        SessionHelper.removeObject(KEY_KEY_WORD);
        HttpSessionHelper.put(KEY_KEY_WORD, null);
        SessionHelper.removeObject(KEY_SELECTED_TAB);
        HttpSessionHelper.put(KEY_SELECTED_TAB, null);
        SessionHelper.removeObject(KEY_SORT_ORDER);
        HttpSessionHelper.put(KEY_SORT_ORDER, null);
        SessionHelper.removeObject(KEY_SORT_COLUMN);
        SessionHelper.removeObject(KEY_SORT_COLUMN);
        HttpSessionHelper.put(KEY_SORT_COLUMN, null);
        SessionHelper.removeObject(KEY_EMAIL_TO);
        HttpSessionHelper.put(KEY_EMAIL_TO, null);
        SessionHelper.removeObject(KEY_EMAIL_SUBJECT);
        HttpSessionHelper.put(KEY_EMAIL_SUBJECT, null);
        SessionHelper.removeObject(KEY_EMAIL_BODY);
        HttpSessionHelper.put(KEY_EMAIL_BODY, null);
        SessionHelper.removeObject(KEY_EMAIL_FILE);
        HttpSessionHelper.put(KEY_EMAIL_FILE, null);
        SessionHelper.removeObject(KEY_PAGE_NUMBER);
        HttpSessionHelper.put(KEY_PAGE_NUMBER, null);
        SessionHelper.removeObject(KEY_ROWS_PER_PAGE);
        HttpSessionHelper.put(KEY_ROWS_PER_PAGE, null);
    }
    public void goMain() {
        RedirectHelper.goTo(PageTypes.MAIL_MANAGER_LIST);
    }

    public void createNewMail() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        RedirectHelper.goTo(PageTypes.MAIL_MANAGER_EDIT);
    }

    public void processManagedArchiveState() {
        if (!ValidationHelper.isNullOrEmpty(getEntityEditId())) {
            try {
                WLGInbox mail = DaoManager.get(WLGInbox.class,
                        getEntityEditId());

                if (mail != null) {
                    mail.setUser(DaoManager.get(User.class, getCurrentUser().getId()));
                    DaoManager.save(mail, true);
                    RedirectHelper.goToCreateRequestFromMail(mail.getId(), true, false);
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    public void openRequest() {
        if (getSelectedRequestId() != null) {
            RedirectHelper.goToOnlyView(PageTypes.REQUEST_EDIT,
                    getSelectedRequestId());
        }
    }

    public void assignUserOnMail() {
        if (!ValidationHelper.isNullOrEmpty(getSelectedInboxes())) {
            try {
                for (WLGInboxShort inbox : getSelectedInboxes()) {
                    WLGInbox mail = DaoManager.get(WLGInbox.class, inbox.getId());
                    if (mail != null) {
                        User user = null;
                        if (!ValidationHelper.isNullOrEmpty(getSelectedUserId())) {
                            user = DaoManager.get(User.class, getSelectedUserId());
                        }
                        mail.setUser(user);
                        DaoManager.save(mail, true);
                    }
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    public void changeMailState() {
        changeMailsStatus(MailManagerStatuses.findById(getSelectedStateId()));
    }

    public void changeMailType() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (getSelectedTypeId() != null && !ValidationHelper.isNullOrEmpty(getSelectedInboxes())) {
            WLGFolder folder = DaoManager.get(WLGFolder.class, getSelectedTypeId());
            for (WLGInboxShort inbox : getSelectedInboxes()) {
                MailManagerHelper.changeFolder(DaoManager.get(WLGInbox.class, inbox.getId()),
                        folder, getCurrentUser().getId());
            }
        }
    }

    public void replyMail() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getEntityEditId())) {
            RedirectHelper.goToMailEdit(getEntityEditId(), MailEditType.REPLY);
        } else if (getSelectedInboxes().size() == 1) {
            RedirectHelper.goToMailEdit(getSelectedInboxes().get(0).getId(), MailEditType.REPLY);
        }
    }

    public void replyToAllMail() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getEntityEditId())) {
            RedirectHelper.goToMailEdit(getEntityEditId(), MailEditType.REPLY_TO_ALL);
        } else if (getSelectedInboxes().size() == 1) {
            RedirectHelper.goToMailEdit(getSelectedInboxes().get(0).getId(), MailEditType.REPLY_TO_ALL);
        }
    }

    public void forwardMail() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getEntityEditId())) {
            RedirectHelper.goToMailEdit(getEntityEditId(), MailEditType.FORWARD);
        } else if (getSelectedInboxes().size() == 1) {
            RedirectHelper.goToMailEdit(getSelectedInboxes().get(0).getId(), MailEditType.FORWARD);
        }
    }

    public boolean getCanDeleteMail() {
        try {
            return AccessBean.canDeleteInPage(PageTypes.MAIL_MANAGER_LIST);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return false;
    }

    public boolean isActual() {
        return !ValidationHelper.isNullOrEmpty(getSelectedInboxes()) && getSelectedInboxes().stream()
                .allMatch(item -> !item.getState().equals(MailManagerStatuses.DELETED.getId())
                        && !item.getState().equals(MailManagerStatuses.CANCELED.getId()));
    }

    @Override
    public void editEntity() throws HibernateException, InstantiationException,
            IllegalAccessException, PersistenceBeanException {
        if (getCanEdit()) {
            RedirectHelper.goToMailEdit(getEntityEditId(), MailEditType.EDIT);
        }
    }

    public void rowDblSelectListener() {
        SessionHelper.put("mailManagerLazyModel", this.getLazyModel());
        try {
            WLGInboxShort wlgInbox = DaoManager.get(WLGInboxShort.class, getEntityEditId());
            if (!wlgInbox.getRead()) {
                ReadWLGInbox inbox = new ReadWLGInbox(wlgInbox.getId(), getCurrentUser().getId());
                try {
                    DaoManager.save(inbox, true);
                } catch (PersistenceBeanException e) {
                    LogHelper.log(log, e);
                }
            }
            if (wlgInbox.getState().equals(MailManagerStatuses.NEW.getId())) {
                wlgInbox.setState(MailManagerStatuses.READ.getId());
                try {
                    DaoManager.save(wlgInbox, true);
                } catch (PersistenceBeanException e) {
                    LogHelper.log(log, e);
                }
            }
            updateFilterValueInSession();
            RedirectHelper.goToSavePage(PageTypes.MAIL_MANAGER_VIEW, getEntityEditId(),
                    getCurrentPageNumber());
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void sortData() {
        String tableHeader = FacesContext.getCurrentInstance().
                getExternalContext().getRequestParameterMap().get("tableHeader");

        if(!ValidationHelper.isNullOrEmpty(tableHeader)){
            String sortOrder = getTableSortOrder();
            if(sortOrder.equalsIgnoreCase("DESC") || sortOrder.equalsIgnoreCase("UNSORTED")){
                setTableSortOrder("ASC");
            }else {
                setTableSortOrder("DESC");
            }
            if(tableHeader.equalsIgnoreCase("date_header")){
                setTableSortColumn("sendDate");
                filterTableFromPanel();
            }else  if(tableHeader.equalsIgnoreCase("email_from_header")){
                if(getNeedShowEmailFrom()){
                    setTableSortColumn("emailFrom");
                }else {
                    setTableSortColumn("emailTo");
                }
                filterTableFromPanel();
            }else  if(tableHeader.equalsIgnoreCase("status_header")){
                if(sortOrder.equalsIgnoreCase("DESC") || sortOrder.equalsIgnoreCase("UNSORTED")){
                    ((List<WLGInboxShort>)getLazyModel().getWrappedData())
                            .sort(Comparator.comparing(WLGInboxShort::getStateStr).reversed());
                }else {
                    ((List<WLGInboxShort>)getLazyModel().getWrappedData())
                            .sort(Comparator.comparing(WLGInboxShort::getStateStr));

                }
            }
        }
    }

    public boolean getCanGotoFolders() {
        return getCurrentUser().getPermissions().get("PMMF").isCanView() ||
                getCurrentUser().getPermissions().get("PMMF").isCanCreate() ||
                getCurrentUser().getPermissions().get("PMMF").isCanDelete() ||
                getCurrentUser().getPermissions().get("PMMF").isCanEdit() ||
                getCurrentUser().getPermissions().get("PMMF").isCanList();
    }

    public void gotoFolders() {
        RedirectHelper.goTo(PageTypes.MAIL_MANAGER_FOLDER);
    }

    /**
     * Method for escaping special RegEx characters
     */
    private String shieldingQuery(String str) {
        str = str.replaceAll("\\.", "\\\\\\\\.");
        str = str.replaceAll("\\*", "\\\\\\\\*");
        str = str.replaceAll("\\+", "\\\\\\\\+");
        return str;
    }

    @SuppressWarnings("unchecked")
    public void filterTableFromPanel() {
        updateFilterValueInSession();
        setGlobalFilter(null);
        List<Criterion> restrictions = new ArrayList<>();

        if (!ValidationHelper.isNullOrEmpty(getDateFrom())) {
            restrictions.add(Restrictions.ge("sendDate",
                    DateTimeHelper.getDayStart(getDateFrom())));
        }

        if (!ValidationHelper.isNullOrEmpty(getDateTo())) {
            restrictions.add(Restrictions.le("sendDate",
                    DateTimeHelper.getDayEnd(getDateTo())));
        }

        if (!ValidationHelper.isNullOrEmpty(getFilterEmail())
                || !ValidationHelper.isNullOrEmpty(getFilterAll())) {

            List<Long> listId = null;
            try {
                listId = DaoManager.loadIds(WLGInbox.class, new CriteriaAlias[]{
                                new CriteriaAlias("wlgExports", "we", JoinType.INNER_JOIN)},
                        new Criterion[]{
                                Restrictions.ilike("we.destinationPath",
                                        !ValidationHelper.isNullOrEmpty(getFilterAll())
                                                ? getFilterAll() : getFilterEmail(), MatchMode.ANYWHERE)});
            } catch (Exception e) {
                LogHelper.log(log, e);
            }

            if (!ValidationHelper.isNullOrEmpty(listId)) {
                if(!ValidationHelper.isNullOrEmpty(getFilterAll())){
                    restrictions.add(Restrictions.or(
                            Restrictions.like("emailTo", getFilterAll(), MatchMode.ANYWHERE),
                            Restrictions.like("emailFrom", getFilterAll(), MatchMode.ANYWHERE),
                            Restrictions.like("emailSubject", getFilterAll(), MatchMode.ANYWHERE),
                            Restrictions.like("emailBody", getFilterAll(), MatchMode.ANYWHERE),
                            Restrictions.in("id", listId)));
                }else {
                    restrictions.add(Restrictions.or(
                            Restrictions.like("emailTo", getFilterEmail(), MatchMode.ANYWHERE),
                            Restrictions.like("emailFrom", getFilterEmail(), MatchMode.ANYWHERE),
                            Restrictions.like("emailCC", getFilterEmail(), MatchMode.ANYWHERE),
                            Restrictions.in("id", listId)));
                }
            } else {
                if(!ValidationHelper.isNullOrEmpty(getFilterAll())){
                    restrictions.add(Restrictions.or(
                            Restrictions.like("emailTo", getFilterAll(), MatchMode.ANYWHERE),
                            Restrictions.like("emailFrom", getFilterAll(), MatchMode.ANYWHERE),
                            Restrictions.like("emailSubject", getFilterAll(), MatchMode.ANYWHERE),
                            Restrictions.like("emailBody", getFilterAll(), MatchMode.ANYWHERE)
                            )
                    );
                }else {
                    restrictions.add(Restrictions.or(
                            Restrictions.like("emailTo", getFilterEmail(), MatchMode.ANYWHERE),
                            Restrictions.like("emailFrom", getFilterEmail(), MatchMode.ANYWHERE),
                            Restrictions.like("emailCC", getFilterEmail(), MatchMode.ANYWHERE)
                            )
                    );
                }
            }
        }

        List<Long> listId = null;
        if (!ValidationHelper.isNullOrEmpty(getFilterEmailFile())) {
            try {
                listId = DaoManager.loadIds(WLGInbox.class, new CriteriaAlias[]{
                                new CriteriaAlias("wlgExports", "we", JoinType.INNER_JOIN)},
                        new Criterion[]{Restrictions.ilike("we.destinationPath", getFilterEmailFile(), MatchMode.ANYWHERE)});
            } catch (Exception e) {
                LogHelper.log(log, e);
            }

        }

        List<Criterion> restrictionsLike = new ArrayList<>();
        if(!ValidationHelper.isNullOrEmpty(getFilterEmailFrom())) {
            Criterion r = Restrictions.like("emailFrom", getFilterEmailFrom(), MatchMode.ANYWHERE);
            restrictionsLike.add(r);
        }
        if(!ValidationHelper.isNullOrEmpty(getFilterEmailTo())) {
            Criterion r = Restrictions.like("emailTo", getFilterEmailTo(), MatchMode.ANYWHERE);
            restrictionsLike.add(r);
        }
        if(!ValidationHelper.isNullOrEmpty(getFilterEmailSubject())) {
            Criterion r = Restrictions.like("emailSubject", getFilterEmailSubject(), MatchMode.ANYWHERE);
            restrictionsLike.add(r);
        }
        if(!ValidationHelper.isNullOrEmpty(getFilterEmailBody())) {
            Criterion r = Restrictions.like("emailBody", getFilterEmailBody(), MatchMode.ANYWHERE);
            restrictionsLike.add(r);
        }
        if (!ValidationHelper.isNullOrEmpty(listId)) {
            Criterion r = Restrictions.in("id", listId);
            restrictionsLike.add(r);
        }

        if(restrictionsLike.size()>0) {
            if(restrictionsLike.size()>1) {
                restrictions.add(Restrictions.or(restrictionsLike.toArray(new Criterion[restrictionsLike.size()])));
            }else {
                restrictions.add(restrictionsLike.get(0));
            }
        }
        if (!ValidationHelper.isNullOrEmpty(getSelectedSearchStateIds())) {
            List<MailManagerStatuses> states = MailManagerStatuses.getStates(this.getSelectedSearchStateIds());
            List<Long> idList = new ArrayList<>();
            try {
                idList = DaoManager.loadField(ReadWLGInbox.class, "mailId", Long.class, new Criterion[]{
                        Restrictions.eq("userId", UserHolder.getInstance().getCurrentUser().getId())
                });
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
            List<Long> stateIds = MailManagerStatuses.getStatesIds(states);
            if (states.size() == 1 && states.contains(MailManagerStatuses.READ)) {
                restrictions.add(Restrictions.in("id", idList));
                restrictions.add(Restrictions.eq("state", MailManagerStatuses.NEW.getId()));
            }else if (states.size() == 1 && states.contains(MailManagerStatuses.NEW)) {
                restrictions.add(Restrictions.not(Restrictions.in("id", idList)));
                restrictions.add(Restrictions.eq("state", MailManagerStatuses.NEW.getId()));
            }else {
                restrictions.add(Restrictions.in("state", stateIds));
            }
        }

        if (true) {
            List<Long> stateIds = new ArrayList<>();

            if (defaultType == null || defaultType.getType() != MailManagerTypes.STORAGE) {
                Arrays.stream(MailManagerStatuses.values()).filter(MailManagerStatuses::isNeedShow)
                        .forEach(wkrsw -> stateIds.add(wkrsw.getId()));
            } else {
                stateIds.add(MailManagerStatuses.CANCELED.getId());
                stateIds.add(MailManagerStatuses.DELETED.getId());
            }

            if (!ValidationHelper.isNullOrEmpty(stateIds)) {
                restrictions.add(Restrictions.in("state", stateIds));
            }
        }

        restrictions.add(Restrictions.isNull("folder"));
        if (defaultType != null) {
            switch (defaultType.getType()) {
                case SENT:
                    restrictions.add(Restrictions.eq("serverId", Long.parseLong(ApplicationSettingsHolder
                            .getInstance().getByKey(ApplicationSettingsKeys.SENT_SERVER_ID).getValue())));
                    setNeedShowEmailTo(Boolean.TRUE);
                    setNeedShowEmailFrom(Boolean.FALSE);
                    setNeedShowEmailUser(Boolean.TRUE);
                    setNeedShowChangeStateUser(Boolean.FALSE);
                    break;
                case DRAFT:
                    restrictions.add(Restrictions.isNull("serverId"));
                    setNeedShowEmailTo(Boolean.TRUE);
                    setNeedShowEmailFrom(Boolean.FALSE);
                    setNeedShowEmailUser(Boolean.FALSE);
                    setNeedShowChangeStateUser(Boolean.FALSE);
                    break;
                case RECEIVED:
                    restrictions.add(Restrictions.eq("serverId", Long.parseLong(ApplicationSettingsHolder
                            .getInstance().getByKey(ApplicationSettingsKeys.RECEIVED_SERVER_ID).getValue())));
                    setNeedShowEmailTo(Boolean.FALSE);
                    setNeedShowEmailFrom(Boolean.TRUE);
                    setNeedShowEmailUser(Boolean.FALSE);
                    setNeedShowChangeStateUser(Boolean.TRUE);
                    break;
                case STORAGE:
                    setNeedShowEmailTo(Boolean.FALSE);
                    setNeedShowEmailFrom(Boolean.TRUE);
                    setNeedShowEmailUser(Boolean.FALSE);
                    setNeedShowChangeStateUser(Boolean.FALSE);
                    break;
            }
        }
        boolean isLoaded = false;
        if (SessionHelper.get("isFromMailView") != null
                && ((Boolean) SessionHelper.get("isFromMailView"))) {

            SessionHelper.removeObject("isFromMailView");
            LazyDataModel<WLGInboxShort> lazyDataModel = (LazyDataModel<WLGInboxShort>) SessionHelper.get("mailManagerLazyModel");
            if(lazyDataModel != null) {
                isLoaded = true;
                this.setLazyModel(lazyDataModel);
                SessionHelper.removeObject("mailManagerLazyModel");
            }
        }
        if(!isLoaded) {
            this.setLazyModel(new EntityLazyListModel<>(WLGInboxShort.class, restrictions.toArray(new Criterion[0]),
                    new Order[]{
                            Order.desc("sendDate")
                    }, new CriteriaAlias[]{
                    new CriteriaAlias("folder", "folder", JoinType.LEFT_OUTER_JOIN)
            }));
        }
        getLazyModel().load( (getTablePage()-1)*getRowsPerPage(), getRowsPerPage(), getTableSortColumn(),
                (getTableSortOrder() == null || getTableSortOrder().equalsIgnoreCase("DESC") || getTableSortOrder().equalsIgnoreCase("UNSORTED")) ? SortOrder.DESCENDING : SortOrder.ASCENDING, new HashMap<>());


        Integer rowCount = (int)Math.ceil((getLazyModel().getRowCount()*1.0)/getRowsPerPage());
        if(rowCount == 0)
            rowCount = 1;
        setTotalPages(rowCount);
        if(rowCount > 10)
            setRowCount(10);
        else
            setRowCount(rowCount);

        setPaginator(getCurrentPageNumber());
    }

    public void archiveMail() {
        changeMailsStatus(MailManagerStatuses.ARCHIVED);
    }

    public void reestablishMail() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getSelectedInboxes())) {
            for (WLGInboxShort inbox : getSelectedInboxes()) {
                WLGInbox mail = DaoManager.get(WLGInbox.class, inbox.getId());
                if (mail != null) {
                    if (mail.getPreviousState() != null) {
                        mail.setState(mail.getPreviousState());
                        mail.setPreviousState(null);
                    } else {
                        mail.setState(MailManagerStatuses.NEW.getId());
                        if (!mail.getRead()) {
                            ReadWLGInbox readWLGInbox = new ReadWLGInbox(mail.getId(), getCurrentUser().getId());
                            DaoManager.save(readWLGInbox, true);
                        }
                    }
                    DaoManager.save(mail, true);
                }
            }
        }
    }

    public boolean getHavePermissionArchive() {
        return PermissionsHelper.getPermission(SpecialPermissionTypes.CAN_ARCHIVE_MAIL);
    }

    public boolean getSelectedCanActivateProcedure() {
        return !getSelectedInboxes().isEmpty() && getSelectedInboxes().stream().allMatch(WLGInboxShort::getCanActivateProcedure);
    }

    private void changeMailsStatus(MailManagerStatuses status) {
        if (!ValidationHelper.isNullOrEmpty(getSelectedInboxes())) {
            try {
                for (WLGInboxShort inbox : getSelectedInboxes()) {
                    WLGInbox mail = DaoManager.get(WLGInbox.class, inbox.getId());
                    if (mail != null) {
                        mail.setUserChangedState(null);
                        if (MailManagerStatuses.READ == status) {
                            if (!mail.getRead()) {
                                ReadWLGInbox readWLGInbox = new ReadWLGInbox(mail.getId(), getCurrentUser().getId());
                                DaoManager.save(readWLGInbox, true);
                            }
                        } else if (MailManagerStatuses.NEW == status) {
                            List<ReadWLGInbox> readWLGInboxList = DaoManager.load(ReadWLGInbox.class, new Criterion[]{
                                    Restrictions.eq("mailId", mail.getId()),
                                    Restrictions.eq("userId", getCurrentUser().getId())
                            });
                            if (!ValidationHelper.isNullOrEmpty(readWLGInboxList)) {
                                for (ReadWLGInbox read : readWLGInboxList) {
                                    DaoManager.remove(read, true);
                                }
                            }
                        } else if (MailManagerStatuses.CANCELED == status) {
                            mail.setPreviousState(mail.getState());
                        } else if (MailManagerStatuses.PARTIAL == status || MailManagerStatuses.MANAGED == status) {
                            mail.setUserChangedState(DaoManager.get(User.class, getCurrentUser().getId()));
                        }
                        mail.setState(status.getId());
                        DaoManager.save(mail, true);
                    }
                    DaoManager.getSession().refresh(inbox);
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    public void goTable() {
    }

    public boolean getCanCreate() {
        try {
            return AccessBean.canCreateInPage(PageTypes.MAIL_MANAGER_LIST);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return false;
    }

    private void deleteMail(List<ClientEmailWrapper> mails) {
        for (int i = 0; i < mails.size(); ++i) {
            if (mails.get(i).getClientEmail().getId()
                    .equals(getEmailDeleteId())) {
                try {
                    DaoManager.remove(mails.get(i).getClientEmail(), true);
                    mails.remove(i);

                    break;
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
            }
        }
    }

    public void executePoll() throws PersistenceBeanException, IllegalAccessException {
        Long count = DaoManager.getCount(WLGInbox.class, "id");
        if (!count.equals(oldSize)) {
            executeJS("updatePoll();");
            oldSize = count;
        }
    }

    public void cancelMail() {
        changeMailsStatus(MailManagerStatuses.CANCELED);
        setSelectedInboxes(new ArrayList<WLGInboxShort>());
    }

    public void filterLetterByOwner() {
        setTablePage(0);
        getSelectedInboxes().clear();
        Long id = getMailManagerButtonSelectedId();
        MailManagerTypeWrapper wrapper = new MailManagerTypeWrapper(MailManagerTypes.getById(id));
        setDefaultType(wrapper);
        setSessionValue(KEY_SELECTED_TAB, Long.toString(getMailManagerButtonSelectedId()));
        filterTableFromPanel();
    }

    public String getMailManagerRECEIVED() {
        return getMailTitleValue(new MailManagerTypeWrapper(MailManagerTypes.RECEIVED));
    }

    public String getMailManagerSENT() {
        return getMailTitleValue(new MailManagerTypeWrapper(MailManagerTypes.SENT));
    }

    public String getMailManagerDRAFT() {
        return getMailTitleValue(new MailManagerTypeWrapper(MailManagerTypes.DRAFT));
    }

    public String getMailManagerSTORAGE() {
        return getMailTitleValue(new MailManagerTypeWrapper(MailManagerTypes.STORAGE));
    }

    public boolean isSendOrStorage() {
        if (!ValidationHelper.isNullOrEmpty(getMailManagerButtonSelectedId())) {
            return (getMailManagerButtonSelectedId().equals(MailManagerTypes.SENT.getId())
                    || getMailManagerButtonSelectedId().equals(MailManagerTypes.STORAGE.getId()));
        }
        return false;
    }

    public boolean isReceivedOrDraft() {
        if (!ValidationHelper.isNullOrEmpty(getMailManagerButtonSelectedId())) {
            return (getMailManagerButtonSelectedId().equals(MailManagerTypes.RECEIVED.getId())
                    || getMailManagerButtonSelectedId().equals(MailManagerTypes.DRAFT.getId()));
        }
        return false;
    }

    private String getMailTitleValue(MailManagerTypeWrapper typeWrapper) {
        if (typeWrapper.getType().isAmount()) {
            return typeWrapper.getFacesTitle(String.valueOf(
                    getSessionBean().getMailCountByTypeAndStatus(typeWrapper.getType())));
        }
        return typeWrapper.getValue();
    }

    public void rowSelectListener(SelectEvent event) {
        WLGInboxShort wlgInbox = (WLGInboxShort) event.getObject();
        setEntityEditId(wlgInbox.getId());
    }

    public void rowDblSelectListener(SelectEvent event) {
        SessionHelper.put("mailManagerLazyModel", this.getLazyModel());
        WLGInboxShort wlgInbox = (WLGInboxShort) event.getObject();
        setEntityEditId(wlgInbox.getId());
        if (!wlgInbox.getRead()) {
            ReadWLGInbox inbox = new ReadWLGInbox(wlgInbox.getId(), getCurrentUser().getId());
            try {
                DaoManager.save(inbox, true);
            } catch (PersistenceBeanException e) {
                LogHelper.log(log, e);
            }
        }
        if (wlgInbox.getState().equals(MailManagerStatuses.NEW.getId())) {
            wlgInbox.setState(MailManagerStatuses.READ.getId());
            try {
                DaoManager.save(wlgInbox, true);
            } catch (PersistenceBeanException e) {
                LogHelper.log(log, e);
            }
        }
        DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot()
                .findComponent("form:table");
        updateFilterValueInSession();
        setSessionValue(KEY_SORT_ORDER, dataTable.getSortOrder());
        if (!ValidationHelper.isNullOrEmpty(dataTable.getSortColumn())) {
            setSessionValue(KEY_SORT_COLUMN, dataTable.getSortColumn().getClientId().split(":")[1]);
        }
        RedirectHelper.goToSavePage(PageTypes.MAIL_MANAGER_VIEW, getEntityEditId(),
                dataTable.getPage() * dataTable.getRows());
    }


    private void updateFilterValueInSession() {
        if (!ValidationHelper.isNullOrEmpty(getDateFrom())) {
            setSessionValue(KEY_DATE_FROM, Long.toString(getDateFrom().getTime()));
        }else {
            setSessionValue(KEY_DATE_FROM, null);
        }
        if (!ValidationHelper.isNullOrEmpty(getDateTo())) {
            setSessionValue(KEY_DATE_TO, Long.toString(getDateTo().getTime()));
        }else {
            setSessionValue(KEY_DATE_TO, null);
        }
        if (!ValidationHelper.isNullOrEmpty(getSelectedSearchStateIds())) {
            setSessionValue(KEY_MAIL_TYPE, Arrays.toString(getSelectedSearchStateIds()));
        }else {
            setSessionValue(KEY_MAIL_TYPE, null);
        }
        if (!ValidationHelper.isNullOrEmpty(getFilterEmailFrom())) {
            setSessionValue(KEY_EMAIL_FROM, getFilterEmailFrom());
        }else {
            setSessionValue(KEY_EMAIL_FROM, null);
        }
        if (!ValidationHelper.isNullOrEmpty(getFilterEmailTo())) {
            setSessionValue(KEY_EMAIL_TO, getFilterEmailTo());
        }else {
            setSessionValue(KEY_EMAIL_TO, null);
        }
        if (!ValidationHelper.isNullOrEmpty(getFilterEmailSubject())) {
            setSessionValue(KEY_EMAIL_SUBJECT, getFilterEmailSubject());
        }else {
            setSessionValue(KEY_EMAIL_SUBJECT, null);
        }
        if (!ValidationHelper.isNullOrEmpty(getFilterEmailBody())) {
            setSessionValue(KEY_EMAIL_BODY, getFilterEmailBody());
        }else {
            setSessionValue(KEY_EMAIL_BODY, null);
        }
        if (!ValidationHelper.isNullOrEmpty(getFilterEmailFile())) {
            setSessionValue(KEY_EMAIL_FILE, getFilterEmailFile());
        }else {
            setSessionValue(KEY_EMAIL_FILE, null);
        }
        if (!ValidationHelper.isNullOrEmpty(getFilterEmail())) {
            setSessionValue(KEY_EMAIL, getFilterEmail());
        }else {
            setSessionValue(KEY_EMAIL, null);
        }
        if (!ValidationHelper.isNullOrEmpty(getFilterAll())) {
            setSessionValue(KEY_KEY_WORD, getFilterAll());
        }else {
            setSessionValue(KEY_KEY_WORD, null);
        }
        if (!ValidationHelper.isNullOrEmpty(getMailManagerButtonSelectedId())) {
            setSessionValue(KEY_SELECTED_TAB, Long.toString(getMailManagerButtonSelectedId()));
        }else {
            setSessionValue(KEY_SELECTED_TAB, null);
        }
        if (!ValidationHelper.isNullOrEmpty(getRowsPerPage())) {
            setSessionValue(KEY_ROWS_PER_PAGE, Long.toString(getRowsPerPage()));
        }else {
            setSessionValue(KEY_ROWS_PER_PAGE, null);
        }

        if (!ValidationHelper.isNullOrEmpty(getTablePage())) {
            setSessionValue(KEY_PAGE_NUMBER, Long.toString(getTablePage()));
        }else {
            setSessionValue(KEY_PAGE_NUMBER, null);
        }
    }

    public boolean isSentMail() {
        return getMailManagerButtonSelectedId() != null
                && MailManagerTypes.getById(getMailManagerButtonSelectedId()) == MailManagerTypes.SENT;
    }

    public boolean isReceivedMail() {
        return getMailManagerButtonSelectedId() != null
                && MailManagerTypes.getById(getMailManagerButtonSelectedId()) == MailManagerTypes.RECEIVED;
    }

    public void clearFiltraPanel() {
        setDateFrom(null);
        setDateTo(null);
        setSelectedSearchStateIds(null);
        setFilterAll(null);
        filterTableFromPanel();
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }

    public List<SelectItem> getAllUsers() {
        return allUsers;
    }

    public void setAllUsers(List<SelectItem> allUsers) {
        this.allUsers = allUsers;
    }

    public List<SelectItem> getAvailableStates() {
        return availableStates;
    }

    public void setAvailableStates(List<SelectItem> availableStates) {
        this.availableStates = availableStates;
    }

    public List<SelectItem> getMailTypes() {
        return mailTypes;
    }

    public void setMailTypes(List<SelectItem> mailTypes) {
        this.mailTypes = mailTypes;
    }

    public Integer getActiveTabIndex() {
        return activeTabIndex;
    }

    public void setActiveTabIndex(Integer activeTabIndex) {
        this.activeTabIndex = activeTabIndex;
    }

    public Long getSelectedUserId() {
        return selectedUserId;
    }

    public void setSelectedUserId(Long selectedUserId) {
        this.selectedUserId = selectedUserId;
    }

    public Long getSelectedStateId() {
        return selectedStateId;
    }

    public void setSelectedStateId(Long selectedStateId) {
        this.selectedStateId = selectedStateId;
    }

    public Long getSelectedRequestId() {
        return selectedRequestId;
    }

    public void setSelectedRequestId(Long selectedRequestId) {
        this.selectedRequestId = selectedRequestId;
    }

    public Long getOldSize() {
        return oldSize;
    }

    public void setOldSize(Long oldSize) {
        this.oldSize = oldSize;
    }

    public Long getMailManagerButtonSelectedId() {
        return mailManagerButtonSelectedId;
    }

    public void setMailManagerButtonSelectedId(Long mailManagerButtonSelectedId) {
        this.mailManagerButtonSelectedId = mailManagerButtonSelectedId;
    }

    public MailManagerTypeWrapper getDefaultType() {
        return defaultType;
    }

    public void setDefaultType(MailManagerTypeWrapper defaultType) {
        this.defaultType = defaultType;
    }

    public Integer getActiveIndex() {
        return activeTabIndex;
    }

    public void setActiveIndex(Integer index) {
        this.activeTabIndex = index;
    }

    public List<WLGInboxShort> getSelectedInboxes() {
        return selectedInboxes;
    }

    public void setSelectedInboxes(List<WLGInboxShort> selectedInboxes) {
        this.selectedInboxes = selectedInboxes;
    }

    public Long getSelectedTypeId() {
        return selectedTypeId;
    }

    public void setSelectedTypeId(Long selectedTypeId) {
        this.selectedTypeId = selectedTypeId;
    }

    public Boolean getNeedShowEmailTo() {
        return needShowEmailTo;
    }

    public void setNeedShowEmailTo(Boolean needShowEmailTo) {
        this.needShowEmailTo = needShowEmailTo;
    }

    public Boolean getNeedShowEmailFrom() {
        return needShowEmailFrom;
    }

    public void setNeedShowEmailFrom(Boolean needShowEmailFrom) {
        this.needShowEmailFrom = needShowEmailFrom;
    }

    public Long getEmailDeleteId() {
        return emailDeleteId;
    }

    public void setEmailDeleteId(Long emailDeleteId) {
        this.emailDeleteId = emailDeleteId;
    }

    public Boolean getNeedShowEmailUser() {
        return needShowEmailUser;
    }

    public void setNeedShowEmailUser(Boolean needShowEmailUser) {
        this.needShowEmailUser = needShowEmailUser;
    }

    public List<SelectItem> getSearchStates() {
        return searchStates;
    }

    public void setSearchStates(List<SelectItem> searchStates) {
        this.searchStates = searchStates;
    }

    public Long[] getSelectedSearchStateIds() {
        return selectedSearchStateIds;
    }

    public void setSelectedSearchStateIds(Long[] selectedSearchStateIds) {
        this.selectedSearchStateIds = selectedSearchStateIds;
    }

    public Integer getTablePage() {
        return tablePage;
    }

    public void setTablePage(Integer tablePage) {
        this.tablePage = tablePage;
    }

    public Boolean getNeedShowChangeStateUser() {
        return needShowChangeStateUser;
    }

    public void setNeedShowChangeStateUser(Boolean needShowChangeStateUser) {
        this.needShowChangeStateUser = needShowChangeStateUser;
    }

    public String getTableSortOrder() {
        return tableSortOrder;
    }

    public void setTableSortOrder(String tableSortOrder) {
        this.tableSortOrder = tableSortOrder;
    }

    public String getTableSortColumn() {
        return tableSortColumn;
    }

    public void setTableSortColumn(String tableSortColumn) {
        this.tableSortColumn = tableSortColumn;
    }

    public String getFilterAll() {
        return filterAll;
    }

    public void setFilterAll(String filterAll) {
        this.filterAll = filterAll;
    }

    public void globalSearch() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if(!ValidationHelper.isNullOrEmpty(this.globalFilter)){
            List<Long> ids = new ArrayList<>();
            for (MailManagerStatuses mailManagerStatuses : MailManagerStatuses.values()) {
                if (mailManagerStatuses.toString().toLowerCase().contains(this.globalFilter.toLowerCase())) {
                    if(!ids.contains(mailManagerStatuses.getId())){
                        ids.add(mailManagerStatuses.getId());
                    }
                }
            }
            Long[] selectedSearchStateIds = ids.toArray(new Long[0]);
            List<Criterion> restrictions = new ArrayList<>();
            restrictions.add(Restrictions.like("emailSubject", this.globalFilter, MatchMode.ANYWHERE));
            if (!ValidationHelper.isNullOrEmpty(selectedSearchStateIds)) {
                List<MailManagerStatuses> states = MailManagerStatuses.getStates(selectedSearchStateIds);
                List<Long> idList = new ArrayList<>();
                try {
                    idList = DaoManager.loadField(ReadWLGInbox.class, "mailId", Long.class, new Criterion[]{
                            Restrictions.eq("userId", UserHolder.getInstance().getCurrentUser().getId())
                    });
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
                List<Long> stateIds = MailManagerStatuses.getStatesIds(states);
                if (states.size() == 1 && states.contains(MailManagerStatuses.READ)) {
                    restrictions.add(Restrictions.in("id", idList));
                    restrictions.add(Restrictions.eq("state", MailManagerStatuses.NEW.getId()));
                }else if (states.size() == 1 && states.contains(MailManagerStatuses.NEW)) {
                    restrictions.add(Restrictions.not(Restrictions.in("id", idList)));
                    restrictions.add(Restrictions.eq("state", MailManagerStatuses.NEW.getId()));
                }else {
                    restrictions.add(Restrictions.in("state", stateIds));
                }
            }
            Criterion r = Restrictions.or(
                    Restrictions.like("emailTo", this.globalFilter, MatchMode.ANYWHERE),
                    Restrictions.like("emailFrom", this.globalFilter, MatchMode.ANYWHERE),
                    Restrictions.like("emailBody", this.globalFilter, MatchMode.ANYWHERE));
            restrictions.add(r);
            List<Criterion> rest = new ArrayList<>();
            if(restrictions.size() > 0){
                rest.add(Restrictions.or(restrictions.toArray(new Criterion[restrictions.size()])));
            }
            loadList(WLGInboxShort.class,  rest.toArray(new Criterion[0]),
                    new Order[]{
                            Order.desc("sendDate")
                    }, new CriteriaAlias[]{
                            new CriteriaAlias("folder", "folder", JoinType.LEFT_OUTER_JOIN)
                    });
        }else {
            this.filterTableFromPanel();
        }
    }

    public Integer getRowsPerPage() {
        return rowsPerPage;
    }

    public void setRowsPerPage(Integer rowsPerPage) {
        this.rowsPerPage = rowsPerPage;
    }

    public void onPageChange() {
        String rowsPerPage = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("table_rppDD");
        String pageNumber = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("pageNumber");

        if(!ValidationHelper.isNullOrEmpty(rowsPerPage))
            setRowsPerPage(Integer.parseInt(rowsPerPage));
        if(!ValidationHelper.isNullOrEmpty(pageNumber)){
            Integer currentPage = Integer.parseInt(pageNumber);
            setCurrentPageNumber(currentPage);
            StringBuilder builder = new StringBuilder();
            String cls = "ui-paginator-first ui-state-default ui-corner-all";
            if(currentPage == 1){
                cls += " ui-state-disabled";
            }
            builder.append("<a href=\"#\" class=\"" + cls + "\"");
            builder.append(" tabindex=\"-1\" onclick=\"firstPage()\">\n");
            builder.append("<span class=\"ui-icon ui-icon-seek-first\">F</span>\n</a>\n");
            builder.append("<a href=\"#\" onclick=\"previousPage()\"");
            cls = "ui-paginator-prev ui-corner-all";
            if(currentPage == 1){
                cls += " ui-state-disabled";
            }
            builder.append(" class=\"" + cls + "\"");
            builder.append(" tabindex=\"-1\">\n");
            builder.append("<span class=\"ui-icon ui-icon-seek-prev\">P</span>\n</a>\n");

            setPageNavigationStart(builder.toString());

//            builder.setLength(0);
//            for(int i = 1; i <=10;i++ ){
//                builder.append("<a class=\"ui-paginator-page ui-state-default ui-corner-all page_" + i + "\"");
//                builder.append("tabindex=\"0\" href=\"#\" onclick=\"changePage(" + i + ")\">" + i +"</a>");
//            }

            if(currentPage == getTotalPages()){
//                builder.setLength(0);
//                Integer pageEnd = getTotalPages();
//                Integer pageStart = getTotalPages() - 10;
//                for(int i = pageStart; i <= pageEnd;i++ ){
//                    builder.append("<a class=\"ui-paginator-page ui-state-default ui-corner-all page_" + i + "\"");
//                    builder.append("tabindex=\"0\" href=\"#\" onclick=\"changePage(" + i + ")\">" + i +"</a>");
//                }
//                setPaginatorString(builder.toString());
                builder.setLength(0);
                builder.append("<a href=\"#\" class=\"ui-paginator-next ui-state-default ui-corner-all ui-state-disabled\"");
                builder.append(" tabindex=\"0\">\n");
                builder.append("<span class=\"ui-icon ui-icon-seek-next\">N</span>\n</a>\n");
                builder.append("<a href=\"#\"");
                builder.append(" class=\"ui-paginator-last ui-state-default ui-corner-all ui-state-disabled\" tabindex=\"-1\">\n");
                builder.append("<span class=\"ui-icon ui-icon-seek-end\">E</span>\n</a>\n");
                setPageNavigationEnd(builder.toString());
            }else {
                builder.setLength(0);
                builder.append("<a href=\"#\" class=\"ui-paginator-next ui-state-default ui-corner-all\"  onclick=\"nextPage()\"");
                builder.append(" tabindex=\"0\">\n");
                builder.append("<span class=\"ui-icon ui-icon-seek-next\">N</span>\n</a>\n");
                builder.append("<a href=\"#\"");
                builder.append(" class=\"ui-paginator-last ui-state-default ui-corner-all\" tabindex=\"-1\" onclick=\"lastPage()\">\n");
                builder.append("<span class=\"ui-icon ui-icon-seek-end\">E</span>\n</a>\n");
                setPageNavigationEnd(builder.toString());
//                if(currentPage != null && (currentPage -1)% 10 == 0){
//
//                    builder.setLength(0);
//                    Integer pageEnd = currentPage+9;
//                    if(pageEnd > getTotalPages())
//                        pageEnd = getTotalPages();
//                    System.out.println("Current page " + currentPage + " pageEnd " + pageEnd);
//                    for(int i = currentPage; i <= pageEnd;i++ ){
//                        builder.append("<a class=\"ui-Tapaginator-page ui-state-default ui-corner-all page_" + i + "\"");
//                        builder.append("tabindex=\"0\" href=\"#\" onclick=\"changePage(" + i + ")\">" + i +"</a>");
//                    }
//                    setPaginatorString(builder.toString());
//                }
            }
            setTablePage(currentPage);
            filterTableFromPanel();
           // setPaginator(currentPage);
        }
    }

    public void handleRowsChange() {
        String rowsPerPage = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("table_rppDD");
        String pageNumber = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("pageNumber");
        if(!ValidationHelper.isNullOrEmpty(rowsPerPage))
            setRowsPerPage(Integer.parseInt(rowsPerPage));

        Integer totalPages = getRowCount()/getRowsPerPage();
        Integer pageEnd = 10;
        if(pageEnd < totalPages)
            pageEnd = totalPages;
        StringBuilder builder = new StringBuilder();
        for(int i = 1; i <= pageEnd;i++ ){
            builder.append("<a class=\"ui-paginator-page ui-state-default ui-corner-all page_" + i + "\"");
            builder.append("tabindex=\"0\" href=\"#\" onclick=\"changePage(" + i + ")\">" + i +"</a>");
        }
        setPaginatorString(builder.toString());
        setTablePage(1);
        filterTableFromPanel();
    }

    public void onBottomPageChange() {
        String rowsPerPage = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("table_rppDD_bottom");
        String pageNumber = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("pageNumber");
        if(!ValidationHelper.isNullOrEmpty(rowsPerPage))
            setRowsPerPage(Integer.parseInt(rowsPerPage));
        if(!ValidationHelper.isNullOrEmpty(pageNumber))
            setTablePage(Integer.parseInt(pageNumber));

        filterTableFromPanel();
    }
    public void onPageChange(PageEvent event) {
        if (event != null)
            setTablePage(event.getPage());
        setSessionValue(KEY_PAGE_NUMBER, Long.toString(getTablePage()));
        Map<String, String> params = FacesContext.getCurrentInstance().
                getExternalContext().getRequestParameterMap();
        if (!params.isEmpty()) {
            String rows = params.get("table_rows");
            if (!ValidationHelper.isNullOrEmpty(rows)) {
                try {
                    setRowsPerPage(Integer.parseInt(rows));
                    setSessionValue(KEY_ROWS_PER_PAGE, Long.toString(getRowsPerPage()));
                } catch (NumberFormatException e) {
                }
            }
        }
    }

    private void setPaginator(Integer currentPageNumber){
        StringBuilder builder = new StringBuilder();
        if(currentPageNumber == getTotalPages()){
            builder.setLength(0);
            Integer pageEnd = getTotalPages();
            Integer pageStart = getTotalPages() > 10 ? (getTotalPages() - 10) : 1;
            for(int i = pageStart; i <= pageEnd;i++ ){
                builder.append("<a class=\"ui-paginator-page ui-state-default ui-corner-all page_" + i + "\"");
                builder.append("tabindex=\"0\" href=\"#\" onclick=\"changePage(" + i + ")\">" + i +"</a>");
            }
        }else if(currentPageNumber > 10 && currentPageNumber != null && (currentPageNumber -1)% 10 == 0){
            builder.setLength(0);
            Integer pageEnd = currentPageNumber+9;
            if(pageEnd > getTotalPages())
                pageEnd = getTotalPages();
            for(int i = currentPageNumber; i <= pageEnd;i++ ){
                builder.append("<a class=\"ui-paginator-page ui-state-default ui-corner-all page_" + i + "\"");
                builder.append("tabindex=\"0\" href=\"#\" onclick=\"changePage(" + i + ")\">" + i +"</a>");
            }
        }else {
            int pageStart = 1;
            if(currentPageNumber > 10){
                if(currentPageNumber %10 == 0){
                    pageStart = 10*((currentPageNumber-1)/10) + 1;
                }else {
                    pageStart = 10*(currentPageNumber/10) + 1;
                }
            }

            int page;

            if((pageStart + 10) > getTotalPages())
                page = getTotalPages();
            else
                page = pageStart + 9;

            for(int i = pageStart; i <=page;i++ ){
                builder.append("<a class=\"ui-paginator-page ui-state-default ui-corner-all page_" + i + "\"");
                builder.append("tabindex=\"0\" href=\"#\" onclick=\"changePage(" + i + ")\">" + i +"</a>");
            }

        }
        setPaginatorString(builder.toString());
    }

    public void selectInbox(WLGInboxShort wlgInboxShort){
        Map<String, String> requestMap = FacesContext.getCurrentInstance().
                getExternalContext().getRequestParameterMap();
        String key = "mail_item_" + wlgInboxShort.getId() + "_input";
        if(requestMap.containsKey(key)){
            getSelectedInboxes().add(wlgInboxShort);
        }else {
            Predicate<WLGInboxShort> condition = wlgInbox -> wlgInbox.getId().equals(wlgInboxShort.getId());
            getSelectedInboxes().removeIf(condition);
        }
//        List<Map.Entry<String,String>> selectedInputs = FacesContext.getCurrentInstance().
//                getExternalContext().getRequestParameterMap()
//                .entrySet().stream()
//                .filter(e -> e.getKey().contains("mail_item_"))
//                .collect(Collectors.toList());
//
//        for (Map.Entry<String, String> entry : selectedInputs) {
//            if(entry.getValue().equalsIgnoreCase("on")){
//                Long selectedId = Long.parseLong(entry.getKey().split("\\_")[2]);
//
//
//            }
//        }
    }

    public void selectAllInbox() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        String selectedIds = FacesContext.getCurrentInstance().
                getExternalContext().getRequestParameterMap().get("selectedIds");
        List<String> selectedIdList = Arrays.asList(selectedIds.split(",", -1));

        if(ValidationHelper.isNullOrEmpty(selectedIdList)){
            setSelectedInboxes(new ArrayList<>());
        }else {
            for(String selectedId : selectedIdList){
                if(!ValidationHelper.isNullOrEmpty(selectedId)){
                    Long id = Long.parseLong(selectedId.split("\\_")[2]);
                    getSelectedInboxes().add(DaoManager.get(WLGInboxShort.class, id));
                }
            }
        }

    }

    public String getSelectedIds() {
        return CollectionUtils.emptyIfNull(getSelectedInboxes()).stream()
                .map(WLGInboxShort::getStrId)
                .collect(Collectors.joining("_"));

    }

    public void setSelectedIds(String selectedIds) {
        this.selectedIds = selectedIds;
    }
}
