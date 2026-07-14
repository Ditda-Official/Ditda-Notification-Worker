package ditda.notification.mail;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MailListener {

	@RabbitListener(queues = MailRabbitConfig.MAIL_QUEUE)
	public void handle(MailMessage message) {
		log.info("메일 메세지 수신. type: {}, to: {}", message.type(), message.to());
	}
}
