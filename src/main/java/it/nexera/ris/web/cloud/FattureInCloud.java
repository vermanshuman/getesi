package it.nexera.ris.web.cloud;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.poi.util.IOUtils;
import org.json.JSONObject;

public class FattureInCloud {
	private String uid;
	private String apiKey;
	
	private static String ENDPOINT = "https://api.fattureincloud.it/v1";
	
	public JSONObject makePostRequest(String address, 
			JSONObject map){
		try {
			CloseableHttpClient client = HttpClients.createDefault();
			
			HttpPost httpPost = new HttpPost(ENDPOINT+address);
			
			JSONObject object = new JSONObject();
		
			object.put("api_uid", getUid());
			object.put("api_key", getApiKey());
			
			for(String key : map.keySet())
				object.put(key, map.get(key));
		
			StringEntity entity = new StringEntity(object.toString());
			httpPost.setEntity(entity);
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");

			CloseableHttpResponse response = client.execute(httpPost);
	    	
	    	byte[] content = IOUtils.toByteArray(response.getEntity().getContent());
	    	
	    	JSONObject responseObject = new JSONObject(new String(content));
	    				
        	client.close();
        
			return responseObject;
		}
		
		catch(Exception e) {
			//log.error(e);
			e.printStackTrace();
			return null;
		}
	}
	
	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	
	public FattureInCloud(String uid, String apiKey) {
		this.uid = uid;
		this.apiKey = apiKey;
	}
	
	public JSONObject genericRequest() {
		return makePostRequest("/richiesta/info", new JSONObject());
	}

	public JSONObject getClientList(JSONObject object) {
		return makePostRequest("/clienti/lista", object);
	}
	
	public JSONObject newClient(JSONObject object) {
		return makePostRequest("/clienti/nuovo", object);
	}
	
	public JSONObject importClients(JSONObject objects) {
		return makePostRequest("/clienti/importa", objects);
	}
	
	public JSONObject editClient(JSONObject object) {
		return makePostRequest("/clienti/modifica", object);
	}
	
	public JSONObject removeClient(JSONObject object) {
		return makePostRequest("/clienti/elimina", object);
	}
	
	public JSONObject getSupplierList(JSONObject object) {
		return makePostRequest("/fornitori/lista", object);
	}
	
	public JSONObject newSupplier(JSONObject object) {
		return makePostRequest("/fornitori/nuovo", object);
	}
	
	public JSONObject importSuppliers(JSONObject objects) {
		return makePostRequest("/fornitori/importa", objects);
	}
	
	public JSONObject editSupplier(JSONObject object) {
		return makePostRequest("/fornitori/modifica", object);
	}
	
	public JSONObject removeSuppliers(JSONObject object) {
		return makePostRequest("/fornitori/elimina", object);
	}
	
	public JSONObject getProductList(JSONObject object) {
		return makePostRequest("/prodotti/lista", object);
	}
	
	public JSONObject newProduct(JSONObject object) {
		return makePostRequest("/prodotti/nuovo", object);
	}
	
	public JSONObject importProducts(JSONObject objects) {
		return makePostRequest("/prodotti/importa", objects);
	}
	
	public JSONObject editProducts(JSONObject object) {
		return makePostRequest("/prodotti/modifica", object);
	}
	
	public JSONObject removeProducts(JSONObject object) {
		return makePostRequest("/prodotti/elimina", object);
	}
	
	public JSONObject getPurchaseList(JSONObject object) {
		return makePostRequest("/acquisti/lista", object);
	}
	
	public JSONObject newPurchase(JSONObject object) {
		return makePostRequest("/acquisti/nuovo", object);
	}
	
	public JSONObject importPurchases(JSONObject objects) {
		return makePostRequest("/acquisti/importa", objects);
	}
	
	public JSONObject editPurchases(JSONObject object) {
		return makePostRequest("/acquisti/modifica", object);
	}
	
	public JSONObject removePurchases(JSONObject object) {
		return makePostRequest("/acquisti/elimina", object);
	}
	
	public JSONObject getCompensationList(JSONObject object) {
		return makePostRequest("/corrispettivi/lista", object);
	}
	
	public JSONObject newCompensation(JSONObject object) {
		return makePostRequest("/corrispettivi/nuovo", object);
	}
	
	public JSONObject importCompensations(JSONObject objects) {
		return makePostRequest("/corrispettivi/importa", objects);
	}
	
	public JSONObject editCompensations(JSONObject object) {
		return makePostRequest("/corrispettivi/modifica", object);
	}
	
	public JSONObject removeCompensations(JSONObject object) {
		return makePostRequest("/corrispettivi/elimina", object);
	}

	public JSONObject getGoodsArrivalList(JSONObject object) {
		return makePostRequest("/arrivimerce/lista", object);
	}
	
	public JSONObject getGoodsArrivalDetails(JSONObject object) {
		return makePostRequest("/arrivimerce/dettagli", object);
	}
	
	public JSONObject getMailList(JSONObject object) {
		return makePostRequest("/mail/lista", object);
	}


	public JSONObject getReceiptList(JSONObject object) {
		return makePostRequest("/ricevute/lista", object);
	}
	
	public JSONObject getReceiptDetails(JSONObject object) {
		return makePostRequest("/ricevute/dettagli", object);
	}
	
	public JSONObject getReceiptInfo(JSONObject object) {
		return makePostRequest("/ricevute/info", object);
	}
	
	public JSONObject newReceipt(JSONObject object) {
		return makePostRequest("/ricevute/nuovo", object);
	}
	
	public JSONObject importReceipts(JSONObject objects) {
		return makePostRequest("/ricevute/importa", objects);
	}
	
	public JSONObject editReceipt(JSONObject object) {
		return makePostRequest("/ricevute/modifica", object);
	}
	
	public JSONObject removeReceipt(JSONObject object) {
		return makePostRequest("/ricevute/elimina", object);
	}
	
	public JSONObject getReceiptMailInfo(JSONObject object) {
		return makePostRequest("/ricevute/mailinfo", object);
	}
	
	public JSONObject receiptSendMail(JSONObject object) {
		return makePostRequest("/ricevute/inviamail", object);
	}
	
	public JSONObject getProformaList(JSONObject object) {
		return makePostRequest("/proforma/lista", object);
	}
	
	public JSONObject getProformaDetails(JSONObject object) {
		return makePostRequest("/proforma/dettagli", object);
	}
	
	public JSONObject getProformaInfo(JSONObject object) {
		return makePostRequest("/proforma/info", object);
	}
	
	public JSONObject newProforma(JSONObject object) {
		return makePostRequest("/proforma/nuovo", object);
	}
	
	public JSONObject importProformas(JSONObject objects) {
		return makePostRequest("/proforma/importa", objects);
	}
	
	public JSONObject editProforma(JSONObject object) {
		return makePostRequest("/proforma/modifica", object);
	}
	
	public JSONObject removeProforma(JSONObject object) {
		return makePostRequest("/proforma/elimina", object);
	}
	
	public JSONObject getProformaMailInfo(JSONObject object) {
		return makePostRequest("/proforma/mailinfo", object);
	}
	
	public JSONObject proformaSendMail(JSONObject object) {
		return makePostRequest("/proforma/inviamail", object);
	}
	
	public JSONObject getOrderList(JSONObject object) {
		return makePostRequest("/ordini/lista", object);
	}
	
	public JSONObject getOrderDetails(JSONObject object) {
		return makePostRequest("/ordini/dettagli", object);
	}
	
	public JSONObject getOrderInfo(JSONObject object) {
		return makePostRequest("/ordini/info", object);
	}
	
	public JSONObject newOrder(JSONObject object) {
		return makePostRequest("/ordini/nuovo", object);
	}
	
	public JSONObject importOrders(JSONObject objects) {
		return makePostRequest("/ordini/importa", objects);
	}
	
	public JSONObject editOrder(JSONObject object) {
		return makePostRequest("/ordini/modifica", object);
	}
	
	public JSONObject removeOrder(JSONObject object) {
		return makePostRequest("/ordini/elimina", object);
	}
	
	public JSONObject getOrderMailInfo(JSONObject object) {
		return makePostRequest("/ordini/mailinfo", object);
	}
	
	public JSONObject orderSendMail(JSONObject object) {
		return makePostRequest("/ordini/inviamail", object);
	}
	
	public JSONObject getQuotationList(JSONObject object) {
		return makePostRequest("/preventivi/lista", object);
	}
	
	public JSONObject getQuotationDetails(JSONObject object) {
		return makePostRequest("/preventivi/dettagli", object);
	}
	
	public JSONObject getQuotationInfo(JSONObject object) {
		return makePostRequest("/preventivi/info", object);
	}
	
	public JSONObject newQuotation(JSONObject object) {
		return makePostRequest("/preventivi/nuovo", object);
	}
	
	public JSONObject importQuotations(JSONObject objects) {
		return makePostRequest("/preventivi/importa", objects);
	}
	
	public JSONObject editQuotation(JSONObject object) {
		return makePostRequest("/preventivi/modifica", object);
	}
	
	public JSONObject removeQuotation(JSONObject object) {
		return makePostRequest("/preventivi/elimina", object);
	}
	
	public JSONObject getQuotationMailInfo(JSONObject object) {
		return makePostRequest("/preventivi/mailinfo", object);
	}
	
	public JSONObject quotationSendMail(JSONObject object) {
		return makePostRequest("/preventivi/inviamail", object);
	}
	
	
	public JSONObject getInvoiceList(JSONObject object) {
		return makePostRequest("/fatture/lista", object);
	}
	
	public JSONObject getInvoiceDetails(JSONObject object) {
		return makePostRequest("/fatture/dettagli", object);
	}
	
	public JSONObject getInvoiceInfo(JSONObject object) {
		return makePostRequest("/fatture/info", object);
	}
	
	public JSONObject newInvoice(JSONObject object) {
		return makePostRequest("/fatture/nuovo", object);
	}
	
	public JSONObject importInvoices(JSONObject objects) {
		return makePostRequest("/fatture/importa", objects);
	}
	
	public JSONObject editInvoice(JSONObject object) {
		return makePostRequest("/fatture/modifica", object);
	}
	
	public JSONObject removeInvoice(JSONObject object) {
		return makePostRequest("/fatture/elimina", object);
	}
	
	public JSONObject getInvoiceMailInfo(JSONObject object) {
		return makePostRequest("/fatture/mailinfo", object);
	}
	
	public JSONObject invoiceSendMail(JSONObject object) {
		return makePostRequest("/fatture/inviamail", object);
	}
	
	public JSONObject getNdcList(JSONObject object) {
		return makePostRequest("/ndc/lista", object);
	}
	
	public JSONObject getNdcDetails(JSONObject object) {
		return makePostRequest("/ndc/dettagli", object);
	}
	
	public JSONObject getNdcInfo(JSONObject object) {
		return makePostRequest("/ndc/info", object);
	}
	
	public JSONObject newNdc(JSONObject object) {
		return makePostRequest("/ndc/nuovo", object);
	}
	
	public JSONObject importNdcs(JSONObject objects) {
		return makePostRequest("/ndc/importa", objects);
	}
	
	public JSONObject editNdc(JSONObject object) {
		return makePostRequest("/ndc/modifica", object);
	}
	
	public JSONObject removeNdc(JSONObject object) {
		return makePostRequest("/ndc/elimina", object);
	}
	
	public JSONObject getNdcMailInfo(JSONObject object) {
		return makePostRequest("/ndc/mailinfo", object);
	}
	
	public JSONObject ndcSendMail(JSONObject object) {
		return makePostRequest("/ndc/inviamail", object);
	}
	
	public JSONObject getDdtList(JSONObject object) {
		return makePostRequest("/ddt/lista", object);
	}
	
	public JSONObject getDdtDetails(JSONObject object) {
		return makePostRequest("/ddt/dettagli", object);
	}
	
	public JSONObject getDdtInfo(JSONObject object) {
		return makePostRequest("/ddt/info", object);
	}
	
	public JSONObject newDdt(JSONObject object) {
		return makePostRequest("/ddt/nuovo", object);
	}
	
	public JSONObject importDdts(JSONObject objects) {
		return makePostRequest("/ddt/importa", objects);
	}
	
	public JSONObject editDdt(JSONObject object) {
		return makePostRequest("/ddt/modifica", object);
	}
	
	public JSONObject removeDdt(JSONObject object) {
		return makePostRequest("/ddt/elimina", object);
	}
	
	public JSONObject getDdtMailInfo(JSONObject object) {
		return makePostRequest("/ddt/mailinfo", object);
	}
	
	public JSONObject ddtSendMail(JSONObject object) {
		return makePostRequest("/ddt/inviamail", object);
	}
	
	public JSONObject getRelationshipList(JSONObject object) {
		return makePostRequest("/rapporti/lista", object);
	}
	
	public JSONObject getRelationshipDetails(JSONObject object) {
		return makePostRequest("/rapporti/dettagli", object);
	}
	
	public JSONObject getRelationshipInfo(JSONObject object) {
		return makePostRequest("/rapporti/info", object);
	}
	
	public JSONObject newRelationship(JSONObject object) {
		return makePostRequest("/rapporti/nuovo", object);
	}
	
	public JSONObject importRelationships(JSONObject objects) {
		return makePostRequest("/rapporti/importa", objects);
	}
	
	public JSONObject editRelationship(JSONObject object) {
		return makePostRequest("/rapporti/modifica", object);
	}
	
	public JSONObject removeRelationship(JSONObject object) {
		return makePostRequest("/rapporti/elimina", object);
	}
	
	public JSONObject getRelationshipMailInfo(JSONObject object) {
		return makePostRequest("/rapporti/mailinfo", object);
	}
	
	public JSONObject relationshipSendMail(JSONObject object) {
		return makePostRequest("/rapporti/inviamail", object);
	}
	
	public JSONObject getSupplierOrderList(JSONObject object) {
		return makePostRequest("/ordforn/lista", object);
	}
	
	public JSONObject getSupplierOrderDetails(JSONObject object) {
		return makePostRequest("/ordforn/dettagli", object);
	}
	
	public JSONObject getSupplierOrderInfo(JSONObject object) {
		return makePostRequest("/ordforn/info", object);
	}
	
	public JSONObject newSupplierOrder(JSONObject object) {
		return makePostRequest("/ordforn/nuovo", object);
	}
	
	public JSONObject importSupplierOrders(JSONObject objects) {
		return makePostRequest("/ordforn/importa", objects);
	}
	
	public JSONObject editSupplierOrder(JSONObject object) {
		return makePostRequest("/ordforn/modifica", object);
	}
	
	public JSONObject removeSupplierOrder(JSONObject object) {
		return makePostRequest("/ordforn/elimina", object);
	}
	
	public JSONObject getSupplierOrderMailInfo(JSONObject object) {
		return makePostRequest("/ordforn/mailinfo", object);
	}
	
	public JSONObject supplierOrderSendMail(JSONObject object) {
		return makePostRequest("/ordforn/inviamail", object);
	}
	
	public JSONObject getAccountInfo() {
		return makePostRequest("/info/account", new JSONObject());
	}
	
	public static boolean wasSuccessful(JSONObject object) {
		return (object != null) && object.has("success") && object.getBoolean("success");
	}
}
