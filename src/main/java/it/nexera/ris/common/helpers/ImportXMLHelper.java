package it.nexera.ris.common.helpers;

import it.nexera.ris.common.enums.AnnotationForPDFXMLElements;
import it.nexera.ris.common.enums.CadastralDataXMLElements;
import it.nexera.ris.common.enums.CommunicationForPDFXMLElements;
import it.nexera.ris.common.enums.CommunicationXMLElements;
import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.common.enums.EstateFormalityGroupXMLElements;
import it.nexera.ris.common.enums.EstateFormalityXMLElements;
import it.nexera.ris.common.enums.EstateLocationsXMLElements;
import it.nexera.ris.common.enums.NextProcedureFormXMLElements;
import it.nexera.ris.common.enums.ParentXMLTag;
import it.nexera.ris.common.enums.PropertyAnnotationBuildingTablePDFXMLElements;
import it.nexera.ris.common.enums.PropertyAnnotationForPDFXMLElements;
import it.nexera.ris.common.enums.PropertyForPDFXMLElements;
import it.nexera.ris.common.enums.PropertyInstatRowForPDFXMLElements;
import it.nexera.ris.common.enums.PropertyInstatTableForPDFXMLElements;
import it.nexera.ris.common.enums.PropertyRowsForPDFXMLElements;
import it.nexera.ris.common.enums.PropertyXMLElements;
import it.nexera.ris.common.enums.RealEstateType;
import it.nexera.ris.common.enums.RelationshipType;
import it.nexera.ris.common.enums.SubjectForPDFXMLElements;
import it.nexera.ris.common.enums.SubjectForSyntheticListXMLElements;
import it.nexera.ris.common.enums.SubjectType;
import it.nexera.ris.common.enums.SubjectXMLElements;
import it.nexera.ris.common.enums.SubjectXMLTag;
import it.nexera.ris.common.enums.SyntheticFormalitiesForPDFXMLElements;
import it.nexera.ris.common.enums.SyntheticListXMLElements;
import it.nexera.ris.common.enums.TagsForPDFXMLElements;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.exceptions.TypeActNotConfigureException;
import it.nexera.ris.common.xml.wrappers.CadastralDataXMLWrapper;
import it.nexera.ris.common.xml.wrappers.CommunicationXMLWrapper;
import it.nexera.ris.common.xml.wrappers.EstateFormalityGroupXMLWrapper;
import it.nexera.ris.common.xml.wrappers.EstateFormalityXMLWrapper;
import it.nexera.ris.common.xml.wrappers.EstateLocationsXMLWrapper;
import it.nexera.ris.common.xml.wrappers.NextProcedureFormXMLWrapper;
import it.nexera.ris.common.xml.wrappers.PropertyXMLWrapper;
import it.nexera.ris.common.xml.wrappers.SubjectXMLWrapper;
import it.nexera.ris.persistence.SessionManager;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.CadastralCategory;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Country;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.LandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;
import it.nexera.ris.web.beans.pages.EstateSituationViewBean;
import it.nexera.ris.web.beans.wrappers.Pair;
import it.nexera.ris.web.beans.wrappers.logic.UploadSubjectWrapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Vlad Strunenko
 * <p>
 * <pre>
 * class for parse XML file
 * </pre>
 */
public class ImportXMLHelper extends BaseHelper {

    private static final String DESCRIPTION_SYNTHETIC_LIST = "Periodo recuperato e validato";

    private static final String PROPERTY_BUILDING_TOTAL_FOOTER = "** Si intendono escluse le \"superfici di balconi, " +
            "terrazzi e aree scoperte pertinenziali e accessorie, comunicanti o non comunicanti\"\n" +
            "(cfr. Provvedimento del Direttore dell'Agenzia delle Entrate 29 marzo 2013).";

    private static final String PERSON_PATTERN = "(([A-ZÀÈÉÌÍÎÒÓÙÚ`]{2,}\\s?){1,3})\\s(([a-zA-ZàèéìíîòóùúÀÈÉÌÍÎÒÓÙÚ`]{3,}\\s?){1," +
            "3})\\s(nata|nato)\\s(a?|in)\\s?(([A-ZÀÈÉÌÍÎÒÓÙÚ`]{1,}\\s?){1,})\\s\\w*?\\s(\\d*\\/\\d*\\/\\d*)";

    private static final String LEGAL_PATTERN = "(.+)(\\s*con sede in)\\s(([A-ZÀÈÉÌÍÎÒÓÙÚ`]\\s?)+)";

    private static final String PERSON_PATTERN_ALT = "(([A-Z]{2,}\\s?){1,3})\\s(([a-zA-Z]{3,}\\s?){1,"
            + "3})\\s(\\d*\\/\\d*\\/\\d*)\\s(([A-Z]{2,}\\s?){1,3})(\\([A-Z]{2,}\\))";

    private static final String VANI = "VANI";

    private static final String TRUE = "true";

    public static List<EstateFormality> handleXMLTagsEstateFormality(File inputFile, Request request,
                                                                     it.nexera.ris.persistence.beans.entities.domain.Document document,
                                                                     Session session) throws TypeActNotConfigureException {
        List<EstateFormality> estateFormalities = new ArrayList<>();
        Transaction tr = null;
        try {
            tr = session.beginTransaction();
            inputFile = prepareFile(inputFile);
            Document doc = prepareDocument(inputFile);
            if (doc == null) return null;
            NodeList baseNList = doc.getElementsByTagName("SituazioneAggiornamento");
            EstateFormalityGroupXMLWrapper group = new EstateFormalityGroupXMLWrapper();
            for (int temp = 0; temp < baseNList.getLength(); temp++) {
                Node nNode = baseNList.item(temp);
                EstateFormalityGroupXMLWrapper importGroup = fillEstateFormalityGroupXMLWrapper(nNode);
                if (importGroup.getConservationDate() != null && (group.getConservationDate() == null
                        || importGroup.getConservationDate().after(group.getConservationDate()))) {
                    group = importGroup;
                }
            }
            EstateFormalityGroup groupEntity = group.toEntity();

            List<EstateFormalityGroup> list = DaoManager.load(EstateFormalityGroup.class, new Criterion[]{
                    Restrictions.eq("conservationDate", groupEntity.getConservationDate())});
            if (ValidationHelper.isNullOrEmpty(list)) {
                ConnectionManager.save(groupEntity, session);
            } else {
                groupEntity = list.get(0);
            }
            String chargesRegistryName = getEstateFormalityChargesRegistry(doc);
            LandChargesRegistry registry = ConnectionManager.get(LandChargesRegistry.class, new Criterion[]{
                    Restrictions.eq("name", chargesRegistryName),
                    Restrictions.eq("type", request.getAggregationLandChargesRegistry()
                            .getLandChargesRegistries().get(0).getType())
            }, session);
            if (request != null && !ValidationHelper.isNullOrEmpty(groupEntity.getConservationDate())) {
                RequestConservatory requestConservatory = ConnectionManager.get(RequestConservatory.class, new Criterion[]{
                        Restrictions.eq("request.id", request.getId()),
                        Restrictions.eq("registry.id", registry.getId())
                }, session);
                if (requestConservatory == null) {
                    requestConservatory = new RequestConservatory();
                    requestConservatory.setRequest(request);
                    requestConservatory.setRegistry(registry);
                    requestConservatory.setConservatoryDate(groupEntity.getConservationDate());
                    ConnectionManager.save(requestConservatory, session);
                } else {
                    if (groupEntity.getConservationDate().after(requestConservatory.getConservatoryDate())) {
                        requestConservatory.setConservatoryDate(groupEntity.getConservationDate());
                        ConnectionManager.save(requestConservatory, session);
                    }
                }
            }
            baseNList = doc.getElementsByTagName("DocumentoIpotecario");
            for (int temp = 0; temp < baseNList.getLength(); temp++) {

                Node nNode = baseNList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    List<EstateLocationsXMLWrapper> estateList = new ArrayList<>();
                    List<NextProcedureFormXMLWrapper> nextProcList = new ArrayList<>();
                    List<CommunicationXMLWrapper> communicationList = new ArrayList<>();
                    if (nNode.hasChildNodes()) {

                        NodeList children = nNode.getChildNodes();

                        for (int j = 0; j < children.getLength(); j++) {
                            if (!(children.item(j) instanceof Element)) continue;
                            switch (children.item(j).getNodeName()) {
                                case "UbicazioneImmobili":
                                    estateList.add(fillEstateLocationsXMLWrapper(children.item(j)));
                                    break;
                                case "FormalitaSuccessive":
                                    nextProcList.add(fillNextProcedureFormXMLWrapper(children.item(j)));
                                    break;
                                case "Comunicazioni":
                                    communicationList.add(fillCommunicationXMLWrapper(children.item(j)));
                                    break;
                            }
                        }
                    }
                    EstateFormality estateFormality = fillProcedureFormXMLWrapper(nNode, registry)
                            .toEntityWithException(session);
                    Integer year = estateFormality.getReferenceYear();
                    List<EstateFormality> estateFormalityList = DaoManager.load(EstateFormality.class,
                            new CriteriaAlias[]{new CriteriaAlias("typeAct", "tA", JoinType.INNER_JOIN)},
                            new Criterion[]{
                                    Restrictions.eq("numRP", estateFormality.getNumRP()),
                                    Restrictions.eq("numRG", estateFormality.getNumRG()),
                                    Restrictions.eq("date", estateFormality.getDate()),
                                    Restrictions.eq("landChargesRegistry", estateFormality.getLandChargesRegistry()),
                                    Restrictions.or(
                                            Restrictions.eq("repertoire", estateFormality.getRepertoire()),
                                            Restrictions.isNull("repertoire")),
                                    Restrictions.eq("estateFormalityType", estateFormality.getEstateFormalityType()),
                                    Restrictions.eq("tA.type", estateFormality.getTypeAct().getType())});

                    if (!ValidationHelper.isNullOrEmpty(estateFormalityList)) {
                        estateFormality = estateFormalityList.get(0);
                        if(year != null && year > 0)
                            estateFormality.setReferenceYear(year);
                    }
                    estateFormality.setEstateFormalityGroup(groupEntity);
                    estateFormality.setDocument(document);
                    if (!isEstateFormalityExists(estateFormality, request, session)) {
                        saveNewEstateFormality(estateFormality, request, estateList, nextProcList, communicationList, session);
                    } else {
                        saveEstateFormalitySuccess(nextProcList, estateFormality, session);
                        saveCommunications(communicationList, estateFormality, session);
                        ConnectionManager.save(estateFormality, session);
                    }
                    estateFormalities.add(estateFormality);
                }
            }
            DocumentSubject documentSubject;
            if (request != null) {
                documentSubject = new DocumentSubject(document, request.getSubject(),
                        registry, null, getEstateFormalityDate(doc), DocumentType.ESTATE_FORMALITY);
                ConnectionManager.save(documentSubject, session);
            } else {
                Node nNode = doc.getElementsByTagName("PersonaGiuridica").item(0);
                if (nNode == null) {
                    nNode = doc.getElementsByTagName("PersonaFisica").item(0);
                }
                SubjectXMLWrapper subject = new SubjectXMLWrapper();
                Element eElement = (Element) nNode;
                for (SubjectForSyntheticListXMLElements element : SubjectForSyntheticListXMLElements.values()) {
                    String value = getValueFromXML(eElement, element.getElement());
                    if (!ValidationHelper.isNullOrEmpty(value)) {
                        subject.setField(element, value);
                    }
                }
                List<Subject> subjectsDB;
                if (!ValidationHelper.isNullOrEmpty(subject.getNumberVAT())) {
                    subjectsDB = ConnectionManager.load(Subject.class, new Criterion[]{
                            Restrictions.eq("numberVAT", subject.getNumberVAT())
                    }, session);
                    if (ValidationHelper.isNullOrEmpty(subjectsDB)) {
                        Subject ent = new Subject();
                        ent.setNumberVAT(subject.getNumberVAT());
                        ent.setTypeId(SubjectType.LEGAL_PERSON.getId());
                        ent.setIncomplete(true);
                        ConnectionManager.save(ent, session);
                        subjectsDB = Collections.singletonList(ent);
                    }
                } else {
                    subjectsDB = ConnectionManager.load(Subject.class, new Criterion[]{
                            Restrictions.eq("name", subject.getName()),
                            Restrictions.eq("surname", subject.getSurname()),
                            Restrictions.eq("birthDate", subject.getBirthDate())
                    }, session);
                    if (ValidationHelper.isNullOrEmpty(subjectsDB)) {
                        Subject ent = new Subject();
                        ent.setName(subject.getName());
                        ent.setSurname(subject.getSurname());
                        ent.setBirthDate(subject.getBirthDate());
                        ent.setTypeId(SubjectType.PHYSICAL_PERSON.getId());
                        ent.setIncomplete(true);
                        ConnectionManager.save(ent, session);
                        subjectsDB = Collections.singletonList(ent);
                    }
                }
                for (Subject sub : subjectsDB) {
                    documentSubject = new DocumentSubject(document, sub,
                            ConnectionManager.get(LandChargesRegistry.class, new Criterion[]{
                                    Restrictions.eq("name", chargesRegistryName)
                            }, session), null, getEstateFormalityDate(doc), DocumentType.ESTATE_FORMALITY);
                    documentSubject.setUseSubjectFromXml(true);
                    ConnectionManager.save(documentSubject, session);
                }
            }
        } catch (TypeActNotConfigureException e) {
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
            throw e;
        } catch (PersistenceBeanException | IllegalAccessException | InstantiationException e) {
            LogHelper.log(log, e);
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
        } finally {
            if (tr != null && tr.isActive() && !tr.wasRolledBack()) {
                tr.commit();
            }
        }
        return estateFormalities;
    }


    public static boolean isEstateFormalityExists(EstateFormality estateFormality, Request request, Session session)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<EstateFormality> estateFormalities = ConnectionManager.load(EstateFormality.class, new CriteriaAlias[]{
                new CriteriaAlias("requestFormalities", "request", JoinType.INNER_JOIN)
        }, new Criterion[]{
                Restrictions.eq("landChargesRegistry", estateFormality.getLandChargesRegistry()),
                Restrictions.eq("date", estateFormality.getDate()),
                Restrictions.eq("numRG", estateFormality.getNumRG()),
                Restrictions.eq("numRP", estateFormality.getNumRP()),
                Restrictions.eq("request.request.id", request == null ? 0L : request.getId()),
                Restrictions.ne("id", estateFormality.isNew() ? 0L : estateFormality.getId())
        }, session);

        if(!ValidationHelper.isNullOrEmpty(estateFormalities)){
            List<RequestFormality> requestFormalities = ConnectionManager.load(RequestFormality.class, new Criterion[]{
                    Restrictions.eq("request.id", request.getId()),
                            Restrictions.eq("formality.id", estateFormalities.get(0).getId())
            }, session);

            if(!ValidationHelper.isNullOrEmpty(requestFormalities)){
                request.getEstateFormalityList().remove(requestFormalities.get(0).getFormality());
                request.getEstateFormalityList().add(estateFormality);
                ConnectionManager.save(request, false, session);
            }
        }
        return estateFormalities.size() != 0;
    }

    private static void saveCommunications(List<CommunicationXMLWrapper> communicationList, EstateFormality estateFormality, Session session){
        List<Communication> communications = convertCommunicationWrapper(communicationList);

        if (!ValidationHelper.isNullOrEmpty(communications)) {
            for (Communication communication : communications) {
                List<Communication> existsCommunication = ConnectionManager.load(Communication.class, new Criterion[]{
                        Restrictions.eq("communicationCode", communication.getCommunicationCode()),
                        Restrictions.eq("particularRegister", communication.getParticularRegister()),
                        Restrictions.eq("receiveDate", communication.getReceiveDate()),
                        Restrictions.eq("communicationDate", communication.getCommunicationDate()),
                        Restrictions.eq("extinctionDate", communication.getExtinctionDate()),
                        Restrictions.or(Restrictions.eq("formalityType", communication.getFormalityType()),
                                Restrictions.isNull("formalityType")),
                        Restrictions.eq("estateFormality.id", estateFormality.getId()),
                }, session);


                if (ValidationHelper.isNullOrEmpty(existsCommunication)) {
                    communication.setEstateFormality(estateFormality);
                    ConnectionManager.save(communication, session);
                }
            }
        }
    }

    private static void saveEstateFormalitySuccess(List<NextProcedureFormXMLWrapper> nextProcList, EstateFormality estateFormality, Session session){
        List<EstateFormalitySuccess> nextProcedureFormList = convertNextProcedureWrapper(nextProcList);
        if (!ValidationHelper.isNullOrEmpty(nextProcedureFormList)) {
            for (EstateFormalitySuccess npToSave : nextProcedureFormList) {
                List<EstateFormalitySuccess> success = ConnectionManager.load(EstateFormalitySuccess.class, new Criterion[]{
                        Restrictions.eq("numRP", npToSave.getNumRP()),
                        Restrictions.eq("date", npToSave.getDate()),
                        Restrictions.eq("noteType", npToSave.getNoteType()),
                        Restrictions.eq("estateFormality.id", estateFormality.getId())
                }, session);
                if (ValidationHelper.isNullOrEmpty(success)) {
                    npToSave.setEstateFormality(estateFormality);
                    ConnectionManager.save(npToSave, session);
                }
            }
        }
    }

    public static String getEstateFormalityChargesRegistry(Document doc) {
        NodeList nList = doc.getElementsByTagName("ElencoSintetico");
        return getValueFromXML((Element) nList.item(0), "ElencoSintetico.DescrizioneUfficio").trim();
    }

    private static Date getEstateFormalityDate(Document doc) {
        NodeList nList = doc.getElementsByTagName("ElencoSintetico");
        return DateTimeHelper.fromXMLString(getValueFromXML((Element) nList.item(0),
                "ElencoSintetico.DataRichiesta").trim());
    }

    private static void saveNewEstateFormality(EstateFormality estateFormality, Request request,
                                               List<EstateLocationsXMLWrapper> estateList,
                                               List<NextProcedureFormXMLWrapper> nextProcList,
                                               List<CommunicationXMLWrapper> communicationList, Session session)
            throws InstantiationException, IllegalAccessException, PersistenceBeanException {
        if (ValidationHelper.isNullOrEmpty(estateFormality.getRequestList())) {
            estateFormality.setRequestList(new LinkedList<>());
        }
        estateFormality.getRequestList().add(request);
        estateFormality.setAccountable(true);
        boolean isNewRequestFormality = estateFormality.addRequestFormality(request, session);
        if (isNewRequestFormality && request.getService().getIsUpdateAndNotNull()) {
            estateFormality.setRequestListUpdate(new LinkedList<>());
            estateFormality.getRequestListUpdate().add(request);
        }
        ConnectionManager.save(estateFormality, session);

        List<EstateLocation> estateLocationsList = convertEstateLocationsWrapper(estateList);
        if (!ValidationHelper.isNullOrEmpty(estateLocationsList)) {
            for (EstateLocation esToSave : estateLocationsList) {
                esToSave.setEstateFormality(estateFormality);
                ConnectionManager.save(esToSave, session);
            }
        }
        saveEstateFormalitySuccess(nextProcList, estateFormality, session);

        saveCommunications(communicationList, estateFormality, session);

    }

    private static File prepareFile(File inputFile) {
        try {
            if (GeneralFunctionsHelper.P7M.equalsIgnoreCase(FileHelper.
                    getFileExtension(inputFile.getPath()).replace(".", ""))) {
                byte[] inputData = Files.readAllBytes(Paths.get(inputFile.getPath()));
                byte[] data = GeneralFunctionsHelper.getData(inputData);

                if (data != null) {
                    String randomFileName = FileHelper.getRandomFileName("1.xml");

                    return new File(FileHelper.writeFileToFolder(randomFileName,
                            new File(FileHelper.getLocalFileDir()), data));
                }
            }
        } catch (IOException e) {
            LogHelper.log(log, e);
        }
        return inputFile;
    }

    private static List<Communication> convertCommunicationWrapper(List<CommunicationXMLWrapper> communicationInput) {
        if (!ValidationHelper.isNullOrEmpty(communicationInput)) {
            return communicationInput.stream().map(CommunicationXMLWrapper::toEntity).collect(Collectors.toList());
        }
        return null;
    }

    private static List<EstateLocation> convertEstateLocationsWrapper(List<EstateLocationsXMLWrapper> estateList) {
        try {
            if (!ValidationHelper.isNullOrEmpty(estateList)) {
                List<EstateLocation> estateLocationsList = new ArrayList<>();
                for (EstateLocationsXMLWrapper elWrapper : estateList) {
                    estateLocationsList.add(elWrapper.toEntity());
                }
                return estateLocationsList;
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return null;
    }

    private static List<EstateFormalitySuccess> convertNextProcedureWrapper(List<NextProcedureFormXMLWrapper> nextProcedure) {
        try {
            if (!ValidationHelper.isNullOrEmpty(nextProcedure)) {
                List<EstateFormalitySuccess> nextProcedureList = new ArrayList<>();
                for (NextProcedureFormXMLWrapper npWrapper : nextProcedure) {
                    nextProcedureList.add(npWrapper.toEntity());
                }
                return nextProcedureList;
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return null;
    }

    private static EstateFormalityGroupXMLWrapper fillEstateFormalityGroupXMLWrapper(Node nNode) {
        EstateFormalityGroupXMLWrapper group = new EstateFormalityGroupXMLWrapper();

        for (EstateFormalityGroupXMLElements element : EstateFormalityGroupXMLElements.values()) {
            org.w3c.dom.NamedNodeMap namedNodeMap = nNode.getAttributes();
            if (!ValidationHelper.isNullOrEmpty(namedNodeMap.getNamedItem(element.getElement()))) {
                String value = namedNodeMap.getNamedItem(element.getElement()).getNodeValue();
                group.setField(element, value);
            }
        }
        return group;
    }

    private static EstateFormalityXMLWrapper fillProcedureFormXMLWrapper(Node nNode, LandChargesRegistry chargesRegistry) {
        EstateFormalityXMLWrapper procedure = new EstateFormalityXMLWrapper();
        procedure.setChargesRegistry(chargesRegistry);
        for (EstateFormalityXMLElements element : EstateFormalityXMLElements.values()) {
            org.w3c.dom.NamedNodeMap namedNodeMap = nNode.getAttributes();
            if (!ValidationHelper.isNullOrEmpty(namedNodeMap.getNamedItem(element.getElement()))) {
                String value = namedNodeMap.getNamedItem(element.getElement()).getNodeValue();
                procedure.setField(element, value);
            }
        }
        return procedure;
    }

    private static CommunicationXMLWrapper fillCommunicationXMLWrapper(Node nNode) {
        CommunicationXMLWrapper communication = new CommunicationXMLWrapper();
        for (CommunicationXMLElements element : CommunicationXMLElements.values()) {
            org.w3c.dom.NamedNodeMap namedNodeMap = nNode.getAttributes();
            if (!ValidationHelper.isNullOrEmpty(namedNodeMap.getNamedItem(element.getElement()))) {
                String value = namedNodeMap.getNamedItem(element.getElement()).getNodeValue();
                communication.setField(element, value);
            }
        }
        return communication;
    }

    private static EstateLocationsXMLWrapper fillEstateLocationsXMLWrapper(Node nNode) {
        EstateLocationsXMLWrapper estate = new EstateLocationsXMLWrapper();
        for (EstateLocationsXMLElements element : EstateLocationsXMLElements.values()) {
            org.w3c.dom.NamedNodeMap namedNodeMap = nNode.getAttributes();
            if (!ValidationHelper.isNullOrEmpty(namedNodeMap.getNamedItem(element.getElement()))) {
                String value = namedNodeMap.getNamedItem(element.getElement()).getNodeValue();
                estate.setField(element, value);
            }
        }
        return estate;
    }

    private static NextProcedureFormXMLWrapper fillNextProcedureFormXMLWrapper(Node nNode) {
        NextProcedureFormXMLWrapper nextProc = new NextProcedureFormXMLWrapper();

        for (NextProcedureFormXMLElements element : NextProcedureFormXMLElements
                .values()) {
            org.w3c.dom.NamedNodeMap namedNodeMap = nNode.getAttributes();
            if (!ValidationHelper.isNullOrEmpty(namedNodeMap.getNamedItem(element.getElement()))) {
                String value = namedNodeMap.getNamedItem(element.getElement()).getNodeValue();
                nextProc.setField(element, value);
            }
        }
        return nextProc;
    }

    public static String handlXMLTagsForPDF(File inputFile, String html, Session session)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        Document doc = prepareDocument(inputFile);
        if (doc == null) return html;
        for (TagsForPDFXMLElements element : TagsForPDFXMLElements.values()) {
            if (html.contains(element.getElementHTML())) {
                String value;

                if (element.isSpecialFlow()) {
                    value = handleSpecialTags(doc.getDocumentElement(), element, session);
                } else {
                    value = getValueFromXML(doc.getDocumentElement(), element.getElementXML());
                }
                value = Matcher.quoteReplacement(value);
                if (value.contains("*")) {
                    int place = value.indexOf("*");
                    value = value.substring(0, place).concat("\\*").concat(value.substring(place + 1));
                }
                html = html.replaceAll(element.getElementHTML(), value);
            }
        }
        return appendFooter(doc, html);
    }

    private static String appendFooter(Document doc, String html) {
        boolean needFooter = false;
        NodeList nods = doc.getDocumentElement().getElementsByTagName("SuperficieF");
        if (nods.getLength() > 0) {
            for (int temp = 0; temp < nods.getLength(); temp++) {
                String text = getValueFromXML((Element) nods.item(temp), "SuperficieF.TotaleE");
                if (!ValidationHelper.isNullOrEmpty(text)) {
                    needFooter = true;
                }
            }
        }
        if (needFooter) {
            return html.replaceAll("%PROPERTY_BUILDING_TOTAL_FOOTER%", PROPERTY_BUILDING_TOTAL_FOOTER);
        } else {
            return html.replaceAll("%PROPERTY_BUILDING_TOTAL_FOOTER%", "");
        }
    }

    public static List<Subject> loadAllSubjects(File inputFile, Session session)
            throws PersistenceBeanException, IllegalAccessException, java.lang.InstantiationException {
        List<Subject> subjects = new ArrayList<>();

        Document doc = prepareDocument(inputFile);
        if (doc == null) return null;
        NodeList baseNList = doc.getElementsByTagName("SoggettiImmobiliSelezionati");

        if (baseNList.getLength() > 0) {
            NodeList nList = ((Element) baseNList.item(0)).getElementsByTagName("SoggettoF");

            if (nList.getLength() == 0) {
                nList = ((Element) baseNList.item(0)).getElementsByTagName("SoggettoN");
            }

            for (int temp = 0; temp < nList.getLength(); temp++) {

                SubjectXMLWrapper subject = fillSubjectXMLWrapper(nList.item(temp));
                Subject subjectEnt = subject.loadByFiscalCode(session);

                if (subjectEnt == null) {
                    subjectEnt = subject.toEntity();
                }

                subjectEnt.setTempId((long) (temp + 1));

                subjects.add(subjectEnt);
            }
        }

        return subjects;
    }

    public static Document prepareDocument(File inputFile) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

            dbFactory.setNamespaceAware(false);
            dbFactory.setValidating(false);
            dbFactory.setFeature("http://xml.org/sax/features/namespaces", false);
            dbFactory.setFeature("http://xml.org/sax/features/validation", false);
            dbFactory.setFeature(
                    "http://apache.org/xml/features/nonvalidating/load-dtd-grammar",
                    false);
            dbFactory.setFeature(
                    "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                    false);
            String content = FileUtils.readFileToString(inputFile, "UTF-8");
            InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(stream);

            doc.getDocumentElement().normalize();

            return doc;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LogHelper.log(log, e);
        }
        return null;
    }

    public static Pair<String, String> generateDocumentTitleAndCost(File inputFile, Long selectedTypeId, Request request) {
        StringJoiner joiner = new StringJoiner("_");
        String cost = "";
        if (!ValidationHelper.isNullOrEmptyMultiple(inputFile, selectedTypeId)) {
            Document doc = prepareDocument(inputFile);
            if (doc == null) return new Pair<>("", "");
            switch (DocumentType.getById(selectedTypeId)) {
                case CADASTRAL: {
                    NodeList list = doc.getElementsByTagName("DatiLiquidazione");
                    if (list.getLength() > 0) {
                        cost = getValueFromXML((Element) list.item(0), "DatiLiquidazione.TributiErariali").trim();
                    }
                    NodeList nList = doc.getElementsByTagName("DatiRichiesta");
                    if (nList.getLength() > 0) {
                        String value = getValueFromXML((Element) nList.item(0), "DatiRichiesta.Comune").trim();
                        if (!StringUtils.isBlank(value))
                            joiner.add(value);

                        value = getValueFromXML((Element) nList.item(0), "DatiRichiesta.Foglio").trim();
                        if (!StringUtils.isBlank(value))
                            joiner.add(value);

                        value = getValueFromXML((Element) nList.item(0), "DatiRichiesta.ParticellaNum").trim();
                        if (!StringUtils.isBlank(value))
                            joiner.add(value);

                        value = getValueFromXML((Element) nList.item(0), "DatiRichiesta.Subalterno").trim();
                        if (!StringUtils.isBlank(value))
                            joiner.add(value);

                        value = getValueFromXML((Element) nList.item(0), "DatiRichiesta.Provincia").trim();
                        if (!StringUtils.isBlank(value))
                            joiner.add(value);
                    }
                    joiner.add("CAT");
                    Date date = getCadastralDate(doc);
                    if (date != null) {
                        joiner.add(DateTimeHelper.toXMLPatern(date).trim());
                    }

                    nList = doc.getElementsByTagName("SoggettoIndividuato");
                    if (nList.getLength() > 0) {
                        NodeList subElement = ((Element) nList.item(0)).getElementsByTagName("SoggettoPF");
                        if (subElement.getLength() > 0) {
                            joiner.add(((Element) subElement.item(0)).getAttribute("Cognome").trim());
                            joiner.add(((Element) subElement.item(0)).getAttribute("Nome").trim());
                            joiner.add(((Element) subElement.item(0)).getAttribute("DataNascita").trim());
                        } else {
                            subElement = ((Element) nList.item(0)).getElementsByTagName("SoggettoPNF");
                            if (subElement.getLength() > 0) {
                                joiner.add(((Element) subElement.item(0)).getAttribute("Denominazione").trim());
                            }
                        }
                        if (subElement.getLength() > 0) {
                            joiner.add(((Element) subElement.item(0)).getAttribute("CodiceFiscale").trim());
                        }
                    }
                }
                break;

                case ESTATE_FORMALITY: {
                    joiner.add(getEstateFormalityChargesRegistry(doc));
                    joiner.add("EL");
                    NodeList nList = doc.getElementsByTagName("ElencoSintetico");
                    if (nList.getLength() > 0) {
                        String value = getValueFromXML((Element) nList.item(0), "ElencoSintetico.DataRichiesta");
                        joiner.add(DateTimeHelper.toXMLPatern(DateTimeHelper.fromXMLString(value)).trim());
                    }
                    if (!ValidationHelper.isNullOrEmpty(request)) {

                        NodeList soggettoFNode = doc.getElementsByTagName("SoggettoF");
                        NodeList soggettoNNode = doc.getElementsByTagName("SoggettoN");

                        String lastName = getValueFromTags(doc, "SoggettoF.Cognome");
                        String firstName = getValueFromTags(doc, "SoggettoF.Nome");
                        String birthDateString = getValueFromTags(doc, "SoggettoF.DataNascita");

                        String businessName = getValueFromTags(doc, "SoggettoN.Denominazione");
                        String numberVAT = getValueFromTags(doc, "SoggettoN.CodiceFiscale");

                        if ((request.isPhysicalPerson() && soggettoFNode.getLength() > 0) ||
                                (!request.isPhysicalPerson() && soggettoNNode.getLength() == 0)) {
                            Date birthDate = DateTimeHelper.fromXMLString(birthDateString);
                            joiner.add(lastName != null ? lastName : request.getSubject().getSurname().toUpperCase());
                            joiner.add(firstName != null ? firstName : request.getSubject().getName().toUpperCase());
                            joiner.add(birthDate != null ? DateTimeHelper.toXMLPatern(birthDate) : birthDateString != null ?
                                    birthDateString : DateTimeHelper.toXMLPatern(request.getSubject().getBirthDate()));
                            joiner.add(request.getSubject().getFiscalCode());
                        }else   if ((!request.isPhysicalPerson() && soggettoNNode.getLength() > 0) ||
                                (request.isPhysicalPerson() && soggettoFNode.getLength() == 0)) {
                            joiner.add(businessName != null ? businessName : request.getSubject().getBusinessName());
                            joiner.add(numberVAT != null ? numberVAT : request.getSubject().getNumberVAT());
                        }
                    }
                }

                break;

                default:
                    break;
            }
        }

        return new Pair<>(joiner.toString(), cost);
    }

    private static String getValueFromTags(Document doc, String xmlStringName) {
        String result = null;
        NodeList soggetto = doc.getElementsByTagName(xmlStringName.substring(0, xmlStringName.indexOf(".")));
        if (soggetto.getLength() > 0) {
            result = getValueFromXML((Element) soggetto.item(0), xmlStringName);
        }
        return result;
    }

    public static Subject checkEstateFormalitySubject(File inputFile, Request request, Session session)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        Document doc = prepareDocument(inputFile);
        if (doc == null) return null;
        Node nNode = doc.getElementsByTagName("PersonaGiuridica").item(0);
        if (nNode == null) {
            nNode = doc.getElementsByTagName("PersonaFisica").item(0);
        }
        if (Node.ELEMENT_NODE != nNode.getNodeType()) {
            return null;
        }
        SubjectXMLWrapper subject = new SubjectXMLWrapper();
        Element eElement = (Element) nNode;
        for (SubjectForSyntheticListXMLElements element : SubjectForSyntheticListXMLElements.values()) {
            String value = getValueFromXML(eElement, element.getElement());
            if (!ValidationHelper.isNullOrEmpty(value)) {
                subject.setField(element, value);
            }
        }
        Subject subjectEnt = subject.loadByFiscalCode(session);

        return subjectEnt != null && request.getSubject().getId().equals(subjectEnt.getId())
                ? null : subjectEnt;
    }

    public static Subject checkSubject(File inputFile, Request request, Session session)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {

        Document doc = prepareDocument(inputFile);
        if (doc == null) return null;
        Node nNode = doc.getElementsByTagName(ParentXMLTag.SUBJECT.getElement()).item(0);
        if (ValidationHelper.isNullOrEmpty(nNode) || Node.ELEMENT_NODE != nNode.getNodeType()) {
            return null;

        }
        SubjectXMLWrapper subjectXMLWrapper = new SubjectXMLWrapper();
        Element eElement = (Element) nNode;
        for (SubjectXMLElements element : SubjectXMLElements.values()) {
            String value = getValueFromXML(eElement, element.getElement());
            if (!ValidationHelper.isNullOrEmpty(value)) {
                subjectXMLWrapper.setField(element, value);
            }
        }
        Subject subjectEnt = subjectXMLWrapper.loadByFiscalCode(session);

        if (subjectEnt != null && request.getSubject() != null && request.getSubject().getId().equals(subjectEnt.getId())) {
            return null;
        } else {
            return subjectEnt;
        }
    }

    public static String handleXML(File inputFile, it.nexera.ris.persistence.beans.entities.domain.Document document,
                                   Request request, Boolean useRequestSubject, Session session)
            throws PersistenceBeanException, IllegalAccessException,
            InstantiationException {
        String subjectFiscalCode = null;

        Document doc = prepareDocument(inputFile);
        if (doc == null) return null;
        NodeList nList = doc.getElementsByTagName("DatiRichiesta");
        String str = getValueFromXML((Element) nList.item(0), "DatiRichiesta.Provincia").trim();
        Province docProvince = ConnectionManager.get(Province.class, new Criterion[]{
                Restrictions.eq("code", str)
        }, session);
        Date docDate = getCadastralDate(doc);

        Subject subject = handleSubject(doc.getElementsByTagName(ParentXMLTag.SUBJECT.getElement()).item(0), session);
        if (subject != null) {
            subjectFiscalCode = SubjectType.PHYSICAL_PERSON.getId().equals(subject.getTypeId())
                    ? subject.getFiscalCode() : subject.getNumberVAT();
        }

        NodeList nodeList = doc.getElementsByTagName("VisuraFabbricatiStorica");
        if (nodeList.getLength() != 0) {
            savePropertySubjectForStoricaDocument(((Element) nodeList.item(0)), subjectFiscalCode, request, doc,
                    document, useRequestSubject, docProvince, docDate, session);
        } else {
            nodeList = doc.getElementsByTagName("GruppoUnitaImmobiliari");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element eElement = (Element) nodeList.item(i);
                savePropertySubjectForCadastralDocument(eElement, subjectFiscalCode, request, doc, document,
                        useRequestSubject, docProvince, docDate, session);
            }
        }

        if (!ValidationHelper.isNullOrEmpty(useRequestSubject) && useRequestSubject
                && !ValidationHelper.isNullOrEmpty(request.getSubject())) {
            subjectFiscalCode = SubjectType.PHYSICAL_PERSON.getId().equals(request.getSubject().getTypeId())
                    ? request.getSubject().getFiscalCode() : request.getSubject().getNumberVAT();
            subject = request.getSubject();
        }
        DocumentSubject documentSubject = new DocumentSubject(document, subject,
                null, docProvince, docDate, DocumentType.CADASTRAL);
        ConnectionManager.save(documentSubject, true, session);
        return subjectFiscalCode;
    }

    private static void savePropertySubjectForStoricaDocument(Element eElement, String subjectFiscalCode, Request request, Document doc,
                                                              it.nexera.ris.persistence.beans.entities.domain.Document document,
                                                              Boolean useRequestSubject, Province docProvince, Date docDate, Session session)
            throws IllegalAccessException, PersistenceBeanException, InstantiationException {

        List<Property> propertyList = new ArrayList<>();
        List<UploadSubjectWrapper> subjects = new ArrayList<>();

        for (ParentXMLTag parentTag : ParentXMLTag.values()) {
            NodeList nList = eElement.getElementsByTagName(parentTag.getElement());

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);

                switch (parentTag) {
                    case PROPERTY_BUILDING:
                    case PROPERTY_BUILDING_ALT:
                        if (nNode.getAttributes().getNamedItem("IndiceImmobile") != null
                                && "1".equals(nNode.getAttributes().getNamedItem("IndiceImmobile").getNodeValue())) {
                            City propertyCity = getPropertyCityFromSpecialTag(eElement);
                            Property property = handlePropertyAndCreateDocumentPropertyFromNode(subjectFiscalCode, request,
                                    document, docProvince, docDate, session, parentTag, nNode, propertyCity);

                            if (property != null) {
                                propertyList.add(property);

                                NodeList documentProperties = ((Element) nNode.getParentNode())
                                        .getElementsByTagName("DatiDerivantiDa");
                                saveDatafromProperties(session, property, documentProperties);

                                temp = nList.getLength();
                            }
                        }
                        break;

                    case STORIC_PROPERTY_SUBJECT:
                        subjects = handleAttachSubject(nNode, session);
                        break;

                    default:
                        break;

                }
            }
        }

        updateOrCreateRelationships(subjectFiscalCode, request, doc, document, useRequestSubject, docProvince,
                docDate, session, propertyList, subjects, DocumentType.CADASTRAL);
    }

    private static void saveDatafromProperties(Session session, Property property, NodeList documentProperties) {
        for (int i = 0; i < documentProperties.getLength(); ++i) {
            NodeList documentPropertyValues = documentProperties.item(i).getChildNodes();
            for (int j = 0; j < documentPropertyValues.getLength(); ++j) {
                DatafromProperty datafromProperty = new DatafromProperty();
                datafromProperty.setText(documentPropertyValues.item(j).getNodeValue());
                datafromProperty.setProperty(property);
                ConnectionManager.save(datafromProperty, true, session);
            }
        }
    }

    private static City getPropertyCityFromSpecialTag(Element eElement)
            throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        City propertyCity = null;
        if (eElement.getElementsByTagName("DatiRichiesta").getLength() != 0
                && eElement.getElementsByTagName("DatiRichiesta").item(0).getAttributes()
                .getNamedItem("CodiceComune") != null) {
            propertyCity = DaoManager.get(City.class, new Criterion[]{
                    Restrictions.eq("cfis", eElement.getElementsByTagName("DatiRichiesta")
                            .item(0).getAttributes().getNamedItem("CodiceComune").getNodeValue())
            });
        }
        return propertyCity;
    }

    private static void savePropertySubjectForCadastralDocument(Element eElement, String subjectFiscalCode, Request request, Document doc,
                                                                it.nexera.ris.persistence.beans.entities.domain.Document document,
                                                                Boolean useRequestSubject, Province docProvince, Date docDate, Session session)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<Property> propertyList = new ArrayList<>();
        List<UploadSubjectWrapper> subjects = new ArrayList<>();

        for (ParentXMLTag parentTag : ParentXMLTag.values()) {
            NodeList nList = eElement.getElementsByTagName(parentTag.getElement());

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);

                switch (parentTag) {
                    case PROPERTY_BUILDING:
                    case PROPERTY_LAND:
                    case PROPERTY_BUILDING_ALT:
                    case PROPERTY_LAND_ALT:
                        Property property = handlePropertyAndCreateDocumentPropertyFromNode(subjectFiscalCode,
                                request, document, docProvince, docDate, session, parentTag, nNode, null);
                        if (property != null) propertyList.add(property);
                        break;

                    case PROPERTY_SUBJECT:
                        subjects = handleAttachSubject(nNode, session);
                        break;

                    default:
                        break;
                }
            }
        }
        updateOrCreateRelationships(subjectFiscalCode, request, doc, document, useRequestSubject, docProvince,
                docDate, session, propertyList, subjects, DocumentType.INDIRECT_CADASTRAL);
        visualizeCityErrorDialogOnPage(propertyList);
    }

    private static Property handlePropertyAndCreateDocumentPropertyFromNode(String subjectFiscalCode, Request request,
                                                                            it.nexera.ris.persistence.beans.entities.domain.Document document,
                                                                            Province docProvince, Date docDate, Session session,
                                                                            ParentXMLTag parentTag, Node nNode, City propertyCity)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        boolean isLand = (parentTag == ParentXMLTag.PROPERTY_LAND) || (parentTag == ParentXMLTag.PROPERTY_LAND_ALT);
        Property property = handleProperty(nNode, subjectFiscalCode, request, propertyCity,
                isLand, session);
        if (property != null) {
            DocumentProperty documentProperty = new DocumentProperty(document, property,
                    null, new Date(), docProvince, docDate);
            ConnectionManager.save(documentProperty, true, session);
        }
        return property;
    }

    private static void updateOrCreateRelationships(String subjectFiscalCode, Request request, Document doc,
                                                    it.nexera.ris.persistence.beans.entities.domain.Document document,
                                                    Boolean useRequestSubject, Province docProvince, Date docDate,
                                                    Session session, List<Property> propertyList,
                                                    List<UploadSubjectWrapper> subjects, DocumentType documentType)
            throws InstantiationException, IllegalAccessException, PersistenceBeanException {
        for (UploadSubjectWrapper wrapper : subjects) {
            if (!ValidationHelper.isNullOrEmpty(wrapper) && !ValidationHelper.isNullOrEmpty(wrapper.getMainSubject())
                    && !ValidationHelper.isNullOrEmpty(propertyList)) {

                if (!(!ValidationHelper.isNullOrEmpty(subjectFiscalCode)
                        && (subjectFiscalCode.equals(wrapper.getMainSubject().getFiscalCode())
                        || subjectFiscalCode.equals(wrapper.getMainSubject().getNumberVAT())))) {
                    DocumentSubject documentSubject = ConnectionManager.get(DocumentSubject.class, new Criterion[]{
                            Restrictions.eq("document.id", document.getId()),
                            Restrictions.eq("subject.id", wrapper.getMainSubject().getId()),
                            Restrictions.eq("type", documentType)
                    }, session);
                    if (documentSubject == null) {
                        documentSubject = new DocumentSubject(document, wrapper.getMainSubject(),
                                null, docProvince, docDate, documentType);
                        ConnectionManager.save(documentSubject, true, session);
                    }
                }
                if (!ValidationHelper.isNullOrEmpty(subjectFiscalCode)
                        && (subjectFiscalCode.equals(wrapper.getMainSubject().getFiscalCode())
                        || subjectFiscalCode.equals(wrapper.getMainSubject().getNumberVAT()))) {
                    if (!ValidationHelper.isNullOrEmpty(useRequestSubject) && useRequestSubject
                            && !ValidationHelper.isNullOrEmpty(request.getSubject())) {
                        wrapper.setReplacedSubject(wrapper.getMainSubject());
                        wrapper.setMainSubject(request.getSubject());
                    }
                }
                saveRelationship(document, propertyList, wrapper, request, useRequestSubject, getCadastralDate(doc), session);
            }
        }
    }

    private static void visualizeCityErrorDialogOnPage(List<Property> propertyList) {
        HashSet<String> cfisSet = new HashSet<String>();
        for (Property property : propertyList) {
            if (!ValidationHelper.isNullOrEmpty(property.getCity()) &&
                    ValidationHelper.isNullOrEmpty(property.getCity().getDescription())) {
                cfisSet.add(property.getCity().getCfis());
            }
        }
        if (!ValidationHelper.isNullOrEmpty(cfisSet)) {
            StringBuilder stringBuilder = new StringBuilder();
            EstateSituationViewBean bean = (EstateSituationViewBean) SessionManager.getInstance().getSessionBean().getViewState().get(
                    "estateSituationViewBean");
            for (String s : cfisSet) {
                stringBuilder.append(ResourcesHelper.getValidation("cityNoPresentInProperty"));
                stringBuilder.append(s);
                stringBuilder.append("\n");
            }
            bean.setErrorCityNoPresent(stringBuilder.toString());
            bean.openCityErrorDialog();
        }
    }

    private static Date getCadastralDate(Document doc) {
        NodeList nList = doc.getElementsByTagName("TitoloVisura");
        if (nList.getLength() > 0) {
            String value = getValueFromXML((Element) nList.item(0), "TitoloVisura.Data");
            return DateTimeHelper.fromXMLString(value);
        }
        return null;
    }

    private static void saveRelationship(it.nexera.ris.persistence.beans.entities.domain.Document document,
                                         List<Property> properties, UploadSubjectWrapper subjectWrapper, Request request,
                                         Boolean useRequestSubject, Date cadastralDate, Session session)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {

        if (!ValidationHelper.isNullOrEmptyMultiple(document, subjectWrapper, properties)) {
            Subject subject = subjectWrapper.getMainSubject();
  /*          if (Boolean.TRUE.equals(useRequestSubject)) {
                subject = request.getSubject();
            }*/
            for (Property property : properties) {
                List<Relationship> relationships = getRelationships(subjectWrapper, request, subject, property);
                if (!ValidationHelper.isNullOrEmpty(relationships)) {
                    List<Relationship> relationshipsDistinct = new ArrayList<>();
                    for (Relationship r : relationships) {
                        relationshipsDistinct.removeIf(rd -> rd.getSubject().equals(r.getSubject())
                                && rd.getCadastralDate().compareTo(r.getCadastralDate()) < 0);
                        relationshipsDistinct.add(r);
                    }
                    for (Relationship r : relationshipsDistinct) {
                        updateRelationship(document, subjectWrapper, cadastralDate, session, property, r);
                    }
                } else {
                    updateRelationship(document, subjectWrapper, cadastralDate, session, property, null);
                }

                if (!ValidationHelper.isNullOrEmpty(subjectWrapper.getReplacedSubject())) {
                    property.setReplacedSubject(subjectWrapper.getReplacedSubject());
                    ConnectionManager.save(property, true, session);
                }
            }
        }
    }

    private static List<Relationship> getRelationships(UploadSubjectWrapper subjectWrapper, Request request,
                                                       Subject subject, Property property) {
        List<Relationship> resultRelationshipList = null;
        if (subject != null) {
            if (!ValidationHelper.isNullOrEmpty(property.getRelationships())) {
                resultRelationshipList = property.getRelationships().stream()
                        .filter(relationship -> relationship.getSubject().getId().equals(subject.getId())
                                && relationship.getPropertyType().equals(subjectWrapper.getRelationshipPropertyType())
                                && RelationshipType.CADASTRAL_DOCUMENT.getId().equals(relationship.getRelationshipTypeId()))
                        .collect(Collectors.toList());
            }
        } else {
            List<Long> documentsIds = request.getDocumentsRequest().stream()
                    .filter(x -> DocumentType.CADASTRAL.getId().equals(x.getTypeId())).map(IndexedEntity::getId)
                    .collect(Collectors.toList());

            if (!ValidationHelper.isNullOrEmpty(property.getRelationships())) {
                resultRelationshipList = property.getRelationships().stream()
                        .filter(relationship -> documentsIds.contains(relationship.getTableId())
                                && relationship.getPropertyType().equals(subjectWrapper.getRelationshipPropertyType())
                                && RelationshipType.CADASTRAL_DOCUMENT.getId().equals(relationship.getRelationshipTypeId()))
                        .collect(Collectors.toList());
            }
        }
        return resultRelationshipList;
    }

    private static void updateRelationship(it.nexera.ris.persistence.beans.entities.domain.Document document,
                                           UploadSubjectWrapper subjectWrapper, Date cadastralDate,
                                           Session session, Property property, Relationship relationship) {
        if (relationship != null) {
            relationship.setRegime(subjectWrapper.getRelationshipRegimeConiugi());
            relationship.setDerivataData(property.getArisingFromData());
            relationship.setTableId(document.getId());
        } else {
            relationship = new Relationship(subjectWrapper.getMainSubject(), property, document,
                    subjectWrapper.getRelationshipQuote(), subjectWrapper.getRelationshipPropertyType(),
                    subjectWrapper.getRelationshipRegimeConiugi());
            relationship.setCadastralDate(cadastralDate);
        }
        if (QuoteSimplifyHelper.checkQuote(subjectWrapper.getRelationshipQuote())) {
            relationship.setQuote(QuoteSimplifyHelper.simplify(subjectWrapper.getRelationshipQuote()));
        } else {
            relationship.setQuote(subjectWrapper.getRelationshipQuote());
        }
      /*if (Boolean.TRUE.equals(useRequestSubject)) {
            r.setSubject(request.getSubject());
         }*/
        ConnectionManager.save(relationship, true, session);
    }

    private static SubjectXMLWrapper fillSubjectXMLWrapper(Node nNode) {
        SubjectXMLWrapper subject = new SubjectXMLWrapper();

        if (Node.ELEMENT_NODE != nNode.getNodeType()) {
            return subject;
        }
        Element eElement = (Element) nNode;

        for (SubjectForSyntheticListXMLElements element : SubjectForSyntheticListXMLElements.values()) {
            String value = getValueFromXML(eElement, element.getElement());
            subject.setField(element, value);
        }

        return subject;
    }

    private static Subject handleSubjectForSyntheticList(Node nNode, Session session)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        NodeList nList = ((Element) nNode).getElementsByTagName("SoggettiImmobiliSelezionati");
        if (nList.getLength() > 0) {
            nNode = nList.item(0);
        }
        return saveSubjectEntity(nNode, session);
    }

    private static Subject saveSubjectEntity(Node nNode, Session session)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        SubjectXMLWrapper subject = fillSubjectXMLWrapper(nNode);
        Subject subjectEnt = subject.loadByFiscalCode(session);

        if (subjectEnt == null) {
            subjectEnt = subject.toEntity();
            ConnectionManager.save(subjectEnt, true, session);
        }

        return null;
    }

    private static List<UploadSubjectWrapper> handleAttachSubject(Node nNode, Session session)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<UploadSubjectWrapper> subjectList = new ArrayList<>();
        if (Node.ELEMENT_NODE != nNode.getNodeType()) {
            return subjectList;
        }
        NodeList nList = ((Element) nNode).getElementsByTagName("Intestato");

        for (int i = 0; i < nList.getLength(); i++) {
            Element eElement = (Element) nList.item(i);
            String fc = "";
            String subjectStr = "";
            String quote = "";
            String regimeConiugi = "";
            String typeReport = "";
            Subject subject = null;
            for (SubjectXMLTag element : SubjectXMLTag.values()) {
                String value = getValueFromXML(eElement, element.getElement());
                if (!ValidationHelper.isNullOrEmpty(value)) {
                    switch (element) {
                        case FISCAL_CODE:
                            fc = value;
                            break;

                        case SUBJECT:
                            subjectStr = value.replaceAll("\\r|\\n", "");
                            break;

                        case QUOTE:
                            quote = value.trim();
                            break;

                        case TYPE:
                            typeReport = value;
                            break;

                        case REGIME_CONIUGI:
                            regimeConiugi = value;
                            break;

                        default:
                            break;
                    }
                }
            }
            if (ValidationHelper.checkCorrectFormatByExpression(PERSON_PATTERN, subjectStr)) {
                subject = convertStringToPersonSubject(fc, subjectStr, session);
            } else if (ValidationHelper.checkCorrectFormatByExpression(LEGAL_PATTERN, subjectStr)) {
                subject = convertStringToLegalSubject(fc, subjectStr, session);
            } else if (ValidationHelper.checkCorrectFormatByExpression(PERSON_PATTERN_ALT, subjectStr)) {
                if(fc != null)
                    fc = fc.replaceAll("\\r|\\n", "");
                subject = convertNewFormatStringToPersonSubject(fc, subjectStr, session);
            } else {
                subject = crateNewLegalSubject(fc, subjectStr, session);
            }
            if (!ValidationHelper.isNullOrEmpty(subject)) {
                subjectList.add(new UploadSubjectWrapper(subject, null, quote, typeReport, regimeConiugi));
            }
        }
        return subjectList;
    }

    private static Subject crateNewLegalSubject(String numVat, String subjectStr, Session session) {
        if (ValidationHelper.isNullOrEmpty(numVat) || ValidationHelper.isNullOrEmpty(subjectStr)) return null;

        Subject subject = new Subject();
        subject.setNumberVAT(numVat);
        subject.setBusinessName(subjectStr);
        subject.setTypeId(SubjectType.LEGAL_PERSON.getId());
        ConnectionManager.save(subject, true, session);

        return subject;
    }

    private static Subject convertStringToLegalSubject(String numVat, String subjectStr, Session session)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (ValidationHelper.isNullOrEmpty(numVat) || ValidationHelper.isNullOrEmpty(subjectStr)) return null;
        Subject subject = null;
        Pattern pattern = Pattern.compile(LEGAL_PATTERN);
        Matcher m = pattern.matcher(subjectStr);
        while (m.find()) {
            if (!ValidationHelper.isNullOrEmpty(m.group(3))) {
                City city = null;
                List<City> cityList = ConnectionManager.load(City.class, new Criterion[]{Restrictions.eq("description",
                        m.group(3))}, session);
                if (!ValidationHelper.isNullOrEmpty(cityList)) {
                    city = cityList.get(0);
                }
                if (city != null && !ValidationHelper.isNullOrEmpty(city.getProvince())) {
                    List<Subject> subjectList = ConnectionManager.load(Subject.class, new CriteriaAlias[]{
                            new CriteriaAlias("birthCity", "city", JoinType.INNER_JOIN)
                    }, new Criterion[]{
                            Restrictions.eq("numberVAT", numVat),
                            Restrictions.eq("birthProvince.id", city.getProvince().getId()),
                            Restrictions.eq("city.description", city.getDescription())
                    }, session);

                    if (subjectList != null) {
                        for (Subject s : subjectList) {
                            if (RequestHelper.isBusinessNameFunctionallyEqual(s.getBusinessName(), m.group(1))) {
                                subject = s;
                                break;
                            }
                        }
                    }
                }

                //ART_RISFW-463 update data on new import
                if (ValidationHelper.isNullOrEmpty(subject)) {
                    subject = new Subject();
                }
                subject.setNumberVAT(numVat);
                subject.setBusinessName(m.group(1).trim());
                subject.setBirthCity(city);
                subject.setBirthProvince(subject.getBirthCity() != null ? subject.getBirthCity().getProvince() : null);
                subject.setTypeId(SubjectType.LEGAL_PERSON.getId());
                ConnectionManager.save(subject, true, session);
            }
        }
        return subject;
    }

    private static Subject convertStringToPersonSubject(String fiscalCode, String subjectStr, Session session)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (ValidationHelper.isNullOrEmpty(fiscalCode) || ValidationHelper.isNullOrEmpty(subjectStr)) return null;
        Subject subject = null;
        Pattern pattern = Pattern.compile(PERSON_PATTERN);
        Matcher m = pattern.matcher(subjectStr);
        City city = null;
        Country country = null;
        while (m.find()) {
            if (!ValidationHelper.isNullOrEmpty(CalcoloCodiceFiscale.getCityFiscalCode(fiscalCode))) {
                List<City> cities = ConnectionManager.load(City.class, new Criterion[]{Restrictions.eq("cfis",
                        CalcoloCodiceFiscale.getCityFiscalCode(fiscalCode)), Restrictions.isNotNull("province")}, session);
                List<Subject> subjects = null;
                List<Criterion> criterionList = new ArrayList<>(Arrays.asList(
                        Restrictions.eq("name", m.group(3)),
                        Restrictions.eq("surname", m.group(1)),
                        Restrictions.eq("birthDate",
                                DateTimeHelper.fromXMLString(m.group(9).replaceAll("/", ""))),
                        Restrictions.eq("fiscalCode", fiscalCode)
                ));

                if (!ValidationHelper.isNullOrEmpty(cities)) {
                    city = cities.get(0);
                    if (city != null && !ValidationHelper.isNullOrEmpty(city.getProvince())) {
                        criterionList.add(Restrictions.eq("birthProvince.id", city.getProvince().getId()));
                        criterionList.add(Restrictions.eq("city.description", city.getDescription()));

                        subjects = ConnectionManager.load(Subject.class, new CriteriaAlias[]{
                                new CriteriaAlias("birthCity", "city", JoinType.INNER_JOIN)
                        }, criterionList.toArray(new Criterion[0]), session);
                        if (!ValidationHelper.isNullOrEmpty(subjects)) {
                            subject = subjects.get(0);
                        }
                    }
                } else if (m.group(6).equals("in")) {
                    List<Country> countries = ConnectionManager.load(Country.class, new Criterion[]{
                            Restrictions.eq("description", m.group(7))}, session);
                    if (!ValidationHelper.isNullOrEmpty(countries)) {
                        country = countries.get(0);
                        criterionList.add(Restrictions.eq("country.id", country.getId()));
                        subjects = ConnectionManager.load(Subject.class, criterionList.toArray(new Criterion[0]), session);
                    }
                }
                if (!ValidationHelper.isNullOrEmpty(subjects)) {
                    subject = subjects.get(0);
                }
            }
            //ART_RISFW-463 update data on new import
            if (ValidationHelper.isNullOrEmpty(subject)) {
                subject = new Subject();
            }
            subject.setBirthDate(DateTimeHelper.fromXMLString(m.group(9).replaceAll("/", "")));
            subject.setSex(CalcoloCodiceFiscale.getSexFromFiscalCode(fiscalCode));
            subject.setName(m.group(3));
            subject.setSurname(m.group(1));
            subject.setBirthCity(city);
            subject.setCountry(country);
            subject.setBirthProvince(subject.getBirthCity() != null ? subject.getBirthCity().getProvince() : null);
            subject.setFiscalCode(fiscalCode);
            subject.setTypeId(SubjectType.PHYSICAL_PERSON.getId());
            ConnectionManager.save(subject, true, session);
        }
        return subject;
    }

    private static Subject convertNewFormatStringToPersonSubject(String fiscalCode, String subjectStr, Session session)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (ValidationHelper.isNullOrEmpty(fiscalCode) || ValidationHelper.isNullOrEmpty(subjectStr)) return null;
        Subject subject = null;
        Pattern pattern = Pattern.compile(PERSON_PATTERN_ALT);
        Matcher m = pattern.matcher(subjectStr);
        City city = null;
        Country country = null;
        while (m.find()) {
            if (!ValidationHelper.isNullOrEmpty(CalcoloCodiceFiscale.getCityFiscalCode(fiscalCode))) {
                List<City> cities = ConnectionManager.load(City.class, new Criterion[]{Restrictions.eq("cfis",
                        CalcoloCodiceFiscale.getCityFiscalCode(fiscalCode)), Restrictions.isNotNull("province")}, session);
                List<Subject> subjects = null;
                List<Criterion> criterionList = new ArrayList<>(Arrays.asList(
                        Restrictions.eq("name", m.group(3)),
                        Restrictions.eq("surname", m.group(1)),
                        Restrictions.eq("birthDate",
                                DateTimeHelper.fromXMLString(m.group(5).replaceAll("/", ""))),
                        Restrictions.eq("fiscalCode", fiscalCode)
                ));

                if (!ValidationHelper.isNullOrEmpty(cities)) {
                    city = cities.get(0);
                    if (city != null && !ValidationHelper.isNullOrEmpty(city.getProvince())) {
                        criterionList.add(Restrictions.eq("birthProvince.id", city.getProvince().getId()));
                        criterionList.add(Restrictions.eq("city.description", city.getDescription()));

                        subjects = ConnectionManager.load(Subject.class, new CriteriaAlias[]{
                                new CriteriaAlias("birthCity", "city", JoinType.INNER_JOIN)
                        }, criterionList.toArray(new Criterion[0]), session);
                        if (!ValidationHelper.isNullOrEmpty(subjects)) {
                            subject = subjects.get(0);
                        }
                    }
                }
                if (!ValidationHelper.isNullOrEmpty(subjects)) {
                    subject = subjects.get(0);
                }
            }
            //ART_RISFW-463 update data on new import
            if (ValidationHelper.isNullOrEmpty(subject)) {
                subject = new Subject();
            }
            subject.setBirthDate(DateTimeHelper.fromXMLString(m.group(5).replaceAll("/", "")));
            subject.setSex(CalcoloCodiceFiscale.getSexFromFiscalCode(fiscalCode));
            subject.setName(m.group(3));
            subject.setSurname(m.group(1));
            subject.setBirthCity(city);
            subject.setCountry(country);
            subject.setBirthProvince(subject.getBirthCity() != null ? subject.getBirthCity().getProvince() : null);
            subject.setFiscalCode(fiscalCode);
            subject.setTypeId(SubjectType.PHYSICAL_PERSON.getId());
            ConnectionManager.save(subject, true, session);
        }
        return subject;
    }

    private static Property handleProperty(Node nNode, String subjectFiscalCode, Request request, City propertyCity,
                                           Boolean needDefaultCode, Session session)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (Node.ELEMENT_NODE != nNode.getNodeType()) {
            return null;
        }
        PropertyXMLWrapper property = new PropertyXMLWrapper();
        Element eElement = (Element) nNode;

        for (PropertyXMLElements element : PropertyXMLElements.values()) {
            String value;
            if (element.isHaveSeveralTags()) {
                NodeList nList = eElement.getElementsByTagName(element.getElement().split("\\.")[0]);
                for (int temp = 0; temp < nList.getLength(); temp++) {
                    Node nNodeInner = nList.item(temp);
                    value = getValueFromXML((Element) nNodeInner, element.getElement());
                    property.setField(element, value);
                }
            } else if (element.isSpecialFlow()) {
                value = handleSpecialTags(eElement, element,
                        subjectFiscalCode);
                if(element.equals(PropertyXMLElements.CATEGORY_CODE) && !ValidationHelper.isNullOrEmpty(value) &&
                        eElement.getTagName().equalsIgnoreCase("ImmobileFabbricatiS")){
                    if(value.contains(":")){
                        value = value.split("\\:")[0].trim();
                    }
                }
                property.setField(element, value);
            } else {
                value = getValueFromXML(eElement, element.getElement());
                property.setField(element, value);
            }
        }

        if(eElement.getTagName().equalsIgnoreCase("ImmobileFabbricatiS")){
            if(ValidationHelper.isNullOrEmpty(property.getAddress())){
                String value  = getValueFromXML(eElement, "IndirizzoImm");
                if(!ValidationHelper.isNullOrEmpty(value)){
                    property.setField(PropertyXMLElements.ADDRESS, value);
                    Pattern pattern = Pattern.compile("Piano", Pattern.CASE_INSENSITIVE);
                    String tokens[] = pattern.split("VIA DELLA LIBERTA` n. SNC Piano S1");
                    if(tokens.length > 1)
                        property.setField(PropertyXMLElements.FLOOR, tokens[1]);
                }
            }
            if(ValidationHelper.isNullOrEmpty(property.getAdditionalData())){
                String value  = getValueFromXML(eElement, "Annotazione.Descrizione");
                if(!ValidationHelper.isNullOrEmpty(value)){
                    property.setField(PropertyXMLElements.ADDITIONAL_DATA, value);
                }
            }
        }


        if (needDefaultCode && ValidationHelper.isNullOrEmpty(property.getCategoryCode())) {
            property.setCategoryCode("T");
        }
        Property propertyEnt = property.toEntity(session);

        if (propertyEnt != null) {
            if (propertyCity != null) {
                propertyEnt.setCity(propertyCity);
            } else if (propertyEnt.getCity() != null && propertyEnt.getCity().getDescription() == null) {
                propertyEnt.setCity(null);
            }
            List<CadastralData> cList = handleCadastralData(propertyEnt, eElement, session);
            if (ValidationHelper.isNullOrEmpty(cList)) {
                propertyEnt.setCadastralData(cList);
            } else {
                Property propByCD = RealEstateHelper.getExistingPropertyByCD(cList, session);
                if (propByCD != null && propByCD.getCity() != null && propByCD.getCity().getDescription() == null) {
                    propByCD.setCity(null);
                }
                if (!ValidationHelper.isNullOrEmpty(propByCD)) {
                    if(ValidationHelper.isNullOrEmpty(propByCD.getExclusedArea())) {
                        propByCD.setExclusedArea(propertyEnt.getExclusedArea());
                    }
                    if (RealEstateHelper.propertyChanged(propByCD, propertyEnt)) {
                        propByCD.setModified(true);
                        ConnectionManager.save(propByCD, true, session);
                        propertyEnt.setCadastralData(cList);
                    } else {
                        propertyEnt = propByCD;
                    }
                } else {
                    propertyEnt.setCadastralData(cList);
                }
            }

            if (RealEstateType.LAND.getId().equals(propertyEnt.getType()) && !ValidationHelper.isNullOrEmpty(propertyEnt.getSubs())) {
                CadastralCategory categoryofCodeR = DaoManager.get(CadastralCategory.class, new Criterion[]{
                        Restrictions.eq("code", "R"),
                });
                propertyEnt.setCategory(categoryofCodeR);
            }
            ConnectionManager.save(propertyEnt, true, session);
            if (ValidationHelper.isNullOrEmpty(request)) {
                if (!ValidationHelper.isNullOrEmpty(property.getEstimateOMI())) {
                    EstimateOMIHistory history = new EstimateOMIHistory();

                    history.setEstimateOMI(property.getEstimateOMI());
                    history.setPropertyAssessmentDate(
                            property.getPropertyAssessmentDate() == null
                                    ? new Date() : property.getPropertyAssessmentDate());
                    history.setProperty(propertyEnt);

                    ConnectionManager.save(history, true, session);
                }
            } else if (!ValidationHelper.isNullOrEmpty(request)) {
                if (ValidationHelper.isNullOrEmpty(propertyEnt.getRequestList())) {
                    propertyEnt.setRequestList(new LinkedList<>());
                }
                if (propertyEnt.getRequestList().stream().noneMatch(r -> r.getId().equals(request.getId()))) {
                    propertyEnt.getRequestList().add(request);
                    ConnectionManager.save(propertyEnt, true, session);
                }
            }
            return propertyEnt;
        }

        return null;
    }

    private static List<CadastralData> handleCadastralData(Property property, Element eElement, Session session)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        NodeList nList = eElement.getElementsByTagName("IdentificativoDefinitivo");
        if (nList == null || nList.getLength() == 0) {
            ConnectionManager.save(property, true, session);
            return null;
        }
        List<CadastralData> cList = new LinkedList<>();

        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElem = (Element) nNode;

                CadastralDataXMLWrapper data = new CadastralDataXMLWrapper();

                for (CadastralDataXMLElements elment : CadastralDataXMLElements.values()) {
                    String value = eElem.getAttribute(elment.getElement());
                    if(elment.equals(CadastralDataXMLElements.PARTICLE) &&
                    !ValidationHelper.isNullOrEmpty(value)){
                        String particellaDenom = eElem.getAttribute("ParticellaDenom");
                        if(!ValidationHelper.isNullOrEmpty(particellaDenom)){
                            value = value + "/" + particellaDenom;
                        }
                    }
                    data.setField(elment, value);
                }
                data.setPropertyTypeId(property.getType());
                List<CadastralData> dbData = data.entityFromDB(session);
                if (!ValidationHelper.isNullOrEmpty(dbData)) {
                    cList.add(dbData.get(0));
                } else {
                    CadastralData dataNew = data.toEntity();
                    ConnectionManager.save(dataNew, true, session);
                    cList.add(dataNew);
                }
            }
        }

        return cList;
    }

    private static Subject handleSubject(Node nNode, Session session)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (nNode == null || Node.ELEMENT_NODE != nNode.getNodeType()) {
            return null;
        }
        SubjectXMLWrapper subject = new SubjectXMLWrapper();
        Element eElement = (Element) nNode;

        for (SubjectXMLElements element : SubjectXMLElements.values()) {
            String value = getValueFromXML(eElement, element.getElement());
            subject.setField(element, value);
        }
        Subject subjectEnt = subject.loadByFiscalCode(session);

        if (subjectEnt == null) {
            subjectEnt = subject.toEntity();
            if (subjectEnt != null) {
                ConnectionManager.save(subjectEnt, true, session);
            }
        }

        if (subjectEnt.isNew()) {
            ConnectionManager.save(subjectEnt, true, session);
        }

        return subjectEnt;
    }

    /**
     * @param eElement - XML tag
     * @param element  - XML tag's name
     * @return value from XML tag
     * @author Vlad Strunenko
     * <p>
     * <pre>
     * Extract value from tag where parameter specialFlow is false
     *         </pre>
     */
    private static String getValueFromXML(Element eElement, String element) {
        StringJoiner result = new StringJoiner(" ");

        if (!ValidationHelper.isNullOrEmpty(element)) {
            String[] elements = element.split(" ");

            for (String elem : elements) {
                String[] e = elem.split("\\.");
                String tag = null;
                String attribute = null;

                if (e.length > 0) {
                    tag = e[0];
                }

                if (e.length > 1) {
                    attribute = e[1];
                }

                if (tag != null && attribute == null) {
                    Node n = eElement.getElementsByTagName(tag).item(0);

                    if (n == null && tag.equalsIgnoreCase(eElement.getNodeName())) {
                        n = eElement;
                    }

                    if (n != null) {
                        result.add(n.getTextContent());
                    }
                } else if (tag != null) {
                    Element n = null;

                    if (tag.equalsIgnoreCase(eElement.getNodeName())) {
                        n = eElement;
                    }

                    if (n == null && eElement.getParentNode() != null && tag.equalsIgnoreCase(eElement.getParentNode().getNodeName())) {
                        n = (Element) eElement.getParentNode();
                    } else if (n == null) {
                        n = (Element) (eElement.getElementsByTagName(tag).item(0));
                    }

                    if (n != null) {
                        result.add(n.getAttribute(attribute));
                    }
                }
            }
        }

        return result.toString();
    }

    private static String handleSpecialTags(Element eElement, CommunicationXMLElements element) {
        String value = null;

        switch (element) {
            case FORMALITY_TYPE:
                value = "C";
                break;
            default:
                break;
        }

        return value;
    }

    private static String handleSpecialTags(Element eElement, TagsForPDFXMLElements element, Session session)
            throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        String value = "";

        switch (element) {
            case PROVINCE:
                String name = getValueFromXML(eElement, element.getElementXML());

                value = ConnectionManager.loadField(LandChargesRegistry.class, "province.description",
                        String.class, new CriteriaAlias[]{
                                new CriteriaAlias("provinces", "province", JoinType.INNER_JOIN)
                        }, new Criterion[]{
                                Restrictions.eq("name", name)
                        }, session).stream().collect(Collectors.joining(", "));

                break;

            case COMMUNICATON_DATE:
                value = getValueFromXML(eElement, element.getElementXML());
                if (!ValidationHelper.isNullOrEmpty(value)) {
                    Date date = DateTimeHelper.fromXMLString(value);
                    value = DateTimeHelper.toString(date);
                }
                break;

            case DOCUMENT_CREATE_DATE:
                value = DateTimeHelper.toString(new Date());
                break;

            case DOCUMENT_CREATE_TIME:
                value = DateTimeHelper.ToStringTimeWithSeconds(new Date());
                break;

            case DATE_INSPECTION:
                String dateStr = getValueFromXML(eElement, element.getElementXML());
                if (!ValidationHelper.isNullOrEmpty(dateStr)) {
                    Date date = DateTimeHelper.fromXMLString(dateStr);
                    value = DateTimeHelper.toString(date);
                }
                break;

            case APPLICANT_DATA:
                value = "PGNDNC";
                break;

            case SUBJECT_ROW: {
                NodeList nods = eElement.getElementsByTagName("DatiRichiesta");
                if (nods.getLength() > 0) {
                    nods = ((Element) nods.item(0)).getElementsByTagName("SoggettoF");
                    if (nods.getLength() > 0) {
                        value = PrintPDFHelper.readWorkingListFile("subjectRowP", "SyntheticFormality");
                    } else {
                        value = PrintPDFHelper.readWorkingListFile("subjectRowL", "SyntheticFormality");
                    }
                }
            }
            break;

            case SUBJECT_TABLE_C: {
                NodeList nods = eElement.getElementsByTagName("SoggettoPF");
                if (nods.getLength() > 0) {
                    value = PrintPDFHelper.readWorkingListFile("subjectTableP", "Cadastral");
                } else {
                    nods = eElement.getElementsByTagName("SoggettoPNF");
                    if (nods.getLength() > 0) {
                        value = PrintPDFHelper.readWorkingListFile("subjectTableL", "Cadastral");
                    } else {
                        value = PrintPDFHelper.readWorkingListFile("subjectTable", "Cadastral");
                    }
                }
            }
            break;

            case SUBJECT_FISCAL_CODE:
            case SUBJECT_SURNAME:
            case SUBJECT_NAME: {
                NodeList nods = eElement.getElementsByTagName("DatiRichiesta");

                if (nods.getLength() > 0) {
                    value = getValueFromXML((Element) nods.item(0), element.getElementXML());
                }
            }
            break;

            case SUBJECT_BIRTH_DATE: {
                NodeList nods = eElement.getElementsByTagName("DatiRichiesta");

                if (nods.getLength() > 0) {
                    String dateString = getValueFromXML((Element) nods.item(0), element.getElementXML());
                    if (dateString.endsWith("0000")) {
                        value = replaceDateZerosToDash(dateString);
                    } else if (dateString.length() == 4) {           //length of year
                        value = dateString;
                    } else {
                        Date dateDate = DateTimeHelper.fromXMLString(dateString);
                        value = DateTimeHelper.toString(dateDate);
                    }
                }
            }
            break;

            case TYPE_DISPLAYED_FORMALITIES:
                if ("E E E".equalsIgnoreCase(getValueFromXML(eElement, element.getElementXML()))) {
                    value = "Tutte";
                }
                break;

            case RESTRICTIONS:
                if ("NO NO".equalsIgnoreCase(getValueFromXML(eElement, element.getElementXML()))) {
                    value = "Tutte";
                }
                break;

            case PERIOD_FROM_COMPUTERIZED: {
                String tempValue = handleSyntheticListDates(eElement, "DataDal", false);
                if (!ValidationHelper.isNullOrEmpty(tempValue)) {
                    Date tempDate = DateTimeHelper.fromXMLString(tempValue);
                    value = DateTimeHelper.toString(tempDate);
                }
            }
            break;

            case PERIOD_TO_COMPUTERIZED: {
                String tempValue = handleSyntheticListDates(eElement, "DataAl", false);
                if (!ValidationHelper.isNullOrEmpty(tempValue)) {
                    Date tempDate = DateTimeHelper.fromXMLString(tempValue);
                    value = DateTimeHelper.toString(tempDate);
                }
            }
            break;

            case VOLUMES_REPERTORIES: {
                value = getValueFromXML(eElement, element.getElementXML());
                value = value.replaceAll("\\\\", "&#92;");
            }
            break;

            case PERIOD_FROM_VALIDATED: {
                String dat = handleSyntheticListDates(eElement, "DataDal", true);
                if (!ValidationHelper.isNullOrEmpty(dat)) {
                    if (dat.length() == 8) {
                        value = String.format("%s/%s/%s", dat.substring(6, 8), dat.substring(4, 6), dat.substring(0, 4));
                    } else {
                        value = dat;
                    }
                }
            }
            break;

            case PERIOD_TO_VALIDATED: {
                String dat = handleSyntheticListDates(eElement, "DataAl", true);
                if (!ValidationHelper.isNullOrEmpty(dat)) {
                    if (dat.length() == 8) {
                        value = String.format("%s/%s/%s", dat.substring(6, 8), dat.substring(4, 6), dat.substring(0, 4));
                    } else {
                        value = dat;
                    }
                }
            }
            break;

            case SUBJECT_TABLE: {
                NodeList nods = eElement.getElementsByTagName("SoggettiImmobiliSelezionati");

                if (nods.getLength() > 0) {
                    value = parseSubjectTable((Element) nods.item(0));
                }
            }
            break;

            case SYNTHETIC_HEADER:
                value = parseSyntheticHeader(eElement, "Sezione1");
                break;

            case SYNTHETIC_TABLE:
                value = parseSyntheticTable(eElement);
                break;

            case SITUATION_TO_C:
            case VISURA_TITLE_DATE_C:
            case SUBJECT_BIRTHDATE_C:
                value = DateTimeHelper.toString(DateTimeHelper.fromXMLString(
                        getValueFromXML(eElement, element.getElementXML())));
                break;

            case VISURA_TITLE_TIME_C:
                value = LocalTime.parse(getValueFromXML(eElement, element.getElementXML()),
                        DateTimeFormatter.ofPattern("HHmmss")).toString();
                break;

            case DATA_REQUEST_PROVINCE_C:
                Province province = ConnectionManager.get(Province.class, new Criterion[]{
                        Restrictions.eq("code", getValueFromXML(eElement, element.getElementXML()))
                }, session);
                if (province != null) {
                    value = province.getDescription();
                }
                break;

            case PROPERTY_TABLES:
                value = parsePropertyTables(eElement);
                break;

            case SUMMARY_ROWS:
                value = getSummaryRows(eElement);
                break;

            case DATA_REQUEST_COMUNE_C:
            case DATA_REQUEST_CODICE_COMUNE_C:
            case DATA_REQUEST_FOGLIO_C:
            case DATA_REQUEST_PARTICELLANUM_C:
            case DATA_REQUEST_SUBALTERNO_C:
                value = getValueFromXML(eElement, element.getElementXML());
                value = value.replaceAll("\\\\", "&#92;");
                break;

            case PROPERTY_INSTAT_TABLE_C:
                NodeList nList = eElement.getElementsByTagName("IntestazioneAttuale");
                value = parseInstatTableC(nList);

                break;

            case HISTORY_ROWS:
                value = parseHistoryTables(eElement);
                break;

            case PADRI_ROWS:
                value = parsePadriRows(eElement);
                break;

            default:
                break;
        }

        return value;
    }

    private static String getSummaryRows(Element eElement) {
        return addAnnotationLand(eElement.getElementsByTagName("ImmobileTerreni"), true)
                + addAnnotationLand(eElement.getElementsByTagName("ImmobileTerreniS"), true)
                + addAnnotationBuilding(eElement.getElementsByTagName("ImmobileFabbricati"), true);
    }

    private static String parsePropertyTables(Element eElement) {
        StringJoiner result = new StringJoiner("");

        NodeList nList = eElement.getElementsByTagName("GruppoUnitaImmobiliari");

        for (int temp = 0; temp < nList.getLength(); temp++) {
            String html = PrintPDFHelper.readWorkingListFile("propertyTables", "Cadastral");
            Element nNode = (Element) nList.item(temp);

            String building = "";

            if (nNode.getElementsByTagName(TagsForPDFXMLElements.PROPERTY_BUILDING_TABLE.getElementXML()).getLength() > 0) {
                building = parsePropertyTable(nNode, TagsForPDFXMLElements.PROPERTY_BUILDING_TABLE);
            }

            if (!building.isEmpty() && temp + 1 != nList.getLength()) {
                building = String.format("%s %s", building, "<pd4ml-page-break />");
            }

            html = html.replaceAll(TagsForPDFXMLElements.PROPERTY_BUILDING_TABLE.getElementHTML(), building);

            String land = "";

            if (nNode.getElementsByTagName(TagsForPDFXMLElements.PROPERTY_LAND_TABLE.getElementXML()).getLength() > 0) {
                land = parsePropertyTable(nNode, TagsForPDFXMLElements.PROPERTY_LAND_TABLE);
            }

            if (!land.isEmpty() && temp + 1 != nList.getLength()) {
                land = String.format("%s %s", land, "<pd4ml-page-break />");
            }

            html = html.replaceAll(TagsForPDFXMLElements.PROPERTY_LAND_TABLE.getElementHTML(), land);

            result.add(html);
        }

        if (result.length() == 0) {
            NodeList parent = eElement.getElementsByTagName("StoriaImmobileFabbricati");
            if (parent.getLength() > 0) {
                Element pNode = (Element) parent.item(0);

                nList = pNode.getElementsByTagName("ImmobileFabbricati");
                for (int temp = 0; temp < nList.getLength(); temp++) {
                    String html = PrintPDFHelper.readWorkingListFile("propertyTables", "Cadastral");
                    Element nNode = (Element) nList.item(temp);
                    String building = "";
                    building = parsePropertyTable(nNode, TagsForPDFXMLElements.PROPERTY_BUILDING_TABLE);
                    if (!building.isEmpty() && temp + 1 != nList.getLength()) {
                        building = String.format("%s %s", building, "<pd4ml-page-break />");
                    }

                    html = html.replaceAll(TagsForPDFXMLElements.PROPERTY_BUILDING_TABLE.getElementHTML(), building);

                    String land = "";

                    if (nNode.getElementsByTagName(TagsForPDFXMLElements.PROPERTY_LAND_TABLE.getElementXML()).getLength() > 0) {
                        land = parsePropertyTable(nNode, TagsForPDFXMLElements.PROPERTY_LAND_TABLE);
                    }

                    if (!land.isEmpty() && temp + 1 != nList.getLength()) {
                        land = String.format("%s %s", land, "<pd4ml-page-break />");
                    }

                    html = html.replaceAll(TagsForPDFXMLElements.PROPERTY_LAND_TABLE.getElementHTML(), land);

                    result.add(html);
                }
            }
        }

        for (int temp = 0; temp < nList.getLength(); temp++) {
            String html = PrintPDFHelper.readWorkingListFile("propertyTablesAlt", "Cadastral");
            Element nNode = (Element) nList.item(temp);

            String building = "";

            if (nNode.getElementsByTagName(TagsForPDFXMLElements.PROPERTY_BUILDING_TABLE_ALT.getElementXML()).getLength() > 0) {
                building = parsePropertyTable(nNode, TagsForPDFXMLElements.PROPERTY_BUILDING_TABLE_ALT);
            }

            if (!building.isEmpty() && temp + 1 != nList.getLength()) {
                building = String.format("%s %s", building, "<pd4ml-page-break />");
            }

            html = html.replaceAll(TagsForPDFXMLElements.PROPERTY_BUILDING_TABLE_ALT.getElementHTML(), building);

            String land = "";

            if (nNode.getElementsByTagName(TagsForPDFXMLElements.PROPERTY_LAND_TABLE_ALT.getElementXML()).getLength() > 0) {
                land = parsePropertyTable(nNode, TagsForPDFXMLElements.PROPERTY_LAND_TABLE_ALT);
            }

            if (!land.isEmpty() && temp + 1 != nList.getLength()) {
                land = String.format("%s %s", land, "<pd4ml-page-break />");
            }

            html = html.replaceAll(TagsForPDFXMLElements.PROPERTY_LAND_TABLE_ALT.getElementHTML(), land);

            result.add(html);
        }

        if (result.length() == 0) {
            NodeList parent = eElement.getElementsByTagName("StoriaImmobileFabbricati");
            if (parent.getLength() > 0) {
                Element pNode = (Element) parent.item(0);

                nList = pNode.getElementsByTagName("ImmobileFabbricati");
                for (int temp = 0; temp < nList.getLength(); temp++) {
                    String html = PrintPDFHelper.readWorkingListFile("propertyTablesAlt", "Cadastral");
                    Element nNode = (Element) nList.item(temp);
                    String building = "";
                    building = parsePropertyTable(nNode, TagsForPDFXMLElements.PROPERTY_BUILDING_TABLE_ALT);
                    if (!building.isEmpty() && temp + 1 != nList.getLength()) {
                        building = String.format("%s %s", building, "<pd4ml-page-break />");
                    }

                    html = html.replaceAll(TagsForPDFXMLElements.PROPERTY_BUILDING_TABLE_ALT.getElementHTML(), building);

                    String land = "";

                    if (nNode.getElementsByTagName(TagsForPDFXMLElements.PROPERTY_LAND_TABLE_ALT.getElementXML()).getLength() > 0) {
                        land = parsePropertyTable(nNode, TagsForPDFXMLElements.PROPERTY_LAND_TABLE_ALT);
                    }

                    if (!land.isEmpty() && temp + 1 != nList.getLength()) {
                        land = String.format("%s %s", land, "<pd4ml-page-break />");
                    }

                    html = html.replaceAll(TagsForPDFXMLElements.PROPERTY_LAND_TABLE_ALT.getElementHTML(), land);

                    result.add(html);
                }
            }
        }

        return result.toString();
    }

    private static String parseHistoryTables(Element eElement) {
        StringJoiner result = new StringJoiner("");

        NodeList parent = eElement.getElementsByTagName("StoriaIntestazione");

        if (parent.getLength() > 0) {
            Element pNode = (Element) parent.item(0);

            NodeList nList = pNode.getElementsByTagName("MutazioneSoggettiva");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                String html = PrintPDFHelper.readWorkingListFile("propertyHistoryTables", "Cadastral");
                Element nNode = (Element) nList.item(temp);
                String history = "";
                history = parseHistoryTable(nNode, TagsForPDFXMLElements.PROPERTY_HISTORY_TABLE);

                if (!history.isEmpty() && temp + 1 != nList.getLength()) {
                    history = String.format("%s %s", history, "<pd4ml-page-break />");
                }

                html = html.replaceAll(TagsForPDFXMLElements.PROPERTY_HISTORY_TABLE.getElementHTML(), history);
                result.add(html);
            }
        }

        return result.toString();
    }

    private static String parsePadriRows(Element eElement) {
        StringJoiner result = new StringJoiner("");
        NodeList parent = eElement.getElementsByTagName("Padri");

        if (parent.getLength() > 0) {
            Element pNode = (Element) parent.item(0);

            NodeList nList = pNode.getElementsByTagName("IdentificativoDefinitivo");
            String value = "<p class=\"header_text left_text\">" + getValueFromXML(pNode, "Padri.Descrizione") + "</p>";
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Element nNode = (Element) nList.item(temp);
                value += "<p style=\"text-align: left; font-size: 9px !important;\">" +
                        "- foglio" + getValueFromXML(nNode, "IdentificativoDefinitivo.Foglio") +
                        " particella " + getValueFromXML(nNode, "IdentificativoDefinitivo.ParticellaNum") +
                        " subalterno " + getValueFromXML(nNode, "IdentificativoDefinitivo.Subalterno")
                        + "</p>";
            }
            result.add(value);
        }

        return result.toString();
    }

    private static String parsePropertyTable(Element eElement, TagsForPDFXMLElements elementTag) {
        String fileName = "";

        switch (elementTag) {
            case PROPERTY_BUILDING_TABLE:

            case PROPERTY_BUILDING_TABLE_ALT:
                fileName = "propertyBuildingTable";
                break;

            case PROPERTY_LAND_TABLE:

            case PROPERTY_LAND_TABLE_ALT:
                fileName = "propertyLandTable";
                break;

            default:
                break;

        }

        String html = PrintPDFHelper.readWorkingListFile(fileName, "Cadastral");

        for (PropertyForPDFXMLElements element : PropertyForPDFXMLElements.values()) {
            if (html.contains(element.getElementHTML())) {
                String value;

                if (element.isSpecialFlow()) {
                    value = handleSpecialTags(eElement, element, elementTag);
                } else {
                    value = getValueFromXML(eElement, element.getElementXML());
                }

                html = html.replaceAll(element.getElementHTML(), value);
            }
        }

        return html;
    }

    private static String parseHistoryTable(Element eElement, TagsForPDFXMLElements elementTag) {
        String fileName = "propertyHistoryTable";

        String html = PrintPDFHelper.readWorkingListFile(fileName, "Cadastral");

        for (PropertyForPDFXMLElements element : PropertyForPDFXMLElements.values()) {
            if (html.contains(element.getElementHTML())) {
                String value;

                if (element.isSpecialFlow()) {
                    value = handleSpecialTags(eElement, element, elementTag);
                } else {
                    value = getValueFromXML(eElement, element.getElementXML());
                }
                html = html.replaceAll(element.getElementHTML(), value);
            }
        }


        return html;
    }

    private static String handleSpecialTags(Element eElement,
                                            PropertyForPDFXMLElements element, TagsForPDFXMLElements elementTag) {
        String value = "";
        Element eElementTemp = null;
        NodeList nList = null;
        TagsForPDFXMLElements selectedProperty = null;

        switch (elementTag) {
            case PROPERTY_BUILDING_TABLE:
                if (eElement.getTagName().equalsIgnoreCase("ImmobileFabbricati")) {
                    eElementTemp = eElement;
                } else
                    nList = eElement.getElementsByTagName("ImmobileFabbricati");
                selectedProperty = elementTag;
                break;

            case PROPERTY_BUILDING_TABLE_ALT:
                if (eElement.getTagName().equalsIgnoreCase("ImmobileFabbricatiS")) {
                    eElementTemp = eElement;
                } else
                    nList = eElement.getElementsByTagName("ImmobileFabbricatiS");
                selectedProperty = elementTag;
                break;

            case PROPERTY_LAND_TABLE:
                nList = eElement.getElementsByTagName("ImmobileTerreni");
                selectedProperty = elementTag;
                break;

            case PROPERTY_LAND_TABLE_ALT:
                nList = eElement.getElementsByTagName("ImmobileTerreniS");
                selectedProperty = elementTag;
                break;

            case PROPERTY_HISTORY_TABLE:
                if (eElement.getTagName().equalsIgnoreCase("MutazioneSoggettiva")) {
                    eElementTemp = eElement;
                } else
                    nList = eElement.getElementsByTagName("MutazioneSoggettiva");
                selectedProperty = elementTag;
                break;

            default:
                break;

        }

        if (nList != null && nList.getLength() > 0) {
            eElementTemp = (Element) nList.item(0);
        }

        switch (element) {
            case PROPERTY_BUILDING_NUMBER:
            case PROPERTY_HISTORY_DESCRIPTION:
                if (eElementTemp != null) {
                    value = getValueFromXML(eElementTemp, element.getElementXML());
                }

                break;

            case PROPERTY_BUILDING_DESCRIPTION:
                if (eElementTemp != null) {
                    if (eElement.getTagName().equalsIgnoreCase("ImmobileFabbricati")) {
                        value = getValueFromXML(eElementTemp, "Situazione");
                    } else {
                        value = getValueFromXML(eElementTemp, element.getElementXML());
                    }
                }

                break;

            case PROPERTY_ANNOTATION_ROW:
                if (!eElement.getTagName().equalsIgnoreCase("ImmobileFabbricati") &&
                        !eElement.getTagName().equalsIgnoreCase("ImmobileFabbricatiS"))
                    value = parsePropertyAnnotationRow(nList);
                   /* value = parsePropertyAnnotationRow(eElement);
                } else*/

                break;

            case PROPERTY_ANNOTATION_TABLE:
                if (eElement.getTagName().equalsIgnoreCase("ImmobileFabbricati") ||
                        eElement.getTagName().equalsIgnoreCase("ImmobileFabbricatiS")) {
                    value = parsePropertyAnnotationTable(eElement, selectedProperty);
                } else
                    value = parsePropertyAnnotationTable(nList, selectedProperty);
                break;

            case PROPERTY_BUILDING_ROWS:
            case PROPERTY_LAND_ROWS:
                if (eElement.getTagName().equalsIgnoreCase("ImmobileFabbricati") ||
                        eElement.getTagName().equalsIgnoreCase("ImmobileFabbricatiS")) {
                    value = parsePropertyRows(eElement, elementTag);
                } else
                    value = parsePropertyRows(nList, elementTag);
                break;

            case PROPERTY_INSTAT_TABLE:
                if (!eElement.getTagName().equalsIgnoreCase("ImmobileFabbricati") &&
                        !eElement.getTagName().equalsIgnoreCase("ImmobileFabbricatiS")) {
                    value = parseInstatTable(nList);
                }

                break;
            case EXTRA_COLUMNS:
                if (!eElement.getTagName().equalsIgnoreCase("ImmobileFabbricati") &&
                        !eElement.getTagName().equalsIgnoreCase("ImmobileFabbricatiS")) {
                    value = "<td><p>Indirizzo<br/>Dati derivanti da</p></td><td><p>Dati ulteriori</p></td>";
                }
                break;

            case EXTRA_COLUMN:
                if (!eElement.getTagName().equalsIgnoreCase("ImmobileFabbricati") &&
                        !eElement.getTagName().equalsIgnoreCase("ImmobileFabbricatiS")) {
                    value = "<td colspan=\"2\"><p class=\"header_text\">ALTRE INFORMAZIONI</p></td>";
                } else
                    value = "<td colspan=\"2\"><p class=\"header_text\">DATI DERIVANTI DA</p></td>";
                break;

            case PROPERTY_BUILDING_MAPS:
                String maps = "Codice Comune ";
                if (eElement.getParentNode().getNodeName().equalsIgnoreCase("StoriaImmobileFabbricati")) {
                    Node parent = eElement.getParentNode().getParentNode();
                    if (parent != null) {
                        Element pElement = (Element) parent;
                        maps += getValueFromXML(pElement, "DatiRichiesta.CodiceComune");
                    }

                    NodeList mList = eElement.getElementsByTagName("MappaliCorrelati");
                    if (mList.getLength() > 0) {
                        Element mElement = (Element) mList.item(0);
                        maps += "- Sezione ";
                        maps += getValueFromXML(mElement, "IdentificativoCorrelato.SezCensuaria");
                        maps += "- Foglio ";
                        maps += getValueFromXML(mElement, "IdentificativoCorrelato.Foglio");
                        maps += "- Particella ";
                        maps += getValueFromXML(mElement, "IdentificativoCorrelato.ParticellaNum");
                        value = "<p>Mappali Terreni Correlati</p><p class=\"header_text\">" + maps + "</p>";
                    }
                }
                break;

            case PROPERTY_HISTORY_ROWS:
                if (eElement.getTagName().equalsIgnoreCase("MutazioneSoggettiva")) {
                    value = parseHistoryRows(eElement, elementTag);
                }
                break;
            default:
                break;
        }

        return value;
    }

    private static String parseInstatTable(NodeList nList) {
        Element eElementTemp = null;

        if (nList != null && nList.getLength() > 0) {
            eElementTemp = (Element) nList.item(0);
        }

        String html = PrintPDFHelper.readWorkingListFile("propertyIntestateTable", "Cadastral");

        for (PropertyInstatTableForPDFXMLElements element : PropertyInstatTableForPDFXMLElements.values()) {
            if (html.contains(element.getElementHTML())) {
                String value = "";

                if (element.isSpecialFlow()) {
                    value = handleSpecialTags(eElementTemp, element);
                } else {
                    value = getValueFromXML(eElementTemp, element.getElementXML());
                }

                html = html.replaceAll(element.getElementHTML(), value);
            }
        }

        return html;
    }

    private static String parseInstatTableC(NodeList nList) {
        Element eElementTemp = null;

        if (nList != null && nList.getLength() > 0) {
            eElementTemp = (Element) nList.item(0);
        }

        String html = PrintPDFHelper.readWorkingListFile("propertyIntestateTableC", "Cadastral");

        for (PropertyInstatTableForPDFXMLElements element : PropertyInstatTableForPDFXMLElements.values()) {
            if (html.contains(element.getElementHTML())) {
                String value = "";

                if (element.isSpecialFlow()) {
                    value = handleSpecialTags(eElementTemp, element);
                } else {
                    value = getValueFromXML(eElementTemp, element.getElementXML());
                }

                html = html.replaceAll(element.getElementHTML(), value);
            }
        }

        return html;
    }

//	private static String parseInstatTable(Element eElementTemp) {
//
//		String html = PrintPDFHelper.readWorkingListFile("propertyIntestateTable", "Cadastral");
//
//		for (PropertyInstatTableForPDFXMLElements element : PropertyInstatTableForPDFXMLElements.values()) {
//			if (html.contains(element.getElementHTML())) {
//				String value = "";
//
//				if (element.isSpecialFlow()) {
//					value = handleSpecialTags(eElementTemp, element);
//				} else {
//					value = getValueFromXML(eElementTemp, element.getElementXML());
//				}
//
//				html = html.replaceAll(element.getElementHTML(), value);
//			}
//		}
//		return html;
//	}

    private static String handleSpecialTags(Element eElementTemp, PropertyInstatTableForPDFXMLElements element) {
        String value = "";

        switch (element) {
            case PROPERTY_RESULT_DATA:
                value = getValueFromXML((Element) eElementTemp.getParentNode(), element.getElementXML());
                if (!ValidationHelper.isNullOrEmpty(value)) {
                    value = "<td style=\"width: 25%;\" colspan=\"2\">" +
                            "<p style=\"text-align: left;\">DATI DERIVANTI DA</p>" +
                            "</td><td colspan=\"3\"><p style=\"text-align: left;\">" + value + "</p></td>";
                }
                break;

            case PROPERTY_INSTAT_ROWS:
                Element eElement = (Element) eElementTemp.getParentNode();
                value = parseInstatRows(eElement);
                break;

            default:
                break;
        }

        return value;
    }

    private static String parseInstatRows(Element eElement) {

        StringBuffer result = new StringBuffer();
        NodeList pList = eElement.getElementsByTagName("IntestazioneAttuale");
        NodeList nList = null;
        if (pList.getLength() > 0) {
            Element pNode = (Element) pList.item(0);
            nList = pNode.getElementsByTagName("Intestato");
        } else
            nList = eElement.getElementsByTagName("Intestato");

        for (int temp = 0; temp < nList.getLength(); temp++) {
            String html = PrintPDFHelper.readWorkingListFile("propertyIntestateRows", "Cadastral");
            Element nNode = (Element) nList.item(temp);

            for (PropertyInstatRowForPDFXMLElements element : PropertyInstatRowForPDFXMLElements.values()) {
                String value = "";
                if (element.isSpecialFlow()) {
                    switch (element) {
                        case PROPERTY_INSTAT_FISCAL_CODE:
                            value = getValueFromXML(nNode, element.getElementXML());
                            if (getValueFromXML(nNode, "Intestato.CFValidato").equals("1")) {
                                value += "*";
                            }
                            break;
                        case REAL_RIGHTS_QUOTE:
                            value = getValueFromXML(nNode, element.getElementXML());
                            if (!ValidationHelper.isNullOrEmpty(value)) {
                                value = "per " + value;
                            }
                            break;
                        default:
                            break;
                    }
                } else {
                    value = getValueFromXML(nNode, element.getElementXML());
                }
                html = html.replaceAll(element.getElementHTML(), value);
            }

            result.append(html);
        }

        return result.toString();
    }

    private static String parsePropertyRows(NodeList nList, TagsForPDFXMLElements elementTag) {
        StringBuffer result = new StringBuffer();

        String fileName = "";

        boolean parseCategory = false;

        switch (elementTag) {
            case PROPERTY_BUILDING_TABLE:
                fileName = "propertyBuildingRows";
                break;

            case PROPERTY_LAND_TABLE:
                fileName = "propertyLandRows";
                break;

            case PROPERTY_BUILDING_TABLE_ALT:
                parseCategory = true;
                fileName = "propertyBuildingRowsAlt";
                break;

            case PROPERTY_LAND_TABLE_ALT:
                fileName = "propertyLandRowsAlt";
                break;

            default:
                break;

        }

        if (nList != null) {
            for (int temp = 0; temp < nList.getLength(); temp++) {
                String html = PrintPDFHelper.readWorkingListFile(fileName, "Cadastral");
                Element nNode = (Element) nList.item(temp);

                for (PropertyRowsForPDFXMLElements element : PropertyRowsForPDFXMLElements.values()) {
                    if (html.contains(element.getElementHTML())) {
                        String value = "";

                        if (element.isSpecialFlow()) {
                            value = handleSpecialTags(nNode, element);
                        } else {
                            value = getValueFromXML(nNode,
                                    element.getElementXML());
                            if(parseCategory
                                    && element.equals(PropertyRowsForPDFXMLElements.PROPERTY_BUILDING_CATEGORY)){
                                if(value.contains(":")){
                                    value = value.split("\\:")[0].trim();
                                }
                            }
                        }

                        html = html.replaceAll(element.getElementHTML(), value);
                    }
                }

                result.append(html);
            }
        }

        return result.toString();
    }

    private static String parsePropertyRows(Element nNode, TagsForPDFXMLElements elementTag) {
        StringBuffer result = new StringBuffer();
        String fileName = "";

        switch (elementTag) {
            case PROPERTY_BUILDING_TABLE:
            case PROPERTY_BUILDING_TABLE_ALT:
                fileName = "propertyBuildingRows";
                break;

            case PROPERTY_LAND_TABLE:
            case PROPERTY_LAND_TABLE_ALT:
                fileName = "propertyLandRows";
                break;

            default:
                break;

        }
        if (nNode != null) {
            String html = PrintPDFHelper.readWorkingListFile(fileName, "Cadastral");

            for (PropertyRowsForPDFXMLElements element : PropertyRowsForPDFXMLElements.values()) {
                if (html.contains(element.getElementHTML())) {
                    String value = "";

                    if (element.isSpecialFlow()) {
                        value = handleSpecialTags(nNode, element);
                    } else {
                        value = getValueFromXML(nNode, element.getElementXML());
                    }

                    html = html.replaceAll(element.getElementHTML(), value);
                }
            }

            result.append(html);
        }

        return result.toString();
    }

    private static String parseHistoryRows(Element nNode, TagsForPDFXMLElements elementTag) {
        StringBuffer result = new StringBuffer();
        String fileName = "propertyHistoryRows";

        if (nNode != null) {
            String html = PrintPDFHelper.readWorkingListFile(fileName, "Cadastral");

            for (PropertyRowsForPDFXMLElements element : PropertyRowsForPDFXMLElements.values()) {
                if (html.contains(element.getElementHTML())) {
                    String value = "";

                    if (element.isSpecialFlow()) {
                        value = handleSpecialTags(nNode, element);
                    } else {
                        value = getValueFromXML(nNode, element.getElementXML());
                    }

                    html = html.replaceAll(element.getElementHTML(), value);
                }
            }

            result.append(html);
        }

        return result.toString();
    }

    private static String handleSpecialTags(Element nNode, PropertyRowsForPDFXMLElements element) {
        String value = "";
        StringBuffer result;
        switch (element) {
            case PROPERTY_BUILDING_ADDRESS:
                result = new StringBuffer();
                String str = "";
                if (!nNode.getParentNode().getNodeName().equalsIgnoreCase("StoriaImmobileFabbricati")) {
                    str = getValueFromXML(nNode, "IndirizzoImm.IndirizzoImm");
                    if (!ValidationHelper.isNullOrEmpty(str)) {
                        result.append(str);
                        result.append("<br/>");
                    }else if(nNode.getTagName().equalsIgnoreCase("ImmobileFabbricatiS")){
                        str = getValueFromXML(nNode, "IndirizzoImm");
                        if (!ValidationHelper.isNullOrEmpty(str)) {
                            result.append(str);
                            result.append("<br/>");
                        }
                    }
                    NodeList nList = nNode.getElementsByTagName("Piano");
                    if (nList.getLength() > 0) {
                        result.append(" piano: ");

                        for (int temp = 0; temp < nList.getLength(); temp++) {
                            Element eElement = (Element) nList.item(temp);

                            result.append(getValueFromXML(eElement, "Piano"));

                            if (temp < nList.getLength() - 1) {
                                result.append("-");
                            }
                        }
                    }
                }

                if (!nNode.getParentNode().getNodeName().equalsIgnoreCase("StoriaImmobileFabbricati")) {
                    str = getValueFromXML(nNode, "Interno");
                    if (!ValidationHelper.isNullOrEmpty(str)) {
                        result.append(" interno: ");
                        result.append(str);
                        result.append(" ");
                    }
                    str = getValueFromXML(nNode, "DatiIndirizzo.Scala");
                    if (!ValidationHelper.isNullOrEmpty(str)) {
                        result.append("scala: ");
                        result.append(str);
                        result.append("; ");
                    }
                }

                str = getValueFromXML(nNode, "DatiDerivantiDa");
                if (!ValidationHelper.isNullOrEmpty(str)) {
                    result.append(str);
                }else if(nNode.getTagName().equalsIgnoreCase("ImmobileFabbricatiS") ||
                        nNode.getTagName().equalsIgnoreCase("ImmobileTerreniS")){
                    str = getValueFromXML(nNode, "DatiDerivantiDa.Descrizione");
                    if (!ValidationHelper.isNullOrEmpty(str)) {
                        result.append(str);
                        result.append("<br/>");
                    }
                }

                value = result.toString();
                break;

            case PROPERTY_BUILDING_ANNOTATION:
                if (!nNode.getParentNode().getNodeName().equalsIgnoreCase("StoriaImmobileFabbricati")) {
                    String tempValue = getValueFromXML(nNode, element.getElementXML());
                    if (!ValidationHelper.isNullOrEmpty(tempValue)) {
                        value = "<td><p>Annotazione</p></td> ";
                    }
                }

                break;

            case PROPERTY_BUILDING_CONSISTENCY:
                String textMq = getValueFromXML(nNode, element.getElementXML());

                if (!ValidationHelper.isNullOrEmpty(textMq)) {
                    value = textMq.replaceAll("MQ", "m<sup>2</sup>");
                }
                break;

            case PROPERTY_BUILDING_INDRIZZO:

                result = new StringBuffer();
                if (nNode.getParentNode().getNodeName().equalsIgnoreCase("StoriaImmobileFabbricati")) {
                    str = getValueFromXML(nNode, "IndirizzoImm.IndirizzoImm");
                    if (!ValidationHelper.isNullOrEmpty(str)) {
                        result.append(str);
                    }

                    str = getValueFromXML(nNode, "NumeroCivico");
                    if (!ValidationHelper.isNullOrEmpty(str)) {
                        result.append(" n. " + str);
                    }

                    NodeList nList = nNode.getElementsByTagName("Piano");
                    if (nList.getLength() > 0) {
                        result.append(" piano: ");

                        for (int temp = 0; temp < nList.getLength(); temp++) {
                            Element eElement = (Element) nList.item(temp);

                            result.append(getValueFromXML(eElement, "Piano"));

                            if (temp < nList.getLength() - 1) {
                                result.append("-");
                            }
                        }
                    }
                    nList = nNode.getElementsByTagName("Interno");
                    if (nList.getLength() > 0) {
                        result.append(" interno: ");

                        for (int temp = 0; temp < nList.getLength(); temp++) {
                            Element eElement = (Element) nList.item(temp);

                            result.append(getValueFromXML(eElement, "Interno"));

                            if (temp < nList.getLength() - 1) {
                                result.append("-");
                            }
                        }
                    }
                    str = getValueFromXML(nNode, "DatiIndirizzo.Scala");
                    if (!ValidationHelper.isNullOrEmpty(str)) {
                        result.append(" scala: ");
                        result.append(str.trim());
                    }
                    result.append(";");
                    value = "<tr>"
                            + "<td colspan=\"3\">"
                            + "<p class=\"header_text left_text\">Indirizzo</p>"
                            + "</td>"
                            + "<td colspan=\"11\"><p class=\"left_text\">" + result.toString() + "</p></td>"
                            + "<tr>";
                }
                break;

            case PROPERTY_BUILDING_NOTIFICATION:
                String notifica = "";
                String partita = "";
                if (nNode.getParentNode().getNodeName().equalsIgnoreCase("StoriaImmobileFabbricati")) {
                    NodeList nList = nNode.getElementsByTagName("Notifica");
                    if (nList.getLength() > 0) {
                        Element eElement = (Element) nList.item(0);
                        notifica = getValueFromXML(eElement, "Notifica");
                    }
                    nList = nNode.getElementsByTagName("Partita");
                    if (nList.getLength() > 0) {
                        Element eElement = (Element) nList.item(0);
                        partita = getValueFromXML(eElement, "Partita");
                    }

                    String br = "";

                    if (!StringUtils.isBlank(notifica)) {
                        br = "<br/>";
                    }
                    if (!StringUtils.isBlank(notifica) || !StringUtils.isBlank(partita)) {
                        value = "<tr>"
                                + "<td colspan=\"8\"><p class=\"header_text left_text\">Notifica&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- " + notifica + "</p></td>"
                                + "<td colspan=\"1\"> " + br + "<p class=\"header_text left_text\">Partita</p></td>"
                                + "<td colspan=\"1\"><p>" + partita + "</p></td>"
                                + "<td>" + br + "<p class=\"header_text left_text\" >Mod.58</p></td>"
                                + "<td colspan=\"3\"></td>"
                                + "<tr>";
                    }

                }
                break;

            case PROPERTY_BUILDING_REMARKS:
                String remarks = "";
                if (nNode.getParentNode().getNodeName().equalsIgnoreCase("StoriaImmobileFabbricati")) {
                    NodeList nList = nNode.getElementsByTagName("Annotazione");
                    if (nList.getLength() > 0) {
                        Element eElement = (Element) nList.item(0);
                        remarks = getValueFromXML(eElement, "Annotazione");
                    }

                    value = "<tr>"
                            + "<td colspan=\"4\"><p class=\"header_text left_text\">Annotazioni</p></td>"
                            + "<td colspan=\"10\"><p class=\"left_text\">" + remarks + "</p></td>"
                            + "<tr>";
                }
                break;


            case PROPERTY_LAND_DOMINICALE: {
                NodeList nList1 = nNode.getElementsByTagName("DatiClassamentoT");
                result = new StringBuffer();
                for (int temp = 0; temp < nList1.getLength(); temp++) {
                    Node nNodeInner = nList1.item(temp);
                    String dom = getValueFromXML((Element) nNodeInner, element.getElementXML());
                    if (!ValidationHelper.isNullOrEmpty(dom)) {
                        result.append("Euro:&nbsp;");
                        result.append(dom);
                        result.append("<br/>");
                    }
                    dom = getValueFromXML((Element) nNodeInner, "DatiClassamentoT.RedditoDominicaleLire");
                    if (!ValidationHelper.isNullOrEmpty(dom)) {
                        result.append("L.&nbsp;");
                        result.append(dom);
                        result.append("<br/>");
                    }
                }

                value = result.toString();
                break;
            }
            case PROPERTY_LAND_DOMINICALE_ALT: {
                NodeList nList1 = nNode.getElementsByTagName("ClassamentoT");
                result = new StringBuffer();
                for (int temp = 0; temp < nList1.getLength(); temp++) {
                    Node nNodeInner = nList1.item(temp);
                    String dom = getValueFromXML((Element) nNodeInner, element.getElementXML());
                    if (!ValidationHelper.isNullOrEmpty(dom)) {
                        result.append("Euro:&nbsp;");
                        result.append(dom);
                        result.append("<br/>");
                    }
                    dom = getValueFromXML((Element) nNodeInner, "ClassamentoT.RedditoDominicaleLire");
                    if (!ValidationHelper.isNullOrEmpty(dom)) {
                        result.append("L.&nbsp;");
                        result.append(dom);
                        result.append("<br/>");
                    }
                }

                value = result.toString();
                break;
            }
            case PROPERTY_LAND_AGRICULTURAL: {
                NodeList nList1 = nNode.getElementsByTagName("DatiClassamentoT");
                result = new StringBuffer();
                for (int temp = 0; temp < nList1.getLength(); temp++) {
                    Node nNodeInner = nList1.item(temp);
                    String arg = getValueFromXML((Element) nNodeInner, element.getElementXML());
                    if (!ValidationHelper.isNullOrEmpty(arg)) {
                        result.append("Euro:&nbsp;");
                        result.append(arg);
                        result.append("<br/>");
                    }
                    arg = getValueFromXML((Element) nNodeInner, "DatiClassamentoT.RedditoAgrarioLire");
                    if (!ValidationHelper.isNullOrEmpty(arg)) {
                        result.append("L.&nbsp;");
                        result.append(arg);
                        result.append("<br/>");
                    }
                }
                value = result.toString();
                break;
            }
            case PROPERTY_LAND_AGRICULTURAL_ALT: {
                NodeList nList1 = nNode.getElementsByTagName("ClassamentoT");
                result = new StringBuffer();
                for (int temp = 0; temp < nList1.getLength(); temp++) {
                    Node nNodeInner = nList1.item(temp);
                    String arg = getValueFromXML((Element) nNodeInner, element.getElementXML());
                    if (!ValidationHelper.isNullOrEmpty(arg)) {
                        result.append("Euro:&nbsp;");
                        result.append(arg);
                        result.append("<br/>");
                    }
                    arg = getValueFromXML((Element) nNodeInner, "ClassamentoT.RedditoAgrarioLire");
                    if (!ValidationHelper.isNullOrEmpty(arg)) {
                        result.append("L.&nbsp;");
                        result.append(arg);
                        result.append("<br/>");
                    }
                }
                value = result.toString();
                break;
            }
            case PROPERTY_LAND_HA:
            case PROPERTY_LAND_CA:
            case PROPERTY_LAND_ARE: {
                NodeList nList1 = nNode.getElementsByTagName("DatiClassamentoT");
                result = new StringBuffer();
                for (int temp = 0; temp < nList1.getLength(); temp++) {
                    Node nNodeInner = nList1.item(temp);
                    String arg = getValueFromXML((Element) nNodeInner, element.getElementXML());
                    if (!ValidationHelper.isNullOrEmpty(arg)) {
                        result.append(arg);
                    }
                    result.append("<br/>");
                }
                value = result.toString();
                break;
            }
            case PROPERTY_BUILDING_TOTAL:
                result = new StringBuffer();
                String text = getValueFromXML(nNode, "SuperficieF.Totale");
                if (!ValidationHelper.isNullOrEmpty(text) && Long.parseLong(text) != 0L) {
                    result.append("Totale:&nbsp;");
                    result.append(text);
                    result.append("m<sup>2</sup><br/>");
                }
                text = getValueFromXML(nNode, "SuperficieF.TotaleE");
                if (!ValidationHelper.isNullOrEmpty(text) && Long.parseLong(text) != 0L) {
                    result.append("Totale escluse aree<br/>scoperte**:&nbsp;");
                    result.append(text);
                    result.append("m<sup>2</sup>");
                }
                value = result.toString();
                break;
            case PROPERTY_BUILDING_REVENUE:
                result = new StringBuffer();
                String t = getValueFromXML(nNode, "DatiClassamentoF.RenditaEuro");
                if (!ValidationHelper.isNullOrEmpty(t)) {
                    result.append("Euro&nbsp;");
                    result.append(t);
                    result.append("<br/>");
                }
                t = getValueFromXML(nNode, "DatiClassamentoF.RenditaLire");
                if (!ValidationHelper.isNullOrEmpty(t)) {
                    result.append("L.&nbsp;");
                    result.append(t);
                }
                value = result.toString();
                break;
            case PROPERTY_BUILDING_SEC_URBANA:
                value = getGraffatiStr(nNode, "IdentificativoDefinitivo.SezUrbana");
                break;
            case PROPERTY_BUILDING_SHEET:
                value = getGraffatiStr(nNode, "IdentificativoDefinitivo.Foglio");
                break;
            case PROPERTY_BUILDING_PARTICLE:
                value = getGraffatiStr(nNode, "IdentificativoDefinitivo.ParticellaNum");
                break;
            case PROPERTY_BUILDING_SUB:
                value = getGraffatiStr(nNode, "IdentificativoDefinitivo.Subalterno");
                break;
            case PROPERTY_HISTORY_NOMINATIVE:
            case PROPERTY_HISTORY_FISCAL_CODE:
            case PROPERTY_HISTORY_LAW_CODE:
            case PROPERTY_HISTORY_REAL_RIGHTS_DESCRIPTION:
            case PROPERTY_HISTORY_REAL_RIGHTS_QUOTE:
                NodeList nList = nNode.getElementsByTagName("Intestato");
                if (nList.getLength() > 0) {
                    Element eElement = (Element) nList.item(0);
                    value = getValueFromXML(eElement, element.getElementXML());
                }
                break;
            case PROPERTY_HISTORY_RESULT_DATA:
                value = getValueFromXML(nNode, "DatiDerivantiDaMutazSogg");
                break;
            default:
                break;

        }

        return value;
    }

    private static String getGraffatiStr(Element nNode, String elementName) {
        NodeList list = nNode.getElementsByTagName("IdentificativoDefinitivo");
        if (list.getLength() > 0) {
            List<String> strs = new LinkedList<>();
            for (int temp = 0; temp < list.getLength(); temp++) {
                Element eElement = (Element) list.item(temp);
                String val = getValueFromXML(eElement, elementName);
                if (!ValidationHelper.isNullOrEmpty(val)
                        && PropertyRowsForPDFXMLElements.PROPERTY_BUILDING_SUB.getElementXML().equals(elementName)) {
                    strs.add(val);
                } else if (!ValidationHelper.isNullOrEmpty(val) && !strs.contains(val)) {
                    strs.add(val);
                }
            }
            return strs.stream().collect(Collectors.joining("<br/>"));
        }
        return "";
    }

    private static String parsePropertyAnnotationRow(NodeList nList) {
        StringBuilder result = new StringBuilder();

        if (nList != null) {
            for (int temp = 0; temp < nList.getLength(); temp++) {
                String html = PrintPDFHelper.readWorkingListFile("propertyAnnotationRow", "Cadastral");
                Element nNode = (Element) nList.item(temp);

                for (PropertyAnnotationForPDFXMLElements element : PropertyAnnotationForPDFXMLElements.values()) {
                    if (html.contains(element.getElementHTML())) {
                        String value = getValueFromXML(nNode, element.getElementXML());
                        if (value.contains("$")) {
                            value = value.replaceAll("\\$", "");
                        }
                        html = html.replaceAll(element.getElementHTML(), value);
                        if(nNode.getTagName().equalsIgnoreCase("ImmobileFabbricati")){
                            if (ValidationHelper.isNullOrEmpty(value)
                                    && PropertyAnnotationForPDFXMLElements.PROPERTY_ANNOTATION.equals(element))
                                html = "";
                        }else if(nNode.getTagName().equalsIgnoreCase("ImmobileFabbricatiS")){
                            if (ValidationHelper.isNullOrEmpty(value)
                                    && PropertyAnnotationForPDFXMLElements.PROPERTY_ANNOTATION_ALT.equals(element))
                                html = "";
                        }
                    }
                }
                result.append(html);
            }
        }

        return result.toString();
    }

    private static String parsePropertyAnnotationRow(Element nNode) {
        StringBuilder result = new StringBuilder();
        if (nNode != null) {
            String html = PrintPDFHelper.readWorkingListFile("propertyAnnotationRow", "Cadastral");
            for (PropertyAnnotationForPDFXMLElements element : PropertyAnnotationForPDFXMLElements.values()) {
                if (html.contains(element.getElementHTML())) {
                    String value = getValueFromXML(nNode, element.getElementXML());
                    if (value.contains("$")) {
                        value = value.replaceAll("\\$", "");
                    }
                    html = html.replaceAll(element.getElementHTML(), value);
                    if (ValidationHelper.isNullOrEmpty(value) && PropertyAnnotationForPDFXMLElements.PROPERTY_ANNOTATION.equals(element))
                        html = "";
                }
            }
            result.append(html);
        }
        return result.toString();
    }

    private static String parsePropertyAnnotationTable(Element nNode, TagsForPDFXMLElements selectedProperty) {
        StringBuilder result = new StringBuilder();

        if (nNode != null) {
            if (selectedProperty.equals(TagsForPDFXMLElements.PROPERTY_BUILDING_TABLE) ||
                    selectedProperty.equals(TagsForPDFXMLElements.PROPERTY_BUILDING_TABLE_ALT)) {
                result.append(addAnnotationBuilding(nNode));
            } else if (selectedProperty.equals(TagsForPDFXMLElements.PROPERTY_LAND_TABLE) ||
                    selectedProperty.equals(TagsForPDFXMLElements.PROPERTY_LAND_TABLE_ALT)) {
                result.append(addAnnotationLand(nNode));
            }
        }

        return result.toString();
    }

    private static String parsePropertyAnnotationTable(NodeList nList, TagsForPDFXMLElements selectedProperty) {
        StringBuilder result = new StringBuilder();

        if (nList != null && nList.getLength() > 1) {
            if (selectedProperty.equals(TagsForPDFXMLElements.PROPERTY_BUILDING_TABLE)) {
                result.append(addAnnotationBuilding(nList));
            } else if (selectedProperty.equals(TagsForPDFXMLElements.PROPERTY_LAND_TABLE)) {
                result.append(addAnnotationLand(nList));
            }
        }

        return result.toString();
    }

    private static String addAnnotationLand(NodeList nList) {
        return addAnnotationLand(nList, false);
    }

    private static String addAnnotationLand(Element nNode) {
        return addAnnotationLand(nNode, false);
    }

    private static String addAnnotationLand(NodeList nList, boolean global) {
        String surfaceAre = "";
        String surfaceCa = "";
        String surfaceHa = "";
        int finalSurface = 0;
        double annuityBuilding = 0.0;
        double annuityLand = 0.0;

        double annuityBuildingAlt = 0.0;
        double annuityLandAlt = 0.0;

        String html = PrintPDFHelper.readWorkingListFile("propertyAnnotationLandTable", "Cadastral");

        for (int temp = 0; temp < nList.getLength(); temp++) {
            Element nNode = (Element) nList.item(temp);
            Element elementSurface = (Element) (nNode.getElementsByTagName("DatiClassamentoT").item(0));
            for (PropertyAnnotationBuildingTablePDFXMLElements element : PropertyAnnotationBuildingTablePDFXMLElements.values()) {
                if (html.contains(element.getElementHTML())) {
                    String value = getValueFromXML(nNode, element.getElementXML());
                    if (!ValidationHelper.isNullOrEmpty(value)) {
                        if (PropertyAnnotationBuildingTablePDFXMLElements.AGRARIO_ANNUITY_PROPERTY_TAB.equals(element)) {
                            annuityLand += Double.parseDouble(value.replaceAll("\\.", "").replaceAll(",", "."));
                        } else if (PropertyAnnotationBuildingTablePDFXMLElements.SUNDAY_ANNUITY_PROPERTY_TAB.equals(element)) {
                            annuityBuilding += Double.parseDouble(value.replaceAll("\\.", "").replaceAll(",", "."));
                        }else if (PropertyAnnotationBuildingTablePDFXMLElements.AGRARIO_ANNUITY_PROPERTY_TAB_ALT.equals(element)) {
                            annuityLandAlt += Double.parseDouble(value.replaceAll("\\.", "").replaceAll(",", "."));
                        } else if (PropertyAnnotationBuildingTablePDFXMLElements.SUNDAY_ANNUITY_PROPERTY_TAB_ALT.equals(element)) {
                            annuityBuildingAlt += Double.parseDouble(value.replaceAll("\\.", "").replaceAll(",", "."));
                        }
                    }
                    value = getValueFromXML(elementSurface, element.getElementXML());
                    if (!ValidationHelper.isNullOrEmpty(value)) {
                        if (PropertyAnnotationBuildingTablePDFXMLElements.SURFACE_ARE_PROPERTY_TAB.equals(element)) {
                            surfaceAre = value;
                        } else if (PropertyAnnotationBuildingTablePDFXMLElements.SURFACE_CA_PROPERTY_TAB.equals(element)) {
                            surfaceCa = value;
                        } else if (PropertyAnnotationBuildingTablePDFXMLElements.SURFACE_HA_PROPERTY_TAB.equals(element)) {
                            surfaceHa = value;
                        }
                    }
                }
            }
            String surface = surfaceHa + surfaceAre + surfaceCa;
            if (!ValidationHelper.isNullOrEmpty(surface)) {
                finalSurface += Integer.parseInt(surface);
            }
        }
        DecimalFormat df = new DecimalFormat("#.00");
        DecimalFormat finalFormat = new DecimalFormat("##,##.##");
        if (finalSurface != 0 && annuityBuilding != 0.0 && annuityLand != 0.0) {
            html = html.replaceAll("%G%", global ? "Genarale" : "");
            html = html.replaceAll(PropertyAnnotationBuildingTablePDFXMLElements
                    .AGRARIO_ANNUITY_PROPERTY_TAB.getElementHTML(), df.format(annuityLand));
            html = html.replaceAll(PropertyAnnotationBuildingTablePDFXMLElements
                    .SUNDAY_ANNUITY_PROPERTY_TAB.getElementHTML(), df.format(annuityBuilding));
            html = html.replaceAll(PropertyAnnotationBuildingTablePDFXMLElements
                    .SURFACE_ARE_PROPERTY_TAB.getElementHTML(), finalFormat.format(finalSurface));

            html = html.replaceAll(PropertyAnnotationBuildingTablePDFXMLElements
                    .AGRARIO_ANNUITY_PROPERTY_TAB_ALT.getElementHTML(), "");
            html = html.replaceAll(PropertyAnnotationBuildingTablePDFXMLElements
                    .SUNDAY_ANNUITY_PROPERTY_TAB_ALT.getElementHTML(), "");

            return html;
        } else if (annuityBuildingAlt != 0.0 && annuityLandAlt != 0.0) {
            html = html.replaceAll("%G%", global ? "Genarale" : "");
            html = html.replaceAll(PropertyAnnotationBuildingTablePDFXMLElements
                    .AGRARIO_ANNUITY_PROPERTY_TAB_ALT.getElementHTML(), df.format(annuityLandAlt));
            html = html.replaceAll(PropertyAnnotationBuildingTablePDFXMLElements
                    .SUNDAY_ANNUITY_PROPERTY_TAB_ALT.getElementHTML(), df.format(annuityBuildingAlt));

            html = html.replaceAll(PropertyAnnotationBuildingTablePDFXMLElements
                    .AGRARIO_ANNUITY_PROPERTY_TAB.getElementHTML(), "");
            html = html.replaceAll(PropertyAnnotationBuildingTablePDFXMLElements
                    .SUNDAY_ANNUITY_PROPERTY_TAB.getElementHTML(), "");
            html = html.replaceAll(PropertyAnnotationBuildingTablePDFXMLElements
                    .SURFACE_ARE_PROPERTY_TAB.getElementHTML(), "");

            return html;
        }else return "";
    }

    private static String addAnnotationLand(Element nNode, boolean global) {
        String surfaceAre = "";
        String surfaceCa = "";
        String surfaceHa = "";
        int finalSurface = 0;
        double annuityBuilding = 0.0;
        double annuityLand = 0.0;
        String html = PrintPDFHelper.readWorkingListFile("propertyAnnotationLandTable", "Cadastral");
        Element elementSurface = (Element) (nNode.getElementsByTagName("DatiClassamentoT").item(0));
        for (PropertyAnnotationBuildingTablePDFXMLElements element : PropertyAnnotationBuildingTablePDFXMLElements.values()) {
            if (html.contains(element.getElementHTML())) {
                String value = getValueFromXML(nNode, element.getElementXML());
                if (!ValidationHelper.isNullOrEmpty(value)) {
                    if (PropertyAnnotationBuildingTablePDFXMLElements.AGRARIO_ANNUITY_PROPERTY_TAB.equals(element)) {
                        annuityLand += Double.parseDouble(value.replaceAll("\\.", "").replaceAll(",", "."));
                    } else if (PropertyAnnotationBuildingTablePDFXMLElements.SUNDAY_ANNUITY_PROPERTY_TAB.equals(element)) {
                        annuityBuilding += Double.parseDouble(value.replaceAll("\\.", "").replaceAll(",", "."));
                    }
                }
                value = getValueFromXML(elementSurface, element.getElementXML());
                if (!ValidationHelper.isNullOrEmpty(value)) {
                    if (PropertyAnnotationBuildingTablePDFXMLElements.SURFACE_ARE_PROPERTY_TAB.equals(element)) {
                        surfaceAre = value;
                    } else if (PropertyAnnotationBuildingTablePDFXMLElements.SURFACE_CA_PROPERTY_TAB.equals(element)) {
                        surfaceCa = value;
                    } else if (PropertyAnnotationBuildingTablePDFXMLElements.SURFACE_HA_PROPERTY_TAB.equals(element)) {
                        surfaceHa = value;
                    }
                }
            }
        }
        String surface = surfaceHa + surfaceAre + surfaceCa;
        if (!ValidationHelper.isNullOrEmpty(surface)) {
            finalSurface += Integer.parseInt(surface);
        }
        DecimalFormat df = new DecimalFormat("#.00");
        DecimalFormat finalFormat = new DecimalFormat("##,##.##");
        if (finalSurface != 0 && annuityBuilding != 0.0 && annuityLand != 0.0) {
            html = html.replaceAll("%G%", global ? "Genarale" : "");
            html = html.replaceAll(PropertyAnnotationBuildingTablePDFXMLElements
                    .AGRARIO_ANNUITY_PROPERTY_TAB.getElementHTML(), df.format(annuityLand));
            html = html.replaceAll(PropertyAnnotationBuildingTablePDFXMLElements
                    .SUNDAY_ANNUITY_PROPERTY_TAB.getElementHTML(), df.format(annuityBuilding));
            html = html.replaceAll(PropertyAnnotationBuildingTablePDFXMLElements
                    .SURFACE_ARE_PROPERTY_TAB.getElementHTML(), finalFormat.format(finalSurface));

            return html;
        } else return "";
    }

    private static String addAnnotationBuilding(NodeList nList) {
        return addAnnotationBuilding(nList, false);
    }

    private static String addAnnotationBuilding(Element nNode) {
        return addAnnotationBuilding(nNode, false);
    }

    private static String addAnnotationBuilding(NodeList nList, boolean global) {
        double unitRoom = 0.0;
        double unitMq = 0.0;
        double annuity = 0.0;
        String html = PrintPDFHelper.readWorkingListFile("propertyAnnotationBuildingTable", "Cadastral");
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Element nNode = (Element) nList.item(temp);
            for (PropertyAnnotationBuildingTablePDFXMLElements element : PropertyAnnotationBuildingTablePDFXMLElements.values()) {
                if (html.contains(element.getElementHTML())) {
                    String value = getValueFromXML(nNode, element.getElementXML());
                    if (!ValidationHelper.isNullOrEmpty(value)) {
                        double val = Double.parseDouble(value.replaceAll("\\.", "")
                                .replaceAll(",", "."));
                        if (PropertyAnnotationBuildingTablePDFXMLElements.ANNUITY_PROPERTY_TAB.equals(element)) {
                            annuity += val;
                        }
                        if (PropertyAnnotationBuildingTablePDFXMLElements.VANI_PROPERTY_TAB.equals(element)) {
                            if (getValueFromXML(nNode, "Consistenza.Unita").equals("MQ")) {
                                unitMq += val;
                            } else {
                                unitRoom += val;
                            }
                        }
                    }
                }
            }
        }
        return generateHTMLAnnotationBuilding(global, unitRoom, unitMq, annuity, html);
    }

    private static String addAnnotationBuilding(Element nNode, boolean global) {
        double unitRoom = 0.0;
        double unitMq = 0.0;
        double annuity = 0.0;
        String html = PrintPDFHelper.readWorkingListFile("propertyAnnotationBuildingTable", "Cadastral");
        for (PropertyAnnotationBuildingTablePDFXMLElements element : PropertyAnnotationBuildingTablePDFXMLElements.values()) {
            if (html.contains(element.getElementHTML())) {
                String value = getValueFromXML(nNode, element.getElementXML());
                if (!ValidationHelper.isNullOrEmpty(value)) {
                    double val = Double.parseDouble(value.replaceAll("\\.", "")
                            .replaceAll(",", "."));
                    if (PropertyAnnotationBuildingTablePDFXMLElements.ANNUITY_PROPERTY_TAB.equals(element)) {
                        annuity += val;
                    }
                    if (PropertyAnnotationBuildingTablePDFXMLElements.VANI_PROPERTY_TAB.equals(element)) {
                        if (getValueFromXML(nNode, "Consistenza.Unita").equals("MQ")) {
                            unitMq += val;
                        } else {
                            unitRoom += val;
                        }
                    }
                }
            }
        }
        return generateHTMLAnnotationBuilding(global, unitRoom, unitMq, annuity, html);
    }

    private static String generateHTMLAnnotationBuilding(boolean global, double unitRoom, double unitMq,
                                                         double annuity, String html) {
        DecimalFormat df = new DecimalFormat("0.00");
        if (unitRoom != 0.0 && unitMq != 0.0 && annuity != 0.0) {
            html = html.replaceAll("%G%", global ? "Genarale" : "");
            html = html.replaceAll(PropertyAnnotationBuildingTablePDFXMLElements
                    .ANNUITY_PROPERTY_TAB.getElementHTML(), df.format(annuity));
            html = html.replaceAll(PropertyAnnotationBuildingTablePDFXMLElements
                    .VANI_PROPERTY_TAB.getElementHTML(), df.format(unitRoom));
            html = html.replaceAll(PropertyAnnotationBuildingTablePDFXMLElements
                    .MQ_PROPERTY_TAB.getElementHTML(), df.format(unitMq));
            return html;
        } else return "";
    }

    private static String parseSyntheticHeader(Element eElement, String description) {
        String s = description.substring(0, description.length() - 1);
        String descriptionWithSpace = s + " " + description.substring(description.length() - 1, description.length());
        NodeList nList = eElement.getElementsByTagName("ElencoFormalita");
        boolean containsSezione1 = false;
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Element nNode = (Element) nList.item(temp);
            String value = getValueFromXML(nNode, "ElencoFormalita.Descrizione");
            if (description.equals(value)) {
                containsSezione1 = true;
                break;
            }
        }
        if (containsSezione1) {
            nList = eElement.getElementsByTagName("SituazioneAggiornamento");
            String dal1 = null;
            String al1 = null;
            String dal2 = null;
            String al2 = null;
            for (int temp = 0; temp < nList.getLength(); temp++) {


                Element nNode = (Element) nList.item(temp);
                String value = getValueFromXML(nNode, "SituazioneAggiornamento.Descrizione");
                if (descriptionWithSpace.equals(value)) {
                    dal1 = DateTimeHelper.toString(DateTimeHelper.fromXMLString(
                            getValueFromXML(nNode, "SituazioneAggiornamento.DataDal")));
                    if (ValidationHelper.isNullOrEmpty(dal1)
                            && getValueFromXML(nNode, "SituazioneAggiornamento.DataDal").endsWith("0000")) {
                        dal1 = replaceDateZerosToDash(getValueFromXML(nNode, "SituazioneAggiornamento.DataDal"));
                    }
                    al1 = DateTimeHelper.toString(DateTimeHelper.fromXMLString(
                            getValueFromXML(nNode, "SituazioneAggiornamento.DataAl")));
                }
                if (DESCRIPTION_SYNTHETIC_LIST.equals(value)) {
                    dal2 = DateTimeHelper.toString(DateTimeHelper.fromXMLString(
                            getValueFromXML(nNode, "SituazioneAggiornamento.DataDal")));
                    if (ValidationHelper.isNullOrEmpty(dal2)
                            && getValueFromXML(nNode, "SituazioneAggiornamento.DataDal").endsWith("0000")) {
                        dal2 = replaceDateZerosToDash(getValueFromXML(nNode, "SituazioneAggiornamento.DataDal"));
                    }
                    al2 = DateTimeHelper.toString(DateTimeHelper.fromXMLString(
                            getValueFromXML(nNode, "SituazioneAggiornamento.DataAl")));
                }
            }
            return String.format("<b>%s</b> periodo informatizzato dal %s al %s " +
                            "- Periodo recuperato e validato dal %s al %s<br/>",
                    descriptionWithSpace, dal1, al1, dal2, al2);
        }
        return "";
    }

    private static String replaceDateZerosToDash(String dateWithZeros) {
        String year = dateWithZeros.substring(0, 4);
        String month = dateWithZeros.substring(4, 6);
        String day = dateWithZeros.substring(6, 8);
        String replacer = "-";
        String connector = "/";

        if (year.equals("0000")) {
            year.replace("0", replacer);
        }
        if (month.equals("00")) {
            month = month.replace("0", replacer);
        }
        if (day.equals("00")) {
            day = day.replace("0", replacer);
        }
        return day + connector + month + connector + year;
    }

    private static String parseSyntheticTable(Element eElement) {
        StringBuffer result = new StringBuffer();
        String name = getValueFromXML(eElement, "ElencoFormalita.Descrizione");

        NodeList nList = eElement.getElementsByTagName("ElencoFormalita");

        for (int temp = 0; temp < nList.getLength(); temp++) {
            Element tempNode = (Element) nList.item(temp);
            String tempName = getValueFromXML(tempNode, "ElencoFormalita.Descrizione");

            if (!tempName.equals("Sezione1")) {
                result.append(parseSyntheticHeader(eElement, tempName));
            }

            NodeList documentoList = tempNode.getElementsByTagName("DocumentoIpotecario");
            for (int j = 0; j < documentoList.getLength(); j++) {
                String html = PrintPDFHelper.readWorkingListFile("syntheticFormalities", "SyntheticFormality");

                Element nNode = (Element) documentoList.item(j);
                for (SyntheticFormalitiesForPDFXMLElements element : SyntheticFormalitiesForPDFXMLElements.values()) {
                    if (html.contains(element.getElementHTML())) {
                        String value = "";

                        if (element.isSpecialFlow()) {
                            value = handleSpecialTags(nNode, element, j + 1);
                        } else {
                            value = getValueFromXML(nNode, element.getElementXML());
                        }

                        html = html.replaceAll(element.getElementHTML(), value);
                    }
                }
                result.append(html);
            }

        }

        return result.toString();
    }


    private static String handleSpecialTags(Element nNode, SyntheticFormalitiesForPDFXMLElements element, int num) {
        String value = "";
        String tempValue = "";
        Date tempDate = null;

        switch (element) {
            case REFERENCE_FORMALITIES:
                tempValue = getValueFromXML(nNode, "DocumentoIpotecario.RifNumRPUno");
                if (!ValidationHelper.isNullOrEmpty(tempValue)) {
                    String type = getValueFromXML(nNode, "DocumentoIpotecario.RifTipoNota");
                    switch (type) {
                        case "I":
                            type = "Iscrizione";
                            break;
                        case "T":
                            type = "Trascrizione";
                            break;
                        case "A":
                            type = "Annotazione";
                            break;
                        default:
                            type = "";
                            break;
                    }
                    String anno = getValueFromXML(nNode, "DocumentoIpotecario.RifAnno");
                    value = String.format("Formalit\u00E0 di riferimento: %s n. %s del %s <br/>", type, tempValue, anno);
                }
                break;

            case PROVENANCE:
                value = getValueFromXML(nNode, element.getElementXML());
                break;

            case PROPERTY_LOCATION: {
                NodeList nList = nNode.getElementsByTagName("UbicazioneImmobili");
                StringJoiner joiner = new StringJoiner(", ");
                for (int temp = 0; temp < nList.getLength(); temp++) {
                    Node n = nList.item(temp);
                    joiner.add(getValueFromXML((Element) n, element.getElementXML()));
                }
                tempValue = joiner.toString();
            }
            if (!ValidationHelper.isNullOrEmpty(tempValue)) {
                value = String.format("Immobili siti in %s <br/>", tempValue);
            }
            break;

            case QUALIFICATION:
                tempValue = getValueFromXML(nNode, element.getElementXML());
                if (!ValidationHelper.isNullOrEmpty(tempValue)) {
                    value = String.format("%s <br/>", tempValue);
                }
                break;

            case PUBLIC_OFFICIAL:
                tempValue = getValueFromXML(nNode, "DocumentoIpotecario.DenominazionePU");
                if (!ValidationHelper.isNullOrEmpty(tempValue)) {
                    String numRepertory = getValueFromXML(nNode, "DocumentoIpotecario.Repertorio");
                    Date dateDirectory = DateTimeHelper.fromXMLString(
                            getValueFromXML(nNode, "DocumentoIpotecario.DataTitolo").trim());
                    value = String.format("Pubblico ufficiale %s Repertorio %s del %s <br/>",
                            tempValue, numRepertory, DateTimeHelper.toString(dateDirectory));
                }
                break;

            case ACT_CODE_STRING:
                tempValue = getValueFromXML(nNode, element.getElementXML());
                if (tempValue.startsWith("8") || tempValue.startsWith("9")) {
                    value = "Rettifica a ";
                }
                break;

            case FORMALITY_NUMBER:
                value = String.valueOf(num);
                break;

            case FORMALITY_TYPE:
                tempValue = getValueFromXML(nNode, element.getElementXML());

                if ("T".equalsIgnoreCase(tempValue)) {
                    value = "TRASCRIZIONE";
                } else if ("A".equalsIgnoreCase(tempValue)) {
                    value = "ANNOTAZIONE";
                } else if ("I".equalsIgnoreCase(tempValue)) {
                    value = "ISCRIZIONE";
                }

                break;

            case ACT_TYPE:
                tempValue = getValueFromXML(nNode, "DocumentoIpotecario.TipoNota");
                if ("I".equalsIgnoreCase(tempValue)) {
                    value = getValueFromXML(nNode, "DocumentoIpotecario.SpecieAtto");
                    value += " derivante da ";
                    value += getValueFromXML(nNode, "DocumentoIpotecario.Descrizione");
                } else {
                    value = getValueFromXML(nNode, element.getElementXML());
                }
                break;

            case QUALIFICATION_TYPE:
                tempValue = getValueFromXML(nNode, element.getElementXML());

                if ("C".equalsIgnoreCase(tempValue)) {
                    value = "CONTRO";
                } else if ("F".equalsIgnoreCase(tempValue)) {
                    value = "A FAVORE";
                } else if ("E".equalsIgnoreCase(tempValue)) {
                    value = "A FAVORE E CONTRO";
                }

                break;

            case FORMALITY_DATE:
                tempValue = getValueFromXML(nNode, element.getElementXML());
                tempDate = DateTimeHelper.fromXMLString(tempValue);
                value = DateTimeHelper.toString(tempDate);
                break;

            case AC_ROW:
                value = parseACRow(nNode);
                break;

            default:
                break;
        }

        return value;
    }

    private static String parseACRow(Element eElement) {
        StringBuffer result = new StringBuffer();
        NodeList nList = eElement.getElementsByTagName("FormalitaSuccessive");
        long index = 0l;

        for (int temp = 0; temp < nList.getLength(); temp++) {
            ++index;
            String html = PrintPDFHelper.readWorkingListFile("acRow", "SyntheticFormality");
            Element nNode = (Element) nList.item(temp);

            for (AnnotationForPDFXMLElements element : AnnotationForPDFXMLElements
                    .values()) {
                if (html.contains(element.getElementHTML())) {
                    String value = "";

                    if (element.isSpecialFlow()) {
                        value = handleSpecialTags(nNode, element, index);
                    } else {
                        value = getValueFromXML(nNode, element.getElementXML());
                    }

                    html = html.replaceAll(element.getElementHTML(), value);
                }
            }

            result.append(html);
        }

        nList = eElement.getElementsByTagName("Comunicazioni");

        for (int temp = 0; temp < nList.getLength(); temp++) {
            ++index;
            String html = PrintPDFHelper.readWorkingListFile("acRowCodeRemarks", "SyntheticFormality");
            Element nNode = (Element) nList.item(temp);

            for (CommunicationForPDFXMLElements element : CommunicationForPDFXMLElements.values()) {
                if (html.contains(element.getElementHTML())) {
                    String value = "";

                    if (element.isSpecialFlow()) {
                        value = handleSpecialTags(nNode, element, index);
                    } else {
                        value = getValueFromXML(nNode, element.getElementXML());
                    }

                    html = html.replaceAll(element.getElementHTML(), value);
                }
            }

            result.append(html);
        }

        if (!ValidationHelper.isNullOrEmpty(result.toString())) {
            result.insert(0, "Documenti successivi correlati: <br />");
        }

        return result.toString();
    }

    private static String handleSpecialTags(Element nNode, CommunicationForPDFXMLElements element, long index) {
        String value = "";

        switch (element) {
            case AC_NUMBER:
                value = String.valueOf(index);
                break;

            case AC_TYPE:
                value = "Comunicazione";
                break;

            case AC_RECORD_DATE:
                String dateStr = getValueFromXML(nNode, element.getElementXML());
                Date date = DateTimeHelper.fromXMLString(dateStr);
                value = DateTimeHelper.toString(date);
                break;

            case AC_ACT_TYPE:
                break;

            case AC_STATUS_BY_COMMUNICATION_CODE:
                String code = getValueFromXML(nNode, element.getElementXML());
                if ("100".equals(code)) {
                    value = "Cancellazione";
                } else {
                    value = "Restrizione di beni";
                }
                break;

            case AC_REMARKS:
                value = getValueFromXML(nNode, element.getElementXML());
                break;

            default:
                break;
        }

        return value;
    }

    private static String handleSpecialTags(Element nNode, AnnotationForPDFXMLElements element, long index) {
        String value = "";

        switch (element) {
            case AC_NUMBER:
                value = String.valueOf(index);
                break;

            case AC_TYPE: {
                String tempValue = getValueFromXML(nNode, element.getElementXML());
                if (!ValidationHelper.isNullOrEmpty(tempValue)) {
                    switch (tempValue) {
                        case "A":
                            value = "Annotazione";
                            break;

                        case "T":
                            value = "Trascrizione";
                            break;

                        case "I":
                            value = "Iscrizione";
                            break;
                    }
                }

                if (value.isEmpty()) {
                    value = "Annotazione";
                }
            }
            break;

            case AC_RECORD_DATE:
                String dateStr = getValueFromXML(nNode, element.getElementXML());
                Date date = DateTimeHelper.fromXMLString(dateStr);
                value = DateTimeHelper.toString(date);
                break;

            case AC_ACT_TYPE:
                value = String.format("(%s)", getValueFromXML(nNode, element.getElementXML()));
                break;

            default:
                break;
        }

        return value;
    }

    private static String parseSubjectTable(Element eElement) {
        StringBuffer result = new StringBuffer();
        NodeList nList = eElement.getElementsByTagName("SoggettoF");
        boolean isPhysical = true;

        if (nList.getLength() == 0) {
            nList = eElement.getElementsByTagName("SoggettoN");
            isPhysical = false;
        }

        for (int temp = 0; temp < nList.getLength(); temp++) {
            String html = PrintPDFHelper.readWorkingListFile(isPhysical ? "subjects" : "subjectsL",
                    "SyntheticFormality");
            Element nNode = (Element) nList.item(temp);

            for (SubjectForPDFXMLElements element : SubjectForPDFXMLElements.values()) {
                if (html.contains(element.getElementHTML())) {
                    String value = "";

                    if (element.isSpecialFlow()) {
                        value = handleSpecialTags(nNode, element, temp + 1);
                    } else {
                        value = getValueFromXML(nNode, element.getElementXML());
                    }
                    value = Matcher.quoteReplacement(value);
                    if (value.contains("*")) {
                        int place = value.indexOf("*");
                        value = value.substring(0, place).concat("\\*").concat(value.substring(place + 1));
                    }
                    html = html.replaceAll(element.getElementHTML(), value);
                }
            }

            result.append(html);
        }

        return result.toString();
    }

    private static String handleSpecialTags(Element nNode, SubjectForPDFXMLElements element, int num) {
        String value = "";

        switch (element) {
            case SUBJECT_NUMBER:
                value = String.valueOf(num);
                break;

            case FISCAL_CODE_HOMONYM:
                value = getValueFromXML(nNode, element.getElementXML());
                if (!ValidationHelper.isNullOrEmpty(value)) {
                    value += "*";
                }
                break;

            case BIRTH_DATE_HOMONYM:
                String dateStr = getValueFromXML(nNode, element.getElementXML());
                Date date = DateTimeHelper.fromXMLString(dateStr);
                value = DateTimeHelper.toString(date);
                break;

            default:
                break;
        }

        return value;
    }

    private static String handleSpecialTags(Element eElement, SyntheticListXMLElements element) {
        String value = null;

        switch (element) {
            case COMPUTER_UPDATE_FROM:
                value = handleSyntheticListDates(eElement, "DataDal", false);
                break;

            case COMPUTER_UPDATE_TO:
                value = handleSyntheticListDates(eElement, "DataAl", false);
                break;

            case PERIOD_RETRIEV_VALIDAT_FROM:
                value = handleSyntheticListDates(eElement, "DataDal", true);
                break;

            case PERIOD_RETRIEV_VALIDAT_TO:
                value = handleSyntheticListDates(eElement, "DataAl", true);
                break;

            default:
                break;
        }

        return value;
    }

    private static String handleSyntheticListDates(Element eElement, String attribute, boolean equals) {
        String value = null;

        NodeList nList = eElement.getElementsByTagName("SituazioneAggiornamento");

        for (int temp = 0; temp < nList.getLength(); temp++) {
            Element nNode = (Element) nList.item(temp);
            String description = nNode.getAttribute("Descrizione");

            if (equals && DESCRIPTION_SYNTHETIC_LIST.equalsIgnoreCase(description)) {
                value = nNode.getAttribute(attribute);

                break;
            } else if (!equals && !DESCRIPTION_SYNTHETIC_LIST.equalsIgnoreCase(description)) {
                value = nNode.getAttribute(attribute);

                break;
            }
        }

        return value;
    }

    private static String handleSpecialTags(Element eElement, PropertyXMLElements element, String subjectFiscalCode) {
        String value = null;

        switch (element) {
            case CLASS_REAL_ESTATE:
                value = handleClassRealEstate(eElement);
                break;
            case NUMBER_OF_ROOMS:
                value = handleNumberOfRooms(eElement);
                break;
            case TYPE_ID:
                value = handleType(eElement);
                break;
            case PROVINCE_CODE:
                value = handleProvinceCode(eElement);
                break;
            case QUOTE:
            case PROPERTYTYPE:
                value = handleAdditionalTags(eElement, element, subjectFiscalCode);
                break;
            case CATEGORY_CODE:
                value = getValueFromXML(eElement, element.getElement()).replaceAll("/", "");
                break;
            case ADDRESS:
                value = getValueFromXML(eElement, element.getElement());
                break;
            case FLOOR:
                NodeList nList = eElement.getElementsByTagName("Piano");
                StringJoiner result = new StringJoiner("-");
                if (nList.getLength() > 0) {
                    for (int temp = 0; temp < nList.getLength(); temp++) {
                        Element eEl = (Element) nList.item(temp);
                        result.add(getValueFromXML(eEl, "Piano"));
                    }
                }
                value = result.toString();
                break;

            default:
                break;
        }

        return value;
    }

    private static String handleAdditionalTags(Element eElement, PropertyXMLElements element, String subjectFiscalCode) {
        String value = null;

        eElement = (Element) eElement.getParentNode();

        NodeList nList = eElement.getElementsByTagName("Intestato");

        for (int temp = 0; temp < nList.getLength(); temp++) {
            Element nNode = (Element) nList.item(temp);

            String fc = getValueFromXML(nNode, "CF");

            if (fc != null && fc.equalsIgnoreCase(subjectFiscalCode)) {
                value = getValueFromXML(nNode, element.getElement());

                break;
            }
        }

        return value;
    }

    private static String handleProvinceCode(Element eElement) {
        String value = null;

        Document doc = eElement.getOwnerDocument();

        if (doc != null) {
            Element n = (Element) (eElement.getOwnerDocument().getElementsByTagName("DatiRichiesta")).item(0);

            if (n != null) {
                value = n.getAttribute("Provincia");
            }
        }

        return value;
    }

    private static String handleType(Element eElement) {
        Element n = (Element) eElement.getParentNode();
        if (n != null) {
            if (ValidationHelper.isNullOrEmpty(n.getAttribute("TipoCatasto"))) {
                n = (Element) n.getParentNode();
                if (n != null) {
                    NodeList nodes = n.getElementsByTagName("DatiRichiesta");
                    if (nodes.getLength() != 0) {
                        n = (Element) nodes.item(0);
                    }
                }
            }
            if ("F".equalsIgnoreCase(n.getAttribute("TipoCatasto"))) {
                return "1";
            } else if ("T".equalsIgnoreCase(n.getAttribute("TipoCatasto"))) {
                return "2";
            }
        }
        return null;
    }

    private static String handleNumberOfRooms(Element eElement) {
        String value = null;

        Element n = (Element) (eElement.getElementsByTagName("Consistenza").item(0));

        if (n != null && VANI.equalsIgnoreCase(n.getAttribute("Unita"))) {
            value = n.getAttribute("Valore");
        }

        return value;
    }

    private static String handleClassRealEstate(Element eElement) {
        String value = null;

        Element n = (Element) (eElement.getElementsByTagName("DatiClassamentoF").item(0));

        if (n == null) {
            n = (Element) (eElement.getElementsByTagName("DatiClassamentoT").item(0));
        }

        if (n != null) {
            value = n.getAttribute("Classe");
        }

        return value;
    }

}