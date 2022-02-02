package it.nexera.ris.web.services;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.primefaces.push.EventBus;
import org.primefaces.push.EventBusFactory;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.enums.SubjectType;
import it.nexera.ris.common.helpers.BaseHelper;
import it.nexera.ris.common.helpers.CalcoloCodiceFiscale;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.common.helpers.create.xls.XlsxHelper;
import it.nexera.ris.common.utils.ProcessMonitor;
import it.nexera.ris.persistence.PersistenceSession;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.entities.domain.ReportFormalitySubject;
import it.nexera.ris.persistence.beans.entities.domain.Subject;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.LandChargesRegistry;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.services.base.BaseService;

public class ImportReportFormalitySubjectService extends BaseService {

	private static final int MAX_NUMBER_OF_ELEMENTS_BEFORE_COMMIT = 1000;
	
    private static transient final Log log = LogFactory.getLog(BaseHelper.class);

    private ProcessMonitor processMonitor;

    private Boolean showProgressPanel;

    public ImportReportFormalitySubjectService() {
        super("ImportReportFormalitySubjectService");
    }

    @Override
    protected void preStart() {
        setShowProgressPanel(true);
        setNotWaitBeforeStop(true);
    }
    
    @Override
    protected void postStart() {
    	socketPush();
    }
    
    @Override
    protected void postRoutineFunc() {
        super.postRoutineFunc();
        setShowProgressPanel(false);
        socketPush();
    }
    
    @Override
    protected void onResume() {
    	synchronized(monitor) {
    		monitor.notifyAll();
    	}
    }
    
    @Override
    public boolean isPausingSupported() {
    	return true;
    }

    @Override
    public void routineFuncInternal() {
    	log.info("import record formality subject service ROUTINE!!!");
    	
        PersistenceSession ps = null;
        String filePath = ApplicationSettingsHolder.getInstance().
        		getByKey(ApplicationSettingsKeys.IMPORT_REPORT_FORMALITY_SUBJECT_PATH).getValue();
        
        if (ValidationHelper.isNullOrEmpty(filePath)) {
        	LogHelper.log(log, "Not running record formality subject import: path is null or empty.");
        	return;
        }
        
        int r = 0;
        int numberOfElements = 0;
        
        int curFileNum = 0;
        
        Transaction tr = null;
        
        try {
            LogHelper.log(log, "file path");
            LogHelper.log(log, filePath);
            ps = new PersistenceSession();
            Session session = ps.getSession();
            
            tr = session.beginTransaction();
            
            String permission = ApplicationSettingsHolder.getInstance()
                    .getByKey(ApplicationSettingsKeys.PERMISSION_IMPORT_REPORT_FORMALITY_SUBJECT).getValue();
            LogHelper.log(log, "permission");
            LogHelper.log(log, permission);
            if (permission == null || Boolean.valueOf(permission)) {
                    List<File> filePathList = getFilePathList(filePath);
                    for (File file : filePathList) {
                        LogHelper.log(log, "process file " + file.getName());
                        getProcessMonitor().setStatusStr(String.format("[%s] %s", file.getName(), "Parse excel ..."));
                        socketPush();
                                                
                        Sheet sheet = XlsxHelper.readSheet(file.getPath());
                        
                        getProcessMonitor().setEndValue(sheet.getLastRowNum());
                                                
                        int format = -1;
                        
                        r = 0;
                        
                        for(Iterator<Row> iterator = sheet.rowIterator();
                        		iterator.hasNext(); r++) {
                        	Row row = iterator.next();
                        	City city = null;
                            getProcessMonitor().setStartValue(r);
                            if (r % 10 == 0)
                                socketPush();
                        
                        	if (r == 0) {
                        		String val = getValueFromCell(row.getCell(0));
                        		                        		
                        		if(val.equalsIgnoreCase("COGNOME"))
                        			format = 0;
                        		else if(val.equalsIgnoreCase("RAGIONE_SO"))
                       				format = 1;
                        		else {
                        			LogHelper.log(log, "Invalid format for ImportReportFormalitySubject!");
                        			break;
                        		}
                        		
                        		LogHelper.log(log, "format = " + format);
                        		
                        	} else {
                        		Subject subject = null;
                        		String fiscalCode = null;
                        		int pos = -1;
                           		ReportFormalitySubject rfs = 
                               			new ReportFormalitySubject();
                				
                        		if (format == 0) { // physical person                        			
                        			fiscalCode = getValueFromCell(row.getCell(4));

                    				city = getFirst(ConnectionManager.load(City.class,
                     		               new Criterion[] { Restrictions.eq("cfis", 
                     		            		   getValueFromCell(row.getCell(2))) },
                     		               session));
                    				
                    				rfs.setName(getValueFromCell(row.getCell(1)));
                    				rfs.setSurname(getValueFromCell(row.getCell(0)));
                    			
                    				rfs.setBirthCity(city);
                    				
                    				if (city != null)
                    					rfs.setBirthProvince(city.getProvince());
                    				
                    				rfs.setBirthDate(getDateFromCell(row.getCell(3, 
                    						MissingCellPolicy.CREATE_NULL_AS_BLANK)));
                    				rfs.setFiscalCode(fiscalCode);

                    				if (!ValidationHelper.isNullOrEmpty(fiscalCode)) {
                    					Long sexId = CalcoloCodiceFiscale.getSexFromFiscalCode(fiscalCode);
                    					if(sexId==null) {LogHelper.log(log, "getSexFromFiscalCode error so skipping ...");
                    						continue;
                    					}
                    					rfs.setSex(sexId);
                    				}
                    					
                    				
                    				rfs.setTypeId(SubjectType.PHYSICAL_PERSON.getId());
                    				
                        			pos = 5;
                        		} else if (format == 1) {
									if (row.getCell(2) != null) {
										fiscalCode = row.getCell(2).getStringCellValue();
										fiscalCode = ValidationHelper.validateNumberVat(fiscalCode);
									}

                    				city = getFirst(ConnectionManager.load(City.class,
                      		               new Criterion[] { Restrictions.eq("cfis",
                      		            		   getValueFromCell(row.getCell(1))) },
                      		               session));

                    				rfs.setBusinessName(row.getCell(0).getStringCellValue());
                    				rfs.setBirthCity(city);

                    				if (city != null)
                    					rfs.setBirthProvince(city.getProvince());

                    				rfs.setFiscalCode(fiscalCode);
                    				rfs.setNumberVAT(rfs.getFiscalCode());

                    				rfs.setTypeId(SubjectType.LEGAL_PERSON.getId());

                        			pos = 3;
                        		}
                        		                        		
                        		
                        		String conservatorshipCode =
                            		getValueFromCell(row.getCell(pos++));

                           		String nOfficeStr =
                           			getValueFromCell(row.getCell(pos++));                        				
                            	
                           		int nOffice = -1;
                           		
                           		if(!ValidationHelper.isNullOrEmpty(nOfficeStr)) {
                           		
                           			try {
                           				nOffice = (int) Double.parseDouble(nOfficeStr);
                           			}
                           		
                           			catch(Exception ne) {
                           				ne.printStackTrace();
                           			}
                           		}
                           			
                          		String formalityCode =
                          			getValueFromCell(row.getCell(pos++));
                           						
                           		Date dAct =	getDateFromCell(row.getCell(pos++));
                    
                           		String nAct =
                         			getValueFromCell(row.getCell(pos++));
                           		
                           		Double dbl = getDoubleValue(formalityCode);
                           		                           		
                           		rfs.setTypeFormalityId((dbl == null)
                           				? 1L : dbl.longValue());  		
                           		
                          		rfs.setNumber(nAct);
                          		try {
                          		    if(dAct != null) {
                          		        Timestamp ts=new Timestamp(dAct.getTime());  
                          		        Date dt = new Date();
                          		        dt.setTime(ts.getTime());
                          		        String formattedDate = DateTimeHelper.toFormatedString(dt, "yyyy");
                          		        if(Integer.parseInt(formattedDate) > 2050) {
                          		            throw new Exception();
                          		        }else {
                          		            rfs.setDate(dAct);    
                          		        }
                          		    }else {
                          		        rfs.setDate(dAct);
                          		    }

                          		} catch (Exception e) {
                                  LogHelper.log(log, "Date is not valid for ROW = " + r);
                                  continue;
                                }                               		
                           		
                          		City conservatorshipCity = getFirst(ConnectionManager.load(City.class,
                          		        new CriteriaAlias[]{new CriteriaAlias("landChargesRegistries", "landChargesRegistries", JoinType.INNER_JOIN)},
                          		        new Criterion[] { Restrictions.eq("cfis", 
                          		                conservatorshipCode) },
                          		        session));

                          		LandChargesRegistry landChargesRegistry =
                          		        (conservatorshipCity == null) ? null :
                          		            ((conservatorshipCity.getLandChargesRegistries() == null 
                          		            || conservatorshipCity.getLandChargesRegistries().size() ==0) 
                          		                    ? null : conservatorshipCity.getLandChargesRegistries().get(0));
                           						
                       			rfs.setLandChargesRegistry(landChargesRegistry);
                       			
                       			
                       			List<Criterion> criteria = new ArrayList<>();
                       			
                       			criteria.add(Example.create(rfs));

								if (rfs.getFiscalCode() == null) {
									criteria.add(Restrictions.isNull("fiscalCode"));
								}
                       			
                       			if (landChargesRegistry != null)
                       				criteria.add(Restrictions.eq("landChargesRegistry.id", 
                       					landChargesRegistry.getId()));
                       			
                       			if (ConnectionManager.get(ReportFormalitySubject.class,
                 						criteria.toArray(new Criterion[0]), session) == null) {                       				
                       				ConnectionManager.save(rfs, session);
                       			}
                       			
                   				numberOfElements++;
                       			
                       			if (isPaused() || (numberOfElements > MAX_NUMBER_OF_ELEMENTS_BEFORE_COMMIT)) {
                       				if (tr != null)
                       					tr.commit();
                       				
                       				if (ps != null)
                       					ps.closeSession();
                       				
                       	            ps = new PersistenceSession();
                       	            session = ps.getSession();
                       				
                       				tr = session.beginTransaction();
                       				
                       				numberOfElements = 0;
                       				
                       				if(isPaused()) {
                            			synchronized(monitor) {
                            				monitor.wait();
                            			}
                       				}
                       			}         				
                        	}
                        }
                                                
           				if (tr != null && numberOfElements > 0)
           					tr.commit();
           				           				
                        LogHelper.log(log, "OK");
                    }
            }
        } catch (Exception e) {
        	LogHelper.log(log, "ROW = " + r);
            LogHelper.log(log, e);
            
           if (tr != null)
        	   tr.rollback();
        } finally {
            getProcessMonitor().resetCounters();
            ApplicationSettingsHolder.getInstance()
                    .applyNewValue(ApplicationSettingsKeys.PERMISSION_IMPORT_FORMALITY, "false");
            stopFlag = true;
            
            if (tr != null && !tr.wasCommitted() && !tr.wasRolledBack())
            	tr.commit();
            	
            if (ps != null) {
                ps.closeSession();
            }
        }
    }
 
    private List<File> getFilePathList(String folderPath) {
        File folder = new File(folderPath);
        List<File> fileList = new ArrayList<>();
        File[] existsFiles = folder.listFiles();
        if (folder.isDirectory() && !ValidationHelper.isNullOrEmpty(existsFiles)) {
            for (File f : existsFiles) {
                if (f.getName().contains("xlsx")) {
                    fileList.add(f);
                }
            }
        } else if (folder.isFile()) {
            if (folder.getName().contains("xlsx")) {
                fileList.add(folder);
            }
        }
        return fileList;
    }

    @Override
    protected int getPollTimeKey() {
        return 0;
    }

    private void socketPush() {
        EventBus eventBus = EventBusFactory.getDefault().eventBus();
        eventBus.publish("/notify", "");
    }
    
    private Date getDateFromCell(Cell cell) {
    	if (cell != null) {
    		switch(cell.getCellTypeEnum()) {
    		case STRING:
    			return DateTimeHelper.fromString(cell.getStringCellValue(), "MM/dd/yy");
    		default:
    			return cell.getDateCellValue();
    		}
    	}
    	
    	return null;
    }

    private String getValueFromCell(Cell cell) {
        if (cell != null) {
            switch (cell.getCellTypeEnum()) {
                case NUMERIC:
                   // LogHelper.log(log, "NUMBER " + cell.getNumericCellValue());
                    return String.valueOf(cell.getNumericCellValue());

                case STRING:
                    return cell.getStringCellValue();
                    
                case BOOLEAN:
                	return String.valueOf(cell.getBooleanCellValue());
                	
                default:
            }
            
            LogHelper.log(log, "NOTHING " + cell.getCellTypeEnum().name());
            LogHelper.log(log, "NOTHING " + cell.getRichStringCellValue().getString());
        }

        return null;
    }
    
    public <T> T getFirst(List<T> list) {
    	if (ValidationHelper.isNullOrEmpty(list))
    		return null;
    	
    	if(list.size() > 1) {
    		LogHelper.log(log, "WARNING: Query did not return a unique result.");
    	}
    		
    	return list.get(0);
    }

    public ProcessMonitor getProcessMonitor() {
        return processMonitor;
    }

    public void setProcessMonitor(ProcessMonitor processMonitor) {
        this.processMonitor = processMonitor;
    }

    public void setShowProgressPanel(Boolean showProgressPanel) {
        this.showProgressPanel = showProgressPanel;
    }

    public Boolean getShowProgressPanel() {
        return showProgressPanel;
    }
    
    private Double getDoubleValue(String string) {
    	try {
    		return Double.valueOf(string);    		
    	}
    	
    	catch(Exception ex) {
    		return null;
    	}
    }
}
