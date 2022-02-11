package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.FileHelper;

public enum FileExtensionIconType {

    FILE("fa fa-file-o", IconColor.WHITE_BLACK, ""),
    ARCHIVE("fa fa-file-archive-o", IconColor.WHITE_BLACK, "7z", "cbr", "deb", "gz", "pkg", "rar", "rpm", "sitx", "tar", "gz", "zip", "zipx"),
    AUDIO("fa fa-file-audio-o", IconColor.LIGHT_BLUE, "aif", "iff", "m3u", "m4a", "mid", "mp3", "mpa", "wav", "wma"),
    DOC("fa fa-file-word-o", IconColor.BLUE, "doc", "docx"),
    XLS("fa fa-file-excel-o", IconColor.GREEN, "xls", "xlsx", "xlr"),
    IMG("fa fa-file-image-o", IconColor.LIGHT_BLUE, "bmp", "dds", "gif", "jpg", "png", "psd", "tga", "thm", "tif", "tiff", "yuv", "ai", "eps", "ps", "svg"),
    PDF("fa fa-file-pdf-o", IconColor.RED, "pdf"),
    PPT("fa fa-file-powerpoint-o", IconColor.RED, "ppt", "pptx", "pps"),
    TXT("fa fa-file-text-o", IconColor.WHITE_BLACK, "log", "txt", "odt", "pages", "rtf"),
    VIDEO("fa fa-file-video-o", IconColor.LIGHT_BLUE, "3g2", "3gp", "asf", "avi", "flv", "m4v", "mov", "mp4", "mpg", "rm", "srt", "swf", "vob", "wmv");

    private String icon;

    private IconColor style;

    private String[] extensions;

    FileExtensionIconType(String icon, IconColor style, String... extensions) {
        this.icon = icon;
        this.style = style;
        this.extensions = extensions;
    }

    public static String getFileIcon(String fileName) {
        String extension = FileHelper.getFileExtension(fileName).replaceFirst(".", "");
        for (FileExtensionIconType type : FileExtensionIconType.values()) {
            for (String typeExt : type.getExtensions()) {
                if (typeExt.equalsIgnoreCase(extension)) {
                    return type.getIcon();
                }
            }
        }
        return FILE.getIcon();
    }

    public static String getFileStyle(String fileName) {
        String extension = FileHelper.getFileExtension(fileName).replaceFirst(".", "");
        for (FileExtensionIconType type : FileExtensionIconType.values()) {
            for (String typeExt : type.getExtensions()) {
                if (typeExt.equalsIgnoreCase(extension)) {
                    return type.getStyle();
                }
            }
        }
        return FILE.getStyle();
    }

    public String getIcon() {
        return icon;
    }

    public String getStyle() {
        return style.getStyle();
    }

    public String[] getExtensions() {
        return extensions;
    }

    enum IconColor {
        WHITE_BLACK("white-black-file"),
        LIGHT_BLUE("light-blue-file"),
        BLUE("blue-file"),
        GREEN("green-file"),
        RED("red-file");

        private String style;

        IconColor(String style) {
            this.style = style;
        }

        public String getStyle() {
            return style;
        }
    }
}
