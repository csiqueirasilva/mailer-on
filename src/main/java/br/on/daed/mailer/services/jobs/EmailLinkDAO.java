/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.on.daed.mailer.services.jobs;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author csiqueira
 */
@Repository
public interface EmailLinkDAO extends JpaRepository<EmailLink, Long> {

    @Query("SELECT l FROM EmailLink l WHERE l.job.mail.subject LIKE CONCAT('%',:assunto,'%') AND l.url LIKE CONCAT('%',:url,'%')")
    Page<EmailLink> findByAssuntoUrl(@Param("assunto") String assunto, @Param("url") String url, Pageable page);

}
