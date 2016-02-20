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
 * @author caio
 */
@Repository
public interface EmailClickDAO extends JpaRepository<EmailClick, Long> {

    @Query("SELECT c FROM EmailClick c WHERE c.conta.email LIKE CONCAT('%',:email,'%') AND c.link.url LIKE CONCAT('%',:url,'%')")
    Page<EmailClick> findByContaEmailUrl(@Param("email") String email, @Param("url") String url, Pageable page);
}
