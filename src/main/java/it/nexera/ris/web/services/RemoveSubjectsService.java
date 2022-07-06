package it.nexera.ris.web.services;

import it.nexera.ris.common.enums.SessionNames;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.common.utils.ProcessMonitor;
import it.nexera.ris.persistence.PersistenceSession;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.web.services.base.BaseDBService;
import it.nexera.ris.web.services.base.BaseService;
import it.nexera.ris.web.services.base.ThreadFactoryEx;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.primefaces.push.EventBus;
import org.primefaces.push.EventBusFactory;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Getter
@Setter
public class RemoveSubjectsService extends BaseService implements Serializable{

    private static final long serialVersionUID = -166231791853726478L;

    public RemoveSubjectsService() {
        super("RemoveSubjectsService");
    }

    private ProcessMonitor processMonitor;

    private boolean showProgressPanel;

    @Override
    public void start() {
        setExecutorService(Executors.newSingleThreadExecutor(new ThreadFactoryEx(this.name)));
        setStopFlag(false);
        setShowProgressPanel(false);
        setNotWaitBeforeStop(true);
        getExecutorService().execute(this);
        socketPush();
    }

    @Override
    protected void routineFuncInternal() {
        getProcessMonitor().setStatusStr("Clear Duplicate Subjects...");
        PersistenceSession ps = null;
        Transaction tr = null;
        try {
            ps = new PersistenceSession();
            Session session = ps.getSession();
            List<BigInteger> ids1 = session.createSQLQuery("select min(id)\n" +
                    "  from subject\n" +
                    "  where fiscal_code is not null and fiscal_code !=''\n" +
                    "  group by fiscal_code, birth_city_id, birth_province_id, birth_date, first_name, last_name\n" +
                    "  having count(id) > 1;").list();
            List<BigInteger> ids2 = session.createSQLQuery("select min(id)\n" +
                    "  from subject\n" +
                    "  where number_vat is not null and number_vat !=''\n" +
                    "  group by number_vat, birth_city_id, birth_province_id\n" +
                    "  having count(id) > 1;").list();
            if (!ValidationHelper.isNullOrEmpty(ids1) || !ValidationHelper.isNullOrEmpty(ids2)) {
                for (long id : ids1.stream().map(BigInteger::longValue).collect(Collectors.toList())) {
                    Subject subject = ConnectionManager.get(Subject.class, id, session);
                    if(!checkSubjectPersonFields(subject)) {
                        List<Subject> subjectsToRemove = ConnectionManager.load(Subject.class, new Criterion[]{
                                Restrictions.eq("fiscalCode", subject.getFiscalCode()),
                                Restrictions.eq("birthProvince.id", subject.getBirthProvince() == null
                                        ? 0L : subject.getBirthProvince().getId()),
                                Restrictions.eq("birthCity.id", subject.getBirthCity() == null
                                        ? 0L : subject.getBirthCity().getId()),
                                Restrictions.eq("birthDate", subject.getBirthDate()),
                                Restrictions.eq("name", subject.getName()),
                                Restrictions.eq("surname", subject.getSurname()),
                                Restrictions.ne("id", subject.getId())
                        }, session);
                        updateSubjectReference(subject, subjectsToRemove, session);
                    }
                }
                for (long id : ids2.stream().map(BigInteger::longValue).collect(Collectors.toList())) {
                    Subject subject = ConnectionManager.get(Subject.class, id, session);
                    if(!checkSubjectFields(subject)) {
                        List<Subject> subjectsToRemove = ConnectionManager.load(Subject.class, new Criterion[]{
                                Restrictions.eq("numberVAT", subject.getNumberVAT()),
                                Restrictions.eq("businessName", subject.getBusinessName()),
                                Restrictions.eq("birthProvince.id", subject.getBirthProvince() == null
                                        ? 0L : subject.getBirthProvince().getId()),
                                Restrictions.eq("birthCity.id", subject.getBirthCity() == null
                                        ? 0L : subject.getBirthCity().getId()),
                                Restrictions.ne("id", subject.getId())
                        }, session);
                        updateSubjectReference(subject, subjectsToRemove, session);
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
        } finally {
            if (tr != null && tr.isActive() && !tr.wasRolledBack()) {
                try {
                    tr.commit();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    if (tr.isActive()) {
                        tr.rollback();
                    }
                }
            }
            if (ps != null) {
                ps.closeSession();
            }
            getProcessMonitor().resetCounters();
            postRoutineFunc();
            stop();
        }
    }

    @Override
    protected int getPollTimeKey() {
        return 10;
    }

    private boolean checkSubjectPersonFields(Subject sub) {
        boolean result = false;
        if (sub.getFiscalCode() == null || sub.getFiscalCode().equalsIgnoreCase("null")) {
            result = true;
        }
        if (sub.getName() == null || sub.getName().equalsIgnoreCase("null")) {
            result = true;
        }
        if (sub.getSurname() == null || sub.getSurname().equalsIgnoreCase("null")) {
            result = true;
        }
        if (sub.getBirthDate() == null) {
            result = true;
        }
        if (sub.getBirthProvince() == null) {
            result = true;
        }
        if (sub.getBirthCity() == null) {
            result = true;
        }
        return result;
    }

    private boolean checkSubjectFields(Subject sub) {
        boolean result = false;
        if (sub.getNumberVAT() == null || sub.getNumberVAT().equalsIgnoreCase("null")) {
            result = true;
        }
        if (sub.getBusinessName() == null || sub.getBusinessName().equalsIgnoreCase("null")) {
            result = true;
        }
        if (sub.getBirthProvince() == null) {
            result = true;
        }
        if (sub.getBirthCity() == null) {
            result = true;
        }
        return result;
    }

    private void updateSubjectReference(Subject subject, List<Subject> subjectsToRemove, Session session) {
        if (!ValidationHelper.isNullOrEmpty(subjectsToRemove)) {
            List<Request> requestsToEdit = ConnectionManager.load(Request.class, new Criterion[]{
                    Restrictions.in("subject.id", subjectsToRemove.stream()
                            .map(Subject::getId).collect(Collectors.toList()))
            }, session);
            for (Request request : requestsToEdit) {
                request.setSubject(subject);
                ConnectionManager.save(request, session);
            }

            requestsToEdit = ConnectionManager.load(Request.class, new CriteriaAlias[]{
                    new CriteriaAlias("requestSubjects", "rs", JoinType.INNER_JOIN)
            }, new Criterion[]{
                    Restrictions.in("rs.subject.id", subjectsToRemove.stream()
                            .map(Subject::getId).collect(Collectors.toList()))
            }, session);
            for (Request request : requestsToEdit) {
                request.getSubjectList().removeIf(s -> subjectsToRemove.stream().anyMatch(sr -> sr.getId().equals(s.getId())));
                request.getSubjectList().add(subject);
                ConnectionManager.save(request, session);
            }

            List<Relationship> relationshipsToEdit = ConnectionManager.load(Relationship.class, new Criterion[]{
                    Restrictions.in("subject.id", subjectsToRemove.stream()
                            .map(Subject::getId).collect(Collectors.toList()))
            }, session);
            for (Relationship relationship : relationshipsToEdit) {
                relationship.setSubject(subject);
                ConnectionManager.save(relationship, session);
            }
            List<SectionC> sectionCToEdit = ConnectionManager.load(SectionC.class,
                    new CriteriaAlias[]{
                            new CriteriaAlias("subject", "sub", JoinType.LEFT_OUTER_JOIN)},
                    new Criterion[]{
                            Restrictions.in("sub.id", subjectsToRemove.stream()
                                    .map(Subject::getId).collect(Collectors.toList()))
                    }, session);
            for (SectionC sectionC : sectionCToEdit) {
                if(!ValidationHelper.isNullOrEmpty(sectionC.getSubject())){
                    sectionC.getSubject().removeIf(subjectsToRemove::contains);
                    sectionC.getSubject().add(subject);
                } else {
                    sectionC.setSubject(new ArrayList<>());
                    sectionC.getSubject().add(subject);
                }
                ConnectionManager.save(sectionC, session);
            }
            List<Formality> formalitysToRemove = ConnectionManager.load(Formality.class, new Criterion[]{
                    Restrictions.in("subject.id", subjectsToRemove.stream()
                            .map(Subject::getId).collect(Collectors.toList()))
            }, session);
            for (Formality formality : formalitysToRemove) {
                formality.setSubject(subject);
                ConnectionManager.save(formality, session);
            }
            for (Subject sub : subjectsToRemove) {
                ConnectionManager.remove(sub, session);
            }
        }
    }

    @Override
    protected void postRoutineFunc() {
        super.postRoutineFunc();
        setShowProgressPanel(false);
        socketPush();
    }

    private void socketPush() {
        EventBus eventBus = EventBusFactory.getDefault().eventBus();
        eventBus.publish("/notify", "");
    }
}