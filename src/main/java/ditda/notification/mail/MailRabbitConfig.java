package ditda.notification.mail;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailRabbitConfig {

	public static final String MAIL_EXCHANGE = "mail.exchange";
	public static final String MAIL_ROUTING_KEY = "mail.send";
	public static final String MAIL_QUEUE = "mail.queue";

	public static final String MAIL_DLX = "mail.dlx";
	public static final String MAIL_DLQ = "mail.dlq";
	public static final String MAIL_DEAD_ROUTING_KEY = "mail.dead";

	@Bean
	DirectExchange mailExchange() {
		return new DirectExchange(MAIL_EXCHANGE);
	}

	@Bean
	Queue mailQueue() {
		return QueueBuilder.durable(MAIL_QUEUE)
			.deadLetterExchange(MAIL_DLX)
			.deadLetterRoutingKey(MAIL_DEAD_ROUTING_KEY)
			.build();
	}

	@Bean
	Binding mailBinding() {
		return BindingBuilder.bind(mailQueue()).to(mailExchange()).with(MAIL_ROUTING_KEY);
	}

	@Bean
	DirectExchange mailDlx() {
		return new DirectExchange(MAIL_DLX);
	}

	@Bean
	Queue mailDlq() {
		return QueueBuilder.durable(MAIL_DLQ).build();
	}

	@Bean
	Binding mailDlqBinding() {
		return BindingBuilder.bind(mailDlq()).to(mailDlx()).with(MAIL_DEAD_ROUTING_KEY);
	}
}
