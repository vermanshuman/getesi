package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.CostManipulationHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Event;
import it.nexera.ris.web.beans.EntityLazyInListEditPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@ManagedBean(name = "eventListBean")
@ViewScoped
public class EventListBean extends EntityLazyInListEditPageBean<Event> implements Serializable {

    private static final long serialVersionUID = 7661776336325805119L;

    private ScheduleModel scheduleModel;

    private Date startDate;

    private ScheduleEvent event;

    private ScheduleEvent eventSelect;

    private boolean hiddenPanel;

    private CostManipulationHelper costManipulationHelper;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, IOException {
        setScheduleModel(new DefaultScheduleModel());
        List<Event> eventList = DaoManager.load(Event.class, new Criterion[]{
                Restrictions.eq("createUserId", getCurrentUser().getId())});
        for (Event ev : eventList) {
            getScheduleModel().addEvent(new DefaultScheduleEvent(ev.getText(), ev.getStartDate(), ev.getEndDate()));
        }
        RequestContext.getCurrentInstance().update("form");
    }

    @Override
    protected void setEditedValues() {
        try {
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            Date date = formatter.parse(formatter.format(getEvent().getStartDate()));
            getEntity().setDate(date);
            getEditedEntity().setEndDate(setTimeToDate(date, getEntity().getEndDate()));
            getEditedEntity().setStartDate(setTimeToDate(date, getEntity().getStartDate()));
            getEditedEntity().setText(getEntity().getText());
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    @Override
    protected void validate() throws PersistenceBeanException {
        if (ValidationHelper.isNullOrEmpty(getEntity().getStartDate())) {
            addRequiredFieldException("form:from");
        } else if (ValidationHelper.isNullOrEmpty(getEntity().getEndDate())) {
            addRequiredFieldException("form:to");
        } else if (getEntity().getStartDate().after(getEntity().getEndDate())) {
            addFieldException("form:to", "visitStartAfterEndTime");
        }
    }

    @Override
    public void save() throws HibernateException, PersistenceBeanException, NumberFormatException, IOException, InstantiationException, IllegalAccessException {
        try {
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            Date date = formatter.parse(formatter.format(getEvent().getStartDate()));
            getEntity().setDate(date);
            getEntity().setStartDate(setTimeToDate(date, getEntity().getStartDate()));
            getEntity().setEndDate(setTimeToDate(date, getEntity().getEndDate()));
            DaoManager.save(getEntity());
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void onDateSelect(SelectEvent selectEvent) {
        try {
            setHiddenPanel(true);
            resetFields();
            event = new DefaultScheduleEvent("", (Date) selectEvent.getObject(), (Date) selectEvent.getObject());
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            setStartDate(formatter.parse(formatter.format(getEvent().getStartDate())));
            this.loadList(Event.class, new Criterion[]
                    {Restrictions.eq("createUserId", getCurrentUser().getId()),
                            Restrictions.eq("date", getStartDate())}, new Order[]{Order.asc("startDate")});
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void onEventSelect(SelectEvent selectEvent) {
        eventSelect = (ScheduleEvent) selectEvent.getObject();
    }

    private Date setTimeToDate(Date date, Date time) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        Calendar timeC = Calendar.getInstance();
        timeC.setTime(time);
        c.set(Calendar.HOUR, timeC.get(Calendar.HOUR));
        c.set(Calendar.MINUTE, timeC.get(Calendar.MINUTE));
        return c.getTime();
    }

    public ScheduleModel getScheduleModel() {
        return scheduleModel;
    }

    public void setScheduleModel(ScheduleModel scheduleModel) {
        this.scheduleModel = scheduleModel;
    }

    public ScheduleEvent getEvent() {
        return event;
    }

    public void setEvent(ScheduleEvent event) {
        this.event = event;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public boolean getHiddenPanel() {
        return hiddenPanel;
    }

    public void setHiddenPanel(boolean hiddenPanel) {
        this.hiddenPanel = hiddenPanel;
    }

    public ScheduleEvent getEventSelect() {
        return eventSelect;
    }

    public void setEventSelect(ScheduleEvent eventSelect) {
        this.eventSelect = eventSelect;
    }

    public CostManipulationHelper getCostManipulationHelper() {
        return costManipulationHelper;
    }

    public void setCostManipulationHelper(CostManipulationHelper costManipulationHelper) {
        this.costManipulationHelper = costManipulationHelper;
    }
}
