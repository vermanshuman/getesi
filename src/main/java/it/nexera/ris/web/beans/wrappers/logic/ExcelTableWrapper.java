package it.nexera.ris.web.beans.wrappers.logic;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import it.nexera.ris.persistence.beans.entities.domain.Request;

public class ExcelTableWrapper implements Serializable {

    private static final long serialVersionUID = 5972086648733731440L;

    public transient final Log log = LogFactory
            .getLog(ExcelTableWrapper.class);

    private String requestName;
    
    private List<String> columnNames;
    
    List<Request> requests;
    
    Map<String, String> columnValues;
    
    private Map<String, String> footerValues;
    
    private List<Request> originalRequests;

    public String getRequestName() {
        return requestName;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setRequestName(String requestName) {
        this.requestName = requestName;
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    public List<Request> getRequests() {
        return requests;
    }

    public void setRequests(List<Request> requests) {
        this.requests = requests;
    }


    public void setColumnValues(Map<String, String> columnValues) {
        this.columnValues = columnValues;
    }

    public Map<String, String> getColumnValues() {
        return columnValues;
    }

    public Map<String, String> getFooterValues() {
        return footerValues;
    }

    public void setFooterValues(Map<String, String> footerValues) {
        this.footerValues = footerValues;
    }

    /**
     * @return the originalRequests
     */
    public List<Request> getOriginalRequests() {
        return originalRequests;
    }

    /**
     * @param originalRequests the originalRequests to set
     */
    public void setOriginalRequests(List<Request> originalRequests) {
        this.originalRequests = originalRequests;
    }
}
