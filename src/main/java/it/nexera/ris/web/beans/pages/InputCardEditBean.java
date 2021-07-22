package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.ManageTypeFields;
import it.nexera.ris.common.enums.ManageTypeFieldsState;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.InputCard;
import it.nexera.ris.persistence.beans.entities.domain.InputCardManageField;
import it.nexera.ris.web.beans.EntityEditPageBean;
import it.nexera.ris.web.beans.wrappers.logic.InputCardFieldWrapper;
import org.hibernate.HibernateException;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ManagedBean(name = "inputCardEditBean")
@ViewScoped
public class InputCardEditBean extends EntityEditPageBean<InputCard> implements Serializable {

    private static final long serialVersionUID = 7629206129013622252L;

    private List<InputCardFieldWrapper> fields;

    private List<SelectItem> stateValues;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        loadFields();
        setStateValues(ComboboxHelper.fillList(ManageTypeFieldsState.class, false, false));
    }

    private void loadFields() {
        List<InputCardFieldWrapper> fields = new LinkedList<>();
        for (ManageTypeFields field : ManageTypeFields.values()) {
            InputCardManageField inputCardManageField = null;
            if (!getEntity().isNew()) {
                inputCardManageField = getEntity().getFields()
                        .stream().filter(f -> f.getField().equals(field)).findAny().orElse(null);
            }
            InputCardFieldWrapper wrapper;
            if (inputCardManageField != null) {
                wrapper = new InputCardFieldWrapper(inputCardManageField);
            } else {
                wrapper = new InputCardFieldWrapper(field);
            }
            fields.add(wrapper);
        }
        setFields(fields);
    }

    @Override
    public void onValidate() throws PersistenceBeanException, HibernateException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(getEntity().getName())) {
            addRequiredFieldException("name");
        }
        if (ValidationHelper.isNullOrEmpty(getFields().stream()
                .filter(f -> f.getSelected() != null && f.getSelected()).collect(Collectors.toList()))) {
            addException("inputCardValidationFailed");
        }
        List<InputCardManageField> selected = getFields().stream().filter(w -> w.getSelected() != null
                && w.getSelected()).map(InputCardFieldWrapper::getField).collect(Collectors.toList());
        IntStream.range(0, selected.size()).forEach(i -> {
            InputCardManageField checking = selected.get(i);
            if (checking.getPosition() != null) {
                if (!checking.getField().getEnumPropsWrapper().isHasManyFields()) {
                    if (IntStream.range(i + 1, selected.size()).mapToObj(selected::get)
                            .anyMatch(field -> field.getField() != checking.getField()
                                    && field.getPosition() != null
                                    && field.getPosition().equals(checking.getPosition()))) {
                        addException(String.format(ResourcesHelper.getValidation("duplicatePosition"),
                                checking.getPosition()), true);
                    }
                } else {
                    int line = (int) Math.floor(checking.getPosition() / 4);
                    for (InputCardManageField field : selected) {
                        if (field.getField() != checking.getField()
                                && field.getPosition() != null
                                && ((int) Math.floor(field.getPosition() / 4)) == line) {
                            addException(String.format(ResourcesHelper.getValidation("lineOccupied"),
                                    line + 1, checking.getField().toString()), true);
                            break;
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException, NumberFormatException,
            IOException, InstantiationException, IllegalAccessException {
        DaoManager.save(getEntity());
        for (InputCardFieldWrapper wrapper : getFields()) {
            if (wrapper != null) {
                if (wrapper.getSelected()) {
                    wrapper.getField().setInputCard(getEntity());
                    DaoManager.save(wrapper.getField());
                } else if (!wrapper.getField().isNew()) {
                    DaoManager.remove(wrapper.getField());
                }
            }
        }
    }

    public List<InputCardFieldWrapper> getFields() {
        return fields;
    }

    public void setFields(List<InputCardFieldWrapper> fields) {
        this.fields = fields;
    }

    public List<SelectItem> getStateValues() {
        return stateValues;
    }

    public void setStateValues(List<SelectItem> stateValues) {
        this.stateValues = stateValues;
    }
}
