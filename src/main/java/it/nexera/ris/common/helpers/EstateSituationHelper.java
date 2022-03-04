package it.nexera.ris.common.helpers;

import it.nexera.ris.common.enums.*;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.LandChargesRegistry;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.TypeFormality;
import it.nexera.ris.persistence.view.FormalityView;
import it.nexera.ris.web.beans.wrappers.logic.SubjectDifferenceWrapper;
import it.nexera.ris.web.common.WrapperLazyModel;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import java.util.*;
import java.util.stream.Collectors;

import static it.nexera.ris.common.helpers.TemplatePdfTableHelper.distinctByKey;

public class EstateSituationHelper extends BaseHelper {

    private static List<Long> distinctIdsSubjects;

    public static boolean isValidFormalityCadastral(Long requestId) throws PersistenceBeanException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(requestId)) {
            List<EstateSituation> estateSituationList = DaoManager.load(EstateSituation.class, new Criterion[]{
                    Restrictions.eq("request.id", requestId)
            });
            for (EstateSituation situation : estateSituationList) {
                if (!isValidFormalityCadastral(situation.getPropertyList(), situation.getFormalityList())) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isValidFormalityCadastral(List<Property> propertyList, List<Formality> formalityList)
            throws PersistenceBeanException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(formalityList)) {
            return true;
        }
        List<CadastralData> formalityCadastral = DaoManager.load(CadastralData.class, new CriteriaAlias[]{
                new CriteriaAlias("propertyList", "property", JoinType.INNER_JOIN),
                new CriteriaAlias("property.sectionB", "sectionB", JoinType.INNER_JOIN),
                new CriteriaAlias("sectionB.formality", "formality", JoinType.INNER_JOIN)
        }, new Criterion[]{
                Restrictions.in("formality.id", formalityList.stream()
                        .map(Formality::getId).collect(Collectors.toList()))
        });
        if (ValidationHelper.isNullOrEmpty(formalityCadastral)) {
            return true;
        }
        if (ValidationHelper.isNullOrEmpty(propertyList)) {
            return false;
        }
        List<CadastralData> selectedPropertyCadastral = propertyList.stream()
                .map(Property::getCadastralData).flatMap(List::stream).distinct().collect(Collectors.toList());
        if (ValidationHelper.isNullOrEmpty(selectedPropertyCadastral)) {
            return false;
        }
        for (CadastralData data : formalityCadastral) {
            if (!ValidationHelper.isNullOrEmpty(data.getSub()) && !ValidationHelper.isNullOrEmpty(data.getParticle())) {
                if (selectedPropertyCadastral.stream().noneMatch(d -> d.getSheet().equals(data.getSheet())
                        && d.getParticle().equals(data.getParticle()) && ((d.getSub() == null) || d.getSub().equals(data.getSub())))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static List<Property> loadProperty(Request request)
            throws PersistenceBeanException, IllegalAccessException {
        return loadProperty(request, null);
    }

    public static List<Property> loadProperty(Request request, Long propertyTypeId)
            throws PersistenceBeanException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(request)) {
            List<Criterion> restrictions = new LinkedList<>();
            restrictions.add(Restrictions.eq("request.id", request.getId()));
            if (request.getAggregationLandChargesRegistry() != null) {
                if (!ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry()
                        .getLandChargesRegistries().stream().map(LandChargesRegistry::getCities)
                        .flatMap(List::stream).map(City::getDescription).collect(Collectors.toList()))) {
                    restrictions.add(Restrictions.in("c.description", request.getAggregationLandChargesRegistry()
                            .getLandChargesRegistries().stream().map(LandChargesRegistry::getCities)
                            .flatMap(List::stream).map(City::getDescription).collect(Collectors.toList())));
                }
            }
            if (propertyTypeId != null) {
                restrictions.add(Restrictions.eq("type", propertyTypeId));
            }
            List<Property> propertyList = DaoManager.load(Property.class, new CriteriaAlias[]{
                    new CriteriaAlias("requestList", "request", JoinType.INNER_JOIN),
                    new CriteriaAlias("city", "c", JoinType.INNER_JOIN)
            }, restrictions.toArray(new Criterion[0]));
            propertyList.stream().filter(p -> !ValidationHelper.isNullOrEmpty(p.getEstateSituationList()))
                    .forEach(p -> p.setUsed(p.getEstateSituationList().stream()
                            .anyMatch(es -> es.getRequest() != null && es.getRequest().getId().equals(request.getId()))));
            return propertyList;
        }
        return null;
    }

    public static WrapperLazyModel<Property, Property> loadLazyProperty(Request request, Long propertyTypeId) {
        if (!ValidationHelper.isNullOrEmpty(request)) {
            List<Criterion> restrictions = new LinkedList<>();
            restrictions.add(Restrictions.eq("request.id", request.getId()));
            if (request.getAggregationLandChargesRegistry() != null) {
                DaoManager.refresh(request);
                if (!ValidationHelper.isNullOrEmpty(request.getAggregationLandChargesRegistry()
                        .getLandChargesRegistries().stream().map(LandChargesRegistry::getCities)
                        .flatMap(List::stream).map(City::getDescription).collect(Collectors.toList()))) {
                    restrictions.add(Restrictions.in("c.description", request.getAggregationLandChargesRegistry()
                            .getLandChargesRegistries().stream().map(LandChargesRegistry::getCities)
                            .flatMap(List::stream).map(City::getDescription).collect(Collectors.toList())));
                }
            }
            if (propertyTypeId != null) {
                restrictions.add(Restrictions.eq("type", propertyTypeId));
            }
            WrapperLazyModel<Property, Property> propertyList = new WrapperLazyModel<>(Property.class,
                    restrictions.toArray(new Criterion[0]), null, new CriteriaAlias[]{
                    new CriteriaAlias("requestList", "request", JoinType.INNER_JOIN),
                    new CriteriaAlias("city", "c", JoinType.INNER_JOIN)
            });
            propertyList.setRequest(request);
            return propertyList;
        }
        return null;
    }

    public static WrapperLazyModel<Property, Property> loadLazyProperty(EstateSituation estateSituation, Long propertyTypeId) {
        if (!ValidationHelper.isNullOrEmpty(estateSituation)) {
            List<Criterion> restrictions = new LinkedList<>();
            restrictions.add(Restrictions.in("id",
                    estateSituation.getPropertyList().stream().map(Property::getId)
                            .collect(Collectors.toList())));
            if (propertyTypeId != null) {
                restrictions.add(Restrictions.eq("type", propertyTypeId));
            }
            WrapperLazyModel<Property, Property> propertyList = new WrapperLazyModel<>(
                    Property.class, restrictions.toArray(new Criterion[0]), null, null);
            propertyList.setEstateSituation(estateSituation);
            return propertyList;
        }
        return null;
    }

    public static List<EstateFormality> loadEstateFormality(Request request) throws PersistenceBeanException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(request)) {
            List<EstateFormality> estateFormalityList = DaoManager.load(EstateFormality.class, new CriteriaAlias[]{
                    new CriteriaAlias("requestFormalities", "request", JoinType.INNER_JOIN)
            }, new Criterion[]{
                    Restrictions.eq("request.request.id", request.getId())
            });
            estateFormalityList.stream().filter(ef -> ef.getAccountable() == null)
                    .forEach(ef -> ef.setAccountable(true));
            estateFormalityList.stream()
                    .filter(ef -> ef.getEstateSituationList() != null)
                    .forEach(ef -> ef.setUsed(ef.getEstateSituationList().stream()
                            .filter(es -> es.getRequest() != null).anyMatch(es -> request.getId().equals(es.getRequest().getId()))));
            estateFormalityList = estateFormalityList.stream().filter(
                    (distinctByKey(x -> (x.getNumRG() + "-" + x.getNumRP() + "-" + x.getDate() + "-"
                            + x.getLandChargesRegistry())))).collect(Collectors.toList());

            List<EstateFormality> estateFormalitiesUpdated = DaoManager.load(EstateFormality.class, new CriteriaAlias[]{
                    new CriteriaAlias("requestListUpdate", "request", JoinType.INNER_JOIN)
            }, new Criterion[]{
                    Restrictions.eq("request.id", request.getId())
            });

            if (!ValidationHelper.isNullOrEmpty(estateFormalitiesUpdated)) {
                for (EstateFormality estateFormality : estateFormalityList) {
                    if (estateFormalitiesUpdated.contains(estateFormality)) {
                        estateFormality.setUpdated(true);
                    } else {
                        estateFormality.setUpdated(false);
                    }
                }
            }

            return estateFormalityList;
        }
        return null;
    }

    public static WrapperLazyModel<EstateFormality, EstateFormality> loadLazyEstateFormality(Request request)
            throws PersistenceBeanException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(request)) {
            WrapperLazyModel<EstateFormality, EstateFormality> estateFormalityList = new WrapperLazyModel<>(
                    EstateFormality.class,
                    new Criterion[]{Restrictions.eq("request.request.id", request.getId())},
                    new Order[]{Order.asc("landChargesRegistry")},
                    new CriteriaAlias[]{
                            new CriteriaAlias("requestFormalities", "request", JoinType.INNER_JOIN)});

            //Because of LazyInitializationException
            DaoManager.load(EstateFormality.class, new CriteriaAlias[]{
                            new CriteriaAlias("requestFormalities", "request", JoinType.INNER_JOIN)},
                    new Criterion[]{Restrictions.eq("request.request.id", request.getId())});

            estateFormalityList.setRequest(request);
            return estateFormalityList;
        }
        return null;
    }

    public static WrapperLazyModel<EstateFormality, EstateFormality> loadLazyEstateFormality(EstateSituation estateSituation) {
        if (!ValidationHelper.isNullOrEmpty(estateSituation)) {
            List<Criterion> restrictions = new LinkedList<>();
            restrictions.add(Restrictions.in("id",
                    estateSituation.getEstateFormalityList().stream().map(EstateFormality::getId)
                            .collect(Collectors.toList())));
            WrapperLazyModel<EstateFormality, EstateFormality> formalityList = new WrapperLazyModel<>(
                    EstateFormality.class, restrictions.toArray(new Criterion[0]), new Order[]{Order.asc("date")}, null);
            formalityList.setEstateSituation(estateSituation);
            return formalityList;
        }
        return null;
    }


    public static WrapperLazyModel<EstateFormality, EstateFormality> loadLazyEstateFormalityBySubject(Long subjectId) {
        return new WrapperLazyModel<EstateFormality, EstateFormality>(
                EstateFormality.class, new Criterion[]{Restrictions.eq("subject.id", subjectId)}, null,
                new CriteriaAlias[]{
                        new CriteriaAlias("requestFormalities", "requestAN", JoinType.INNER_JOIN),
                        new CriteriaAlias("requestAN.request", "reqAN", JoinType.INNER_JOIN),
                        new CriteriaAlias("reqAN.subject", "subject", JoinType.INNER_JOIN)
                });
    }


    public static Long loadFormalityNum(Request request) throws PersistenceBeanException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(request)) {
            return null;
        }
        List<Long> chargesRegistryIds = request.getAggregationLandChargesRegistersIds();
        return DaoManager.getCount(Formality.class, "id", new CriteriaAlias[]{
                new CriteriaAlias("sectionC", "sc", JoinType.INNER_JOIN),
                new CriteriaAlias("sc.subject", "ss", JoinType.INNER_JOIN)
        }, new Criterion[]{
                Restrictions.or(
                        Restrictions.eq("subject.id", request.getSubject().getId()),
                        Restrictions.and(
                                Restrictions.or(
                                        Restrictions.in("reclamePropertyService.id", chargesRegistryIds),
                                        Restrictions.in("provincialOffice.id", chargesRegistryIds)
                                ),
                                Restrictions.eq("ss.id", request.getSubject().getId())
                        )
                )
        });
    }

    public static List<Formality> loadFormality(Request request) throws PersistenceBeanException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(request)) {
            return null;
        }
        List<Long> chargesRegistryIds = request.getAggregationLandChargesRegistersIds();
        return DaoManager.load(Formality.class, new CriteriaAlias[]{
                new CriteriaAlias("sectionC", "sc", JoinType.INNER_JOIN),
                new CriteriaAlias("sc.subject", "ss", JoinType.INNER_JOIN)
        }, new Criterion[]{
                Restrictions.or(
                        Restrictions.eq("subject.id", request.getSubject().getId()),
                        Restrictions.and(
                                Restrictions.or(
                                        Restrictions.in("reclamePropertyService.id", chargesRegistryIds),
                                        Restrictions.in("provincialOffice.id", chargesRegistryIds)
                                ),
                                Restrictions.eq("ss.id", request.getSubject().getId())
                        )
                )
        }).stream().peek(f -> f.setCurrentSubject(request.getSubject())).collect(Collectors.toList());
    }

    public static List<FormalityView> loadFormalityViewByDistraint(Request request) throws PersistenceBeanException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(request)) {
            return null;
        }
        List<Long> resultFormalityViewIds = new ArrayList<>();
        List<FormalityView> resultList = new ArrayList<>();

        List<Long> formalityIdsBySubject = getFormalityIdsBySubject(request);

        resultFormalityViewIds.addAll(getFormalityForcedIds(request));
        resultFormalityViewIds.addAll(formalityIdsBySubject);
        resultFormalityViewIds.addAll(getFormalityIdsByProperty(request));

        if (!ValidationHelper.isNullOrEmpty(resultFormalityViewIds)) {
            resultFormalityViewIds = resultFormalityViewIds.stream().distinct().collect(Collectors.toList());


            resultList = DaoManager.load(FormalityView.class,
                    new Criterion[]{Restrictions.in("id", resultFormalityViewIds)},
                    new Order[]{Order.asc("conservatoryName"), Order.asc("presentationDate"), Order.asc("generalRegister")})
                    .stream().peek(f -> f.setCurrentSubject(request.getSubject())).collect(Collectors.toList());

            for (FormalityView fv : resultList) {
                if (formalityIdsBySubject.contains(fv.getId())) {
                    fv.setPresumableSubjects(getDistinctIdsSubjects());
                }
            }
        }

        return resultList;
    }

    private static List<Long> getFormalityIdsBySubject(Request request) throws PersistenceBeanException, IllegalAccessException {
        List<Long> idsSubjects = new ArrayList<>();
        List<Long> formalityIdsBySubject = new ArrayList<>();

        List<Subject> firstPartSubjects = DaoManager.load(Subject.class, new CriteriaAlias[]{
                        new CriteriaAlias("sectionC", "sc", JoinType.INNER_JOIN)},
                new Criterion[]{Restrictions.eq("sc.formality", request.getDistraintFormality()),
                        Restrictions.eq("sc.sectionCType", SectionCType.CONTRO.getName())});

        for (Subject sub : firstPartSubjects) {
            idsSubjects.addAll(getIdSubjects(sub));
            idsSubjects.add(sub.getId());
        }

        if (request.getSubject() != null) {
            idsSubjects.add(request.getSubject().getId());
        }
        idsSubjects = idsSubjects.stream().distinct().collect(Collectors.toList());

        setDistinctIdsSubjects(idsSubjects);

        List<Long> chargesRegistryIds = request.getAggregationLandChargesRegistersIds();
        if (!ValidationHelper.isNullOrEmpty(idsSubjects)) {
            formalityIdsBySubject = DaoManager.loadIds(Formality.class, new CriteriaAlias[]{
                            new CriteriaAlias("sectionC", "sc", JoinType.INNER_JOIN),
                            new CriteriaAlias("sc.subject", "ss", JoinType.INNER_JOIN)},
                    new Criterion[]{Restrictions.in("ss.id", idsSubjects),
                            Restrictions.or(
                                    Restrictions.and(
                                            Restrictions.isNotNull("reclamePropertyService"),
                                            Restrictions.in("reclamePropertyService.id",chargesRegistryIds)
                                    ),
                                    Restrictions.and(
                                            Restrictions.isNull("reclamePropertyService"),
                                            Restrictions.in("provincialOffice.id",chargesRegistryIds)
                                    )
                            )
                    });
        }
        return formalityIdsBySubject;
    }


    private static List<Long> getFormalityIdsByProperty(Request request) throws PersistenceBeanException, IllegalAccessException {
        List<Long> idProperties = new ArrayList<>();
        List<Long> formalityIdsByProperties = new ArrayList<>();

        List<Property> propertyList = DaoManager.load(Property.class, new CriteriaAlias[]{
                        new CriteriaAlias("sectionB", "sb", JoinType.INNER_JOIN)},
                new Criterion[]{Restrictions.eq("sb.formality", request.getDistraintFormality())});

        for (Property prop : propertyList) {
            idProperties.addAll(getIdsPresumablyProperties(prop));
        }
        idProperties = idProperties.stream().distinct().collect(Collectors.toList());
        List<Long> chargesRegistryIds = request.getAggregationLandChargesRegistersIds();
        if (!ValidationHelper.isNullOrEmpty(idProperties)) {
            formalityIdsByProperties = DaoManager.loadIds(Formality.class, new CriteriaAlias[]{
                            new CriteriaAlias("sectionB", "sb", JoinType.INNER_JOIN),
                            new CriteriaAlias("sb.properties", "sp", JoinType.INNER_JOIN)},
                    new Criterion[]{
                            Restrictions.in("sp.id", idProperties),
                            Restrictions.or(
                                    Restrictions.in("reclamePropertyService.id", chargesRegistryIds),
                                    Restrictions.in("provincialOffice.id", chargesRegistryIds)
                            )
                    });
        }
        return formalityIdsByProperties;
    }

    private static List<Long> getIdsPresumablyProperties(Property prop) throws PersistenceBeanException, IllegalAccessException {
        return DaoManager.loadIds(Property.class, new CriteriaAlias[]{
                        new CriteriaAlias("cadastralData", "cData", JoinType.INNER_JOIN)},
                new Criterion[]{Restrictions.eq("province", prop.getProvince()),
                        Restrictions.eq("city", prop.getCity()),
                        Restrictions.eq("type", prop.getType()),
                        Restrictions.eq("category", prop.getCategory()),
                        Restrictions.eq("cData.id", prop.getCadastralData().get(0).getId())});
    }

    public static List<FormalityView> loadFormalityViewForSales(Request request) throws PersistenceBeanException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(request)) {
            return null;
        }

        List<Long> chargesRegistryIds = request.getAggregationLandChargesRegistersIds();
        List<Long> ids = ValidationHelper.isNullOrEmpty(request.getSubject()) ? new ArrayList<>() :
                DaoManager.loadIds(Formality.class, new CriteriaAlias[]{
                        new CriteriaAlias("sectionC", "sc", JoinType.INNER_JOIN),
                        new CriteriaAlias("sc.subject", "ss", JoinType.INNER_JOIN)
                }, new Criterion[]{
                        Restrictions.eq("sc.sectionCType", SectionCType.CONTRO.getName()),
                        Restrictions.or(
                                Restrictions.and(
                                        Restrictions.isNotNull("reclamePropertyService"),
                                        Restrictions.in("reclamePropertyService.id", chargesRegistryIds)
                                ),
                                Restrictions.and(
                                        Restrictions.isNull("reclamePropertyService"),
                                        Restrictions.in("provincialOffice.id", chargesRegistryIds)
                                )
                        ),
                        Restrictions.eq("ss.id", request.getSubject().getId())

                });

        List<Long> idSubjects = getIdSubjects(request);
        List<Long> formalityIdsBySubject = new ArrayList<>();
        if (!ValidationHelper.isNullOrEmpty(idSubjects)) {
            formalityIdsBySubject = DaoManager.loadIds(Formality.class, new CriteriaAlias[]{
                    new CriteriaAlias("sectionC", "sc", JoinType.INNER_JOIN),
                    new CriteriaAlias("sc.subject", "ss", JoinType.INNER_JOIN)
            }, new Criterion[]{
                    Restrictions.eq("sc.sectionCType", SectionCType.CONTRO.getName()),
                    Restrictions.in("ss.id", idSubjects),
                    Restrictions.or(
                            Restrictions.and(
                                    Restrictions.isNotNull("reclamePropertyService"),
                                    Restrictions.in("reclamePropertyService.id",chargesRegistryIds)
                            ),
                            Restrictions.and(
                                    Restrictions.isNull("reclamePropertyService"),
                                    Restrictions.in("provincialOffice.id",chargesRegistryIds)
                            )
                    )
            });
        }
        ids.addAll(formalityIdsBySubject);

        List<Formality> formalities = DaoManager.load(Formality.class, new Criterion[]{
                (!ValidationHelper.isNullOrEmpty(ids) ?
                        Restrictions.in("id", ids) :
                        Restrictions.eq("id", 0L))
        });

        Iterator<Formality> itr = formalities.iterator();

        while (itr.hasNext()) {
            Formality formality = itr.next();
            if (formality.getSectionA() != null && !ValidationHelper.isNullOrEmpty(formality.getType())) {
                NoteType id = NoteType.getEnumByString(formality.getTypeEnum().toString());
                String code = "";
                TypeFormality typeFormality = null;
                List<TypeFormality> typeFormalities;
                switch (id) {
                    case NOTE_TYPE_I:
                        code = getCodeSectionA(formality.getSectionA().getDerivedFrom());
                        typeFormalities = DaoManager.load(TypeFormality.class, new Criterion[]{
                                Restrictions.eq("code", code), Restrictions.eq("type", TypeActEnum.TYPE_I)});
                        if(!ValidationHelper.isNullOrEmpty(typeFormalities)) {
                            typeFormality = typeFormalities.get(0);
                        }
                        break;
                    case NOTE_TYPE_A:
                        code = getCodeSectionA(formality.getSectionA().getAnnotationDescription());
                        typeFormalities = DaoManager.load(TypeFormality.class, new Criterion[]{
                                Restrictions.eq("code", code), Restrictions.eq("type", TypeActEnum.TYPE_A)});
                        if(!ValidationHelper.isNullOrEmpty(typeFormalities)) {
                            typeFormality = typeFormalities.get(0);
                        }
                        break;
                    case NOTE_TYPE_T:
                        code = getCodeSectionA(formality.getSectionA().getConventionDescription());
                        typeFormalities = DaoManager.load(TypeFormality.class, new Criterion[]{
                                Restrictions.eq("code", code), Restrictions.eq("type", TypeActEnum.TYPE_T)});
                        if (!ValidationHelper.isNullOrEmpty(typeFormalities)) {
                            typeFormality = typeFormalities.get(0);
                        }
                        break;
                }
                if(typeFormality != null)
                if(ValidationHelper.isNullOrEmpty(typeFormality) || ValidationHelper.isNullOrEmpty(typeFormality.getSalesDevelopment())
                    || !typeFormality.getSalesDevelopment()){
                    itr.remove();
                }
            }
        }

        ids = formalities.stream().map(Formality::getId).collect(Collectors.toList());
        List<FormalityView> resultList = DaoManager.load(FormalityView.class, new Criterion[]{
                (!ValidationHelper.isNullOrEmpty(ids) ?
                        Restrictions.in("id", ids) :
                        Restrictions.eq("id", 0L))
        }, new Order[]{Order.asc("conservatoryName"), Order.asc("presentationDate"), Order.asc("generalRegister")})
                .stream().peek(f -> f.setCurrentSubject(request.getSubject())).collect(Collectors.toList());
        for (FormalityView formalityView : resultList) {
            if (formalityIdsBySubject.contains(formalityView.getId())) {
                formalityView.setPresumableSubjects(idSubjects);
            }
        }
        return resultList;
    }

    private static String getCodeSectionA(String value) {
        if (!ValidationHelper.isNullOrEmpty(value)) {

            if (value.charAt(0) == '0') {
                return value.substring(1, value.indexOf(' '));
            } else {
                if (value.contains(" ")) {
                    return value.substring(0, value.indexOf(' '));
                } else {
                    return value;
                }
            }
        }
        return "";
    }
    public static List<FormalityView> loadFormalityView(Request request) throws PersistenceBeanException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(request)) {
            return null;
        }
        List<Long> chargesRegistryIds = request.getAggregationLandChargesRegistersIds();
        List<Long> ids = ValidationHelper.isNullOrEmpty(request.getSubject()) ? new ArrayList<>() :
                DaoManager.loadIds(Formality.class, new CriteriaAlias[]{
                        new CriteriaAlias("sectionC", "sc", JoinType.INNER_JOIN),
                        new CriteriaAlias("sc.subject", "ss", JoinType.INNER_JOIN)
                }, new Criterion[]{
                        Restrictions.or(
                                Restrictions.eq("subject.id", request.getSubject().getId()),
                                Restrictions.and(
                                        Restrictions.or(
                                                Restrictions.and(
                                                        Restrictions.isNotNull("reclamePropertyService"),
                                                        Restrictions.in("reclamePropertyService.id",chargesRegistryIds)
                                                ),
                                                Restrictions.and(
                                                        Restrictions.isNull("reclamePropertyService"),
                                                        Restrictions.in("provincialOffice.id",chargesRegistryIds)
                                                )
                                        ),
                                        Restrictions.eq("ss.id", request.getSubject().getId())
                                )
                        )
                });
        List<Long> idSubjects = getIdSubjects(request);
        List<Long> formalityIdsBySubject = new ArrayList<>();
        if (!ValidationHelper.isNullOrEmpty(idSubjects)) {
            formalityIdsBySubject = DaoManager.loadIds(Formality.class, new CriteriaAlias[]{
                    new CriteriaAlias("sectionC", "sc", JoinType.INNER_JOIN),
                    new CriteriaAlias("sc.subject", "ss", JoinType.INNER_JOIN)
            }, new Criterion[]{
                    Restrictions.in("ss.id", idSubjects),
                    Restrictions.or(
                            Restrictions.and(
                                    Restrictions.isNotNull("reclamePropertyService"),
                                    Restrictions.in("reclamePropertyService.id",chargesRegistryIds)
                            ),
                            Restrictions.and(
                                    Restrictions.isNull("reclamePropertyService"),
                                    Restrictions.in("provincialOffice.id",chargesRegistryIds)
                            )
                    )
            });
        }

        List<Long> formalityForcedIds = getFormalityForcedIds(request);

        ids.addAll(formalityIdsBySubject);
        ids.addAll(formalityForcedIds);
        List<FormalityView> resultList = DaoManager.load(FormalityView.class, new Criterion[]{
                (!ValidationHelper.isNullOrEmpty(ids) ?
                        Restrictions.in("id", ids) :
                        Restrictions.eq("id", 0L))
        }, new Order[]{Order.asc("conservatoryName"), Order.asc("presentationDate"), Order.asc("generalRegister")})
                .stream().peek(f -> f.setCurrentSubject(request.getSubject())).collect(Collectors.toList());
        for (FormalityView formalityView : resultList) {
            if (formalityIdsBySubject.contains(formalityView.getId())) {
                formalityView.setPresumableSubjects(idSubjects);
            }
        }
        return resultList;
    }

    private static List<Long> getFormalityForcedIds(Request request) throws PersistenceBeanException, IllegalAccessException {
        return DaoManager.loadIds(Formality.class, new CriteriaAlias[]{
                new CriteriaAlias("requestForcedList", "rFL", JoinType.INNER_JOIN)}, new Criterion[]{
                Restrictions.eq("rFL.id", request.getId())});
    }

    public static List<FormalityView> loadFormalityView(Subject subject) throws PersistenceBeanException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(subject)) {
            return null;
        }

        List<Criterion> criteria = new ArrayList<>();

        if (subject.getTypeIsPhysicalPerson())
            criteria.add(Restrictions.eq("sub.id", subject.getId()));
        else
            criteria.add(Restrictions.eq("sub.numberVAT", subject.getNumberVAT()));

        List<Long> ids = DaoManager.loadIds(Formality.class, new CriteriaAlias[]{new CriteriaAlias
                ("sectionC", "sectionC", JoinType.INNER_JOIN),
                new CriteriaAlias("sectionC.subject", "sub", JoinType.INNER_JOIN)
        }, criteria.toArray(new Criterion[0]));

        List<FormalityView> resultList = DaoManager.load(FormalityView.class, new Criterion[]{
                (!ValidationHelper.isNullOrEmpty(ids) ?
                        Restrictions.in("id", ids) :
                        Restrictions.eq("id", 0L))
        }, new Order[]{Order.asc("conservatoryName"), Order.asc("presentationDate"), Order.asc("generalRegister")})
                .stream().peek(f -> f.setCurrentSubject(subject)).collect(Collectors.toList());

        return resultList;
    }

    public static List<Subject> getListSubjects(List<Long> ids) throws PersistenceBeanException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(ids)) {
            return DaoManager.load(Subject.class, new Criterion[]{
                    Restrictions.in("id", ids)
            });
        }
        return new ArrayList<>();
    }

    public static List<Subject> getListSubjects(List<Long> ids, Subject sub) throws PersistenceBeanException,
            IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(ids)) {
            List<Subject> subjects =
                    DaoManager.load(Subject.class, new Criterion[]{
                            Restrictions.in("id", ids)});

            if (!ValidationHelper.isNullOrEmpty(subjects)) {
                for (Subject subject : subjects) {
                    if (RequestHelper.isBusinessNameFunctionallyEqual(
                            subject.getBusinessName(), sub.getBusinessName())) {
                        List<Subject> list = new ArrayList<>();
                        list.add(subject);
                        return list;
                    }
                }
            }
        }
        return new ArrayList<>();
    }

    public static void associatePresumableFormalityWithRequestSubjectBySectionC(
            Map<EstateFormality, List<Formality>> presumableFormalities, Request request) throws PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(presumableFormalities)) {
            for (Map.Entry<EstateFormality, List<Formality>> entry : presumableFormalities.entrySet()) {
                for (Formality formality : entry.getValue().stream()
                        .filter(f -> !ValidationHelper.isNullOrEmpty(f.getVisible())
                                && f.getVisible()).collect(Collectors.toList())) {
                    SectionC sectionC = new SectionC();
                    sectionC.setFormality(formality);
                    SectionCType sectionCType = SectionCType.getByEstateFormalityType(entry.getKey()
                            .getEstateFormalityType());
                    sectionC.setSectionCType(!ValidationHelper.isNullOrEmpty(sectionCType) ? sectionCType.getName() : null);
                    sectionC.setSubject(Collections.singletonList(request.getSubject()));
                    DaoManager.save(sectionC, true);
                }
            }
        }
    }

    public static List<Formality> getPresumableFormalityListByEstateFormality(Request request,
                                                                              EstateFormality estateFormality)
            throws PersistenceBeanException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(request.getSubject())) {
            List<Formality> presumableFormalities = DaoManager.load(Formality.class, new Criterion[]{
                    Restrictions.eq("generalRegister", estateFormality.getNumRG().toString()),
                    Restrictions.eq("particularRegister", estateFormality.getNumRP()),
                    Restrictions.or(
                            Restrictions.and(
                                    Restrictions.isNotNull("reclamePropertyService"),
                                    Restrictions.eq("reclamePropertyService.id",
                                            estateFormality.getLandChargesRegistry().getId())
                            ),
                            Restrictions.and(
                                    Restrictions.isNull("reclamePropertyService"),
                                    Restrictions.eq("provincialOffice.id",
                                            estateFormality.getLandChargesRegistry().getId())
                            )
                    ),
                    Restrictions.eq("presentationDate", estateFormality.getDate())
            });
            if (!ValidationHelper.isNullOrEmpty(presumableFormalities)) {
                List<Long> idPresumableSubjects = EstateSituationHelper.getIdSubjects(request);
                idPresumableSubjects.add(request.getSubject().getId());

                List<Formality> formalitiesThatAreNotAssociatedWithPresumableSubjects = new ArrayList<>();
                for (Formality presumableFormality : presumableFormalities) {
                    List<Long> subjectBySectionCIds = presumableFormality.getSectionC().stream().map(SectionC::getSubject)
                            .flatMap(List::stream).map(IndexedEntity::getId).collect(Collectors.toList());
                    if (subjectBySectionCIds.stream().noneMatch(idPresumableSubjects::contains)) {
                        presumableFormality.setVisible(true);
                        formalitiesThatAreNotAssociatedWithPresumableSubjects.add(presumableFormality);
                    }
                }
                return formalitiesThatAreNotAssociatedWithPresumableSubjects;
            }
        }
        return null;
    }

    public static List<Long> getIdSubjects(Request request) throws PersistenceBeanException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(request))
            return getIdSubjects(request.getSubject());
        else
            return new ArrayList<Long>();
    }

    public static List<Long> getIdSubjects(Subject subject) throws PersistenceBeanException, IllegalAccessException {
        List<Long> resultList = new ArrayList<>();
        List<Subject> difference;
        if (!ValidationHelper.isNullOrEmpty(subject)) {
            if (subject.getTypeIsPhysicalPerson()) {
                difference = getSubjects(subject);
            } else {
                difference = getLegalSubjects(subject);
            }
            if (!ValidationHelper.isNullOrEmpty(difference)) {
                for (Subject s : difference) {
                    resultList.add(s.getId());
                }
            }
        }
        return resultList;
    }

    public static List<SubjectDifferenceWrapper> loadSubjectDifference(Request request)
            throws PersistenceBeanException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(request)) {
            List<Subject> difference;
            difference = getSubjects(request.getSubject());
            return difference.stream()
                    .map(subject -> new SubjectDifferenceWrapper(subject, request.getSubject()))
                    .collect(Collectors.toList());
        }
        return null;
    }

    private static List<Subject> getLegalSubjects(Subject subject) throws PersistenceBeanException, IllegalAccessException {
        List<Subject> difference = null;
        if (!subject.getTypeIsPhysicalPerson()) {
            difference = DaoManager.load(Subject.class, new Criterion[]{
                    Restrictions.ne("id", subject.getId()),
                    Restrictions.eq("typeId", SubjectType.LEGAL_PERSON.getId()),
                    Restrictions.or(
                            Restrictions.eq("numberVAT", subject.getNumberVAT()))});
        }
        return difference;
    }

    private static List<Subject> getSubjects(Subject subject) throws PersistenceBeanException, IllegalAccessException {
        List<Subject> difference;
        if (subject.getTypeIsPhysicalPerson()) {
            Calendar subjectBirthDate = Calendar.getInstance();
            if(subject.getBirthDate() != null)
            	subjectBirthDate.setTime(subject.getBirthDate());
            difference = DaoManager.load(Subject.class, new CriteriaAlias[]{
                    new CriteriaAlias("birthCity", "city", JoinType.LEFT_OUTER_JOIN)
            }, new Criterion[]{
                    Restrictions.ne("id", subject.getId()),
                    Restrictions.eq("typeId", SubjectType.PHYSICAL_PERSON.getId()),
                    Restrictions.or(
                            Restrictions.eq("fiscalCode", subject.getFiscalCode()),
                            Restrictions.and(
                                    Restrictions.eq("birthDate", subject.getBirthDate()),
                                    Restrictions.eq("surname", subject.getSurname()),
                                    Restrictions.eq("sex", subject.getSex()),
                                    (subject.getForeignCountryExist() ?
                                            Restrictions.eq("country.id", subject.getCountry().getId()) :
                                            Restrictions.eq("birthProvince.id", subject
                                                    .getBirthProvince() == null ? null : subject.getBirthProvince().getId())),
                                    Restrictions.eq("city.description", subject
                                            .getBirthCity() == null ? null : subject.getBirthCity().getDescription()
                                    )
                            ),
                            Restrictions.and(
                                    Restrictions.eq("birthDate", subject.getBirthDate()),
                                    Restrictions.eq("name", subject.getName()),
                                    Restrictions.eq("sex", subject.getSex()),
                                    (subject.getForeignCountryExist() ?
                                            Restrictions.eq("country.id", subject.getCountry().getId()) :
                                            Restrictions.eq("birthProvince.id", subject
                                                    .getBirthProvince() == null ? null : subject.getBirthProvince().getId())),
                                    Restrictions.eq("city.description", subject
                                            .getBirthCity() == null ? null : subject.getBirthCity().getDescription()
                                    )
                            ),
                            Restrictions.and(
                                    Restrictions.eq("birthDate", subject.getBirthDate()),
                                    Restrictions.eq("name", subject.getName()),
                                    Restrictions.eq("surname", subject.getSurname()),
                                    (subject.getForeignCountryExist() ?
                                            Restrictions.eq("country.id", subject.getCountry().getId()) :
                                            Restrictions.eq("birthProvince.id", subject
                                                    .getBirthProvince() == null ? null : subject.getBirthProvince().getId())),
                                    Restrictions.eq("city.description", subject
                                            .getBirthCity() == null ? null : subject.getBirthCity().getDescription()
                                    )
                            ),
                            Restrictions.and(
                                    Restrictions.eq("birthDate", subject.getBirthDate()),
                                    Restrictions.eq("name", subject.getName()),
                                    Restrictions.eq("surname", subject.getSurname()),
                                    Restrictions.eq("sex", subject.getSex()),
                                    Restrictions.eq("city.description", subject
                                            .getBirthCity() == null ? null : subject.getBirthCity().getDescription())
                            ),
                            Restrictions.and(
                                    Restrictions.eq("birthDate", subject.getBirthDate()),
                                    Restrictions.eq("name", subject.getName()),
                                    Restrictions.eq("surname", subject.getSurname()),
                                    Restrictions.eq("sex", subject.getSex()),
                                    Restrictions.eq("birthProvince.id", subject
                                            .getBirthProvince() == null ? null : subject.getBirthProvince().getId())
                            ),
                            Restrictions.and(
                                    Restrictions.sqlRestriction("day(birth_date) = " + subjectBirthDate.get(Calendar.DAY_OF_MONTH)),
                                    Restrictions.sqlRestriction("month(birth_date) = " + (subjectBirthDate.get(Calendar.MONTH) + 1)),
                                    Restrictions.eq("name", subject.getName()),
                                    Restrictions.eq("surname", subject.getSurname()),
                                    Restrictions.eq("sex", subject.getSex()),
                                    (subject.getForeignCountryExist() ?
                                            Restrictions.eq("country.id", subject.getCountry().getId()) :
                                            Restrictions.eq("birthProvince.id", subject
                                                    .getBirthProvince() == null ? null : subject.getBirthProvince().getId())),
                                    Restrictions.eq("city.description", subject
                                            .getBirthCity() == null ? null : subject.getBirthCity().getDescription()
                                    )
                            ),
                            Restrictions.and(
                                    Restrictions.sqlRestriction("day(birth_date) = " + subjectBirthDate.get(Calendar.DAY_OF_MONTH)),
                                    Restrictions.sqlRestriction("year(birth_date) = " + subjectBirthDate.get(Calendar.YEAR)),
                                    Restrictions.eq("name", subject.getName()),
                                    Restrictions.eq("surname", subject.getSurname()),
                                    Restrictions.eq("sex", subject.getSex()),
                                    (subject.getForeignCountryExist() ?
                                            Restrictions.eq("country.id", subject.getCountry().getId()) :
                                            Restrictions.eq("birthProvince.id", subject
                                                    .getBirthProvince() == null ? null : subject.getBirthProvince().getId())),
                                    Restrictions.eq("city.description", subject
                                            .getBirthCity() == null ? null : subject.getBirthCity().getDescription()
                                    )
                            ),
                            Restrictions.and(
                                    Restrictions.sqlRestriction("month(birth_date) = " + (subjectBirthDate.get(Calendar.MONTH) + 1)),
                                    Restrictions.sqlRestriction("year(birth_date) = " + subjectBirthDate.get(Calendar.YEAR)),
                                    Restrictions.eq("name", subject.getName()),
                                    Restrictions.eq("surname", subject.getSurname()),
                                    Restrictions.eq("sex", subject.getSex()),
                                    (subject.getForeignCountryExist() ?
                                            Restrictions.eq("country.id", subject.getCountry().getId()) :
                                            Restrictions.eq("birthProvince.id", subject
                                                    .getBirthProvince() == null ? null : subject.getBirthProvince().getId())),
                                    Restrictions.eq("city.description", subject
                                            .getBirthCity() == null ? null : subject.getBirthCity().getDescription()
                                    )
                            ),
                            Restrictions.and(
                                    Restrictions.eq("birthDate", subject.getBirthDate()),
                                    Restrictions.eq("name", subject.getName()),
                                    Restrictions.eq("surname", subject.getSurname()),
                                    Restrictions.eq("sex", subject.getSex()),
                                    Restrictions.eq("country.id", subject
                                            .getCountry() == null ? null : subject.getCountry().getId())
                            )
                    )
            });
        } else {
            difference = DaoManager.load(Subject.class, new CriteriaAlias[]{
                    new CriteriaAlias("birthCity", "city", JoinType.INNER_JOIN)
            }, new Criterion[]{
                    Restrictions.ne("id", subject.getId()),
                    Restrictions.eq("typeId", SubjectType.LEGAL_PERSON.getId()),
                    Restrictions.or(
                            Restrictions.eq("numberVAT", subject.getNumberVAT()),
                            Restrictions.or(Restrictions.eq("country.id", subject
                                            .getCountry() == null ? null : subject.getCountry().getId()),
                                    Restrictions.and(
                                            Restrictions.eq("birthProvince.id", subject.getBirthProvince().getId()),
                                            Restrictions.eq("city.description", subject.getBirthCity().getDescription())
                                    )),
                            Restrictions.and(
                                    Restrictions.eq("businessName", subject.getBusinessName()),
                                    Restrictions.eq("city.description", subject.getBirthCity().getDescription())
                            ),
                            Restrictions.and(
                                    Restrictions.eq("businessName", subject.getBusinessName()),
                                    Restrictions.eq("birthProvince.id", subject.getBirthProvince().getId())
                            )
                    )
            });
        }
        return difference;
    }

    public static List<Property> loadPropertyBySubject(Long subjectId)
            throws PersistenceBeanException, IllegalAccessException {
        return loadPropertyBySubject(subjectId, null);
    }

    public static List<Property> loadPropertyBySubject(Long subjectId, Long propertyTypeId)
            throws PersistenceBeanException, IllegalAccessException {
        return DaoManager.load(Property.class, new CriteriaAlias[]{
                new CriteriaAlias("requestList", "request", JoinType.INNER_JOIN),
                new CriteriaAlias("request.subject", "subject", JoinType.INNER_JOIN)
        }, new Criterion[]{
                (propertyTypeId != null ? Restrictions.and(
                        Restrictions.eq("type", propertyTypeId),
                        Restrictions.eq("subject.id", subjectId))
                        : Restrictions.eq("subject.id", subjectId))
        });
    }

    public static WrapperLazyModel<Property, Property> loadLazyPropertyBySubject(Request request, Long subjectId, Long propertyTypeId) {
        WrapperLazyModel<Property, Property> propertyList = new WrapperLazyModel<>(Property.class,
                new Criterion[]{
                        (propertyTypeId != null ? Restrictions.and(
                                Restrictions.eq("type", propertyTypeId),
                                Restrictions.eq("subject.id", subjectId))
                                : Restrictions.eq("subject.id", subjectId))
                }, null, new CriteriaAlias[]{
                new CriteriaAlias("requestList", "request", JoinType.INNER_JOIN),
                new CriteriaAlias("request.subject", "subject", JoinType.INNER_JOIN)});
        propertyList.setRequest(request);
        propertyList.setSubject(request.getSubject());
        return propertyList;
    }

    public static List<EstateFormality> loadEstateFormalityBySubject(Long subjectId)
            throws PersistenceBeanException, IllegalAccessException {
        return DaoManager.load(EstateFormality.class, new CriteriaAlias[]{
                new CriteriaAlias("requestFormalities", "requestAN", JoinType.INNER_JOIN),
                new CriteriaAlias("requestAN.request", "reqAN", JoinType.INNER_JOIN),
                new CriteriaAlias("reqAN.subject", "subject", JoinType.INNER_JOIN)
        }, new Criterion[]{
                Restrictions.eq("subject.id", subjectId)
        });
    }

    public static List<Document> getDocuments(RequestOutputTypes type, Request request) throws PersistenceBeanException,
            IllegalAccessException {
        List<Criterion> restrictions = new ArrayList<>();
        restrictions.add(Restrictions.eq("request.id", request.getId()));
        switch (type) {
            case ONLY_EDITOR:
                restrictions.add(Restrictions.in("typeId", DocumentType.getOnlyEditorDocumentType()));
                break;
            case ONLY_FILE:
                restrictions.add(Restrictions.not(
                        Restrictions.in("typeId", DocumentType.getOnlyEditorDocumentType())));
                break;
        }

        List<Document> documentList = DaoManager.load(Document.class, restrictions.toArray(new Criterion[0]));
        if ((type == RequestOutputTypes.ALL || type == RequestOutputTypes.ONLY_EDITOR)
                && !ValidationHelper.isNullOrEmpty(request.getSituationEstateLocations())
                && request.getSituationEstateLocations().stream()
                .map(EstateSituation::getFormalityList).flatMap(List::stream).anyMatch(Objects::nonNull)) {
            Criterion[] criterions = getCriterions(request, documentList);

            if (criterions.length != 0) {
                List<Document> formalityDocs = DaoManager.load(Document.class, criterions);
                documentList.addAll(formalityDocs);
            }
        }
        if (!ValidationHelper.isNullOrEmpty(documentList)) {
            List<Formality> formalities = new ArrayList<>();
            for (Document tempDocument : documentList) {
                if (DocumentType.FORMALITY.getId().equals(tempDocument.getTypeId())) {
                    formalities.addAll(tempDocument.getFormality());
                }
            }

            DaoManager.refresh(request);
            request.setFormalityPdfList(formalities);
            DaoManager.save(request, true);
        }

        documentList.forEach(document -> document.setSelectedForEmail(true));
        List<Document> documentListToView = new ArrayList<>();
        for (Document document : documentList) {
            if (!ValidationHelper.isNullOrEmpty(document.getFormality())) {
                for (Formality formality : document.getFormality()) {
                    if (!ValidationHelper.isNullOrEmpty(formality.getEstateSituationList()) && formality
                            .getEstateSituationList().stream().anyMatch(x -> x.getRequest().equals(request))) {
                        documentListToView.add(document);
                    }
                }
            } else {
                documentListToView.add(document);
            }
        }
        return documentListToView;
    }

    private static Criterion[] getCriterions(Request request, List<Document> documentList) {
        List<Criterion> restrictionsList = new ArrayList<>();

        List<Long> documentIds = request.getSituationEstateLocations().stream()
                .map(EstateSituation::getFormalityList).flatMap(List::stream).map(Formality::getDocument)
                .filter(Objects::nonNull).map(Document::getId).collect(Collectors.toList());

        List<Long> notInIds = documentList.stream().map(Document::getId).collect(Collectors.toList());

        if (!ValidationHelper.isNullOrEmpty(documentIds)) {
            restrictionsList.add(Restrictions.in("id", documentIds));
            if (!ValidationHelper.isNullOrEmpty(notInIds)) {
                restrictionsList.add(Restrictions.not(Restrictions.in("id", notInIds)));
            }
        }

        return restrictionsList.toArray(new Criterion[0]);
    }

    public static List<Document> getDocumentsNonSale(RequestOutputTypes type, Request request) throws PersistenceBeanException,
            IllegalAccessException {
        List<Criterion> restrictions = new ArrayList<>();
        restrictions.add(Restrictions.eq("request.id", request.getId()));
        restrictions.add(Restrictions.eq("typeId", DocumentType.FORMALITY.getId()));

        List<Document> documentList = DaoManager.load(Document.class, restrictions.toArray(new Criterion[0]));
        if ((type == RequestOutputTypes.ALL || type == RequestOutputTypes.ONLY_EDITOR)
                && !ValidationHelper.isNullOrEmpty(request.getSituationEstateLocations())
                && request.getSituationEstateLocations().stream()
                .map(EstateSituation::getFormalityList).flatMap(List::stream).anyMatch(Objects::nonNull)) {
            Criterion[] criterions = getNonSaleCriterions(request, documentList);

            documentList.clear();
            if (criterions.length != 0) {
                List<Document> formalityDocs = DaoManager.load(Document.class, criterions);
                documentList.addAll(formalityDocs);
            }
        }
        if (!ValidationHelper.isNullOrEmpty(documentList)) {
            List<Formality> formalities = new ArrayList<>();
            for (Document tempDocument : documentList) {
                if (DocumentType.FORMALITY.getId().equals(tempDocument.getTypeId())) {
                    formalities.addAll(tempDocument.getFormality());
                }
            }

            DaoManager.refresh(request);
            request.setFormalityPdfList(formalities);
            DaoManager.save(request, true);
        }

        documentList.forEach(document -> document.setSelectedForEmail(true));
        List<Document> documentListToView = new ArrayList<>();
        for (Document document : documentList) {
            if (!ValidationHelper.isNullOrEmpty(document.getFormality())) {
                for (Formality formality : document.getFormality()) {
                    if (!ValidationHelper.isNullOrEmpty(formality.getEstateSituationList()) && formality
                            .getEstateSituationList().stream().anyMatch(x -> x.getRequest().equals(request))) {
                        documentListToView.add(document);
                    }
                }
            } else {
                documentListToView.add(document);
            }
        }
        return documentListToView;
    }


    public static List<Document> getDocumentsSale(RequestOutputTypes type, Request request) throws PersistenceBeanException,
            IllegalAccessException {
        List<Criterion> restrictions = new ArrayList<>();
        restrictions.add(Restrictions.eq("request.id", request.getId()));
        restrictions.add(Restrictions.eq("typeId", DocumentType.FORMALITY.getId()));

        List<Document> documentList = DaoManager.load(Document.class, restrictions.toArray(new Criterion[0]));
        if ((type == RequestOutputTypes.ALL || type == RequestOutputTypes.ONLY_EDITOR)
                && !ValidationHelper.isNullOrEmpty(request.getSituationEstateLocations())
                && request.getSituationEstateLocations().stream()
                .map(EstateSituation::getFormalityList).flatMap(List::stream).anyMatch(Objects::nonNull)) {
            Criterion[] criterions = getSaleCriterions(request, documentList);

            documentList.clear();
            if (criterions.length != 0) {
                List<Document> formalityDocs = DaoManager.load(Document.class, criterions);
                documentList.addAll(formalityDocs);
            }
        }
        if (!ValidationHelper.isNullOrEmpty(documentList)) {
            List<Formality> formalities = new ArrayList<>();
            for (Document tempDocument : documentList) {
                if (DocumentType.FORMALITY.getId().equals(tempDocument.getTypeId())) {
                    formalities.addAll(tempDocument.getFormality());
                }
            }

            DaoManager.refresh(request);
            request.setFormalityPdfList(formalities);
            DaoManager.save(request, true);
        }

        documentList.forEach(document -> document.setSelectedForEmail(true));
        List<Document> documentListToView = new ArrayList<>();
        for (Document document : documentList) {
            if (!ValidationHelper.isNullOrEmpty(document.getFormality())) {
                for (Formality formality : document.getFormality()) {
                    if (!ValidationHelper.isNullOrEmpty(formality.getEstateSituationList()) && formality
                            .getEstateSituationList().stream().anyMatch(x -> x.getRequest().equals(request))) {
                        documentListToView.add(document);
                    }
                }
            } else {
                documentListToView.add(document);
            }
        }
        return documentListToView;
    }

    private static Criterion[] getNonSaleCriterions(Request request, List<Document> documentList) {
        List<Criterion> restrictionsList = new ArrayList<>();

        List<Long> documentIds = request
                .getSituationEstateLocations()
                .stream()
                .filter(s -> s.getSalesDevelopment() == null || !s.getSalesDevelopment())
                .map(EstateSituation::getFormalityList)
                .flatMap(List::stream)
                .map(Formality::getDocument)
                .filter(Objects::nonNull)
                .map(Document::getId)
                .collect(Collectors.toList());

        List<Long> notInIds = documentList.stream().map(Document::getId).collect(Collectors.toList());


        if (!ValidationHelper.isNullOrEmpty(documentIds)) {
            restrictionsList.add(Restrictions.in("id", documentIds));
            if (!ValidationHelper.isNullOrEmpty(notInIds)) {
                restrictionsList.add(Restrictions.not(Restrictions.in("id", notInIds)));
            }
        }

        return restrictionsList.toArray(new Criterion[0]);
    }


    private static Criterion[] getSaleCriterions(Request request, List<Document> documentList) {
        List<Criterion> restrictionsList = new ArrayList<>();

        List<Long> documentIds = request
                .getSituationEstateLocations()
                .stream()
                .filter(s -> s.getSalesDevelopment() != null && s.getSalesDevelopment())
                .map(EstateSituation::getFormalityList)
                .flatMap(List::stream)
                .map(Formality::getDocument)
                .filter(Objects::nonNull)
                .map(Document::getId)
                .collect(Collectors.toList());

        List<Long> notInIds = documentList.stream().map(Document::getId).collect(Collectors.toList());


        if (!ValidationHelper.isNullOrEmpty(documentIds)) {
            restrictionsList.add(Restrictions.in("id", documentIds));
            if (!ValidationHelper.isNullOrEmpty(notInIds)) {
                restrictionsList.add(Restrictions.not(Restrictions.in("id", notInIds)));
            }
        }

        return restrictionsList.toArray(new Criterion[0]);
    }

    public static void setDistinctIdsSubjects(List<Long> distinctIdsSubjects) {
        EstateSituationHelper.distinctIdsSubjects = distinctIdsSubjects;
    }

    public static List<Long> getDistinctIdsSubjects() {
        return distinctIdsSubjects;
    }
}
