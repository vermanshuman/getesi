package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.TemplateDocumentModel;

import javax.persistence.*;

@Entity
@Table(name = "document_template")
public class DocumentTemplate extends IndexedEntity {
    private static final long serialVersionUID = 7057936577829371954L;

    @ManyToOne
    @JoinColumn(name = "model_id")
    private TemplateDocumentModel model;

    @Column
    private String name;

    @Column
    private Boolean header;

    @Column
    private Boolean footer;

    @Column(name = "header_content", columnDefinition = "LONGTEXT")
    private String headerContent;

    @Column(name = "body_content", columnDefinition = "LONGTEXT")
    private String bodyContent;

    @Column(name = "footer_content", columnDefinition = "LONGTEXT")
    private String footerContent;

    @Column
    private Long height;

    @Column
    private Long headerHeight;

    @Column
    private Long footerHeight;

    @Column
    private Long width;

    @Column(name = "margin_top")
    private Long marginTop;

    @Column(name = "margin_bottom")
    private Long marginBottom;

    @Column(name = "margin_left")
    private Long marginLeft;

    @Column(name = "margin_right")
    private Long marginRight;

    public String getEscapedBodyContent() {
        return "<span style=\"font-size:14px;\"><span style=\"font-family:Courier New,Courier,monospace;\">" +
                bodyContent.replaceAll("style\\s*=\\s*\"\\s*font-size\\s*:\\s*\\d+\\s*px\\s*;\\s*\"", "")
                + "</span></span>";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getHeader() {
        return header;
    }

    public void setHeader(Boolean header) {
        this.header = header;
    }

    public Boolean getFooter() {
        return footer;
    }

    public void setFooter(Boolean footer) {
        this.footer = footer;
    }

    public String getHeaderContent() {
        return headerContent;
    }

    public void setHeaderContent(String headerContent) {
        this.headerContent = headerContent;
    }

    public String getBodyContent() {
        return bodyContent;
    }

    public void setBodyContent(String bodyContent) {
        this.bodyContent = bodyContent;
    }

    public String getFooterContent() {
        return footerContent;
    }

    public void setFooterContent(String footerContent) {
        this.footerContent = footerContent;
    }

    public Long getHeight() {
        return height;
    }

    public void setHeight(Long height) {
        this.height = height;
    }

    public Long getWidth() {
        return width;
    }

    public void setWidth(Long width) {
        this.width = width;
    }

    public Long getMarginTop() {
        return marginTop == null ? 0L : marginTop;
    }

    public void setMarginTop(Long marginTop) {
        this.marginTop = marginTop;
    }

    public Long getMarginBottom() {
        return marginBottom == null ? 0L : marginBottom;
    }

    public void setMarginBottom(Long marginBottom) {
        this.marginBottom = marginBottom;
    }

    public Long getMarginLeft() {
        return marginLeft == null ? 0L : marginLeft;
    }

    public void setMarginLeft(Long marginLeft) {
        this.marginLeft = marginLeft;
    }

    public Long getMarginRight() {
        return marginRight == null ? 0L : marginRight;
    }

    public void setMarginRight(Long marginRight) {
        this.marginRight = marginRight;
    }

    public TemplateDocumentModel getModel() {
        return model;
    }

    public void setModel(TemplateDocumentModel model) {
        this.model = model;
    }

    public Long getFooterHeight() {
        return footerHeight;
    }

    public void setFooterHeight(Long footerHeight) {
        this.footerHeight = footerHeight;
    }

    public Long getHeaderHeight() {
        return headerHeight;
    }

    public void setHeaderHeight(Long headerHeight) {
        this.headerHeight = headerHeight;
    }
}
