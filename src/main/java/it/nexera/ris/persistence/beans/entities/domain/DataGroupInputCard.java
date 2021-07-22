package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.DataGroup;

import javax.persistence.*;

@Entity
@Table(name = "data_group_input_card")
public class DataGroupInputCard extends IndexedEntity {

    @ManyToOne
    @JoinColumn(name = "input_card")
    private InputCard inputCard;

    @ManyToOne
    @JoinColumn(name = "data_group")
    private DataGroup dataGroup;

    @Column(name = "order_num")
    private Integer order;

    public InputCard getInputCard() {
        return inputCard;
    }

    public void setInputCard(InputCard inputCard) {
        this.inputCard = inputCard;
    }

    public DataGroup getDataGroup() {
        return dataGroup;
    }

    public void setDataGroup(DataGroup dataGroup) {
        this.dataGroup = dataGroup;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
}
