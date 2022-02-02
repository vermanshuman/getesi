package it.nexera.ris.common.xml.wrappers.importXLSX;

import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.entities.domain.Document;
import it.nexera.ris.persistence.beans.entities.domain.Formality;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DocumentXLSXWrapper {

    private String title;

    private String folder;

    private String type;

    private List<FormalityXLSXWrapper> formalityList;

    public List<Document> getEntity(Session session) {
        List<Document> list = new ArrayList<>();
        if(!ValidationHelper.isNullOrEmpty(getTitle()) || !ValidationHelper.isNullOrEmpty(getFolder())) {
            list = ConnectionManager.load(Document.class, new CriteriaAlias[]{
                    new CriteriaAlias("formality", "f", JoinType.LEFT_OUTER_JOIN),
                    new CriteriaAlias("f.sectionC", "sc", JoinType.LEFT_OUTER_JOIN)
            }, new Criterion[]{
                    Restrictions.or(
                            Restrictions.eq("duplicate", false),
                            Restrictions.isNull("duplicate")
                    ),
                    Restrictions.ilike("path", getTitle(), MatchMode.ANYWHERE),
                    Restrictions.eq("typeId", DocumentType.FORMALITY.getId()),
                   /* Restrictions.or(
                            Restrictions.isNull("f.id"),
                            Restrictions.isNull("sc.id")
                    )*/
            }, session);

            additionalCheck(list);
        }
        return list;
    }

    public DocumentXLSXWrapper(String title) {
        this.title = title;
        formalityList = new LinkedList<>();
    }

    public DocumentXLSXWrapper(String title, String folder) {
        this.title = title;
        this.folder = folder;
        formalityList = new LinkedList<>();
    }

    private void additionalCheck(List<Document> list) {
        if ("VISURA CARTACEA".equalsIgnoreCase(getType())) {
            for (Document doc : list) {
                doc.setTypeId(9L);
                if (!ValidationHelper.isNullOrEmpty(getFormalityList())) {
                    doc.setTitle(getType() + "_" + DateTimeHelper.toString(getFormalityList().get(0).getPresentationDate())
                            + "_" + getFormalityList().get(0).getConservatory());
                }
            }
        }
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<FormalityXLSXWrapper> getFormalityList() {
        return formalityList;
    }

    public void setFormalityList(List<FormalityXLSXWrapper> formalityList) {
        this.formalityList = formalityList;
    }
}
