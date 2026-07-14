package ditda.notification.mail;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailListener {

	private final EmailSender emailSender;

	@RabbitListener(queues = MailRabbitConfig.MAIL_QUEUE)
	public void handle(MailMessage message) throws MessagingException {
		log.info("[Email] Mail message received. type: {}, to: {}", message.type(), MailMasker.mask(message.to()));

		NotificationType type = NotificationType.valueOf(message.type());
		emailSender.send(message.to(), type, message.variables());
	}
}
