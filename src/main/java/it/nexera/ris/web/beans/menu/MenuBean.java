package it.nexera.ris.web.beans.menu;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuElement;
import org.primefaces.model.menu.MenuModel;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.web.beans.PageBean;
import it.nexera.ris.web.beans.base.AccessBean;

@ManagedBean(name = "menuBean")
@ViewScoped 
public class MenuBean extends PageBean implements Serializable {

    private static final long serialVersionUID = -5426864162684772597L;

    private MenuModel mainMenuModel;

    @Override
    protected void onConstruct() {
        mainMenuModel = new DefaultMenuModel();

        DefaultSubMenu firstSubmenu = new DefaultSubMenu(ResourcesHelper.getString("menuConfiguration"), null);
        DefaultSubMenu secondSubmenu = new DefaultSubMenu(ResourcesHelper.getString("menuConfigurationArea"), null);

        try {
            submenuAddElement(firstSubmenu, PageTypes.USER_LIST, "fa fa-fw fa-users");
            submenuAddElement(firstSubmenu, PageTypes.ROLE_LIST, "fa fa-fw fa-user-plus");
            submenuAddElement(firstSubmenu, PageTypes.APPLICATION_SETTINGS, "fa fa-fw fa-wrench");
            submenuAddElement(firstSubmenu, PageTypes.MONITORING_VIEW, "fa fa-fw fa-wrench");
            submenuAddElement(firstSubmenu, PageTypes.DOCUMENT_TEMPLATE_LIST, "fa fa-fw fa-folder");
            submenuAddElement(firstSubmenu, PageTypes.INSTANCE_PHASES_LIST, "fa fa-fw fa-cogs");
            submenuAddElement(firstSubmenu, PageTypes.IMPORT_PROPERTY_SETTINGS, "fa fa-fw fa-download");
            submenuAddElement(firstSubmenu, PageTypes.IMPORT_ESTATE_FORMALITY_SETTINGS, "fa fa-fw fa-download");
            submenuAddElement(firstSubmenu, PageTypes.IMPORT_FORMALITY_SETTINGS, "fa fa-fw fa-download");
            submenuAddElement(firstSubmenu, PageTypes.IMPORT_REPORT_FORMALITY_SUBJECT_SETTINGS, "fa fa-fw fa-download");
            submenuAddElement(firstSubmenu, PageTypes.IMPORT_VISURE_RTF_SETTINGS, "fa fa-fw fa-download");
            submenuAddElement(firstSubmenu, PageTypes.IMPORT_VISURE_DH_SETTINGS, "fa fa-fw fa-download");
            submenuAddElement(firstSubmenu, PageTypes.IMPORT_REQUEST_OLD_SETTINGS, "fa fa-fw fa-download");
            submenuAddElement(firstSubmenu, PageTypes.DOCUMENT_CONVERSION_SETTINGS, "fa fa-fw fa-cogs");
            submenuAddElement(firstSubmenu, PageTypes.OMI_VALUATION_DOCUMENTS, "fa fa-fw fa-cogs");
            submenuAddElement(firstSubmenu, PageTypes.DATI_AZIENDALI, "fa fa-fw fa-cogs");
            submenuAddElement(firstSubmenu, PageTypes.CODICI_CONSERVATORIE, "fa fa-fw fa-cogs");
            
            createThirdSubmenu(secondSubmenu);
            submenuAddElement(secondSubmenu, PageTypes.CLIENT_LIST, "fa fa-fw fa-user");

        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        addBackBtn();
        pushSubMenu(firstSubmenu);
        pushSubMenu(secondSubmenu);
        addForwardBtn();

        // sort Configurazioni menu
        for(MenuElement menuElement : mainMenuModel.getElements()) {
            if(menuElement instanceof DefaultSubMenu) {
                DefaultSubMenu defaultSubMenu = (DefaultSubMenu) menuElement;
                if(!ValidationHelper.isNullOrEmpty(defaultSubMenu.getLabel()) && 
                        defaultSubMenu.getLabel().equalsIgnoreCase("Configurazioni")){
                    Collections.sort(defaultSubMenu.getElements(), new Comparator<MenuElement>() {
                        @Override
                        public int compare(final MenuElement object1, final MenuElement object2) {
                            DefaultMenuItem o1 = (DefaultMenuItem)object1;
                            DefaultMenuItem o2 = (DefaultMenuItem)object2;
                            if (o1.getValue() == null) {
                                return (o2.getValue() == null) ? 0 : -1;
                            }
                            if (o2.getValue() == null) {
                                return 1;
                            }
                            return o1.getValue().toString().compareTo(o2.getValue().toString());
                        }
                    });
                }
            }
        }
    }

    private void addBackBtn() {
        boolean isESVPage = this.getContext().getViewRoot().getViewId().contains("EstateSituationView");

        DefaultMenuItem item = new DefaultMenuItem();
        item.setValue(ResourcesHelper.getString("menuBackBtn"));
        item.setIcon("fa fa-chevron-circle-left");
        item.setIconPos("left");
        item.setStyleClass("first-menu-icon");
        if (isESVPage) {
            item.setOnclick("window.location=document.referrer;");
        } else {
            item.setOnclick("history.go(-1);");
        }
        item.setEscape(false);
        mainMenuModel.addElement(item);
    }

    private void addForwardBtn() {
        DefaultMenuItem item = new DefaultMenuItem();
        item.setValue(ResourcesHelper.getString("menuForwardBtn"));
        item.setIcon("fa fa-chevron-circle-right");
        item.setIconPos("right");
        item.setStyleClass("last-menu-icon");
        item.setOnclick("history.go(1);");
        item.setEscape(true);
        mainMenuModel.addElement(item);
    }

    private void pushSubMenu(DefaultSubMenu menu) {
        if (menu.getElementsCount() > 0) {
            mainMenuModel.addElement(menu);
        }
    }

    private void createThirdSubmenu(DefaultSubMenu submenu) {
        try {
            submenuAddElement(submenu, PageTypes.COMMUNICATION_MESSAGE_LIST, "fa fa-fw fa-bars");
            submenuAddElement(submenu, PageTypes.TEMPLATE_DOCUMENT_MODEL_LIST, "fa fa-fw fa-bars");
            submenuAddElement(submenu, PageTypes.REQUEST_TYPE_LIST, "fa fa-fw fa-bars");
            submenuAddElement(submenu, PageTypes.COST_CONFIGURATION_LIST, "fa fa-fw fa-bars");
            submenuAddElement(submenu, PageTypes.SERVICE_LIST, "fa fa-fw fa-bars");
            submenuAddElement(submenu, PageTypes.LAND_CHARGES_REGISTRY_LIST, "fa fa-fw fa-bars");
            submenuAddElement(submenu, PageTypes.AGGREGATION_LAND_CHARGES_REGISTRY_LIST, "fa fa-fw fa-bars");
            submenuAddElement(submenu, PageTypes.CADASTRAL_CATEGORY_LIST, "fa fa-fw fa-bars");
            submenuAddElement(submenu, PageTypes.STRUCTURE_LIST, "fa fa-fw fa-bars");
            submenuAddElement(submenu, PageTypes.REFERENT_LIST, "fa fa-fw fa-bars");
            submenuAddElement(submenu, PageTypes.DATA_GROUP_LIST, "fa fa-fw fa-bars");
            submenuAddElement(submenu, PageTypes.EVENT_LIST, "fa fa-fw fa-bars");
            submenuAddElement(submenu, PageTypes.INPUT_CARD_LIST, "fa fa-fw fa-bars");
            submenuAddElement(submenu, PageTypes.DAY_PHRASE, "fa fa-fw fa-quote-right");
            submenuAddElement(submenu, PageTypes.TYPE_ACT, "fa fa-fw fa-bars");
            submenuAddElement(submenu, PageTypes.TYPE_FORMALITY, "fa fa-fw fa-bars");
            submenuAddElement(submenu, PageTypes.CADASTRAL_TOPOLOGY, "fa fa-fw fa-bars");
            submenuAddElement(submenu, PageTypes.IBAN_LIST, "fa fa-fw fa-bars");
            submenuAddElement(submenu, PageTypes.PAYMENT_TYPE_LIST, "fa fa-fw fa-bars");
            submenuAddElement(submenu, PageTypes.INVOICE_LIST, "fa fa-fw fa-bars");
            submenuAddElement(submenu, PageTypes.CITIES_LIST, "fa fa-fw fa-bars");
            submenuAddElement(submenu, PageTypes.NOTARY_LIST, "fa fa-fw fa-bars");
            submenuAddElement(submenu, PageTypes.OMI_KML_LIST, "fa fa-fw fa-bars");
            submenuAddElement(submenu, PageTypes.CATEGORY_PERCENT_VALUE_LIST, "fa fa-fw fa-bars");
            submenuAddElement(submenu, PageTypes.COURT_LIST, "fa fa-fw fa-legal");
            submenuAddElement(submenu, PageTypes.RELATIONSHIP_TYPES, "fa fa-fw fa-bars");
            submenuAddElement(submenu, PageTypes.REGIME_CONIUGI_LIST, "fa fa-fw fa-bars");
            submenuAddElement(submenu, PageTypes.SECTION_D_FORMAT_LIST, "fa fa-fw fa-bars");
            submenuAddElement(submenu, PageTypes.LAND_OMI_LIST, "fa fa-fw fa-bars");
            submenuAddElement(submenu, PageTypes.LAND_CULTURE_LIST, "fa fa-fw fa-bars");
            submenuAddElement(submenu, PageTypes.FOREIGN_STATE_LIST, "fa fa-fw fa-bars");
            submenuAddElement(submenu, PageTypes.TAX_RATE_LIST, "fa fa-fw fa-bars");
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    private void submenuAddElement(DefaultSubMenu submenu, PageTypes pageType, String icon)
            throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        if (AccessBean.canAccessPage(pageType)) {
            submenu.addElement(createSubmenuItem(pageType, icon));
        }
    }

    private DefaultMenuItem createSubmenuItem(PageTypes pageTypes, String icon)
            throws InstantiationException, IllegalAccessException, PersistenceBeanException {
        DefaultMenuItem item = new DefaultMenuItem(pageTypes.getMenuName(), icon);
        item.setUrl(pageTypes.getPagesContext());
        item.setId(pageTypes.toString());
        return item;
    }

    public MenuModel getMainMenuModel() {
        return mainMenuModel;
    }

    public void setMainMenuModel(MenuModel mainMenuModel) {
        this.mainMenuModel = mainMenuModel;
    }
}
