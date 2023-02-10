package it.nexera.ris.web.beans.session;

import it.nexera.ris.common.enums.*;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.common.helpers.SessionHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.*;
import it.nexera.ris.persistence.beans.entities.domain.readonly.RequestMailShort;
import it.nexera.ris.persistence.beans.entities.domain.readonly.WLGInboxHome;
import it.nexera.ris.persistence.beans.entities.domain.readonly.WLGInboxShort;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.base.AccessBean;
import it.nexera.ris.web.beans.wrappers.logic.UserWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.ocsp.Req;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.primefaces.context.RequestContext;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ManagedBean(name = "sessionBean")
@SessionScoped
public class SessionBean implements Serializable {
    private static final long serialVersionUID = -3444727566938800223L;

    protected transient final Log log = LogFactory.getLog(getClass());

    private transient Map<String, Object> Session = new HashMap<>();

    public static final boolean useNativeViewState = true;

    private static long timeToRefresh = 60; //in seconds

    private Integer mailCounts=0;
    
    private Integer requestCounts=0;
    
    private Integer numberEmailsReadyForSent=0;

    private Boolean loadingEmailCountDone = false;

    public SessionBean() {
        Session.put("my_session_id", DateTimeHelper.toSessionTime(new Date()));
    }

    private Boolean overLayMode = Boolean.TRUE;

    public boolean canViewPageUrl(String url) {
        PageTypes type = PageTypes.getPageTypeByPath(url);
        return canViewPage(type);
    }

    public boolean canViewPage(PageTypes pageTypes) {
        try {
            return AccessBean.canViewPage(pageTypes);
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getViewState() {
        if (useNativeViewState) {
            return FacesContext.getCurrentInstance().getViewRoot().getViewMap();
        } else {
            if (Session == null) {
                Session = new HashMap<>();
            }
            Session.computeIfAbsent(getRequestUrl(), k -> new HashMap<String, Object>());
            return (Map<String, Object>) Session.get(getRequestUrl());
        }
    }

    private String getRequestUrl() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) ctx
                .getExternalContext().getRequest();

        return request.getServletPath();
    }

    public Map<String, Object> getSession() {
        try {
            if (Session == null) {
                Session = new HashMap<>();
            }
            return Session;
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return null;
    }

    public UserWrapper getCurrentUser() {
        return UserHolder.getInstance().getCurrentUser();
    }

    public int getMailCountByTypeAndStatus(MailManagerTypes type) {
        String query = null;
        switch (type) {
            case RECEIVED:
                query = getCountReceivedNewMailsQuery();
                break;
            case STORAGE:
                query = getCountStorageMailsQuery();
                break;
            case DRAFT:
                query = getCountDraftMailsQuery();
                break;
            case SENT:
                query = getCountSentMailsQuery();
                break;
        }
        if (query != null) {
            try {
                setMailCounts(DaoManager.countQuery(query).intValue());
                return getMailCounts();
                //return DaoManager.countQuery(query).intValue();
            } catch (PersistenceBeanException | IllegalAccessException e) {
                LogHelper.log(log, e);
            }

        }
        return 0;
    }

    public int getNewMailCount() {
        return getMailCountByTypeAndStatus(MailManagerTypes.RECEIVED);
    }

    public void updateMailCount() {
        getNewMailCount();
    }
    
    public boolean getNeedShowNewMailCount() {
        return getNewMailCount() > 0;
    }

    public void updateNumberUnclosedRequests() {
        getNumberUnclosedInserted();
    }
    
    public int getNumberUnclosedRequests() {
        return getNumberUnclosedRequestsByStatus(RequestState.INSERTED, false);
    }

    public int getNumberUnclosedNotarialCertifications() {
        return getNumberUnclosedRequestsByStatus(RequestState.INSERTED, true);
    }

    public int getNumberUnclosedRequestsInWork() {
        return getNumberUnclosedRequestsByStatuses(Arrays.asList(RequestState.IN_WORK.getId(), RequestState.PROFILED.getId()),
                false);
    }

    
    public long getSumOfEntriesCountInManyTables(){
        try {
            long count = 0L;

            count += DaoManager.getCount(User.class,"id");
            count += DaoManager.getCount(Client.class,"id");
            count += DaoManager.getCount(AggregationLandChargesRegistry.class,"id");
            count += DaoManager.getCount(CadastralCategory.class,"id");
            count += DaoManager.getCount(Service.class,"id");
            count += DaoManager.getCount(RequestType.class,"id");
            count += DaoManager.getCount(TypeAct.class,"id");
            count += DaoManager.getCount(Formality.class,"id");
            count += DaoManager.getCount(Property.class,"id");
            count += DaoManager.getCount(VisureRTF.class,"id");
            count += DaoManager.getCount(VisureDH.class,"id");
            count += DaoManager.getCount(WLGInboxShort.class,"id");
            count += DaoManager.getCount(Document.class,"id");

            return count;
        } catch (PersistenceBeanException | IllegalAccessException e) {
            LogHelper.log(log, e);
        }
        return 0;
    }

	public boolean getNeedShowNumberUnclosedRequests() {
        return getNumberUnclosedRequests() > 0;
    }

    public boolean getNeedShowNumberUnclosedNotarialCertifications() {
        return getNumberUnclosedNotarialCertifications() > 0;
    }

    private int getNumberUnclosedRequestsByStatus(RequestState inserted, boolean onlyWithDistraintActId) {
        try {
            return DaoManager.getCount(Request.class, "id", new Criterion[]{
                    Restrictions.isNotNull(onlyWithDistraintActId ? "distraintFormality" : "id"),
                    Restrictions.eq("stateId", inserted.getId()),
                    Restrictions.or(
                            Restrictions.isNull("isDeleted"),
                            Restrictions.ne("isDeleted", true))}).intValue();
        } catch (PersistenceBeanException | IllegalAccessException e) {
            LogHelper.log(log, e);
        }
        return 0;
    }

    private int getNumberUnclosedRequestsByStatuses(List<Long> states, boolean onlyWithDistraintActId) {
        try {
            return DaoManager.getCount(Request.class, "id", new Criterion[]{
                    Restrictions.isNotNull(onlyWithDistraintActId ? "distraintFormality" : "id"),
                    Restrictions.in("stateId", states),
                    Restrictions.or(
                            Restrictions.isNull("isDeleted"),
                            Restrictions.ne("isDeleted", true))}).intValue();
        } catch (PersistenceBeanException | IllegalAccessException e) {
            LogHelper.log(log, e);
        }
        return 0;
    }

    public void getNumberEmailsReadyForSentByStatusToBeSentOrEstToSent() {
        long start = System.currentTimeMillis();

        NumberFormat formatter = new DecimalFormat("#0.00000");
        try {
            loadingEmailCountDone = false;
            List<Long> stateIds = Arrays.stream(RequestState.values())
                    .filter(r -> !r.equals(RequestState.EVADED) && !r.equals(RequestState.TO_BE_SENT) &&
                            !r.equals(RequestState.EST_TO_SENT))
                    .map(r -> r.getId())
                    .collect(Collectors.toList());

            List<Long> dashboardDataIds = DaoManager.loadIds(WLGInbox.class,
                    new Criterion[]{
                            Restrictions.or(
                                    Restrictions.isNull("requests"),
                                    Restrictions.isEmpty("requests"))
                    }
            );

            dashboardDataIds.addAll(DaoManager.loadIds(WLGInbox.class,
                    new CriteriaAlias[]{
                            new CriteriaAlias("requests", "r", JoinType.INNER_JOIN)
                    },
                    new Criterion[]{
                            Restrictions.in("r.stateId", stateIds)
                    }
            ));
            long end = System.currentTimeMillis();
            log.info("Execution time for laod list 1 " + formatter.format((end - start) / 1000d) + " seconds");

            List<WLGInboxHome> mailsWithEvaded = DaoManager.load(WLGInboxHome.class,
                    new CriteriaAlias[]{
                            new CriteriaAlias("requests", "r", JoinType.INNER_JOIN)
                    },
                    new Criterion[]{
                            Restrictions.eq("r.stateId", RequestState.EVADED.getId()),
                            Restrictions.or(Restrictions.eq("r.isDeleted", Boolean.FALSE),
                                    Restrictions.isNull("r.isDeleted"))
                    }
            );
            end = System.currentTimeMillis();
            log.info("Execution time for laod list 2 " + formatter.format((end - start) / 1000d) + " seconds");
            dashboardDataIds.addAll(mailsWithEvaded
                    .stream()
                    .filter(w -> w.getRequests()
                            .stream()
                            .filter(r -> r.isDeletedRequest() && r.getStateId() != null
                                    && !r.getStateId().equals(3l))
                            .findFirst().orElse(null) == null)
                    .map(w -> w.getId())
                    .collect(Collectors.toList()));

            end = System.currentTimeMillis();
            log.info("Execution time for laod list 3 " + formatter.format((end - start) / 1000d) + " seconds");
            List<Criterion> restrictions = new ArrayList<>();
            if(!ValidationHelper.isNullOrEmpty(dashboardDataIds)){
                restrictions.add(Restrictions.not(Restrictions.in("id",dashboardDataIds)));
            }
            restrictions.add(Restrictions.ne("state",MailManagerStatuses.CANCELED.getId()));
            SessionHelper.put("MAIL_MANAGER_LIST_TO_BE_SENT_R", restrictions);
            setNumberEmailsReadyForSent(DaoManager.getCount(WLGInbox.class, "id",
                    restrictions.toArray(new Criterion[0])).intValue());
            end = System.currentTimeMillis();
            log.info("Execution time for laod list " + formatter.format((end - start) / 1000d) + " seconds");
        } catch (PersistenceBeanException | IllegalAccessException e) {
            LogHelper.log(log, e);
        }finally {
            loadingEmailCountDone = true;
        }


    }
    
    private int getNumberUnclosedInserted() {
        try {
            setRequestCounts( DaoManager.getCount(Request.class, "id", new Criterion[]{
                    Restrictions.isNotNull("id"),
                    Restrictions.eq("stateId", RequestState.INSERTED.getId()),
                    Restrictions.or(
                            Restrictions.isNull("isDeleted"),
                            Restrictions.ne("isDeleted", true))}).intValue());
            return getRequestCounts();
        } catch (PersistenceBeanException | IllegalAccessException e) {
            LogHelper.log(log, e);
        }
        return 0;
    }

    public String getCountReceivedNewMailsQuery() {
        String states = Arrays.stream(MailManagerStatuses.values()).filter(MailManagerStatuses::isNeedShow)
                .map(MailManagerStatuses::getId).map(l -> Long.toString(l)).collect(Collectors.joining(", "));
        return "select count(*) from wlg_inbox outt where not exists " +
                "(select 1 from wlg_inbox_read inn where inn.user_id=" + getCurrentUser().getId() + " and inn.wlg_inbox_id=outt.id) " +
                "and folder_id is null and state_id in (" + states + ") and server_id = "
                + ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.RECEIVED_SERVER_ID).getValue();
    }

    public String getCountSentMailsQuery() {
        return "select count(*) from wlg_inbox outt where " +
                "folder_id is null and server_id = "
                + ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.SENT_SERVER_ID).getValue();
    }

    public String getCountDraftMailsQuery() {
        String states = Arrays.stream(MailManagerStatuses.values()).filter(MailManagerStatuses::isNeedShow)
                .map(MailManagerStatuses::getId).map(l -> Long.toString(l)).collect(Collectors.joining(", "));
        return "select count(*) from wlg_inbox outt where " +
                "folder_id is null and state_id in (" + states + ") and server_id is null";
    }

    public void updateOverLayMode(){
        String overlayMode = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("overlay_mode");
        if(StringUtils.isNotBlank(overlayMode)){
            setOverLayMode(Boolean.parseBoolean(overlayMode));
        }else {
            setOverLayMode(Boolean.TRUE);
        }
        RequestContext.getCurrentInstance().update("overLayModeData");
    }

    public String getCountStorageMailsQuery() {
        return "select count(*) from wlg_inbox outt where folder_id is null and state_id in ("
                + MailManagerStatuses.CANCELED.getId() + ", " + MailManagerStatuses.DELETED.getId() + ")";
    }

    public static long getTimeToRefresh() {
        return timeToRefresh;
    }

    public void getRequestListPage() {
        RedirectHelper.goTo(PageTypes.REQUEST_LIST);
    }

    public void getNotarialCertificationListPage(){
        RedirectHelper.goTo(PageTypes.NOTARIAL_CERTIFICATION_LIST);
    }

    public void getDatabaseListPage() {
        RedirectHelper.goTo(PageTypes.DATABASE_LIST);
    }

    public void cleanFiltersOnRequestManager() {
        SessionHelper.removeObject("searchLastName");
        SessionHelper.removeObject("searchFiscalCode");
        SessionHelper.removeObject("searchCreateUser");
    }

    public void cleanFiltersOnDatabaseListSubjectTable() {
        SessionHelper.removeObject("filtersTableSubject");
    }

    public Integer getMailCounts() {
        return mailCounts;
    }

    public void setMailCounts(Integer mailCounts) {
        this.mailCounts = mailCounts;
    }

    public Integer getRequestCounts() {
        return requestCounts;
    }

    public void setRequestCounts(Integer requestCounts) {
        this.requestCounts = requestCounts;
    }

    public Boolean getOverLayMode() {
        return overLayMode;
    }

    public void setOverLayMode(Boolean overLayMode) {
        this.overLayMode = overLayMode;
    }

    public void getBillingListPage() {
        RedirectHelper.goTo(PageTypes.BILLING_LIST);
    }

    public void getBillingListOldPage() {
        RedirectHelper.goTo(PageTypes.BILLING_LIST_OLD);
    }
    
    public Integer getNumberEmailsReadyForSent() {
		return numberEmailsReadyForSent;
	}

	public void setNumberEmailsReadyForSent(Integer numberEmailsReadyForSent) {
		this.numberEmailsReadyForSent = numberEmailsReadyForSent;
	}

    public Boolean getLoadingEmailCountDone() {
        return loadingEmailCountDone;
    }

    public void setLoadingEmailCountDone(Boolean loadingEmailCountDone) {
        this.loadingEmailCountDone = loadingEmailCountDone;
    }
    public void cleanFiltersOnMailManager() {
        SessionHelper.removeObject("MAIL_MANAGER_LIST_TO_BE_SENT");
        SessionHelper.removeObject("MAIL_MANAGER_LIST_TO_BE_SENT_R");
        SessionHelper.put("resetMailLoaded", "true");
    }

    public void getMailListPage() {
        RedirectHelper.goTo(PageTypes.MAIL_MANAGER_LIST);
    }

}

