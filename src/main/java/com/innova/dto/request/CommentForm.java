package com.innova.dto.request;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

public class CommentForm {

    @NotBlank
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
