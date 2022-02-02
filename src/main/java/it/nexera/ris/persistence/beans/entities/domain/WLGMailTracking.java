package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "wlg_mail_tracking")
public class WLGMailTracking extends IndexedEntity {

    private static final long serialVersionUID = -1745992539485797443L;

    @Column(name = "mailbox_id", nullable = false)
    private Long mailBoxId;

    @Column(name = "send_date", nullable = false)
    private Date sendDate;

    @Column(name = "msg_uid", length = 200, nullable = false)
    private String msgUid;

    @Column(name = "size", nullable = false)
    private Long size;

    public Long getMailBoxId() {
        return mailBoxId;
    }

    public void setMailBoxId(Long mailBoxId) {
        this.mailBoxId = mailBoxId;
    }

    public Date getSendDate() {
        return sendDate;
    }

    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }

    public String getMsgUid() {
        return msgUid;
    }

    public void setMsgUid(String msgUid) {
        this.msgUid = msgUid;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

}
