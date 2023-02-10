package it.nexera.ris.web.beans.wrappers;

import java.util.Date;
import java.util.List;

import it.nexera.ris.persistence.beans.entities.domain.dictionary.RequestType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;
import org.hibernate.HibernateException;

import it.nexera.ris.common.enums.SubjectType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.persistence.beans.entities.domain.RequestSubject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CertificationListRequestWrapper {

	private Date createDate;
	private String clientName;
	private String actType;
	private String conservatory;
	private String subjectNames;
	private String managers;
	private String fiduciary;
	private String note;
	private Request request;
	private String textInVisura;
	private String stateDescription;
	private String clientFiduciary;
	private Long managerId;
	private String clientNameProfessional;
	private Boolean haveMail;
	private String serviceName;
	private RequestType requestType;
	private String iconStyleClass;
	private String requestTypeName;
	private List<String> multipleServices;
	private Long requestId;
	private Boolean manageCertification;

	public String getSubjectName(RequestSubject requestSubject) {
		String subjectName = "";
		if(!ValidationHelper.isNullOrEmpty(requestSubject)) {
			if(!ValidationHelper.isNullOrEmpty(requestSubject.getSubject().getTypeId()) && requestSubject.getSubject().getTypeId().equals(SubjectType.LEGAL_PERSON.getId())) {
				subjectName = requestSubject.getSubject().getBusinessName();
			} else {
				String subject = requestSubject.getSubject().getSurname() + " " + requestSubject.getSubject().getName();
				subjectName = subject.toUpperCase();
			}
		}
		return subjectName;
	}

	public String getFiscalCodeVATNumber(RequestSubject requestSubject) {
		String firstFiscalCodeVATNamber = "";
		if(!ValidationHelper.isNullOrEmpty(requestSubject)) {
			if (requestSubject.getSubject() != null) {
				if (requestSubject.getSubject().getTypeIsPhysicalPerson()) {
					firstFiscalCodeVATNamber = requestSubject.getSubject().getFiscalCode();
				} else {
					firstFiscalCodeVATNamber = requestSubject.getSubject().getNumberVAT();
				}
			}
		}
		return firstFiscalCodeVATNamber;
	}

	public String getTextInVisura() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
            Request request = DaoManager.get(Request.class, this.getRequestId());
            if(!ValidationHelper.isNullOrEmpty(request.getSpecialFormality())
                    && !ValidationHelper.isNullOrEmpty(request.getSpecialFormality().getTextInVisura())) {
                textInVisura = request.getSpecialFormality().getTextInVisura();
            }
        return textInVisura;
    }
}
