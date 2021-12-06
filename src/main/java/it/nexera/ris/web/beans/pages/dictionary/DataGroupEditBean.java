package it.nexera.ris.web.beans.pages.dictionary;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.primefaces.model.DualListModel;

import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.DataGroupInputCard;
import it.nexera.ris.persistence.beans.entities.domain.InputCard;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.DataGroup;
import it.nexera.ris.web.beans.EntityEditPageBean;

@ManagedBean
@ViewScoped
public class DataGroupEditBean extends EntityEditPageBean<DataGroup> {

    private boolean onlyView;

    private DualListModel<InputCard> inputCards;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        setOnlyView(false);
        loadInputCards();
    }

    private void loadInputCards() throws PersistenceBeanException, IllegalAccessException {
        List<DataGroupInputCard> selectedInputCards = DaoManager.load(DataGroupInputCard.class, new Criterion[]{
                Restrictions.eq("dataGroup.id", getEntity().getId())
        }, new Order[]{
                Order.asc("order")
        });
        List<InputCard> selected = selectedInputCards.stream().map(DataGroupInputCard::getInputCard)
                .collect(Collectors.toList());
        List<InputCard> notSelected;
        if (!ValidationHelper.isNullOrEmpty(selectedInputCards)) {
            notSelected = DaoManager.load(InputCard.class, new Criterion[]{
            		 Restrictions.and(
            				  Restrictions.not(Restrictions.in("id", selectedInputCards.stream()
                                      .map(DataGroupInputCard::getInputCard)
                                      .map(InputCard::getId)
                                      .collect(Collectors.toList()))),
            				  Restrictions.or(
            							Restrictions.eq("isDeleted", Boolean.FALSE),
            							Restrictions.isNull("isDeleted"))
                     )
            });
        } else {
            notSelected = DaoManager.load(InputCard.class, new Criterion[]{
       				  Restrictions.or(
       							Restrictions.eq("isDeleted", Boolean.FALSE),
       							Restrictions.isNull("isDeleted"))
       });
        }
        setInputCards(new DualListModel<>(notSelected, selected));
    }

    @Override
    public void onValidate() throws PersistenceBeanException, HibernateException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(getEntity().getName())) {
            addRequiredFieldException("form:name");
        }
    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException, NumberFormatException, IOException,
            InstantiationException, IllegalAccessException {
        getEntity().setInputCardList(new LinkedList<>());
        DaoManager.save(getEntity());
        List<DataGroupInputCard> selectedInputCards = DaoManager.load(DataGroupInputCard.class, new Criterion[]{
                Restrictions.eq("dataGroup.id", getEntity().getId())
        }, new Order[]{
                Order.asc("order")
        });
        for (int i = 0; i < getInputCards().getTarget().size(); i++) {
            InputCard card = getInputCards().getTarget().get(i);
            card.setDataGroupInputCardList(new LinkedList<>());
            DaoManager.save(card);
            DataGroupInputCard groupCard = selectedInputCards.stream()
                    .filter(c -> c.getInputCard().equals(card)).findAny().orElse(null);
            if (groupCard != null) {
                selectedInputCards.remove(groupCard);
            } else {
                groupCard = new DataGroupInputCard();
                groupCard.setInputCard(card);
                groupCard.setDataGroup(getEntity());
            }
            groupCard.setOrder(i);
            DaoManager.save(groupCard);
        }
        for (DataGroupInputCard card : selectedInputCards) {
            DaoManager.remove(card);
        }
    }

    public void cancel() {
        RedirectHelper.goTo(PageTypes.DATA_GROUP_LIST);
    }

    public DualListModel<InputCard> getInputCards() {
        return inputCards;
    }

    public void setInputCards(DualListModel<InputCard> inputCards) {
        this.inputCards = inputCards;
    }

    public boolean isOnlyView() {
        return onlyView;
    }

    public void setOnlyView(boolean onlyView) {
        this.onlyView = onlyView;
    }
}
