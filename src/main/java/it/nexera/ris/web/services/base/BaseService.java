package it.nexera.ris.web.services.base;

import it.nexera.ris.common.helpers.LogHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BaseService implements Runnable {

    private static final int SLEEP_TIME_MULTIPLIER = 30;

    protected final Log log = LogFactory
            .getLog(getClass());

    protected volatile Object monitor;

    protected volatile long sleepTimeMs;


    private ExecutorService executorService;

    protected String name;

    private boolean isRunning;
    
    private boolean isPaused;

    private boolean notWaitBeforeStop;
    
    protected boolean stopFlag;

    private Date lastStartTime;

    public BaseService(String name) {
        stopFlag = true;
        this.name = name;

        monitor = new Object();
    }

    public void start() {
    	preStart();
    	
        executorService = Executors
                .newSingleThreadExecutor(new ThreadFactoryEx(name));
        System.out.println("Running " + name + "...");
        stopFlag = false;
        isPaused = false;
        executorService.execute(this);
        
        postStart();
    }
    
    protected void preStart() {
    	
    }
    
    protected void postStart() {
    	
    }

    public void stop() {
    	preStop();
    	
        stopFlag = true;
        isPaused = false;
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
            executorService = null;
        }
        
        postStop();
    }
    
    protected void preStop() {
    	
    }
    
    protected void postStop() {
    	
    }   
    
    public void pause() {
        System.out.println("Pausing " + name + "...");
    	
    	isPaused = true;
    	onPause();
    }
    
    protected void onPause() {
    	
    }
    
    public void resume() {
        System.out.println("Resuming " + name + "...");
    	
    	isPaused = false;
    	onResume();
    }
    
    protected void onResume() {
    }
    
    public void startOrPause() {
    	if (isRunning && isPausingSupported()) {
    		if (!isPaused) pause();
    		else resume();
    	}
    	else start();
    }
    
    public boolean isPausingSupported() {
    	return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        isRunning = true;
        synchronized (monitor) {
            try {
                if(!notWaitBeforeStop) {
                    monitor.wait(1 * 30 * 1000);
                }
            } catch (InterruptedException e) {
                isRunning = false;
                System.out.println("interrupted.. " + name);
                return;
            }
        }

        // Do not use while(true)
        int i = 0;

        while (i < 10) {
            lastStartTime = new Date();
            if (monitor == null || stopFlag) {
                isRunning = false;
                return;
            }

            //LogHelper.debugInfo(log, this.name + " started");

            if(!isPaused)
            	runInternal();

            if (monitor != null && !stopFlag) {
                synchronized (monitor) {
                    try {
                        monitor.wait(sleepTimeMs);
                    } catch (InterruptedException e) {
                        isRunning = false;
                        System.out.println("interrupted.. " + name);
                        return;
                    }
                }
            } else {
                // Object will be destroyed
                isRunning = false;
                onDestroy();
                return;
            }

            i++;
            if (i > 5) {
                i = 0;
            }
        }
        isRunning = false;
    }

    protected void runInternal() {
        try {
            preRoutineFuncInternal();
            preRoutineFunc();
            routineFunc();
            postRoutineFunc();

        } catch (Exception e) {
            isRunning = false;
            LogHelper.log(log, e);
        } finally {
            postRoutineFuncInternal();
        }
    }

    protected void onDestroy() {

    }

    private void preRoutineFuncInternal() {
        if (stopFlag) {
            return;
        }

        updateSleepTime();
    }

    protected void postRoutineFuncInternal() {
        if (stopFlag) {
            return;
        }
    }

    protected final void routineFunc() {
        if (stopFlag) {
            return;
        }

        routineFuncInternal();
    }

    protected abstract void routineFuncInternal();

    protected void preRoutineFunc() {
        if (stopFlag) {
            return;
        }
    }

    protected void postRoutineFunc() {
        if (stopFlag) {
            return;
        }
    }

    // Poll time methods

    protected abstract int getPollTimeKey();

    protected void updateSleepTime() {
        sleepTimeMs = getPollTimeKey() * SLEEP_TIME_MULTIPLIER * 1000;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        if (monitor != null) {
            synchronized (monitor) {
                monitor = null;
            }
        }

        super.finalize();
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }
    
    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean isPaused) {
        this.isPaused = isPaused;
    }

    public Date getLastStartTime() {
        return lastStartTime;
    }

    public void setLastStartTime(Date lastStartTime) {
        this.lastStartTime = lastStartTime;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void setNotWaitBeforeStop(boolean notWaitBeforeStop) {
        this.notWaitBeforeStop = notWaitBeforeStop;
    }

    public boolean isNotWaitBeforeStop() {
        return notWaitBeforeStop;
    }

    public boolean isStopFlag() {
        return stopFlag;
    }

    public void setStopFlag(boolean stopFlag) {
        this.stopFlag = stopFlag;
    }
}
