package it.nexera.ris.persistence.beans.entities.domain.readonly;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "wlg_inbox")
public class WLGInboxHome extends IndexedEntity {

    public transient final Log log = LogFactory.getLog(WLGInboxHome.class);

    private static final long serialVersionUID = -1797608425636926549L;

    @OneToMany(mappedBy = "mail", fetch = FetchType.LAZY)
    private List<RequestMailShort> requests;
}
