package io.github.bardiakz.notification_service.service;

import io.github.bardiakz.notification_service.dto.EmailTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class TemplateService {

    private static final Logger logger = LoggerFactory.getLogger(TemplateService.class);
    private static final String TEMPLATE_PATH = "email-templates/";

    /**
     * Load and process an email template
     */
    public EmailTemplate loadTemplate(String templateName, Map<String, String> variables) {
        try {
            String templateContent = loadTemplateFile(templateName + ".html");
            String processedContent = processTemplate(templateContent, variables);
            
            String subject = extractSubject(processedContent);
            String body = processedContent;

            return new EmailTemplate(templateName, subject, body);
            
        } catch (IOException e) {
            logger.error("Failed to load template: {}", templateName, e);
            throw new RuntimeException("Template loading failed", e);
        }
    }

    /**
     * Load template file from resources
     */
    private String loadTemplateFile(String fileName) throws IOException {
        ClassPathResource resource = new ClassPathResource(TEMPLATE_PATH + fileName);
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    /**
     * Process template by replacing variables
     */
    private String processTemplate(String template, Map<String, String> variables) {
        String processed = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            processed = processed.replace(placeholder, entry.getValue());
        }
        return processed;
    }

    /**
     * Extract subject from template (looks for {{SUBJECT:...}} pattern)
     */
    private String extractSubject(String template) {
        String subjectPattern = "\\{\\{SUBJECT:([^}]+)\\}\\}";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(subjectPattern);
        java.util.regex.Matcher matcher = pattern.matcher(template);
        
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        return "Notification from University Management System";
    }

    /**
     * Create simple text template (fallback when HTML template is not available)
     */
    public EmailTemplate createSimpleTemplate(String subject, String message) {
        String body = "<html><body><p>" + message + "</p></body></html>";
        return new EmailTemplate("simple", subject, body);
    }
}
