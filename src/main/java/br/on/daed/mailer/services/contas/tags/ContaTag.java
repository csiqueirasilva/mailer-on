/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.on.daed.mailer.services.contas.tags;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

/**
 *
 * @author csiqueira
 */
@Entity
@SequenceGenerator(name = "seq_tag", allocationSize = 1, initialValue = 1)
public class ContaTag implements Serializable {
	
	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_tag")
    private Long id;

	@Column(unique = true)
	private String tag;

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
}
