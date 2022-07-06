package it.nexera.ris.persistence.beans.entities.domain.dictionary;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "dic_cadastral_topology")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "data_entry")
public class CadastralTopology extends IndexedEntity {

    private static final long serialVersionUID = 1745373036516203743L;

    @Column(name = "article")
    private String article;

    @Column(name = "description")
    private String description;

    public String getArticle() {
        return article;
    }

    public void setArticle(String article) {
        this.article = article;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
