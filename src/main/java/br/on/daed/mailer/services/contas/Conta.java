/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.on.daed.mailer.services.contas;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.time.ZonedDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 *
 * @author csiqueira
 */
@Entity
@SequenceGenerator(name = "seq_conta", allocationSize = 1, initialValue = 1)
public class Conta implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_conta")
    private Long id;

    @Column(unique = true)
    private String email;

    @Column
    private Boolean enabled;

    @Column
    @JsonIgnore
    private ZonedDateTime criadoem;

    @Column
    @JsonIgnore
    private ZonedDateTime editadoem;

    public ZonedDateTime getCriadoem() {
        return criadoem;
    }

    public void setCriadoem(ZonedDateTime criadoem) {
        this.criadoem = criadoem;
    }

    public ZonedDateTime getEditadoem() {
        return editadoem;
    }

    public void setEditadoem(ZonedDateTime editadoem) {
        this.editadoem = editadoem;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

}
