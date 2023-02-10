package it.nexera.ris.common.helpers;

import it.nexera.ris.common.enums.DocumentGenerationPlaces;
import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.common.enums.ImportSettingsType;
import it.nexera.ris.common.enums.RequestEnumTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Document;
import it.nexera.ris.persistence.beans.entities.domain.DocumentTemplate;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.persistence.beans.entities.domain.Subject;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.InstancePhases;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.TemplateDocumentModel;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.wrappers.DocumentTemplateWrapper;
import it.nexera.ris.web.beans.wrappers.Pair;
import it.nexera.ris.web.beans.wrappers.logic.UploadDocumentWrapper;
import it.nexera.ris.web.beans.wrappers.logic.UserWrapper;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.xml.serialize.XMLSerializer;
import org.bouncycastle.cms.CMSSignedData;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class GeneralFunctionsHelper extends BaseHelper {

    private static final String TEXT_XML = "xml";

    public static final String P7M = "p7m";

    private static final String ZIP = "zip";

    public static final String APPLICATION_PDF = "pdf";

    public static String prepareP7MDocument(String fileName, byte[] contents) throws IOException {
        if (P7M.equalsIgnoreCase(FileHelper.getFileExtension(fileName).replaceAll("\\.", ""))) {
            byte[] data = getData(contents);

            if (data != null) {
                String randomFileName = FileHelper.getRandomFileName("1.xml");

                return FileHelper.writeFileToFolder(randomFileName,
                        new File(FileHelper.getLocalFileDir()), data);
            }
        }
        return null;
    }

    public static String checkConservatoryEstateFormalityUpload(String uploadedFileName, byte[] contents) {
        String result = null;
        if (!ValidationHelper.isNullOrEmpty(contents) && !ValidationHelper.isNullOrEmpty(uploadedFileName)) {

            String sb = FileHelper.getDocumentSavePath() +
                    DateTimeHelper.ToFilePathString(new Date()) +
                    UserHolder.getInstance().getCurrentUser().getId() +
                    "\\";
            File filePath = new File(sb);
            boolean p7m = false;

            try {
                String fileName = FileHelper.writeFileToFolder(uploadedFileName,
                        filePath, contents);

                String newFileName = prepareP7MDocument(fileName, contents);
                if (newFileName != null) {
                    p7m = true;
                    fileName = newFileName;
                }
                org.w3c.dom.Document doc = ImportXMLHelper.prepareDocument(new File(fileName));
                if (doc == null) return "";
                result = ImportXMLHelper.getEstateFormalityChargesRegistry(doc);

                if (p7m) {
                    FileHelper.delete(fileName);
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        return result;
    }

    public static Subject checkSubjectEstateFormalityUpload(String uploadedFileName, byte[] contents, Request request,
                                                            Session session) {
        Subject result = null;
        if (!ValidationHelper.isNullOrEmpty(contents) && !ValidationHelper.isNullOrEmpty(uploadedFileName)) {

            String sb = FileHelper.getDocumentSavePath() +
                    DateTimeHelper.ToFilePathString(new Date()) +
                    UserHolder.getInstance().getCurrentUser().getId() +
                    "\\";
            File filePath = new File(sb);
            boolean p7m = false;

            try {
                String fileName = FileHelper.writeFileToFolder(uploadedFileName,
                        filePath, contents);

                String newFileName = prepareP7MDocument(fileName, contents);
                if (newFileName != null) {
                    p7m = true;
                    fileName = newFileName;
                }

                result = ImportXMLHelper.checkEstateFormalitySubject(new File(fileName), request, session);

                if (p7m) {
                    FileHelper.delete(fileName);
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        return result;
    }

    public static Subject checkSubjectPropertyUpload(String uploadedFileName, byte[] contents, Request request,
                                                     Session session) {
        Subject result = null;
        if (!ValidationHelper.isNullOrEmpty(contents) && !ValidationHelper.isNullOrEmpty(uploadedFileName)) {

            String sb = FileHelper.getDocumentSavePath() +
                    DateTimeHelper.ToFilePathString(new Date()) +
                    UserHolder.getInstance().getCurrentUser().getId() +
                    "\\";
            File filePath = new File(sb);
            boolean p7m = false;

            try {
                String fileName = FileHelper.writeFileToFolder(uploadedFileName,
                        filePath, contents);

                String newFileName = prepareP7MDocument(fileName, contents);
                if (newFileName != null) {
                    p7m = true;
                    fileName = newFileName;
                }

                result = ImportXMLHelper.checkSubject(new File(fileName), request, session);

                if (p7m) {
                    FileHelper.delete(fileName);
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        return result;
    }

    public static String saveUploadedFile(String uploadedFileName, Long userId, byte[] contents) {
        if (!ValidationHelper.isNullOrEmpty(contents)
                && !ValidationHelper.isNullOrEmpty(uploadedFileName)) {
            StringBuffer sb = new StringBuffer();

            sb.append(FileHelper.getDocumentSavePath());
            sb.append(DateTimeHelper.ToFilePathString(new Date()));
            if (!ValidationHelper.isNullOrEmpty(userId)) {
                sb.append(userId);
            } else {
                sb.append(UserHolder.getInstance().getCurrentUser().getId());
            }
            sb.append("\\");

            File filePath = new File(sb.toString());

            try {
                return FileHelper.writeFileToFolder(uploadedFileName,
                        filePath, contents);
            } catch (IOException e) {
                LogHelper.log(log, e);
            }
        }
        return null;
    }

    /**
     * @author Vlad Strunenko
     * <p>
     * <p>
     * <p>
     * Function for handle upload file
     * </p>
     * <p>
     * <p>
     * 1. in case PDF - open new tab for interaction with third part
     * application
     * </p>
     * <p>
     * 2. in case XML - parse XML using
     * {@link ImportXMLHelper XMLHelper}
     * </p>
     * <p>
     * 3. in case P7M - first extract XML using
     * {@link it.nexera.ris.common.helpers.GeneralFunctionsHelper#getData(byte[])
     * extract_XML}, then parse XML (see point 2)
     * </p>
     * <p>
     * 4. in case ZIP - first extract all files from zip-archive, then
     * parse them
     * </p>
     */
    public static UploadDocumentWrapper handleFileUpload(
            String uploadedFileName, byte[] contents, Long selectedTypeId, String documentTitle, Date documentDate,
            Document document, Request request, Session session)
            throws PersistenceBeanException, IllegalAccessException {
        String fileName = saveUploadedFile(uploadedFileName, null, contents);
        return handleFileUpload(fileName, selectedTypeId, documentTitle, documentDate,
                document, request, null, null, session, true);
    }

    public static UploadDocumentWrapper handleFileUpload(
            String fileName, Long selectedTypeId, String documentTitle, Date documentDate,
            Document document, Request request, Boolean useRequestSubject, Session session)
            throws PersistenceBeanException, IllegalAccessException {
        return handleFileUpload(fileName, selectedTypeId, documentTitle,
                documentDate, document, request, useRequestSubject, null, session, true);
    }

    public static UploadDocumentWrapper handleFileUploadWithoutOpenExternalToolPage(
            String uploadedFileName, byte[] contents, Long selectedTypeId, String documentTitle, Date documentDate,
            Document document, Request request, Long currentUserId, Session session)
            throws PersistenceBeanException, IllegalAccessException {
        String fileName = saveUploadedFile(uploadedFileName, currentUserId, contents);
        return handleFileUpload(fileName, selectedTypeId, documentTitle, documentDate,
                document, request, null, currentUserId, session, false);
    }

    private static UploadDocumentWrapper handleFileUpload(
            String fileName, Long selectedTypeId, String documentTitle, Date documentDate,
            Document document, Request request, Boolean useRequestSubject, Long userId, Session session,
            boolean openExternalToolPage)
            throws PersistenceBeanException, IllegalAccessException {
        UploadDocumentWrapper uploadDocument = new UploadDocumentWrapper();
        String uploadedFileName = FileHelper.getFileName(fileName);

        String extension = FileHelper.getFileExtension(fileName).replaceAll("\\.", "");

        if (P7M.equalsIgnoreCase(extension)) {
            try (FileInputStream inputStream = new FileInputStream(fileName)) {
                byte[] data = getData(inputStream);

                if (data != null) {
                    extension = TEXT_XML;
                    String randomFileName = FileHelper
                            .getRandomFileName("1.xml");
                    String newFileName = FileHelper.writeFileToFolder(randomFileName,
                            new File(fileName.replaceAll(uploadedFileName, "")), data);
                    FileHelper.delete(fileName);
                    fileName = newFileName;
                }
            } catch (IOException e) {
                LogHelper.log(log, e);
                return null;
            }
        }

        if (document == null) {
            String cost = "";
            if (TEXT_XML.equalsIgnoreCase(extension)) {
                Pair<String, String> titleAndCost = ImportXMLHelper.generateDocumentTitleAndCost(
                        new File(fileName), selectedTypeId, request);

                documentTitle = titleAndCost.getFirst();
                cost = titleAndCost.getSecond();
            }else {
                documentTitle = FilenameUtils.getBaseName(documentTitle);
            }


            if (!ValidationHelper.isNullOrEmpty(request)) {
                DaoManager.refresh(request);
            }

            document = new Document();
            document.setRequest(request);
            document.setTitle(documentTitle);
            document.setTypeId(selectedTypeId);
            document.setDate(documentDate);
            document.setPath(fileName);
            document.setCost(cost);
            try {
                ConnectionManager.save(document, 0L, true, session);
                if (!ValidationHelper.isNullOrEmpty(request)) {
                    if (!Hibernate.isInitialized(request.getDocumentsRequest())) {
                        request.reloadDocumentRequests(session);
                    }
                    request.getDocumentsRequest().add(document);
                }
                if (document.getId() == null) {
                    log.info("Document id after save is null");
                    log.info("Document id - " + document.getId() + ", session is connected - " + (session.isConnected()));
                    return null;
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }

        uploadDocument.setDocument(document);

        log.info("Document id - " + document.getId() + ", path - " + document.getPath());
        if (DocumentType.FORMALITY.getId().equals(selectedTypeId)) {
            StringBuilder sb = new StringBuilder();

            sb.append(FileHelper.getDocumentSavePathForThirdApp());
            sb.append(DateTimeHelper.ToFilePathString(new Date()));

            if (ValidationHelper.isNullOrEmpty(userId)) {
                try {
                    userId = UserHolder.getInstance().getCurrentUser().getId();
                } catch (IllegalStateException e) {
                    userId = 0L;
                }
            }

            sb.append(userId);
            sb.append("\\");

            String folderName = new File(ApplicationSettingsHolder.getInstance().getByKey(ImportSettingsType.FORMALITY
                    .getPathKey()).getValue()).toPath().getFileName().toString();

            sb.append(folderName);
            sb.append("\\");
            document.setPath(sb.toString() + uploadedFileName);
            document.setFolder(folderName);
            try {
                ConnectionManager.save(document, 0L, true, session);
            } catch (Exception e) {
                log.info("Error on update document");
                log.info("Document id - " + document.getId() + ", session is null == " + (session == null));
                LogHelper.log(log, e);
            }

            File filePath = new File(sb.toString());
            try {
                FileHelper.writeFileToFolder(uploadedFileName, filePath,
                        Files.readAllBytes(new File(fileName).toPath()));
            } catch (IOException e) {
                LogHelper.log(log, e);
                return null;
            }
            if (APPLICATION_PDF.equalsIgnoreCase(extension)) {
                invokeExternalTool(document, useRequestSubject, uploadedFileName, sb, openExternalToolPage);
            }
        } else if (DocumentType.CADASTRAL.getId().equals(selectedTypeId)
                && TEXT_XML.equalsIgnoreCase(extension)) {
            try {
                String fiscalCode = ImportXMLHelper.handleXML(new File(fileName), document, request,
                        useRequestSubject, session);

                uploadDocument.setFiscalCode(fiscalCode);
            } catch (PersistenceBeanException | IllegalAccessException | InstantiationException e) {
                LogHelper.log(log, e);
            }

        } else if (ZIP.equalsIgnoreCase(extension)) {
            uploadDocument = handleZipFile(fileName, selectedTypeId, documentTitle, documentDate, document,
                    session, uploadDocument);
        }

        return uploadDocument;
    }

    private static UploadDocumentWrapper handleZipFile(String fileName, Long selectedTypeId, String documentTitle,
                                                       Date documentDate, Document document, Session session,
                                                       UploadDocumentWrapper uploadDocument)
            throws PersistenceBeanException, IllegalAccessException {
        try (ZipFile zipFile = new ZipFile(fileName)) {

            Enumeration<? extends ZipEntry> entries = zipFile
                    .entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                try (InputStream stream = zipFile.getInputStream(entry)) {
                    byte[] fileByte = IOUtils.toByteArray(stream);
                    stream.close();

                    uploadDocument = handleFileUpload(entry.getName(),
                            fileByte, selectedTypeId, documentTitle, documentDate, document,
                            null, session);
                }
            }
        } catch (IOException e) {
            LogHelper.log(log, e);
        }
        return uploadDocument;
    }

    public static void invokeExternalTool(Document document, Boolean useRequestSubject, String uploadedFileName,
                                           StringBuilder sb, boolean openExternalToolPage) {

        if ((!ValidationHelper.isNullOrEmpty(useRequestSubject) && useRequestSubject) || !openExternalToolPage) {
            RedirectHelper.sendExternalUrl(
                    FileHelper.getThirdPartAppURL() + "url="
                            + sb.toString() + uploadedFileName
                            + "&docid=" + document.getId());
        } else {
            RedirectHelper.sendExternalRedirect(
                    FileHelper.getThirdPartAppURL() + "url="
                            + sb.toString() + uploadedFileName
                            + "&docid=" + document.getId());
        }
    }

    /**
     * @param p7bytes - byte array of P7M file
     * @return byte array of XML file
     *
     * <pre>
     * Function for extract XML data from P7M without checking signatures
     *         </pre>
     * @author Vlad Strunenko
     */
    public static byte[] getData(final byte[] p7bytes) {
        try {
            CMSSignedData cms = new CMSSignedData(p7bytes);

            if (cms.getSignedContent() == null) {
                LogHelper.log(log, "CMSSignedData SignedContent is null");

                return null;
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            cms.getSignedContent().write(out);

            return out.toByteArray();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return null;
    }

    public static byte[] getData(InputStream is) {
        try {
            CMSSignedData cms = new CMSSignedData(is);

            if (cms.getSignedContent() == null) {
                LogHelper.log(log, "CMSSignedData SignedContent is null");

                return null;
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            cms.getSignedContent().write(out);

            return out.toByteArray();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return null;
    }

    public static List<SelectItem> fillTemplates(DocumentGenerationPlaces place, RequestEnumTypes requestType,
                                                 Long documentId, Session session)
            throws HibernateException {
        return fillTemplates(place, requestType, documentId, false, session);
    }

    public static List<SelectItem> fillTemplates(DocumentGenerationPlaces place, RequestEnumTypes requestType,
                                                 Long documentId, boolean filter, Session session)
            throws HibernateException {
        List<Long> modelIds = new ArrayList<>();

        List<SelectItem> templates = new ArrayList<>();
        if(!filter){
            List<Criterion> criteria = new ArrayList<>();
            criteria.add(Restrictions.eq("place", place));
            criteria.add(Restrictions.isNotNull("model"));

            if (!ValidationHelper.isNullOrEmpty(requestType)) {
                criteria.add(Restrictions.eq("requestTypeId", requestType.getId()));
            }

            if (!ValidationHelper.isNullOrEmpty(documentId)) {
                criteria.add(Restrictions.eq("documentTypeId", documentId));
            }

            List<InstancePhases> instancePhases = ConnectionManager.load(
                    InstancePhases.class,
                    criteria.toArray(new Criterion[criteria.size()]), session);



            if (!ValidationHelper.isNullOrEmpty(instancePhases)) {
                for (InstancePhases ip : instancePhases) {
                    modelIds.add(ip.getModel().getId());
                }
            }

            modelIds.add(4L); // add model of certification
            modelIds.add(5L); // add model of CATASTO NAZIONALE
        }else {
            modelIds.add(5L);
            modelIds.add(2L);
        }

        if (!ValidationHelper.isNullOrEmpty(modelIds)) {
            List<Long> templateIds = new ArrayList<>();

            List<TemplateDocumentModel> templateDocumentModels = ConnectionManager
                    .load(TemplateDocumentModel.class, new Criterion[]
                            {
                                    Restrictions.in("id", modelIds.toArray())
                            }, session);

            if (!ValidationHelper.isNullOrEmpty(templateDocumentModels)) {
                for (TemplateDocumentModel tdm : templateDocumentModels) {
                    if (!ValidationHelper
                            .isNullOrEmpty(tdm.getDocumentTemplates())) {
                        for (DocumentTemplate dt : tdm.getDocumentTemplates()) {
                            if (!templateIds.contains(dt.getId())) {
                                templates.add(new SelectItem(dt.getId(),
                                        dt.getName()));
                                templateIds.add(dt.getId());
                            }
                        }
                    }
                }
            }
        }

        return templates;
    }

    public static Document saveReport(Request radiologyExamRequest, Long selectedTemplateId, UserWrapper currentUser,
            boolean printWithBozza, String customBody, String fileName, Session session) {
        
        return saveReport(radiologyExamRequest, selectedTemplateId, currentUser, printWithBozza, 
                customBody, fileName, session,DocumentType.REQUEST_REPORT.getId());
        
    }
    public static Document saveReport(Request radiologyExamRequest, Long selectedTemplateId, UserWrapper currentUser,
                                      boolean printWithBozza, String customBody, 
                                      String fileName, Session session,Long reportId) {

        if (ValidationHelper.isNullOrEmpty(selectedTemplateId)) {
            return null;
        }

        try {
            DocumentTemplate documentTemplate = ConnectionManager.get(DocumentTemplate.class, new Criterion[]{
                    Restrictions.eq("id", selectedTemplateId)
            }, session);

            if (ValidationHelper.isNullOrEmpty(documentTemplate)) {
                return null;
            }
            DocumentTemplateWrapper filledTemplate = TemplateToPdfHelper.fillTemplate(
                    documentTemplate, radiologyExamRequest, currentUser, customBody);

            if (filledTemplate == null) {
                return null;
            }

            byte[] data = null;

            if (printWithBozza) {
                String watermark = String.format("file:/%s/resources/images/watermark.png",
                        FileHelper.getLocalDir()).replaceAll("//", "/");

                data = TemplateToPdfHelper.convertAndReturnData(documentTemplate, filledTemplate,
                        watermark);
            }else if ("4".equals(documentTemplate.getModel().getStrId())) {

                String watermark = String.format("file:/%s/resources/images/certificazione_watermark_transparent.png",
                        FileHelper.getLocalDir()).replaceAll("//", "/");

                data = TemplateToPdfHelper.convertAndReturnData(documentTemplate, filledTemplate,
                        watermark);
            } else {
                data = TemplateToPdfHelper.convertAndReturnData(documentTemplate, filledTemplate,
                        null);
            }

            String pathToFile = FileHelper.writeFileToFolder(fileName + ".pdf",
                    new File(FileHelper.getApplicationProperties().getProperty("requestReportSavePath")
                            + File.separator + radiologyExamRequest.getId()),
                    data);

            Document document = null;
            List<Document> documentList = ConnectionManager.load(Document.class, new Criterion[]{
                    Restrictions.eq("typeId", reportId),
                    Restrictions.eq("request.id", radiologyExamRequest.getId()),
            }, session);

            if (!ValidationHelper.isNullOrEmpty(documentList)) {
                document = documentList.get(0);
            }

            if (document == null) document = new Document();
            document.setTitle(fileName);
            document.setPath(pathToFile);
            document.setTypeId(reportId);
            document.setDate(new Date());
            document.setRequest(ConnectionManager.get(Request.class, radiologyExamRequest.getId(), session));
            ConnectionManager.save(document, false, session);
            return document;

        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return null;
    }
    

    public static String showReport(Request radiologyExamRequest, Long selectedTemplateId, UserWrapper currentUser,
                                    boolean printWithBozza, String customBody, Session session) {
        return showReport(radiologyExamRequest, selectedTemplateId, currentUser, printWithBozza, customBody,
                FileHelper.getRandomFileName("1.pdf"), session);
    }

    public static String showReport(Request radiologyExamRequest, Long selectedTemplateId, UserWrapper currentUser,
                                    boolean printWithBozza, String customBody, String fileName, Session session) {
        boolean showErrMsg = false;

        try {
            if (!ValidationHelper.isNullOrEmpty(selectedTemplateId)) {
                DocumentTemplate documentTemplate = ConnectionManager.get(DocumentTemplate.class, new Criterion[]{
                        Restrictions.eq("id", selectedTemplateId)
                }, session);

                if (!ValidationHelper.isNullOrEmpty(documentTemplate)) {
                    DocumentTemplateWrapper filledTemplate = TemplateToPdfHelper.fillTemplate(
                            documentTemplate, radiologyExamRequest, currentUser, customBody);
                    if (filledTemplate != null) {
                        
                        String bodyContent = filledTemplate.getBodyContent().replaceAll("\\<br \\/>" + System.getProperty("line.separator") + "\\s+\\<br \\/>", "\\<br \\/>");
                        Pattern space = Pattern.compile("<br />" + System.getProperty("line.separator")+ "\\s+\\<br />");
                        while(true) {
                            Matcher matcherSpace = space.matcher(bodyContent);
                            boolean constainsSpace = matcherSpace.find();
                            if(constainsSpace) {
                                bodyContent = bodyContent.replaceAll("\\<br \\/>" + System.getProperty("line.separator") + "\\s+\\<br \\/>", "\\<br \\/>");
                            }else {
                                break;
                            }
                        }
                        String replaceText = "<div style=\"text-align: justify;\"><br />";
                        if(bodyContent.contains(replaceText)) {
                            bodyContent = bodyContent.replaceAll(replaceText, "<div style=\"text-align: justify;\">");
                        }
                        bodyContent = bodyContent.replaceFirst("\\<\\/b\\>\\s+-\\s+", "\\<\\/b\\>\\-\\&nbsp\\;");
                        bodyContent = bodyContent.replaceAll("<div style\\=\"text-align\\: center\\;\">", "<div style\\=\"text-align\\: center\\;margin\\-top\\:1px\">");
                        filledTemplate.setBodyContent(bodyContent);
                        log.info("Body content is " + filledTemplate.getBodyContent());
                        byte[] data = null;

                        if (printWithBozza) {
                            String watermark = String.format("file:/%s/resources/images/watermark.png",
                                    FileHelper.getLocalDir()).replaceAll("//", "/");

                            data = TemplateToPdfHelper.convertAndReturnData(documentTemplate, filledTemplate,
                                    watermark);
                        } else if ("4".equals(documentTemplate.getModel().getStrId())) {

                            String watermark = String.format("file:/%s/resources/images/certificazione_watermark_transparent.png",
                                    FileHelper.getLocalDir()).replaceAll("//", "/");

                            data = TemplateToPdfHelper.convertAndReturnData(documentTemplate, filledTemplate,
                                    watermark);
                        } else {
                            data = TemplateToPdfHelper.convertAndReturnData(documentTemplate, filledTemplate,
                                    null);
                        }

                        FileHelper.writeFileToFolder(fileName, new File(FileHelper.getLocalFileDir()), data);

                        RedirectHelper.sendRedirect("/File/" + fileName + "?pfdrid_c=true", true);
                    } else {
                        showErrMsg = true;
                    }
                } else {
                    showErrMsg = true;
                }
            } else {
                showErrMsg = true;
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
            showErrMsg = true;
        }

        if (showErrMsg) {
            showPdfNotGeneratedWarnMsg();
        }

        return fileName;
    }

    public static String showReport(Request radiologyExamRequest, Long selectedTemplateId, UserWrapper currentUser,
                                    boolean printWithBozza, Session session) {
        return showReport(radiologyExamRequest, selectedTemplateId, currentUser, printWithBozza, null, session);
    }

    public static void showPdfNotGeneratedWarnMsg() {
        MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_WARN,
                ResourcesHelper.getValidation("warning"),
                ResourcesHelper.getValidation("pdfDocumentWasNotGenerated"));
    }

    
    public static void main(String[] args) throws IOException {
        
        String content = new String(Files.readAllBytes(Paths.get("D:\\test\\ris_framework\\1298\\Cons.xml"))).trim().replaceAll("[^\\x00-\\x7F]", "");
       // org.jsoup.nodes.Document doc = Jsoup.parse(content, "UTF-8");   
       // doc.outputSettings().prettyPrint(false);

        System.out.println(content);
        try {
            Source xmlInput = new StreamSource(new StringReader(content));
            StringWriter stringWriter = new StringWriter();
            StreamResult xmlOutput = new StreamResult(stringWriter);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();

            transformerFactory.setAttribute("indent-number", 4);
            Transformer transformer = transformerFactory.newTransformer(); 
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(xmlInput, xmlOutput);
        } catch (Exception e) {
            throw new RuntimeException(e); // simple exception handling, please review it
        }
        
        try {
            org.w3c.dom.Document document = parseXmlFile(content);

            org.apache.xml.serialize.OutputFormat format = new org.apache.xml.serialize.OutputFormat(document);
            format.setLineWidth(65);
            format.setIndenting(true);
            format.setIndent(2);
            Writer out = new StringWriter();
            XMLSerializer serializer = new XMLSerializer(out, format);
            serializer.serialize(document);
            System.out.println(out.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        
        
    }
    
    
    static void formatXMLFile(String file) throws Exception{
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        org.w3c.dom.Document document = builder.parse(new InputSource(new InputStreamReader(new FileInputStream(
            file))));

        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.setOutputProperty(OutputKeys.METHOD, "xml");
        xformer.setOutputProperty(OutputKeys.INDENT, "yes");
        xformer.setOutputProperty("b;http://xml.apache.org/xsltd;indent-amount", "4");
        xformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        Source source = new DOMSource(document);
        Result result = new StreamResult(new File(file));
        xformer.transform(source, result);
      }
    
    
    /**
     * This function converts String XML to Document object
     * @param in - XML String
     * @return Document object
     */
    private static org.w3c.dom.Document parseXmlFile(String in) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(in));
            return db.parse(is);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String formatDoubleString(String value){
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setGroupingSeparator('.');
        DecimalFormat formatter = new DecimalFormat("###,###.##", symbols);
        return formatter.format(new BigDecimal(value));
    }

    public static String formatOMIString(String value){
        if(StringUtils.isNotBlank(value)){
            NumberFormat format = NumberFormat.getInstance(Locale.GERMAN);
            try {
                Number number = format.parse(value);
                double d = number.doubleValue();
                return InvoiceHelper.format(d);
            } catch (ParseException e) {
                e.printStackTrace();
                LogHelper.log(log, e);
            }
        }
        return value;
    }

    public static String formatStringWithDecimal(String value){
        if(StringUtils.isNotBlank(value)){
            String[] toks = value.split("\\,");
            if(toks.length > 1 && toks[1].length() == 1){
                return value + "0";
            }
        }
        return value;
    }
}


