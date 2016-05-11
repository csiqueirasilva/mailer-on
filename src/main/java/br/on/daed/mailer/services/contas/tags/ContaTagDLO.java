/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.on.daed.mailer.services.contas.tags;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author csiqueira
 */
@Service
public class ContaTagDLO {

    @Autowired
    private ContaTagDAO dao;

	public ContaTag findByTag(String tag) {
        return dao.findByTag(tag);
    }
	
    public Iterable<ContaTag> findAll() {
        return dao.findAll();
    }

	public List<ContaTag> findByString(String source) {
		List<ContaTag> tags = new ArrayList();

		String[] split = source.split(",");

		for (String tagStr : split) {
			ContaTag findByTag = findByTag(tagStr);
			if(findByTag != null) {
				tags.add(findByTag);
			}
		}
		
		return tags;
	}
	
	public List<ContaTag> persistTags(Collection<String> tags) {

		List<String> transformedTags = new ArrayList();
		
		for(String tag : tags) {
			transformedTags.add(tag.toUpperCase());
		}
		
		List<ContaTag> foundTags = dao.findByTagIn(transformedTags);
		
		for(String tag : tags) {
			String uppercaseTag = tag.toUpperCase();

			boolean possuiTag = false;
			for(int i = 0; i < foundTags.size() && !possuiTag; i++) {
				ContaTag contaTag = foundTags.get(i);
				if(contaTag.getTag().equals(uppercaseTag)) {
					possuiTag = true;
				}
			}

			if(!possuiTag) {
				ContaTag t = new ContaTag();
				t.setTag(uppercaseTag);
				foundTags.add(t);
			}
		}

		dao.save(foundTags);
		dao.flush();
		
		return foundTags;
    }
	
}