package com.innova.message.request;

public class ForgotPasswordForm {
    
    private String email;

    public ForgotPasswordForm(){

    }

    public ForgotPasswordForm(String email){
        this.email = email;
    }

    public String getEmail(){
        return this.email;
    }

    public void setEmail(String email){
        this.email = email;
    }
}
