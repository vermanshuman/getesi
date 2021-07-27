package it.nexera.ris.common.helpers.tableGenerator;

import it.nexera.ris.common.enums.SexTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.exceptions.TypeFormalityNotConfigureException;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.TemplatePdfTableHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.common.wrapper.AllegatoAWrapper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.web.beans.pages.RequestTextEditBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AllegatoATableGenerator {
    private Request request;
    private String header;
    private String body;

    public AllegatoATableGenerator(Request request) {
        this.request = request;
    }

    public String compileTable() throws PersistenceBeanException, IllegalAccessException {
//        List<Property> list = request.getPropertyList() != null ?
//                request.getPropertyList().stream()
//                .filter(p -> p.getType() == 2)
//                .collect(Collectors.toList()) : null;
//        Map<Long, AllegatoAWrapper> mapCityToProp = new HashMap<>();
//        Double totalMq;
//        Double totalOmi;
//        if (list != null) {
//            list.stream()
//                    .forEach(property -> {
//                        if (mapCityToProp.get(property.getCity().getId()) == null) {
//                            AllegatoAWrapper item = new AllegatoAWrapper();
//                            item.setCityName(property.getCityDescription());
//                            item.setProviceName(property.getProvinceDescription());
//                            String datiTerreno = "";
//                            if (property.getCadastralData() != null && property.getCadastralData().size() > 0) {
//                                CadastralData cadastralData = property.getCadastralData().get(0);
//                                datiTerreno = String.format("foglio %s p.lla %",
//                                        cadastralData.getSheet(),
//                                        cadastralData.getParticle());
//                            }
//                            item.setDatiTerreno(datiTerreno);
//                            item.setEstensione(property.getCadastralArea().toString());
//                        }
//                    });
//        } else {
//            return "";
//        }
        StringBuilder table = new StringBuilder();
        buildHeader();
        buildBody();
        return table.append("<table border=\"1\" cellpadding=\"1\" cellspacing=\"1\" style=\"width:500px;\">")
                .append("<thead>")
                .append(this.header)
                .append("</thead>")
                .append("<tbody>")
                .append(this.body)
                .append("</tbody>")
                .append("</table>").toString();
    }

    private void buildHeader() {
        StringBuilder header = new StringBuilder();
        header.append("<tr>")
                .append("<th>").append("</th>")
                .append("<th colspan='3'>")
                .append("<p style=\"text-align: left;\"><span style=\"font-size:11px;\">").append("Comune: ").append("MARZANO APPIO").append("</span></p>")
                .append("<p style=\"text-align: left;\"><span style=\"font-size:11px;\">").append("Provincia: ").append("CASERTA").append("</span></p>")
                .append("<p style=\"text-align: right;\"><span style=\"font-size:10px;\">").append("Aggiornamento dati valori OMI: 2018\n").append("</span></p>")
                .append("</th>")
                .append("</tr>");
        this.header = header.toString();
    }

    private void buildBody() throws PersistenceBeanException, IllegalAccessException {
        StringBuilder body = new StringBuilder();
        body.append("<tbody>")
                .append(generateBody())
                .append("</tbody>");
        this.body = body.toString();
    }

    private String generateBody() throws PersistenceBeanException, IllegalAccessException {
        StringBuilder row = new StringBuilder();
        row.append("<tr>")
                .append("<td><span style=\"font-size:11px;\">").append("COLTURA").append("</span></td>")
                .append("<td><span style=\"font-size:11px;\">").append("Dati terreno").append("</span></td>")
                .append("<td><span style=\"font-size:11px;\">").append("Estensione (Mq)").append("</span></td>")
                .append("<td><span style=\"font-size:11px;\">").append("Valore OMI (€)").append("</span></td>")
                .append("</tr>");
        return row.toString();
    }


}
