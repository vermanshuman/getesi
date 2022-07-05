package it.nexera.ris.web.beans.wrappers.logic;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import it.nexera.ris.persistence.beans.entities.domain.Client;
import lombok.Data;

@Data
public class ExcelDataWrapper implements Serializable {

	private static final long serialVersionUID = -1862635460398600519L;

	public transient final Log log = LogFactory.getLog(ExcelDataWrapper.class);

	private String ndg;

	private Long reportn;

	private String fatturaDiRiferimento;

	private String referenceRequest;

	private Client clientInvoice;

	private List<Client> managers;

	private Client clientFiduciary;

	private String fiduciary;

	private Long fatturan;

	private Date data;

	private String office;

	private String invoiceNumber;

//    public String getNdg() {
//        return ndg;
//    }
//    public Long getReportn() {
//        return reportn;
//    }
//    public void setNdg(String ndg) {
//        this.ndg = ndg;
//    }
//    public void setReportn(Long reportn) {
//        this.reportn = reportn;
//    }
//    public String getFatturaDiRiferimento() {
//        return fatturaDiRiferimento;
//    }
//    public void setFatturaDiRiferimento(String fatturaDiRiferimento) {
//        this.fatturaDiRiferimento = fatturaDiRiferimento;
//    }
//    public String getReferenceRequest() {
//        return referenceRequest;
//    }
//    public void setReferenceRequest(String referenceRequest) {
//        this.referenceRequest = referenceRequest;
//    }
//
//    public Client getClientInvoice() {
//        return clientInvoice;
//    }
//
//    public void setClientInvoice(Client clientInvoice) {
//        this.clientInvoice = clientInvoice;
//    }
//
//    public List<Client> getManagers() {
//        return managers;
//    }
//
//    public void setManagers(List<Client> managers) {
//        this.managers = managers;
//    }
//
//    public Client getClientFiduciary() {
//        return clientFiduciary;
//    }
//
//    public void setClientFiduciary(Client clientFiduciary) {
//        this.clientFiduciary = clientFiduciary;
//    }
//    public Long getFatturan() {
//        return fatturan;
//    }
//    public void setFatturan(Long fatturan) {
//        this.fatturan = fatturan;
//    }
//    public Date getData() {
//        return data;
//    }
//    public void setData(Date data) {
//        this.data = data;
//    }
//    public String getOffice() {
//        return office;
//    }
//    public void setOffice(String office) {
//        this.office = office;
//    }
}
