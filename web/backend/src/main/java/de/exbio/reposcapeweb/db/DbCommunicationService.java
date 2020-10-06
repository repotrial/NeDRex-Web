package de.exbio.reposcapeweb.db;

import org.springframework.stereotype.Service;

@Service
public class DbCommunicationService {
    private boolean updateInProgress = false;
    private boolean importInProgress = false;
    private boolean dbLocked = false;


    public boolean isDbLocked() {
        return dbLocked;
    }

    public boolean isImportInProgress() {
        return importInProgress;
    }

    public boolean isUpdateInProgress() {
        return updateInProgress;
    }

    public boolean setUpdateInProgress(boolean value) {
        if (!updateInProgress & value & importInProgress)
            return false;

        this.updateInProgress = value;
        return true;
    }

    public boolean setImportInProgress(boolean value) {
        if (!importInProgress & value & dbLocked)
            return false;
        this.importInProgress = value;
        return true;
    }

    public boolean setDbLocked(boolean value) {
        if (!dbLocked & value & importInProgress)
            return false;
        this.dbLocked = value;
        return true;
    }

    public void scheduleImport(boolean allowOnUpdate) {
        if (!allowOnUpdate)
            while (!setImportInProgress(true)) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        else
            importInProgress = true;
    }

    public void scheduleUpdate() {
        while (!setUpdateInProgress(true)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void scheduleRead() {
        while (isDbLocked()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void scheduleLock() {
        while (!setDbLocked(true)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
