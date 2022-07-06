package it.nexera.ris.common.helpers;

import it.nexera.ris.common.xml.wrappers.SelectItemWrapper;
import it.nexera.ris.persistence.beans.entities.Entity;
import it.nexera.ris.persistence.beans.entities.domain.Client;

import javax.faces.model.SelectItem;
import java.util.List;

/**
 * SelectItemHelper
 * Used to fill drop down lists
 */
public class SelectItemHelper {
    public static SelectItem getNotSelected() {
        StringBuilder sb = new StringBuilder();
        sb.append("- ");
        sb.append(ResourcesHelper.getString("notSelected"));
        sb.append(" -");
        return new SelectItem("", sb.toString());
    }

    public static SelectItem getVirtualEntity() {
        StringBuilder sb = new StringBuilder();
        sb.append("- ");
        sb.append(ResourcesHelper.getString("notSelected"));
        sb.append(" -");

        return new SelectItem("-1", sb.toString());
    }

    public static SelectItem getAllElement() {
        StringBuilder sb = new StringBuilder();
        sb.append("- ");
        sb.append(ResourcesHelper.getString("all"));
        sb.append(" -");
        return new SelectItem(0l, sb.toString());
    }

    public static SelectItem getNoneElement() {
        StringBuilder sb = new StringBuilder();
        sb.append("- ");
        sb.append(ResourcesHelper.getString("none"));
        sb.append(" -");
        return new SelectItem("", sb.toString());
    }

    public static SelectItem getUnlimitedElement() {
        StringBuilder sb = new StringBuilder();
        sb.append(ResourcesHelper.getString("unlimited"));
        return new SelectItem("-1", sb.toString());
    }

    public static SelectItem getFreeElement() {
        StringBuilder sb = new StringBuilder();
        sb.append(ResourcesHelper.getString("free"));
        return new SelectItem("-1", sb.toString());
    }

    public static <T extends Entity> SelectItemWrapper<T> getNotSelectedWrapper() {
        StringBuilder sb = new StringBuilder();
        sb.append("- ");
        sb.append(ResourcesHelper.getString("notSelected"));
        sb.append(" -");
        return new SelectItemWrapper<>(0L, sb.toString());
    }

    public static void addItemToListIfItIsNotInIt(List<SelectItem> items, Entity item) {

    	if (items.stream().noneMatch(x -> !ValidationHelper.isNullOrEmpty(x.getValue()) &&
                !ValidationHelper.isNullOrEmpty(item.getId()) && x.getValue().equals(item.getId()))) {
    		items.add(new SelectItem(item.getId(), item.toString()));
    	}
    }

    public static void addItemsToListIfItIsNotInIt(List<SelectItem> items, List<Client> clients) {
    	clients.stream().forEach(x -> {
    		if (items.stream().noneMatch(i -> i.getValue().equals(x.getId()))) {
    			items.add(new SelectItem(x.getId(), x.toString()));
    		}
    	});
    }
}
