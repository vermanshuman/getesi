package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.Dictionary;
import it.nexera.ris.persistence.beans.entities.IEntity;
import it.nexera.ris.persistence.beans.entities.ITreeNode;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Area;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Office;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Sublevel;
import it.nexera.ris.web.beans.BaseEntityPageBean;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ManagedBean(name = "structureListBean")
@ViewScoped
public class StructureListBean extends BaseEntityPageBean {

    private TreeNode root;

    private TreeNode selectedNode;

    private Dictionary selectedElement;

    private IEntity selectedEntity;

    private String code;

    private String description;

    private String codeSub;

    private String descriptionSub;

    @Override
    protected void onConstruct() {
        buildTree();
    }

    public void prepareCreateDlg() {
        setCode(null);
        setDescription(null);
    }

    public void prepareCreateSubDlg() {
        setCodeSub(null);
        setDescriptionSub(null);
    }

    public void prepareEditSubDlg() {
        if (!ValidationHelper.isNullOrEmpty(getSelectedElement())) {
            setCodeSub(getSelectedElement().getCode());
            setDescriptionSub(getSelectedElement().getDescription());
        }
    }

    public void saveEditSub() {
        if (!ValidationHelper.isNullOrEmpty(getSelectedElement())) {
            try {
                getSelectedElement().setCode(getCodeSub());
                getSelectedElement().setDescription(getDescriptionSub());
                DaoManager.save(getSelectedElement(), true);
                setSelectedEntity(getSelectedElement());
                buildTree();
                executeJS("PF('editSubDlgWV').hide();");
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    public void saveSub() {
        try {
            if (getSelectedElement() instanceof Area) {
                Office office = new Office();
                office.setCode(getCodeSub());
                office.setDescription(getDescriptionSub());
                office.setArea((Area) getSelectedElement());
                DaoManager.save(office, true);
                setSelectedEntity(office);
            } else if (getSelectedElement() instanceof Office) {
                Sublevel sublevel = new Sublevel();
                sublevel.setCode(getCodeSub());
                sublevel.setDescription(getDescriptionSub());
                sublevel.setOffice((Office) getSelectedElement());
                DaoManager.save(sublevel, true);
                setSelectedEntity(sublevel);
            } else if (getSelectedElement() instanceof Sublevel) {
                Sublevel sublevel = new Sublevel();
                sublevel.setCode(getCodeSub());
                sublevel.setDescription(getDescriptionSub());
                sublevel.setPreviousSublevel((Sublevel) getSelectedElement());
                DaoManager.save(sublevel, true);
                setSelectedEntity(sublevel);
            }
            buildTree();
            executeJS("PF('addSubDlgWV').hide();");
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void saveArea() {
        try {
            Area area = new Area();
            area.setCode(getCode());
            area.setDescription(getDescription());
            DaoManager.save(area, true);
            buildTree();
            executeJS("PF('addAreaDlgWV').hide();");
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void onNodeSelect(NodeSelectEvent event) {
        setSelectedElement((Dictionary) event.getTreeNode().getData());
    }

    public void buildTree() {
        if (this.getRoot() == null) {
            setRoot(new DefaultTreeNode("root", null));
        } else {
            this.getRoot().getChildren().clear();
        }
        setSelectedNode(null);
        setSelectedElement(null);

        try {
            List<Area> areas = DaoManager.load(Area.class,
                    new Criterion[]
                            {
                                    Restrictions.or(
                                            Restrictions.isNull("externalBrexa"),
                                            Restrictions.eq("externalBrexa", Boolean.FALSE)
                                            )
                            });
            List<Office> offices = DaoManager.load(Office.class);
            List<Sublevel> sublevels = new CopyOnWriteArrayList<>(DaoManager.load(Sublevel.class));

            for (Area area : areas) {
                DefaultTreeNode nodeA = new DefaultTreeNode(area, getRoot());

                for (Office office : offices) {
                    if (office.isChild(area)) {
                        createNoteWithChildren(office, sublevels, nodeA);
                    }
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void removeNode() {
        if (!ValidationHelper.isNullOrEmpty(getSelectedElement())) {
            try {
                DaoManager.remove(getSelectedElement(), true);
                buildTree();
            } catch (PersistenceBeanException e) {
                LogHelper.log(log, e);
            }
        }
    }

    public void onNodeCollapse(NodeCollapseEvent event) {
        event.getTreeNode().setExpanded(false);
        changeChildrenState(event.getTreeNode().getChildren(), false);

        TreeNode parent = event.getTreeNode().getParent();

        while (parent != null) {
            parent.setExpanded(true);
            parent = parent.getParent();
        }
    }

    private void changeChildrenState(List<TreeNode> children, boolean expand) {
        for (TreeNode node : children) {
            changeChildrenState(node.getChildren(), expand);

            node.setExpanded(expand);
        }
    }

    public void onNodeExpand(NodeExpandEvent event) {
        event.getTreeNode().setExpanded(true);
    }

    private <T extends ITreeNode & IEntity> void createNoteWithChildren(IEntity parent, List<T> elements, DefaultTreeNode parentNode) {
        DefaultTreeNode node = new DefaultTreeNode(parent, parentNode);
        elements.remove(parent);

        if (getSelectedEntity() != null
                && getSelectedEntity().getClass().equals(parent.getClass())
                && getSelectedEntity().getId().equals(parent.getId())) {
            TreeNode parentTreeNode = node.getParent();
            while (parentTreeNode != null) {
                parentTreeNode.setExpanded(true);
                parentTreeNode = parentTreeNode.getParent();
            }
            if (node.getChildCount() != 0) {
                node.setExpanded(true);
            }
            node.setSelected(true);
            setSelectedElement((Dictionary) parent);
            setSelectedNode(node);
        }

        if (hasChild(parent, elements)) {
            elements.stream().filter(e -> e.isChild(parent)).forEach(e -> createNoteWithChildren(e, elements, node));
        }
    }

    private boolean hasChild(IEntity parent, List<? extends ITreeNode> elements) {
        return elements.stream().anyMatch(n -> n.isChild(parent));
    }

    public String getDeleteMessage() {
        if (!ValidationHelper.isNullOrEmpty(getSelectedElement())) {
            return String.format(ResourcesHelper.getString("deleteMessage"),
                    getSelectedElement().getDescription());
        }

        return "";
    }

    public TreeNode getRoot() {
        return root;
    }

    public void setRoot(TreeNode root) {
        this.root = root;
    }

    public TreeNode getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;
    }

    public Dictionary getSelectedElement() {
        return selectedElement;
    }

    public void setSelectedElement(Dictionary selectedElement) {
        this.selectedElement = selectedElement;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCodeSub() {
        return codeSub;
    }

    public String getDescriptionSub() {
        return descriptionSub;
    }

    public void setCodeSub(String codeSub) {
        this.codeSub = codeSub;
    }

    public void setDescriptionSub(String descriptionSub) {
        this.descriptionSub = descriptionSub;
    }

    public IEntity getSelectedEntity() {
        return selectedEntity;
    }

    public void setSelectedEntity(IEntity selectedEntity) {
        this.selectedEntity = selectedEntity;
    }
}
