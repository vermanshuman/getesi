package it.nexera.ris.common.helpers;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.InvalidParameterException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import it.nexera.ris.common.enums.DocumentGenerationTags;
import it.nexera.ris.common.exceptions.CannotProcessException;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.exceptions.TypeFormalityNotConfigureException;
import it.nexera.ris.common.utils.CustomClassLoader;
import it.nexera.ris.persistence.beans.entities.domain.DocumentTemplate;
import it.nexera.ris.persistence.beans.entities.domain.Request;
import it.nexera.ris.web.beans.wrappers.DocumentTemplateWrapper;
import it.nexera.ris.web.beans.wrappers.logic.TemplateEntity;
import it.nexera.ris.web.beans.wrappers.logic.UserWrapper;

public class TemplateToPdfHelper extends BaseHelper {

    // be careful with CERTIFICATE_HEADER_HEIGHT and CERTIFICATE_FOOTER_HEIGHT - it is for Certificazione template
    private final static int CERTIFICATE_HEADER_HEIGHT = 69;
    private final static int CERTIFICATE_FOOTER_HEIGHT = 50;

    public static byte[] convert(DocumentTemplate template, Request entity, UserWrapper currentUser) {
        if (template != null && entity != null) {
            try {
                DocumentTemplateWrapper documentTemplateWrapper = fillTemplate(template, entity, currentUser);
                return convertToPDF(template, documentTemplateWrapper);

            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
        return null;

    }

    public static DocumentTemplateWrapper fillTemplate(DocumentTemplate template, Request entity,
                                                       UserWrapper currentUser, String customBody)
            throws TypeFormalityNotConfigureException {
        DocumentTemplateWrapper documentTemplateWrapper = new DocumentTemplateWrapper(template);
        TemplateEntity wrappedEntity = new TemplateEntity(entity, currentUser);

        if (template.getFooter()) {
            documentTemplateWrapper.setFooterContent(replaceTags(template.getFooterContent(), wrappedEntity));
        }
        if (template.getHeader()) {
            documentTemplateWrapper.setHeaderContent(replaceTags(template.getHeaderContent(), wrappedEntity));
        }
        documentTemplateWrapper.setBodyContent(
                replaceTags(customBody == null ? template.getBodyContent() : customBody, wrappedEntity));
        return documentTemplateWrapper;
    }

    public static DocumentTemplateWrapper fillTemplate(DocumentTemplate template, Request entity, UserWrapper currentUser)
            throws TypeFormalityNotConfigureException {
        return fillTemplate(template, entity, currentUser, null);
    }

    public static byte[] convertToPDF(DocumentTemplate document, DocumentTemplateWrapper wrapper)
            throws InvalidParameterException, IOException {
        return convertToPDF(document, wrapper, null);
    }

    public static byte[] convertToPDFCertificate(DocumentTemplate document,
                                                 DocumentTemplateWrapper wrapper, String watermark, String style)
            throws InvalidParameterException, IOException {
        if (wrapper == null || document == null) {
            return null;
        }

        String header = null, footer = null;

        if (document.getHeader() != null && document.getHeader()
                && document.getHeaderContent() != null
                && !document.getHeaderContent().isEmpty()) {
            header = wrapper.getHeaderContent();
        } else {
            header = "";
        }

        if (document.getFooter() != null && document.getFooter()
                && document.getFooterContent() != null
                && !document.getFooterContent().isEmpty()) {
            footer = wrapper.getFooterContent();
        } else {
            footer = "";
        }

        Long width = document.getWidth();
        Long height = document.getHeight();

        if (width <= 0L || height <= 0L) {
            return null;
        }

        Long marginTop = document.getMarginTop() < 0L ? 0L : document.getMarginTop();
        Long marginBottom = document.getMarginBottom() < 0L ? 0L : document.getMarginBottom();
        Long marginLeft = document.getMarginLeft() < 0L ? 0L : document.getMarginLeft();
        Long marginRight = document.getMarginRight() < 0L ? 0L : document.getMarginRight();

        StringReader isr = new StringReader(wrapper.getBodyContent());

        ServletContext servletContext = (ServletContext) FacesContext
                .getCurrentInstance().getExternalContext().getContext();
        URL pd4mlJar = servletContext.getResource("/WEB-INF/lib/pd4ml.jar");
        URL servletJar = servletContext.getResource("/WEB-INF/lib/servlet-api-2.5.jar");

        URL[] classLoaderUrls = new URL[]{pd4mlJar,servletJar};
        
        URLClassLoader urlClassLoader = new CustomClassLoader(classLoaderUrls);

        try {
            
            Class<?> beanClass = urlClassLoader.loadClass("org.zefer.pd4ml.PD4ML");
            Constructor<?> constructor = beanClass.getConstructor();
            Object beanObj = constructor.newInstance();
            
            Method method = beanClass.getMethod("useTTF",new Class[]{String.class, boolean.class});
            method.invoke(beanObj,"java:fonts", true);

            method = beanClass.getMethod("setHtmlWidth",new Class[]{int.class});
            method.invoke(beanObj,756);

            if (style != null) {
                method = beanClass.getMethod("addStyle",new Class[]{String.class,boolean.class});
                method.invoke(beanObj,style,true);
            }
            Dimension truePageSize = new Dimension(MMtoDots(width), MMtoDots(height));
            method = beanClass.getMethod("setPageSize",new Class[]{Dimension.class});
            method.invoke(beanObj,truePageSize);
            
            method = beanClass.getMethod("setPageInsets",new Class[]{Insets.class});
            method.invoke(beanObj,new Insets(MMtoDots(marginTop), MMtoDots(marginLeft),
                    MMtoDots(marginBottom), MMtoDots(marginRight)));

            method = beanClass.getMethod("enableImgSplit",new Class[]{boolean.class});
            method.invoke(beanObj, false);

            Class<?> headerPDFClass = urlClassLoader.loadClass("org.zefer.pd4ml.PD4PageMark");
            Constructor<?>headerPDFConstructor = headerPDFClass.getConstructor();
            Object headerPDFObj = headerPDFConstructor.newInstance();
            method = headerPDFClass.getMethod("setHtmlTemplate",new Class[]{String.class});
            method.invoke(headerPDFObj,header);
            method = headerPDFClass.getMethod("setAreaHeight",new Class[]{int.class});
            method.invoke(headerPDFObj,CERTIFICATE_HEADER_HEIGHT);
            method = beanClass.getMethod("setPageHeader",new Class[]{headerPDFClass});
            method.invoke(beanObj, headerPDFObj);

            if (footer != null) {
                long footerHeight = document.getFooterHeight() != null
                        ? document.getFooterHeight() : 0L;

                        if (footerHeight > 0L || watermark != null) {
                            
                            Class<?> footerPDFClass = urlClassLoader.loadClass("org.zefer.pd4ml.PD4PageMark");
                            Constructor<?> footerPDFconstructor = footerPDFClass.getConstructor();
                            Object footerPDFObj = footerPDFconstructor.newInstance();
                            method = footerPDFClass.getMethod("setHtmlTemplate",new Class[]{String.class});
                            method.invoke(footerPDFObj,footer);
                            method = footerPDFClass.getMethod("setAreaHeight",new Class[]{int.class});
                            method.invoke(footerPDFObj,CERTIFICATE_FOOTER_HEIGHT);
                            if (watermark != null) {
                                method = footerPDFClass.getMethod("setWatermark",new Class[]{String.class, 
                                        Rectangle.class, int.class});
                                method.invoke(footerPDFObj,   
                                        // watermark image location URL.
                                        // For local images use "file:" protocol i.e. "file:images/logo.png"
                                        watermark,
                                        // watermark image position
                                        new Rectangle(0, -6,
                                                (int) truePageSize.getWidth(),
                                                (int) truePageSize.getHeight()),
                                        // watermark opacity in percents
                                        1000);
                                method = beanClass.getMethod("setPageFooter",new Class[]{footerPDFClass});
                                method.invoke(beanObj, footerPDFObj);
                            }
                            
                            method = beanClass.getMethod("setPageFooter",new Class[]{footerPDFClass});
                            method.invoke(beanObj, footerPDFObj);
                        }
            }
           
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
        
//        PD4ML html = new PD4ML();
//        html.useTTF("java:fonts", true);
//        html.setHtmlWidth(756);
//        if (style != null) {
//            html.addStyle(style, true);
//        }
//        Dimension truePageSize = new Dimension(MMtoDots(width), MMtoDots(height));
//        html.setPageSize(truePageSize);
//        html.setPageInsets(new Insets(MMtoDots(marginTop), MMtoDots(marginLeft),
//                MMtoDots(marginBottom), MMtoDots(marginRight)));
//        html.enableImgSplit(false);

//        ByteArrayOutputStream baos = new ByteArrayOutputStream();

//
//        PD4PageMark headerPDF = new PD4PageMark();
//        headerPDF.setHtmlTemplate(header);
//        headerPDF.setAreaHeight(CERTIFICATE_HEADER_HEIGHT);
//        html.setPageHeader(headerPDF);


//        if (footer != null) {
//            long footerHeight = document.getFooterHeight() != null
//                    ? document.getFooterHeight() : 0L;
//
//            if (footerHeight > 0L || watermark != null) {
//                PD4PageMark footerPDF = new PD4PageMark();
//                footerPDF.setHtmlTemplate(footer);
//                footerPDF.setAreaHeight(CERTIFICATE_FOOTER_HEIGHT);
//
//                if (watermark != null) {
//                    footerPDF.setWatermark(
//                            // watermark image location URL.
//                            // For local images use "file:" protocol i.e. "file:images/logo.png"
//                            watermark,
//                            // watermark image position
//                            new Rectangle(0, -6,
//                                    (int) truePageSize.getWidth(),
//                                    (int) truePageSize.getHeight()),
//                            // watermark opacity in percents
//                            1000);
//                    html.setPageFooter(footerPDF);
//                }
//                html.setPageFooter(footerPDF);
//            }
//        }
//
//        html.render(isr, baos);
//
//        try {
//            return baos.toByteArray();
//        } catch (Exception e) {
//            return null;
//        }
    }

    public static byte[] convertToPDF(DocumentTemplate document, DocumentTemplateWrapper wrapper, String watermark)
            throws InvalidParameterException, IOException {
        if (wrapper == null || document == null) {
            return null;
        }

        if (document.getWidth() <= 0L || document.getHeight() <= 0L) {
            return null;
        }
        
        StringReader isr = new StringReader(wrapper.getBodyContent());

        ServletContext servletContext = (ServletContext) FacesContext
                .getCurrentInstance().getExternalContext().getContext();
        URL pd4mlJar = servletContext.getResource("/WEB-INF/lib/pd4ml.jar");
        URL servletJar = servletContext.getResource("/WEB-INF/lib/servlet-api-2.5.jar");
        URL[] classLoaderUrls = new URL[]{pd4mlJar,servletJar};

        URLClassLoader urlClassLoader = new CustomClassLoader(classLoaderUrls);

        try {
            Class<?> beanClass = urlClassLoader.loadClass("org.zefer.pd4ml.PD4ML");
            Constructor<?> constructor = beanClass.getConstructor();
            Object beanObj = constructor.newInstance();
            
            Method method = beanClass.getMethod("useTTF",new Class[]{String.class, boolean.class});
            method.invoke(beanObj,"java:fonts", true);
            
            Dimension truePageSize = new Dimension(document.getWidth().intValue(), document.getHeight().intValue());
            method = beanClass.getMethod("setPageSizeMM",new Class[]{Dimension.class});
            method.invoke(beanObj,truePageSize);
            
            method = beanClass.getMethod("setHtmlWidth",new Class[]{int.class});
            method.invoke(beanObj,756);
            
            method = beanClass.getMethod("setPageInsetsMM",new Class[]{Insets.class});
            method.invoke(beanObj,new Insets(document.getMarginTop().intValue(), document.getMarginLeft().intValue(),
                    document.getMarginBottom().intValue(), document.getMarginRight().intValue()));

            method = beanClass.getMethod("enableImgSplit",new Class[]{boolean.class});
            method.invoke(beanObj, false);
            
            if (document.getHeader() != null && document.getHeader()
                    && !ValidationHelper.isNullOrEmpty(document.getHeaderContent())) {
                String header = fixTableStyle(wrapper.getHeaderContent());
                if (header != null) {
                    Long headerHeight = document.getHeaderHeight();
                    if (headerHeight > 0L) {
                        Class<?> headerPDFClass = urlClassLoader.loadClass("org.zefer.pd4ml.PD4PageMark");
                        Constructor<?>headerPDFConstructor = headerPDFClass.getConstructor();
                        Object headerPDFObj = headerPDFConstructor.newInstance();
                        method = headerPDFClass.getMethod("setHtmlTemplate",new Class[]{String.class});
                        method.invoke(headerPDFObj,header);
                        method = headerPDFClass.getMethod("setAreaHeight",new Class[]{int.class});
                        method.invoke(headerPDFObj,MMtoDots(headerHeight));
                        method = beanClass.getMethod("setPageHeader",new Class[]{headerPDFClass});
                        method.invoke(beanObj, headerPDFObj);
                    }
                }
            }
            


            if (document.getFooter() != null && document.getFooter()
                    && !ValidationHelper.isNullOrEmpty(document.getFooterContent())) {
                String footer = fixTableStyle(wrapper.getFooterContent());
                if (footer != null) {
                    Long footerHeight = document.getFooterHeight();
                    if (footerHeight > 0L) {
                        Class<?> footerPDFClass = urlClassLoader.loadClass("org.zefer.pd4ml.PD4PageMark");
                        Constructor<?> footerPDFconstructor = footerPDFClass.getConstructor();
                        Object footerPDFObj = footerPDFconstructor.newInstance();
                        method = footerPDFClass.getMethod("setPageNumberAlignment",new Class[]{int.class});
                        method.invoke(footerPDFObj,footerPDFClass.getField("RIGHT_ALIGN").get(null));
                        method = footerPDFClass.getMethod("setPageNumberTemplate",new Class[]{String.class});
                        method.invoke(footerPDFObj,String.format("$[page] %s $[total]",
                                ResourcesHelper.getString("paginationDelimiter")));
                        method = footerPDFClass.getMethod("setAreaHeight",new Class[]{int.class});
                        method.invoke(footerPDFObj,MMtoDots(footerHeight));
                        if (watermark != null) {
                            method = footerPDFClass.getMethod("setWatermark",new Class[]{String.class, 
                                    Rectangle.class, int.class});
                            method.invoke(footerPDFObj,   

                                    // watermark image location URL.
                                    // For local images use "file:" protocol i.e.
                                    // "file:images/logo.png"
                                    watermark,
                                    // watermark image position
                                    new Rectangle(10, 10, (int) truePageSize.getWidth(),
                                            (int) truePageSize.getHeight()
                                                    - (int) MMtoDots(footerHeight)),
                                    // watermark opacity in percents
                                    50);
                            method = beanClass.getMethod("setPageFooter",new Class[]{footerPDFClass});
                            method.invoke(beanObj, footerPDFObj);
                        }
                        method = beanClass.getMethod("setPageFooter",new Class[]{footerPDFClass});
                        method.invoke(beanObj, footerPDFObj);
                    }
                }
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            log.info("before html.render");
            ExecutorService executor = Executors.newCachedThreadPool();
            Callable<Boolean> task = () -> {
                Method renderMethod = beanClass.getMethod("render",new Class[]{StringReader.class,OutputStream.class});
                renderMethod.invoke(beanObj, isr,baos);
                return true;
            };
            Future<Boolean> future = executor.submit(task);
            boolean conversionComplete = false;
            int maxCountOfAttempts = 0;
            while (maxCountOfAttempts < 5 && !conversionComplete) {
                try {
                    maxCountOfAttempts++;
                    conversionComplete = future.get(1, TimeUnit.MINUTES);
                } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                    log.info("TimeoutException in html.render");
                    baos.reset();
                    future.cancel(true);
                    future = executor.submit(task);
                }
            }
            if (!future.isCancelled()) {
                future.cancel(true);
            }
            log.info("after html.render");
            try {
                return baos.toByteArray();
            } catch (Exception e) {
                log.info("baos.toByteArray() Exception");
                return null;
            }
            
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
        
//        PD4ML html = new PD4ML();
//        html.useTTF("java:fonts", true);
//        Dimension truePageSize = new Dimension(document.getWidth().intValue(), document.getHeight().intValue());
//        html.setPageSizeMM(truePageSize);
//        html.setHtmlWidth(756);
//        html.setPageInsetsMM(new Insets(document.getMarginTop().intValue(), document.getMarginLeft().intValue(),
//                document.getMarginBottom().intValue(), document.getMarginRight().intValue()));
//        html.enableImgSplit(false);
//
//        if (document.getHeader() != null && document.getHeader()
//                && !ValidationHelper.isNullOrEmpty(document.getHeaderContent())) {
//            String header = fixTableStyle(wrapper.getHeaderContent());
//            if (header != null) {
//                Long headerHeight = document.getHeaderHeight();
//                if (headerHeight > 0L) {
//                    PD4PageMark headerPDF = new PD4PageMark();
//                    headerPDF.setHtmlTemplate(header);
//                    headerPDF.setAreaHeight(MMtoDots(headerHeight));
//                    html.setPageHeader(headerPDF);
//                }
//            }
//        }
//        if (document.getFooter() != null && document.getFooter()
//                && !ValidationHelper.isNullOrEmpty(document.getFooterContent())) {
//            String footer = fixTableStyle(wrapper.getFooterContent());
//            if (footer != null) {
//                Long footerHeight = document.getFooterHeight();
//                if (footerHeight > 0L) {
////                    final String footerDiv = footer;
//                    PD4PageMark footerPDF = new PD4PageMark() {
//                        private static final long serialVersionUID = -3245499820898292398L;
//
//                        @Override
//                        public String getHtmlTemplate(int i) {
//                            if (i != Integer.parseInt(html.getLastRenderInfo(PD4Constants.PD4ML_TOTAL_PAGES).toString())) {
//                                return super.getHtmlTemplate();
//                            } else {
//                                return footer;
//                            }
//                        }
//                    };
//                    footerPDF.setPageNumberAlignment(PD4PageMark.RIGHT_ALIGN);
//                    footerPDF.setPageNumberTemplate(String.format("$[page] %s $[total]",
//                            ResourcesHelper.getString("paginationDelimiter")));
//                    footerPDF.setAreaHeight(MMtoDots(footerHeight));
//                    if (watermark != null) {
//                        footerPDF.setWatermark(
//                                // watermark image location URL.
//                                // For local images use "file:" protocol i.e.
//                                // "file:images/logo.png"
//                                watermark,
//                                // watermark image position
//                                new Rectangle(10, 10, (int) truePageSize.getWidth(),
//                                        (int) truePageSize.getHeight()
//                                                - (int) MMtoDots(footerHeight)),
//                                // watermark opacity in percents
//                                50);
//                    }
//                    html.setPageFooter(footerPDF);
//                }
//            }
//        }
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        log.info("before html.render");
//        ExecutorService executor = Executors.newCachedThreadPool();
//        Callable<Boolean> task = () -> {
//            html.render(new StringReader(fixTableStyle(wrapper.getBodyContent())), baos);
//            return true;
//        };
//        Future<Boolean> future = executor.submit(task);
//        boolean conversionComplete = false;
//        int maxCountOfAttempts = 0;
//        while (maxCountOfAttempts < 5 && !conversionComplete) {
//            try {
//                maxCountOfAttempts++;
//                conversionComplete = future.get(1, TimeUnit.MINUTES);
//            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
//                log.info("TimeoutException in html.render");
//                baos.reset();
//                future.cancel(true);
//                future = executor.submit(task);
//            }
//        }
//        if (!future.isCancelled()) {
//            future.cancel(true);
//        }
//        log.info("after html.render");
//        try {
//            return baos.toByteArray();
//        } catch (Exception e) {
//            log.info("baos.toByteArray() Exception");
//            return null;
//        }
    }

    public static String fixTableStyle(String html) {
        if (html != null) {
            return html.replaceAll("width:97%;", "width:100%;");
        } else {
            return null;
        }
    }

    private static int MMtoDots(Long mm) {
        return (int) (mm * 72f / 25.4f);
    }

    public static byte[] convertAndReturnData(DocumentTemplate template, DocumentTemplateWrapper wrapper,
                                              String watermark) throws IOException {
        if (template != null && wrapper != null) {
            byte[] data = null;
            if ("4".equals(template.getModel().getStrId())) {
                data = convertToPDFCertificate(template, wrapper, watermark, null);
            } else {
                data = convertToPDF(template, wrapper, watermark);
            }

            if (data != null) {
                return data;
            }
        }

        return null;
    }

    public static String replaceTags(String source, TemplateEntity entity) throws TypeFormalityNotConfigureException {
        if (!ValidationHelper.isNullOrEmpty(source)) {
            String result = source;

            for (DocumentGenerationTags tag : DocumentGenerationTags.values()) {
                try {
                    if (result.contains(tag.getTag())) {
                        String replacement = entity.invokeGetMethod(tag, result);
                        if (ValidationHelper.isNullOrEmpty(replacement)) {
                            replacement = "";
                        }
                        if (DocumentGenerationTags.CERTIFICAZIONE_TABLE.equals(tag)) {
                            result = replacement.replaceAll(tag.getTag(), replacement);
                        } else {
                            result = result.replaceAll(tag.getTag(), replacement);
                        }
                    }
                } catch (CannotProcessException | PersistenceBeanException |
                        IllegalAccessException | InstantiationException e) {
                    LogHelper.log(log, e);
                }
            }

            return result;
        }

        return "";
    }
}