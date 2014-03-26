package org.jbake.app.creator;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

public class FileMetadata {

    private final String type;
    private final String status;
    private String title;
    private String date;
    private String tags;

    public FileMetadata(String type, String status) {
        this.type = StringUtils.defaultIfBlank(type, "post");
        this.status = StringUtils.defaultIfBlank(status, "draft");
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public boolean hasTitle() {
        return StringUtils.isNotBlank(getTitle());
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean hasDate() {
        return date != null;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        if (StringUtils.isBlank(date)) {
            this.date = null;
        } else if (date.equals("today")) {
            this.date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        } else {
            this.date = date;
        }
    }

    public boolean hasTags() {
        return StringUtils.isNotBlank(getTags());
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}
