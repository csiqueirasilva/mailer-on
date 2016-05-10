/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.on.daed.mailer.services.contas.tags;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author csiqueira
 */
@Repository
public interface ContaTagDAO extends JpaRepository<ContaTag, Long> {
    public List<ContaTag> findByTagIn(List<String> tags);
	public ContaTag findByTag(String tag);
}