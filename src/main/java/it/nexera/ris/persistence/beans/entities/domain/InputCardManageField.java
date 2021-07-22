package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.enums.ManageTypeFields;
import it.nexera.ris.common.enums.ManageTypeFieldsState;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.*;

@Entity
@Table(name = "input_card_manage_field")
public class InputCardManageField extends IndexedEntity {

    private static final long serialVersionUID = 578273587030808875L;

    @ManyToOne
    @JoinColumn(name = "input_card")
    private InputCard inputCard;

    @Column(name = "manage_type_state")
    @Enumerated(EnumType.STRING)
    private ManageTypeFieldsState state;

    @Column(name = "manage_type_field")
    @Enumerated(EnumType.STRING)
    private ManageTypeFields field;

    @Column(name = "position")
    private Integer position;

    public boolean isRequired() {
        return state.equals(ManageTypeFieldsState.ENABLE_AND_MANDATORY);
    }

    public InputCard getInputCard() {
        return inputCard;
    }

    public void setInputCard(InputCard inputCard) {
        this.inputCard = inputCard;
    }

    public ManageTypeFieldsState getState() {
        return state;
    }

    public void setState(ManageTypeFieldsState state) {
        this.state = state;
    }

    public ManageTypeFields getField() {
        return field;
    }

    public void setField(ManageTypeFields field) {
        this.field = field;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }
}
