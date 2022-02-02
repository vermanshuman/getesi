package it.nexera.ris.web.services.base;

import it.nexera.ris.common.enums.ImportSettingsType;
import it.nexera.ris.common.enums.SessionNames;
import it.nexera.ris.common.helpers.FileHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.settings.ApplicationSettingsHolder;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseImportService extends BaseDBService implements Serializable {

    private static final long serialVersionUID = 1888680776378161634L;

    private ImportSettingsType currentType;

    public BaseImportService(SessionNames name, ImportSettingsType type) {
        super(name);
        currentType = type;
    }

    protected abstract void importFunction(File file) throws Exception;

    @Override
    protected void routineFuncInternal() {
        LogHelper.log(log, getServiceName().name() + ": Start");
        try {
            String poolTime = ApplicationSettingsHolder.getInstance()
                    .getByKey(getCurrentType().getIntervalKey()).getValue();
            String filePath = ApplicationSettingsHolder.getInstance()
                    .getByKey(getCurrentType().getPathKey()).getValue();
            if (ValidationHelper.isNullOrEmpty(poolTime) || ValidationHelper.isNullOrEmpty(filePath)) {
                LogHelper.log(log, getServiceName().name() + ": import settings not configured");
                LogHelper.log(log, getServiceName().name() + ": stop import");
                return;
            }
            File folder = new File(filePath);
            if (!folder.exists() || !folder.isDirectory()) {
                LogHelper.log(log, getServiceName().name() + ": import settings configured incorrect");
                LogHelper.log(log, getServiceName().name() + ": stop import");
                return;
            }
            List<File> files = Arrays.stream(folder.listFiles()).collect(Collectors.toList());
            List<String> filesToDelete = new LinkedList<>();
            Integer numFiles = getNumberOfFilesPerIteration();
            if (numFiles != null) {
                LogHelper.log(log, getServiceName().name() + ": number of files per iteration is " + numFiles);
            }
            while (files != null && files.size() != 0) {
                if (numFiles != null && filesToDelete.size() == numFiles) {
                    break;
                }
                try {
                    File file = files.get(0);
                    LogHelper.log(log, getServiceName().name() + ": start importing file: " + file.getPath());
                    importFunction(file);
                    files.remove(0);
                    filesToDelete.add(file.getPath());
                } catch (Exception e) {
                    LogHelper.log(log, getServiceName().name() + ": something went wrong");
                    LogHelper.log(log, e);
                    LogHelper.log(log, getServiceName().name() + ": stop import file");
                }
            }
            folder = null;
            for (String str : filesToDelete) {
                boolean deleted = false;
                deleted = FileHelper.delete(str);
                if (deleted) {
                    LogHelper.log(log, getServiceName().name() + ": delete file: " + str);
                } else {
                    LogHelper.log(log, getServiceName().name() + ": can not delete file: " + str);
                }
            }
        } catch (Exception e) {
            LogHelper.log(log, getServiceName().name() + ": something went wrong");
            LogHelper.log(log, e);
            LogHelper.log(log, getServiceName().name() + ": stop import");
        }
        LogHelper.log(log, getServiceName().name() + ": Stop");
    }

    private Integer getNumberOfFilesPerIteration() {
        if (ImportSettingsType.FORMALITY == getCurrentType()) {
            String number = ApplicationSettingsHolder.getInstance()
                    .getByKey(getCurrentType().getNumberFilesKey()).getValue();
            if (!ValidationHelper.isNullOrEmpty(number)) {
                return Integer.parseInt(number);
            }
        }
        return null;
    }

    /**
     * @return minutes
     */
    @Override
    protected int getPollTimeKey() {
        String poll = ApplicationSettingsHolder.getInstance()
                .getByKey(getCurrentType().getIntervalKey()).getValue();
        if (!ValidationHelper.isNullOrEmpty(poll)) {
            return Integer.parseInt(poll);
        } else {
            return 30;
        }
    }

    /**
     * sets milliseconds
     */
    @Override
    protected void updateSleepTime() {
        sleepTimeMs = getPollTimeKey() * 1000 * 60;
    }

    public ImportSettingsType getCurrentType() {
        return currentType;
    }
}
