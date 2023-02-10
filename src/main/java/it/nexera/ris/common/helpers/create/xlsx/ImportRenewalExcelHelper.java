package it.nexera.ris.common.helpers.create.xlsx;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.primefaces.model.UploadedFile;

import com.monitorjbl.xlsx.StreamingReader;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.enums.SubjectType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.SubjectHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.common.helpers.create.xls.XlsxHelper;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Client;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.persistence.beans.entities.domain.RequestSubject;
import it.nexera.ris.persistence.beans.entities.domain.Subject;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.AggregationLandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RequestType;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;
import it.nexera.ris.settings.ApplicationSettingsHolder;

public final class ImportRenewalExcelHelper {

    protected static final Log log = LogFactory.getLog(ImportRenewalExcelHelper.class);

    /*static {
        try {
        	importRenewalExcel();
		} catch (HibernateException | IllegalAccessException | InstantiationException | PersistenceBeanException e) {
			e.printStackTrace();
			log.error(e);
		}
    }*/

    public synchronized static void importRenewalExcel(byte[] excelContents, Long clientId) throws HibernateException, IllegalAccessException, PersistenceBeanException, InstantiationException {

        try {
        	InputStream inputStream = new ByteArrayInputStream(excelContents);
            Workbook workbook = StreamingReader.builder()
                    .rowCacheSize(100)
                    .open(inputStream);
            inputStream.close();
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.rowIterator();

            Row row = rowIterator.next();
            if(checkIfRowIsEmpty(row)) {
            	row = rowIterator.next();
            }
            int cellWithImportExcelDate = 0;
            int cellWithImportExcelClient = 0;
            int cellWithImportExcelNdg = 0;
            int cellWithImportExcelManager = 0;
            int cellWithImportExcelTipo = 0;
            int cellWithImportExcelNameSurname = 0;
            int cellWithImportExcelName = 0;
            int cellWithImportExcelTaxcodeVat = 0;
            int cellWithImportExcelMortgageImport = 0;
            int cellWithImportExcelActType = 0;
            int cellWithImportExcelNaturalAct = 0;
            int cellWithImportExcelAggregationLandCharReg = 0;
            int cellWithImportExcelActDate = 0;
            int cellWithImportExcelNoPart = 0;
            int cellWithImportExcelNoGen = 0;
            int cellWithImportExcelNote = 0;
            		
            if(!checkIfRowIsEmpty(row)) {
	             cellWithImportExcelDate = XlsxHelper.findColumnIndexByName(row, ResourcesHelper.getString("importExcelDate"));
	             cellWithImportExcelClient = XlsxHelper.findColumnIndexByName(row, ResourcesHelper.getString("importExcelClient"));
	             cellWithImportExcelNdg = XlsxHelper.findColumnIndexByName(row, ResourcesHelper.getString("importExcelNdg"));
	             cellWithImportExcelManager = XlsxHelper.findColumnIndexByName(row, ResourcesHelper.getString("importExcelManager"));
	             cellWithImportExcelTipo = XlsxHelper.findColumnIndexByName(row, ResourcesHelper.getString("importExcelType"));
	             cellWithImportExcelNameSurname = XlsxHelper.findColumnIndexByName(row, ResourcesHelper.getString("importExcelNameSurname"));
	             cellWithImportExcelName = XlsxHelper.findColumnIndexByName(row, ResourcesHelper.getString("importExcelName"));
	             cellWithImportExcelTaxcodeVat = XlsxHelper.findColumnIndexByName(row, ResourcesHelper.getString("importExcelTaxcodeVat"));
	             cellWithImportExcelMortgageImport = XlsxHelper.findColumnIndexByName(row, ResourcesHelper.getString("importExcelAmount"));
	             cellWithImportExcelActType = XlsxHelper.findColumnIndexByName(row, ResourcesHelper.getString("importExcelDeedType"));
	             cellWithImportExcelNaturalAct = XlsxHelper.findColumnIndexByName(row, ResourcesHelper.getString("importExcelNaturalAct"));
	             cellWithImportExcelAggregationLandCharReg = XlsxHelper.findColumnIndexByName(row, ResourcesHelper.getString("importExcelConservatory"));
	             cellWithImportExcelActDate = XlsxHelper.findColumnIndexByName(row, ResourcesHelper.getString("importExcelMortgageDate"));
	             cellWithImportExcelNoPart = XlsxHelper.findColumnIndexByName(row, ResourcesHelper.getString("importExcelNoPart"));
	             cellWithImportExcelNoGen = XlsxHelper.findColumnIndexByName(row, ResourcesHelper.getString("importExcelNoGen"));
	             cellWithImportExcelNote = XlsxHelper.findColumnIndexByName(row, ResourcesHelper.getString("importExcelNote"));
            }
            
            while (rowIterator.hasNext()) {
            	Request request = new Request();
                row = rowIterator.next();
                if(checkIfRowIsEmpty(row)) {
                	continue;
                }
                /*Cell dateCell = row.getCell(cellWithImportExcelDate);
                if (dateCell != null) {
                    String date  = getCellValue(dateCell);
                    if(!ValidationHelper.isNullOrEmpty(date)) {
                    	
                    }
                }*/
                Client client = DaoManager.get(Client.class, clientId);
                request.setClient(client);
                
                List<Service> services = DaoManager.load(Service.class, new Criterion[] {
                        Restrictions.eq("manageRenewal", Boolean.TRUE)},
                		new Order[]{
                                Order.desc("id")});
                request.setService(services.get(0));
                request.setRequestType(services.get(0).getRequestType());
                
                Cell clientCell = row.getCell(cellWithImportExcelClient);
                if (clientCell != null) {
                    String clientStr  = getCellValue(clientCell);
                    if(!ValidationHelper.isNullOrEmpty(clientStr)) {
                    	
                    }
                }
                
                Cell ndgCell = row.getCell(cellWithImportExcelNdg);
                if (ndgCell != null) {
                    String ndg  = getCellValue(ndgCell);
                    if(!ValidationHelper.isNullOrEmpty(ndg)) {
                    	request.setNdg(ndg);
                    }
                }
                
                Cell managerCell = row.getCell(cellWithImportExcelManager);
                if (managerCell != null) {
                    String manager  = getCellValue(managerCell);
                    if(!ValidationHelper.isNullOrEmpty(manager)) {
                    	request.setRequestMangerList(new ArrayList<>());
                        List<Client> clients = DaoManager.load(Client.class, new Criterion[] {
                                Restrictions.eq("clientName", manager.toLowerCase())});
                        request.getRequestMangerList().addAll(clients);
                    }
                }
                
                String fiscalCode = null;
                Cell fiscalCodeCell = row.getCell(cellWithImportExcelTaxcodeVat);
                if (fiscalCodeCell != null) {
                    fiscalCode  = getCellValue(fiscalCodeCell);
                }
                
                String surname  = null;
                Cell surnameCell = row.getCell(cellWithImportExcelNameSurname);
                if (surnameCell != null) {
                    surname  = getCellValue(surnameCell);
                }
                
                String name  = null;
                Cell nameCell = row.getCell(cellWithImportExcelName);
                if (nameCell != null) {
                	name  = getCellValue(nameCell);
                }
                
                Cell tipoCell = row.getCell(cellWithImportExcelTipo);
                Subject newSubject = null;
                if (tipoCell != null) {
                    String tipo  = getCellValue(tipoCell);
                    if(!ValidationHelper.isNullOrEmpty(tipo)) {
                    	//physical subject
                    	if(tipo.equalsIgnoreCase("P")) {
                    		newSubject = new Subject();
                			newSubject.setName(name);
                			newSubject.setSurname(surname);
                			newSubject.setFiscalCode(fiscalCode);
                			newSubject.setNumberVAT(fiscalCode);
                			newSubject.setTypeId(SubjectType.PHYSICAL_PERSON.getId());
                    	}
                    	if(tipo.equalsIgnoreCase("S")) {
                    		newSubject = new Subject();
                			newSubject.setBusinessName(surname);
                			newSubject.setFiscalCode(fiscalCode);
                			newSubject.setNumberVAT(fiscalCode);
                			newSubject.setTypeId(SubjectType.LEGAL_PERSON.getId());
                    	}
                    	
                    }
                }
                
                Cell mortgageImportCell = row.getCell(cellWithImportExcelMortgageImport);
                if (mortgageImportCell != null) {
                    double mortgageImport  = Double.parseDouble(getCellValue(mortgageImportCell));
                    if(!ValidationHelper.isNullOrEmpty(mortgageImport)) {
                    	request.setMortagageImport(mortgageImport);
                    }
                }
                
                Cell actTypeCell = row.getCell(cellWithImportExcelActType);
                if (actTypeCell != null) {
                    String actType  = getCellValue(actTypeCell);
                    if(!ValidationHelper.isNullOrEmpty(actType)) {
                    	request.setActType(actType);
                    }
                }
                
                Cell aggregationLandCharRegCell = row.getCell(cellWithImportExcelAggregationLandCharReg);
                if (aggregationLandCharRegCell != null) {
                    String aggregationLandCharReg  = getCellValue(aggregationLandCharRegCell);
                    if(!ValidationHelper.isNullOrEmpty(aggregationLandCharReg)) {
                    	AggregationLandChargesRegistry aggregationLandChargesRegistry = DaoManager.get(AggregationLandChargesRegistry.class, 
                    			new Criterion[] {Restrictions.eq("name", aggregationLandCharReg.toUpperCase())});
                    	request.setAggregationLandChargesRegistry(aggregationLandChargesRegistry);
                    }
                }
                
                Cell actDateCell = row.getCell(cellWithImportExcelActDate);
                if (actDateCell != null) {
                	if(DateUtil.isCellDateFormatted(actDateCell)) {
                		Date actDate  = actDateCell.getDateCellValue();
	                    if(!ValidationHelper.isNullOrEmpty(actDate)) {
	                    	request.setActDate(actDate);
	                    }
                	}
                }
                
                String noPart = "";
                Cell noPartCell = row.getCell(cellWithImportExcelNoPart);
                if (noPartCell != null) {
                    noPart  = getCellValue(noPartCell);
                }
                
                String noGen  = "";
                Cell noGenCell = row.getCell(cellWithImportExcelNoGen);
                if (noGenCell != null) {
                    noGen  = getCellValue(noGenCell);
                }
                
                String actNumber = "..";
                if(!ValidationHelper.isNullOrEmpty(noPart)) {
                	actNumber = noPart;
                }
                if(!ValidationHelper.isNullOrEmpty(noGen)) {
                	actNumber = actNumber + "/" + noGen;
                } else {
                	actNumber = actNumber + "/" + "..";
                }
                request.setActNumber(actNumber);
                
                Cell noteCell = row.getCell(cellWithImportExcelNote);
                if (noteCell != null) {
                    String note  = getCellValue(noteCell);
                    if(!ValidationHelper.isNullOrEmpty(note)) {
                    	request.setNote(note);
                    }
                }
                
                DaoManager.save(request, true);
                request.setSubjectList(new LinkedList<>());
                if(newSubject != null) {
                	Subject subjectFromDB = SubjectHelper.getSubjectIfExists(newSubject, newSubject.getTypeId());
                    if (subjectFromDB != null) {
                        boolean isRequestSubjectExist = request.getSubjectList().stream()
                                .anyMatch(sw -> sw.getId().equals(subjectFromDB.getId()));
                        if(!isRequestSubjectExist) {
                            request.getSubjectList().add(subjectFromDB);
                        }
                        newSubject = subjectFromDB;
                    }
                    else {
                        DaoManager.save(newSubject, true);
                        request.getSubjectList().add(newSubject);
                    }
            	}
                request.setSubject(newSubject);
                DaoManager.save(request, true);
                RequestSubject requestSubject = new RequestSubject(request, newSubject);
                
                Entry<Long, String> result = request.getSubjectTypeMapping().entrySet().stream()
                        .filter(x -> x.getKey() == request.getSubject().getId()).findFirst().orElse(null);
                if (!ValidationHelper.isNullOrEmpty(result)) {
                    requestSubject.setType(result.getValue());
                }
                ConnectionManager.saveObject(requestSubject, true, DaoManager.getSession());
            }
        } catch (IOException e) {
            LogHelper.log(log, e);
        }
    }
    
    private static boolean checkIfRowIsEmpty(Row row) {
        if (row == null) {
            return true;
        }
        if (row.getLastCellNum() <= 0) {
            return true;
        }
        for (int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
            Cell cell = row.getCell(cellNum);
            if (cell != null && cell.getCellType() != CellType.BLANK && !ValidationHelper.isNullOrEmpty(cell.toString())) {
                return false;
            }
        }
        return true;
    }
    
    private static String getCellValue(Cell cell) {
		String val = null;

		switch (cell.getCellType()) {
			case NUMERIC:
				val = (long) cell.getNumericCellValue() + "";
				break;
			case STRING:
				val = cell.getStringCellValue();
				break;
			case BLANK:
				break;
			case BOOLEAN:
				val = String.valueOf(cell.getBooleanCellValue());
				break;
			case ERROR:
				break;
			case FORMULA:
				switch(cell.getCachedFormulaResultType()) {
		            case NUMERIC:
		                val = String.valueOf(cell.getNumericCellValue());
		                break;
		            case STRING:
		                val = cell.getRichStringCellValue().getString();
		                break;
		            case _NONE:
		    			break;
					default:
						break;
				}
				break;
			case _NONE:
				break;
			default:
				break;
		}
		return val;
    }

}