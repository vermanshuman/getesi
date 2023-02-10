package it.nexera.ris.persistence.beans.entities.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import it.nexera.ris.common.enums.BillingTypeFields;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "report_column")
public class ReportColumn extends IndexedEntity implements Serializable {

	private static final long serialVersionUID = 1653872561284810116L;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
	private Report report;
	
	@Column(name = "type_field")
    @Enumerated(EnumType.STRING)
    private BillingTypeFields field;
	
	@Override
	public String toString() {
		return this.getId() != null ? this.getStrId() : this.getField().toString();
	}

}
