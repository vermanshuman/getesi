package it.nexera.ris.api;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.FileHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Invoice;
import it.nexera.ris.persistence.beans.entities.domain.InvoiceItem;
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
import java.util.List;

public class FatturaAPI {

    public transient final Log log = LogFactory.getLog(getClass());
    public String getDataForXML(Invoice invoice, List<InvoiceItem> invoiceItems) throws HibernateException, IllegalAccessException, PersistenceBeanException {
        if(invoiceItems == null)
            invoiceItems = DaoManager.load(InvoiceItem.class, new Criterion[] { Restrictions.eq("invoice.id", invoice.getId()) });
        //first, get and initialize an engine
        VelocityEngine velocityEngine = new VelocityEngine();

        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER,"class,file");
        velocityEngine.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        velocityEngine.init();
        //add data to a VelocityContext
        VelocityContext context = addDataToVelocityContext(invoice, invoiceItems);
        //get the Template
        Template template = velocityEngine.getTemplate(FileHelper.getFatturaAPITemplatePath());
        //now render the template into a Writer, here  a StringWriter
        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        return writer.toString();
    }

    public String getDataForXML(Invoice invoice) throws HibernateException, IllegalAccessException, PersistenceBeanException {
        return getDataForXML(invoice, null);
    }

    public VelocityContext addDataToVelocityContext(Invoice invoice, List<InvoiceItem> invoiceItems) {
        VelocityContext context = new VelocityContext();
        context.put("documentType", invoice.getDocumentType());
        String customerName = invoice.getClient().getNameOfTheCompany();
        if(!(customerName != null && !customerName.equals("")))
            customerName = invoice.getClient().getNameProfessional();
        context.put("customerName", customerName);
        String customerAddress = invoice.getClient().getAddressHouseNumber() != null ? invoice.getClient().getAddressHouseNumber() : ""
                + invoice.getClient().getAddressStreet() != null ? invoice.getClient().getAddressStreet() : "";
        context.put("customerAddress", customerAddress);
        context.put("customerPostcode", invoice.getClient().getAddressPostalCode());
        context.put("customerCity", invoice.getClient().getAddressCityId() != null ? invoice.getClient().getAddressCityId().getDescription() : "");
        context.put("customerProvince", invoice.getClient().getAddressProvinceId() != null ? invoice.getClient().getAddressProvinceId().getCode() : "");
        context.put("customerCountry", invoice.getClient().getCountry() != null ? invoice.getClient().getCountry().getDescription() : "");
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
        if(!ValidationHelper.isNullOrEmpty(invoice.getNotes()))
            context.put("invoiceNote", invoice.getNotes());
        else
            context.put("invoiceNote", "");
        if(!ValidationHelper.isNullOrEmpty(invoice.getDate())){
            context.put("invoiceDate", DateTimeHelper.toFormatedString(invoice.getDate(),
                    DateTimeHelper.getMySQLDatePattern()));
        }

        return context;
    }

    public FatturaAPIResponse callFatturaAPI(String xmlSource, Log log) {
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
            if(!ValidationHelper.isNullOrEmpty(item.getAmount()))
                totalAmount += item.getAmount();
        }

        return totalAmount;
    }

    public Double getTotalVat(List<InvoiceItem> invoiceItems) {
        Double totalVat = 0D;
        for(InvoiceItem item : invoiceItems)
            if(!ValidationHelper.isNullOrEmpty(item.getVatAmount()))
                totalVat += item.getVatAmount();
        return totalVat;
    }

    public Double getTotalGrossAmount(List<InvoiceItem> invoiceItems) {
        return getTotalAmount(invoiceItems) + getTotalVat(invoiceItems);
    }

}
