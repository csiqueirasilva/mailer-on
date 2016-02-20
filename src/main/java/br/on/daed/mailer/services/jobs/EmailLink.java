package br.on.daed.mailer.services.jobs;

import br.on.daed.mailer.services.contas.Conta;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import org.hibernate.annotations.Type;

/**
 *
 * @author csiqueira
 */
@Entity
@SequenceGenerator(name = "seq_email_link", allocationSize = 1, initialValue = 1)
public class EmailLink implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_email_link")
    private Long id;

    @Column
    private String url;

    @OneToMany(mappedBy = "link", cascade = CascadeType.ALL)
    private List<EmailClick> clicados;

    @ManyToOne
    private Job job;

    public EmailLink() {
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<EmailClick> getClicados() {
        return clicados;
    }

    public void setClicados(List<EmailClick> clicados) {
        this.clicados = clicados;
    }

}
