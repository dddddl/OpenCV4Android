package com.ddddl.opencvdemo.ui.baidu;

public class BodyBean implements Cloneable {


    /**
     * log_id : 716033439
     * labelmap : xxxx
     * scoremap : xxxx
     * foreground : xxxx
     */

    private long log_id;
    private String labelmap;
    private String scoremap;
    private String foreground;

    public long getLog_id() {
        return log_id;
    }

    public void setLog_id(long log_id) {
        this.log_id = log_id;
    }

    public String getLabelmap() {
        return labelmap;
    }

    public void setLabelmap(String labelmap) {
        this.labelmap = labelmap;
    }

    public String getScoremap() {
        return scoremap;
    }

    public void setScoremap(String scoremap) {
        this.scoremap = scoremap;
    }

    public String getForeground() {
        return foreground;
    }

    public void setForeground(String foreground) {
        this.foreground = foreground;
    }
}
