package it.nexera.ris.web.beans.wrappers.logic;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import it.nexera.ris.persistence.beans.entities.domain.Client;

@Data
public class ExcelDataWrapper implements Serializable {

    private static final long serialVersionUID = -1862635460398600519L;
    
    public transient final Log log = LogFactory
            .getLog(ExcelDataWrapper.class);

    private String ndg;
    
    private Long reportn;
    
    private String fatturaDiRiferimento; 
    
    private String referenceRequest;
    
    private Client clientInvoice;
    
    private List<Client> managers;
     
    private Client clientFiduciary;
    
    private Long fatturan;
    
    private Date data;
    
    private String office;

    private String invoiceNumber;

    private String fiduciary;
    
    private String documentNote;

    private Boolean showReport;
}
