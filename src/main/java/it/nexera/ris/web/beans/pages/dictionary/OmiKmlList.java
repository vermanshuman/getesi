package it.nexera.ris.web.beans.pages.dictionary;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.helpers.FileHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.BaseEntityPageBean;
import it.nexera.ris.web.beans.wrappers.logic.FileWrapper;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ManagedBean
@ViewScoped
public class OmiKmlList extends BaseEntityPageBean {

    private List<FileWrapper> files;

    @Override
    protected void onConstruct() {
        this.files = new LinkedList<>();
        String path = ApplicationSettingsHolder.getInstance()
                .getByKey(ApplicationSettingsKeys.OMI_KML_FILES).getValue();
        if (!ValidationHelper.isNullOrEmpty(path)) {
            File[] kmlFilesInFolder = new File(path).listFiles();
            if (kmlFilesInFolder == null) {
                return;
            }
            for (int i = 0; i < kmlFilesInFolder.length; i++) {
                File file = kmlFilesInFolder[i];
                FileWrapper fileWrapper = new FileWrapper((long) i, file.getName(), file.getPath());
                this.files.add(fileWrapper);
            }
        }
    }

    public StreamedContent downloadFile(Long fileId) {
        FileWrapper fileWrapper = getFiles().get(Math.toIntExact(fileId));
        byte[] content = FileHelper.loadContentByPath(fileWrapper.getFilePath());
        if (ValidationHelper.isNullOrEmpty(content)) {
            return null;
        }
        return new DefaultStreamedContent(new ByteArrayInputStream(content),
                FileHelper.getFileExtension(fileWrapper.getFilePath()), fileWrapper.getFileName());
    }
    
}
