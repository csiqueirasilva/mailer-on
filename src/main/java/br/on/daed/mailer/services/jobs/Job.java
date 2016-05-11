/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.on.daed.mailer.services.jobs;

import br.on.daed.mailer.services.mails.Mail;
import br.on.daed.mailer.services.contas.Conta;
import br.on.daed.mailer.services.contas.tags.ContaTag;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.MapKey;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.hibernate.annotations.Type;

/**
 *
 * @author csiqueira
 */
@Entity
@SequenceGenerator(name = "seq_job", allocationSize = 1, initialValue = 1)
public class Job implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_job")
	private Long id;

	@OneToMany(mappedBy = "job", cascade = CascadeType.ALL)
	private List<EmailLink> links;

	@Column
	@JsonIgnore
	private ZonedDateTime criadoem;

	@ManyToMany
	private List<ContaTag> tags;

	@OneToOne(cascade = CascadeType.ALL)
	private Mail mail;

	@Column
	private boolean terminated;

	public List<ContaTag> getTags() {
		return tags;
	}

	public void setTags(List<ContaTag> tags) {
		this.tags = tags;
	}

	public Job() {
		this.links = new ArrayList();
		this.criadoem = ZonedDateTime.now();
		terminated = false;
	}

	public boolean isTerminated() {
		return terminated;
	}

	public void setTerminated(boolean terminated) {
		this.terminated = terminated;
	}

	public Mail getMail() {
		return mail;
	}

	public void setMail(Mail mail) {
		this.mail = mail;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ZonedDateTime getCriadoem() {
		return criadoem;
	}

	public void setCriadoem(ZonedDateTime criadoem) {
		this.criadoem = criadoem;
	}

	public List<EmailLink> getLinks() {
		return links;
	}

	public void setLinks(List<EmailLink> links) {
		this.links = links;
	}

}
