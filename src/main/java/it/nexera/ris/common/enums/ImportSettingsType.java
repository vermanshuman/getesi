package it.nexera.ris.common.enums;

public enum ImportSettingsType {
    PROPERTY(ApplicationSettingsKeys.IMPORT_PROPERTY_PATH,
            ApplicationSettingsKeys.IMPORT_PROPERTY_INTERVAL),
    ESTATE_FORMALITY(ApplicationSettingsKeys.IMPORT_ESTATE_FORMALITY_PATH,
            ApplicationSettingsKeys.IMPORT_ESTATE_FORMALITY_INTERVAL),
    FORMALITY(ApplicationSettingsKeys.IMPORT_FORMALITY_PATH,
            ApplicationSettingsKeys.IMPORT_FORMALITY_INTERVAL,
            ApplicationSettingsKeys.IMPORT_FORMALITY_NUMBER_FILES),
    VISURE_RTF(ApplicationSettingsKeys.IMPORT_VISURE_RTF_PATH,
            ApplicationSettingsKeys.IMPORT_VISURE_RTF_INTERVAL,
            ApplicationSettingsKeys.IMPORT_VISURE_RTF_NUMBER_FILES,
            ApplicationSettingsKeys.IMPORT_VISURE_RTF_PATH_FILES_SERVER),
    VISURE_DH(ApplicationSettingsKeys.IMPORT_VISURE_DH_PATH,
            ApplicationSettingsKeys.IMPORT_VISURE_DH_INTERVAL,
            ApplicationSettingsKeys.IMPORT_VISURE_DH_NUMBER_FILES),
    REQUEST_OLD(ApplicationSettingsKeys.IMPORT_REQUEST_OLD_PATH,
            ApplicationSettingsKeys.IMPORT_REQUEST_OLD_INTERVAL,
            ApplicationSettingsKeys.IMPORT_REQUEST_OLD_NUMBER_FILES),
    REPORT_FORMALITY_SUBJECT(ApplicationSettingsKeys.IMPORT_REPORT_FORMALITY_SUBJECT_PATH,
            ApplicationSettingsKeys.IMPORT_REPORT_FORMALITY_SUBJECT_INTERVAL,
            ApplicationSettingsKeys.IMPORT_REPORT_FORMALITY_SUBJECT_NUMBER_FILES);

    private ApplicationSettingsKeys pathKey;

    private ApplicationSettingsKeys intervalKey;

    private ApplicationSettingsKeys numberFilesKey;

    private ApplicationSettingsKeys pathFilesServer;

    ImportSettingsType(ApplicationSettingsKeys pathKey, ApplicationSettingsKeys intervalKey, ApplicationSettingsKeys numberFilesFey) {
        this.pathKey = pathKey;
        this.intervalKey = intervalKey;
        this.numberFilesKey = numberFilesFey;
    }

    ImportSettingsType(ApplicationSettingsKeys pathKey, ApplicationSettingsKeys intervalKey, ApplicationSettingsKeys numberFilesFey, ApplicationSettingsKeys pathFilesServer) {
        this.pathKey = pathKey;
        this.intervalKey = intervalKey;
        this.numberFilesKey = numberFilesFey;
        this.pathFilesServer = pathFilesServer;
    }

    ImportSettingsType(ApplicationSettingsKeys pathKey, ApplicationSettingsKeys intervalKey) {
        this.pathKey = pathKey;
        this.intervalKey = intervalKey;
    }

    public ApplicationSettingsKeys getPathKey() {
        return pathKey;
    }

    public ApplicationSettingsKeys getIntervalKey() {
        return intervalKey;
    }

    public ApplicationSettingsKeys getNumberFilesKey() {
        return numberFilesKey;
    }

    public ApplicationSettingsKeys getPathFilesServer() {
        return pathFilesServer;
    }

    public boolean hasNumberFilesKey() {
        return numberFilesKey != null;
    }

    public boolean hasPathFilesServer() {
        return pathFilesServer != null;
    }
}
