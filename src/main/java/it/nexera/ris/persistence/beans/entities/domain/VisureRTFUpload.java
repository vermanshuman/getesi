package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.IEntity;
import lombok.Data;
import org.apache.commons.io.FilenameUtils;
import org.hibernate.HibernateException;

import javax.persistence.*;
import java.io.File;
import java.util.Date;

@Entity
@Table(name = "visurertf_upload")
@Data
public class VisureRTFUpload implements IEntity {

    private static final long serialVersionUID = -6538311160499900122L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "path")
    private String path;

    @Column(name = "update_date")
    private Date updateDate;

    @Column(name = "num_formality")
    private Long numFormality;

    @OneToOne
    @JoinColumn(name = "visurertf_id")
    private VisureRTF visureRTF;

    public String getUpdateDateString() {
        return DateTimeHelper.toString(getUpdateDate());
    }

    public String getVisureName() {
        String result = "";
        if (!ValidationHelper.isNullOrEmpty(getUpdateDate())) {
            result += getUpdateDateString();
        }
        if (!ValidationHelper.isNullOrEmpty(getNumFormality())) {
            result += " - " + getNumFormality();
        }
        return result;
    }

    public boolean getRTFExtension() {
        if (!ValidationHelper.isNullOrEmpty(getPath())) {
            File file = new File(getPath());
            String extension = FilenameUtils.getExtension(file.getName());

            if (extension != null && extension.trim().equalsIgnoreCase("rtf"))
                return true;
        }
        return false;
    }

    @Override
    public boolean isNew() {
        return false;
    }

    @Override
    public boolean isCustomId() {
        return false;
    }

    @Override
    public boolean getDeletable() throws HibernateException, PersistenceBeanException, IllegalAccessException {
        return false;
    }

    @Override
    public boolean getEditable() throws HibernateException, PersistenceBeanException, IllegalAccessException {
        return false;
    }

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}