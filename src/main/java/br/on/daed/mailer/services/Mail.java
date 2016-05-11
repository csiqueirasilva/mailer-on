/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.on.daed.mailer.services;

import br.on.daed.mailer.services.contas.Conta;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;

/**
 *
 * @author csiqueira
 */
@Entity
@SequenceGenerator(name = "seq_mail", allocationSize = 1, initialValue = 1)
public class Mail implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_mail")
	private Long id;

	@ManyToMany
	List<Conta> to;

	@Column
	private String subject;

	@Column(columnDefinition = "TEXT")
	private String body;

	@Column(name = "mailfrom")
	private String from;

	@Column
	private String password;

	@Column(name = "mailuser")
	private String user;

	@Column
	private Integer countEnviados;

	@Column
	private Integer countTotal;

	@ElementCollection
	private Map<Conta, String> countFalhados;

	public Mail() {
		countFalhados = new HashMap();
		to = new ArrayList();
		countEnviados = countTotal = 0;
	}

	public Map<Conta, String> getCountFalhados() {
		return countFalhados;
	}

	public void setCountFalhados(Map<Conta, String> countFalhados) {
		this.countFalhados = countFalhados;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getCountEnviados() {
		return countEnviados;
	}

	public void setCountEnviados(Integer countEnviados) {
		this.countEnviados = countEnviados;
	}

	public Integer getCountTotal() {
		return countTotal;
	}

	public void setCountTotal(Integer countTotal) {
		this.countTotal = countTotal;
	}

	public void incrementarEnviado() {
		countEnviados++;
	}

	public Integer getTotal() {
		return this.countTotal;
	}

	public Integer getEnviados() {
		return this.countEnviados;
	}

	public void setCountTotal() {
		this.countTotal = this.to.size();
	}

	public List<Conta> getTo() {
		return to;
	}

	public void setTo(List<Conta> to) {
		this.to = to;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

}
