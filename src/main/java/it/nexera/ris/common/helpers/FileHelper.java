package it.nexera.ris.common.helpers;

import it.nexera.ris.persistence.UserHolder;
import it.nexera.ris.web.listeners.ApplicationListener;

import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.primefaces.model.UploadedFile;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * FileHelper class Used for working with filesystem
 */
public class FileHelper extends BaseHelper {
    private static final String PROJECT_PROPERTIES_FILE_NAME = "project.properties";

    private static String CONTEXT_NAME = "ris";

    private static String realPath;

    private static Map<String, XWPFRun> templateMapping = new HashMap<>();

    public static String getLocalFilePath(String path) throws IOException {
        if (FileHelper.exists(FileHelper.getLocalFileDir(), path)) {

        } else if (FileHelper.exists(FileHelper.getFileDir(), path)) {
            FileHelper.copy(new File(FileHelper.getFileDir(), path),
                    new File(FileHelper.getLocalFileDir(), path));

        } else if (FileHelper.exists(FileHelper.getTempDir(), path)) {
            FileHelper.copy(new File(FileHelper.getTempDir(), path),
                    new File(FileHelper.getLocalFileDir(), path));
        } else {
            return getPathWithDefaultSeparator(path);
        }

        return getPathWithDefaultSeparator(path);
    }

    public static String getPathWithDefaultSeparator(String path) {
        return path.replace("\\", "/");
    }

    public static boolean exists(String path) throws IOException {
        return FileHelper.exists(FileHelper.getLocalFileDir(), path)
                || FileHelper.exists(FileHelper.getFileDir(), path)
                || FileHelper.exists(FileHelper.getTempDir(), path);
    }

    public static boolean existInTemp(String path) {
        return FileHelper.exists(FileHelper.getTempDir(), path);
    }

    public static boolean existInLocalTemp(String path) {
        return FileHelper.exists(FileHelper.getLocalTempDir(), path);
    }

    public static boolean exists(String parent, String filename) {
        return new File(parent, filename).exists();
    }

    public static String getFileName(String path) {
        String name = EncodingHelper.ConvertToUTF8String(path);
        return (new File(name)).getName();
    }

    public static String getFileExtension(String path) {
        if (path.lastIndexOf('.') == -1) {
            return "";
        }

        return path.substring(path.lastIndexOf('.'));
    }

    public static String getFileNameWOExtension(String path) {
        if (path.lastIndexOf('.') == -1) {
            return path;
        }

        return path.substring(0, path.lastIndexOf('.'));
    }

    public static String newFileName(String name) {
        return newFileName("", name);
    }

    public static String newFileName(String parent, String dest) {
        File file = new File(getBaseDir(),
                "File" + File.separator + CONTEXT_NAME);
        file.setWritable(true, false);
        file.mkdir();

        StringJoiner joiner = new StringJoiner(File.separator);
        joiner.add(getBaseDir());
        joiner.add("File");
        joiner.add(CONTEXT_NAME);
        joiner.add(parent);
        joiner.add(UUID.randomUUID().toString());
        joiner.add(FileHelper.getFileExtension(dest));

        return joiner.toString();
    }

    public static String getUserDirPath(Long userId) {
        String sb = "user_" + userId + File.separatorChar;

        File file = new File(getFileDir() + File.separatorChar + sb);
        if (!file.exists()) {
            file.setWritable(true, false);
            file.mkdirs();
        }
        return sb;
    }

    public static String getUserLocalPath(Long userId) {
        String sb = "user_" + userId + File.separatorChar;

        File file = new File(getLocalFileDir() + File.separatorChar + sb);
        if (!file.exists()) {
            file.setWritable(true, false);
            file.mkdirs();
        }
        return sb;
    }

    public static String getBaseDir() {
        return new File(getLocalDir()).getParent();
    }

    public static String getImageDir() {
        return new File(getLocalDir() + "resources" + File.separator + "images")
                .getAbsolutePath();
    }

    public static String getCustomFolderDir(String folder) {
        if (folder != null) {
            return new File(FileHelper.getRealPath(), folder).getAbsolutePath();
        }
        return null;
    }

    public static String getLogsDir() {
        return new File(
                new File(new File(getBaseDir()).getParent()).getParent(),
                "logs").getPath();
    }

    public static String getLocalDir() {
        String s2 = getRealPath();

        if (s2 != null) {
            return s2.substring(0, s2.length() - 1);
        } else {
            return "";
        }
    }

    public static String getRandomFileName(String name) {
        return UUID.randomUUID().toString() + FileHelper.getFileExtension(name);
    }

    public static String newLocalFileName(String name) {
        File file = new File(getLocalDir(), "File");
        file.setWritable(true, false);
        file.mkdir();

        StringBuilder sb = new StringBuilder();
        sb.append(getLocalDir() + File.separator + "File" + File.separator);
        sb.append(UUID.randomUUID().toString());
        sb.append(FileHelper.getFileExtension(name));

        return sb.toString();
    }

    public static String newTempFileName(String name) {
        File file = new File(getLocalDir(), "Temp");
        file.setWritable(true, false);
        file.mkdir();

        StringBuilder sb = new StringBuilder();
        sb.append(getLocalDir() + File.separator + "Temp" + File.separator);
        sb.append(UUID.randomUUID().toString());
        sb.append(FileHelper.getFileExtension(name));

        return sb.toString();
    }

    public static String getTempDir() {
        File file = new File(getBaseDir(), "Temp");
        file.setWritable(true, false);
        file.mkdir();

        return file.getPath();
    }

    public static String getLocalTempDir() {
        File file = new File(getLocalDir(), "Temp");
        file.setWritable(true, false);
        file.mkdir();

        return file.getPath();
    }

    public static String getFileDir() {
        File file = new File(getBaseDir(),
                "File" + File.separator + CONTEXT_NAME);
        file.setWritable(true, false);
        file.mkdirs();

        return file.getPath();
    }

    public static String getLocalFileDir() {
        File file = new File(getLocalDir(), "File");
        file.setWritable(true, false);
        file.mkdir();

        return file.getPath();
    }

    public static File getProfilePicturePath(Long userId,
                                             String profilePictureName) {
        return new File(
                FileHelper.getFileDir() + File.separatorChar
                        + FileHelper.getUserDirPath(userId),
                profilePictureName);
    }

    public static String getNewCroppedThumbnailName() {
        return FileHelper.getRandomFileName(".jpg");
    }

    public static boolean delete(File resource) {
        if (resource.isDirectory()) {
            File[] childFiles = resource.listFiles();

            if (childFiles != null) {
                for (File child : childFiles) {
                    delete(child);
                }
            }
        }

        return resource.delete();
    }

    public static boolean delete(String resource) {
        if (resource == null || resource.isEmpty()) {
            return false;
        }
        File file = new File(resource);
        return delete(file);
    }

    public static boolean delete(String parent, String resource) {
        if (resource == null || resource.isEmpty()) {
            return false;
        }
        File file = new File(parent, resource);
        return delete(file);
    }

    public static void moveFile(File source, File dest) throws IOException {
        Boolean bRet = source.renameTo(dest);
        log.info(
                String.format("Move file to %s was %s", dest, bRet.toString()));
    }

    public static void copy(File fromFile, File toFile) throws IOException {
        if (toFile.isDirectory()) {
            toFile = new File(toFile, fromFile.getName());
            toFile.setWritable(true, false);
            toFile.mkdirs();
        } else {
            File parent = toFile.getParentFile();
            parent.setWritable(true, false);
            parent.mkdirs();
        }

        if (toFile.exists()) {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(System.in));
            String response = in.readLine();

            if (response == null) {
                throw new IOException("FileCopy: empty string.");
            } else if (!response.equals("Y") && !response.equals("y")) {
                throw new IOException(
                        "FileCopy: " + "existing file was not overwritten.");
            }
        } else {
            String parent = toFile.getParent();
            if (parent == null) {
                parent = System.getProperty("user.dir");
            }
            File dir = new File(parent);
            if (!dir.exists()) {
                throw new IOException("FileCopy: "
                        + "destination directory doesn't exist: " + parent);
            }
            if (dir.isFile()) {
                throw new IOException("FileCopy: "
                        + "destination is not a directory: " + parent);
            }
            if (!dir.canWrite()) {
                throw new IOException("FileCopy: "
                        + "destination directory is unwriteable: " + parent);
            }
        }

        try (FileInputStream from = new FileInputStream(fromFile); FileOutputStream to = new FileOutputStream(toFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = from.read(buffer)) != -1) {
                to.write(buffer, 0, bytesRead);
            }
        }
    }

    public static void moveFileFromTemp(String source, String dest)
            throws IOException {
        moveFile(new File(getTempDir(), source), new File(getFileDir(), dest));
    }

    public static void copyFileFromTemp(String source, String dest)
            throws IOException {
        copy(new File(getTempDir(), source), new File(getFileDir(), dest));
    }

    public static void copyFileFromLocalTemp(String source, String dest)
            throws IOException {
        copy(new File(getLocalTempDir(), source), new File(getFileDir(), dest));
    }

    public static void copyFileFromTempToLocalDir(String source, String dest)
            throws IOException {
        copy(new File(getTempDir(), source), new File(getLocalFileDir(), dest));
    }

    public static void copyFileToTemp(String source, String dest)
            throws IOException {
        copy(new File(getFileDir(), source), new File(getTempDir(), dest));
    }

    public static void copyFileToLocalTemp(String source, String dest)
            throws IOException {
        copy(new File(getFileDir(), source), new File(getLocalTempDir(), dest));
    }

    public static void writeFileToTemp(String name, byte[] data)
            throws IOException {
        try (FileOutputStream out = new FileOutputStream(new File(getTempDir(), name))) {
            out.write(data);
        }
    }

    public static String writeFileToLocalTemp(String name, byte[] data)
            throws IOException {

        File file = new File(getLocalTempDir(), name);
        try (FileOutputStream out = new FileOutputStream(file)) {

            out.write(data);

            return file.getAbsolutePath();
        }
    }

    public static void sendFile(String fileName, byte[] data) {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) context
                .getExternalContext().getResponse();
        ServletOutputStream output = null;
        try {
            log.info("Inside sendFile " + fileName);
            int length = data.length;

            response.reset();
            response.setHeader("Content-Type",
                    FileHelper.getFileExtension(fileName));
            response.setHeader("Content-Length", String.valueOf(length));
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + fileName + "\"");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Pragma", "no-cache");
            // Streams we will use to read, write the file bytes to our response

            output = response.getOutputStream();
            output.write(data);
            output.flush();
        } catch (Exception e) {
            LogHelper.log(log, e);
        } finally {
            try {
                if (output != null) {
                    context.responseComplete();
                    output.close();
                }
            } catch (IOException e) {
                LogHelper.log(log, e);
            }
        }
    }

    /**
     * Send file throu response
     *
     * @param fileName
     * @param inputFile
     * @param dataLength
     * @param dataLength
     */
    public static void sendFile(String fileName, InputStream inputFile, int dataLength) {
        try {
            byte[] data = new byte[dataLength];
            inputFile.read(data);
            sendFile(fileName, data);
        } catch (IOException e) {
            LogHelper.log(log, e);
        }
        try {
            if (inputFile != null) {
                inputFile.close();
            }
        } catch (IOException e) {
            LogHelper.log(log, e);
        }
    }

    public static String writeFileToFolder(String name, File folder,
                                           byte[] data) throws IOException {
        File f = new File(folder, name);
        if (!f.exists()) {

            boolean isCreatedDirs = new File(f.getParent()).mkdirs();
            boolean isCreatedFile = f.createNewFile();
            LogHelper.debugInfo(log, "Directories were created " + String.valueOf(isCreatedDirs));
            LogHelper.debugInfo(log, "File was created " + String.valueOf(isCreatedFile));
        }
        LogHelper.debugInfo(log, "File exists " + String.valueOf(f.exists()));
        try (FileOutputStream out = new FileOutputStream(new File(folder, name))) {
            out.write(data);
        }

        return f.getAbsolutePath();
    }

    public static byte[] loadContentByPath(String path) {
        if (!ValidationHelper.isNullOrEmpty(path)) {
            FileInputStream fileInputStream = null;

            File file = new File(path);
            if (file.exists()) {
                byte[] data = new byte[(int) file.length()];

                try {
                    fileInputStream = new FileInputStream(file);
                    fileInputStream.read(data);
                    fileInputStream.close();

                    return data;
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
            }
        }

        return null;
    }

    public static String getRealPath() {
        return realPath;
    }

    public static void setRealPath(String realPath) {
        FileHelper.realPath = realPath;
    }

    public static Map<String, XWPFRun> getTemplateMapping() {
        return templateMapping;
    }

    public static void setTemplateMapping(Map<String, XWPFRun> templateMapping) {
        FileHelper.templateMapping = templateMapping;
    }

    public static Properties getApplicationProperties() {
        Properties projectProperties = new Properties();

        try {
            InputStream is = new FileInputStream(
                    new File("./" + PROJECT_PROPERTIES_FILE_NAME));
            projectProperties.load(is);
        } catch (IOException e) {
            InputStream is = ApplicationListener.class
                    .getResourceAsStream("/" + PROJECT_PROPERTIES_FILE_NAME);
            try {
                projectProperties.load(is);
            } catch (IOException e1) {
                LogHelper.log(log, "Project properties hasn't been read.");
            }
        }

        return projectProperties;
    }

    public static String getDocumentSavePath() {
        return getApplicationProperties().getProperty("documentSavePath");
    }

    public static String getCommunicationFilePath() {
        return getApplicationProperties().getProperty("communicationFilePath");
    }

    public static String getPathToXml() {
        return getApplicationProperties().getProperty("pathToXlsx");
    }

    public static String getDocumentSavePathForThirdApp() {
        return getApplicationProperties()
                .getProperty("documentSavePathForThirdPartApp");
    }

    public static String getThirdPartAppURL() {
        return getApplicationProperties().getProperty("thirdPartAppURL");
    }

    public static File getFileFromUpload(UploadedFile uploadedFile) {
        String sb = FileHelper.getDocumentSavePath() +
                DateTimeHelper.ToFilePathString(new Date()) +
                UserHolder.getInstance().getCurrentUser().getId() +
                "\\";
        File filePath = new File(sb);
        String fileName = "";
        try {
            fileName = writeFileToFolder(uploadedFile.getFileName(), filePath, uploadedFile.getContents());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new File(fileName);
    }

    public static void downloadFiles(Map<String, byte[]> files) throws IOException {
        if (!ValidationHelper.isNullOrEmpty(files)) {
            FileHelper.sendZippedFile(files);
        }
    }

    public static void sendZippedFile(Map<String, byte[]> files)
            throws IOException {
        ByteArrayOutputStream baos = null;
        ZipOutputStream zos = null;
        String zipName = "documents";

        baos = new ByteArrayOutputStream();
        zos = new ZipOutputStream(baos);
        zos.putNextEntry(new ZipEntry(zipName + "/"));

        for (String fileName : files.keySet()) {
            ZipEntry entry = new ZipEntry(zipName + "/" + fileName);
            entry.setSize(files.get(fileName).length);
            zos.putNextEntry(entry);
            zos.write(files.get(fileName));
        }
        zos.closeEntry();
        zos.close();
        String randomFileName = zipName + ".zip";

        FileHelper.writeFileToFolder(randomFileName,
                new File(FileHelper.getLocalFileDir()),
                baos.toByteArray());
        RedirectHelper.sendRedirect("/File/" + randomFileName, true);
    }

    public static String getFatturaAPITemplatePath() {
        return getApplicationProperties().getProperty("fatturaAPITemplatePath");
    }
}
