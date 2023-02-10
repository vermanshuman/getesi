package it.nexera.ris.persistence.beans.entities.domain;

import java.util.Date;
import java.util.StringJoiner;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.annotations.Type;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.LandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.TypeFormality;
import it.nexera.ris.persistence.beans.entities.domain.support.ActType;

@Entity
@Table(name = "section_a")
public class SectionA extends IndexedEntity {

    private static final long serialVersionUID = -1825548892677257100L;

    public transient final Log log = LogFactory.getLog(getClass());

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formality_id")
    private Formality formality;

    @Column(name = "title_description", length = 100)
    private String titleDescription;

    @Column(name = "title_date")
    private Date titleDate;

    @Column(name = "public_official_notary", length = 150)
    private String publicOfficialNotary;

    @Column(name = "public_official")
    private String publicOfficial;

    @Column(name = "number_directory", length = 30)
    private String numberDirectory;

    @Column(name = "fiscal_code", length = 16)
    private String fiscalCode;

    @Column(name = "seat", length = 100)
    private String seat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mortgage_species_or_privilege")
    private ActType mortgageSpeciesOrPrivilege;

    @Column(name = "derived_from", length = 200)
    private String derivedFrom;

    @Column(name = "derived_from_code")
    private String derivedFromCode;

    @Column(name = "capital")
    private String capital;

    @Column(name = "annual_interest_rate")
    private String annualInterestRate;

    @Column(name = "semi_annual_interest_rate")
    private String semiAnnualInterestRate;

    @Column(name = "interests")
    private String interests;

    @Column(name = "expense")
    private String expense;

    @Column(name = "total")
    private String total;

    @Column(name = "varying_amounts")
    private Boolean varyingAmounts;

    @Column(name = "foreign_currency", length = 50)
    private String foreignCurrency;

    @Column(name = "entered_amount_auto_increase")
    private Boolean enteredAmountAutomaticallyIncrease;

    @Column(name = "presence_of_cond_subseq")
    private String presenceOfConditionSubsequent;

    @Column(name = "duration_month")
    private Long durationMonth;

    @Column(name = "duration_year")
    private Long durationYear;

    @Column(name = "duration")
    private String duration;

    @Column(name = "deadline")
    private Date deadline;

    @Column(name = "death_date")
    private Date deathDate;

    @Column(name = "number_negotiating_units")
    private Long numberNegotiatingUnits;

    @Column(name = "number_subjects_in_favor")
    private Long numberSubjectsInFavor;

    @Column(name = "number_persons_or_entities")
    private Long numberPersonsOrEntities;

    @Column(name = "mortgage_species")
    private String mortgageSpecies;

    @Column(name = "convention_species")
    private String conventionSpecies;

    @Column(name = "convention_description")
    private String conventionDescription;

    @Column(name = "other_data")
    private Date otherData;

    @Column(name = "other_particular_register")
    private String otherParticularRegister;

    @Column(name = "other_type_formality")
    private String otherTypeFormality;

    @Column(name = "annotation_type")
    private String annotationType;

    @Column(name = "annotation_description")
    private String annotationDescription;

    @Column(name = "annaotation_properties")
    private String annotationProperties;
    
    @Column(name = "applicant")
    @Type(type="text")
    private String applicant;
    
    @Column(name = "fiscal_code_appliant")
    @Type(type="text")
    private String fiscalCodeAppliant;
    
    @Column(name = "address_appliant")
    @Type(type="text")
    private String addressAppliant;

    @ManyToOne
    @JoinColumn(name = "other_land_registry")
    private LandChargesRegistry landChargesRegistry;

    public String generateDeliveredFromCodeStr() {
        if (!ValidationHelper.isNullOrEmpty(getDerivedFromCode())) {
            Integer code = Integer.parseInt(getDerivedFromCode()) % 1000;
            if (!ValidationHelper.isNullOrEmpty(code)) {
                if (code == 112) {
                    return " vende ";
                }
                if (code >= 133 && code <= 136) {
                    return " dona ";
                }
                if (code >= 107 && code <= 109) {
                    return " cede ";
                }
                if (code == 607) {
                    return " trasferisce  ";
                }
                if (code >= 103 && code <= 105) {
                    return " assegna ";
                }
                if (code >= 113 && code <= 115) {
                    return " conferisce ";
                }
            }
        }
        return "";
    }

    public String getSpecies() {
        return String.format("%s %s",
                getMortgageSpecies() == null ? "" : getMortgageSpecies(),
                getConventionSpecies() == null ? "" : getConventionSpecies());
    }

    public String getFormalityReportStr() {
        try {
            if (!ValidationHelper.isNullOrEmpty(getDerivedFromCode())) {
                return DaoManager.getField(TypeFormality.class, "textInVisura", new Criterion[]{
                        Restrictions.eq("code", "" + Long.parseLong(getDerivedFromCode()))
                }, new CriteriaAlias[]{});
            }
        } catch (PersistenceBeanException | IllegalAccessException e) {
            LogHelper.log(log, e);
        }
        return "";
    }

    public String getCapitalizeSeat() {
        if (!ValidationHelper.isNullOrEmpty(getSeat())) {
            StringJoiner joiner = new StringJoiner(" ");
            String[] splitSeat = getSeat().split(" ");
            for (String s : splitSeat) {
                if (!s.startsWith("(")) {
                    joiner.add(WordUtils.capitalize(s.toLowerCase()));
                } else {
                    joiner.add(s);
                }
            }
            return joiner.toString();
        }
        return "";
    }

    private String correctlyCamelCapitalizeCityOrSeat(String s) {
        if(ValidationHelper.isNullOrEmpty(s))
            return "";
		String[] a = s.split("\\(");
		
		String r = " di ";
		
		if (a.length > 0)
			r += WordUtils.capitalizeFully(a[0].trim());

		if (a.length > 1)
		 	r += " (" + WordUtils.capitalize(a[1]);

		return r;
    }

    public String getCodeWithoutFirstZero() {
        String result = getDerivedFromCode();
        if (!ValidationHelper.isNullOrEmpty(result) && result.startsWith("0")) {
            result = result.substring(1);
        }
        return result;
    }
    
    public String getSeatCamelCase() {
        return correctlyCamelCapitalizeCityOrSeat(getSeat());
    }
    
    public String getPublicOfficialCamelCase() {
    	return correctlyCamelCapitalizeCityOrSeat(getPublicOfficial());
    }

    public Formality getFormality() {
        return formality;
    }

    public void setFormality(Formality formality) {
        this.formality = formality;
    }

    public String getTitleDescription() {
        return titleDescription;
    }

    public void setTitleDescription(String titleDescription) {
        this.titleDescription = titleDescription;
    }

    public Date getTitleDate() {
        return titleDate;
    }

    public void setTitleDate(Date titleDate) {
        this.titleDate = titleDate;
    }

    public String getPublicOfficialNotary() {
        return publicOfficialNotary;
    }

    public void setPublicOfficialNotary(String publicOfficialNotary) {
        this.publicOfficialNotary = publicOfficialNotary;
    }

    public String getNumberDirectory() {
        return numberDirectory;
    }

    public void setNumberDirectory(String numberDirectory) {
        this.numberDirectory = numberDirectory;
    }

    public String getFiscalCode() {
        return fiscalCode;
    }

    public void setFiscalCode(String fiscalCode) {
        this.fiscalCode = fiscalCode;
    }

    public String getSeat() {
        return seat;
    }

    public void setSeat(String seat) {
        this.seat = seat;
    }

    public ActType getMortgageSpeciesOrPrivilege() {
        return mortgageSpeciesOrPrivilege;
    }

    public void setMortgageSpeciesOrPrivilege(
            ActType mortgageSpeciesOrPrivilege) {
        this.mortgageSpeciesOrPrivilege = mortgageSpeciesOrPrivilege;
    }

    public String getDerivedFrom() {
        return derivedFrom;
    }

    public void setDerivedFrom(String derivedFrom) {
        this.derivedFrom = derivedFrom;
    }

    public String getCapital() {
        return capital;
    }

    public void setCapital(String capital) {
        this.capital = capital;
    }

    public String getAnnualInterestRate() {
        return annualInterestRate;
    }

    public void setAnnualInterestRate(String annualInterestRate) {
        this.annualInterestRate = annualInterestRate;
    }

    public String getSemiAnnualInterestRate() {
        return semiAnnualInterestRate;
    }

    public void setSemiAnnualInterestRate(String semiAnnualInterestRate) {
        this.semiAnnualInterestRate = semiAnnualInterestRate;
    }

    public String getInterests() {
        return interests;
    }

    public void setInterests(String interests) {
        this.interests = interests;
    }

    public String getExpense() {
        return expense;
    }

    public void setExpense(String expense) {
        this.expense = expense;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public Boolean getVaryingAmounts() {
        return varyingAmounts;
    }

    public void setVaryingAmounts(Boolean varyingAmounts) {
        this.varyingAmounts = varyingAmounts;
    }

    public String getForeignCurrency() {
        return foreignCurrency;
    }

    public void setForeignCurrency(String foreignCurrency) {
        this.foreignCurrency = foreignCurrency;
    }

    public Boolean getEnteredAmountAutomaticallyIncrease() {
        return enteredAmountAutomaticallyIncrease;
    }

    public void setEnteredAmountAutomaticallyIncrease(
            Boolean enteredAmountAutomaticallyIncrease) {
        this.enteredAmountAutomaticallyIncrease = enteredAmountAutomaticallyIncrease;
    }

    public String getPresenceOfConditionSubsequent() {
        return presenceOfConditionSubsequent;
    }

    public void setPresenceOfConditionSubsequent(
            String presenceOfConditionSubsequent) {
        this.presenceOfConditionSubsequent = presenceOfConditionSubsequent;
    }

    public Long getDurationMonth() {
        return durationMonth;
    }

    public void setDurationMonth(Long durationMonth) {
        this.durationMonth = durationMonth;
    }

    public Long getDurationYear() {
        return durationYear;
    }

    public void setDurationYear(Long durationYear) {
        this.durationYear = durationYear;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public Long getNumberNegotiatingUnits() {
        return numberNegotiatingUnits;
    }

    public void setNumberNegotiatingUnits(Long numberNegotiatingUnits) {
        this.numberNegotiatingUnits = numberNegotiatingUnits;
    }

    public Long getNumberSubjectsInFavor() {
        return numberSubjectsInFavor;
    }

    public void setNumberSubjectsInFavor(Long numberSubjectsInFavor) {
        this.numberSubjectsInFavor = numberSubjectsInFavor;
    }

    public Long getNumberPersonsOrEntities() {
        return numberPersonsOrEntities;
    }

    public void setNumberPersonsOrEntities(Long numberPersonsOrEntities) {
        this.numberPersonsOrEntities = numberPersonsOrEntities;
    }

    public String getMortgageSpecies() {
        return mortgageSpecies;
    }

    public void setMortgageSpecies(String mortgageSpecies) {
        this.mortgageSpecies = mortgageSpecies;
    }

    public String getConventionSpecies() {
        return conventionSpecies;
    }

    public void setConventionSpecies(String conventionSpecies) {
        this.conventionSpecies = conventionSpecies;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getConventionDescription() {
        return conventionDescription;
    }

    public void setConventionDescription(String conventionDescription) {
        this.conventionDescription = conventionDescription;
    }

    public String getPublicOfficial() {
        return publicOfficial;
    }

    public void setPublicOfficial(String publicOfficial) {
        this.publicOfficial = publicOfficial;
    }

    public String getDerivedFromCode() {
        return derivedFromCode;
    }

    public void setDerivedFromCode(String derivedFromCode) {
        this.derivedFromCode = derivedFromCode;
    }

    public Date getOtherData() {
        return otherData;
    }

    public void setOtherData(Date otherData) {
        this.otherData = otherData;
    }

    public String getOtherParticularRegister() {
        return otherParticularRegister;
    }

    public void setOtherParticularRegister(String otherParticularRegister) {
        this.otherParticularRegister = otherParticularRegister;
    }

    public Date getDeathDate() {
        return deathDate;
    }

    public void setDeathDate(Date deathDate) {
        this.deathDate = deathDate;
    }

    public String getAnnotationType() {
        return annotationType;
    }

    public void setAnnotationType(String annotationType) {
        this.annotationType = annotationType;
    }

    public String getAnnotationDescription() {
        return annotationDescription;
    }

    public void setAnnotationDescription(String annotationDescription) {
        this.annotationDescription = annotationDescription;
    }

    public String getAnnotationProperties() {
        return annotationProperties;
    }

    public void setAnnotationProperties(String annotationProperties) {
        this.annotationProperties = annotationProperties;
    }

    public String getOtherTypeFormality() {
        return otherTypeFormality;
    }

    public void setOtherTypeFormality(String otherTypeFormality) {
        this.otherTypeFormality = otherTypeFormality;
    }

    public String getApplicant() {
        return applicant;
    }

    public void setApplicant(String applicant) {
        this.applicant = applicant;
    }

    public String getAddressAppliant() {
        return addressAppliant;
    }

    public void setAddressAppliant(String addressAppliant) {
        this.addressAppliant = addressAppliant;
    }
    
    public String getFiscalCodeAppliant() {
        return fiscalCodeAppliant;
    }

    public void setFiscalCodeAppliant(String fiscalCodeAppliant) {
        this.fiscalCodeAppliant = fiscalCodeAppliant;
    }

    public LandChargesRegistry getLandChargesRegistry() {
        return landChargesRegistry;
    }

    public void setLandChargesRegistry(LandChargesRegistry landChargesRegistry) {
        this.landChargesRegistry = landChargesRegistry;
    }
}
