package it.nexera.ris.common.helpers.tableGenerator;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.common.wrapper.AllegatoATableWrapper;
import it.nexera.ris.common.wrapper.AllegatoAWrapper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class AllegatoATableGenerator {
    public transient final Log log = LogFactory.getLog(getClass());
    private Request request;

    public AllegatoATableGenerator(Request request) {
        this.request = request;
    }

    public String compileTable() throws PersistenceBeanException, IllegalAccessException {
        List<Property> list = request.getPropertyList() != null ?
                request.getPropertyList().stream()
                .filter(p -> p.getType() == 2)
                .collect(Collectors.toList()) : null;
        if (!ValidationHelper.isNullOrEmpty(list)) {
            Map<Long, AllegatoATableWrapper> mapCityToProp =
                    buildTableList(list);
            StringBuilder result = new StringBuilder();
            result.append("<div style=\"margin-right: auto; margin-left: auto;\">");
            result.append(buildTable(mapCityToProp));
            result.append("</div>");
            return result.toString();
        } else {
            return "";
        }
    }

    private Map<Long, AllegatoATableWrapper> buildTableList(List<Property> list) {
        Map<Long, AllegatoATableWrapper> mapCityToProp = new HashMap<>();
        for (Property property : list) {
            if (property.getCity() == null)
                continue;
            if (mapCityToProp.get(property.getCity().getId()) == null) {
                AllegatoATableWrapper table = new AllegatoATableWrapper();
                table.setCityName(property.getCityDescription());
                table.setProviceName(property.getProvinceDescription());
                table.setTotalMq(Double.valueOf(0.0));
                table.setTotalOmi(Double.valueOf(0.0));
                table.setTableData(new ArrayList<>());
                table.setNonDisponTableData(new ArrayList<>());
                try {
                    List<LandCadastralCulture> listLandCulture =
                            DaoManager.load(LandCadastralCulture.class,
                                    new Criterion[]{Restrictions.eq("description", property.getQuality())});
                    if (!ValidationHelper.isNullOrEmpty(listLandCulture)) {
                        AllegatoAWrapper tableItem = new AllegatoAWrapper();
                        LandCadastralCulture landCadastralCulture = listLandCulture.get(0);
                        tableItem.setColtura(landCadastralCulture.getLandCulture().getName());
                        tableItem.setDatiTerreno(property.getDraftString());
                        tableItem.setEstensione(property.getLandMQ());
                        tableItem.setValore(2.5);//TODO will replace with the correct value
                        table.setTotalMq(table.getTotalMq() + property.getLandMQ());
                        table.getTableData().add(tableItem);
                    } else {
                        AllegatoAWrapper nonDisponTableItem = new AllegatoAWrapper();
                        nonDisponTableItem.setColtura("NON DISPONIBILE");
                        nonDisponTableItem.setDatiTerreno(property.getDraftString());
                        nonDisponTableItem.setEstensione(property.getLandMQ());
                        nonDisponTableItem.setValore(2.5);//TODO will replace with the correct value
                        table.getNonDisponTableData().add(nonDisponTableItem);
                    }
                    mapCityToProp.put(property.getCity().getId(), table);
                } catch (PersistenceBeanException e) {
                    log.error("", e);
                } catch (IllegalAccessException e) {
                    log.error("", e);
                }
            } else {
                AllegatoATableWrapper table =  mapCityToProp.get(property.getCity().getId());
                try {
                    List<LandCadastralCulture> listLandCulture =
                            DaoManager.load(LandCadastralCulture.class,
                                    new Criterion[]{Restrictions.eq("description", property.getQuality())});
                    if (!ValidationHelper.isNullOrEmpty(listLandCulture)) {
                        AllegatoAWrapper tableItem = new AllegatoAWrapper();
                        LandCadastralCulture landCadastralCulture = listLandCulture.get(0);
                        tableItem.setColtura(landCadastralCulture.getLandCulture().getName());
                        tableItem.setDatiTerreno(property.getDraftString());
                        tableItem.setEstensione(property.getLandMQ());
                        tableItem.setValore(2.5);//TODO will replace with the correct value
                        table.setTotalMq(table.getTotalMq() + property.getLandMQ());
                        table.getTableData().add(tableItem);
                    } else {
                        AllegatoAWrapper nonDisponTableItem = new AllegatoAWrapper();
                        nonDisponTableItem.setColtura("NON DISPONIBILE");
                        nonDisponTableItem.setDatiTerreno(property.getDraftString());
                        nonDisponTableItem.setValore(2.5);//TODO will replace with the correct value
                        nonDisponTableItem.setEstensione(property.getLandMQ());
                        table.getNonDisponTableData().add(nonDisponTableItem);
                    }

                } catch (PersistenceBeanException e) {
                    log.error("", e);
                } catch (IllegalAccessException e) {
                    log.error("", e);
                }
            }
        }
        return mapCityToProp;
    }

    private String buildHeader(AllegatoATableWrapper table) {
        StringBuilder header = new StringBuilder();
        header.append("<tr>")
                .append("<th>").append("</th>")
                .append("<th colspan='3'>")
                .append("<p style=\"text-align: left;\"><span style=\"font-size:11px;\">").append("Comune: ").append(table.getCityName()).append("</span></p>")
                .append("<p style=\"text-align: left;\"><span style=\"font-size:11px;\">").append("Provincia: ").append(table.getProviceName()).append("</span></p>")
                .append("<p style=\"text-align: right;\"><span style=\"font-size:10px;\">").append("Aggiornamento dati valori OMI: 2018\n").append("</span></p>")
                .append("</th>")
                .append("</tr>");
        return header.toString();
    }

    private String buildTable(Map<Long, AllegatoATableWrapper> mapCityToProp) {
        StringBuilder body = new StringBuilder();
        mapCityToProp.entrySet()
                .stream()
                .forEach(i -> {
                    body.append("</br>")
                            .append("<table border=\"1\" cellpadding=\"1\" cellspacing=\"1\" style=\"width:500px;\" style=\"margin-left: auto;margin-right: auto;\">")
                            .append("<thead>")
                            .append(buildHeader(i.getValue()))
                            .append("</thead>")
                            .append("<tbody>")
                            .append(generateColumnTitle())
                            .append(generateTableData(i.getValue().getTableData()))
                            .append("<tr><td colspan='4'></td></tr>")
                            .append(generateNonDisponTableData(i.getValue().getNonDisponTableData()))
                            .append("</tbody>")
                            .append("</table>");
                });
        return body.toString();
    }

    private String generateTableData(List<AllegatoAWrapper> tableData) {
        StringBuilder rows = new StringBuilder();
        Double mqTotal = 0.0;
        Double omiTotal = 0.0;
        for (AllegatoAWrapper data : tableData) {
            rows.append("<tr>")
                    .append("<td><span style=\"font-size:9px;\">").append(getTextContent(data.getColtura())).append("</span></td>")
                    .append("<td><span style=\"font-size:9px;\">").append(getTextContent(data.getDatiTerreno())).append("</span></td>")
                    .append("<td><span style=\"font-size:9px;\">").append(getNumberContent(data.getEstensione())).append("</span></td>")
                    .append("<td><span style=\"font-size:9px;\">").append(getNumberContent(data.getValore())).append("</span></td>")
                    .append("</tr>");
            if (data.getEstensione() != null) {
                mqTotal += data.getEstensione();
            }
            if (data.getValore() != null) {
                omiTotal += data.getValore();
            }
        }
        rows.append("<tr>")
                .append("<td><span style=\"font-size:11px;\">").append("TOTALE").append("</span></td>")
                .append("<td><span style=\"font-size:9px;\">").append("").append("</span></td>")
                .append("<td><span style=\"font-size:9px;\">").append(mqTotal).append("</span></td>")
                .append("<td><span style=\"font-size:9px;\">").append(omiTotal).append("</span></td>")
                .append("</tr>");

        return rows.toString();
    }

    private String generateNonDisponTableData(List<AllegatoAWrapper> nonDisponTableData) {
        StringBuilder rows = new StringBuilder();
        for (AllegatoAWrapper data : nonDisponTableData) {
            rows.append("<tr>")
                    .append("<td><span style=\"font-size:9px;\">").append(getTextContent(data.getColtura())).append("</span></td>")
                    .append("<td><span style=\"font-size:9px;\">").append(getTextContent(data.getDatiTerreno())).append("</span></td>")
                    .append("<td><span style=\"font-size:9px;\">").append(getNumberContent(data.getEstensione())).append("</span></td>")
                    .append("<td><span style=\"font-size:9px;\">").append(getNumberContent(data.getValore())).append("</span></td>")
                    .append("</tr>");
        }
        return rows.toString();
    }

    private String generateColumnTitle() {
        StringBuilder row = new StringBuilder();
        row.append("<tr>")
                .append("<td><span style=\"font-size:11px;\">").append("COLTURA").append("</span></td>")
                .append("<td><span style=\"font-size:11px;\">").append("Dati terreno").append("</span></td>")
                .append("<td><span style=\"font-size:11px;\">").append("Estensione (Mq)").append("</span></td>")
                .append("<td><span style=\"font-size:11px;\">").append("Valore OMI (€)").append("</span></td>")
                .append("</tr>");
        return row.toString();
    }

    private String getTextContent(String text) {
        return text == null ? "" : text;
    }

    private String getNumberContent(Double number) {
        return number == null ? "" : number.toString();
    }

}
