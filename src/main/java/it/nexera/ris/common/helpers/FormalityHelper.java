package it.nexera.ris.common.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.Action;
import it.nexera.ris.persistence.TransactionExecuter;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.CadastralData;
import it.nexera.ris.persistence.beans.entities.domain.Document;
import it.nexera.ris.persistence.beans.entities.domain.EstateSituation;
import it.nexera.ris.persistence.beans.entities.domain.Formality;
import it.nexera.ris.persistence.beans.entities.domain.OldProperty;
import it.nexera.ris.persistence.beans.entities.domain.Property;
import it.nexera.ris.persistence.beans.entities.domain.Relationship;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.persistence.beans.entities.domain.SectionA;
import it.nexera.ris.persistence.beans.entities.domain.SectionB;
import it.nexera.ris.persistence.beans.entities.domain.SectionC;
import it.nexera.ris.persistence.beans.entities.domain.SectionD;
import it.nexera.ris.persistence.beans.entities.domain.Subject;
import it.nexera.ris.persistence.view.FormalityView;

public class FormalityHelper {

	public static transient final Log log = LogFactory.getLog(FormalityHelper.class);

	public static void downloadFormalityPdf(Long entityEditId)
			throws PersistenceBeanException, IllegalAccessException, InstantiationException {
		Document document = DaoManager.get(Document.class, new CriteriaAlias[]{
				new CriteriaAlias("formality", "f", JoinType.INNER_JOIN)
		}, new Criterion[]{
				Restrictions.eq("f.id", entityEditId)
		});
		if (!ValidationHelper.isNullOrEmpty(document)) {
			File file = new File(document.getPath());
			try (FileInputStream fis = new FileInputStream(file)) {
				FileHelper.sendFile(file.getName(), fis, (int) file.length());
			} catch (IOException e) {
				LogHelper.log(log, e);
			}
		}else {
			Formality formality = DaoManager.get(Formality.class
					, new Criterion[]{Restrictions.eq("id", entityEditId)});

			String body = getPdfBody(formality);
			
			try {
				FileHelper.sendFile("richiesta-" + formality.getStrId() + ".pdf",
						PrintPDFHelper.convertToPDF(getHeader(formality, formality.getSectionA()), body, null,
								DocumentType.FORMALITY));
			} catch (InvalidParameterException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	public static String getPdfBody(Formality formality) {
		return getSectionA(formality, formality.getSectionA())
				+ getSectionB(formality, formality.getSectionB())
				+ getSectionC(formality, formality.getSectionC())
				+ getSectionD(formality, formality.getSectionD());
	}

    public static boolean isFormalityHasForcedRequestOrEstateSituation(Long formalityId)
            throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(formalityId)) {
            Formality formality = DaoManager.get(Formality.class, formalityId);
            return !ValidationHelper.isNullOrEmpty(formality)
                    && (!ValidationHelper.isNullOrEmpty(formality.getRequestForcedList())
                    || !ValidationHelper.isNullOrEmpty(formality.getEstateSituationList()));
        }
        return false;
    }

	private static String getHeader(Formality formality, SectionA secA) {
		StringBuilder result = new StringBuilder();
		
		//if (!ValidationHelper.isNullOrEmpty(formality)) {
			
			result.append("<p style=\"margin-bottom: 0cm;margin-right:50px;margin-top:50px\" align=\"right\">");
			result.append("<span style=\"font-size:40px;\">");
			result.append(ResourcesHelper.getString("formalityNote"));
			result.append(" ");
			result.append(!ValidationHelper.isNullOrEmpty(formality.getType()) ? formality.getType() : "");
			result.append("</span>");
			result.append("</p>");
			result.append("<p style=\"margin-bottom: 0cm;margin-right:50px;font-size:25px;\" align=\"right\">");
			result.append("<i>");
			result.append(ResourcesHelper.getString("formalityPublicOfficial"));
			result.append("</i>");
			result.append("  ");
			result.append(!ValidationHelper.isNullOrEmpty(secA) ? secA.getFiscalCode() : "");
			result.append("</p>");
			result.append("<p style=\"margin-bottom: 0cm;margin-right:50px;font-size:25px;\" align=\"right\">");
			result.append("<i>");
			result.append(ResourcesHelper.getString("formalityDirectoryNumber"));
			result.append("</i>");
			result.append("  ");
			result.append(!ValidationHelper.isNullOrEmpty(secA) ? secA.getNumberDirectory() : "");
			result.append("</p>");
			
		//}
		return result.toString();
	}

	private static String getSectionA(Formality formality, SectionA secA) {
		StringBuilder result = new StringBuilder();
		result.append("<style type=\"text/css\">div.indent{margin-left:40px;}</style>");
		if (!ValidationHelper.isNullOrEmpty(formality)) {

			result.append("<hr style=\"border: 1px solid black;background-color: black; height: 1px;\"/>");
			result.append("<h3>");
			result.append(ResourcesHelper.getString("formalitySectionA"));
			result.append("</h3>");
			result.append("<hr style=\"border: 1px solid black;background-color: black; height: 0.2px;\"/>");

			if(!ValidationHelper.isNullOrEmpty(secA)
				/* && !ValidationHelper.isNullOrEmpty(secA.getTitleDescription()) */) {
				result.append("<span style=\"font-size: 20px\">");
				result.append("<i>");
				if(!ValidationHelper.isNullOrEmpty(secA.getTitleDescription())) {
					result.append(ResourcesHelper.getString("formalityDataRelation"));
				}else
					result.append("");

				result.append("</i>");
				result.append("</span>");

				if(!ValidationHelper.isNullOrEmpty(secA.getTitleDescription())) {
					result.append("<br/>");
					result.append("<br/>");
					result.append("<table style=\"width: 100%;\">");
					result.append("<tbody>");
					result.append("<tr>");
					result.append("<td style=\"width:15%\"><i>");
					result.append(ResourcesHelper.getString("formalityDescription"));
					result.append("</td></i>");
					result.append("<td>");
					result.append(secA.getTitleDescription());
					result.append("</td>");
					result.append("</tr>");

					result.append("<tr>");
					result.append("<td style=\"width:15%\"><i>");
					result.append(ResourcesHelper.getString("formalityDate"));
					result.append("</td></i>");
					result.append("<td style=\"width:40%\">");
					result.append(DateTimeHelper.toString(secA.getTitleDate()));
					result.append("</td>");
					result.append("<td style=\"width:20%\"><i>");
					result.append(ResourcesHelper.getString("formalityDirectoryNumber"));
					result.append("</td></i>");
					result.append("<td>");
					result.append(secA.getNumberDirectory());
					result.append("</td>");
					result.append("</tr>");

					result.append("<tr>");
					
					if(!ValidationHelper.isNullOrEmpty(secA.getPublicOfficial())) {
					    result.append("<td style=\"width:15%\"><i>");
					    result.append(ResourcesHelper.getString("formalityPublicOfficial"));
	                    result.append("</td></i>");
	                    result.append("<td style=\"width:40%\">");
	                    result.append(secA.getPublicOfficial());
	                    result.append("</td>");
					}else if(!ValidationHelper.isNullOrEmpty(secA.getPublicOfficialNotary())) {
                        result.append("<td style=\"width:15%\"><i>");
                        result.append(ResourcesHelper.getString("formalityNotary"));
                        result.append("</td></i>");
                        result.append("<td style=\"width:40%\">");
                        result.append(secA.getPublicOfficialNotary());
                        result.append("</td>");
                    }
					
					
					result.append("<td style=\"width:20%\"><i>");
					result.append(ResourcesHelper.getString("formalityFiscalCode"));
					result.append("</td></i>");
					result.append("<td>");
					result.append(secA.getFiscalCode());
					result.append("</td>");
					result.append("</tr>");
					result.append("<tr>");
					result.append("<td style=\"width:15%\"><i>");
					result.append(ResourcesHelper.getString("formalitySeat"));
					result.append("</td></i>");
					result.append("<td style=\"width:40%\">");
					result.append(secA.getSeat());
					result.append("</td>");
					result.append("</tr>");
					result.append("</tbody>");
					result.append("</table>");
				}

				
				if(!ValidationHelper.isNullOrEmpty(secA.getAnnotationType())) {
				    result.append("<br/>");
					result.append("<span style=\"font-size: 20px\">");
					result.append("<i>");
					result.append(ResourcesHelper.getString("formalityAnnotationDate"));
					result.append("</i>");
					result.append("</span>");

					result.append("<table style=\"width: 100%;\">");
					result.append("<tbody>");

					result.append("<tr>");
					result.append("<td style=\"width:25%\"><i>");
					result.append(ResourcesHelper.getString("annotationType"));
					result.append("</td></i>");
					result.append("<td>");
					result.append(secA.getAnnotationType());
					result.append("</td>");
					result.append("</tr>");

					result.append("<tr>");
					result.append("<td style=\"width:25%\"><i>");
					result.append(ResourcesHelper.getString("annotationDescription"));
					result.append("</td></i>");
					result.append("<td>");
					result.append(secA.getAnnotationDescription());
					result.append("</td>");
					result.append("</tr>");

					result.append("<tr>");
					result.append("<td style=\"width:30%\"><i>");
					result.append(ResourcesHelper.getString("annotationProperties"));
					result.append("</td></i>");
					result.append("<td>");
					result.append(secA.getAnnotationProperties());
					result.append("</td>");
					result.append("</tr>");

					result.append("</tbody>");
					result.append("</table>");

				}else
					result.append("");

				
				if(!ValidationHelper.isNullOrEmpty(secA.getMortgageSpecies())
						&& ValidationHelper.isNullOrEmpty(secA.getAnnotationType())) {
				    result.append("<br/>");
					result.append("<span style=\"font-size: 20px\">");
					result.append("<i>");
					result.append(ResourcesHelper.getString("formalityDataPrivilege"));
					result.append("</i>");
					result.append("</span>");
					result.append("<br/>");

					result.append("<table style=\"width: 100%;\">");
					result.append("<tbody>");


					result.append("<tr>");
					result.append("<td style=\"width:10%\"><i>");
					result.append(ResourcesHelper.getString("formalityTypePrivilege"));
					result.append("</td></i>");
					result.append("<td>");
					result.append(secA.getMortgageSpecies());
					result.append("</td>");
					result.append("</tr>");

					result.append("<tr>");
					result.append("<td style=\"width:10%\"><i>");
					result.append(ResourcesHelper.getString("formalityDerivedFrom"));
					result.append("</td></i>");
					result.append("<td>");
					result.append(secA.getDerivedFrom());
					result.append("</td>");
					result.append("</tr>");


					if(!ValidationHelper.isNullOrEmpty(secA.getDeathDate())) {
						result.append("<tr>");
						result.append("<td style=\"width:10%\"><i>");
						result.append(ResourcesHelper.getString("formalityDeathDesc"));
						result.append("</td></i>");
						result.append("<td>");
						result.append(ResourcesHelper.getString("formalityDeathDate"));
						result.append(" ");
						result.append(DateTimeHelper.toString(secA.getDeathDate()));
						result.append("</td>");
						result.append("</tr>");
					}
					
					result.append("<tr>");
					if(!ValidationHelper.isNullOrEmpty(secA.getCapital())) {
                       
                        result.append("<td style=\"width:10%\"><i>");
                        result.append(ResourcesHelper.getString("formalityCapital"));
                        result.append("</i>&nbsp;");
                        result.append("&nbsp;");
                        result.append("&nbsp;");
                        result.append("&nbsp;");
                        result.append("&nbsp;");
                        result.append("&euro;");
                        result.append("&nbsp;");
                        result.append(secA.getCapital());
                        result.append("</td>");
                    }
					
					if(!ValidationHelper.isNullOrEmpty(secA.getAnnualInterestRate())) {
	                       
                        result.append("<td><i>");
                        result.append(ResourcesHelper.getString("formalityAnnualInterestRate"));
                        result.append("</i>&nbsp;");
                        result.append("&nbsp;");
                        result.append("&nbsp;");
                        result.append("&nbsp;");
                        result.append("&nbsp;");
                        result.append("&euro;");
                        result.append("&nbsp;");
                        result.append(secA.getAnnualInterestRate());
                        result.append("</td>");
                    }
					
					if(!ValidationHelper.isNullOrEmpty(secA.getSemiAnnualInterestRate())) {
                        
                        result.append("<td><i>");
                        result.append(ResourcesHelper.getString("formalitySemiAnnualInterestRate"));
                        result.append("</i>&nbsp;");
                        result.append("&nbsp;");
                        result.append("&nbsp;");
                        result.append("&nbsp;");
                        result.append("&nbsp;");
                        result.append("&euro;");
                        result.append("&nbsp;");
                        result.append(secA.getSemiAnnualInterestRate());
                        result.append("</td>");
                    }
					
					result.append("</tr>");

                    result.append("<tr>");
                    if(!ValidationHelper.isNullOrEmpty(secA.getInterests())) {
                        result.append("<td style=\"width:10%\"><i>");
                        result.append(ResourcesHelper.getString("formalityInterest"));
                        result.append("</i>&nbsp;");
                        result.append("&nbsp;");
                        result.append("&nbsp;");
                        result.append("&nbsp;");
                        result.append("&nbsp;");
                        result.append("&euro;");
                        result.append("&nbsp;");
                        result.append(secA.getInterests());
                        result.append("</td>");
                    }
                    
                    if(!ValidationHelper.isNullOrEmpty(secA.getExpense())) {
                           
                        result.append("<td style=\"width:10%\"><i>");
                        result.append(ResourcesHelper.getString("formalityExpenses"));
                        result.append("</i>&nbsp;");
                        result.append("&nbsp;");
                        result.append("&nbsp;");
                        result.append("&nbsp;");
                        result.append("&nbsp;");
                        result.append("&euro;");
                        result.append("&nbsp;");
                        result.append(secA.getExpense());
                        result.append("</td>");
                    }
                    
                    if(!ValidationHelper.isNullOrEmpty(secA.getTotal())) {
                        
                        result.append("<td style=\"width:10%\"><i>");
                        result.append(ResourcesHelper.getString("formalityTotal"));
                        result.append("</i>&nbsp;");
                        result.append("&nbsp;");
                        result.append("&nbsp;");
                        result.append("&nbsp;");
                        result.append("&nbsp;");
                        result.append("&euro;");
                        result.append("&nbsp;");
                        result.append(secA.getTotal());
                        result.append("</td>");
                    }
                    result.append("</tr>");
                    
					if(!ValidationHelper.isNullOrEmpty(secA.getDuration())) {
						result.append("<tr>");
						result.append("<td style=\"width:40%\"><i>");
						result.append(ResourcesHelper.getString("formalityDurata"));
						result.append("</td></i>");
						result.append("<td>");
						result.append(secA.getDuration());
						result.append("</td>");
						result.append("</tr>");
					}
					
					result.append("</tbody>");
					result.append("</table>");
				}

				result.append("<br/>");
				result.append("<span style=\"font-size: 20px\">");
				if(!ValidationHelper.isNullOrEmpty(secA)
						&& (ValidationHelper.isNullOrEmpty(secA.getMortgageSpecies())
						&& ValidationHelper.isNullOrEmpty(secA.getAnnotationType()))) {
					result.append("<i>");
					result.append(ResourcesHelper.getString("formalityDataConvention"));
					result.append("</i>");
					result.append("</span>");
					result.append("<table style=\"width: 100%;\">");
					result.append("<tbody>");
					result.append("<tr>");
					result.append("<td style=\"width:15%\"><i>");
					result.append(ResourcesHelper.getString("formalityTypeConvention"));
					result.append("</td></i>");
					result.append("<td>");
					result.append(secA.getSpecies());
					result.append("</td>");
					result.append("</tr>");
					result.append("<tr>");
					result.append("<td style=\"width:15%\"><i>");
					result.append(ResourcesHelper.getString("formalityTypeConventionDesc"));
					result.append("</td></i>");
					result.append("<td>");
					result.append(secA.getConventionDescription() != null ? secA.getConventionDescription() : "" );
					result.append("</td>");
					result.append("</tr>");

					if(!ValidationHelper.isNullOrEmpty(secA.getDeathDate())) {
						result.append("<tr>");
						result.append("<td style=\"width:30%\"><i>");
						result.append(ResourcesHelper.getString("formalityDeathDesc"));
						result.append("</td></i>");
						result.append("<td>");
						result.append(ResourcesHelper.getString("formalityDeathDate"));
						result.append(" ");
						result.append(DateTimeHelper.toString(secA.getDeathDate()));
						result.append("</td>");
						result.append("</tr>");
					}

					result.append("</tbody>");
					result.append("</table>");

					if(!ValidationHelper.isNullOrEmpty(secA.getOtherData())
							|| !ValidationHelper.isNullOrEmpty(
							secA.getOtherParticularRegister())) {
						result.append("<br/>");
						result.append("<span style=\"font-size: 20px\">");
						result.append("<i>");
						result.append(ResourcesHelper.getString("formalityOtherData"));
						result.append("</i>");
						result.append("</span>");

						result.append("<table style=\"width: 100%;\">");
						result.append("<tbody>");

						result.append("<tr>");
                        result.append("<td style=\"width:25%\"><i>");
						result.append(ResourcesHelper.getString("formalityReferenceFormalities"));
						result.append("</td>");
						result.append("<td>");
						result.append("Servizio di PI di");
						result.append("<i>");
						if(!ValidationHelper.isNullOrEmpty(secA.getLandChargesRegistry()) && 
						        !ValidationHelper.isNullOrEmpty(secA.getLandChargesRegistry().getName())) {
						    result.append(" " + secA.getLandChargesRegistry().getName());
						}
						result.append("</td>");
						result.append("</tr>");
						result.append("<tr>");
						result.append("<td style=\"width:25%\"><i>");
						result.append(secA.getOtherTypeFormality() != null ? secA.getOtherTypeFormality() : "");
						result.append("</td>");
						result.append("<td><i>");
						result.append(ResourcesHelper.getString("formalityReferenceFormalitiesNumber"));
						result.append("</i>&nbsp;&nbsp;");
						result.append(secA.getOtherParticularRegister() != null ? secA.getOtherParticularRegister() : "");
						result.append("&nbsp;&nbsp;&nbsp;&nbsp;");
						result.append(ResourcesHelper.getString("formalityReferenceFormalitiesOfThe"));
						result.append("&nbsp;&nbsp;");
						result.append(DateTimeHelper.toString(secA.getOtherData()));
						result.append("</td>");
						result.append("</tr>");
						result.append("</tbody>");
	                    result.append("</table>");
	                    
	                    result.append("<table style=\"width: 100%;\">");
                        result.append("<tbody>");
						result.append("<tr>");
						result.append("<td style=\"width:25%\"><i>");
                        result.append(ResourcesHelper.getString("formalityApplicant"));
                        result.append("</i></td>");
                        result.append("<td style=\"width:15%\">");
                        result.append("</td>");
                        result.append("<td style=\"width:55%\">");
                        result.append(secA.getApplicant() != null ? secA.getApplicant() : "");
                        result.append("</td>");
                        result.append("<td style=\"width:15%\">");
                        result.append("</td>");
                        result.append("</tr>");
                        
                        result.append("<tr>");
                        result.append("<td>");
                        result.append("</td>");
                        result.append("<td style=\"width:15%\"><i>");
                        result.append(ResourcesHelper.getString("formalityFiscalCode"));
                        result.append("</i></td>");
                        result.append("<td style=\"width:35%;text-align: right;\">");
                        result.append(secA.getFiscalCodeAppliant() != null ? secA.getFiscalCodeAppliant() : "");
                        result.append("</td>");
                        result.append("<td style=\"width:20%\">");
                        result.append("</td>");
                        result.append("</tr>");
                        
                        result.append("<tr>");
                        result.append("<td>");
                        result.append("</td>");
                        result.append("<td style=\"width:15%\"><i>");
                        result.append(ResourcesHelper.getString("formalityAddress"));
                        result.append("</i></td>");
                        result.append("<td style=\"width:35%;text-align: right;\">");
                        result.append(secA.getAddressAppliant() != null ? secA.getAddressAppliant() : "");
                        result.append("</td>");
                        result.append("<td style=\"width:20%\">");
                        result.append("</td>");
                        result.append("</tr>");
						result.append("</tbody>");
						result.append("</table>");


					}
				}
				
				result.append("<br/>");
				String negotiatingUnits = "";
				String favourCount = "";
				String controCount = "";
				
                if(!ValidationHelper.isNullOrEmpty(formality.getSectionB())){
                    Map<String, Long> units = formality.getSectionB()
                            .stream()
                            .filter(b -> !ValidationHelper.isNullOrEmpty(b.getBargainingUnit()))
                            .collect(Collectors.groupingBy(SectionB::getBargainingUnit, 
                                 LinkedHashMap::new,
                                 Collectors.counting()));
                    negotiatingUnits = String.valueOf(units.size());
                }
                
                if(!ValidationHelper.isNullOrEmpty(formality.getSectionC())){
                    List<SectionC> subjectSectionCEntries = formality.getSectionC().stream()
                            .filter(sc -> "A favore".equalsIgnoreCase(sc.getSectionCType()))
                            .collect(Collectors.toList());
                    if(!ValidationHelper.isNullOrEmpty(subjectSectionCEntries))
                        favourCount = String.valueOf(subjectSectionCEntries.size());

                    subjectSectionCEntries = formality.getSectionC().stream()
                            .filter(sc -> "Contro".equalsIgnoreCase(sc.getSectionCType()))
                            .collect(Collectors.toList());
                    if(!ValidationHelper.isNullOrEmpty(subjectSectionCEntries))
                        controCount = String.valueOf(subjectSectionCEntries.size());

                }
                
                if(!ValidationHelper.isNullOrEmpty(negotiatingUnits) || 
                        !ValidationHelper.isNullOrEmpty(favourCount) || 
                        !ValidationHelper.isNullOrEmpty(controCount)) {
                    result.append("<span style=\"font-size: 20px\">");
                    result.append("<i>");
                    if(!ValidationHelper.isNullOrEmpty(secA.getTitleDescription())) {
                        result.append(ResourcesHelper.getString("formalitySummaryData"));
                    }else
                        result.append("");

                    result.append("</i>");
                    result.append("</span>");
                    result.append("<br/>");
                    result.append("<br/>");
                    result.append("<table style=\"width: 100%;\">");
                    result.append("<tbody>");
                    result.append("<tr>");
                }
                
                if(!ValidationHelper.isNullOrEmpty(negotiatingUnits)) {
                    result.append("<td style=\"width:15%\"><i>");
                    result.append(ResourcesHelper.getString("formalityNegotiatingunits"));
                    result.append("</td></i>");
                    result.append("<td>");
                    result.append(negotiatingUnits);
                    result.append("</td>");
                  
                }
                
                if(!ValidationHelper.isNullOrEmpty(favourCount)) {
                    result.append("<td style=\"width:15%\"><i>");
                    result.append(ResourcesHelper.getString("formalitySubjectsInFavour"));
                    result.append("</td></i>");
                    result.append("<td>");
                    result.append(favourCount);
                    result.append("</td>");
                  
                }
                
                if(!ValidationHelper.isNullOrEmpty(controCount)) {
                    result.append("<td style=\"width:15%\"><i>");
                    result.append(ResourcesHelper.getString("formalitySubjectsInContro"));
                    result.append("</td></i>");
                    result.append("<td>");
                    result.append(controCount);
                    result.append("</td>");
                  
                }
                if(!ValidationHelper.isNullOrEmpty(negotiatingUnits) || 
                        !ValidationHelper.isNullOrEmpty(favourCount) || 
                        !ValidationHelper.isNullOrEmpty(controCount)) {
                    result.append("</tr>");   
                    result.append("</tbody>");
                    result.append("</table>");
                }
			}
		}
		return result.toString();
	}

	private static String getSectionB(Formality formality, List<SectionB> sectionBs) {
		StringBuilder result = new StringBuilder();
		if (!ValidationHelper.isNullOrEmpty(formality) &&
				!ValidationHelper.isNullOrEmpty(sectionBs)) {
			result.append("<br/>");
			result.append("<hr style=\"border: 1px solid black;background-color: black; height: 1px;\"/>");
			result.append("<h3>");
			result.append(ResourcesHelper.getString("formalitySectionB"));
			result.append("</h3>");
			result.append("<hr style=\"border: 1px solid black;background-color: black; height: 0.2px;\"/>");
			try {
				for(Map.Entry<Integer, List<Property>> entry: formality.getSectionBMap()) {
					result.append("<span style=\"font-size: 20px\">");
					result.append("<i>");
					result.append(ResourcesHelper.getString("formalitySectionBMapKey"));
					result.append("</i>");
					result.append(entry.getKey());
					result.append("</span>");
					result.append("<br/>");
					result.append("<br/>");
					for(Property property: entry.getValue()) {
						result.append("<span style=\"font-size: 20px\">");
						result.append("<i>");
						result.append(ResourcesHelper.getString("estateLocation"));
						result.append(" n.");
						result.append("</i>");
						result.append(property.getNumberInFormalityGroup());
						result.append("</span>");
						result.append("<table style=\"width: 100%;\">");
						result.append("<tbody>");
						result.append("<tr>");
						result.append("<td style=\"width:15%\"><i>");
						result.append(ResourcesHelper.getString("formalityCity"));
						result.append("</td></i>");
						result.append("<td>");
						result.append(property.getCity());
						result.append("</td>");
						result.append("</tr>");
						result.append("<tr>");
						result.append("<td style=\"width:15%\"><i>");
						result.append(ResourcesHelper.getString("formalityLandRegistry"));
						result.append("</td></i>");
						result.append("<td>");
						result.append(property.getLandRegistry());
						result.append("</td>");
						result.append("</tr>");
						for(CadastralData cadastralData : property.getCadastralData()) {
							result.append("<tr></tr>");
							result.append("<tr>");
							result.append("<td style=\"width:15%\"><i>");
							result.append(ResourcesHelper.getString("formalitySection"));
							result.append("</td></i>");
							result.append("<td>");
							result.append(cadastralData.getSection());
							result.append("</td>");
							result.append("<td style=\"width:15%\"><i>");
							result.append(ResourcesHelper.getString("formalitySheet"));
							result.append("</td></i>");
							result.append("<td>");
							result.append(cadastralData.getSheet());
							result.append("</td>");
							result.append("<td style=\"width:15%\"><i>");
							result.append(ResourcesHelper.getString("formalityParticle"));
							result.append("</td></i>");
							result.append("<td>");
							result.append(cadastralData.getParticle());
							result.append("</td>");
							result.append("<td style=\"width:15%\"><i>");
							result.append(ResourcesHelper.getString("formalitySub"));
							result.append("</td></i>");
							result.append("<td>");
							result.append(cadastralData.getSub());
							result.append("</td>");
							result.append("</tr>");
						}
						result.append("<tr>");
						result.append("<td style=\"width:15%\"><i>");
						result.append(ResourcesHelper.getString("formalityPropertyCategory"));
						result.append("</td></i>");
						result.append("<td>");
						result.append(property.getCategoryStr());
						result.append("</td>");
						result.append("<td style=\"width:15%\"><i>");
						result.append(ResourcesHelper.getString("formalityConsistency"));
						result.append("</td></i>");
						result.append("<td>");
						result.append(!ValidationHelper.isNullOrEmpty(property.getRightConsistency()) ? property.getRightConsistency() : "");
						result.append("</td>");
						result.append("</tr>");
						result.append("<tr>");
						result.append("<td style=\"width:15%\"><i>");
						result.append(ResourcesHelper.getString("formalityAddress"));
						result.append("</td></i>");
						result.append("<td>");
						result.append(property.getAddress());
						result.append("</td>");
						result.append("<td></td>Rreques");
						result.append("<td style=\"width:15%\"><i>");
						result.append(ResourcesHelper.getString("clientAddressHouseNumber"));
						result.append("</td></i>");
						if(!ValidationHelper.isNullOrEmpty(property.getAddress())) {
							result.append("<td>");
							List<String> tokens = Arrays.asList(StringUtils.splitPreserveAllTokens(property.getAddress(), ","));
							if(tokens.size() > 1) {
								result.append(tokens.get(1));	
							}
							result.append("</td>");
							result.append("</tr>");
							result.append("<tr>");
							result.append("<td style=\"width:15%\"><i>");
							result.append(ResourcesHelper.getString("databaseListRealEstateFloor"));
							result.append("</td></i>");
							result.append("<td>");
							if(!ValidationHelper.isNullOrEmpty(property.getFloor())) {
							    result.append(property.getFloor());    
							}else
							    result.append("");
							result.append("</td>");
							result.append("</tr>");
						}
						
						result.append("</tbody>");
						result.append("</table>");	
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		return result.toString();
	}
	
	private static String getSectionC(Formality formality, Set<SectionC> sectionCs) {
		StringBuilder result = new StringBuilder();
		if (!ValidationHelper.isNullOrEmpty(formality) && 
				!ValidationHelper.isNullOrEmpty(sectionCs)) {
			result.append("<br/>");
			result.append("<hr style=\"border: 1px solid black;background-color: black; height: 1px;\"/>");
			result.append("<h3>");
			result.append(ResourcesHelper.getString("formalitySectionC"));
			result.append("</h3>");
			result.append("<hr style=\"border: 1px solid black;background-color: black; height: 0.2px;\"/>");
			try {
				Map<String, List<Subject>> sectionC = new HashMap<>();
				List<Subject> aFavoreSubjects = sectionCs.stream()
						.filter(c -> "A favore".equals(c.getSectionCType())).map(SectionC::getSubject)
						.flatMap(List::stream).peek(s -> s.setTempFormality(formality)).distinct().collect(Collectors.toList());
				IntStream.range(0, aFavoreSubjects.size()).forEach(i -> aFavoreSubjects.get(i).setNumberInFormalityGroup(i + 1));
				List<Subject> controSubjects = sectionCs.stream()
						.filter(c -> "Contro".equals(c.getSectionCType())).map(SectionC::getSubject)
						.flatMap(List::stream).peek(s -> s.setTempFormality(formality)).distinct().collect(Collectors.toList());
				IntStream.range(0, controSubjects.size()).forEach(i -> controSubjects.get(i).setNumberInFormalityGroup(i + 1));
				List<Subject> debitoriSubjects = sectionCs.stream()
						.filter(c -> "Debitori non datori di ipoteca".equals(c.getSectionCType())).map(SectionC::getSubject)
						.flatMap(List::stream).peek(s -> s.setTempFormality(formality)).distinct().collect(Collectors.toList());
				IntStream.range(0, debitoriSubjects.size()).forEach(i -> debitoriSubjects.get(i).setNumberInFormalityGroup(i + 1));
				sectionC.put(ResourcesHelper.getString("formalityInFavor"), aFavoreSubjects);
				sectionC.put(ResourcesHelper.getString("formalityVersus"), controSubjects);
				sectionC.put(ResourcesHelper.getString("formalityDebitori"), debitoriSubjects);


				for(Map.Entry<String, List<Subject>> entry: sectionC.entrySet()) {
					if(!ValidationHelper.isNullOrEmpty(entry.getValue())) {
						result.append("<span style=\"font-size: 30px\">");
						result.append(entry.getKey());
						result.append("</span>");
						result.append("<br/>");
						for(Subject subject : entry.getValue()) {
							if(!subject.getTypeIsPhysicalPerson()) {
								result.append("<span style=\"font-size: 20px\">");
								result.append("<i>");
								result.append(ResourcesHelper.getString("databaseListRealEstateSubject"));
								result.append(" n.");
								result.append("</i>");
								result.append(subject.getNumberInFormalityGroup());
								result.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ");
								result.append("<i>");
								result.append("In qualità di");
								result.append("</i>");
								result.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ");
								result.append(entry.getKey());
								result.append("</span>");
								result.append("<br/>");
								result.append("<br/>");	
								result.append("<div class=\"indent\">");
								result.append("<table style=\"width: 100%;\">");
								result.append("<tbody>");
								result.append("<tr>");
								result.append("<td style=\"width:30%\"><i>");
								result.append(ResourcesHelper.getString("formalityCompanyName"));
								result.append("</td></i>");
								result.append("<td>");
								result.append(subject.getBusinessName());
								result.append("</td>");
								result.append("</tr>");
//								if(subject.getNameBirthCity() != null 
//										&& !subject.getNameBirthCity().isEmpty()) {
//									result.append("<tr>");
//									result.append("<td style=\"width:30%\"><i>");
//									result.append(ResourcesHelper.getString("formalitySeat"));
//									result.append("</td></i>");
//									result.append("<td>");
//									result.append(subject.getNameBirthCity());
//									result.append("</td>");
//									result.append("</tr>");
//								}
								if(subject.getTypeId() == 2 && 
										!ValidationHelper.isNullOrEmpty(subject.getBirthCity()) &&
										!ValidationHelper.isNullOrEmpty(subject.getBirthCity().getDescription())) {

									result.append("<tr>");
									result.append("<td style=\"width:30%\"><i>");
									result.append(ResourcesHelper.getString("formalitySedeCode"));
									result.append("</td></i>");
									result.append("<td>");
									result.append(subject.getBirthCityDescription());
									result.append(" (");
									result.append(subject.getBirthProvince().getCode());
									result.append(")" );
									result.append("</td>");
									result.append("</tr>");
								
								}
								result.append("<tr>");
								result.append("<td style=\"width:30%\"><i>");
								result.append(ResourcesHelper.getString("formalityFiscalCode"));
								result.append("</td></i>");
								result.append("<td>");
								result.append(subject.getNumberVAT());
								result.append("</td>");
								result.append("</tr>");
								result.append("<tr>");
								result.append("<td colspan=\"2\">");
								result.append(subject.getBargainingUnitStr(entry.getKey()));
								result.append("</td>");
								result.append("</tr>");
								result.append("</tbody>");
								result.append("</table>");
								result.append("</div>");
							}
							
							if(subject.getTypeIsPhysicalPerson()) {
								result.append("<span style=\"font-size: 20px\">");
								result.append("<i>");
								result.append(ResourcesHelper.getString("databaseListRealEstateSubject"));
								result.append(" n.");
								result.append("</i>");
								result.append(subject.getNumberInFormalityGroup());
								result.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ");
								result.append("<i>");
								result.append("In qualità di");
								result.append("</i>");
								result.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ");
								result.append(entry.getKey());
								result.append("</span>");
								result.append("<br/>");
								result.append("<br/>");	
								result.append("<div class=\"indent\">");
								result.append("<table style=\"width: 100%;\">");
								result.append("<tbody>");
								result.append("<tr>");
								result.append("<td style=\"width:30%\"><i>");
								result.append(ResourcesHelper.getString("formalitySurname"));
								result.append("</i>&nbsp;&nbsp;&nbsp;&nbsp;");
								result.append(subject.getSurname());
								result.append("</td>");
								result.append("<td>");
								result.append("</td>");
								result.append("<td><i>");
								result.append(ResourcesHelper.getString("formalityName"));
								result.append("</i>&nbsp;&nbsp;&nbsp;&nbsp;");
								result.append(subject.getName());
								result.append("</td>");
								result.append("</tr>");
								
								result.append("<tr>");
								result.append("<td colspan=\"2\"><i>");
								result.append(ResourcesHelper.getString("formalityBorn"));
								result.append("</i>&nbsp;&nbsp;&nbsp;&nbsp;");
								result.append(DateTimeHelper.toString(subject.getBirthDate()));
								result.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<i>");
                                result.append(ResourcesHelper.getString("formalityBornIn"));
                                result.append("</i>&nbsp;&nbsp;&nbsp;&nbsp;");
                                result.append(subject.getNameBirthCity());
								result.append("</td>");
								result.append("</tr>");
								
								result.append("<tr>");
                                result.append("<td colspan=\"2\"><i>");
                                result.append(ResourcesHelper.getString("formalitySex"));
                                result.append("</i>&nbsp;&nbsp;&nbsp;&nbsp;");
                                result.append(subject.getSexType().getShortValue());
                                result.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<i>");
                                result.append(ResourcesHelper.getString("formalityFiscalCode"));
                                result.append("</i>&nbsp;&nbsp;&nbsp;&nbsp;");
                                result.append(subject.getFiscalCode());
                                result.append("</td>");
                                result.append("</tr>");
								
								result.append("<tr>");
								result.append("<td colspan=\"4\">");
								result.append(subject.getBargainingUnitStr(entry.getKey()));
								result.append("</td>");
								result.append("</tr>");
								result.append("</tbody>");
								result.append("</table>");
								result.append("</div>");
							}
							
						}
					}
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		return result.toString();
	}
	
	private static String getSectionD(Formality formality, List<SectionD> sectionDs) {
		StringBuilder result = new StringBuilder();
		if (!ValidationHelper.isNullOrEmpty(formality) && 
				!ValidationHelper.isNullOrEmpty(sectionDs)) {
			
			result.append("<br/>");
			result.append("<hr style=\"border: 1px solid black;background-color: black; height: 1px;\"/>");
			result.append("<h3>");
			result.append(ResourcesHelper.getString("formalitySectionD"));
			result.append("</h3>");
			result.append("<hr style=\"border: 1px solid black;background-color: black; height: 0.2px;\"/>");
			try {
				for(SectionD sectionD : formality.getSectionD()) {
					result.append("<span style=\"font-size: 20px\">");
					result.append("<i>");
					result.append(sectionD.getAdditionalInformation());
					result.append("</i>");
					result.append("</span>");
				}
			}catch (Exception e) {
				e.printStackTrace();
			} 
		}
		return result.toString();
	}
	public static void deleteFormality(Long editId) throws Exception {
		TransactionExecuter.execute(new Action() {
			@Override
			public void execute() throws Exception {

				List<SectionA> sectionAList = DaoManager.load(SectionA.class, new Criterion[]{Restrictions.eq("formality.id", editId)});

				if (!ValidationHelper.isNullOrEmpty(sectionAList)) {
					for (SectionA a : sectionAList) {
						DaoManager.remove(a);
					}
				}

				List<SectionB> sectionBList = DaoManager.load(SectionB.class
						, new Criterion[]{Restrictions.eq("formality.id", editId)});

				if (!ValidationHelper.isNullOrEmpty(sectionBList)) {
					for (SectionB b : sectionBList) {
						DaoManager.remove(b);
					}
				}

				List<SectionC> sectionCList = DaoManager.load(SectionC.class
						, new Criterion[]{Restrictions.eq("formality.id", editId)});

				if (!ValidationHelper.isNullOrEmpty(sectionCList)) {
					for (SectionC c : sectionCList) {
						for (Subject subject : c.getSubject()) {
							subject.getSectionC().remove(c);
							DaoManager.save(subject);
						}
						DaoManager.remove(c);
					}
				}

				List<SectionD> sectionDList = DaoManager.load(SectionD.class
						, new Criterion[]{Restrictions.eq("formality.id", editId)});

				if (!ValidationHelper.isNullOrEmpty(sectionDList)) {
					for (SectionD d : sectionDList) {
						DaoManager.remove(d);
					}
				}


				List<Relationship> relationships = DaoManager.load(Relationship.class
						, new Criterion[]{Restrictions.eq("formality.id", editId)});

				if (!ValidationHelper.isNullOrEmpty(relationships)) {
					for (Relationship relationship : relationships) {
						DaoManager.remove(relationship);
					}
				}

				List<EstateSituation> estateSituations = DaoManager.load(EstateSituation.class,
						new CriteriaAlias[]{new CriteriaAlias("formalityList", "f", JoinType.INNER_JOIN)},
						new Criterion[]{Restrictions.eq("f.id", editId)});

				for (EstateSituation situation : estateSituations) {
					situation.getFormalityList().removeIf(x -> x.getId().equals(editId));
					DaoManager.save(situation);
				}

				List<Request> requestList = DaoManager.load(Request.class,
						new CriteriaAlias[]{new CriteriaAlias("formalityPdfList", "f", JoinType.INNER_JOIN)},
						new Criterion[]{Restrictions.eq("f.id", editId)});

				for (Request request : requestList) {
					request.getFormalityPdfList().removeIf(x -> x.getId().equals(editId));
					DaoManager.save(request);
				}

				List<OldProperty> oldProperties = DaoManager.load(OldProperty.class
						, new Criterion[]{Restrictions.eq("formality.id", editId)});

				if (!ValidationHelper.isNullOrEmpty(oldProperties)) {
					for (OldProperty property : oldProperties) {
						DaoManager.remove(property);
					}
				}

				DaoManager.remove(Formality.class, editId);
			}
		});
	}

	public static boolean deleteFormalityFromRequest(Long formalityId, Long requestId) throws PersistenceBeanException, IllegalAccessException {
		List<Formality> formalityForcedList = DaoManager.load(Formality.class, new CriteriaAlias[]{
				new CriteriaAlias("requestForcedList", "rFL", JoinType.INNER_JOIN)}, new Criterion[]{
				Restrictions.eq("rFL.id", requestId), Restrictions.eq("id", formalityId)});

		if (!ValidationHelper.isNullOrEmpty(formalityForcedList)) {
			for (Formality formality : formalityForcedList) {
				formality.getRequestForcedList().removeIf(x -> x.getId().equals(requestId));
				DaoManager.save(formality, true);
			}
			return true;
		}
		return false;
	}

	public static void addRequestFormalityForcedRecordIfFormalitiesAreNotLinkedWithRequest(Request request,
																						   List<Formality> formalities,
																						   boolean beginTransaction)
			throws PersistenceBeanException, IllegalAccessException {
		List<FormalityView> formalitiesAssociatedWithRequest =
				EstateSituationHelper.loadFormalityViewByDistraint(request);
		if (!ValidationHelper.isNullOrEmpty(formalitiesAssociatedWithRequest)) {
			formalities = formalities.stream().filter(f -> formalitiesAssociatedWithRequest.stream()
					.noneMatch(x -> x.getId().equals(f.getId()))).collect(Collectors.toList());
		}
		for (Formality formality : formalities) {
			if (formality.getRequestForcedList() == null) {
				formality.setRequestForcedList(new LinkedList<>());
			}
			formality.getRequestForcedList().add(request);
			DaoManager.save(formality, beginTransaction);
		}
	}
}
