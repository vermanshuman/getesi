package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.entities.domain.CommunicationMessage;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import it.nexera.ris.web.beans.wrappers.logic.RoleWrapper;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@ManagedBean
@ViewScoped
public class CommunicationMessageListBean extends EntityLazyListPageBean<CommunicationMessage> {

    private Date dateFrom;

    private Date dateTo;

    private String filterMessage;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, IOException {
        filterTableFromPanel();
    }

    public void createNewCommunicationMessage() {
        RedirectHelper.goTo(PageTypes.COMMUNICATION_MESSAGE_EDIT);
    }

    public void filterTableFromPanel() {
        List<Criterion> restrictions = new ArrayList<>();

        if (!ValidationHelper.isNullOrEmpty(getDateFrom())) {
            restrictions.add(Restrictions.ge("startDate",
                    DateTimeHelper.getDayStart(getDateFrom())));
        }

        if (!ValidationHelper.isNullOrEmpty(getDateTo())) {
            restrictions.add(Restrictions.le("endDate",
                    DateTimeHelper.getDayEnd(getDateTo())));
        }

        if (!ValidationHelper.isNullOrEmpty(getFilterMessage())) {
            restrictions.add(Restrictions.like("message",
                    getFilterMessage(), MatchMode.ANYWHERE));
        }

        List<Long> ids = getSessionBean().getCurrentUser().getRoles().stream()
                .map(RoleWrapper::getId)
                .collect(Collectors.toList());
        restrictions.add(Restrictions.in("ar.id", ids));

        this.loadList(CommunicationMessage.class, restrictions.toArray(new Criterion[0]), new Order[]{}, new CriteriaAlias[]{
                new CriteriaAlias("assosiatedRoles", "ar", JoinType.INNER_JOIN)
        });
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public String getFilterMessage() {
        return filterMessage;
    }

    public void setFilterMessage(String filterMessage) {
        this.filterMessage = filterMessage;
    }
}
