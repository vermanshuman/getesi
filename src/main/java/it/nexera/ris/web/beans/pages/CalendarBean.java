package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.CalendarEvent;
import it.nexera.ris.web.beans.EntityLazyInListEditPageBean;
import lombok.Data;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.primefaces.context.RequestContext;
import org.primefaces.event.ScheduleEntryMoveEvent;
import org.primefaces.event.ScheduleEntryResizeEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@ManagedBean
@RequestScoped
@Data
public class CalendarBean extends EntityLazyInListEditPageBean<CalendarEvent> implements Serializable {


    private static final long serialVersionUID = -309048299017214344L;

    private ScheduleModel eventModel;

    private ScheduleEvent event = new DefaultScheduleEvent();

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        eventModel = new DefaultScheduleModel();
        List<CalendarEvent> calendarList = DaoManager.load(CalendarEvent.class, new Criterion[]{
                Restrictions.eq("createUserId", getCurrentUser().getId())});
        for (CalendarEvent ce : calendarList) {
            DefaultScheduleEvent defaultScheduleEvent = new DefaultScheduleEvent(ce.getText(), ce.getStartDate(), ce.getEndDate());
            //defaultScheduleEvent.setId(ce.getId().toString());
            eventModel.addEvent(defaultScheduleEvent);
        }
        RequestContext.getCurrentInstance().update("form");
    }


    public void onEventSelect(SelectEvent selectEvent) {
        event = (ScheduleEvent) selectEvent.getObject();
        System.out.println(event + ">>>>>>>>>>>>>>>>>>" + selectEvent.getObject());
    }

    public void onDateSelect(SelectEvent selectEvent) {
        Date endDate = (Date) selectEvent.getObject();
        event = new DefaultScheduleEvent("", (Date)selectEvent.getObject(), new DateTime(endDate).plusHours(1).toDate());;
    }

    public void onEventMove(ScheduleEntryMoveEvent event) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Event moved", "Delta:" + event.getMinuteDelta());

        addMessage(message);
    }

    public void onEventResize(ScheduleEntryResizeEvent event) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Event resized", "Delta:" + event.getDayDelta());

        addMessage(message);
    }

    private void addMessage(FacesMessage message) {
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    public void addEvent() throws ParseException, PersistenceBeanException {
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date = formatter.parse(formatter.format(getEvent().getStartDate()));
        getEntity().setDate(date);
        getEntity().setText(getEvent().getTitle());
        getEntity().setStartDate(getEvent().getStartDate());
        if (event.isAllDay()) {
            if (event.getStartDate().equals(event.getEndDate())) {
                getEntity().setEndDate(new DateTime(event.getEndDate()).plusHours(1).toDate());
            }else {
                getEntity().setEndDate(getEvent().getEndDate());
            }
        }else {
            getEntity().setEndDate(getEvent().getEndDate());
        }
        DaoManager.save(getEntity(), true);
        if(event.getId() == null)
            eventModel.addEvent(event);
        else
            eventModel.updateEvent(event);
        event = new DefaultScheduleEvent();
    }

    @Override
    protected void setEditedValues() {

    }

    @Override
    protected void validate() throws PersistenceBeanException {

    }

    @Override
    public void save() throws HibernateException, PersistenceBeanException, NumberFormatException, IOException, InstantiationException, IllegalAccessException {

    }
}
