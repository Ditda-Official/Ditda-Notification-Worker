package ditda.notification.mail;

import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MailMasker {

	public static String mask(String email) {
		if (!StringUtils.hasText(email) || !email.contains("@")) {
			return "***";
		}

		int at = email.indexOf("@");
		String local = email.substring(0, at);
		String domain = email.substring(at);

		if (local.length() <= 2) {
			return "****" + domain;
		}

		return local.substring(0, 2) + "****" + domain;
	}
}
