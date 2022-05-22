package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;

public enum PageTypes {
    HOME("/Pages/Home.jsf"),
    ERROR("Common/Error.jsf"),

    LOGIN("/Login.jsf"),
    LOGOUT("j_spring_security_logout"),

    // Configuration

    APPLICATION_SETTINGS("/Pages/Configuration/ApplicationSettings.jsf"),
    IMPORT_PROPERTY_SETTINGS("/Pages/Configuration/ImportSettings.jsf?type=property"),
    IMPORT_ESTATE_FORMALITY_SETTINGS("/Pages/Configuration/ImportSettings.jsf?type=estate_formality"),
    IMPORT_FORMALITY_SETTINGS("/Pages/Configuration/ImportSettings.jsf?type=formality"),
    IMPORT_REPORT_FORMALITY_SUBJECT_SETTINGS("/Pages/Configuration/ImportSettings.jsf?type=report_formality_subject"),
    IMPORT_VISURE_RTF_SETTINGS("/Pages/Configuration/ImportSettings.jsf?type=visure_RTF"),
    IMPORT_VISURE_DH_SETTINGS("/Pages/Configuration/ImportSettings.jsf?type=visure_DH"),
    IMPORT_REQUEST_OLD_SETTINGS("/Pages/Configuration/ImportSettings.jsf?type=request_OLD"),
    DOCUMENT_CONVERSION_SETTINGS("/Pages/Configuration/DocumentConversionSettings.jsf"),
    OMI_VALUATION_DOCUMENTS("/Pages/Configuration/OMIValuationDocuments.jsf"),
    DATI_AZIENDALI("/Pages/Configuration/DatiAziendali.jsf"),
    CODICI_CONSERVATORIE("/Pages/Configuration/CodiciConservatorie.jsf"),

    MONITORING_VIEW("/Pages/Configuration/Monitoring.jsf"),

    USER_LIST("/Pages/Configuration/UserList.jsf"),
    USER_EDIT("/Pages/Configuration/UserEdit.jsf"),
    USER_PROFILE_VIEW("/Pages/Configuration/UserProfileView.jsf"),

    ROLE_LIST("/Pages/Configuration/RoleList.jsf"),
    ROLE_EDIT("/Pages/Configuration/RoleEdit.jsf"),

    DOCUMENT_TEMPLATE_LIST("/Pages/Configuration/DocumentTemplateList.jsf"),
    DOCUMENT_TEMPLATE_EDIT("/Pages/Configuration/DocumentTemplateEdit.jsf"),

    INSTANCE_PHASES_EDIT("/Pages/Configuration/InstancePhasesEdit.jsf"),
    INSTANCE_PHASES_LIST("/Pages/Configuration/InstancePhasesList.jsf"),

    EVENT_LIST("/Pages/Configuration/EventList.jsf"),

    INPUT_CARD_EDIT("/Pages/Configuration/InputCardEdit.jsf"),
    INPUT_CARD_LIST("/Pages/Configuration/InputCardList.jsf"),

    // Dictionary

    TEMPLATE_DOCUMENT_MODEL_LIST("/Pages/Dictionaries/TemplateDocumentModelList.jsf", true),
    TEMPLATE_DOCUMENT_MODEL_EDIT("/Pages/Dictionaries/TemplateDocumentModelEdit.jsf", true),

    REQUEST_TYPE_LIST("/Pages/Dictionaries/RequestTypeList.jsf", true),
    REQUEST_TYPE_EDIT("/Pages/Dictionaries/RequestTypeEdit.jsf", true),
    FOREIGN_STATE_LIST("/Pages/Dictionaries/ForeignStateList.jsf", true),
    FOREIGN_STATE_EDIT("/Pages/Dictionaries/ForeignStateEdit.jsf", true),
    STRUCTURE_LIST("/Pages/Dictionaries/StructureList.jsf", true),

    COST_CONFIGURATION_LIST("/Pages/Dictionaries/CostConfigurationList.jsf", true),
    COST_CONFIGURATION_EDIT("/Pages/Dictionaries/CostConfigurationEdit.jsf", true),

    SERVICE_LIST("/Pages/Dictionaries/ServiceList.jsf", true),
    SERVICE_EDIT("/Pages/Dictionaries/ServiceEdit.jsf", true),

    LAND_CHARGES_REGISTRY_LIST("/Pages/Dictionaries/LandChargesRegistryList.jsf", true),
    LAND_CHARGES_REGISTRY_EDIT("/Pages/Dictionaries/LandChargesRegistryEdit.jsf", true),

    AGGREGATION_LAND_CHARGES_REGISTRY_LIST("/Pages/Dictionaries/AggregationLandChargesRegistryList.jsf", true),
    AGGREGATION_LAND_CHARGES_REGISTRY_EDIT("/Pages/Dictionaries/AggregationLandChargesRegistryEdit.jsf", true),

    CADASTRAL_CATEGORY_LIST("/Pages/Dictionaries/CadastralCategoryList.jsf", true),
    CADASTRAL_CATEGORY_EDIT("/Pages/Dictionaries/CadastralCategoryEdit.jsf", true),


    DATA_GROUP_LIST("/Pages/Dictionaries/DataGroupList.jsf"),
    DATA_GROUP_EDIT("/Pages/Dictionaries/DataGroupEdit.jsf"),

    REFERENT_LIST("/Pages/Dictionaries/ReferentList.jsf", true),
    REFERENT_EDIT("/Pages/Dictionaries/ReferentEdit.jsf", true),

    DAY_PHRASE_LIST("/Pages/Dictionaries/DayPhraseList.jsf", true),
    DAY_PHRASE_EDIT("/Pages/Dictionaries/DayPhraseEdit.jsf"),
    TYPE_ACT_LIST("/Pages/Dictionaries/TypeActList.jsf", true),
    TYPE_ACT_EDIT("/Pages/Dictionaries/TypeActEdit.jsf", true),
    TYPE_FORMALITY_LIST("/Pages/Dictionaries/TypeFormalityList.jsf", true),
    TYPE_FORMALITY_EDIT("/Pages/Dictionaries/TypeFormalityEdit.jsf", true),
    CADASTRAL_TOPOLOGY("/Pages/Dictionaries/CadastralTopology.jsf", true),

    // ConfigurationArea

    DICTIONARY_LIST("/Pages/ConfigurationArea/DictionaryList.jsf", true),

    CLIENT_LIST("/Pages/ConfigurationArea/ClientList.jsf"),
    CLIENT_EDIT("/Pages/ConfigurationArea/ClientEdit.jsf"),
    CLIENT_VIEW("/Pages/ConfigurationArea/ClientView.jsf"),
    CLIENT_CREATE("/Pages/ConfigurationArea/ClientCreate.jsf"),

    CATEGORY_PERCENT_VALUE_LIST("/Pages/ConfigurationArea/CategoryPercentValueList.jsf"),
    // ManagementGroup

    MAIL_MANAGER_LIST("/Pages/ManagementGroup/MailManagerList.jsf"),
    MAIL_MANAGER_EDIT("/Pages/ManagementGroup/MailManagerEdit.jsf"),
    MAIL_MANAGER_VIEW("/Pages/ManagementGroup/MailManagerView.jsf"),
    MAIL_MANAGER_FOLDER("/Pages/ManagementGroup/MailManagerFolder.jsf"),

    COMMUNICATION_MESSAGE_LIST("/Pages/ManagementGroup/CommunicationMessageList.jsf"),
    COMMUNICATION_MESSAGE_EDIT("/Pages/ManagementGroup/CommunicationMessageEdit.jsf"),

    DATABASE_LIST("/Pages/ManagementGroup/DatabaseList.jsf"),
    SUBJECT("/Pages/ManagementGroup/Subject.jsf"),
    REAL_ESTATE("/Pages/ManagementGroup/RealEstate.jsf"),
    REAL_ESTATE_VIEW("/Pages/ManagementGroup/RealEstateView.jsf"),
    REAL_ESTATE_EDIT("/Pages/ManagementGroup/RealEstateEdit.jsf"),
    REQUEST_LIST("/Pages/ManagementGroup/RequestList.jsf"),
    BILLING_LIST_OLD("/Pages/ManagementGroup/BillingList_Old.jsf"),
    BILLING_LIST("/Pages/ManagementGroup/BillingList.jsf"),
    REQUEST_EDIT("/Pages/ManagementGroup/RequestEdit.jsf"),
    REQUEST_TEXT_EDIT("/Pages/ManagementGroup/RequestTextEdit.jsf"),
    REQUEST_ESTATE_SITUATION_LIST("/Pages/ManagementGroup/EstateSituationList.jsf"),
    REQUEST_ESTATE_SITUATION_VIEW("/Pages/ManagementGroup/EstateSituationView.jsf"),
    REQUEST_ESTATE_SITUATION_EDIT("/Pages/ManagementGroup/EstateSituationEdit.jsf"),
    REQUEST_ESTATE_FORMALITY("/Pages/ManagementGroup/EstateFormality.jsf"),
    REQUEST_FORMALITY("/Pages/ManagementGroup/Formality.jsf"),
    REQUEST_FORMALITY_EDIT("/Pages/ManagementGroup/FormalityEdit.jsf"),
    REQUEST_FORMALITY_CREATE("/Pages/ManagementGroup/FormalityCreate.jsf"),
    REPORT_LIST("/Pages/ManagementGroup/ReportList.jsf"),
    NOTARIAL_CERTIFICATION_LIST("/Pages/ManagementGroup/NotarialCertificationList.jsf"),

    IBAN_LIST("/Pages/ManagementGroup/IbanList.jsf"),
    IBAN_EDIT("/Pages/ManagementGroup/IbanEdit.jsf"),
    PAYMENT_TYPE_LIST("/Pages/ManagementGroup/PaymentTypeList.jsf"),
    PAYMENT_TYPE_EDIT("/Pages/ManagementGroup/PaymentTypeEdit.jsf"),

    INVOICE_LIST("/Pages/ManagementGroup/InvoiceList.jsf"),
    INVOICE_EDIT("/Pages/ManagementGroup/InvoiceEdit.jsf"),

    CITIES_LIST("/Pages/Dictionaries/CitiesList.jsf"),
    CITIES_EDIT("/Pages/Dictionaries/CitiesEdit.jsf"),

    OMI_KML_LIST("/Pages/Dictionaries/OMIKmlList.jsf"),

    NOTARY_LIST("/Pages/Dictionaries/NotaryList.jsf", true),
    NOTARY_EDIT("/Pages/Dictionaries/NotaryEdit.jsf", true),
    
    COURT_LIST("/Pages/Dictionaries/CourtList.jsf", true),
    COURT_EDIT("/Pages/Dictionaries/CourtEdit.jsf", true),
    
    RELATIONSHIP_TYPES_LIST("/Pages/Dictionaries/RelationshipTypeList.jsf", true),
    RELATIONSHIP_TYPES_EDIT("/Pages/Dictionaries/RelationshipTypeEdit.jsf", true),
    
    EXCEL_DATA("/Pages/ManagementGroup/ExcelData.jsf"),
    
    REGIME_CONIUGI_LIST("/Pages/Dictionaries/RegimeConiugiList.jsf", true),
    SECTION_D_FORMAT_LIST("/Pages/Dictionaries/SectionDFormatList.jsf", true),
    SECTION_D_FORMAT_EDIT("/Pages/Dictionaries/SectionDFormatEdit.jsf", true),
    DUE_REQUESTS_VIEW("/Pages/Dictionaries/DueRequestsView.jsf", true),

    LAND_OMI_LIST("/Pages/ConfigurationArea/LandOmiList.jsf", true),
    LAND_OMI_EDIT("/Pages/ConfigurationArea/LandOmiEdit.jsf", true),

    LAND_CULTURE_LIST("/Pages/ConfigurationArea/LandCultureList.jsf", true),
    LAND_CULTURE_EDIT("/Pages/ConfigurationArea/LandCultureEdit.jsf", true),

    CALENDAR("/Pages/Configuration/Calendar.jsf"),
    FOREIGN_STATES("/Pages/Configuration/ForeignStates.jsf"),

    TAX_RATE_LIST("/Pages/ManagementGroup/TaxRateList.jsf"),
    TAX_RATE_EDIT("/Pages/ManagementGroup/TaxRateEdit.jsf"),

    CONTACT_LIST("/Pages/ManagementGroup/ContactList.jsf"),

    EXCEL_DATA_REQUEST("/Pages/ManagementGroup/ExcelDataRequest.jsf");

    private String page;

    private boolean dictionaryPage;

    private PageTypes(String page) {
        this.page = page;
        dictionaryPage = false;
    }

    private PageTypes(String page, boolean dictionaryPage) {
        this.page = page;
        this.dictionaryPage = dictionaryPage;
    }

    public static String getPageByPath(String path) {
        for (PageTypes type : PageTypes.values()) {
            if (path.contains(type.getPagesContext())) {
                return type.getPagesContext();
            }
        }

        return "";
    }

    public static PageTypes getPageTypeByPath(String path) {
        if (path == null) {
            return null;
        }

        for (PageTypes type : PageTypes.values()) {
            if (path.contains(type.getPagesContext())) {
                return type;
            }
        }

        return null;
    }

    public static PageTypes getPageTypeByCode(String code) {
        if (code == null) {
            return null;
        }

        for (PageTypes type : PageTypes.values()) {
            if (code.equalsIgnoreCase(type.name())) {
                return type;
            }
        }

        return null;
    }

    public String getPagesContext() {
        return this.page;
    }

    public static PageTypes getEditPageByClass(String className) {
        if (className.contains("Short")) {
            className = className.replace("Short", "");
        }

        for (PageTypes type : PageTypes.values()) {
            if (type.getPagesContext().contains("/" + className + "Edit")) {
                return type;
            }else if(className.equalsIgnoreCase("Country")){
                return PageTypes.FOREIGN_STATE_EDIT;
            }else if(className.equalsIgnoreCase("TipologieDiritti")){
                return PageTypes.RELATIONSHIP_TYPES_EDIT;
            }else if(className.equalsIgnoreCase("City")){
                return PageTypes.CITIES_EDIT;
            }
        }

        if (className.endsWith("VIEW")) {
            className = className.substring(0, className.indexOf("VIEW"));

            for (PageTypes type : PageTypes.values()) {
                if (type.getPagesContext().contains("/" + className + "Edit")) {
                    return type;
                }
            }
        }

        return null;
    }

    public static PageTypes getViewPageByClass(String className) {
        if (className.contains("Short")) {
            className = className.replace("Short", "");
        }

        for (PageTypes type : PageTypes.values()) {
            if (type.getPagesContext().contains("/" + className + "View")) {
                return type;
            }
        }

        return null;
    }

    public static PageTypes getListPageByClass(String className) {
        if (className.contains("Short")) {
            className = className.replace("Short", "");
        }

        for (PageTypes type : PageTypes.values()) {
            if (type.getPagesContext().contains("/" + className + "List")) {
                return type;
            }else if(className.equalsIgnoreCase("Country")){
                return PageTypes.FOREIGN_STATE_LIST;
            }else if(className.equalsIgnoreCase("City")){
                return PageTypes.CITIES_LIST;
            }else if(className.equalsIgnoreCase("TipologieDiritti")){
                return PageTypes.RELATIONSHIP_TYPES_LIST;
            }
        }

        return null;
    }

    public String getMenuName() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }

    public String getCode() {
        return this.name();
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public boolean isDictionaryPage() {
        return dictionaryPage;
    }
}
