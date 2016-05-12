/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.on.daed.mailer.services.jobs;

import br.on.daed.mailer.services.mails.Mail;
import br.on.daed.mailer.services.mails.MailDLO;
import br.on.daed.mailer.services.contas.*;
import br.on.daed.mailer.services.controllers.MailerController;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.mail.MessagingException;
import javax.transaction.Transactional;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

/**
 *
 * @author csiqueira
 */
@PropertySource("classpath:mailer.properties")
@Service
public class JobDLO {

	@Autowired
	private JobDAO dao;

	@Autowired
	private EmailLinkDAO emailLinkDAO;

	@Autowired
	private EmailClickDAO emailClickDAO;

	@Autowired
	private ContaDLO contaDLO;

	@Value("${servidor.endereco}")
	private String ENDERECO_SERVIDOR;
	
	private final String UNSUB_LINK_MARK = "%%%UNSUB_LINK%%%";

	static final Pattern HREF_PATTERN = Pattern.compile(" href=\"(.*?)\"");

	/* src: http://stackoverflow.com/questions/2221413/how-to-encode-a-mapstring-string-as-base64-string */
	public static String serialize(Object object) throws IOException {
		ByteArrayOutputStream byteaOut = new ByteArrayOutputStream();
		GZIPOutputStream gzipOut = null;
		try {
			gzipOut = new GZIPOutputStream(new Base64OutputStream(byteaOut));
			gzipOut.write(new Gson().toJson(object).getBytes("UTF-8"));
		} finally {
			if (gzipOut != null) {
				try {
					gzipOut.close();
				} catch (IOException logOrIgnore) {
				}
			}
		}
		return new String(byteaOut.toByteArray());
	}

	/* src: http://stackoverflow.com/questions/2221413/how-to-encode-a-mapstring-string-as-base64-string */
	public static <T> T deserialize(String string, Type type) throws IOException {
		ByteArrayOutputStream byteaOut = new ByteArrayOutputStream();
		GZIPInputStream gzipIn = null;
		try {
			gzipIn = new GZIPInputStream(new Base64InputStream(new ByteArrayInputStream(string.getBytes("UTF-8"))));
			for (int data; (data = gzipIn.read()) > -1;) {
				byteaOut.write(data);
			}
		} finally {
			if (gzipIn != null) {
				try {
					gzipIn.close();
				} catch (IOException logOrIgnore) {
				}
			}
		}
		return new Gson().fromJson(new String(byteaOut.toByteArray()), type);
	}

	public void indexStats(ModelMap map) {
		long countClicks = emailClickDAO.count();
		long countLinksGerados = emailLinkDAO.count();

		map.addAttribute("totalLinks", countLinksGerados);
		map.addAttribute("totalLinksClicados", countClicks);
	}

	public void criarEmailClick(EmailLink link, Conta conta) {
		EmailClick ec = new EmailClick();
		ec.setEmail(conta);
		ec.setLink(link);
		ec.setDataclick(ZonedDateTime.now());
		link.getClicados().add(ec);
	}

	public String persistUserClick(String data) throws IOException {
		Map<String, String> deserialize = JobDLO.deserialize(data, new TypeToken<Map<String, String>>() {
		}.getType());
		Long id = Long.parseLong(deserialize.get("linkId"));
		Long contaId = Long.parseLong(deserialize.get("userId"));

		EmailLink link = emailLinkDAO.findOne(id);
		Conta conta = contaDLO.findById(contaId);

		criarEmailClick(link, conta);

		String ret = link.getUrl();

		emailLinkDAO.flush();

		return ret;
	}

	public void replaceLinksAndSend(Mail m, Job job) throws IOException, MessagingException {

		String baseBody = m.getBody();
		int idxMail = m.getCountEnviados();

		for (EmailLink link : job.getLinks()) {
			Map<String, String> map = new HashMap();

			map.put("linkId", link.getId().toString());
			map.put("userId", m.getTo().get(idxMail).getId().toString());

			String encoded = serialize(map);

			String baseUrl = link.getUrl();
			String encodedUrl = "http://" + ENDERECO_SERVIDOR + "/" + MailerController.REQUEST_MAPPING_URL + "?d=" + encoded;
			String replaced = m.getBody().replaceFirst(" href=\"" + baseUrl + "\"", " href=\"" + encodedUrl + "\"");
			m.setBody(replaced);
		}

		MailDLO.sendMail(m);

		m.setBody(baseBody);

		dao.save(job);
	}

	public void terminateJob(Job j) {
		j.setTerminated(true);
		dao.save(j);
	}

	public Job createJob(final Mail m) {
		final Job j = new Job();

		j.setMail(m);

		String mailBody = m.getBody();
		
		if(mailBody.contains("</body>")) {
			String unsubLinkTag = "<h6 style=\"text-align: center; width: 100%; margin-top: 5%;\">Se você não quiser mais emails do sistema de mala direta da Divisão de Atividades Educacionais do Observatório Nacional, clique <a href='" + UNSUB_LINK_MARK + "'>aqui</a>.</h6></body>";
			mailBody = mailBody.replace("</body>", unsubLinkTag);
		}
		
		m.setBody(mailBody);
		
		Matcher matcher = HREF_PATTERN.matcher(mailBody);

		final List<String> urls = new ArrayList();

		while (matcher.find()) {
			String url = matcher.group(1);
			urls.add(url);
			/* adiciona cada link individualmente, mesmo se repetido */
		}

		urls.forEach(new Consumer<String>() {

			@Override
			public void accept(String url) {
				EmailLink el = new EmailLink();

				el.setJob(j);
				el.setUrl(url);
				j.getLinks().add(el);
			}

		});

		dao.save(j);

		return j;
	}

	public Page<Job> getJobLog(Integer pageNumber, String assunto) {
		PageRequest request
				= new PageRequest(pageNumber - 1, MailerController.PAGE_SIZE, Sort.Direction.DESC, "criadoem");

		if (assunto == null) {
			assunto = "";
		}

		return dao.findByAssunto(assunto, request);
	}

	public Page<EmailClick> getClickLog(Integer pageNumber, String email, String url) {
		PageRequest request
				= new PageRequest(pageNumber - 1, MailerController.PAGE_SIZE, Sort.Direction.DESC, "dataclick");

		if (email == null) {
			email = "";
		}

		if (url == null) {
			url = "";
		}

		Page<EmailClick> ret = emailClickDAO.findByContaEmailUrl(email, url, request);

		return ret;
	}

	public Page<EmailLink> getLinkLog(Integer pageNumber, String assunto, String url) {
		PageRequest request
				= new PageRequest(pageNumber - 1, MailerController.PAGE_SIZE, Sort.Direction.DESC, "job.id", "id");

		if (assunto == null) {
			assunto = "";
		}

		if (url == null) {
			url = "";
		}

		return emailLinkDAO.findByAssuntoUrl(assunto, url, request);
	}

	public Job getCurrentJob() {
		Job j = dao.findTop1ByTerminatedFalseOrderByCriadoemAsc();
		return j;
	}

	private static final int LIMIT_WORK = 1;

	@Transactional
	public boolean work(Job job) throws IOException {

		job = dao.findOne(job.getId());
		
		Mail m = job.getMail();

		String baseBody = m.getBody();

		for (int i = 0; i < LIMIT_WORK && m.getCountEnviados() < m.getCountTotal(); i++) {

			int idxMail = m.getCountEnviados();
			
			String contaId = m.getTo().get(idxMail).getId().toString();
			
			for (EmailLink link : job.getLinks()) {
				Map<String, String> map = new HashMap();

				map.put("linkId", link.getId().toString());
				map.put("userId", contaId);

				String encoded = serialize(map);

				String baseUrl = link.getUrl();
				String encodedUrl = "http://" + ENDERECO_SERVIDOR + "/" + MailerController.REQUEST_MAPPING_URL + "?d=" + encoded;
				String replaced = m.getBody().replaceFirst(" href=\"" + baseUrl + "\"", " href=\"" + encodedUrl + "\"");
				m.setBody(replaced);
			}

			if(m.getBody().contains(UNSUB_LINK_MARK)) {
				String replaced = m.getBody();
				String unsubUrl = "http://" + ENDERECO_SERVIDOR + "/" + MailerController.REQUEST_UNSUB_URL + "?id=" + contaId;
				replaced = replaced.replace(UNSUB_LINK_MARK, unsubUrl);
				m.setBody(replaced);
			}
			
			MailDLO.sendMail(m);
		}

		m.setBody(baseBody);

		if(m.getCountEnviados().equals(m.getCountTotal())) {
			job.setTerminated(true);
		}
		
		dao.save(job);

		return job.isTerminated();
	}

}
