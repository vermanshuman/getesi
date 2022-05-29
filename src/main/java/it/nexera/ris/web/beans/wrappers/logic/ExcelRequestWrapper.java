package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.common.enums.RequestEnumTypes;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.xml.wrappers.RequestWrapper;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RequestType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ExcelRequestWrapper implements Serializable {

    private static final long serialVersionUID = 5972086648733731440L;

    public transient final Log log = LogFactory
            .getLog(ExcelRequestWrapper.class);

    private Long requestId;

    private String tempId;

    private Client client;

    private List<EstateFormality> estateFormalityList;

    private Date evasionDate;

    private Client billingClient;

    private List<Document> documentsRequest;

    private Service service;

    private List<Service> multipleServices;

    private Date createDate;

    private Subject subject;

    private String fiscalCodeVATNamber;

    private String serviceName;

    private String aggregationLandChargesRegistryName;

    private Double numberActOrSumOfEstateFormalitiesAndOther;

    private Double costCadastral;

    private String costNote;

    private String cdr;

    private String ndg;

    public String requestExcelUserName;

    private String position;

    private Request request;

    private Long userOfficeId;

    public String getCreateDateStr() {
        return DateTimeHelper.toString(getCreateDate());
    }
}
