/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.on.daed.mailer.services.contas;

import br.on.daed.mailer.services.contas.tags.ContaTag;
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
public interface ContaDAO extends JpaRepository<Conta, Long> {

    public Iterable<Conta> findByEnabled(Boolean enabled);

    public Iterable<Conta> findByEmailIn(Iterable<String> emails);

    public Conta findByEmail(String email);

    @Query(value = "select c from Conta c where c.email LIKE :partialEmail")
    public Iterable<Conta> findByPartialEmail(@Param("partialEmail") String partialEmail);

    @Query("SELECT c FROM Conta c WHERE c.email LIKE CONCAT('%',:email,'%')")
    Page<Conta> findByEmailLike(@Param("email") String email, Pageable page);

    @Query("SELECT c FROM Conta c WHERE :tag MEMBER OF c.tags")
    Page<Conta> findByTag(@Param("tag") ContaTag tag, Pageable page);
}