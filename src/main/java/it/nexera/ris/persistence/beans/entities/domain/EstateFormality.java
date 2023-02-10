package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.enums.EstateFormalityType;
import it.nexera.ris.common.enums.NoteType;
import it.nexera.ris.common.enums.Provenance;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.LandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.TypeAct;
import it.nexera.ris.persistence.interfaces.BeforeSave;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

@Entity
@Table(name = "estate_formality")
public class EstateFormality extends IndexedEntity implements BeforeSave {

    private static final long serialVersionUID = 863893897426501718L;

    @Enumerated(EnumType.STRING)
    @Column(name = "estate_formality_type")
    private EstateFormalityType estateFormalityType;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "provenance")
    private Provenance provenance;

    @Column(name = "date")
    private Date date;

    @Column(name = "num_rp")
    private String numRP;

    @Column(name = "num_rpb")
    private Integer numRPB;

    @Column(name = "num_rg")
    private Integer numRG;

    @Column(name = "repertoire")
    private String repertoire;

    @Column(name = "species_act")
    private String speciesAct;

    @ManyToOne
    @JoinColumn(name = "type_act_id")
    private TypeAct typeAct;

    @Column(name = "title_date")
    private Date titleDate;

    @Column(name = "denomination_pu")
    private String denominationPU;

    @OneToMany(fetch = FetchType.LAZY,mappedBy = "estateFormality", cascade = CascadeType.ALL)
    private List<EstateFormalitySuccess> estateFormalitySuccessList;

    @OneToMany(fetch = FetchType.LAZY,mappedBy = "estateFormality", cascade = CascadeType.ALL)
    private List<EstateLocation> estateLocationList;

    @ManyToMany(fetch = FetchType.LAZY,mappedBy = "estateFormalityList")
    private List<EstateSituation> estateSituationList;

    @Transient
    private List<Request> requestList;

    @OneToMany(fetch = FetchType.LAZY,mappedBy = "formality", cascade = CascadeType.REFRESH)
    private List<RequestFormality> requestFormalities;

    @ManyToMany
    @JoinTable(name = "request_formality_update", joinColumns = {
            @JoinColumn(name = "formality_id", table = "estate_formality")
    }, inverseJoinColumns = {
            @JoinColumn(name = "request_id", table = "request")
    })
    private List<Request> requestListUpdate;

    @Column(name = "accountable")
    private Boolean accountable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private EstateFormalityGroup estateFormalityGroup;

    @Column(name = "comment", columnDefinition = "LONGTEXT")
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charges_registry")
    private LandChargesRegistry landChargesRegistry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    @OneToMany(fetch = FetchType.LAZY,mappedBy = "estateFormality")
    private List<Communication> communications;

    @Column(name = "reference_year")
    private Integer referenceYear;

    @Transient
    private Boolean visible;

    @Transient
    private boolean used;

    @Transient
    private Boolean updated;

    @Override
    public String toString() {
        return description;
    }

    public String getEstateFormalitySuccessTypeStr(NoteType type) {
        if (!ValidationHelper.isNullOrEmpty(type)) {
            switch (type) {
                case NOTE_TYPE_A:
                    return "Ann.";
                case NOTE_TYPE_I:
                    return "Iscr. ";
                case NOTE_TYPE_T:
                    return "Trascr. ";
            }
        }
        return "";
    }

    public boolean getShowToggler() throws PersistenceBeanException, IllegalAccessException {
        return DaoManager.getCount(EstateFormalitySuccess.class, "id", new Criterion[]{
                Restrictions.eq("estateFormality.id", getId())}) > 0
                || DaoManager.getCount(Communication.class, "id", new Criterion[]{
                Restrictions.eq("estateFormality.id", getId())}) > 0;
    }

    public List<String> getExpansionText() throws PersistenceBeanException, IllegalAccessException {
        List<String> result = new LinkedList<>();
        List<EstateFormalitySuccess> estateFormalitySuccessList = DaoManager.load(EstateFormalitySuccess.class, new Criterion[]{
                Restrictions.eq("estateFormality.id", getId())
        });
        if (!ValidationHelper.isNullOrEmpty(estateFormalitySuccessList)) {
            estateFormalitySuccessList.stream().map(this::getFormalitySuccessRow).distinct().forEach(result::add);
        }
        List<Communication> communications = DaoManager.load(Communication.class, new Criterion[]{
                Restrictions.eq("estateFormality.id", getId())
        });
        if (!ValidationHelper.isNullOrEmpty(communications)) {
            for (Communication communication : communications) {
                result.add(getCommunicationRow(communication));
            }
        }
        return result;
    }

    public List<String> getExpansionText(NoteType type) throws PersistenceBeanException, IllegalAccessException {
        List<EstateFormalitySuccess> estateFormalitySuccessList = DaoManager.load(EstateFormalitySuccess.class, new Criterion[]{
                Restrictions.eq("estateFormality.id", getId()),
                Restrictions.eq("noteType", type)
        });
        if (!ValidationHelper.isNullOrEmpty(estateFormalitySuccessList)) {
            return estateFormalitySuccessList.stream().map(this::getFormalitySuccessRow).collect(Collectors.toList());
        }
        return new LinkedList<>();
    }

    public String getTypeActString() {
        if (!ValidationHelper.isNullOrEmpty(getTypeAct())) {
            String formatStr;
            if (getTypeAct().getCode().length() == 4 && getTypeAct().getCode().matches("[0-9]+")
                    && (getTypeAct().getCode().startsWith("9") || getTypeAct().getCode().startsWith("8"))) {
                formatStr = "%s - RETTIFICA - %s";
            } else {
                formatStr = "%s - %s";
            }
            return String.format(formatStr, getTypeAct().getCode(), getTypeAct().getDescription().toUpperCase());

        } else {
            return "";
        }
    }

    public String getExpansionTextStr() throws PersistenceBeanException, IllegalAccessException {
        return getExpansionText().stream().collect(Collectors.joining("<br/>"));
    }

    private String getFormalitySuccessRow(EstateFormalitySuccess success) {
        return String.format(ResourcesHelper.getString("estateFormalitySuccessRow"),
                getEstateFormalitySuccessTypeStr(success.getNoteType()), success.getNumRP(),
                DateTimeHelper.toStringDateWithDots(success.getDate()),
                !ValidationHelper.isNullOrEmpty(success.getDescription()) ? success.getDescription().toLowerCase() : "");

    }

    public String getCommunicationRow(Communication communication) {
        return String.format(ResourcesHelper.getString("estateFormalityCommunicationRow"),
                communication.getParticularRegister(), DateTimeHelper.toStringDateWithDots(communication.getReceiveDate()),
                communication.getCommunicationCode().equals("100") ? "cancellazione" : "restrizione");
    }

    public String getCommentForTag() {
        if (!ValidationHelper.isNullOrEmpty(getComment()) && !ValidationHelper.isNullOrEmpty(getEstateFormalityType())
                && !ValidationHelper.isNullOrEmpty(getTypeAct()) && !ValidationHelper.isNullOrEmpty(getTypeAct().getTextInVisura())
                && "compravendita".equalsIgnoreCase(getTypeAct().getDescription())) {
            if (getEstateFormalityType() == EstateFormalityType.FORMALITY_TYPE_F
                    || getEstateFormalityType() == EstateFormalityType.FORMALITY_TYPE_C) {
                return comment.replaceAll(getTypeAct().getTextInVisura(),
                        getEstateFormalityType() == EstateFormalityType.FORMALITY_TYPE_F ? "acquisto" : "vendita");
            }
        }
        return comment;
    }

    public String getChangedTypeAct() {
        if (!ValidationHelper.isNullOrEmpty(getEstateFormalityType()) && !ValidationHelper.isNullOrEmpty(getTypeAct())
                && !ValidationHelper.isNullOrEmpty(getTypeAct().getTextInVisura())
                && "compravendita".equalsIgnoreCase(getTypeAct().getDescription())) {
            if (getEstateFormalityType() == EstateFormalityType.FORMALITY_TYPE_F) {
                return "acquisto";
            }
            if (getEstateFormalityType() == EstateFormalityType.FORMALITY_TYPE_C) {
                return "vendita";
            }
        }
        return getTypeAct() == null || getTypeAct().getTextInVisura() == null ? "" : getTypeAct().getTextInVisura();
    }

    @Override
    public void beforeSave() {
        if (!Hibernate.isInitialized(getRequestFormalities()) || getRequestFormalities() == null) {
            return;
        }
        List<RequestFormality> listToRemove = new ArrayList<>();
        List<RequestFormality> listToAdd = new ArrayList<>();
        emptyIfNull(getRequestFormalities()).stream()
                .filter(f -> emptyIfNull(getRequestList()).stream().noneMatch(r -> r.getId().equals(f.getRequest().getId())))
                .forEach(r -> {
                            DaoManager.removeWeak(r, false);
                            listToRemove.add(r);
                        }
                );
        getRequestFormalities().removeAll(listToRemove);
        emptyIfNull(getRequestList()).stream()
                .filter(f -> emptyIfNull(getRequestFormalities()).stream().noneMatch(r -> r.getRequest().getId().equals(f.getId())))
                .forEach(r -> {
                            RequestFormality requestFormality = new RequestFormality(r, this);
                            DaoManager.saveWeak(requestFormality, false);
                            listToAdd.add(requestFormality);
                        }
                );
        getRequestFormalities().addAll(listToAdd);
    }

    public void reloadRequestFormalities() throws PersistenceBeanException, IllegalAccessException {
        setRequestFormalities(DaoManager.load(RequestFormality.class, new Criterion[]{
                Restrictions.eq("formality.id", this.getId())
        }));
    }

    public boolean addRequestFormality(Request request, Session session) throws PersistenceBeanException, IllegalAccessException {
        RequestFormality requestFormality = new RequestFormality(request, this);
        if (!ValidationHelper.isNullOrEmpty(this.getDocument())) {
            requestFormality.setDocumentId(this.getDocument().getId());
        }

       if(ValidationHelper.isNullOrEmpty(getRequestFormalities())) {
           setRequestFormalities(new ArrayList<>());
       }
        RequestFormality alreadyExistsRequestFormality = this.getRequestFormalities().stream()
                .filter(f -> f.getFormality().getId().equals(requestFormality.getFormality().getId()))
                .filter(f -> f.getRequest().getId().equals(requestFormality.getRequest().getId())).findFirst().orElse(null);

        if (ValidationHelper.isNullOrEmpty(alreadyExistsRequestFormality)) {
            this.getRequestFormalities().add(requestFormality);
            ConnectionManager.saveObject(requestFormality, false, session);
            return true;
        }else {
            alreadyExistsRequestFormality.setDocumentId(requestFormality.getDocumentId());
        }
        return false;
    }

    public List<EstateLocation> getEstateLocationList() {
        return estateLocationList;
    }

    public void setEstateLocationList(List<EstateLocation> estateLocationList) {
        this.estateLocationList = estateLocationList;
    }

    public void reloadEstateFormalitySuccessList() throws PersistenceBeanException, IllegalAccessException {
        setEstateFormalitySuccessList(DaoManager.load(EstateFormalitySuccess.class, new Criterion[]{
                Restrictions.eq("estateFormality.id", getId())
        }));
    }

    public List<EstateFormalitySuccess> getEstateFormalitySuccessList() {
        return estateFormalitySuccessList;
    }

    public void setEstateFormalitySuccessList(List<EstateFormalitySuccess> estateFormalitySuccessList) {
        this.estateFormalitySuccessList = estateFormalitySuccessList;
    }

    public EstateFormalityType getEstateFormalityType() {
        return estateFormalityType;
    }

    public void setEstateFormalityType(EstateFormalityType estateFormalityType) {
        this.estateFormalityType = estateFormalityType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Provenance getProvenance() {
        return provenance;
    }

    public void setProvenance(Provenance provenance) {
        this.provenance = provenance;
    }

    public String getNumRP() {
        return numRP;
    }

    public void setNumRP(String numRP) {
        this.numRP = numRP;
    }

    public Integer getNumRPB() {
        return numRPB;
    }

    public void setNumRPB(Integer numRPB) {
        this.numRPB = numRPB;
    }

    public Integer getNumRG() {
        return numRG;
    }

    public void setNumRG(Integer numRG) {
        this.numRG = numRG;
    }

    public String getRepertoire() {
        return repertoire;
    }

    public void setRepertoire(String repertoire) {
        this.repertoire = repertoire;
    }

    public String getSpeciesAct() {
        return speciesAct;
    }

    public void setSpeciesAct(String speciesAct) {
        this.speciesAct = speciesAct;
    }

    public String getDenominationPU() {
        return denominationPU;
    }

    public void setDenominationPU(String denominationPU) {
        this.denominationPU = denominationPU;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getTitleDate() {
        return titleDate;
    }

    public void setTitleDate(Date titleDate) {
        this.titleDate = titleDate;
    }

    public List<EstateSituation> getEstateSituationList() {
        return estateSituationList;
    }

    public void setEstateSituationList(List<EstateSituation> estateSituationList) {
        this.estateSituationList = estateSituationList;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public List<Request> getRequestList(Request request) throws PersistenceBeanException, IllegalAccessException {
        if (requestList == null) {
            List<RequestFormality> takenRequestFormalities = DaoManager.load(RequestFormality.class, new Criterion[]{
                    Restrictions.eq("request.id", request.getId()),
                    Restrictions.eq("formality.id", this.getId())});
            requestList = emptyIfNull(takenRequestFormalities).stream().map(RequestFormality::getRequest).collect(Collectors.toList());
        }
        return requestList;
    }

    public List<Request> getRequestList() {
        if (requestList == null) {
            requestList = emptyIfNull(getRequestFormalities()).stream().map(RequestFormality::getRequest).collect(Collectors.toList());
        }
        return requestList;
    }

    public void setRequestList(List<Request> requestList) {
        this.requestList = requestList;
    }

    public Boolean getAccountable() {
        return accountable;
    }

    public void setAccountable(Boolean accountable) {
        this.accountable = accountable;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public TypeAct getTypeAct() {
        return typeAct;
    }

    public void setTypeAct(TypeAct typeAct) {
        this.typeAct = typeAct;
    }

    public EstateFormalityGroup getEstateFormalityGroup() {
        return estateFormalityGroup;
    }

    public void setEstateFormalityGroup(EstateFormalityGroup estateFormalityGroup) {
        this.estateFormalityGroup = estateFormalityGroup;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LandChargesRegistry getLandChargesRegistry() {
        return landChargesRegistry;
    }

    public void setLandChargesRegistry(LandChargesRegistry landChargesRegistry) {
        this.landChargesRegistry = landChargesRegistry;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public List<Communication> getCommunications() {
        return communications;
    }

    public void setCommunications(List<Communication> communications) {
        this.communications = communications;
    }

    public List<Request> getRequestListUpdate() {
        return requestListUpdate;
    }

    public void setRequestListUpdate(List<Request> requestListUpdate) {
        this.requestListUpdate = requestListUpdate;
    }

    public Boolean getUpdated() {
        return updated;
    }

    public void setUpdated(Boolean updated) {
        this.updated = updated;
    }

    public List<RequestFormality> getRequestFormalities() {
        return requestFormalities;
    }

    public void setRequestFormalities(List<RequestFormality> requestFormalities) {
        this.requestFormalities = requestFormalities;
    }

    public Integer getReferenceYear() {
        return referenceYear;
    }

    public void setReferenceYear(Integer referenceYear) {
        this.referenceYear = referenceYear;
    }
}