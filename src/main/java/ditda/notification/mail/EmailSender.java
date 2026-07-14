package ditda.notification.mail;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.springframework.boot.mail.autoconfigure.MailProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSender {

	private static final String LOGO_CID = "logoImage";
	private static final String LOGO_PATH = "email-images/logo.png";

	private static final String FROM_NAME = "DITDA";

	private final JavaMailSender mailSender;
	private final SpringTemplateEngine templateEngine;
	private final MailProperties mailProperties;

	public void send(String to, NotificationType type, Map<String, Object> variables)
		throws MessagingException {

		MimeMessage mimeMessage = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

		try {
			helper.setFrom(new InternetAddress(mailProperties.getUsername(), FROM_NAME, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("UTF-8 is not supported", e);
		}

		helper.setTo(to);
		helper.setSubject(type.getSubject());
		helper.setText(renderTemplate(type.getTemplate(), variables), true);

		helper.addInline(LOGO_CID, new ClassPathResource(LOGO_PATH));

		mailSender.send(mimeMessage);

		log.info("[Email] Email sent. to={}, type={}", MailMasker.mask(to), type);
	}

	private String renderTemplate(String templateName, Map<String, Object> variables) {
		Context context = new Context();
		variables.forEach(context::setVariable);
		return templateEngine.process(templateName, context);
	}
}
