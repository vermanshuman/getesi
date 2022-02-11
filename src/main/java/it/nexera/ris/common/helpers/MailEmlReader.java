package it.nexera.ris.common.helpers;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.WLGExport;
import it.nexera.ris.persistence.beans.entities.domain.WLGInbox;
import it.nexera.ris.persistence.beans.entities.domain.WLGServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class MailEmlReader {

    protected static transient final Log log = LogFactory.getLog(BaseHelper.class);

    private WLGInbox inbox;

    private String mailText;

    private String mailHtml;

    private List<File> images;

    private List<MailImageWrapper> imageNames;

    public MailEmlReader(WLGInbox inbox) {
        this.inbox = inbox;
        this.imageNames = new LinkedList<>();
    }

    public void readInbox() {
        if (getInbox().getPath() == null || !new File(getInbox().getPath()).exists()) {
            return;
        }
        File initialFile = new File(getInbox().getPath());

        try (InputStream mailIn = new FileInputStream(initialFile)) {
            WLGServer server = DaoManager.get(WLGServer.class, MailHelper.mailReceiveServerId(DaoManager.getSession()));
            Properties props = System.getProperties();
            props.put("mail.host", server.getHost());
//            props.put("mail.transport.protocol", "smtp");

            Session mailSession = Session.getDefaultInstance(props, null);
            MimeMessage message = new MimeMessage(mailSession, mailIn);
            if (message.isMimeType("text/plain")) {
                setMailText(message.getContent().toString());
            } else if (message.isMimeType("multipart/*")) {
                MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
                getTextFromMimeMultipart(mimeMultipart);
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
        if (!ValidationHelper.isNullOrEmpty(getImageNames())) {
            Document doc = Jsoup.parse(getMailHtml());
            for (Element element : doc.select("img")) {
                String id = element.attr("id");
                String src = element.attr("src");
                getImageNames().stream()
                        .filter(w -> src.contains(w.getCid().replaceAll("<", "")
                                .replaceAll(">", ""))).findAny().ifPresent(wrapper -> wrapper.setTagId(id));
            }
        }
    }

    public String prepareBodyToSend(String body) throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (!ValidationHelper.isNullOrEmpty(getImageNames())) {
            Document doc = Jsoup.parse(body);
            for (Element element : doc.select("img")) {
                String imageID = element.attr("imageID");
                if (!ValidationHelper.isNullOrEmpty(imageID)) {
                    WLGExport wlgExport = DaoManager.get(WLGExport.class, Long.parseLong(imageID));
                    body = body.replaceAll("imageid=\"" + imageID + "\" src=\"(.*?)\"",
                            "imageid=\"" + imageID + "\" src=\"cid:" + wlgExport.getFileName() + "\"");
                }
            }
        }
        return body;
    }

    private void getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                setMailText(bodyPart.getContent().toString());
            } else if (bodyPart.isMimeType("text/html")) {
                setMailHtml((String) bodyPart.getContent());
            } else if (bodyPart.isMimeType("image/*")) {
                getImageNames().add(new MailImageWrapper(bodyPart.getDescription(), ((MimeBodyPart) bodyPart).getContentID()));
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
            }
        }
    }

    public WLGInbox getInbox() {
        return inbox;
    }

    public void setInbox(WLGInbox inbox) {
        this.inbox = inbox;
    }

    public String getMailText() {
        return mailText;
    }

    public void setMailText(String mailText) {
        this.mailText = mailText;
    }

    public String getMailHtml() {
        return mailHtml;
    }

    public void setMailHtml(String mailHtml) {
        this.mailHtml = mailHtml;
    }

    public List<File> getImages() {
        return images;
    }

    public void setImages(List<File> images) {
        this.images = images;
    }

    public List<MailImageWrapper> getImageNames() {
        return imageNames;
    }

    public void setImageNames(List<MailImageWrapper> imageNames) {
        this.imageNames = imageNames;
    }

    public class MailImageWrapper {

        private String imageName;

        private String cid;

        private String tagId;

        public MailImageWrapper(String imageName, String cid) {
            this.imageName = imageName;
            this.cid = cid;
        }

        public String getImageName() {
            return imageName;
        }

        public void setImageName(String imageName) {
            this.imageName = imageName;
        }

        public String getCid() {
            return cid;
        }

        public void setCid(String cid) {
            this.cid = cid;
        }

        public String getTagId() {
            return tagId;
        }

        public void setTagId(String tagId) {
            this.tagId = tagId;
        }
    }
}
