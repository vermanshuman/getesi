package it.nexera.ris.common.helpers;

import it.nexera.ris.common.enums.ExtraCostType;
import it.nexera.ris.common.enums.MortgageType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.create.xls.CreateExcelRequestsReportHelper;
import it.nexera.ris.persistence.Action;
import it.nexera.ris.persistence.TransactionExecuter;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.EstateFormality;
import it.nexera.ris.persistence.beans.entities.domain.ExtraCost;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.web.beans.PageBean;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.primefaces.context.RequestContext;

import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.List;

public class CostManipulationHelper extends PageBean {

    private static final Double IPOTECARIO_DEFAULT = 6.30d;
    private static final Double IPOTECARIO_TITOLO = 7.20d;
    private static final Double IPOTECARIO_ADDITIONAL_FORMALITY = 3.60d;
    private static final Double CATASTO_DIVISIBLE = 0.90d;
    private static final Double MARCHE_DA_BOLLO_DEFAULT = 16.0d;

    private String extraCostLandRegistry;

    private String extraCostPostalExpense;

    private String extraCostOther;

    private List<ExtraCost> requestExtraCosts;

    private Boolean costOutput;

    private Integer spinnerNumber;

    private Integer stampSpinnerNumber;

    private String selectedMortgageNote;

    private List<SelectItem> mortgageTypeList;

    private String estateFormalityCost;

    private Double numberActOrSumOfEstateFormalitiesAndOtherTemp;

    private Double costEstateFormalityTemp;

    private Double costCadastralTemp;

    private Double costPayTemp;

    private Double initCostTemp;

    private Double taxableCostTemp;

    private boolean isEditable;

    private String extraCostOtherNote;

    private String costNote;

    private Boolean includeNationalCost;

    private String extraCostMortgageTable;

    private String extraCostLandRegistryTable;


    public void saveRequestEstateFormalityCost(Request request) throws PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(getEstateFormalityCost())) {
            try {
                Double.parseDouble(getEstateFormalityCost());
            } catch (NumberFormatException e) {
                setEstateFormalityCost(null);
                return;
            }
            request.setNumberActUpdate(Double.valueOf(getEstateFormalityCost()));
            DaoManager.save(request, true);
        }
        setEstateFormalityCost(null);
    }

    public void viewExtraCost(Request request) throws PersistenceBeanException, IllegalAccessException,
            InstantiationException {
        viewExtraCost(request, false);
    }

    public void viewExtraCost(Request request, boolean recalculate) throws PersistenceBeanException, IllegalAccessException,
            InstantiationException {
        if (request == null) {
            return;
        } else {
            updateHelperParametersFromRequest(request);
        }
        CreateExcelRequestsReportHelper createExcelRequestsReportHelper = new CreateExcelRequestsReportHelper();
        setIncludeNationalCost(request.getIncludeNationalCost());

        List<EstateFormality> estateFormalitiesUpdated = DaoManager.load(EstateFormality.class, new CriteriaAlias[]{
                new CriteriaAlias("requestListUpdate", "request", JoinType.INNER_JOIN)
        }, new Criterion[]{
                Restrictions.eq("request.id", request.getId())
        });

        if (!ValidationHelper.isNullOrEmpty(request.getService()) && request.getService().getIsUpdateAndNotNull()
                && !ValidationHelper.isNullOrEmpty(estateFormalitiesUpdated)) {
            setEstateFormalityCost(String.valueOf(estateFormalitiesUpdated.size()));
        }

        if (!ValidationHelper.isNullOrEmpty(request.getService()) && request.getService().getIsUpdate()
                && ValidationHelper.isNullOrEmpty(request.getNumberActUpdate())) {
            RequestContext.getCurrentInstance().update("inputEstateCost");
            executeJS("PF('estateFormalityCostDlg').show();");
            return;
        }


        setCostNote(createExcelRequestsReportHelper.generateCorrectNote(request));
        //if (!ValidationHelper.isNullOrEmpty(request.getCostNote()))
            //setCostNote(request.getCostNote());

        CostCalculationHelper calculation = new CostCalculationHelper(request);

        calculation.calculateAllCosts(true, recalculate);

        List<ExtraCost> extraCosts = DaoManager.load(ExtraCost.class, new Criterion[]{
                Restrictions.eq("requestId", request.getId())});

        setRequestExtraCosts(extraCosts);
        boolean isCostMismatch = createExcelRequestsReportHelper.checkTotalCostSpecialColumn( request);
        if(isCostMismatch){
            String note = !ValidationHelper.isNullOrEmpty(getCostNote()) ? (getCostNote() + "\n") : "";
            note += "Anomalia costi";
            setCostNote(note);
        }
        updateHelperParametersFromRequest(request);
        RequestContext.getCurrentInstance().update("requestExtraCostTable");
        RequestContext.getCurrentInstance().update("requestExtraCostDialog");

        executeJS("PF('requestExtraCostDlg').show();");

    }

    public void addExtraCost(String extraCostValue, Long requestId) {
        ExtraCost newExtraCost = new ExtraCost();
        ExtraCostType extraCostType = ExtraCostType.getEnumByCode(extraCostValue);
        if (extraCostType != null) {
            switch (ExtraCostType.getEnumByCode(extraCostValue)) {
                case IPOTECARIO:
                    Double cost = IPOTECARIO_DEFAULT;
                    if (getSelectedMortgageNote().equals(MortgageType.AdditionalFormality.getName())) {
                        cost = IPOTECARIO_ADDITIONAL_FORMALITY;
                    } else if (getSelectedMortgageNote().equals(MortgageType.Titolo.getName())) {
                        cost = IPOTECARIO_TITOLO;
                    }
                    newExtraCost.setPrice((getSpinnerNumber() == null ? 1 : getSpinnerNumber()) * (cost));
                    newExtraCost.setType(ExtraCostType.IPOTECARIO);
                    newExtraCost.setNote(MortgageType.valueOf(getSelectedMortgageNote()).toString());
                    break;
                case CATASTO:
                    if (!ValidationHelper.isNullOrEmpty(getExtraCostLandRegistry())) {
                        try {
                            Double.parseDouble(getExtraCostLandRegistry().replaceAll(",", "."));
                            if ((Double.parseDouble(getExtraCostLandRegistry().replaceAll(",", ".")) / CATASTO_DIVISIBLE) % 1 != 0) {
                                markInvalid("inputCostLandRegistry", "warning");
                                setExtraCostLandRegistry(null);
                                return;
                            }
                        } catch (NumberFormatException e) {
                            markInvalid("inputCostLandRegistry", "warning");
                            setExtraCostLandRegistry(null);
                            return;
                        }
                        newExtraCost.setPrice(Double.valueOf(getExtraCostLandRegistry().replaceAll(",", ".")));
                        newExtraCost.setType(ExtraCostType.CATASTO);
                        cleanValidation();
                    } else {
                        return;
                    }
                    break;
                case ALTRO:
                    if (!ValidationHelper.isNullOrEmpty(getExtraCostOther())) {
                        try {
                            Double.parseDouble(getExtraCostOther().replaceAll(",", "."));
                        } catch (NumberFormatException e) {
                            setExtraCostOther(null);
                            return;
                        }
                        newExtraCost.setPrice(
                                Double.valueOf(Double.parseDouble(getExtraCostOther().replaceAll(",", "."))));

                        newExtraCost.setType(ExtraCostType.ALTRO);
                        newExtraCost.setNote(getExtraCostOtherNote());
                    } else {
                        return;
                    }
                    break;
                case NAZIONALEPOSITIVA:
                    if (!ValidationHelper.isNullOrEmpty(getExtraCostOther())) {
                        try {
                            Double.parseDouble(getExtraCostOther().replaceAll(",", "."));
                        } catch (NumberFormatException e) {
                            setExtraCostOther(null);
                            return;
                        }
                        newExtraCost.setPrice(Double.valueOf(getExtraCostOther()));
                        newExtraCost.setType(ExtraCostType.NAZIONALEPOSITIVA);
                        newExtraCost.setNote(getExtraCostOtherNote());
                    } else {
                        return;
                    }
                    break;
                case MARCA:
                    Double stampcost = MARCHE_DA_BOLLO_DEFAULT;
                    newExtraCost.setPrice((getStampSpinnerNumber() == null ? 1 : getStampSpinnerNumber()) * (stampcost));
                    newExtraCost.setType(ExtraCostType.MARCA);
                    newExtraCost.setNote("Marca da bollo");
                    break;
                case POSTALE:
                    if (!ValidationHelper.isNullOrEmpty(getExtraCostPostalExpense())) {
                        try {
                            Double.parseDouble(getExtraCostPostalExpense().replaceAll(",", "."));
                        } catch (NumberFormatException e) {
                            markInvalid("inputCostPostalExp", "warning");
                            setExtraCostPostalExpense(null);
                            return;
                        }
                        newExtraCost.setPrice(Double.valueOf(getExtraCostPostalExpense().replaceAll(",", ".")));
                        newExtraCost.setType(ExtraCostType.POSTALE);
                        newExtraCost.setNote("Spese postali");
                        cleanValidation();
                    } else {
                        return;
                    }
                    break;

            }
        } else {
            switch (extraCostValue) {
                case "IPOTECARIO_TAVOLARE":
                    if (!ValidationHelper.isNullOrEmpty(getExtraCostMortgageTable())) {
                        try {
                            Double.parseDouble(getExtraCostMortgageTable().replaceAll(",", "."));
                        } catch (NumberFormatException e) {
                            setExtraCostMortgageTable(null);
                            return;
                        }
                        newExtraCost.setPrice(
                                Double.valueOf(Double.parseDouble(getExtraCostMortgageTable().replaceAll(",", "."))));

                        newExtraCost.setType(ExtraCostType.IPOTECARIO);
                        newExtraCost.setNote("Tavolare ipotecario");
                    } else {
                        return;
                    }
                    break;

                case "CATASTO_TAVOLARE":
                    if (!ValidationHelper.isNullOrEmpty(getExtraCostLandRegistryTable())) {
                        try {
                            Double.parseDouble(getExtraCostLandRegistryTable().replaceAll(",", "."));
                        } catch (NumberFormatException e) {
                            setExtraCostLandRegistryTable(null);
                            return;
                        }
                        newExtraCost.setPrice(
                                Double.valueOf(Double.parseDouble(getExtraCostLandRegistryTable().replaceAll(",", "."))));

                        newExtraCost.setType(ExtraCostType.CATASTO);
                        newExtraCost.setNote("Tavolare catasto");
                    } else {
                        return;
                    }
                    break;
            }
        }

        newExtraCost.setRequestId(requestId);

        if (ValidationHelper.isNullOrEmpty(getRequestExtraCosts())) {
            setRequestExtraCosts(new ArrayList<>());
        }
        getRequestExtraCosts().add(newExtraCost);
        setExtraCostPostalExpense(null);
        setSpinnerNumber(null);
        setStampSpinnerNumber(null);
        setSelectedMortgageNote(null);
        setExtraCostLandRegistry(null);
        setExtraCostOther(null);

    }

    public void deleteExtraCost(ExtraCost extraCostToDelete) {
        getRequestExtraCosts().remove(extraCostToDelete);
    }

    public void saveRequestExtraCost(Request request) throws Exception {
        saveRequestExtraCost(request, Boolean.TRUE);
    }

    public void saveRequestExtraCost(Request request, Boolean costButtonConfirmClicked) throws Exception {
        List<ExtraCost> removeList = DaoManager.load(ExtraCost.class, new Criterion[]{
                Restrictions.eq("requestId", request.getId())});

        TransactionExecuter.execute(new Action() {
            @Override
            public void execute() throws Exception {

                if (!ValidationHelper.isNullOrEmpty(getRequestExtraCosts()) || !getRequestExtraCosts().equals(removeList)) {
                    for (ExtraCost cost : removeList) {
                        DaoManager.remove(cost);
                    }
                    Double requestCost = 0d;

                    if (!ValidationHelper.isNullOrEmpty(request.getInitCost())) {
                        requestCost = Double.valueOf(request.getInitCost().replaceAll(",", "."));
                    }

                    for (ExtraCost cost : getRequestExtraCosts()) {
                        if (ValidationHelper.isNullOrEmpty(cost.getType()) ||
                                !ExtraCostType.NAZIONALEPOSITIVA.equals(cost.getType())) {
                            requestCost += cost.getPrice();
                        }

                        ExtraCost newCost = new ExtraCost();
                        newCost.setPrice(cost.getPrice());
                        newCost.setNote(cost.getNote());
                        newCost.setType(cost.getType());
                        newCost.setRequestId(cost.getRequestId());

                        DaoManager.save(newCost);
                    }

                }
                request.setCostButtonConfirmClicked(costButtonConfirmClicked);
                request.setConfirmExtraCostsPressed(true);
                request.setLastCostChanging(true);
                request.setIncludeNationalCost(getIncludeNationalCost());
                if (!ValidationHelper.isNullOrEmpty(getCostNote())){
                    request.setCostNote(getCostNote().replaceAll("Anomalia costi", ""));
                }

                if (isEditable()) {
                    updateExamRequestParametersFromHelper(request);
                }
                DaoManager.save(request);
            }
        });

    }

    private void updateHelperParametersFromRequest(Request request) {
        if (!ValidationHelper.isNullOrEmpty(request.getInitCost())) {
            setInitCostTemp(Double.valueOf(request.getInitCost().replaceAll(",", ".")));
        } else {
            setInitCostTemp(null);
        }
        setCostPayTemp(request.getCostPay());
        setCostCadastralTemp(request.getCostCadastral());
        setCostEstateFormalityTemp(request.getCostEstateFormality());
        setNumberActOrSumOfEstateFormalitiesAndOtherTemp(request.getNumberActOrSumOfEstateFormalitiesAndOther());
        if (!ValidationHelper.isNullOrEmpty(request.getTaxable())) {
            setTaxableCostTemp(request.getTaxable());
        } else {
            setTaxableCostTemp(null);
        }
    }

    public void updateExamRequestParametersFromHelper(Request request) throws PersistenceBeanException {
        DaoManager.refresh(request);
        request.setInitCost(getInitCostTemp().toString());
        request.setCostPay(getCostPayTemp());
        request.setCostCadastral(getCostCadastralTemp());
        request.setCostEstateFormality(getCostEstateFormalityTemp());
        request.setNumberActUpdate(getNumberActOrSumOfEstateFormalitiesAndOtherTemp());
        request.setTaxable(getTaxableCostTemp());
        request.setCostNote(getCostNote().replaceAll("Anomalia costi", ""));
        Double totalCost = 0.0;
        if (!ValidationHelper.isNullOrEmpty(getCostPayTemp()))
            totalCost += getCostPayTemp();

        if (!ValidationHelper.isNullOrEmpty(getCostCadastralTemp()))
            totalCost += getCostCadastralTemp();

        if (!ValidationHelper.isNullOrEmpty(getCostEstateFormalityTemp()))
            totalCost += getCostEstateFormalityTemp();

        request.setTotalCost(String.format("%.2f", totalCost));
        DaoManager.save(request, true);
    }

    public String getExtraCostLandRegistry() {
        return extraCostLandRegistry;
    }

    public void setExtraCostLandRegistry(String extraCostLandRegistry) {
        this.extraCostLandRegistry = extraCostLandRegistry;
    }

    public String getExtraCostOther() {
        return extraCostOther;
    }

    public void setExtraCostOther(String extraCostOther) {
        this.extraCostOther = extraCostOther;
    }

    public List<ExtraCost> getRequestExtraCosts() {
        return requestExtraCosts;
    }

    public void setRequestExtraCosts(List<ExtraCost> requestExtraCosts) {
        this.requestExtraCosts = requestExtraCosts;
    }

    public Boolean getCostOutput() {
        return costOutput;
    }

    public void setCostOutput(Boolean costOutput) {
        this.costOutput = costOutput;
    }

    public Integer getSpinnerNumber() {
        return spinnerNumber;
    }

    public void setSpinnerNumber(Integer spinnerNumber) {
        this.spinnerNumber = spinnerNumber;
    }

    public String getSelectedMortgageNote() {
        return selectedMortgageNote;
    }

    public void setSelectedMortgageNote(String selectedMortgageNote) {
        this.selectedMortgageNote = selectedMortgageNote;
    }

    public List<SelectItem> getMortgageTypeList() {
        return mortgageTypeList;
    }

    public void setMortgageTypeList(List<SelectItem> mortgageTypeList) {
        this.mortgageTypeList = mortgageTypeList;
    }

    public String getEstateFormalityCost() {
        return estateFormalityCost;
    }

    public void setEstateFormalityCost(String estateFormalityCost) {
        this.estateFormalityCost = estateFormalityCost;
    }

    @Override
    protected void onConstruct() {

    }

    public Double getNumberActOrSumOfEstateFormalitiesAndOtherTemp() {
        return numberActOrSumOfEstateFormalitiesAndOtherTemp;
    }

    public void setNumberActOrSumOfEstateFormalitiesAndOtherTemp(Double numberActOrSumOfEstateFormalitiesAndOtherTemp) {
        this.numberActOrSumOfEstateFormalitiesAndOtherTemp = numberActOrSumOfEstateFormalitiesAndOtherTemp;
    }

    public Double getCostEstateFormalityTemp() {
        return costEstateFormalityTemp;
    }

    public void setCostEstateFormalityTemp(Double costEstateFormalityTemp) {
        this.costEstateFormalityTemp = costEstateFormalityTemp;
    }

    public Double getCostCadastralTemp() {
        return costCadastralTemp;
    }

    public void setCostCadastralTemp(Double costCadastralTemp) {
        this.costCadastralTemp = costCadastralTemp;
    }

    public Double getCostPayTemp() {
        return costPayTemp;
    }

    public void setCostPayTemp(Double costPayTemp) {
        this.costPayTemp = costPayTemp;
    }

    public Double getInitCostTemp() {
        return initCostTemp;
    }

    public void setInitCostTemp(Double initCostTemp) {
        this.initCostTemp = initCostTemp;
    }

    public boolean isEditable() {
        return isEditable;
    }

    public void setEditable(boolean editable) {
        isEditable = editable;
    }

    public Double getTaxableCostTemp() {
        return taxableCostTemp;
    }

    public void setTaxableCostTemp(Double taxableCostTemp) {
        this.taxableCostTemp = taxableCostTemp;
    }

    public String getExtraCostOtherNote() {
        return extraCostOtherNote;
    }

    public void setExtraCostOtherNote(String extraCostOtherNote) {
        this.extraCostOtherNote = extraCostOtherNote;
    }

    public String getCostNote() {
        return costNote;
    }

    public void setCostNote(String costNote) {
        this.costNote = costNote;
    }

    public Boolean getIncludeNationalCost() {
        return includeNationalCost;
    }

    public void setIncludeNationalCost(Boolean includeNationalCost) {
        this.includeNationalCost = includeNationalCost;
    }

    public Integer getStampSpinnerNumber() {
        return stampSpinnerNumber;
    }

    public void setStampSpinnerNumber(Integer stampSpinnerNumber) {
        this.stampSpinnerNumber = stampSpinnerNumber;
    }

    public String getExtraCostPostalExpense() {
        return extraCostPostalExpense;
    }

    public void setExtraCostPostalExpense(String extraCostPostalExpense) {
        this.extraCostPostalExpense = extraCostPostalExpense;
    }

    public String getExtraCostMortgageTable() {
        return extraCostMortgageTable;
    }

    public void setExtraCostMortgageTable(String extraCostMortgageTable) {
        this.extraCostMortgageTable = extraCostMortgageTable;
    }

    public String getExtraCostLandRegistryTable() {
        return extraCostLandRegistryTable;
    }

    public void setExtraCostLandRegistryTable(String extraCostLandRegistryTable) {
        this.extraCostLandRegistryTable = extraCostLandRegistryTable;
    }
}