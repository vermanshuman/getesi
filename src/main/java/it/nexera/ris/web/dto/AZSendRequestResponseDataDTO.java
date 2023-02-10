package it.nexera.ris.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties
@ToString(callSuper=true)
public class AZSendRequestResponseDataDTO {

    @SerializedName("decesso")
    private DataDeathDTO death;
    @SerializedName("societa")
    private DataSocietyDTO society;
    @SerializedName("residenza")
    private DataResidenceDTO residence;
    @SerializedName("domicilio")
    private DataDomicileDTO domicile;
    @SerializedName("erede_chiamato")
    private List<DataHeirDTO> heirs;
    @SerializedName("erede_accettante")
    private List<DataHeirDTO> acceptanceHeirs;
    @SerializedName("proprieta")
    private List<DataPropertyDTO> properties;
    @SerializedName("recapito_telefonico")
    private List<DataPhoneDTO> phones;
    @SerializedName("conto_corrente")
    private List<DataBankAccountDTO> bankAccounts;
    @SerializedName("lavoro")
    private List<DataWorkDTO> works;
    @SerializedName("pensione")
    private List<DataPensionDTO> pensions;
    @SerializedName("carica")
    private List<DataChargeDTO> charges;
    @SerializedName("partecipazione")
    private List<DataParticipationDTO> participations;
    @SerializedName("cessione_pensione")
    private List<DataAssignmentPensionDTO> assignmentPensions;
    @SerializedName("cessione_lavoro")
    private List<DataJobTransferDTO> transfers;
    @SerializedName("pignoramento_pensione")
    private List<DataPensionForeclosureDTO> pensionForclosures;
    @SerializedName("pignoramento_lavoro")
    private List<DataWorkForeclosureDTO> workForclosures;
    @SerializedName("custom_code")
    private List<Long> customCode;

    private String ticketid;

/*
    @SerializedName("servizio")
    private String service;
    @SerializedName("codice_fiscale")
    private String fiscalCode;


    @SerializedName("azienda")
    private String agency;
    @SerializedName("ragione_sociale")
    private String businessName;
    @SerializedName("nome")
    private String name;
    @SerializedName("cognome")
    private String surname;
    @SerializedName("data_nascita")
    private String birthDate;
    @SerializedName("cessato")
    private String ceased;*/
}
