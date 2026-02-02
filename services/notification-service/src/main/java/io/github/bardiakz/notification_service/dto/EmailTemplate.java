package io.github.bardiakz.notification_service.dto;

public class EmailTemplate {

    private String templateName;
    private String subject;
    private String body;

    public EmailTemplate() {
    }

    public EmailTemplate(String templateName, String subject, String body) {
        this.templateName = templateName;
        this.subject = subject;
        this.body = body;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
