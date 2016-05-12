/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.on.daed.mailer.services.filters;

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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 *
 * @author caio
 */
@Service
@EnableScheduling
public class VerificarAcessoFilter implements Filter {

    private List<String> allowedIPs;

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

        String ip = request.getRemoteAddr();

        if (!ip.equals("127.0.0.1") && !ip.equals("::1") && !ip.equals("0:0:0:0:0:0:0:1")) {

            if (!allowedIPs.contains(ip)) {
                throw new UnsupportedOperationException("Denied " + ip);
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

}