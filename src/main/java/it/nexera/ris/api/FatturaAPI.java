package it.nexera.ris.api;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.FileHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Invoice;
import it.nexera.ris.persistence.beans.entities.domain.InvoiceItem;
import it.nexera.ris.persistence.beans.entities.domain.TaxRate;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class FatturaAPI {

    public transient final Log log = LogFactory.getLog(getClass());
    public String getDataForXML(Invoice invoice, List<InvoiceItem> invoiceItems) throws HibernateException, IllegalAccessException, PersistenceBeanException, InstantiationException {
        if(invoiceItems == null)
            invoiceItems = DaoManager.load(InvoiceItem.class, new Criterion[] { Restrictions.eq("invoice.id", invoice.getId()) });
        //first, get and initialize an engine
        VelocityEngine velocityEngine = new VelocityEngine();

        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER,"class,file");
        velocityEngine.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        velocityEngine.init();
        //add data to a VelocityContext
//        invoiceItems
//                .stream()
//                .filter(iv -> !ValidationHelper.isNullOrEmpty(iv.getDescription())
//                        && iv.getDescription().contains("&"))
//                .forEach(iv -> iv.setXmlDescription(iv.getDescription().replaceAll("&", "E")));
        VelocityContext context = addDataToVelocityContext(invoice, invoiceItems);
        //get the Template
        Template template = velocityEngine.getTemplate(FileHelper.getFatturaAPITemplatePath());
        //now render the template into a Writer, here  a StringWriter
        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        return writer.toString();
    }

    public String getDataForXML(Invoice invoice) throws HibernateException, IllegalAccessException, PersistenceBeanException, InstantiationException {
        return getDataForXML(invoice, null);
    }

    public VelocityContext addDataToVelocityContext(Invoice invoice, List<InvoiceItem> invoiceItems) throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        VelocityContext context = new VelocityContext();
        if(!ValidationHelper.isNullOrEmpty(invoice.getDocumentType()))
            context.put("documentType", invoice.getDocumentType());
        else
            context.put("documentType", "FE");
        String customerName = invoice.getClient().getNameOfTheCompany();
        if(!(customerName != null && !customerName.equals("")))
            customerName = invoice.getClient().getNameProfessional();
        context.put("customerName", customerName);
        String customerAddress = "";
        if(!ValidationHelper.isNullOrEmpty(invoice.getClient())){
            if(!ValidationHelper.isNullOrEmpty(invoice.getClient().getAddressStreet())){
                customerAddress = invoice.getClient().getAddressStreet();
            }
            if(!ValidationHelper.isNullOrEmpty(invoice.getClient().getAddressHouseNumber())){
                customerAddress += invoice.getClient().getAddressHouseNumber();
            }
        }
        context.put("customerAddress", customerAddress);
        context.put("customerPostcode", invoice.getClient().getAddressPostalCode());
        context.put("customerCity", invoice.getClient().getAddressCityId() != null
                ? invoice.getClient().getAddressCityId().getDescription() : "");
        context.put("customerProvince", invoice.getClient().getAddressProvinceId() != null
                ? invoice.getClient().getAddressProvinceId().getCode() : "");
        context.put("customerCountry", "IT");
        context.put("customerFiscalCode", invoice.getClient().getFiscalCode());
        context.put("customerVatCode", invoice.getClient().getNumberVAT());
        context.put("customerCellPhone", invoice.getClient().getPhone());
        if(!ValidationHelper.isNullOrEmpty(invoice.getClient().getEmails()))
            context.put("customerEmail", invoice.getClient().getEmails().get(0).getEmail());
        else
            context.put("customerEmail", "");
        context.put("feCustomerPec", invoice.getClient().getMailPEC());
        context.put("feDestinationCode", invoice.getClient().getAddressSDI());
        context.put("fePaymentCode", invoice.getPaymentType().getCode()); 
        context.put("paymentMethodName", invoice.getPaymentType().getDescription());
        if(!ValidationHelper.isNullOrEmpty( invoice.getPaymentType().getIban()))
            context.put("paymentMethodDescription", invoice.getPaymentType().getIban());
        else
            context.put("paymentMethodDescription", "");
        context.put("totalWithoutTax", getTotalAmount(invoiceItems));
        context.put("vatAmount", getTotalVat(invoiceItems));
        context.put("total", getTotalGrossAmount(invoiceItems));
        context.put("footNotes", "");
        context.put("sendEmail", false);
        context.put("invoiceItems", invoiceItems);
        context.put("numero", invoice.getInvoiceNumber());
        if(!ValidationHelper.isNullOrEmpty(invoice.getNotes())){
            context.put("invoiceNote", invoice.getNotes());
        }

        else
            context.put("invoiceNote", "");
        if(!ValidationHelper.isNullOrEmpty(invoice.getDate())){
            context.put("invoiceDate", DateTimeHelper.toFormatedString(invoice.getDate(),
                    DateTimeHelper.getMySQLDatePattern()));
        }

        return context;
    }

    public FatturaAPIResponse callFatturaAPI(String xmlSource, Log log) {
        log.info("Invoice XMLData : " + xmlSource);
        String apiURL =
                ApplicationSettingsHolder.getInstance().getByKey(
                        ApplicationSettingsKeys.CLOUD_API_URL).getValue().trim();
        String apiKEY =
                ApplicationSettingsHolder.getInstance().getByKey(
                        ApplicationSettingsKeys.CLOUD_API_KEY).getValue().trim();

        FatturaAPIResponse fatturaAPIResponse;
        try {
            CloseableHttpResponse response = new APICall().apiCall(xmlSource, apiURL,apiKEY, log);
            log.info("Response code : "+ response.getStatusLine().getStatusCode());
            if (response.getStatusLine().getStatusCode() != 200) {
                return null;
            }
            String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
            StringReader sr = new StringReader(responseString);
            JAXBContext jaxbContext = JAXBContext.newInstance(FatturaAPIResponse.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            fatturaAPIResponse = (FatturaAPIResponse) unmarshaller.unmarshal(sr);
            log.info("Return code : "+ fatturaAPIResponse.getReturnCode());
        }catch(Exception e){
            return null;
        }
        return fatturaAPIResponse;
    }

    public Double getTotalAmount(List<InvoiceItem> invoiceItems) {
        Double totalAmount = 0D;
        for(InvoiceItem item : invoiceItems){
            if(!ValidationHelper.isNullOrEmpty(item.getInvoiceTotalCost()))
                totalAmount += item.getInvoiceTotalCost();
        }
        BigDecimal tot = BigDecimal.valueOf(totalAmount);
        tot = tot.setScale(2, RoundingMode.HALF_UP);
        totalAmount = tot.doubleValue();
        return totalAmount;
    }

    public Double getTotalVat(List<InvoiceItem> invoiceItems) throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        Double totalVat = 0D;
        for (InvoiceItem item : invoiceItems) {
            if (!ValidationHelper.isNullOrEmpty(item.getInvoiceTotalCost())) {
                if (!ValidationHelper.isNullOrEmpty(item.getTaxRate())) {
                    TaxRate taxrate = DaoManager.get(TaxRate.class, item.getTaxRate().getId());
                    if (!ValidationHelper.isNullOrEmpty(taxrate.getPercentage())) {
                        totalVat += item.getInvoiceTotalCost().doubleValue() * (taxrate.getPercentage().doubleValue() / 100);
                    }
                }
            }
        }
        BigDecimal tot = BigDecimal.valueOf(totalVat);
        tot = tot.setScale(2, RoundingMode.HALF_UP);
        totalVat = tot.doubleValue();
        return totalVat;
    }

    public Double getTotalGrossAmount(List<InvoiceItem> invoiceItems) throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        Double totalGross = getTotalAmount(invoiceItems) + getTotalVat(invoiceItems);
        BigDecimal tot = BigDecimal.valueOf(totalGross);
        tot = tot.setScale(2, RoundingMode.HALF_UP);
        totalGross = tot.doubleValue();
        return totalGross;
    }

}
