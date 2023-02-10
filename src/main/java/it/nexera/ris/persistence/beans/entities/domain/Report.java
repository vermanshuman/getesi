package it.nexera.ris.persistence.beans.entities.domain;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "report")
public class Report extends IndexedEntity implements Serializable {

	private static final long serialVersionUID = 721906073521180487L;

	@Column(name = "name")
    private String name;
	
	@OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
	@LazyCollection(LazyCollectionOption.FALSE)
    private List<ReportColumn> reportColumns;
}
