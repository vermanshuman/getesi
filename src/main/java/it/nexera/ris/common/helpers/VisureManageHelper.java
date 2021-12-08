package it.nexera.ris.common.helpers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.enums.ImportSettingsType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.VisureRTF;
import it.nexera.ris.persistence.beans.entities.domain.VisureRTFUpload;
import it.nexera.ris.settings.ApplicationSettingsHolder;

public class VisureManageHelper {
    public static final Log log = LogFactory.getLog(VisureManageHelper.class);
    
    public static void downloadVisureRTF(Long visureRTFId) throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(visureRTFId)) {
            VisureRTF visureRTFForDownload = DaoManager.get(VisureRTF.class, new Criterion[]{
                    Restrictions.eq("id", visureRTFId)});

            String dir = ApplicationSettingsHolder.getInstance().getByKey(ImportSettingsType.VISURE_RTF
                    .getPathFilesServer()).getValue();


            if (!ValidationHelper.isNullOrEmpty(dir)) {
                String path = dir + File.separator + visureRTFForDownload.getNumDir() + File.separator
                        + visureRTFForDownload.getNumText() + ".rtf";
                File file = new File(path);
                try {
                    FileHelper.sendFile(file.getName(),
                            new FileInputStream(file), (int) file.length());
                } catch (FileNotFoundException e) {
                    FacesMessage msg = new FacesMessage(
                            ResourcesHelper.getValidation("noDocumentOnServer"),
                            "");
                    msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                }
            }
        }
    }

    public static void downloadVisureRTFasPDF(Long visureRTFId) throws IllegalAccessException, PersistenceBeanException,
            InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(visureRTFId)) {
            VisureRTF visureRTFForDownload = DaoManager.get(VisureRTF.class, new Criterion[]{
                    Restrictions.eq("id", visureRTFId)});

            String dir = ApplicationSettingsHolder.getInstance().getByKey(ImportSettingsType.VISURE_RTF
                    .getPathFilesServer()).getValue();


            if (!ValidationHelper.isNullOrEmpty(dir)) {
                String path = dir + File.separator + visureRTFForDownload.getNumDir() + File.separator
                        + visureRTFForDownload.getNumText() + ".rtf";

                File file = new File(path);
                try {
                    if (file.exists()) {
                        String sofficeCommand =
                                ApplicationSettingsHolder.getInstance().getByKey(
                                        ApplicationSettingsKeys.SOFFICE_COMMAND).getValue().trim();
                        String tempDir =
                                ApplicationSettingsHolder.getInstance().getByKey(
                                        ApplicationSettingsKeys.SOFFICE_TEMP_DIR_PREFIX).getValue().trim();

                        tempDir += UUID.randomUUID();

                        FileUtils.forceMkdir(new File(tempDir));

                        String headeredRtfPath = tempDir + File.separator + file.getName();

                        String headerImageUri = ApplicationSettingsHolder.getInstance().getByKey(
                                ApplicationSettingsKeys.DOCUMENT_CONVERSION_HEADER_IMAGE).getValue();
                        String footerImageUri = ApplicationSettingsHolder.getInstance().getByKey(
                                ApplicationSettingsKeys.DOCUMENT_CONVERSION_FOOTER_IMAGE).getValue();

                        if (!RTFHelper.makeRtfWithHeaderAndFooter
                                (file,
                                        new File(headeredRtfPath),
                                        ValidationHelper.isNullOrEmpty(headerImageUri) ? null :
                                                new URL(headerImageUri),
                                        2100, 1000,
                                        ValidationHelper.isNullOrEmpty(footerImageUri) ? null :
                                                new URL(footerImageUri),
                                        5700, 1300))
                            throw new Exception("Could not make headered RTF");

                        sendFileAndDeleteDirectory(visureRTFForDownload, file, sofficeCommand, tempDir, headeredRtfPath);
                    } else
                        throw new FileNotFoundException();

                } catch (FileNotFoundException e) {
                    FacesMessage msg = new FacesMessage(
                            ResourcesHelper.getValidation("noDocumentOnServerMsg"),
                            "");
                    msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                    FacesContext.getCurrentInstance().addMessage(null, msg);

                    e.printStackTrace();
                } catch (Exception e) {
                    FacesMessage msg = new FacesMessage(
                            ResourcesHelper.getValidation("errorOnServerMsg"),
                            "");
                    msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                    FacesContext.getCurrentInstance().addMessage(null, msg);

                    e.printStackTrace();
                }
            } else {
                FacesMessage msg = new FacesMessage(
                        ResourcesHelper.getValidation("noDirectorySpecifiedOnServerMsg"),
                        "");
                msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                FacesContext.getCurrentInstance().addMessage(null, msg);
            }
        }
    }

    private static void sendFileAndDeleteDirectory(VisureRTF visureRTFForDownload, File file, String sofficeCommand,
                                                   String tempDir, String headeredRtfPath)
            throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec(new String[]
                {sofficeCommand,
                        "--headless",
                        "--convert-to",
                        "pdf",
                        "--outdir",
                        tempDir,
                        headeredRtfPath
                });

        p.waitFor();

        String newPath = tempDir + File.separator + file.getName().replaceFirst(".rtf", ".pdf");
        File convertedFile = new File(newPath);
        String pdfFileName = visureRTFForDownload.getFullName();
        if (!ValidationHelper.isNullOrEmpty(visureRTFForDownload.getBirthDate())) {
            String birthDateString = DateTimeHelper.ToMySqlStringWithSeconds(visureRTFForDownload.getBirthDate());
            java.util.Date formattedBirthDat = DateTimeHelper.fromString(birthDateString, DateTimeHelper.getMySQLDateTimePattern());
            pdfFileName = pdfFileName + "-" + DateTimeHelper.toFormatedString(formattedBirthDat, DateTimeHelper.getXmlDatePattert());
        } else {
            pdfFileName = pdfFileName + "-" + visureRTFForDownload.getFiscalCodeVat();
        }
        pdfFileName = pdfFileName + "-Cons-" + visureRTFForDownload.getLandChargesRegistry().getName();
        FileHelper.sendFile(pdfFileName.trim() + ".pdf",
                new ByteArrayInputStream(FileUtils.readFileToByteArray(convertedFile)),
                (int) convertedFile.length());

        FileUtils.deleteDirectory(new File(tempDir));
    }

    public static void downloadVisureRTFUploadedasPDF(VisureRTF downloadVisureUploadedRTF, Long visureRTFId)
            throws IllegalAccessException, PersistenceBeanException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(visureRTFId) && !ValidationHelper.isNullOrEmpty(downloadVisureUploadedRTF)) {
            VisureRTFUpload visureRTFUpload = DaoManager.get(VisureRTFUpload.class, visureRTFId);

            String dir = ApplicationSettingsHolder.getInstance().getByKey(ImportSettingsType.VISURE_RTF
                    .getPathFilesServer()).getValue();

            if (!ValidationHelper.isNullOrEmpty(dir)) {

                String path = visureRTFUpload.getPath();
                File file = new File(path);
                try {
                    if (file.exists()) {
                        String sofficeCommand =
                                ApplicationSettingsHolder.getInstance().getByKey(
                                        ApplicationSettingsKeys.SOFFICE_COMMAND).getValue().trim();
                        String tempDir =
                                ApplicationSettingsHolder.getInstance().getByKey(
                                        ApplicationSettingsKeys.SOFFICE_TEMP_DIR_PREFIX).getValue().trim();

                        tempDir += UUID.randomUUID();

                        FileUtils.forceMkdir(new File(tempDir));

                        String headeredRtfPath = tempDir + File.separator + file.getName();

                        String headerImageUri = ApplicationSettingsHolder.getInstance().getByKey(
                                ApplicationSettingsKeys.DOCUMENT_CONVERSION_HEADER_IMAGE).getValue();
                        String footerImageUri = ApplicationSettingsHolder.getInstance().getByKey(
                                ApplicationSettingsKeys.DOCUMENT_CONVERSION_FOOTER_IMAGE).getValue();

                        if (!RTFHelper.makeRtfWithHeaderAndFooter
                                (file,
                                        new File(headeredRtfPath),
                                        ValidationHelper.isNullOrEmpty(headerImageUri) ? null :
                                                new URL(headerImageUri),
                                        2100, 1000,
                                        ValidationHelper.isNullOrEmpty(footerImageUri) ? null :
                                                new URL(footerImageUri),
                                        5700, 1300))
                            throw new Exception("Could not make headered RTF");

                        sendFileAndDeleteDirectory(downloadVisureUploadedRTF, file, sofficeCommand, tempDir, headeredRtfPath);
                    } else
                        throw new FileNotFoundException();

                } catch (FileNotFoundException e) {
                    FacesMessage msg = new FacesMessage(
                            ResourcesHelper.getValidation("noDocumentOnServerMsg"),
                            "");
                    msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                    FacesContext.getCurrentInstance().addMessage(null, msg);

                    e.printStackTrace();
                } catch (Exception e) {
                    FacesMessage msg = new FacesMessage(
                            ResourcesHelper.getValidation("errorOnServerMsg"),
                            "");
                    msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                    FacesContext.getCurrentInstance().addMessage(null, msg);

                    e.printStackTrace();
                }
            } else {
                FacesMessage msg = new FacesMessage(
                        ResourcesHelper.getValidation("noDirectorySpecifiedOnServerMsg"),
                        "");
                msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                FacesContext.getCurrentInstance().addMessage(null, msg);
            }
        }
    }
    
    public static void sendPDFfromXLSFile(File file, String sofficeCommand,
            String tempDir, String xlsFile) 
            throws IOException, InterruptedException {
        
        Process p = Runtime.getRuntime().exec(new String[] { sofficeCommand, "--headless", 
                "--convert-to", "pdf","--outdir", tempDir, xlsFile });
        p.waitFor();

        String newPath = tempDir + File.separator + file.getName().replaceFirst(".xls", ".pdf");
        FileHelper.delete(xlsFile);
        log.info("Pdf file path " + newPath);
        File convertedFile = new File(newPath);
        String pdfFileName = convertedFile.getName();
        log.info("pdfFileName " + pdfFileName);
        
        FileHelper.sendFile(pdfFileName.trim(),
                new ByteArrayInputStream(FileUtils.readFileToByteArray(convertedFile)),
                (int) convertedFile.length());
        
        FileUtils.deleteDirectory(new File(tempDir));
    }
}

