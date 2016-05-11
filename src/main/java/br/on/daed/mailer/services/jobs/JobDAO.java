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
public interface JobDAO extends JpaRepository<Job, Long> {

    @Query("SELECT l FROM Job l WHERE l.mail.subject LIKE CONCAT('%',:assunto,'%')")
    Page<Job> findByAssunto(@Param("assunto") String assunto, Pageable page);

	Job findTop1ByTerminatedFalseOrderByCriadoemAsc();

}