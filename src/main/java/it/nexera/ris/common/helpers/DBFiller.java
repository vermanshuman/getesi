package it.nexera.ris.common.helpers;

import it.nexera.ris.common.annotations.View;
import it.nexera.ris.common.enums.*;
import it.nexera.ris.common.security.crypto.MD5;
import it.nexera.ris.common.xml.wrappers.*;
import it.nexera.ris.persistence.HibernateUtil;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.entities.domain.*;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.*;
import it.nexera.ris.settings.ApplicationSettingsHolder;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.loader.criteria.CriteriaJoinWalker;
import org.hibernate.loader.criteria.CriteriaQueryTranslator;
import org.hibernate.persister.entity.OuterJoinLoadable;
import org.hibernate.sql.JoinType;
import org.reflections.Reflections;

import javax.persistence.PersistenceException;
import javax.persistence.SequenceGenerator;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public abstract class DBFiller extends BaseHelper {
    // need fill

    public static boolean needFillEntity(Session session,
                                         Class<? extends it.nexera.ris.persistence.beans.entities.Entity> entity) {
        return ConnectionManager.getCount(entity, "id", session) == 0
                || entity == Module.class || entity == ModulePage.class
                || entity == Permission.class || entity == TypeAct.class;
    }

    public static boolean needFillUsers(Session session) {
        return ConnectionManager.load(User.class, new CriteriaAlias[]
                {
                        new CriteriaAlias("roles", "role", JoinType.LEFT_OUTER_JOIN)
                }, new Criterion[]
                {
                        Restrictions.eq("role.type", RoleTypes.ADMINISTRATOR)
                }, session).isEmpty();
    }

    // fill

    public static List<Role> fillRoles() {
        List<Role> list = new ArrayList<Role>();

        Role role = new Role();
        role.setType(RoleTypes.ADMINISTRATOR);
        role.setName("Amministratore");
        list.add(role);
        role = new Role();
        role.setType(RoleTypes.EXTERNAL);
        role.setName("Esterno");
        list.add(role);
        return list;
    }

    public static List<Permission> fillPermissions(File importFile,
                                                   Session session) {
        List<Permission> permissions = null;

        try {
            PermissionList permissionList = new PermissionList();

            JAXBContext context = JAXBContext.newInstance(PermissionList.class);
            javax.xml.bind.Unmarshaller um = context.createUnmarshaller();
            permissionList = (PermissionList) um.unmarshal(importFile);

            for (Permission permission : permissionList.getPermissions()) {
                Module module = null;
                try {
                    module = ConnectionManager.get(Module.class, new Criterion[]
                            {
                                    Restrictions.eq("code", permission.getModule_code())
                            }, session);
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
                Role role = null;
                try {
                    role = ConnectionManager.get(Role.class, new Criterion[]
                            {
                                    Restrictions.eq("id", permission.getRole_id())
                            }, session);
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
                permission.setModule(module);
                permission.setRole(role);
            }

            permissions = permissionList.getPermissions();
        } catch (JAXBException e) {
            LogHelper.log(log, e);
        }
        return permissions;
    }

    public static void fillSequences(Session session) {
        try {
            Reflections reflections = new Reflections(
                    "it.nexera.ris.persistence.beans.entities.domain");

            Set<Method> methods = reflections
                    .getMethodsAnnotatedWith(Deprecated.class);
            System.out.println(methods);

            Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(
                    (Class<? extends Annotation>) javax.persistence.Entity.class);
            for (Class<?> clazz : annotated) {
                String seqName = ((SequenceGenerator) clazz
                        .getAnnotation(SequenceGenerator.class)).sequenceName();
                BigDecimal exist = (BigDecimal) session.createSQLQuery(
                        "select count(*) from user_sequences where sequence_name = '"
                                + seqName + "'")
                        .uniqueResult();

                if (exist == null || exist.compareTo(BigDecimal.ONE) < 0) {
                    session.createSQLQuery("CREATE SEQUENCE \""
                            + HibernateUtil.getUsername() + "\".\"" + seqName
                            + "\" MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 1 NOCACHE NOORDER NOCYCLE ")
                            .executeUpdate();
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public static void createViews(Session session) {
        try {
            Reflections reflections = new Reflections(
                    "it.nexera.ris.persistence.view");

            Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(
                    (Class<? extends Annotation>) javax.persistence.Entity.class);
            for (Class<?> clazz : annotated) {
                String sql = ((View) clazz.getAnnotation(View.class)).sql();
                session.createSQLQuery(sql).executeUpdate();
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public static List<User> fillUsers(Session session)
            throws HibernateException, PersistenceException,
            InstantiationException, IllegalAccessException {
        List<User> list = new ArrayList<User>();

        User user = new User();
        user.setFirstName("admin");
        user.setLastName("admin");
        user.setEmail("admin@ris.it");
        user.setLogin("admin");
        user.setPassword(MD5.encodeString("11111111", null));
        user.setNotDeletable(Boolean.TRUE);
        user.setStatus(UserStatuses.ACTIVE);

        user.setRoles(new ArrayList<Role>());
        user.getRoles().add(ConnectionManager.get(Role.class,
                Restrictions.eq("type", RoleTypes.ADMINISTRATOR), session));

        list.add(user);
        return list;
    }

    public static List<WLGFolder> fillFolders(Session session) {
        List<WLGFolder> folders = new LinkedList<>();

        WLGFolder folder = new WLGFolder();
        folder.setName(ResourcesHelper.getEnum("mailManagerTypesSENT"));
        folder.setDefaultFolder(true);
        folder.setMailType(MailManagerTypes.SENT);
        folders.add(folder);
        folder = new WLGFolder();
        folder.setName(ResourcesHelper.getEnum("mailManagerTypesRECEIVED"));
        folder.setDefaultFolder(true);
        folder.setMailType(MailManagerTypes.RECEIVED);
        folders.add(folder);
        folder = new WLGFolder();
        folder.setName(ResourcesHelper.getEnum("mailManagerTypesDRAFT"));
        folder.setDefaultFolder(true);
        folder.setMailType(MailManagerTypes.DRAFT);
        folders.add(folder);
        folder = new WLGFolder();
        folder.setName(ResourcesHelper.getEnum("mailManagerTypesSTORAGE"));
        folder.setDefaultFolder(true);
        folder.setMailType(MailManagerTypes.STORAGE);
        folders.add(folder);

        return folders;
    }

    // from xml

    public static List<Asl> fillAsls(File importFile, Session session) {
        List<Asl> asls = null;

        try {
            AslList aslList = new AslList();

            JAXBContext context = JAXBContext.newInstance(AslList.class);
            javax.xml.bind.Unmarshaller um = context.createUnmarshaller();
            aslList = (AslList) um.unmarshal(importFile);

            asls = aslList.getAsls();
        } catch (JAXBException e) {
            LogHelper.log(log, e);
        }
        return asls;
    }

    public static List<ModulePage> fillModulePages(File importFile,
                                                   Session session) {
        List<ModulePage> modulePages = null;

        try {
            ModulePageList modulePageList = new ModulePageList();

            JAXBContext context = JAXBContext.newInstance(ModulePageList.class);
            javax.xml.bind.Unmarshaller um = context.createUnmarshaller();
            modulePageList = (ModulePageList) um.unmarshal(importFile);

            for (ModulePage modulePageItem : modulePageList.getModulePages()) {

                Module module = null;
                try {
                    module = ConnectionManager.get(Module.class, new Criterion[]
                            {
                                    Restrictions.eq("code",
                                            modulePageItem.getModule_code())
                            }, session);
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
                modulePageItem.setModule(module);
            }

            modulePages = modulePageList.getModulePages();
        } catch (JAXBException e) {
            LogHelper.log(log, e);
        }
        return modulePages;
    }

    public static List<Module> fillModuleNonEnums(File importFile,
                                                  Session session) {
        List<Module> modules = null;

        try {
            ModuleList moduleList = new ModuleList();

            JAXBContext context = JAXBContext.newInstance(ModuleList.class);
            javax.xml.bind.Unmarshaller um = context.createUnmarshaller();
            moduleList = (ModuleList) um.unmarshal(importFile);

            modules = moduleList.getModules();
        } catch (JAXBException e) {
            LogHelper.log(log, e);
        }
        return modules;
    }

    public static List<AslRegion> fillAslRegions(File importFile,
                                                 Session session) {
        List<AslRegion> aslRegions = null;

        try {
            AslRegionList aslRegionList = new AslRegionList();

            JAXBContext context = JAXBContext.newInstance(AslRegionList.class);
            javax.xml.bind.Unmarshaller um = context.createUnmarshaller();
            aslRegionList = (AslRegionList) um.unmarshal(importFile);

            aslRegions = aslRegionList.getAslRegions();
        } catch (JAXBException e) {
            LogHelper.log(log, e);
        }
        return aslRegions;
    }

    public static List<City> fillCities(File importFile, Session session) {
        List<City> cities = null;

        try {
            CityList cityList = new CityList();

            JAXBContext context = JAXBContext.newInstance(CityList.class);
            javax.xml.bind.Unmarshaller um = context.createUnmarshaller();
            cityList = (CityList) um.unmarshal(importFile);

            for (City city : cityList.getCities()) {
                Asl asl = null;
                try {
                    asl = ConnectionManager.get(Asl.class, new Criterion[]
                            {
                                    Restrictions.eq("code", city.getAsl_code())
                            }, session);
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }

                AslRegion aslRegion = null;
                try {
                    aslRegion = ConnectionManager.get(AslRegion.class,
                            new Criterion[]
                                    {
                                            Restrictions.eq("code",
                                                    city.getAsl_region_code())
                                    }, session);
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }

                Province province = null;
                try {
                    province = ConnectionManager.get(Province.class,
                            new Criterion[]
                                    {
                                            Restrictions.eq("code",
                                                    city.getProvince_code())
                                    }, session);
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
                city.setAsl(asl);
                city.setAslRegion(aslRegion);
                city.setProvince(province);
            }

            cities = cityList.getCities();
        } catch (JAXBException e) {
            LogHelper.log(log, e);
        }
        return cities;
    }

    public static List<CadastralTopology> fillCadastralTopologies(File importFile, Session session) {
        List<CadastralTopology> cadastralTopologies = null;

        try {
            CadastralTopolodyList cadastralTopolodyList = new CadastralTopolodyList();

            JAXBContext context = JAXBContext.newInstance(CadastralTopolodyList.class);
            javax.xml.bind.Unmarshaller um = context.createUnmarshaller();
            cadastralTopolodyList = (CadastralTopolodyList) um.unmarshal(importFile);

            cadastralTopologies = cadastralTopolodyList.getCadastralTopologies();
        } catch (JAXBException e) {
            LogHelper.log(log, e);
        }
        return cadastralTopologies;
    }

    public static List<TypeAct> fillTypeActs(File importFile, Session session) {
        List<TypeAct> typeActs = null;

        try {
            typeActs = new LinkedList<>();
            TypeActList typeActList = new TypeActList();

            JAXBContext context = JAXBContext.newInstance(TypeActList.class);
            javax.xml.bind.Unmarshaller um = context.createUnmarshaller();
            typeActList = (TypeActList) um.unmarshal(importFile);

            typeActList.getTypeActs().forEach(typeAct -> typeAct.setType(TypeActEnum.getByStr(typeAct.getTypeStr())));
            for (TypeAct typeAct : typeActList.getTypeActs()) {
                TypeAct typeActDB = null;
                try {
                    typeActDB = ConnectionManager.get(TypeAct.class, new Criterion[]{
                            Restrictions.eq("code", typeAct.getCode()),
                            Restrictions.eq("type", typeAct.getType())
                    }, session);
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
                if (typeActDB != null) {
                    typeActDB.setDescription(typeAct.getDescription());
                    typeActDB.setTextInVisura(typeAct.getTextInVisura());
                    typeActDB.setType(typeAct.getType());
                    typeAct = typeActDB;
                }
                typeActs.add(typeAct);
            }
        } catch (JAXBException e) {
            LogHelper.log(log, e);
        }
        return typeActs;
    }

    public static List<LandChargesRegistry> fillLandChargesRegistrySymbols(File importFile, Session session) {
        if (0 < ConnectionManager.getCount(LandChargesRegistry.class, "id", new Criterion[]{
                Restrictions.isNotNull("symbol")
        }, session)) {
            return null;
        }
        List<LandChargesRegistry> landChargesRegistries = new LinkedList<>();
        try {
            LandChargesRegistrySymbolList landChargesRegistrySymbolList = new LandChargesRegistrySymbolList();

            JAXBContext context = JAXBContext.newInstance(LandChargesRegistrySymbolList.class);
            javax.xml.bind.Unmarshaller um = context.createUnmarshaller();
            landChargesRegistrySymbolList = (LandChargesRegistrySymbolList) um.unmarshal(importFile);

            for (LandChargesRegistrySymbolWrapper wrapper : landChargesRegistrySymbolList.getLandChargesRegistries()) {
                List<LandChargesRegistry> registryList = ConnectionManager.load(LandChargesRegistry.class, new Criterion[]{
                        Restrictions.eq("name", wrapper.getName())
                }, session);
                if (!ValidationHelper.isNullOrEmpty(registryList)) {
                    for (LandChargesRegistry registry : registryList) {
                        registry.setSymbol(wrapper.getSymbol());
                        landChargesRegistries.add(registry);
                    }
                }
            }
        } catch (JAXBException e) {
            LogHelper.log(log, e);
        }
        return landChargesRegistries;
    }

    public static List<LandChargesRegistry> fillLandChargesRegistries(File importFile, Session session) {
        List<LandChargesRegistry> landChargesRegistries = null;

        try {
            landChargesRegistries = new LinkedList<>();
            LandChargesRegistryList landChargesRegistryList = new LandChargesRegistryList();

            JAXBContext context = JAXBContext.newInstance(LandChargesRegistryList.class);
            javax.xml.bind.Unmarshaller um = context.createUnmarshaller();
            landChargesRegistryList = (LandChargesRegistryList) um.unmarshal(importFile);

            for (LandChargesRegistryWrapper wrapper : landChargesRegistryList.getLandChargesRegistries()) {
                LandChargesRegistry registry = landChargesRegistries.stream()
                        .filter(l -> l.getName().equals(wrapper.getName())
                                && l.getType() == LandChargesRegistryType.valueOf(wrapper.getTypeName()))
                        .findAny().orElse(null);
                if (registry == null) {
                    registry = new LandChargesRegistry();
                    registry.setName(wrapper.getName());
                    registry.setCities(new LinkedList<>());
                    registry.setType(LandChargesRegistryType.valueOf(wrapper.getTypeName()));
                    AggregationLandChargesRegistry aggregationRegistry = new AggregationLandChargesRegistry();
                    aggregationRegistry.setName(wrapper.getName());
                    registry.setAggregationLandChargesRegistries(new LinkedList<>());
                    registry.getAggregationLandChargesRegistries().add(aggregationRegistry);
                    landChargesRegistries.add(registry);
                }
                City city = null;
                List<City> cityList = ConnectionManager.load(City.class, new Criterion[]{
                        Restrictions.eq("cfis", wrapper.getCityCfis())
                }, session);
                if (!ValidationHelper.isNullOrEmpty(cityList) && cityList.size() == 1) {
                    city = cityList.get(0);
                } else {
                    cityList = ConnectionManager.load(City.class, new CriteriaAlias[]{
                            new CriteriaAlias("province", "province", JoinType.INNER_JOIN)
                    }, new Criterion[]{
                            Restrictions.eq("cfis", wrapper.getCityCfis()),
                            Restrictions.eq("description", wrapper.getCityDescription()),
                            Restrictions.eq("province.description", wrapper.getProvinceDescription())
                    }, session);
                    if (!ValidationHelper.isNullOrEmpty(cityList)) {
                        city = cityList.get(0);
                    }
                    if (city == null) {
                        List<Province> provinceList = ConnectionManager.load(Province.class, new Criterion[]{
                                Restrictions.eq("description", wrapper.getProvinceDescription())
                        }, session);
                        City newCity = new City();
                        if (!ValidationHelper.isNullOrEmpty(provinceList)) {
                            newCity.setProvince(provinceList.get(0));
                        }
                        newCity.setDescription(wrapper.getCityDescription());
                        newCity.setCfis(wrapper.getCityCfis());
                        ConnectionManager.save(newCity, session);
                        city = newCity;
                    }
                }
                if (city != null) {
                    registry.getCities().add(city);
                }
            }
        } catch (JAXBException e) {
            LogHelper.log(log, e);
        }
        return landChargesRegistries;
    }

    public static List<TypeFormality> fillTypeFormalityAdditional(File importFile, Session session) {
        String setting = ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.TYPE_FORMALITY_ADDITIONAL_DATA_IMPORTED).getValue();
        if (!ValidationHelper.isNullOrEmpty(setting) && Boolean.parseBoolean(setting)) {
            return null;
        }
        ApplicationSettingsHolder.getInstance()
                .applyNewValue(ApplicationSettingsKeys.TYPE_FORMALITY_ADDITIONAL_DATA_IMPORTED, "true");
        List<TypeFormality> typeFormalities = new LinkedList<>();

        try {
            TypeFormalityList typeFormalityList = new TypeFormalityList();

            JAXBContext context = JAXBContext.newInstance(TypeFormalityList.class);
            javax.xml.bind.Unmarshaller um = context.createUnmarshaller();
            typeFormalityList = (TypeFormalityList) um.unmarshal(importFile);

            for (TypeFormality typeFormality : typeFormalityList.getTypeFormalities()) {
                TypeFormality formalityDB = ConnectionManager.get(TypeFormality.class, new Criterion[]{
                        Restrictions.eq("code", typeFormality.getCode()),
                        Restrictions.eq("type", TypeActEnum.getByStr(typeFormality.getType_act()))
                }, session);
                if (formalityDB == null) {
                    formalityDB = new TypeFormality();
                    formalityDB.setCode(typeFormality.getCode());
                    formalityDB.setDescription(typeFormality.getDescription());
                }
                formalityDB.setType(TypeActEnum.getByStr(typeFormality.getType_act()));
                formalityDB.setInitText(typeFormality.getInitText());
                formalityDB.setFinalText(typeFormality.getFinalText());
                typeFormalities.add(formalityDB);
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return typeFormalities;
    }

    public static List<TypeFormality> fillTypeFormalities(File importFile, Session session) {
        List<TypeFormality> typeFormalities = null;

        try {
            TypeFormalityList typeFormalityList = new TypeFormalityList();

            JAXBContext context = JAXBContext.newInstance(TypeFormalityList.class);
            javax.xml.bind.Unmarshaller um = context.createUnmarshaller();
            typeFormalityList = (TypeFormalityList) um.unmarshal(importFile);

            typeFormalities = typeFormalityList.getTypeFormalities();
        } catch (JAXBException e) {
            LogHelper.log(log, e);
        }
        return typeFormalities;
    }

    public static List<Country> fillContries(File importFile, Session session) {
        List<Country> contries = null;

        try {
            CountryList countryList = new CountryList();

            JAXBContext context = JAXBContext.newInstance(CountryList.class);
            javax.xml.bind.Unmarshaller um = context.createUnmarshaller();
            countryList = (CountryList) um.unmarshal(importFile);

            contries = countryList.getCountries();
        } catch (JAXBException e) {
            LogHelper.log(log, e);
        }

        return contries;
    }

    public static List<Nationality> fillNationalities(File importFile,
                                                      Session session) {
        List<Nationality> nationalities = null;

        try {
            NationalityList nationalityList = new NationalityList();

            JAXBContext context = JAXBContext
                    .newInstance(NationalityList.class);
            javax.xml.bind.Unmarshaller um = context.createUnmarshaller();
            nationalityList = (NationalityList) um.unmarshal(importFile);

            for (Nationality nationality : nationalityList.getNationalities()) {
                AslRegion aslRegion = null;
                try {
                    aslRegion = ConnectionManager.get(AslRegion.class,
                            new Criterion[]
                                    {
                                            Restrictions.eq("code",
                                                    nationality.getAsl_region_code())
                                    }, session);
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }

                nationality.setAslRegion(aslRegion);
            }

            nationalities = nationalityList.getNationalities();
        } catch (JAXBException e) {
            LogHelper.log(log, e);
        }
        return nationalities;
    }

    public static List<Province> fillProvinces(File importFile, Session session) {
        List<Province> provinces = null;

        try {
            ProvinceList nationalityList = new ProvinceList();

            JAXBContext context = JAXBContext.newInstance(ProvinceList.class);
            javax.xml.bind.Unmarshaller um = context.createUnmarshaller();
            nationalityList = (ProvinceList) um.unmarshal(importFile);

            provinces = nationalityList.getProvinces();
        } catch (JAXBException e) {
            LogHelper.log(log, e);
        }
        return provinces;
    }

//    public static String printCriteria(Criteria criteria) {
//        try {
//            CriteriaImpl criteriaImpl = (CriteriaImpl)criteria;
//            SessionImplementor session = criteriaImpl.getSession();
//            SessionFactoryImplementor factory = session.getFactory();
//            CriteriaQueryTranslator translator=new CriteriaQueryTranslator(factory,criteriaImpl,criteriaImpl.getEntityOrClassName(),CriteriaQueryTranslator.ROOT_SQL_ALIAS);
//            String[] implementors = factory.getImplementors( criteriaImpl.getEntityOrClassName() );
//
//            CriteriaJoinWalker walker = new CriteriaJoinWalker((OuterJoinLoadable)factory.getEntityPersister(implementors[0]), 
//                                    translator,
//                                    factory, 
//                                    criteriaImpl, 
//                                    criteriaImpl.getEntityOrClassName(), 
//                                    session.getLoadQueryInfluencers()   );
//
//            String sql=walker.getSQLString();
//            return sql;
//        } catch (Exception e) {
//
//        }
//        
//        return "";
//    }
}
