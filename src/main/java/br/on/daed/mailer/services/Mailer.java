package br.on.daed.mailer.services;

import br.on.daed.mailer.services.contas.Conta;
import br.on.daed.mailer.services.contas.ContaDLO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author caio
 */
@Service
public class Mailer {

	@Autowired
	private ContaDLO contaDLO;

	final private static String EMAIL_PATTERN
			= "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	final private static Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);

	public static Boolean validar(String email) {
		Matcher matcher = emailPattern.matcher(email);
		return matcher.matches();
	}

	public Mail criarMail(String to, String user, String password, String corpo, String assunto) throws FileNotFoundException, IOException {
		Mail m = null;

		Conta conta = contaDLO.findByEmail(to);

		if (conta != null) {
			m = new Mail();

			m.setUser(user);
			m.setPassword(password);
			m.setBody(corpo);
			m.setSubject(assunto);
			m.getTo().add(conta);
			m.setCountTotal();
		}

		return m;
	}

	public Mail criarMail(String user, String password, String corpo, String assunto) throws FileNotFoundException, IOException {

		final Mail m = new Mail();

		m.setUser(user);
		m.setPassword(password);
		m.setBody(corpo);
		m.setSubject(assunto);

		contaDLO.getEnabled().forEach(new Consumer<Conta>() {
			@Override
			public void accept(Conta conta) {
				m.getTo().add(conta);
			}
		});

		m.setCountTotal();

		return m;

	}

	private static Session createSmtpSession(String host, String auth, String port, final String user, final String password) {
		Properties props = new Properties();
		props.setProperty("mail.smtp.host", host);
		props.setProperty("mail.smtp.auth", auth);
		props.setProperty("mail.smtp.port", port);
		props.setProperty("mail.transport.protocol", "smtp");
		//props.setProperty("mail.debug", "true");

		return Session.getDefaultInstance(props, new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(user, password);
			}
		});
	}

	public static void sendEmail(String user, String password, String subject, String content, String to) throws SendFailedException, NoSuchProviderException, MessagingException {

		Date sendDate = new Date();
		InternetAddress from = new InternetAddress(user);

		Session mailSession = createSmtpSession("smtp.on.br", "true", "25", user, password);
		Transport transport = mailSession.getTransport("smtp");

		MimeMessage message = new MimeMessage(mailSession);

		message.setSubject(subject);
		message.setFrom(from);
		message.setSentDate(sendDate);

		Multipart multiPart = new MimeMultipart("alternative");

		MimeBodyPart textPart = new MimeBodyPart();
		textPart.setText(Jsoup.parse(content).text(), "utf-8");

		MimeBodyPart htmlPart = new MimeBodyPart();
		htmlPart.setContent(content, "text/html; charset=utf-8");

		multiPart.addBodyPart(textPart);
		multiPart.addBodyPart(htmlPart);
		message.setContent(multiPart);

		message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

		transport.connect();

		transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
	}

	public static void sendMail(Mail m) {
		Integer indiceEnvio = m.getCountEnviados();
		Conta to = m.getTo().get(indiceEnvio++);

		try {
			sendEmail(m.getUser(), m.getPassword(), m.getSubject(), m.getBody(), to.getEmail());
		} catch (Exception e) {
			m.getCountFalhados().put(to, e.getMessage());
		}

		m.setCountEnviados(indiceEnvio);
	}

	public static void main(String args[]) throws Exception {
		String arquivoTo = "";
		String assunto = "Aplicativo criado pelo Observatório Nacional divulga a Previsão de Marés";
		String corpoEmail = "astro-mares-lancamento-09-09-2015.html";
		String user = "nao-responder@on.br";
		String password = "";

		//sendMailFromFile(user, password, arquivoTo, assunto, user, corpoEmail);
		//criarMail(new File(arquivoTo), user, password, user, assunto);
	}
}
