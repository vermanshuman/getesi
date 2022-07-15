package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "calendar_event")
@Data
public class CalendarEvent extends IndexedEntity {

    @Column(name = "date", nullable = false)
    private Date date;

    @Column(name = "start_date", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", nullable = false)
    private Date startDate;

    @Column(name = "end_date", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", nullable = false)
    private Date endDate;

    @Column(name = "text", columnDefinition = "TEXT")
    private String text;
}
