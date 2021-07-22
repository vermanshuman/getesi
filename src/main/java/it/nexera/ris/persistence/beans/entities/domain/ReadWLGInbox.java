package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "wlg_inbox_read")
public class ReadWLGInbox extends IndexedEntity {

    private static final long serialVersionUID = 5583933679572280262L;

    @Column(name = "wlg_inbox_id")
    private Long mailId;

    @Column(name = "user_id")
    private Long userId;

    public ReadWLGInbox() {
    }

    public ReadWLGInbox(Long mailId, Long userId) {
        this.mailId = mailId;
        this.userId = userId;
    }

    public Long getMailId() {
        return mailId;
    }

    public void setMailId(Long mailId) {
        this.mailId = mailId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
