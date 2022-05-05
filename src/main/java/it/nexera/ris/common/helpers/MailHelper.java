package it.nexera.ris.common.helpers;


import it.nexera.ris.common.annotations.MailTag;
import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.exceptions.ServerNotFoundExceprion;
import it.nexera.ris.persistence.PersistenceSession;
import it.nexera.ris.persistence.beans.dao.ConnectionManager;
import it.nexera.ris.persistence.beans.dao.CriteriaAlias;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.EmailRemove;
import it.nexera.ris.persistence.beans.entities.domain.MailTemplate;
import it.nexera.ris.persistence.beans.entities.domain.WLGInbox;
import it.nexera.ris.persistence.beans.entities.domain.WLGServer;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.wrappers.logic.FileWrapper;
import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.message.DefaultBodyDescriptorBuilder;
import org.apache.james.mime4j.parser.ContentHandler;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.apache.james.mime4j.stream.BodyDescriptorBuilder;
import org.apache.james.mime4j.stream.MimeConfig;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.jsoup.Jsoup;
import tech.blueglacier.email.Attachment;
import tech.blueglacier.email.Email;
import tech.blueglacier.parser.CustomContentHandler;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.Message.RecipientType;
import javax.mail.event.TransportListener;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MailHelper extends BaseHelper {

    private static final String TRUE = "true";

    private static final String defaultSendFromName = "GETESI SRL";

    private static final String customerSendFromName = "CUSTOMER SERVICE GETESI";

    public static String getDestinationPath() {
        return FileHelper.getApplicationProperties()
                .getProperty("destinationPath");
    }

    public static Long mailSendServerId(org.hibernate.Session session) {
        Long id = null;

        try {
            List<Long> ids = ConnectionManager.loadField(WLGServer.class, "id",
                    Long.class, new CriteriaAlias[]{}, new Criterion[]{
                            Restrictions.eq("type", 15L)
                    }, session);

            if (!ValidationHelper.isNullOrEmpty(ids)) {
                id = ids.get(0);
            }
        } catch (HibernateException e) {
            LogHelper.log(log, e);
        }

        return id;
    }

    public static Long mailReceiveServerId(org.hibernate.Session session) {
        Long id = null;

        try {
            List<Long> ids = ConnectionManager.loadField(WLGServer.class, "id",
                    Long.class, new CriteriaAlias[]{}, new Criterion[]{
                            Restrictions.eq("type", 14L)
                    }, session);

            if (!ValidationHelper.isNullOrEmpty(ids)) {
                id = ids.get(0);
            }
        } catch (HibernateException e) {
            LogHelper.log(log, e);
        }

        return id;
    }

    public static List<String> parseMailAddress(String address) {
        if (ValidationHelper.isNullOrEmpty(address)) {
            return new ArrayList<>();
        }

        List<String> emails = Arrays.asList(address.split(","));

        Set<String> emailsSet = new HashSet<>();

        for (String email : emails) {
            Matcher m = Pattern
                    .compile("[a-zA-Z0-9'_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+")
                    .matcher(email);

            while (m.find()) {
                emailsSet.add(m.group().toLowerCase());
            }
        }

        return new ArrayList<>(emailsSet);
    }

    public static boolean checkRemoveMailAddress(String address) {
        try {
            return 0 == DaoManager.getCount(EmailRemove.class, "id", new Criterion[]{
                    Restrictions.eq("email", address)
            });
        } catch (PersistenceBeanException | IllegalAccessException e) {
            LogHelper.log(log, e);
        }
        return true;
    }

    public static String concatMailString(List<String> list) {
        StringBuilder stringBuilder = new StringBuilder();
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            stringBuilder.append(iterator.next());
            if (iterator.hasNext()) {
                stringBuilder.append(",");
            }
        }
        return stringBuilder.toString();
    }

    public static void sendMail(WLGInbox mail, List<FileWrapper> files) throws HibernateException,
            InstantiationException, IllegalAccessException, PersistenceBeanException,
            ServerNotFoundExceprion, UnsupportedEncodingException, MessagingException {
        sendMail(mail, files, null);
    }

    public static void sendMail(WLGInbox mail, List<FileWrapper> files, List<FileWrapper> images)
            throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException,
            ServerNotFoundExceprion, UnsupportedEncodingException, MessagingException {
        PersistenceSession ps = new PersistenceSession();
        WLGServer server = ConnectionManager.get(WLGServer.class, new Criterion[]{
                Restrictions.eq("id", Long.parseLong(ApplicationSettingsHolder.getInstance()
                        .getByKey(ApplicationSettingsKeys.SENT_SERVER_ID).getValue()))
        }, ps.getSession());

        if (server == null) {
            throw new ServerNotFoundExceprion("can not load wlg_server for sendNotification email");
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", server.getHost());
        props.put("mail.smtp.auth", TRUE);
        props.put("mail.smtp.port", server.getPort());

        PopupAuthenticator auth = new PopupAuthenticator();
        auth.setPasswordAuthentication(server.getLogin(), server.getPassword());

        Session mailSession = Session.getInstance(props, auth);

        MimeMessage simpleMessage = new MimeMessage(mailSession);

        List<String> mailsTo = parseMailAddress(mail.getEmailTo());

        List<String> mailsCC = parseMailAddress(mail.getEmailCC());

        List<String> mailBCC = parseMailAddress(mail.getEmailBCC());

        InternetAddress[] toAddress = new InternetAddress[mailsTo.size()];
        InternetAddress[] ccAddress = new InternetAddress[mailsCC.size()];
        InternetAddress[] bccAddress = new InternetAddress[mailBCC.size()];

        InternetAddress fromAddress = new InternetAddress(mail.getEmailFrom(), defaultSendFromName);
        if(mail.getEmailFrom().contains("<")) {
        	fromAddress = new InternetAddress(getOnlyEmails(mail.getEmailFrom()).get(0), defaultSendFromName);
        }

        for (int i = 0; i < mailsTo.size(); ++i) {
            toAddress[i] = new InternetAddress(mailsTo.get(i));
        }

        for (int i = 0; i < mailsCC.size(); ++i) {
            ccAddress[i] = new InternetAddress(mailsCC.get(i));
        }

        for (int i = 0; i < mailBCC.size(); ++i) {
            bccAddress[i] = new InternetAddress(mailBCC.get(i));
        }

        Transport t = null;

        if (server.getUseSecuredConnection()) {
            t = mailSession.getTransport("smtps");
        } else {
            t = mailSession.getTransport("smtp");
        }

        simpleMessage.setFrom(fromAddress);

        simpleMessage.setRecipients(RecipientType.TO, toAddress);
        simpleMessage.setRecipients(RecipientType.CC, ccAddress);
        simpleMessage.setRecipients(RecipientType.BCC, bccAddress);

        simpleMessage.setSubject(mail.getEmailSubject());
        simpleMessage.setSentDate(new Date());

        if (mail.getDispositionNotificationTo() != null && mail.getDispositionNotificationTo()) {
            simpleMessage.setHeader("Disposition-Notification-To", mail.getEmailFrom());
        }
        if (mail.getDeliveredNotification() != null && mail.getDeliveredNotification()) {
            TransportListener ls = new TransportLis(mail.getEmailFrom());
            t.addTransportListener(ls);
        }
        simpleMessage.setHeader("X-Priority", String.valueOf(mail.getXpriority()));
        BodyPart messageBodyPart = new MimeBodyPart();

//        BodyPart messageBodyText = new MimeBodyPart();
//        messageBodyText.setContent(mail.getEmailTextBody(), "text/plain; charset=utf-8");
//        BodyPart messageBodyHtml = new MimeBodyPart();
        messageBodyPart.setContent(mail.getEmailBodyHtml() == null
                ? mail.getEmailTextBody() : mail.getEmailBodyHtml(), "text/html; charset=utf-8");
//        messageBodyPart.setContent(mail.getEmailBodyHtml(),
//                "text/html; charset=utf-8");

//        Multipart multipart = new MimeMultipart("alternative");
        Multipart multipart = new MimeMultipart();

        multipart.addBodyPart(messageBodyPart);

        messageBodyPart = new MimeBodyPart();

//        multipart.addBodyPart(messageBodyText);
//        multipart.addBodyPart(messageBodyHtml);

        if (files != null) {
            for (FileWrapper file : files) {
                addAttachment(multipart, file.getFilePath(), file.getFileName());
            }
        }

        if (images != null) {
            for (FileWrapper file : images) {
                addImage(multipart, file.getFilePath(), file.getFileName());
            }
        }

        simpleMessage.setContent(multipart);

        t.connect(server.getHost(), server.getLogin(), server.getPassword());

        t.sendMessage(simpleMessage, simpleMessage.getAllRecipients() != null ?
                simpleMessage.getAllRecipients() : simpleMessage.getReplyTo());

        t.close();
    }

    public static void sendMail(String mailTo, MailTemplate mailTemplate, List<FileWrapper> files)
            throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException,
            ServerNotFoundExceprion, UnsupportedEncodingException, MessagingException {
        WLGServer server = DaoManager.get(WLGServer.class, new Criterion[]{
                Restrictions.eq("id", Long.parseLong(ApplicationSettingsHolder.getInstance()
                        .getByKey(ApplicationSettingsKeys.SENT_SERVER_ID).getValue()))
        });

        if (server == null) {
            throw new ServerNotFoundExceprion("can not load wlg_server for sendNotification email");
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", server.getHost());
        props.put("mail.smtp.auth", TRUE);
        props.put("mail.smtp.port", server.getPort());

        PopupAuthenticator auth = new PopupAuthenticator();
        auth.setPasswordAuthentication(server.getLogin(), server.getPassword());

        Session mailSession = Session.getInstance(props, auth);

        MimeMessage simpleMessage = new MimeMessage(mailSession);

        List<String> mailsTo = parseMailAddress(mailTo);

        InternetAddress[] toAddress = new InternetAddress[mailsTo.size()];

        InternetAddress fromAddress = new InternetAddress(server.getLogin(), customerSendFromName);

        for (int i = 0; i < mailsTo.size(); ++i) {
            toAddress[i] = new InternetAddress(mailsTo.get(i));
        }

        Transport t = null;

        if (server.getUseSecuredConnection()) {
            t = mailSession.getTransport("smtps");
        } else {
            t = mailSession.getTransport("smtp");
        }

        simpleMessage.setFrom(fromAddress);

        simpleMessage.setRecipients(RecipientType.TO, toAddress);

        simpleMessage.setSubject(mailTemplate.getMailSubject());
        simpleMessage.setSentDate(new Date());

        BodyPart messageBodyPart = new MimeBodyPart();

        messageBodyPart.setContent(mailTemplate.getMailBodyHtml(), "text/html; charset=utf-8");

        Multipart multipart = new MimeMultipart();

        multipart.addBodyPart(messageBodyPart);

        messageBodyPart = new MimeBodyPart();

        if (files != null) {
            for (FileWrapper file : files) {
                addAttachment(multipart, file.getFilePath(), file.getFileName());
            }
        }

        simpleMessage.setContent(multipart);

        t.connect(server.getHost(), server.getLogin(), server.getPassword());

        try {
            t.sendMessage(simpleMessage, simpleMessage.getAllRecipients() != null ?
                    simpleMessage.getAllRecipients() : simpleMessage.getReplyTo());
        } catch (Exception e) {
            LogHelper.log(log, e);
        } finally {
            t.close();
        }

    }

    public static String htmlToText(String html) {
        return Jsoup.parse(html).text();
    }

    public static String prepareEmailToPdf(String email) {
        return email.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }

    public static String prepareEmailToSend(String email) {
        return email.replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&#34;", "\"");
    }

    public static String[] parseEmail(String email) {
        String name = "";
        String address = "";
        if (email != null) {
            int indexLast = email.lastIndexOf("\"");
            if (indexLast == -1) {
                name = email;
                address = email;
            } else {
                name = email.substring(1, indexLast);
                address = email.substring(indexLast + 2);
            }
        }
        return new String[]{name, address};
    }

    public static String fillPDF(WLGInbox email, String html)
            throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        if (email != null && !ValidationHelper.isNullOrEmpty(html)) {
            Method[] methods = email.getClass().getMethods();

            for (Method m : methods) {
                Annotation[] annotations = m.getDeclaredAnnotations();
                for (Annotation a : annotations) {
                    MailTag annot = null;

                    if (a instanceof MailTag) {
                        annot = (MailTag) a;
                    }

                    if (annot != null) {
                        html = html.replaceAll(annot.emailTag().getTag(),
                                (String) m.invoke(email, new Object[0]));
                    }
                }
            }
        }

        return html;
    }

    private static void addAttachment(Multipart multipart, String filePath, String fileName) throws MessagingException {
        LogHelper.log(log, "sending " + filePath);
        DataSource source = new FileDataSource(filePath);
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(fileName);
        multipart.addBodyPart(messageBodyPart);
    }

    private static void addImage(Multipart multipart, String filePath, String cid) throws MessagingException {
        if (new File(filePath).exists()) {
            BodyPart messageBodyPart = new MimeBodyPart();
            DataSource fds = new FileDataSource(filePath);
            messageBodyPart.setDataHandler(new DataHandler(fds));
            messageBodyPart.setHeader("Content-ID", "<" + cid + ">");
            messageBodyPart.setHeader("Content-Disposition", "inline");
//            messageBodyPart.setHeader("X-Attachment-Id",  cid);
            multipart.addBodyPart(messageBodyPart);
        }
    }

    static class PopupAuthenticator extends Authenticator {

        private String userName;

        private String password;

        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(this.getUserName(),
                    this.getPassword());
        }

        public void setPasswordAuthentication(String userName, String password) {
            this.setPassword(password);
            this.setUserName(userName);
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getPassword() {
            return password;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getUserName() {
            return userName;
        }
    }

    public static String getHtmlFromEml(String path) {

        if (path == null || !new File(path).exists()) {
            return null;
        }
        File initialFile = new File(path);

        try (InputStream mailIn = new FileInputStream(initialFile)) {
            ContentHandler contentHandler = new CustomContentHandler();
            MimeConfig mime4jParserConfig = MimeConfig.DEFAULT;
            BodyDescriptorBuilder bodyDescriptorBuilder = new DefaultBodyDescriptorBuilder();
            MimeStreamParser mime4jParser = new MimeStreamParser(mime4jParserConfig, DecodeMonitor.SILENT, bodyDescriptorBuilder);
            mime4jParser.setContentDecoding(true);
            mime4jParser.setContentHandler(contentHandler);
            mime4jParser.parse(mailIn);
            Email email = ((CustomContentHandler) contentHandler).getEmail();
            Attachment htmlBody = email.getHTMLEmailBody();

            return IOUtils.toString(htmlBody.getIs(), email.getHTMLEmailBody().getBd().getCharset());
        } catch (NullPointerException ignored) {

        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        return null;
    }

    public static List<String> getOnlyEmails(String data) {
        List<String> result = new LinkedList<>();
        Matcher m = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9z-]+\\.[a-zA-Z0-9-.]+").matcher(data);
        while (m.find()) {
            result.add(m.group());
        }
        return result;
    }
}
