package it.nexera.ris.common.xml.wrappers;

import it.nexera.ris.common.enums.XMLElements;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.beans.entities.IEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;

/**
 * @author Vlad Strunenko
 *
 * <pre>
 *         Base wrapper for parse XML
 *                 </pre>
 */
public abstract class BaseXMLWrapper<T extends IEntity> {

    protected transient final Log log = LogFactory.getLog(getClass());

    private Long id;

    private Long createUserId;

    private Long version;

    private Date createDate;

    private Date updateDate;

    private Long updateUserId;

    public BaseXMLWrapper() {
        this.setCreateDate(new Date());
        this.setCreateUserId(0l);
        this.setUpdateDate(new Date());
        this.setUpdateUserId(0l);
        this.setVersion(1l);
    }

    /**
     * Function for create entity base on wrapper
     */
    public abstract T toEntity()
            throws InstantiationException,
            IllegalAccessException, PersistenceBeanException;

    public abstract void setField(XMLElements element, String value);

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(Long createUserId) {
        this.createUserId = createUserId;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public Long getUpdateUserId() {
        return updateUserId;
    }

    public void setUpdateUserId(Long updateUserId) {
        this.updateUserId = updateUserId;
    }

}
