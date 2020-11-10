package com.innova.dto.request;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class CreateArticleForm {

    @NotBlank
    private String content;

    @NotBlank
    private boolean published;

    @NotBlank
    private String contentType;

    @NotBlank
    private int readTime;

    @NotBlank
    @Max(50)
    private String heading;

    public CreateArticleForm() {
    }

    public CreateArticleForm(String content, boolean published, String contentType, int readTime, String heading) {
        this.content = content;
        this.published = published;
        this.contentType = contentType;
        this.readTime = readTime;
        this.heading = heading;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean getPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getReadTime() {
        return readTime;
    }

    public void setReadTime(int readTime) {
        this.readTime = readTime;
    }

    public String getHeading() {
        return this.heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }
}
