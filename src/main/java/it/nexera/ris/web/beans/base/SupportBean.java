package it.nexera.ris.web.beans.base;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.*;

@ManagedBean(name = "supportBean")
@ViewScoped
public class SupportBean implements Serializable {

    private static final long serialVersionUID = -5215017908911007651L;

    private static final String VERSION_FILE_PATH = "version.txt";

    private static final String ERROR_READING_SITE_VERSION = "Error reading site version";

    private static final String MIGRATION_PASSWORD = "RISmigrate";

    private String password;

    private Boolean disableMigration;

    private String migrateDBStatusFromOld;

    private String migrateDBStatusFromCurrent;

    public String getSiteVersion() {
        File f = new File(FacesContext.getCurrentInstance()
                .getExternalContext().getRealPath(VERSION_FILE_PATH));

        if (f != null && f.isFile() && f.exists()) {
            try {
                StringBuilder sb = new StringBuilder();
                FileReader reader = new FileReader(f);
                BufferedReader br = new BufferedReader(reader);

                String str;
                while ((str = br.readLine()) != null) {
                    sb.append(str);
                    sb.append("\r\n");
                }

                br.close();
                reader.close();

                return sb.toString();
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            }
        }

        return ERROR_READING_SITE_VERSION;
    }

    public void login() {
        if (MIGRATION_PASSWORD.equals(getPassword())) {
            setDisableMigration(Boolean.FALSE);
            setPassword("");
        }
    }

    public String getMigrateDBStatusFromOld() {
        return migrateDBStatusFromOld;
    }

    public void setMigrateDBStatusFromOld(String migrateDBStatusFromOld) {
        this.migrateDBStatusFromOld = migrateDBStatusFromOld;
    }

    public String getMigrateDBStatusFromCurrent() {
        return migrateDBStatusFromCurrent;
    }

    public void setMigrateDBStatusFromCurrent(String migrateDBStatusFromCurrent) {
        this.migrateDBStatusFromCurrent = migrateDBStatusFromCurrent;
    }

    public Boolean getDisableMigration() {
        return disableMigration == null ? Boolean.TRUE : disableMigration;
    }

    public void setDisableMigration(Boolean disableMigration) {
        this.disableMigration = disableMigration;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
