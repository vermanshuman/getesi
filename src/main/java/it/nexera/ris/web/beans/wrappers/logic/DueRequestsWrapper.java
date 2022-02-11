package it.nexera.ris.web.beans.wrappers.logic;

import java.io.Serializable;

public class DueRequestsWrapper implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID = 8425534028384392731L;
    private Long requestTypeId;
    private String requestTypeName;
    private Long requestCount;
    public DueRequestsWrapper(Long requestTypeId, String requestTypeName, Long requestCount) {
        super();
        this.requestTypeId = requestTypeId;
        this.requestTypeName = requestTypeName;
        this.requestCount = requestCount;
    }
    public Long getRequestTypeId() {
        return requestTypeId;
    }
    public String getRequestTypeName() {
        return requestTypeName;
    }
    public Long getRequestCount() {
        return requestCount;
    }
}