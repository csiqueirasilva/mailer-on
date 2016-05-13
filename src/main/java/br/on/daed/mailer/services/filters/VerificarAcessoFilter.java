/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.on.daed.mailer.services.filters;

import br.on.daed.mailer.services.controllers.MailerController;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UrlPathHelper;

/**
 *
 * @author caio
 */
@Service
@EnableScheduling
public class VerificarAcessoFilter implements Filter {

	private List<String> allowedIPs;
	private final UrlPathHelper urlPathHelper = new UrlPathHelper();

	@Scheduled(fixedDelay = 60000)
	public void getIPs() {
		Path configFile = Paths.get("/opt/mailer.conf");
		try {
			allowedIPs = Files.readAllLines(configFile);
		} catch (IOException ex) {
			Logger.getLogger(VerificarAcessoFilter.class.getName()).log(Level.INFO, null, ex);
			allowedIPs = new ArrayList<String>();
		}
	}

	@Override
	public void init(FilterConfig fc) throws ServletException {
		getIPs();
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		String errorMsg = null;
		
		try {

			HttpServletRequest req = (HttpServletRequest) request;

			String ip = req.getHeader("x-forwarded-for");
			
			if (ip == null) {
				ip = request.getRemoteAddr();
			}

			boolean testIp;

			String servletPath = urlPathHelper.getServletPath(req);
			testIp = !(servletPath.equals("/" + MailerController.REQUEST_UNSUB_URL) || servletPath.equals("/" + MailerController.REQUEST_MAPPING_URL));

			if (testIp && !ip.equals("127.0.0.1") && !ip.equals("::1") && !ip.equals("0:0:0:0:0:0:0:1")) {

				if (!allowedIPs.contains(ip)) {
					errorMsg = "Denied " + ip;
				}
			}

		} catch (Exception e) {
		}

		if(errorMsg != null) {
			throw new UnsupportedOperationException(errorMsg);
		}
		
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
	}

}
