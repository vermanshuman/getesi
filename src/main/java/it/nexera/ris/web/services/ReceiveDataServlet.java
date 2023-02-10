package it.nexera.ris.web.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.nexera.ris.common.enums.TrackingHeirType;
import it.nexera.ris.common.helpers.APIHelper;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.utils.Constants;
import it.nexera.ris.persistence.PersistenceSession;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.web.dto.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReceiveDataServlet extends HttpServlet {

    private static final long serialVersionUID = -3109342515390688832L;
    protected transient final Log log = LogFactory.getLog(ReceiveDataServlet.class);

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        ResponseDto responseDTO = new ResponseDto();
        PersistenceSession ps = null;
        Gson gson = new GsonBuilder().serializeNulls().create();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Session session = null;
        Transaction tr = null;
        try {
            String token = request.getHeader(Constants.AUTHORIZATION_HEADER_NAME);
            LogHelper.debugInfo(log, "Request received for API (RECIEVEDATA)" + token);
            if (StringUtils.isNotBlank(token) && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            if (StringUtils.isBlank(token) || !token.trim().equals(APIHelper.getToken())) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                responseDTO.setResultCode(Constants.API_FAILURE_CODE);
                responseDTO.setResultDescription(Constants.API_CREDENTIALS_FAILURE);
            } else {
                ps = new PersistenceSession();
                session = ps.getSession();
                tr = session.beginTransaction();
                BufferedReader reader = request.getReader();
                AZReceiveDataResponseDTO inputData = gson.fromJson(reader, AZReceiveDataResponseDTO.class);
                log.info("Request recived for RECIEVEDATA" + inputData);
                if (inputData == null || inputData.getData() == null) {
                    response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
                    responseDTO.setResultCode(Constants.API_FAILURE_MISSING_PARAMETER);
                    responseDTO.setResultDescription(Constants.API_RECIEVE_DATA_MISSING_DATA);
                } else {
                    log.info("Data Received " + inputData);
                    if(inputData.getData() != null && inputData.getData().size() > 0){
                        StringBuilder sb = new StringBuilder();
                        for(AZSendRequestResponseDataDTO data : inputData.getData()){
                            if(data.getCustomCode() != null && !data.getCustomCode().isEmpty()){
                                for(Long requestId : data.getCustomCode()){
                                    log.info("Request " + requestId);
                                    Request requestEntity = ConnectionManager.get(Request.class, requestId, session);
                                    populateTrackingRegistry(data,  requestEntity, sb, session);
                                    populateTrackingHeir(requestEntity, TrackingHeirType.CALLED, data.getHeirs(), sb,
                                            session);
                                    populateTrackingHeir(requestEntity, TrackingHeirType.ACCEPTING,
                                            data.getAcceptanceHeirs(), sb, session);
                                    populateTrackingProperty(requestEntity, data.getProperties(), sb, session);
                                    populateTrackingPhone(requestEntity, data.getPhones(), session);
                                    populateTrackingBankAccount(requestEntity, data.getBankAccounts(), sb, session);
                                    populateWorks(requestEntity, data.getWorks(), sb, session);
                                    populateRetirements(requestEntity, data.getPensions(), sb, session);
                                    populateParticipations(requestEntity, data.getParticipations(), session);
                                    populateAssignmentPension(requestEntity, data.getAssignmentPensions(), session);
                                    populateJobTransfers(requestEntity, data.getTransfers(), session);
                                    populatePensionForclosures(requestEntity, data.getPensionForclosures(), session);
                                    populateWorkForclosures(requestEntity, data.getWorkForclosures(), session);
                                    populateCharges(requestEntity, data.getCharges(), session);
                                }
                            }
                        }
                    }
                    response.setStatus(HttpServletResponse.SC_OK);
                    responseDTO.setResultCode(Constants.API_SUCCESS_CODE);
                    responseDTO.setResultDescription(Constants.API_RECIEVE_DATA_SUCCESS);
                }
            }
        } catch (Exception ex) {
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
            LogHelper.log(log, ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseDTO.setResultCode(Constants.API_FAILURE_CODE);
            responseDTO.setResultDescription(ex.getMessage());
        } finally {
            if (tr != null && tr.isActive() && !tr.wasRolledBack()) {
                tr.commit();
            }
            if (ps != null) {
                ps.closeSession();
            }
        }

        response.getWriter().write(gson.toJson(responseDTO));
    }

    private TrackingRegistry populateTrackingRegistry(AZSendRequestResponseDataDTO data, Request request, StringBuilder sb,
                                                      Session session){
        sb.setLength(0);
        TrackingRegistry trackingRegistry = new TrackingRegistry();
        trackingRegistry.setRequest(request);
        trackingRegistry.setSubject(request.getSubject());
        if(data.getDeath() !=  null){
            trackingRegistry.setDeceasedConfirmed(data.getDeath().getDeathConfirmation());
            if(StringUtils.isNotBlank(data.getDeath().getDeceasedDate()))
                trackingRegistry.setDeceasedDate(
                        DateTimeHelper.fromString(data.getDeath().getDeceasedDate(),
                                DateTimeHelper.getMySQLDatePattern()));

            if(StringUtils.isNotBlank(data.getDeath().getToponymAddress()))
                sb.append(data.getDeath().getToponymAddress());
            if(StringUtils.isNotBlank(data.getDeath().getStreetAddress())){
                sb.append(" ");
                sb.append(data.getDeath().getStreetAddress());
            }
            if(StringUtils.isNotBlank(data.getDeath().getAddressAt())){
                sb.append(" presso ");
                sb.append(data.getDeath().getAddressAt());
            }
            trackingRegistry.setDeceasedLastAddress(sb.toString().trim());
            if(StringUtils.isNotBlank(data.getDeath().getCivicAddress()))
                trackingRegistry.setDeceasedCivicAddress(data.getDeath().getCivicAddress());
            if(StringUtils.isNotBlank(data.getDeath().getCityAdress()))
               trackingRegistry.setDeceasedAddressCity(data.getDeath().getCityAdress());
            if(StringUtils.isNotBlank(data.getDeath().getPostalAddress()))
                trackingRegistry.setDeceasedAddressCap(data.getDeath().getPostalAddress());
            if(StringUtils.isNotBlank(data.getDeath().getAddressProvince()))
                trackingRegistry.setDeceasedAddressProvince(data.getDeath().getAddressProvince());
            if(StringUtils.isNotBlank(data.getDeath().getAddressStatus()))
                trackingRegistry.setDeceasedAddressStatus(data.getDeath().getAddressStatus());
            if(StringUtils.isNotBlank(data.getDeath().getNote()))
                trackingRegistry.setDeceasedAddressNotes(data.getDeath().getNote());
        }
        if(data.getSociety() !=  null){
            sb.setLength(0);
            if(StringUtils.isNotBlank(data.getSociety().getNote()))
                trackingRegistry.setNotes(data.getSociety().getNote());
            if(StringUtils.isNotBlank(data.getSociety().getBusinessName()))
                trackingRegistry.setBusinessName(data.getSociety().getBusinessName());
            if(StringUtils.isNotBlank(data.getSociety().getRevokeDateProceeding()))
                trackingRegistry.setRevokeDateProceeding(
                        DateTimeHelper.fromString(data.getSociety().getRevokeDateProceeding(),
                                DateTimeHelper.getMySQLDatePattern()));
            if(StringUtils.isNotBlank(data.getSociety().getCloseDateProceeding()))
                trackingRegistry.setCloseDateProceeding(
                        DateTimeHelper.fromString(data.getSociety().getCloseDateProceeding(),
                                DateTimeHelper.getMySQLDatePattern()));
            if(StringUtils.isNotBlank(data.getSociety().getInsolvencyProceeding()))
                trackingRegistry.setInsolvencyProceeding(data.getSociety().getInsolvencyProceeding());
            if(data.getSociety().getImportTracking() != null)
                trackingRegistry.setImportTracking(data.getSociety().getImportTracking());
            if(StringUtils.isNotBlank(data.getSociety().getCodeAteco()))
                trackingRegistry.setCodeAteco(data.getSociety().getCodeAteco());
            if(StringUtils.isNotBlank(data.getSociety().getDateStart()))
                trackingRegistry.setDateStart(DateTimeHelper.fromString(data.getSociety().getDateStart(),
                                DateTimeHelper.getMySQLDatePattern()));
            if(StringUtils.isNotBlank(data.getSociety().getDateEnd()))
                trackingRegistry.setDateEnd(DateTimeHelper.fromString(data.getSociety().getDateEnd(),
                                DateTimeHelper.getMySQLDatePattern()));
            if(StringUtils.isNotBlank(data.getSociety().getDateCreation()))
                trackingRegistry.setDateCreation(DateTimeHelper.fromString(data.getSociety().getDateCreation(),
                                DateTimeHelper.getMySQLDatePattern()));
            if(StringUtils.isNotBlank(data.getSociety().getGiuridicNature()))
                trackingRegistry.setGiuridicNature(data.getSociety().getGiuridicNature());
            if(StringUtils.isNotBlank(data.getSociety().getDataStatus()))
                trackingRegistry.setDataStatus(data.getSociety().getDataStatus());
            if(StringUtils.isNotBlank(data.getSociety().getStatus()))
                trackingRegistry.setStatus(data.getSociety().getStatus());
            if(StringUtils.isNotBlank(data.getSociety().getType()))
                trackingRegistry.setType(data.getSociety().getType());
            if(StringUtils.isNotBlank(data.getSociety().getNumberVat()))
                trackingRegistry.setNumberVat(data.getSociety().getNumberVat());
            if(StringUtils.isNotBlank(data.getSociety().getToponymAddress()))
                sb.append(data.getSociety().getToponymAddress());
            if(StringUtils.isNotBlank(data.getSociety().getStreetAddress())){
                sb.append(" ");
                sb.append(data.getSociety().getStreetAddress());
            }
            if(StringUtils.isNotBlank(data.getSociety().getAddressAt())){
                sb.append(" presso ");
                sb.append(data.getSociety().getAddressAt());
            }
            trackingRegistry.setAddress(sb.toString().trim());
            if(StringUtils.isNotBlank(data.getSociety().getCivicAddress()))
                trackingRegistry.setCivicAddress(data.getSociety().getCivicAddress());
            if(data.getSociety().getActive() != null)
                trackingRegistry.setActive(data.getSociety().getActive());
            if(StringUtils.isNotBlank(data.getSociety().getProtest()))
                trackingRegistry.setProtest(data.getSociety().getProtest());
            if(StringUtils.isNotBlank(data.getSociety().getPrejudicial()))
                trackingRegistry.setPrejudicial(data.getSociety().getPrejudicial());
            if(StringUtils.isNotBlank(data.getSociety().getCondominium()))
                trackingRegistry.setCondominium(data.getSociety().getCondominium());
            if(StringUtils.isNotBlank(data.getSociety().getCciaa()))
                trackingRegistry.setCciaa(data.getSociety().getCciaa());
            if(data.getSociety().getSocialAmount() != null)
                trackingRegistry.setSocialAmount(data.getSociety().getSocialAmount());
            if(StringUtils.isNotBlank(data.getSociety().getPec()))
                trackingRegistry.setPec(data.getSociety().getPec());
        }
        if(data.getResidence() !=  null) {
            sb.setLength(0);
            if(StringUtils.isNotBlank(data.getResidence().getNote()))
                trackingRegistry.setNotes(data.getResidence().getNote());
            if(data.getResidence().getUntraceable() != null)
                trackingRegistry.setUntraceable(data.getResidence().getUntraceable());
            if(StringUtils.isNotBlank(data.getResidence().getToponymAddress()))
                sb.append(data.getResidence().getToponymAddress());
            if(StringUtils.isNotBlank(data.getResidence().getStreetAddress())){
                sb.append(" ");
                sb.append(data.getResidence().getStreetAddress());
            }
            if(StringUtils.isNotBlank(data.getResidence().getAddressAt())){
                sb.append(" presso ");
                sb.append(data.getResidence().getAddressAt());
            }
            trackingRegistry.setResidenceAddress(sb.toString().trim());
            if(StringUtils.isNotBlank(data.getResidence().getCivicAddress()))
                trackingRegistry.setResidenceCivicAddress(data.getResidence().getCivicAddress());
            if(StringUtils.isNotBlank(data.getResidence().getCityAddress()))
                trackingRegistry.setResidenceCity(data.getResidence().getCityAddress());
            if(StringUtils.isNotBlank(data.getResidence().getPostalAddress()))
                trackingRegistry.setResidenceCap(data.getResidence().getPostalAddress());
            if(StringUtils.isNotBlank(data.getResidence().getAddressProvince()))
                trackingRegistry.setResidenceProvince(data.getResidence().getAddressProvince());
            if(StringUtils.isNotBlank(data.getResidence().getAddressStatus()))
                trackingRegistry.setResidenceStatus(data.getResidence().getAddressStatus());
        }
        if(data.getDomicile() !=  null) {
            sb.setLength(0);
            if(StringUtils.isNotBlank(data.getDomicile().getNote()))
                trackingRegistry.setNotesDomicile(data.getDomicile().getNote());
            if(StringUtils.isNotBlank(data.getDomicile().getToponymAddress()))
                sb.append(data.getDomicile().getToponymAddress());
            if(StringUtils.isNotBlank(data.getDomicile().getStreetAddress())){
                sb.append(" ");
                sb.append(data.getDomicile().getStreetAddress());
            }
            if(StringUtils.isNotBlank(data.getDomicile().getAddressAt())){
                sb.append(" presso ");
                sb.append(data.getDomicile().getAddressAt());
            }
            trackingRegistry.setDomicileAddress(sb.toString().trim());
            if(StringUtils.isNotBlank(data.getDomicile().getCivicAddress()))
                trackingRegistry.setDomicileCivicAddress(data.getDomicile().getCivicAddress());
            if(StringUtils.isNotBlank(data.getDomicile().getCityAddress()))
                trackingRegistry.setDomicileCity(data.getDomicile().getCityAddress());
            if(StringUtils.isNotBlank(data.getDomicile().getPostalAddress()))
                trackingRegistry.setDomicileCap(data.getDomicile().getPostalAddress());
            if(StringUtils.isNotBlank(data.getDomicile().getAddressProvince()))
                trackingRegistry.setDomicileProvince(data.getDomicile().getAddressProvince());
            if(StringUtils.isNotBlank(data.getDomicile().getAddressStatus()))
                trackingRegistry.setDomicileStatus(data.getDomicile().getAddressStatus());
        }
        ConnectionManager.save(trackingRegistry, session);
        return trackingRegistry;
    }

    private List<TrackingHeir> populateTrackingHeir(Request request, TrackingHeirType type, List<DataHeirDTO> heirs,
                                                    StringBuilder sb, Session session){
        List<TrackingHeir> trackingHeirs = new ArrayList<>();
        CollectionUtils.emptyIfNull(heirs)
                .stream()
                .forEach(heir -> {
                    sb.setLength(0);
                    TrackingHeir trackingHeir = new TrackingHeir();
                    trackingHeir.setType(type);
                    if(StringUtils.isNotBlank(heir.getNote()))
                        trackingHeir.setNotes(heir.getNote());
                    if(StringUtils.isNotBlank(heir.getCodiceFiscale()))
                        trackingHeir.setFiscalCode(heir.getCodiceFiscale());
                    if(StringUtils.isNotBlank(heir.getName()))
                        trackingHeir.setFirstName(heir.getName());
                    if(StringUtils.isNotBlank(heir.getSurname()))
                        trackingHeir.setLastName(heir.getSurname());

                    if(StringUtils.isNotBlank(heir.getToponymAddress()))
                        sb.append(heir.getToponymAddress());
                    if(StringUtils.isNotBlank(heir.getStreetAddress())){
                        sb.append(" ");
                        sb.append(heir.getStreetAddress());
                    }
                    if(StringUtils.isNotBlank(heir.getAddressAt())){
                        sb.append(" presso ");
                        sb.append(heir.getAddressAt());
                    }
                    trackingHeir.setAddress(sb.toString().trim());
                    if(StringUtils.isNotBlank(heir.getCivicAddress()))
                        trackingHeir.setCivicAddress(heir.getCivicAddress());
                    if(StringUtils.isNotBlank(heir.getCityAddress()))
                        trackingHeir.setCity(heir.getCityAddress());
                    if(StringUtils.isNotBlank(heir.getPostalAddress()))
                        trackingHeir.setPostalCode(heir.getPostalAddress());
                    if(StringUtils.isNotBlank(heir.getAddressProvince()))
                        trackingHeir.setProvince(heir.getAddressProvince());
                    if(StringUtils.isNotBlank(heir.getAddressStatus()))
                        trackingHeir.setAddressStatus(heir.getAddressStatus());
                    if(StringUtils.isNotBlank(heir.getKin()))
                        trackingHeir.setRelationship(heir.getKin());
                    trackingHeir.setAcceptedHeritage(heir.getAcceptInheritance());
                    trackingHeir.setRequest(request);
                    trackingHeir.setSubject(request.getSubject());
                    ConnectionManager.save(trackingHeir, session);
                    trackingHeirs.add(trackingHeir);
                });
        return trackingHeirs;
    }

    private List<TrackingProperty> populateTrackingProperty(Request request, List<DataPropertyDTO> properties,
                                                        StringBuilder sb, Session session){
        List<TrackingProperty> trackingProperties = new ArrayList<>();
        CollectionUtils.emptyIfNull(properties)
                .stream()
                .forEach(property -> {
                    sb.setLength(0);
                    TrackingProperty trackingProperty = new TrackingProperty();
                    if(StringUtils.isNotBlank(property.getNote()))
                        trackingProperty.setNotes(property.getNote());
                    if(property.getPra() != null)
                        trackingProperty.setPra(property.getPra());
                    if(StringUtils.isNotBlank(property.getPrecautionaryAct()))
                        trackingProperty.setProtectiveAct(property.getPrecautionaryAct());

                    if(StringUtils.isNotBlank(property.getForeclosures()))
                        trackingProperty.setDistraints(property.getForeclosures());

                    if(property.getImportPrejudicial() != null)
                        trackingProperty.setImportPrejudicial(property.getImportPrejudicial());

                    if(StringUtils.isNotBlank(property.getPrejudicial()))
                        trackingProperty.setPrejudicial(property.getPrejudicial());

                    if(StringUtils.isNotBlank(property.getQuoteValue()))
                        trackingProperty.setQuoteValue(property.getQuoteValue());

                    if(StringUtils.isNotBlank(property.getFreeValue()))
                        trackingProperty.setFreeValue(property.getFreeValue());

                    if(property.getValueMortgage() != null)
                        trackingProperty.setValueMortgage(property.getValueMortgage());

                    if(StringUtils.isNotBlank(property.getValueRelationship()))
                        trackingProperty.setValueRelationship(property.getValueRelationship());

                    if(StringUtils.isNotBlank(property.getIntentionalBurden()))
                        trackingProperty.setIntentionalBurden(property.getIntentionalBurden());

                    if(StringUtils.isNotBlank(property.getJudicialBurden()))
                        trackingProperty.setJudicialBurden(property.getJudicialBurden());

                    if(StringUtils.isNotBlank(property.getPropertyValue()))
                        trackingProperty.setPropertyValue(property.getPropertyValue());

                    if(property.getNumberFreeProperty() != null)
                        trackingProperty.setNumberProperty(property.getNumberFreeProperty());

                    if(StringUtils.isNotBlank(property.getLandRegistry()))
                        trackingProperty.setLandRegistry(property.getLandRegistry());

                    if(property.getNumberLand() != null)
                        trackingProperty.setNumberLand(property.getNumberLand());

                    if(property.getNumberProperty() != null)
                        trackingProperty.setNumberProperty(property.getNumberProperty());

                    if(StringUtils.isNotBlank(property.getPropertyOwned()))
                        trackingProperty.setPropertyOwned(property.getPropertyOwned());

                    if(StringUtils.isNotBlank(property.getCadastral()))
                        trackingProperty.setCadastral(property.getCadastral());

                    if(StringUtils.isNotBlank(property.getOwnership()))
                        trackingProperty.setOwnership(property.getOwnership());

                    if(StringUtils.isNotBlank(property.getToponymAddress()))
                        sb.append(property.getToponymAddress());
                    if(StringUtils.isNotBlank(property.getStreetAddress())){
                        sb.append(" ");
                        sb.append(property.getStreetAddress());
                    }
                    if(StringUtils.isNotBlank(property.getAddressAt())){
                        sb.append(" presso ");
                        sb.append(property.getAddressAt());
                    }
                    trackingProperty.setAddress(sb.toString().trim());
                    if(StringUtils.isNotBlank(property.getCivicAddress()))
                        trackingProperty.setCivicAddress(property.getCivicAddress());
                    if(StringUtils.isNotBlank(property.getCityAdress()))
                        trackingProperty.setAddressCity(property.getCityAdress());

                    if(StringUtils.isNotBlank(property.getPostalAddress()))
                        trackingProperty.setAddressCap(property.getPostalAddress());

                    if(StringUtils.isNotBlank(property.getAddressProvince()))
                        trackingProperty.setAddressProvince(property.getAddressProvince());

                    if(StringUtils.isNotBlank(property.getAddressStatus()))
                        trackingProperty.setAddressStatus(property.getAddressStatus());

                    if(StringUtils.isNotBlank(property.getSheet()))
                        trackingProperty.setSheet(property.getSheet());

                    if(StringUtils.isNotBlank(property.getParticle()))
                        trackingProperty.setParticle(property.getParticle());

                    if(StringUtils.isNotBlank(property.getClassification()))
                        trackingProperty.setClassification(property.getClassification());

                    if(StringUtils.isNotBlank(property.getConsistency()))
                        trackingProperty.setConsistency(property.getConsistency());

                    if(StringUtils.isNotBlank(property.getRevenue()))
                        trackingProperty.setRevenue(property.getRevenue());

                    if(StringUtils.isNotBlank(property.getMatch()))
                        trackingProperty.setMatch(property.getMatch());

                    if(StringUtils.isNotBlank(property.getIdCadastral()))
                        trackingProperty.setIdCadastral(property.getIdCadastral());

                    if(StringUtils.isNotBlank(property.getOther()))
                        trackingProperty.setOther(property.getOther());

                    if(StringUtils.isNotBlank(property.getInspectionDate()))
                        trackingProperty.setInspectionDate(
                                DateTimeHelper.fromString(property.getInspectionDate(), DateTimeHelper.getMySQLDatePattern()));

                    if(StringUtils.isNotBlank(property.getPropertyClass()))
                        trackingProperty.setPropertyClass(property.getPropertyClass());

                    if(StringUtils.isNotBlank(property.getRepertoire()))
                        trackingProperty.setRepertoire(property.getRepertoire());

                    if(StringUtils.isNotBlank(property.getPublicOfficial()))
                        trackingProperty.setPubblicOfficial(property.getPublicOfficial());

                    if(StringUtils.isNotBlank(property.getSub()))
                        trackingProperty.setSub(property.getSub());

                    if(StringUtils.isNotBlank(property.getNotesLandRegistry()))
                        trackingProperty.setNotesLandRegistry(property.getNotesLandRegistry());
                    trackingProperty.setRequest(request);
                    trackingProperty.setSubject(request.getSubject());
                    ConnectionManager.save(trackingProperty, session);
                    trackingProperties.add(trackingProperty);
                });
        return trackingProperties;
    }

    private List<TrackingTel> populateTrackingPhone(Request request, List<DataPhoneDTO> phones, Session session){
        List<TrackingTel> trackingPhones = new ArrayList<>();
        CollectionUtils.emptyIfNull(phones)
                .stream()
                .forEach(phone -> {
                    TrackingTel trackingTel = new TrackingTel();
                    if(StringUtils.isNotBlank(phone.getNote()))
                        trackingTel.setNotes(phone.getNote());
                    if(StringUtils.isNotBlank(phone.getNumber()))
                        trackingTel.setTelephone(phone.getNumber());
                    trackingTel.setRequest(request);
                    trackingTel.setSubject(request.getSubject());
                    ConnectionManager.save(trackingTel, session);
                    trackingPhones.add(trackingTel);
                });
        return trackingPhones;
    }

    private List<TrackingBankAccount> populateTrackingBankAccount(Request request, List<DataBankAccountDTO> bankAccounts,
                                                    StringBuilder sb, Session session){
        List<TrackingBankAccount> trackingBankAccounts = new ArrayList<>();
        CollectionUtils.emptyIfNull(bankAccounts)
                .stream()
                .forEach(bankAccount -> {
                    sb.setLength(0);
                    TrackingBankAccount trackingbankAccount = new TrackingBankAccount();
                    if(StringUtils.isNotBlank(bankAccount.getNote()))
                        trackingbankAccount.setNotes(bankAccount.getNote());
                    if(StringUtils.isNotBlank(bankAccount.getChecked()))
                        trackingbankAccount.setChecked(bankAccount.getChecked());
                    if(StringUtils.isNotBlank(bankAccount.getName()))
                        trackingbankAccount.setName(bankAccount.getName());
                    if(StringUtils.isNotBlank(bankAccount.getVat()))
                        trackingbankAccount.setVat(bankAccount.getVat());
                    if(StringUtils.isNotBlank(bankAccount.getToponymAddress()))
                        sb.append(bankAccount.getToponymAddress());
                    if(StringUtils.isNotBlank(bankAccount.getStreetAddress())){
                        sb.append(" ");
                        sb.append(bankAccount.getStreetAddress());
                    }
                    if(StringUtils.isNotBlank(bankAccount.getAddressAt())){
                        sb.append(" presso ");
                        sb.append(bankAccount.getAddressAt());
                    }
                    trackingbankAccount.setAddress(sb.toString().trim());
                    if(StringUtils.isNotBlank(bankAccount.getCivicAddress()))
                        trackingbankAccount.setCivicAddress(bankAccount.getCivicAddress());
                    if(StringUtils.isNotBlank(bankAccount.getCityAddress()))
                        trackingbankAccount.setAddressCity(bankAccount.getCityAddress());
                    if(StringUtils.isNotBlank(bankAccount.getPostalAddress()))
                        trackingbankAccount.setAddressCap(bankAccount.getPostalAddress());
                    if(StringUtils.isNotBlank(bankAccount.getAddressProvince()))
                        trackingbankAccount.setAddressProvince(bankAccount.getAddressProvince());
                    if(StringUtils.isNotBlank(bankAccount.getAddressStatus()))
                        trackingbankAccount.setAddressStatus(bankAccount.getAddressStatus());
                    if(StringUtils.isNotBlank(bankAccount.getCityAddress()))
                        trackingbankAccount.setAddressCity(bankAccount.getCityAddress());
                    if(StringUtils.isNotBlank(bankAccount.getAbi()))
                        trackingbankAccount.setAbi(bankAccount.getAbi());
                    if(StringUtils.isNotBlank(bankAccount.getCab()))
                        trackingbankAccount.setCab(bankAccount.getCab());
                    if(StringUtils.isNotBlank(bankAccount.getActive()))
                        trackingbankAccount.setActive(bankAccount.getActive());
                    if(StringUtils.isNotBlank(bankAccount.getProtests()))
                        trackingbankAccount.setProtests(bankAccount.getProtests());
                    trackingbankAccount.setRequest(request);
                    trackingbankAccount.setSubject(request.getSubject());
                    ConnectionManager.save(trackingbankAccount, session);
                    trackingBankAccounts.add(trackingbankAccount);
                });
        return trackingBankAccounts;
    }

    private void populateWorks(Request request, List<DataWorkDTO> works,
                            StringBuilder sb, Session session){
                        CollectionUtils.emptyIfNull(works)
                                .stream()
                                .forEach(work -> {
                                    sb.setLength(0);
                                    TrackingPdl trackingPdl = new TrackingPdl();
                                    if(StringUtils.isNotBlank(work.getNote()))
                                        trackingPdl.setNotes(work.getNote());
                                    if(work.getNetAmount() != null)
                                        trackingPdl.setNetAmount(work.getNetAmount());
                                    if(work.getGrossAmount() != null)
                                        trackingPdl.setGrossAmount(work.getGrossAmount());
                                    if(StringUtils.isNotBlank(work.getLastDate()))
                                        trackingPdl.setLastDate(
                                                DateTimeHelper.fromString(work.getLastDate(), DateTimeHelper.getMySQLDatePattern()));
                                    if(StringUtils.isNotBlank(work.getStartDate()))
                                        trackingPdl.setJobStartDate(
                                                DateTimeHelper.fromString(work.getStartDate(), DateTimeHelper.getMySQLDatePattern()));
                                    if(StringUtils.isNotBlank(work.getType()))
                                        trackingPdl.setType(work.getType());
                                    if(StringUtils.isNotBlank(work.getEmployer()))
                                        trackingPdl.setEmployer(work.getEmployer());
                                    if(StringUtils.isNotBlank(work.getEmployerFiscalCode()))
                                        trackingPdl.setEmployerFiscalCode(work.getEmployerFiscalCode());
                                    if(StringUtils.isNotBlank(work.getEmployerVat()))
                                        trackingPdl.setEmployerVat(work.getEmployerVat());
                                    if(StringUtils.isNotBlank(work.getLegalToponymAddress()))
                                        sb.append(work.getLegalToponymAddress());
                                    if(StringUtils.isNotBlank(work.getLegalStreetAddress())){
                                        sb.append(" ");
                                        sb.append(work.getLegalStreetAddress());
                                    }
                                    if(StringUtils.isNotBlank(work.getLegalAddressAt())){
                                        sb.append(" presso ");
                                        sb.append(work.getLegalAddressAt());
                                    }
                                    trackingPdl.setEmployerLegalAddress(sb.toString().trim());
                                    if(StringUtils.isNotBlank(work.getLegalCivicAddress()))
                                        trackingPdl.setEmployerLegalCivicAddress(work.getLegalCivicAddress());
                                    if(StringUtils.isNotBlank(work.getLegalCityAddress()))
                                        trackingPdl.setEmployerLegalAddressCity(work.getLegalCityAddress());
                                    if(StringUtils.isNotBlank(work.getLegalPostalAddress()))
                                        trackingPdl.setEmployerLegalAddressCap(work.getLegalPostalAddress());
                                    if(StringUtils.isNotBlank(work.getLegalAddressProvince()))
                                        trackingPdl.setEmployerLegalAddressProvince(work.getLegalAddressProvince());
                                    if(StringUtils.isNotBlank(work.getLegalAddressStatus()))
                                        trackingPdl.setEmployerLegalAddressStatus(work.getLegalAddressStatus());
                                    if(StringUtils.isNotBlank(work.getCellLegalAddress()))
                                        trackingPdl.setCellLegalAddress(work.getCellLegalAddress());
                                    if(StringUtils.isNotBlank(work.getCell2LegalAddress()))
                                        trackingPdl.setCell2LegalAddress(work.getCell2LegalAddress());
                                    if(StringUtils.isNotBlank(work.getFaxLegalAddress()))
                                        trackingPdl.setFaxLegalAddress(work.getFaxLegalAddress());
                                    sb.setLength(0);
                                    if(StringUtils.isNotBlank(work.getOperationalToponymAddress()))
                                        sb.append(work.getOperationalToponymAddress());
                                    if(StringUtils.isNotBlank(work.getOperationalStreetAddress())){
                                        sb.append(" ");
                                        sb.append(work.getOperationalStreetAddress());
                                    }
                                    if(StringUtils.isNotBlank(work.getOperationalAddressAt())){
                                        sb.append(" presso ");
                                        sb.append(work.getOperationalAddressAt());
                                    }
                                    trackingPdl.setEmployerHeadquarter(sb.toString().trim());
                                    if(StringUtils.isNotBlank(work.getOperationalCivicAddress()))
                                        trackingPdl.setEmployerHeadquarterCivicAddress(work.getOperationalCivicAddress());
                                    if(StringUtils.isNotBlank(work.getOperationalCityAddress()))
                                        trackingPdl.setEmployerHeadquarterCity(work.getOperationalCityAddress());
                                    if(StringUtils.isNotBlank(work.getOperationalPostalAddress()))
                                        trackingPdl.setEmployerHeadquarterCap(work.getOperationalPostalAddress());
                                    if(StringUtils.isNotBlank(work.getOperationalAddressProvince()))
                                        trackingPdl.setEmployerHeadquarterProvince(work.getOperationalAddressProvince());
                                    if(StringUtils.isNotBlank(work.getOperationalAddressStatus()))
                                        trackingPdl.setEmployerHeadquarterStatus(work.getOperationalAddressStatus());
                                    if(StringUtils.isNotBlank(work.getCellHeadquarter()))
                                        trackingPdl.setCellHeadquarter(work.getCellHeadquarter());
                                    if(StringUtils.isNotBlank(work.getFaxHeadquarter()))
                                        trackingPdl.setFaxHeadquarter(work.getFaxHeadquarter());
                                    if(StringUtils.isNotBlank(work.getEmployee()))
                                        trackingPdl.setEmployee(work.getEmployee());
                                    if(StringUtils.isNotBlank(work.getContractType()))
                                        trackingPdl.setContractType(work.getContractType());
                                    if(StringUtils.isNotBlank(work.getExpirationContract()))
                                        trackingPdl.setExpirationContract(work.getExpirationContract());
                                    if(work.getSalary() != null)
                                        trackingPdl.setSalary(work.getSalary());
                                    if(StringUtils.isNotBlank(work.getUnemployed()))
                                        trackingPdl.setUnemployed(work.getUnemployed());
                                    if(StringUtils.isNotBlank(work.getProfessional()))
                                        trackingPdl.setProfessional(work.getProfessional());
                                    trackingPdl.setRequest(request);
                                    trackingPdl.setSubject(request.getSubject());
                                    ConnectionManager.save(trackingPdl, session);
                                });
    }

    private void populateRetirements(Request request, List<DataPensionDTO> pensions, StringBuilder sb, Session session){
        CollectionUtils.emptyIfNull(pensions)
                .stream()
                .forEach(pension -> {
                    sb.setLength(0);
                    TrackingPdl trackingPdl = new TrackingPdl();
                    if(StringUtils.isNotBlank(pension.getNote()))
                        trackingPdl.setRetirementNotes(pension.getNote());
                    if(pension.getRetired() != null)
                        trackingPdl.setRetired(pension.getRetired());
                    if(StringUtils.isNotBlank(pension.getLastDate()))
                        trackingPdl.setLastDate(
                                DateTimeHelper.fromString(pension.getLastDate(), DateTimeHelper.getMySQLDatePattern()));
                    if(pension.getNetAmount() != null)
                        trackingPdl.setNetAmount(pension.getNetAmount());
                    if(pension.getGrossAmount() != null)
                        trackingPdl.setGrossAmount(pension.getGrossAmount());
                    if(StringUtils.isNotBlank(pension.getRetirementStartDate()))
                        trackingPdl.setRetirementStartDate(
                                DateTimeHelper.fromString(pension.getRetirementStartDate(), DateTimeHelper.getMySQLDatePattern()));
                    if(StringUtils.isNotBlank(pension.getType()))
                        trackingPdl.setRetirementType(pension.getType());
                    if(StringUtils.isNotBlank(pension.getRetirementName()))
                        trackingPdl.setRetirementName(pension.getRetirementName());
                    if(StringUtils.isNotBlank(pension.getRetirementVat()))
                        trackingPdl.setRetirementVat(pension.getRetirementVat());
                    if(StringUtils.isNotBlank(pension.getRetirementFiscalCode()))
                        trackingPdl.setRetirementFiscalCode(pension.getRetirementFiscalCode());
                    if(StringUtils.isNotBlank(pension.getInstitutionToponymAddress()))
                        sb.append(pension.getInstitutionToponymAddress());
                    if(StringUtils.isNotBlank(pension.getInstitutionStreetAddress())){
                        sb.append(" ");
                        sb.append(pension.getInstitutionStreetAddress());
                    }
                    if(StringUtils.isNotBlank(pension.getInstitutionAddressAt())){
                        sb.append(" presso ");
                        sb.append(pension.getInstitutionAddressAt());
                    }
                    trackingPdl.setRetirementLegalAddress(sb.toString().trim());
                    if(StringUtils.isNotBlank(pension.getInstitutionCivicAddress()))
                        trackingPdl.setRetirementLegalCivicAddress(pension.getInstitutionCivicAddress());
                    if(StringUtils.isNotBlank(pension.getInstitutionCityAddress()))
                        trackingPdl.setRetirementLegalAddress(pension.getInstitutionCityAddress());
                    if(StringUtils.isNotBlank(pension.getInstitutionPostalAddress()))
                        trackingPdl.setRetirementLegalAddressCap(pension.getInstitutionPostalAddress());
                    if(StringUtils.isNotBlank(pension.getInstitutionAddressProvince()))
                        trackingPdl.setRetirementLegalAddressProvince(pension.getInstitutionAddressProvince());
                    if(StringUtils.isNotBlank(pension.getInstitutionAddressStatus()))
                        trackingPdl.setRetirementLegalAddressStatus(pension.getInstitutionAddressStatus());
                    sb.setLength(0);
                    if(StringUtils.isNotBlank(pension.getOperationalToponymAddress()))
                        sb.append(pension.getOperationalToponymAddress());
                    if(StringUtils.isNotBlank(pension.getOperationalStreetAddress())){
                        sb.append(" ");
                        sb.append(pension.getOperationalStreetAddress());
                    }
                    if(StringUtils.isNotBlank(pension.getOperationalAddressAt())){
                        sb.append(" presso ");
                        sb.append(pension.getOperationalAddressAt());
                    }
                    trackingPdl.setRetirementHeadquarter(sb.toString().trim());
                    if(StringUtils.isNotBlank(pension.getOperationalCivicAddress()))
                        trackingPdl.setRetirementHeadquarterCivicAddress(pension.getOperationalCivicAddress());
                    if(StringUtils.isNotBlank(pension.getOperationalCityAddress()))
                        trackingPdl.setRetirementHeadquarterCity(pension.getOperationalCityAddress());
                    if(StringUtils.isNotBlank(pension.getOperationalPostalAddress()))
                        trackingPdl.setRetirementHeadquarterCap(pension.getOperationalPostalAddress());
                    if(StringUtils.isNotBlank(pension.getOperationalAddressProvince()))
                        trackingPdl.setRetirementHeadquarterProvince(pension.getOperationalAddressProvince());
                    if(StringUtils.isNotBlank(pension.getOperationalAddressStatus()))
                        trackingPdl.setRetirementHeadquarterStatus(pension.getOperationalAddressStatus());
                    trackingPdl.setRequest(request);
                    trackingPdl.setSubject(request.getSubject());
                    ConnectionManager.save(trackingPdl, session);
                });
    }

    private void populateCharges(Request request, List<DataChargeDTO> charges, Session session){
        CollectionUtils.emptyIfNull(charges)
                .stream()
                .forEach(charge -> {
                    TrackingPosition trackingPosition = new TrackingPosition();
                    if(StringUtils.isNotBlank(charge.getNote()))
                        trackingPosition.setNotes(charge.getNote());
                    if(StringUtils.isNotBlank(charge.getPositionVat()))
                        trackingPosition.setPositionVat(charge.getPositionVat());
                    if(StringUtils.isNotBlank(charge.getPositionName()))
                        trackingPosition.setPositionName(charge.getPositionName());
                    if(StringUtils.isNotBlank(charge.getPositionActive()))
                        trackingPosition.setPositionActive(charge.getPositionActive());
                    if(StringUtils.isNotBlank(charge.getStatusPosition()))
                        trackingPosition.setStatusPosition(charge.getStatusPosition());
                    if(charge.getSocio() != null)
                        trackingPosition.setSocio(charge.getSocio());
                    if(StringUtils.isNotBlank(charge.getGiuridicNature()))
                        trackingPosition.setGiuridicNature(charge.getGiuridicNature());
                    if(StringUtils.isNotBlank(charge.getStartDate()))
                        trackingPosition.setStartDate(
                                DateTimeHelper.fromString(charge.getStartDate(), DateTimeHelper.getMySQLDatePattern()));
                    if(StringUtils.isNotBlank(charge.getStatusSociety()))
                        trackingPosition.setStatusSociety(charge.getStatusSociety());
                    if(StringUtils.isNotBlank(charge.getPositionLegalAddress()))
                        trackingPosition.setPositionLegalAddress(charge.getPositionLegalAddress());
                    if(StringUtils.isNotBlank(charge.getCode()))
                        trackingPosition.setCode(charge.getCode());
                    if(StringUtils.isNotBlank(charge.getDateNomination()))
                        trackingPosition.setDateNomination(
                                DateTimeHelper.fromString(charge.getDateNomination(), DateTimeHelper.getMySQLDatePattern()));
                    if(charge.getStopped() != null)
                        trackingPosition.setStopped(charge.getStopped());
                    trackingPosition.setRequest(request);
                    trackingPosition.setSubject(request.getSubject());
                    ConnectionManager.save(trackingPosition, session);
                });
    }

    private void populateParticipations(Request request, List<DataParticipationDTO> participations, Session session){
        CollectionUtils.emptyIfNull(participations)
                .stream()
                .forEach(participation -> {
                    TrackingPosition trackingPosition = new TrackingPosition();
                    if(StringUtils.isNotBlank(participation.getNote()))
                        trackingPosition.setNotes(participation.getNote());
                    if(StringUtils.isNotBlank(participation.getParticipationVat()))
                        trackingPosition.setParticipationVat(participation.getParticipationVat());
                    if(StringUtils.isNotBlank(participation.getParticipationName()))
                        trackingPosition.setParticipationName(participation.getParticipationName());
                    if(StringUtils.isNotBlank(participation.getParticipationActive()))
                        trackingPosition.setParticipationActive(participation.getParticipationActive());
                    if(StringUtils.isNotBlank(participation.getParticipationStatus()))
                        trackingPosition.setParticipationStatus(participation.getParticipationStatus());
                    if(participation.getParticipationSocio() != null)
                        trackingPosition.setParticipationSocio(participation.getParticipationSocio());
                    trackingPosition.setRequest(request);
                    trackingPosition.setSubject(request.getSubject());
                    ConnectionManager.save(trackingPosition, session);
                });
    }

    private void populateAssignmentPension(Request request, List<DataAssignmentPensionDTO> assignmentPensions, Session session){
        CollectionUtils.emptyIfNull(assignmentPensions)
                .stream()
                .forEach(assignmentPension -> {
                    TrackingPdl trackingPdl = new TrackingPdl();
                    if(StringUtils.isNotBlank(assignmentPension.getRetirementTransferNote()))
                        trackingPdl.setRetirementTransferNote(assignmentPension.getRetirementTransferNote());
                    if(assignmentPension.getRetirementTransferProgress() != null)
                        trackingPdl.setRetirementTransferProgress(assignmentPension.getRetirementTransferProgress());
                    if(assignmentPension.getRetirementTransferAmount() != null)
                        trackingPdl.setRetirementTransferAmount(assignmentPension.getRetirementTransferAmount());
                    if(StringUtils.isNotBlank(assignmentPension.getRetirementTransferEnd()))
                        trackingPdl.setRetirementTransferEnd(
                                DateTimeHelper.fromString(assignmentPension.getRetirementTransferEnd(), DateTimeHelper.getMySQLDatePattern()));
                    trackingPdl.setRequest(request);
                    trackingPdl.setSubject(request.getSubject());
                    ConnectionManager.save(trackingPdl, session);
                });
    }

    private void populateJobTransfers(Request request, List<DataJobTransferDTO> transfers, Session session){
        CollectionUtils.emptyIfNull(transfers)
                .stream()
                .forEach(transfer -> {
                    TrackingPdl trackingPdl = new TrackingPdl();
                    if(StringUtils.isNotBlank(transfer.getWorkTransferNote()))
                        trackingPdl.setRetirementTransferNote(transfer.getWorkTransferNote());
                    if(transfer.getWorkTransferProgress() != null)
                        trackingPdl.setWorkTransferProgress(transfer.getWorkTransferProgress());
                    if(transfer.getWorkTransferAmount() != null)
                        trackingPdl.setWorkTransferAmount(transfer.getWorkTransferAmount());
                    if(StringUtils.isNotBlank(transfer.getWorkTransferEnd()))
                        trackingPdl.setWorkTransferEnd(
                                DateTimeHelper.fromString(transfer.getWorkTransferEnd(),
                                        DateTimeHelper.getMySQLDatePattern()));
                    trackingPdl.setRequest(request);
                    trackingPdl.setSubject(request.getSubject());
                    ConnectionManager.save(trackingPdl, session);
                });
    }

    private void populatePensionForclosures(Request request, List<DataPensionForeclosureDTO> foreclosures, Session session){
        CollectionUtils.emptyIfNull(foreclosures)
                .stream()
                .forEach(foreclosure -> {
                    TrackingPdl trackingPdl = new TrackingPdl();
                    if(StringUtils.isNotBlank(foreclosure.getDistraintRetirementNote()))
                        trackingPdl.setDistraintRetirementNote(foreclosure.getDistraintRetirementNote());
                    if(foreclosure.getDistraintRetirementProgress() != null)
                        trackingPdl.setDistraintRetirementProgress(foreclosure.getDistraintRetirementProgress());
                    if(foreclosure.getDistraintRetirementAmount() != null)
                        trackingPdl.setDistraintRetirementAmount(foreclosure.getDistraintRetirementAmount());
                    if(StringUtils.isNotBlank(foreclosure.getDistraintRetirementEnd()))
                        trackingPdl.setDistraintRetirementEnd(
                                DateTimeHelper.fromString(foreclosure.getDistraintRetirementEnd(),
                                        DateTimeHelper.getMySQLDatePattern()));
                    trackingPdl.setRequest(request);
                    trackingPdl.setSubject(request.getSubject());
                    ConnectionManager.save(trackingPdl, session);
                });
    }

    private void populateWorkForclosures(Request request, List<DataWorkForeclosureDTO> foreclosures, Session session){
        CollectionUtils.emptyIfNull(foreclosures)
                .stream()
                .forEach(foreclosure -> {
                    TrackingPdl trackingPdl = new TrackingPdl();
                    if(StringUtils.isNotBlank(foreclosure.getDistraintWorkNote()))
                        trackingPdl.setDistraintWorkNote(foreclosure.getDistraintWorkNote());
                    if(foreclosure.getDistraintWorkProgress() != null)
                        trackingPdl.setDistraintWorkProgress(foreclosure.getDistraintWorkProgress());
                    if(foreclosure.getDistraintWorkAmount() != null)
                        trackingPdl.setDistraintWorkAmount(foreclosure.getDistraintWorkAmount());
                    if(StringUtils.isNotBlank(foreclosure.getDistraintWorkEnd()))
                        trackingPdl.setDistraintWorkEnd(DateTimeHelper.fromString(foreclosure.getDistraintWorkEnd(),
                                        DateTimeHelper.getMySQLDatePattern()));
                    trackingPdl.setRequest(request);
                    trackingPdl.setSubject(request.getSubject());
                    ConnectionManager.save(trackingPdl, session);
                });
    }
}
