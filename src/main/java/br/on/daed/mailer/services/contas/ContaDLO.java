/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.on.daed.mailer.services.contas;

import br.on.daed.mailer.services.mails.MailDLO;
import br.on.daed.mailer.services.contas.tags.ContaTag;
import br.on.daed.mailer.services.contas.tags.ContaTagDLO;
import br.on.daed.mailer.services.controllers.MailerController;
import br.on.daed.mailer.services.jobs.Job;
import br.on.daed.mailer.services.jobs.JobDLO;
import com.google.common.collect.Iterables;
import com.google.common.reflect.TypeToken;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
    private ContaTagDLO contaTagDLO;

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

    public Integer inserirEmails(List<String> emails, String tags) {

        String[] tagsArray = tags.split(",");

        final Set<String> purgedTags = new TreeSet(Arrays.asList(tagsArray));

        final List<ContaTag> persistedTags = contaTagDLO.persistTags(purgedTags);

        final List<String> parsedEmails = new ArrayList<String>();
        
        for(String email : emails) {
            email = email.replace(" ", "");
            if(MailDLO.validar(email)) {
                parsedEmails.add(email);
            }
        }
        
        final Set<String> purgedEmails = new TreeSet(parsedEmails);
        
        try {
            Iterable<Conta> findByEmail = contaDAO.findByEmailIn(purgedEmails);

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
                if (MailDLO.validar(t)) {
                    Conta c = new Conta();
                    c.setEnabled(true);
                    c.setEmail(t);
                    c.setTags(persistedTags);
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

        if (email == null) {
            email = "";
        }

        return contaDAO.findByEmailLike(email, request);
    }

    public Page<Conta> getContaLogByTag(Integer pageNumber, String tagName) {
        ContaTag tag = contaTagDLO.findByTag(tagName);

        PageRequest request
                = new PageRequest(pageNumber - 1, MailerController.PAGE_SIZE, Sort.Direction.ASC, "email");

        return contaDAO.findByTag(tag, request);
    }

    public List<Conta> getEnabledWithTags(List<ContaTag> contaTags) {
        return contaDAO.findByEnabledTrueAndTagsIn(contaTags);
    }

    public Conta findById(Long contaId) {
        return contaDAO.findOne(contaId);
    }

    public void setDisabled(String data) throws IOException {
        Map<String, String> deserialize = JobDLO.deserialize(data, new TypeToken<Map<String, String>>() {
        }.getType());

        Long id = Long.parseLong(deserialize.get("userId"));

        Conta findOne = contaDAO.findOne(id);
        if (findOne != null) {
            findOne.setEnabled(false);
            contaDAO.saveAndFlush(findOne);
        }
    }

}
