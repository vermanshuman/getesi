package it.nexera.ris.common.helpers.create.pdf;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RequestType;
import j2html.TagCreator;
import j2html.tags.ContainerTag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static j2html.TagCreator.*;

public class CreatePDFReportHelper extends CreateReportHelper {
    private final static String tableStyle = "border: 2px solid black; border-collapse: collapse; width: 100%; align: center; table-layout: fixed;";
    private final static String tableStyleWithoutBorder = "border: none; border-collapse: collapse; width: 100%; align: center; table-layout: fixed;";
    private final static String titleStyle = "text-align: center; font-size: 12px;";
    private final static String border1pxStyle = "border: 1px solid black;";
    private final static String withoutborderStyle = "border: none;";
    private final static String tdStyle = "border: 1px solid black;  width: 9%; font-size: 8px;";
    private final static String tdStyleWithoutborder = "border: none; width: 9%; font-size: 8px;";
    private final static String tdCenterStyle = "border: 1px solid black;  width: 9%; font-size: 8px;  text-align: center;";
    private final static String tdSpecialStyle = "border: 1px solid black;  width: 9%; font-size: 10px; font-weight: bold; text-align: center;";
    private final static String trSpecialStyle = "background:#FFCB99;";
    private final static String divStyle = "height: 10px;";
    private final static String nobr = " white-space:nowrap;";
    private final static int numberColumn = 11;

    private static ContainerTag tbody = null;

    public String getPdfRequestBodyTable(List<Request> requests) throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        tbody = tbody();
        StringJoiner joiner = new StringJoiner(" ");

        joiner.add(br().toString());

        addHeader(requests.get(0));

        joiner.add(table().with(tbody).withStyle(tableStyleWithoutBorder).toString());

        Map<RequestType, List<Request>> sortedRequests = new HashMap<>();

        sortRequestsByType(requests, sortedRequests);

        for (Map.Entry<RequestType, List<Request>> entry : sortedRequests.entrySet()) {
            joiner.add(br().toString() + entry.getKey().getName());
            tbody = tbody();
            addSeparator();
            addCosts(entry.getValue());
            addEmptyRow(1, true);
            addFooter(entry.getValue());

            joiner.add(table().with(tbody).withStyle(tableStyle).toString());
        }

        return joiner.toString();
    }

    private void addHeader(Request request) {
        ContainerTag tr = tr();

        addEmptyRow(1, false);

        tr = tr.with(TagCreator.td(ResourcesHelper.getString("reportN")).withStyle(tdStyleWithoutborder));
        addEmptyCell(tr, 2, false);
        tr = tr.with(td(ResourcesHelper.getString("referenceInvoice")).withStyle(tdStyleWithoutborder + nobr));
        addEmptyCell(tr, 7, false);
        tbody.with(tr);

        addEmptyRow(1, false);

        String client = "";
        String office = "";
        String ndg = "";
        String referenceRequest = "";
        if (!ValidationHelper.isNullOrEmpty(request.getClient())) {
            client = request.getClient().toString();
            if (!ValidationHelper.isNullOrEmpty(request.getClient().getOffice())) {
                office = request.getClient().getOffice().getDescription();
            }
        }
        if (!ValidationHelper.isNullOrEmpty(request.getNdg())) {
            ndg = request.getNdg();
        }
        if (!ValidationHelper.isNullOrEmpty(request.getMail().getReferenceRequest())) {
            referenceRequest = request.getMail().getReferenceRequest();
        }
        tr = tr();
        tr = tr.with(td(ResourcesHelper.getString("officeText") + office).withStyle(tdStyleWithoutborder));
        addEmptyCell(tr, 1, false);
        tr = tr.with(td(ResourcesHelper.getString("managerText") + client).withStyle(tdStyleWithoutborder + nobr));
        addEmptyCell(tr, 2, false);
        tr = tr.with(td(ResourcesHelper.getString("trust")).withStyle(tdStyleWithoutborder));
        addEmptyCell(tr, 5, false);
        tbody.with(tr);

        addEmptyRow(1, false);

        tr = tr();
        tr = tr.with(td(ResourcesHelper.getString("ndgText") + ndg).withStyle(tdStyleWithoutborder));
        addEmptyCell(tr, 1, false);
        tr = tr.with(td(ResourcesHelper.getString("rifText") + referenceRequest).withStyle(tdStyleWithoutborder));
        addEmptyCell(tr, 8, false);
        tbody.with(tr);

        addEmptyRow(4, false);
    }

    private void addSeparator() {
        ContainerTag tr = tr();

        tr = tr.with(td(ResourcesHelper.getString("requestedDate")).withStyle(tdSpecialStyle));
        tr = tr.with(td(ResourcesHelper.getString("nominative")).withStyle(tdSpecialStyle));
        tr = tr.with(td(ResourcesHelper.getString("codFiscIva")).withStyle(tdSpecialStyle));
        tr = tr.with(td(ResourcesHelper.getString("permissionRequest")).withStyle(tdSpecialStyle));
        tr = tr.with(td(ResourcesHelper.getString("requestListCorservatoryName")).withStyle(tdSpecialStyle));
        tr = tr.with(td(ResourcesHelper.getString("requestPrintFormalityPresentationDate")).withStyle(tdSpecialStyle));
        tr = tr.with(td(ResourcesHelper.getString("formality")).withStyle(tdSpecialStyle));
        tr = tr.with(td(ResourcesHelper.getString("mortgageRights")).withStyle(tdSpecialStyle));
        tr = tr.with(td(ResourcesHelper.getString("landRegistryRights")).withStyle(tdSpecialStyle));
        tr = tr.with(td(ResourcesHelper.getString("compensation")).withStyle(tdSpecialStyle));
        tr = tr.with(td(ResourcesHelper.getString("formalityTotal")).withStyle(tdSpecialStyle));
        tbody.with(tr.withStyle(trSpecialStyle));
    }

    private void addCosts(List<Request> requests) throws PersistenceBeanException, IllegalAccessException {
        ContainerTag tr = null;
        for (Request request : requests) {
            tr = tr();
            tr = tr.with(td(request.getCreateDateStr()).withStyle(tdCenterStyle));
            tr = tr.with(td(request.getSubject() != null ? request.getSubject().getFullName() : "").withStyle(tdCenterStyle));
            tr = tr.with(td(request.getFiscalCodeVATNamber()).withStyle(tdCenterStyle));
            tr = tr.with(td(request.getServiceName().toUpperCase()).withStyle(tdCenterStyle));
            tr = tr.with(td(request.getAggregationLandChargesRegistryName()).withStyle(tdCenterStyle));
            tr = tr.with(TagCreator.td(DateTimeHelper.toString(request.getEvasionDate())).withStyle(tdCenterStyle));
            tr = tr.with(td(request.getNumberActOrSumOfEstateFormalitiesAndOther().toString()).withStyle(tdCenterStyle));
            tr = tr.with(td(getCostEstateFormalityAndExtraCostRelated(request) != 0d ? euroSymbol + String.format("%.2f",
                    getCostEstateFormalityAndExtraCostRelated(request)) : zeroValue).withStyle(tdCenterStyle));
            tr = tr.with(td(getCostCadastralAndExtraCostRelated(request) != 0d ? euroSymbol + String.format("%.2f",
                    getCostCadastralAndExtraCostRelated(request)) : zeroValue).withStyle(tdCenterStyle));
            tr = tr.with(td(request.getCostPay() != null ? euroSymbol + String.format("%.2f", request.getCostPay()) : zeroValue).withStyle(tdCenterStyle));
            tr = tr.with(td(request.getTotalCost() != null ? euroSymbol + request.getTotalCost() : zeroValue).withStyle(tdCenterStyle));
            tbody.with(tr);

        }
    }

    private void addFooter(List<Request> requests) throws PersistenceBeanException, IllegalAccessException {
        ContainerTag tr = tr();

        tr = tr.with(td(ResourcesHelper.getString("formalityTotal").toUpperCase()).withStyle(tdSpecialStyle));
        addEmptyCell(tr, 6, true);
        tr = tr.with(td(euroSymbol + getSumOfCostEstateFormality(requests)).withStyle(tdCenterStyle));
        tr = tr.with(td(euroSymbol + getSumOfCostCadastral(requests)).withStyle(tdCenterStyle));
        tr = tr.with(td(euroSymbol + getSumOfCostPay(requests)).withStyle(tdCenterStyle));
        tr = tr.with(td(euroSymbol + getSumOfCostTotal(requests)).withStyle(tdSpecialStyle));
        tbody.with(tr);
    }

    private void addIndicatedNumberEmptyRows(int number, boolean withBorders) {
        ContainerTag tr = tr();
        for (int i = 0; i < number; i++) {
            tr = tr.with(td(div().withStyle(divStyle)).withStyle(withBorders ? border1pxStyle : withoutborderStyle));
        }
        tbody.with(tr);
    }

    private void addEmptyRow(int number, boolean withBorders) {
        for (int i = 0; i < number; i++) {
            addIndicatedNumberEmptyRows(numberColumn, withBorders);
        }
    }

    private void addEmptyCell(ContainerTag tr, int number, boolean withBorders) {
        ContainerTag td;
        for (int i = 0; i < number; i++) {
            td = td(div().withStyle(divStyle)).withStyle(withBorders ? border1pxStyle : withoutborderStyle);
            tr.with(td);
        }
    }
}
