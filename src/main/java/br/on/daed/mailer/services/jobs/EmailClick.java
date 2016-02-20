/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.on.daed.mailer.services.jobs;

import br.on.daed.mailer.services.contas.Conta;
import java.time.ZonedDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

/**
 *
 * @author caio
 */
@Entity
@SequenceGenerator(name = "seq_email_click", allocationSize = 1, initialValue = 1)
public class EmailClick {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_email_click")
    private Long id;

    @ManyToOne
    private EmailLink link;

    @ManyToOne
    private Conta conta;

    @Column
    private ZonedDateTime dataclick;

    public Conta getConta() {
        return conta;
    }

    public void setConta(Conta conta) {
        this.conta = conta;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EmailLink getLink() {
        return link;
    }

    public void setLink(EmailLink link) {
        this.link = link;
    }

    public Conta getEmail() {
        return conta;
    }

    public void setEmail(Conta email) {
        this.conta = email;
    }

    public ZonedDateTime getDataclick() {
        return dataclick;
    }

    public void setDataclick(ZonedDateTime dataclick) {
        this.dataclick = dataclick;
    }

}
