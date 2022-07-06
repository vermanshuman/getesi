package it.nexera.ris.web.beans.pages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import org.apache.commons.collections4.ListUtils;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.primefaces.model.DualListModel;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.CategoryItemGroupOmi;
import it.nexera.ris.persistence.beans.entities.domain.CategoryPercentValue;
import it.nexera.ris.persistence.beans.entities.domain.ItemGroupOmi;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.CadastralCategory;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.GroupOmi;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import it.nexera.ris.web.beans.wrappers.logic.CategoryColumnWrapper;
import it.nexera.ris.web.beans.wrappers.logic.CategoryGroupItemWrapper;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ManagedBean
@ViewScoped
public class CategoryPercentValueBean extends EntityLazyListPageBean<CategoryPercentValue> {

    private static final long serialVersionUID = 812561680605716632L;

    private CategoryPercentValue categoryPVForDialog;

    private List<SelectItem> categorySelectItemList;

    private Long selectedCategory;

    private Long percentOmi;

    private Long percentCommercial;
    
    private String groupOmiName;

    private Integer groupOmiStep;
    
    private GroupOmi groupOmiPVForDialog;
    
    private List<GroupOmi> groupOmis;
    
    private Integer groupItemPosition;
    
    private DualListModel<CategoryColumnWrapper> categoryColumns;
    
    private Long groupOmiEditId;
    
    private CategoryGroupItemWrapper wrapperPVForDialog;
    
    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException, IOException {
        loadList(CategoryPercentValue.class, new Criterion[]{}, new Order[]{Order.asc("cc.code")}, new CriteriaAlias[]{
                new CriteriaAlias("cadastralCategory", "cc", JoinType.INNER_JOIN)
        });
     
       loadGroupOmis();
       loadCategories();
    }

    public void handleCategoryPercentValueDialogInit(CategoryPercentValue categoryPercentValue)
            throws PersistenceBeanException, IllegalAccessException {
        setCategoryPVForDialog(categoryPercentValue);

        setSelectedCategory(categoryPercentValue != null ? categoryPercentValue.getCadastralCategory().getId() : null);
        setPercentOmi(categoryPercentValue != null ? categoryPercentValue.getPercentOmi() : null);
        setPercentCommercial(categoryPercentValue != null ? categoryPercentValue.getPercentCommercial() : null);

        initCategoryListWithoutAlreadyAssociatedWithAnotherCPVs(getSelectedCategory());
    }

    private void initCategoryListWithoutAlreadyAssociatedWithAnotherCPVs(Long selectedCategoryId)
            throws PersistenceBeanException, IllegalAccessException {
        setCategorySelectItemList(ComboboxHelper.fillList(DaoManager.load(CadastralCategory.class, new CriteriaAlias[]{
                        new CriteriaAlias("categoryPercentValue", "cpv", JoinType.LEFT_OUTER_JOIN)
                },
                new Criterion[]{
                        Restrictions.or(
                                Restrictions.isNull("cpv.id"),
                                Restrictions.eq("id", selectedCategoryId != null ? selectedCategoryId : 0L)
                        )
                }), false));
    }

    public void handleDeleteCategoryPercentValue(CategoryPercentValue categoryPercentValue)
            throws PersistenceBeanException {
        DaoManager.remove(categoryPercentValue, true);
    }

    public void saveCategoryPV() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        CategoryPercentValue categoryPercentValue = getCategoryPVForDialog();
        if (ValidationHelper.isNullOrEmpty(categoryPercentValue)) {
            categoryPercentValue = new CategoryPercentValue();
        }
        if (!ValidationHelper.isNullOrEmpty(getSelectedCategory())) {
            categoryPercentValue.setCadastralCategory(DaoManager.get(CadastralCategory.class, getSelectedCategory()));
        } else {
            categoryPercentValue.setCadastralCategory(null);
        }
        
        if(getPercentCommercial() != null && getPercentCommercial() > 0) {
            categoryPercentValue.setPercentCommercial(getPercentCommercial());
        }else 
        	categoryPercentValue.setPercentCommercial(null);
        
        if(getPercentOmi() != null && getPercentOmi() > 0) {
            categoryPercentValue.setPercentOmi(getPercentOmi());
        }else
        	categoryPercentValue.setPercentOmi(null);
        
        
        DaoManager.save(categoryPercentValue, true);

        setCategoryPVForDialog(null);
    }
    
    public void handleGroupOmiDialogInit(GroupOmi groupOmi)
            throws PersistenceBeanException, IllegalAccessException {
        
        setGroupOmiPVForDialog(groupOmi);
        setGroupOmiName(groupOmi != null ? groupOmi.getName() : null);
        setGroupOmiStep(groupOmi != null ? groupOmi.getStepValue() : null);
    }

    public void saveGroupOmi() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        
        GroupOmi groupOmi = getGroupOmiPVForDialog();
        
        if(ValidationHelper.isNullOrEmpty(groupOmi))
            groupOmi = new GroupOmi();
        
        groupOmi.setName(getGroupOmiName());
        groupOmi.setStepValue(getGroupOmiStep());
        DaoManager.save(groupOmi, true);
        setGroupOmis(DaoManager.load(GroupOmi.class,new Criterion[]{},new Order[]{ Order.asc("name")}));
        setGroupOmiPVForDialog(null);
    }
    
    public void handleGroupItemDialogInit(GroupOmi groupOmi, 
            CategoryGroupItemWrapper categoryGroupItemWrapper)
                    throws PersistenceBeanException, IllegalAccessException, InstantiationException {

        setGroupItemPosition(categoryGroupItemWrapper != null ? categoryGroupItemWrapper.getPosition() : null);
        setGroupOmiPVForDialog(groupOmi);
        setWrapperPVForDialog(categoryGroupItemWrapper);

        if(!ValidationHelper.isNullOrEmpty(groupOmi)) {
            List<ItemGroupOmi> itemGroupOmis =
                    DaoManager.load(ItemGroupOmi.class, new Criterion[]{
                            Restrictions.eq("groupOmi", groupOmi)
                    }, Order.desc("position"), 1);

            if(!ValidationHelper.isNullOrEmpty(itemGroupOmis)) {
                setGroupItemPosition(itemGroupOmis.get(0).getPosition()+1);
            }
        }

        if(ValidationHelper.isNullOrEmpty(getGroupItemPosition())) {
            setGroupItemPosition(1);
        }
        if(!ValidationHelper.isNullOrEmpty(categoryGroupItemWrapper)) {
            String categoryCode = categoryGroupItemWrapper.getCategoryCode();
            List<String> categoryCodes = Stream.of(categoryCode.split(",", -1))
                    .collect(Collectors.toList());

            List<CategoryColumnWrapper> sourceCategoryColumns = new LinkedList<>();

            List<CategoryColumnWrapper> targetCategoryColumns = new LinkedList<>(); 

            List<CadastralCategory> cadastralCategoryList = DaoManager.load(CadastralCategory.class);
            for (CadastralCategory cadastralCategory : cadastralCategoryList) {
                sourceCategoryColumns.add(new CategoryColumnWrapper(cadastralCategory));
            }

            for(String code : categoryCodes) {
                CategoryItemGroupOmi categoryItemGroupOmi = 
                        DaoManager.get(CategoryItemGroupOmi.class, new CriteriaAlias[]{
                                new CriteriaAlias("category", "category", JoinType.INNER_JOIN)
                        }, new Criterion[]{
                                Restrictions.and(Restrictions.eq("itemGroupOmi", categoryGroupItemWrapper.getItemGroupOmi())
                                        ,Restrictions.eq("category.codeInVisura", code.trim()))
                        });
                if (!ValidationHelper
                        .isNullOrEmpty(categoryItemGroupOmi)) {
                    targetCategoryColumns.add(new CategoryColumnWrapper(categoryItemGroupOmi));
                }
            }
            for (CategoryColumnWrapper categoryColumnWrapper : targetCategoryColumns) {
                sourceCategoryColumns.stream().filter(c -> c.getCategoryItemGroupOmi().getCategory().getId().
                        equals(categoryColumnWrapper.getCategoryItemGroupOmi().getCategory().getId()))
                .findAny().ifPresent(cv -> sourceCategoryColumns.remove(cv));
            }
            this.setCategoryColumns(new DualListModel<>(sourceCategoryColumns, targetCategoryColumns));
        }
    }
    
    private void loadCategories() throws HibernateException, IllegalAccessException, PersistenceBeanException {
        List<CategoryColumnWrapper> sourceCategoryColumns = new LinkedList<>();
        List<CadastralCategory> cadastralCategoryList = DaoManager.load(CadastralCategory.class);
        for (CadastralCategory cadastralCategory : cadastralCategoryList) {
            sourceCategoryColumns.add(new CategoryColumnWrapper(cadastralCategory));
        }
        List<CategoryColumnWrapper> targetCategoryColumns = new LinkedList<>();
        this.setCategoryColumns(new DualListModel<>(sourceCategoryColumns, targetCategoryColumns));
    }
    
    private void loadGroupOmis() throws HibernateException, IllegalAccessException, PersistenceBeanException {
        setGroupOmis(DaoManager.load(GroupOmi.class,new Criterion[]{},new Order[]{ Order.asc("name")}));
        for(GroupOmi groupOmi : getGroupOmis()) {
            List<CategoryGroupItemWrapper> categoryGroupItemWrappers = new ArrayList<CategoryGroupItemWrapper>();
            List<ItemGroupOmi> itemGroupOmis = DaoManager.load(ItemGroupOmi.class
                    , new Criterion[]{Restrictions.eq("groupOmi", groupOmi)},new Order[]{ Order.asc("position")});
            for(ItemGroupOmi itemGroupOmi : itemGroupOmis) {
                List<CategoryItemGroupOmi> categoryItemGroupOmis = DaoManager.load(CategoryItemGroupOmi.class
                        , new Criterion[]{Restrictions.eq("itemGroupOmi", itemGroupOmi)});
                
                CategoryGroupItemWrapper categoryGroupItemWrapper = new CategoryGroupItemWrapper();
                categoryGroupItemWrapper.setPosition(itemGroupOmi.getPosition());
                categoryGroupItemWrapper.setItemGroupOmi(itemGroupOmi);
                List<String> categoryCodes = new ArrayList<String>();
                for(CategoryItemGroupOmi categoryItemGroupOmi : categoryItemGroupOmis) {
                    categoryCodes.add(categoryItemGroupOmi.getCategory().getCodeInVisura());
                }
                
                 String categoryCode = categoryCodes.stream()
                                     .collect(Collectors.joining(", "));
                categoryGroupItemWrapper.setCategoryCode(categoryCode);
                categoryGroupItemWrappers.add(categoryGroupItemWrapper);
            }
            groupOmi.setCategoryGroupItems(categoryGroupItemWrappers);
        }
    }
    
    public void saveGroupItemOmi() throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        cleanValidation();
        if (ValidationHelper.isNullOrEmpty(getGroupItemPosition())) {
            addRequiredFieldException("form:groupItemPosition");
            setValidationFailed(true);
        }
        
        if (!getValidationFailed()) {
            CategoryGroupItemWrapper categoryGroupItemWrapper = getWrapperPVForDialog();
            ItemGroupOmi itemGroupOmi = null;
            if(!ValidationHelper.isNullOrEmpty(categoryGroupItemWrapper) && 
                    !ValidationHelper.isNullOrEmpty(categoryGroupItemWrapper.getItemGroupOmi()))
                itemGroupOmi = categoryGroupItemWrapper.getItemGroupOmi();
            else
                itemGroupOmi = new ItemGroupOmi();
            
            itemGroupOmi.setGroupOmi(getGroupOmiPVForDialog());
            itemGroupOmi.setPosition(getGroupItemPosition());
            DaoManager.save(itemGroupOmi, true);
            
            List<CadastralCategory> cadastralCategories = new ArrayList<CadastralCategory>();
            for(int j =0; j < this.getCategoryColumns().getTarget().size(); j++) {
                CategoryColumnWrapper wrapper = this.getCategoryColumns().getTarget().get(j);
                if (wrapper != null) {
                    if (wrapper.getSelected()) {
                        wrapper.getCategoryItemGroupOmi().setItemGroupOmi(itemGroupOmi);
                        DaoManager.save(wrapper.getCategoryItemGroupOmi(),true);
                        cadastralCategories.add(wrapper.getCategoryItemGroupOmi().getCategory());
                    }
                }
            }
            List<CadastralCategory> cadastralCategoryList = DaoManager.load(CadastralCategory.class);
            for(CadastralCategory cadastralCategory : cadastralCategoryList) {
                boolean categoryExists = cadastralCategories.stream().anyMatch(c -> c.getId().equals(cadastralCategory.getId()));
                if(!categoryExists) {
                    CategoryItemGroupOmi categoryItemGroupOmi = 
                            DaoManager.get(CategoryItemGroupOmi.class, new CriteriaAlias[]{
                                    new CriteriaAlias("itemGroupOmi", "itemGroupOmi", JoinType.INNER_JOIN)
                            }, new Criterion[]{
                                    Restrictions.and(Restrictions.eq("category", cadastralCategory)
                                            ,Restrictions.eq("itemGroupOmi.id", itemGroupOmi.getId()))
                            });
                    if(!ValidationHelper.isNullOrEmpty(categoryItemGroupOmi))
                        DaoManager.remove(categoryItemGroupOmi,true);
                }
            }
            executeJS("PF('omiGroupItemWV').hide();");
            try {
                this.onLoad();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public void handleDeleteGroupOmi(GroupOmi groupOmi)
            throws PersistenceBeanException, NumberFormatException, HibernateException, 
            InstantiationException, IllegalAccessException, IOException {
        DaoManager.remove(groupOmi, true);
        loadGroupOmis();
    }
    
    public void handleDeleteCategoryItemGroupOmi(GroupOmi groupOmi,CategoryGroupItemWrapper categoryGroupItemWrapper)
            throws PersistenceBeanException, NumberFormatException, HibernateException, 
            InstantiationException, IllegalAccessException, IOException {

        if(!ValidationHelper.isNullOrEmpty(categoryGroupItemWrapper)) {
            String categoryCode = categoryGroupItemWrapper.getCategoryCode();
            List<String> categoryCodes = Stream.of(categoryCode.split(",", -1))
                    .collect(Collectors.toList());
            for(String code : categoryCodes) {
                CategoryItemGroupOmi categoryItemGroupOmi = 
                        DaoManager.get(CategoryItemGroupOmi.class, new CriteriaAlias[]{
                                new CriteriaAlias("category", "category", JoinType.INNER_JOIN)
                        }, new Criterion[]{
                                Restrictions.and(Restrictions.eq("itemGroupOmi", categoryGroupItemWrapper.getItemGroupOmi())
                                        ,Restrictions.eq("category.codeInVisura", code.trim()))
                        });
                if (!ValidationHelper
                        .isNullOrEmpty(categoryItemGroupOmi)) {
                    DaoManager.remove(categoryItemGroupOmi, true);
                }
                
            }
            int position = categoryGroupItemWrapper.getItemGroupOmi().getPosition();
            DaoManager.remove(categoryGroupItemWrapper.getItemGroupOmi(), true);
            
            List<ItemGroupOmi> itemGroupOmis = DaoManager.load(ItemGroupOmi.class, new CriteriaAlias[]{
                    new CriteriaAlias("groupOmi", "groupOmi", JoinType.INNER_JOIN)
            }, new Criterion[]{
                    Restrictions.and(Restrictions.eq("groupOmi", groupOmi)
                            ,Restrictions.gt("position", position))
            });
            for(ItemGroupOmi itemGroupOmi : ListUtils.emptyIfNull(itemGroupOmis)) {
                int newPosition = itemGroupOmi.getPosition()-1;
                itemGroupOmi.setPosition(newPosition);
                DaoManager.save(itemGroupOmi, true);
            }
            loadGroupOmis();
        }
    }
}
