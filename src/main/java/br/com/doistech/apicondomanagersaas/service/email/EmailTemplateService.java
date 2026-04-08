package br.com.doistech.apicondomanagersaas.service.email;

import br.com.doistech.apicondomanagersaas.common.exception.BadRequestException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class EmailTemplateService {

    public String render(String templatePath, Map<String, String> placeholders, String notFoundMessage) {
        String template = loadTemplate(templatePath, notFoundMessage);
        String rendered = template;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            rendered = rendered.replace("${" + entry.getKey() + "}", escapeHtml(entry.getValue()));
        }
        return rendered;
    }

    public String escapeHtml(String value) {
        return HtmlUtils.htmlEscape(value == null ? "" : value, StandardCharsets.UTF_8.name());
    }

    public String normalizeUrl(String value) {
        if (value == null) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String loadTemplate(String templatePath, String notFoundMessage) {
        try {
            ClassPathResource resource = new ClassPathResource(templatePath);
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new BadRequestException(notFoundMessage);
        }
    }
}
