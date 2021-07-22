package it.nexera.ris.persistence.beans.entities;

public interface ITreeNode {

    boolean isChild(IEntity parent);

    String getIcon();
}
