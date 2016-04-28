/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.on.daed.mailer.services.contas;

import br.on.daed.mailer.services.Mailer;
import br.on.daed.mailer.services.controllers.MailerController;
import br.on.daed.mailer.services.jobs.Job;
import com.google.common.collect.Iterables;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

/**
 *
 * @author csiqueira
 */
@Service
public class ContaDLO {

    @Autowired
    private ContaDAO contaDAO;

    public Iterable<Conta> findByEmail(Iterable<String> emails) {
        return contaDAO.findByEmailIn(emails);
    }

    public Iterable<Conta> getEnabled() {
        return contaDAO.findByEnabled(true);
    }

    public Iterable<Conta> findAll() {
        return contaDAO.findAll();
    }

    public void indexStats(ModelMap map) {
        long total = contaDAO.count();
        long ativos = Iterables.size(contaDAO.findByEnabled(true));
        long inativos = total - ativos;

        map.addAttribute("totalContas", total);
        map.addAttribute("contasInativas", inativos);
        map.addAttribute("contasAtivas", ativos);
    }

    public Integer inserirEmails(List<String> emails) {

        final Set<String> purgedEmails = new TreeSet(emails);

        try {
            Iterable<Conta> findByEmail = contaDAO.findByEmailIn(emails);

            findByEmail.forEach(new Consumer<Conta>() {

                @Override
                public void accept(Conta t) {
                    purgedEmails.remove(t.getEmail());
                }

            });

        } catch (NullPointerException e) {
        }

        final List<Conta> adicionar = new ArrayList();

        purgedEmails.forEach(new Consumer<String>() {

            @Override
            public void accept(String t) {
                if (Mailer.validar(t)) {
                    Conta c = new Conta();
                    c.setEnabled(true);
                    c.setEmail(t);
                    ZonedDateTime dt = ZonedDateTime.now();
                    c.setCriadoem(dt);
                    c.setEditadoem(dt);
                    adicionar.add(c);
                }
            }

        });

        contaDAO.save(adicionar);

        return adicionar.size();
    }

    public Conta findByEmail(String email) {
        return contaDAO.findByEmail(email);
    }

    public Conta adicionarConta(String email, Boolean contaAtiva) {
        Conta c = null;

        if (contaDAO.findByEmail(email) == null) {
            c = new Conta();
            c.setEnabled(contaAtiva);
            c.setEmail(email);
            ZonedDateTime dt = ZonedDateTime.now();
            c.setCriadoem(dt);
            c.setEditadoem(dt);
            contaDAO.save(c);
        }

        return c;
    }

    public Page<Conta> getContaLog(Integer pageNumber, String email) {
        PageRequest request
                = new PageRequest(pageNumber - 1, MailerController.PAGE_SIZE, Sort.Direction.ASC, "email");
        
        if(email == null) {
            email = "";
        }
        
        return contaDAO.findByEmailLike(email, request);

    }
}