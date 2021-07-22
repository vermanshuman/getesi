package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.enums.MailManagerStatuses;
import it.nexera.ris.common.enums.MailManagerTypes;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.common.EntityLazyListModel;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.primefaces.model.LazyDataModel;

import javax.persistence.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Entity
@Table(name = "wlg_folder")
public class WLGFolder extends IndexedEntity {

    private static final long serialVersionUID = 6055058529674945604L;

    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "folder")
    private List<WLGInbox> emails;

    @Column(name = "default_folder")
    private Boolean defaultFolder;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_type")
    private MailManagerTypes mailType;

    @Transient
    private Criterion[] additionalCriterion;

    @Override
    public String toString() {
        return name;
    }

    public LazyDataModel<WLGInbox> getEmailsLazy() {
        List<Criterion> restrictions = new LinkedList<>();
        if (getAdditionalCriterion() != null && getAdditionalCriterion().length != 0) {
            Collections.addAll(restrictions, getAdditionalCriterion());
        }
        if (ValidationHelper.isNullOrEmpty(getDefaultFolder()) || !getDefaultFolder()) {
            restrictions.add(Restrictions.eq("folder.id", getId()));
        } else {
            switch (getMailType()) {
                case SENT:
                    restrictions.add(Restrictions.or(
                            Restrictions.eq("folder.id", getId()),
                            Restrictions.and(
                                    Restrictions.isNull("folder"),
                                    Restrictions.eq("serverId", Long.parseLong(ApplicationSettingsHolder
                                            .getInstance().getByKey(ApplicationSettingsKeys.SENT_SERVER_ID).getValue()))
                            )
                    ));
                    break;

                case RECEIVED:
                    restrictions.add(Restrictions.or(
                            Restrictions.eq("folder.id", getId()),
                            Restrictions.and(
                                    Restrictions.isNull("folder"),
                                    Restrictions.eq("serverId", Long.parseLong(ApplicationSettingsHolder
                                            .getInstance().getByKey(ApplicationSettingsKeys.RECEIVED_SERVER_ID).getValue()))
                            )
                    ));
                    break;

                case DRAFT:
                    restrictions.add(Restrictions.or(
                            Restrictions.eq("folder.id", getId()),
                            Restrictions.and(
                                    Restrictions.isNull("folder"),
                                    Restrictions.isNull("serverId"),
                                    Restrictions.eq("state", MailManagerStatuses.NEW.getId())
                            )
                    ));
                    break;
                case STORAGE:
                    restrictions.add(Restrictions.or(
                            Restrictions.eq("folder.id", getId()),
                            Restrictions.and(
                                    Restrictions.isNull("folder"),
                                    Restrictions.eq("state", MailManagerStatuses.DELETED.getId())
                            )
                    ));
                    break;
            }
        }
        return new EntityLazyListModel<>(WLGInbox.class, restrictions.toArray(new Criterion[0]), new Order[]{});
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<WLGInbox> getEmails() {
        return emails;
    }

    public void setEmails(List<WLGInbox> emails) {
        this.emails = emails;
    }

    public Boolean getDefaultFolder() {
        return defaultFolder;
    }

    public void setDefaultFolder(Boolean defaultFolder) {
        this.defaultFolder = defaultFolder;
    }

    public MailManagerTypes getMailType() {
        return mailType;
    }

    public void setMailType(MailManagerTypes mailType) {
        this.mailType = mailType;
    }

    public Criterion[] getAdditionalCriterion() {
        return additionalCriterion;
    }

    public void setAdditionalCriterion(Criterion[] additionalCriterion) {
        this.additionalCriterion = additionalCriterion;
    }
}
