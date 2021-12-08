package it.nexera.ris.common.helpers;

import java.awt.Dimension;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.InvalidParameterException;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import it.nexera.ris.common.enums.DocumentType;
import it.nexera.ris.common.utils.CustomClassLoader;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Document;
import it.nexera.ris.persistence.beans.entities.domain.WLGInbox;

public class PrintPDFHelper extends BaseHelper {

    private static final String P7M = "p7m";

    public static String generatePDFOnEmail(WLGInbox email, String fileName) {
        try {
            return MailHelper.fillPDF(email, readWorkingListFile(fileName, "Email"));
        } catch (IllegalAccessException | InvocationTargetException e) {
            LogHelper.log(log, e);
        }
        return null;
    }

    public static void generatePDFOnEmail(Long emailId) {
        try {
            WLGInbox email = DaoManager.get(WLGInbox.class, emailId);

            String body = MailHelper.fillPDF(email,
                    readWorkingListFile("body", "Email"));

            FileHelper.sendFile("Email.pdf",
                    convertToPDF(null, body, null, null));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static void generatePDFOnDocument(Long documentId, String projectUrl) {
        try {
            if (documentId != null) {
                Document document = DaoManager.get(Document.class, documentId);
                DocumentType type = DocumentType.getById(document.getTypeId());

                if (type != null && document.getPath() != null) {
                    String fileName = document.getPath();
                    String extension = "";

                    int i = fileName.lastIndexOf('.');

                    if (i > 0) {
                        extension = fileName.substring(i + 1);
                    }

                    if (P7M.equalsIgnoreCase(extension)) {
                        byte[] data = GeneralFunctionsHelper.getData(
                                FileHelper.loadContentByPath(fileName));

                        if (data != null) {
                            String randomFileName = FileHelper
                                    .getRandomFileName("1.xml");

                            fileName = FileHelper.writeFileToFolder(
                                    randomFileName,
                                    new File(FileHelper.getLocalFileDir()),
                                    data);
                        }
                    }

                    convertXML(new File(fileName), projectUrl, type, document.getTitle());
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static byte[] generateAndGetPDFOnDocument(Long documentId) {
        try {
            if (documentId != null) {
                Document document = DaoManager.get(Document.class, documentId);
                DocumentType type = DocumentType.getById(document.getTypeId());
                if (type != null && document.getPath() != null) {
                    String fileName = document.getPath();
                    String extension = "";
                    int i = fileName.lastIndexOf('.');
                    if (i > 0)
                        extension = fileName.substring(i + 1);
                    if (P7M.equalsIgnoreCase(extension)) {
                        byte[] data = GeneralFunctionsHelper.getData(
                                FileHelper.loadContentByPath(fileName));
                        if (data != null) {
                            String randomFileName = FileHelper
                                    .getRandomFileName("1.xml");
                            fileName = FileHelper.writeFileToFolder(
                                    randomFileName,
                                    new File(FileHelper.getLocalFileDir()),
                                    data);
                        }
                    }
                    return convertXMLAndGet(new File(fileName), null, type, document.getTitle());
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return null;
    }

    private static void convertXML(File file, String projectUrl,
                                   DocumentType type, String documentTitle) {
        String body = readWorkingListFile("body", type.getFolderName());
        String header = replaceLocaleImagesUrl(
                readWorkingListFile("header", type.getFolderName()),
                projectUrl);
        String footer = readWorkingListFile("footer", type.getFolderName());

        try {
            header = ImportXMLHelper.handlXMLTagsForPDF(file, header, DaoManager.getSession());
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        try {
            body = ImportXMLHelper.handlXMLTagsForPDF(file, body, DaoManager.getSession());
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        try {
            footer = ImportXMLHelper.handlXMLTagsForPDF(file, footer, DaoManager.getSession());
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        try {
            FileHelper.sendFile(documentTitle + ".pdf",
                    convertToPDF(header, body, footer, type));
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public static byte[] convertXMLAndGet(File file, String projectUrl, DocumentType type, String documentTitle) {
        String body = readWorkingListFile("body", type.getFolderName());
        String header = replaceLocaleImagesUrl(readWorkingListFile("header", type.getFolderName()), projectUrl);
        String footer = readWorkingListFile("footer", type.getFolderName());
        try {
            header = ImportXMLHelper.handlXMLTagsForPDF(file, header, DaoManager.getSession());
            body = ImportXMLHelper.handlXMLTagsForPDF(file, body, DaoManager.getSession());
            footer = ImportXMLHelper.handlXMLTagsForPDF(file, footer, DaoManager.getSession());
            return convertToPDF(header, body, footer, type);
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return null;
    }

    public static String readWorkingListFile(String filename, String folder) {
        BufferedReader br = null;

        StringBuffer sb = new StringBuffer();

        try {
            String currentLine = null;

            filename = (new File(FileHelper.getRealPath(),
                    "resources" + File.separator + "layouts" + File.separator
                            + folder + File.separator + filename + ".html")
                    .getAbsolutePath());

            FileReader fr = new FileReader(filename);

            br = new BufferedReader(fr);

            while ((currentLine = br.readLine()) != null) {
                sb.append(currentLine);
            }

        } catch (IOException e) {
            LogHelper.log(log, e);
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {
                LogHelper.log(log, ex);
            }
        }

        return sb.toString();
    }

    private static String replaceLocaleImagesUrl(String text, String projectUrl) {
        return text.replaceAll("src=\"../", "src=\"" + projectUrl);
    }

    public static byte[] convertToPDF(String header, String body, String footer, DocumentType type)
            throws InvalidParameterException, IOException {
        if (body == null) {
            return null;
        }

        Long footerSize = 13l;
        Long headerSize = 0l;

        Long marginTop = 0l;
        Long marginBottom = 0l;
        Long marginLeft = 3l;
        Long marginRight = 3l;

        if (type != null) {
            switch (type) {
                case CADASTRAL:
                    headerSize = 40l;
                    break;

                case ESTATE_FORMALITY:
                    headerSize = 60l;
                    marginLeft = 10l;
                    marginRight = 10l;
                    break;
                    
                case FORMALITY:
                    headerSize = 40l;
                    marginLeft = 30l;
                    marginRight = 30l;
                    marginBottom = 30l;
                    break;

                default:
                    break;

            }
        }
        

       ServletContext servletContext = (ServletContext) FacesContext
                    .getCurrentInstance().getExternalContext().getContext();
       URL pd4mlJar = servletContext.getResource("/WEB-INF/lib/pd4ml-3.2.3fx5.jar");
       URL servletJar = servletContext.getResource("/WEB-INF/lib/servlet-api-2.5.jar");
       URL scssJar = servletContext.getResource("/WEB-INF/lib/ss_css2-0.9.3.jar");
       URL[] classLoaderUrls = new URL[]{pd4mlJar,servletJar,scssJar};
       URLClassLoader urlClassLoader = new CustomClassLoader(classLoaderUrls);
       StringReader isr = new StringReader(body);

//        PD4ML html = new PD4ML();
//        html.useTTF("java:fonts", true);
//
//        html.outputFormat(PD4Constants.PDF);
//
//        if (type == DocumentType.ESTATE_FORMALITY) {
//            html.setPageSize(PD4Constants.A4);
//            html.setHtmlWidth(768);
//        } else  if (type == DocumentType.FORMALITY) {
//            html.setPageSize(PD4Constants.A4);
//            html.setHtmlWidth(768);
//        }else if (type == DocumentType.INVOICE_REPORT) {
//            html.setPageSize(PD4Constants.LEDGER);
//            html.setHtmlWidth(1024);
//        } else {
//            html.setPageSize(html.changePageOrientation(PD4Constants.A4));
//            html.setHtmlWidth(1024);
//        }
//
//        html.setPageInsets(new Insets(MMtoDots(marginTop), MMtoDots(marginLeft),
//                MMtoDots(marginBottom), MMtoDots(marginRight)));
//
//        html.enableImgSplit(true);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//
//        PD4PageMark headerPDF = new PD4PageMark();
//        headerPDF.setHtmlTemplate(header);
//        headerPDF.setAreaHeight(MMtoDots(headerSize));
//        html.setPageHeader(headerPDF);
//
//        PD4PageMark footerPDF = new PD4PageMark();
//        footerPDF.setHtmlTemplate(footer);
//        footerPDF.setAreaHeight(MMtoDots(footerSize));
//        html.setPageFooter(footerPDF);
//
//        html.render(isr, baos);
        try {
            Class<?> PD4Constants = urlClassLoader.loadClass("org.zefer.pd4ml.PD4Constants");
            Class<?> beanClass = urlClassLoader.loadClass("org.zefer.pd4ml.PD4ML");
            Constructor<?> constructor = beanClass.getConstructor();
            Object beanObj = constructor.newInstance();
            Method method = beanClass.getMethod("outputFormat",new Class[]{String.class});
            method.invoke(beanObj,PD4Constants.getField("PDF").get(null));
           
            method = beanClass.getMethod("useTTF",new Class[]{String.class, boolean.class});
            method.invoke(beanObj,"java:fonts", true);
            
            Method setPageSize = beanClass.getMethod("setPageSize",new Class[]{Dimension.class});
            Method setHtmlWidth = beanClass.getMethod("setHtmlWidth",new Class[]{int.class});
            
            if (type == DocumentType.ESTATE_FORMALITY) {
                setPageSize.invoke(beanObj,PD4Constants.getField("A4").get(null));
                setHtmlWidth.invoke(beanObj,768);
            } else  if (type == DocumentType.FORMALITY) {
                setPageSize.invoke(beanObj,PD4Constants.getField("A4").get(null));
                setHtmlWidth.invoke(beanObj,768);
            }else if (type == DocumentType.INVOICE_REPORT) {
                setPageSize.invoke(beanObj,PD4Constants.getField("LEDGER").get(null));
                setHtmlWidth.invoke(beanObj,1024);
            } else {
                Method changePageOrientation = beanClass.getMethod("changePageOrientation",new Class[]{Dimension.class});
                Dimension dim = (Dimension)changePageOrientation.invoke(beanObj,PD4Constants.getField("A4").get(null));
                setPageSize.invoke(beanObj,dim);
                setHtmlWidth.invoke(beanObj,1024);
            }

            method = beanClass.getMethod("setPageInsets",new Class[]{Insets.class});
            method.invoke(beanObj,new Insets(MMtoDots(marginTop), MMtoDots(marginLeft),
                    MMtoDots(marginBottom), MMtoDots(marginRight)));

            method = beanClass.getMethod("enableImgSplit",new Class[]{boolean.class});

            method.invoke(beanObj, true);

            Class<?> pd4PageMarkClass = urlClassLoader.loadClass("org.zefer.pd4ml.PD4PageMark");
            Constructor<?> pd4PageMarkconstructor = pd4PageMarkClass.getConstructor();
            Object pd4PageMarkObj = pd4PageMarkconstructor.newInstance();
            method = pd4PageMarkClass.getMethod("setHtmlTemplate",new Class[]{String.class});
            method.invoke(pd4PageMarkObj,header);
            method = pd4PageMarkClass.getMethod("setAreaHeight",new Class[]{int.class});
            method.invoke(pd4PageMarkObj,MMtoDots(headerSize));
            method = beanClass.getMethod("setPageHeader",new Class[]{pd4PageMarkClass});
            method.invoke(beanObj, pd4PageMarkObj);

            Class<?> pd4PageMarkFotterClass = urlClassLoader.loadClass("org.zefer.pd4ml.PD4PageMark");
            Constructor<?> pd4PageMarkFotterconstructor = pd4PageMarkFotterClass.getConstructor();
            Object pd4PageMarkFotterObj = pd4PageMarkFotterconstructor.newInstance();
            method = pd4PageMarkFotterClass.getMethod("setHtmlTemplate",new Class[]{String.class});
            method.invoke(pd4PageMarkFotterObj,footer);
            method = pd4PageMarkFotterClass.getMethod("setAreaHeight",new Class[]{int.class});
            method.invoke(pd4PageMarkFotterObj,MMtoDots(footerSize));
            method = beanClass.getMethod("setPageFooter",new Class[]{pd4PageMarkFotterClass});
            method.invoke(beanObj, pd4PageMarkFotterObj);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            method = beanClass.getMethod("render",new Class[]{StringReader.class,OutputStream.class});

            method.invoke(beanObj, isr,baos);


            return baos.toByteArray();
        } catch (Exception e) {
            LogHelper.log(log, e);
            return null;
        }finally {
            if(urlClassLoader != null) {
                try {
                    urlClassLoader.close();
                } catch (Exception e) {
                }
            }
        }

    }

    private static int MMtoDots(Long mm) {
        return (int) (mm * 72f / 25.4f);
    }

    public static String getCalibriBodyContent(String text) {
        return "<span style=\"font-size:14px; \"><span style=\"font-family: Calibri,monospace;\">" +
                text.replaceAll("style\\s*=\\s*\"\\s*font-size\\s*:\\s*\\d+\\s*px\\s*;\\s*\"", "")
                + "</span></span>";
    }

    public static String getTemplateForInvoice(String fileName) {
        return readWorkingListFile(fileName, "Invoice");
    }
}
