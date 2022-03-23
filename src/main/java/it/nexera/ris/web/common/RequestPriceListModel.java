package it.nexera.ris.web.common;

import it.nexera.ris.persistence.beans.entities.domain.Client;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.persistence.beans.entities.domain.TaxRate;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;
import lombok.Data;

@Data
public class RequestPriceListModel {
	private Long requestId;
	private Request request;
	private double totalCost;
	private double amount;
	private TaxRate taxRate;
	private Client client;
	private Service service;
}
