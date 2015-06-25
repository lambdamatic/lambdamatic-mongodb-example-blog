/**
 * 
 */
package org.lambdamatic.example.blog.rest;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.ServletContext;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

/**
 * @author xcoulon
 *
 */
@ApplicationScoped
public class TemplateService {

	private MustacheFactory mf = new DefaultMustacheFactory();

	public Mustache getTemplate(final ServletContext servletContext, final String templateName) {
		final InputStream templateStream = servletContext.getResourceAsStream("WEB-INF" + File.separator + "templates" + File.separator + templateName);
		return mf.compile(new InputStreamReader(templateStream), templateName);
	}
	
}
