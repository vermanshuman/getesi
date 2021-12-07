package it.nexera.ris.web.beans.pages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DualListModel;

import it.nexera.ris.common.enums.UserCategories;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.FileHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.MessageHelper;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.CommunicationMessage;
import it.nexera.ris.persistence.beans.entities.domain.CommunicationMessageExport;
import it.nexera.ris.persistence.beans.entities.domain.Role;
import it.nexera.ris.persistence.beans.entities.domain.User;
import it.nexera.ris.web.beans.EntityEditPageBean;
import it.nexera.ris.web.beans.wrappers.logic.FileWrapper;
import lombok.Getter;
import lombok.Setter;

@ManagedBean
@ViewScoped
public class CommunicationMessageEditBean extends EntityEditPageBean<CommunicationMessage> {

    private DualListModel<Role> dualListOfRoles;

    @Getter
    @Setter
    private DualListModel<User> dualListOfUsers;

    private boolean onlyView;

    @Getter
    @Setter
    private List<FileWrapper> attachedFiles;

    @Getter
    @Setter
    private Long downloadFileIndex;

    @Getter
    @Setter
    private Long deleteFileId;

    @Getter
    @Setter
    private String test;

    @Override
    protected void preLoad() throws PersistenceBeanException {
        if ("true".equalsIgnoreCase(
                this.getRequestParameter(RedirectHelper.ONLY_VIEW))) {
            setOnlyView(true);
        }
    }

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException {
        setDualListOfRoles(new DualListModel<>(DaoManager.load(Role.class), new ArrayList<Role>()));
        setDualListOfUsers(new DualListModel<>(DaoManager.load(User.class, new Criterion[] {
                Restrictions.eq("category", UserCategories.ESTERNO)
        }), new ArrayList<>()));
        Hibernate.initialize(getEntity().getAttachedFiles());
        setAttachedFiles(new ArrayList<>());
        if (!ValidationHelper.isNullOrEmpty(getEntity().getAttachedFiles())) {
            getEntity()
                    .getAttachedFiles()
                    .stream()
                    .forEach(i -> attachedFiles.add(new FileWrapper(i.getId(), i.getFileName(), i.getFilePath())));
        }
    }

    @Override
    public void onValidate() throws PersistenceBeanException, HibernateException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(dualListOfRoles.getTarget())) {
            addRequiredFieldException("form:communicationRoles");
        }

        if (ValidationHelper.isNullOrEmpty(getEntity().getEndDate())) {
            addRequiredFieldException("form:communicationDateTo");
        }

        if (ValidationHelper.isNullOrEmpty(getEntity().getStartDate())) {
            addRequiredFieldException("form:communicationDateFrom");
        }

        if (ValidationHelper.isNullOrEmpty(getEntity().getMessage())) {
            addRequiredFieldException("form:communicationMessage");
        }

        if (ValidationHelper.isNullOrEmpty(getDualListOfUsers().getTarget())) {
            addRequiredFieldException("form:communicationUsers");
        }

        if (!ValidationHelper.isNullOrEmpty(getEntity().getStartDate()) && !ValidationHelper.isNullOrEmpty(getEntity().getEndDate())
                && !DateTimeHelper.DateLessThenMaxDate(getEntity().getStartDate(), getEntity().getEndDate())) {
            addFieldException("form:communicationDateTo", "endDateLessThanStartDate");
        }
    }
    
    public void handleFileUpload(FileUploadEvent event) throws PersistenceBeanException {

        CommunicationMessageExport newFile = new CommunicationMessageExport();
        newFile.setExportDate(new Date());
        DaoManager.save(newFile);
        File filePath = new File(newFile.generateFilePath(event.getFile().getFileName()));
        try {
            String str = FileHelper.writeFileToFolder(event.getFile().getFileName(),
                    filePath, event.getFile().getContents());
            if (!new File(str).exists()) {
                return;
            }
            LogHelper.log(log, newFile.getId() + " " + str);
        } catch (IOException e) {
            LogHelper.log(log, e);
        }
        DaoManager.save(newFile, true);

        addAttachedFile(newFile);
    }

    private void addAttachedFile(CommunicationMessageExport export) {
        if (export == null) {
            return;
        }
        if (getAttachedFiles() == null) {
            setAttachedFiles(new ArrayList<>());
        }
        if (new File(export.getFilePath()).exists()) {
            getAttachedFiles().add(new FileWrapper(export.getId(), export.getFileName(), export.getFilePath()));
        } else {
            LogHelper.log(log, "WARNING failed to attach file | no file on server: " + export.getFilePath());
        }

        attachedFiles = getAttachedFiles().stream().distinct().collect(Collectors.toList());
    }

    public void downloadFile() {
        FileWrapper wrapper = getAttachedFiles().stream().filter(w -> w.getId().equals(getDownloadFileIndex()))
                .findAny().orElse(null);
        if (!ValidationHelper.isNullOrEmpty(wrapper)) {
            if (ValidationHelper.isNullOrEmpty(wrapper.getFilePath())) {
                log.warn("File download error: attached is null");
                return;
            }

            File file = new File(wrapper.getFilePath());
            try {
                FileHelper.sendFile(wrapper.getFileName(), new FileInputStream(file), (int) file.length());
            } catch (FileNotFoundException e) {
                MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                        ResourcesHelper.getValidation("noDocumentOnServer"), "");
            }
        }
    }

    public void deleteFile() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getDeleteFileId())
                && !ValidationHelper.isNullOrEmpty(getAttachedFiles())) {
            CommunicationMessageExport export = DaoManager.get(CommunicationMessageExport.class, getDeleteFileId());
            if (FileHelper.delete(export.getFilePath())) {
                DaoManager.remove(export, true);
                getAttachedFiles().removeAll(getAttachedFiles().stream()
                        .filter(f -> f.getId().equals(getDeleteFileId())).collect(Collectors.toList()));
            }
        }
    }

    private void saveFiles(boolean transaction) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getAttachedFiles())) {
            for (FileWrapper wrapper : getAttachedFiles()) {
                CommunicationMessageExport export = DaoManager.get(CommunicationMessageExport.class, new Criterion[]{
                        Restrictions.eq("id", wrapper.getId())
                });
                export.setExportDate(new Date());
                export.setCommunicationMessage(getEntity());
                DaoManager.save(export, transaction);
            }
        }
    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException, NumberFormatException, IOException, InstantiationException, IllegalAccessException {
    	saveFiles(false);
        getEntity().setAssosiatedRoles(dualListOfRoles.getTarget());
        getEntity().setAssosiatedUsers(dualListOfUsers.getTarget());
        DaoManager.save(getEntity());
    }

    public DualListModel<Role> getDualListOfRoles() {
        return dualListOfRoles;
    }

    public void setDualListOfRoles(DualListModel<Role> dualListOfRoles) {
        this.dualListOfRoles = dualListOfRoles;
    }

    public boolean isOnlyView() {
        return onlyView;
    }

    public void setOnlyView(boolean onlyView) {
        this.onlyView = onlyView;
    }
}
