package it.nexera.ris.common.utils;

public class ProcessMonitor {

    private Double endValue;

    private Double startValue;

    private String statusStr;

    private Integer progress;

    public void resetCounters() {
        endValue = null;
        startValue = null;
    }

    public boolean isFineshed() {
        if (progress >= 100) {
            return true;
        }
        return false;
    }

    private String getDivisionString() {
        if (getEndValue() != null && getStartValue() != null) {
            return String.format("(%d/%d)", getEndValue().intValue(), getStartValue().intValue());
        }
        return "";
    }

    public Integer getProgress() {
        if (progress == null
                || getStartValue() == null
                || getEndValue() == null
                || getEndValue() == 0) {
            progress = 0;
        } else {
            progress = (int) ((getStartValue() / getEndValue()) * 100);

            if (progress > 100)
                progress = 100;
        }
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public void setStatusStr(String statusStr) {
        this.statusStr = statusStr;
    }

    public String getStatusStr() {
        if (statusStr == null) {
            return "";
        }
        return statusStr + getDivisionString();
    }

    public Double getEndValue() {
        return endValue;
    }

    public Double getStartValue() {
        return startValue;
    }

    public void setEndValue(Integer endValue) {
        this.endValue = Double.valueOf(endValue);
    }

    public void setStartValue(Integer startValue) {
        this.startValue = Double.valueOf(startValue);
    }
}
