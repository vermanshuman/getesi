package it.nexera.ris.web.beans.pages;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.primefaces.component.tabview.Tab;
import org.primefaces.model.DualListModel;

import it.nexera.ris.common.enums.BillingTypeFields;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Report;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import it.nexera.ris.web.beans.wrappers.logic.ReportColumnWrapper;
import lombok.Data;

@Data
@ManagedBean(name = "reportListBean")
@ViewScoped
public class ReportListBean extends EntityLazyListPageBean<Report> implements Serializable {

    private static final long serialVersionUID = -5108392810985886849L;
    
    private static transient final Log log = LogFactory.getLog(ReportListBean.class);
    
    private String reportName;
    
    private List<Tab> reportTabs;
    
    private DualListModel<ReportColumnWrapper> reportColumns;
    
    @Override
    public void onConstruct() {
    	try {
			loadReportTabs();
		} catch (HibernateException | IllegalAccessException | PersistenceBeanException e) {
			LogHelper.log(log, e);
		}
		List<ReportColumnWrapper> sourceReportColumns = new LinkedList<>();
		for (BillingTypeFields field : BillingTypeFields.values()) {
			sourceReportColumns.add(new ReportColumnWrapper(field));
        }
        List<ReportColumnWrapper> targetInvoiceColumns = new LinkedList<>();
        this.setReportColumns(new DualListModel<>(sourceReportColumns, targetInvoiceColumns));
    }

	@Override
	public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
			InstantiationException, IllegalAccessException, IOException {
	}
	
	public void saveReport() throws HibernateException, PersistenceBeanException {
		if(!ValidationHelper.isNullOrEmpty(getReportName())) {
			Report report = new Report();
			report.setName(getReportName());
			DaoManager.save(report, true);
			List<BillingTypeFields> fields = new ArrayList<BillingTypeFields>();
	        for(int j =0; j < this.getReportColumns().getTarget().size(); j++) {
	            ReportColumnWrapper wrapper = this.getReportColumns().getTarget().get(j);
	            if (wrapper != null) {
	                if (wrapper.getSelected()) {
	                    wrapper.getField().setReport(report);
	                    DaoManager.save(wrapper.getField(), true);
	                    fields.add(wrapper.getField().getField());
	                }
	            }
	        }
	        addNewReportTab(report);
		}
		reset();
	}
	
	public void addNewReportTab(Report report) {
		if(ValidationHelper.isNullOrEmpty(reportTabs)) {
			reportTabs = new LinkedList<>();
		}
		Tab tab = new Tab();
		tab.setTitle(report.getName());
		reportTabs.add(tab);
	}
	
	public void loadReportTabs() throws HibernateException, IllegalAccessException, PersistenceBeanException {
		reportTabs = new LinkedList<>();
		List<Report>reports = DaoManager.load(Report.class);
		if(!ValidationHelper.isNullOrEmpty(reports)) {
			for(Report report : reports) {
				Tab tab = new Tab();
				tab.setTitle(report.getName());
				reportTabs.add(tab);
			}
		}
    }
	
	private void reset() {
		setReportName(null);
		List<ReportColumnWrapper> sourceReportColumns = new LinkedList<>();
		for (BillingTypeFields field : BillingTypeFields.values()) {
			sourceReportColumns.add(new ReportColumnWrapper(field));
        }
        List<ReportColumnWrapper> targetInvoiceColumns = new LinkedList<>();
        this.setReportColumns(new DualListModel<>(sourceReportColumns, targetInvoiceColumns));
	}
	
    /*private Date filterReportListDate;

    private Long entityEditId;

    private Date dateFrom;

    private Date dateTo;

    private CostManipulationHelper costManipulationHelper;

    private Request examRequest;

    @ManagedProperty(value = "#{requestListBean}")
    private RequestListBean requestListBean;

    private Boolean hideExtraCost = Boolean.TRUE;

    private String costNote;

    private Boolean showRequestCost = Boolean.FALSE;*/
    
    /*@Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException, IllegalAccessException, IOException {
        setCostManipulationHelper(new CostManipulationHelper());
        getCostManipulationHelper().setMortgageTypeList(ComboboxHelper.fillList(MortgageType.class, false, false));
        getCostManipulationHelper().setEditable(true);

        loadList(Document.class, new Criterion[]{
                        Restrictions.eq("typeId", DocumentType.INVOICE_REPORT.getId())},
                new Order[]{Order.asc("invoiceNumber")});
    }*/

    /*public void generateXlsWithInvoicesReport() {
        try {
            CreateExcelInvoicesReportHelper helper = new CreateExcelInvoicesReportHelper();


            List<Criterion> restrictionsList = new ArrayList<>();
            if (!ValidationHelper.isNullOrEmpty(getDateFrom())) {
                restrictionsList.add(Restrictions.ge("m.createDate",
                        DateTimeHelper.getDayStart(getDateFrom())));
            }

            if (!ValidationHelper.isNullOrEmpty(getDateTo())) {
                restrictionsList.add(Restrictions.le("m.createDate",
                        DateTimeHelper.getDayEnd(getDateTo())));
            }
            restrictionsList.add(Restrictions.eq("typeId", DocumentType.INVOICE_REPORT.getId()));
            FileHelper.sendFile("InvoicesReport-" + DateTimeHelper.toStringDateWithDots(new Date()) + ".xls",
                    helper.convertInvoicesToExcel(DaoManager.load(Document.class,
                            new CriteriaAlias[]{
                                    new CriteriaAlias("mail", "m", JoinType.INNER_JOIN)
                            },
                            restrictionsList.toArray(new Criterion[0]),
                            new Order[]{Order.asc("invoiceNumber")})));

        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }*/

    /*public void filterByDate() {
        List<Criterion> restrictionsList = new ArrayList<>();
        if (!ValidationHelper.isNullOrEmpty(getFilterReportListDate())) {
            Date toDate = DateTimeHelper.getDayStart(DateTimeHelper.getTomorrowDate(getFilterReportListDate()));
            restrictionsList.add(Restrictions.ge("date", DateTimeHelper.getDayStart(getFilterReportListDate())));
            restrictionsList.add(Restrictions.lt("date", toDate));
        }
        restrictionsList.add(Restrictions.eq("typeId", DocumentType.INVOICE_REPORT.getId()));

        this.loadList(Document.class,
                restrictionsList.toArray(new Criterion[0]),
                new Order[]{Order.asc("invoiceNumber")});
    }*/

    /*public void viewExtraCost() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        viewExtraCost(false);
    }*/

    /*public void viewExtraCost(boolean recalculate) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setExamRequest(DaoManager.get(Request.class, getEntityEditId()));
        setCostNote(null);
        if(ValidationHelper.isNullOrEmpty(getExamRequest().getCostNote())) {
            try {
                List<Document> requestDocuments = DaoManager.load(Document.class,
                        new CriteriaAlias[]{new CriteriaAlias("request", "request", JoinType.INNER_JOIN)},
                        new Criterion[]{Restrictions.and(Restrictions.eq("request.id", getExamRequest().getId()), Restrictions.eq("typeId", 2L))});
                boolean isAdded = Boolean.FALSE;

                if (!ValidationHelper.isNullOrEmpty(requestDocuments)) {
                    if(getExamRequest().getService() !=null
                            && getExamRequest().getService().getUnauthorizedQuote()!=null
                            && getExamRequest().getService().getUnauthorizedQuote()){
                        costNote = "Preventivo non autorizzato";
                        isAdded = Boolean.TRUE;
                    }
                }

                if(!isAdded && getExamRequest().getAuthorizedQuote()!= null
                        && getExamRequest().getAuthorizedQuote()){
                    costNote = "Preventivo autorizzato";
                }
                if(!isAdded && getExamRequest().getUnauthorizedQuote()!=null
                        && getExamRequest().getUnauthorizedQuote()){
                    costNote = "Preventivo non autorizzato";
                }
                costNote = ValidationHelper.isNullOrEmpty(costNote) ? new CreateExcelRequestsReportHelper().generateCorrectNote(getExamRequest()) : costNote.concat(" ").concat(new CreateExcelRequestsReportHelper().generateCorrectNote(getExamRequest()));
            } catch (PersistenceBeanException | IllegalAccessException e) {
                LogHelper.log(log, e);
            }
        }else
            setCostNote(getExamRequest().getCostNote());
        getCostManipulationHelper().viewExtraCost(getExamRequest(), recalculate);
    }*/

    /*public void updateCosts() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        getExamRequest().setCalledFromReportList(true);
        getCostManipulationHelper().updateExamRequestParametersFromHelper(getExamRequest());
        boolean reCalculate = true;

        if(getExamRequest().getCostButtonConfirmClicked() != null && getExamRequest().getCostButtonConfirmClicked()){
            reCalculate = false;
        }
        getCostManipulationHelper().viewExtraCost(getExamRequest(),reCalculate);
    }*/

    /*public void saveRequestExtraCost() throws Exception {
        getCostManipulationHelper().setCostNote(getCostNote());
        DaoManager.refresh(getExamRequest());
        getCostManipulationHelper().saveRequestExtraCost(getExamRequest());
    }*/

    /*public void saveRequestEstateFormalityCost() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        getCostManipulationHelper().saveRequestEstateFormalityCost(getExamRequest());
        if (!ValidationHelper.isNullOrEmpty(getExamRequest().getNumberActUpdate())) {
            boolean reCalculate = true;
            if(getExamRequest().getCostButtonConfirmClicked() != null && getExamRequest().getCostButtonConfirmClicked()){
                reCalculate = false;
            }
            getCostManipulationHelper().viewExtraCost(getExamRequest(), reCalculate);
        }
    }*/

    /*public void addExtraCost(String extraCostValue) {
        getCostManipulationHelper().addExtraCost(extraCostValue, getExamRequest().getId());
    }*/

    /*public void deleteExtraCost(ExtraCost extraCostToDelete) {
        getCostManipulationHelper().getRequestExtraCosts().remove(extraCostToDelete);
    }

    public void filterInvoicesTableFromPanel() throws PersistenceBeanException, IllegalAccessException {

        List<Criterion> restrictionsList = new ArrayList<>();
        if (!ValidationHelper.isNullOrEmpty(getDateFrom())) {
            restrictionsList.add(Restrictions.ge("m.createDate",
                    DateTimeHelper.getDayStart(getDateFrom())));
        }

        if (!ValidationHelper.isNullOrEmpty(getDateTo())) {
            restrictionsList.add(Restrictions.le("m.createDate",
                    DateTimeHelper.getDayEnd(getDateTo())));
        }

        restrictionsList.add(Restrictions.eq("typeId", DocumentType.INVOICE_REPORT.getId()));

        this.loadList(Document.class,
                restrictionsList.toArray(new Criterion[0]),
                new Order[]{Order.asc("invoiceNumber")},
                new CriteriaAlias[]{
                        new CriteriaAlias("mail", "m", JoinType.INNER_JOIN)
                });
    }

    public void openRequestMail() {
        RedirectHelper.goTo(PageTypes.MAIL_MANAGER_VIEW, getEntityEditId());
    }

    public RequestListBean getRequestListBean() {
        return requestListBean;
    }

    public void setRequestListBean(RequestListBean requestListBean) {
        this.requestListBean = requestListBean;
    }

    public Boolean getHideExtraCost() {
        return hideExtraCost;
    }

    public Boolean getShowRequestCost() {
        return showRequestCost;
    }

    public void setShowRequestCost(Boolean showRequestCost) {
        this.showRequestCost = showRequestCost;
    }*/
}