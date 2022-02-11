package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.MailManagerStatuses;
import it.nexera.ris.common.enums.MailManagerTypes;
import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.web.beans.EntityLazyInListEditPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@ManagedBean(name = "mailManagerFolderBean")
@ViewScoped
public class MailManagerFolderBean extends EntityLazyInListEditPageBean<WLGFolder> implements Serializable {

    private static final long serialVersionUID = 1349877256753155991L;

    private Date dateFrom;

    private Date dateTo;

    private List<SelectItem> searchStates;

    private Long selectedSearchStateId;

    private String filterAll;

    private Date dateFromInner;

    private Date dateToInner;

    private Long selectedSearchStateInnerId;

    private String filterAllInner;

    private WLGFolder selectedFolder;
    
    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, IOException {
        setSearchStates(ComboboxHelper.fillList(MailManagerStatuses.class, true));
        this.loadList(WLGFolder.class, new Criterion[]{
                Restrictions.or(
                        Restrictions.ne("defaultFolder", true),
                        Restrictions.isNull("defaultFolder"))
        }, reloadFilters().toArray(new Criterion[0]), new Order[]{
                Order.asc("name")
        });
    }

    @Override
    protected void setEditedValues() {

    }

    @Override
    protected void validate() throws PersistenceBeanException {
        if (ValidationHelper.isNullOrEmpty(getEntity().getName())) {
            addRequiredFieldException("name");
        }
    }

    @Override
    public void save() throws HibernateException, PersistenceBeanException, NumberFormatException, IOException, InstantiationException, IllegalAccessException {
        getEntity().setUser(DaoManager.get(User.class, getCurrentUser().getId()));
        DaoManager.save(getEntity());
    }

    public void gotoEmailView() {
        String id = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("param");
        if (!ValidationHelper.isNullOrEmpty(id)) {
            RedirectHelper.goToOnlyView(PageTypes.MAIL_MANAGER_VIEW, Long.parseLong(id));
        }
    }

    @Override
    protected void deleteEntityInternal(Long id) throws HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {
        if (id == null || id == 0L) {
            return;
        }
        List<WLGInbox> mails = DaoManager.load(WLGInbox.class, new Criterion[]{
                Restrictions.eq("folder.id", id)
        });
        for (WLGInbox mail : mails) {
            for (Request request : mail.getRequests()) {
                request.setMail(null);
                DaoManager.save(request);
            }
            DaoManager.remove(mail);
        }
        DaoManager.remove(WLGFolder.class, id);
    }

    public void deleteMail() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        String id = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("param");
        if (!ValidationHelper.isNullOrEmpty(id)) {
            WLGInbox mail = DaoManager.get(WLGInbox.class, id);
            for (Request request : mail.getRequests()) {
                request.setMail(null);
                DaoManager.save(request, true);
            }
            DaoManager.remove(mail, true);
        }
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

    public void filterTableFromPanel() {
        this.loadList(WLGFolder.class, new Criterion[]{}, reloadFilters().toArray(new Criterion[0]), new Order[]{});
    }

    public void filterTableInExpansion() {
        getSelectedFolder().setAdditionalCriterion(reloadInnerFilter().toArray(new Criterion[0]));
    }

    private List<Criterion> reloadInnerFilter() {
        List<Criterion> restrictions = new ArrayList<>();

        if (!ValidationHelper.isNullOrEmpty(getDateFromInner())) {
            restrictions.add(Restrictions.ge("sendDate",
                    DateTimeHelper.getDayStart(getDateFromInner())));
        }

        if (!ValidationHelper.isNullOrEmpty(getDateToInner())) {
            restrictions.add(Restrictions.le("sendDate",
                    DateTimeHelper.getDayEnd(getDateToInner())));
        }

        if (!ValidationHelper.isNullOrEmpty(getFilterAllInner())) {
            restrictions.add(getFilterTextCriterion(getFilterAllInner()));
        }

        if (!ValidationHelper.isNullOrEmpty(getSelectedSearchStateInnerId())) {
            restrictions.add(getFilterStateCriterion(getSelectedSearchStateInnerId()));
        }
        return restrictions;
    }

    private List<Criterion> reloadFilters() {
        List<Criterion> restrictions = new ArrayList<>();

        if (!ValidationHelper.isNullOrEmpty(getDateFrom())) {
            restrictions.add(Restrictions.ge("sendDate",
                    DateTimeHelper.getDayStart(getDateFrom())));
        }

        if (!ValidationHelper.isNullOrEmpty(getDateTo())) {
            restrictions.add(Restrictions.le("sendDate",
                    DateTimeHelper.getDayEnd(getDateTo())));
        }

        if (!ValidationHelper.isNullOrEmpty(getFilterAll())) {
            restrictions.add(getFilterTextCriterion(getFilterAll()));
        }

        if (!ValidationHelper.isNullOrEmpty(getSelectedSearchStateId())) {
            restrictions.add(getFilterStateCriterion(getSelectedSearchStateId()));
        }
        return restrictions;
    }

    private Criterion getFilterStateCriterion(Long stateId) {
        MailManagerStatuses state = MailManagerStatuses.findById(this.getSelectedSearchStateId());
        List<Long> idList = new ArrayList<>();
        try {
            idList = DaoManager.loadField(ReadWLGInbox.class, "mailId", Long.class, new Criterion[]{
                    Restrictions.eq("userId", UserHolder.getInstance().getCurrentUser().getId())
            });
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        if (state != null && MailManagerStatuses.READ == state) {
            return Restrictions.and(
                    Restrictions.in("id", idList),
                    Restrictions.eq("state", MailManagerStatuses.NEW.getId()));
        } else if (state != null && MailManagerStatuses.NEW == state) {
            return Restrictions.and(
                    Restrictions.not(Restrictions.in("id", idList)),
                    Restrictions.eq("state", getSelectedSearchStateId()));
        } else {
            return Restrictions.eq("state", getSelectedSearchStateId());
        }
    }

    private Criterion getFilterTextCriterion(String text) {
        List<WLGExport> wlgExports = null;
        try {
            String str = "destination_path REGEXP '[^\\\\" +
                    "]*[a-zA-ZàèéìíîòóùúÀÈÉÌÍÎÒÓÙÚ0-9_]*" +
                    shieldingQuery(text) +
                    "[a-zA-ZàèéìíîòóùúÀÈÉÌÍÎÒÓÙÚ0-9_]*\\.?[a-zA-ZàèéìíîòóùúÀÈÉÌÍÎÒÓÙÚ0-9_]*$'";
            wlgExports = DaoManager.load(WLGExport.class, new Criterion[]{Restrictions.sqlRestriction(str)});
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        List<Long> listId = wlgExports != null ? wlgExports.stream()
                .map(f -> Long.parseLong(f.getSourcePath().substring(f.getSourcePath().lastIndexOf(File.separator) + 1)))
                .collect(Collectors.toList()) : null;
        if (!ValidationHelper.isNullOrEmpty(listId)) {
            return Restrictions.or(Restrictions.like("emailTo",
                    text, MatchMode.ANYWHERE), Restrictions.like("emailFrom",
                    text, MatchMode.ANYWHERE), Restrictions.like("emailSubject",
                    text, MatchMode.ANYWHERE), Restrictions.like("emailBody",
                    text, MatchMode.ANYWHERE), Restrictions.in("id", listId));
        } else {
            return Restrictions.or(Restrictions.like("emailTo",
                    text, MatchMode.ANYWHERE), Restrictions.like("emailFrom",
                    text, MatchMode.ANYWHERE), Restrictions.like("emailSubject",
                    text, MatchMode.ANYWHERE), Restrictions.like("emailBody",
                    text, MatchMode.ANYWHERE));
        }
    }

    public void moveMail() throws Exception {
        
        String id = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("selectedmailid");
        if (!ValidationHelper.isNullOrEmpty(id)) {
            WLGInbox inbox = DaoManager.get(WLGInbox.class, id);
            if(inbox != null) {
                inbox.setFolder(null);
                DaoManager.save(inbox, true);
            }
        }
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

    public List<SelectItem> getSearchStates() {
        return searchStates;
    }

    public void setSearchStates(List<SelectItem> searchStates) {
        this.searchStates = searchStates;
    }

    public Long getSelectedSearchStateId() {
        return selectedSearchStateId;
    }

    public void setSelectedSearchStateId(Long selectedSearchStateId) {
        this.selectedSearchStateId = selectedSearchStateId;
    }

    public String getFilterAll() {
        return filterAll;
    }

    public void setFilterAll(String filterAll) {
        this.filterAll = filterAll;
    }

    public Date getDateFromInner() {
        return dateFromInner;
    }

    public void setDateFromInner(Date dateFromInner) {
        this.dateFromInner = dateFromInner;
    }

    public Date getDateToInner() {
        return dateToInner;
    }

    public void setDateToInner(Date dateToInner) {
        this.dateToInner = dateToInner;
    }

    public Long getSelectedSearchStateInnerId() {
        return selectedSearchStateInnerId;
    }

    public void setSelectedSearchStateInnerId(Long selectedSearchStateInnerId) {
        this.selectedSearchStateInnerId = selectedSearchStateInnerId;
    }

    public String getFilterAllInner() {
        return filterAllInner;
    }

    public void setFilterAllInner(String filterAllInner) {
        this.filterAllInner = filterAllInner;
    }

    public WLGFolder getSelectedFolder() {
        return selectedFolder;
    }

    public void setSelectedFolder(WLGFolder selectedFolder) {
        this.selectedFolder = selectedFolder;
    }
}
