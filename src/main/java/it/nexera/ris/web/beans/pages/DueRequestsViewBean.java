package it.nexera.ris.web.beans.pages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.enums.RequestState;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.common.helpers.SessionHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RequestType;
import it.nexera.ris.web.beans.EntityViewPageBean;
import it.nexera.ris.web.beans.wrappers.logic.DueRequestsWrapper;

@ManagedBean(name = "dueRequestsViewBean")
@ViewScoped
public class DueRequestsViewBean extends EntityViewPageBean<Request> implements Serializable {

    private static final long serialVersionUID = 822443201168769638L;

    private int days;

    private Date expirationDate;

    private List<DueRequestsWrapper> list;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
    InstantiationException, IllegalAccessException {
        days = 0;
        if (!ValidationHelper.isNullOrEmpty(getRequestParameter(RedirectHelper.DAYS_PARAMETER))) {
            try {
                days = Integer.valueOf(getRequestParameter(RedirectHelper.DAYS_PARAMETER));
            } catch (NumberFormatException e) {
                log.info(e.getMessage());
            }
            Date expirationDate = DateTimeHelper.addDays(DateTimeHelper.getNow(), days);
            setExpirationDate(expirationDate);
            Map<RequestType, Integer> fileteredRequests = new HashMap<RequestType,Integer>();
            List<Request> requests = DaoManager.load(Request.class, new Criterion[]{
                    Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                            Restrictions.isNull("isDeleted")),
                    Restrictions.ne("stateId", RequestState.EVADED.getId()),
                    days == 0 ? Restrictions.le("expirationDate",getExpirationDate()) : 
                        Restrictions.ge("expirationDate",getExpirationDate())});

            Function<Request, List<Object>> compositeKey = request ->Arrays.<Object>asList(request.getRequestType());

            Map<Object, List<Request>> collect =
                    requests.stream().collect(Collectors.groupingBy(compositeKey, Collectors.toList()));

            for (Map.Entry<Object, List<Request>> stringListEntry : collect.entrySet()) {
                fileteredRequests.put(stringListEntry.getValue().get(0).getRequestType(), stringListEntry.getValue().size());
            }
            list = new ArrayList<DueRequestsWrapper>(fileteredRequests.size());
            for (Map.Entry<RequestType, Integer> requestType : fileteredRequests.entrySet()) {
                list.add(new DueRequestsWrapper(requestType.getKey().getId(), 
                        requestType.getKey().toString(),new Long(requestType.getValue())));
            }
        }
    }

    @Override
    public void goBack() {
        RedirectHelper.goTo(PageTypes.HOME);
    }

    public void goToRequestList() {
        RedirectHelper.goTo(PageTypes.REQUEST_LIST);
    }

    public void selectRequestType(Long requestTypeId) {
        SessionHelper.put("dueRequestTypeId", requestTypeId);
        SessionHelper.put("expirationDays", days);
        goToRequestList();
    }

    public int getDays() {
        return days;
    }

    public List<DueRequestsWrapper> getDueRequests() {
        return list;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }
}