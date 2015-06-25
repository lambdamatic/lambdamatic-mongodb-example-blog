/**
 * 
 */
package org.lambdamatic.example.blog.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.lambdamatic.example.blog.domain.BlogEntry;

import com.github.mustachejava.Mustache;

/**
 * @author xcoulon
 *
 */
@Provider
@Produces("text/html")
public class BlogEntryMessageBodyWriter implements MessageBodyWriter<BlogEntry> {

	@Inject
	private TemplateService templateService;

	@Context
	private ServletContext servletContext;

	@Override
	public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations,
			final MediaType mediaType) {
		return type.equals(BlogEntry.class);
	}

	@Override
	public long getSize(final BlogEntry t, final Class<?> type, final Type genericType, final Annotation[] annotations,
			final MediaType mediaType) {
		return -1;
	}

	@Override
	public void writeTo(final BlogEntry blogEntry, final Class<?> type, final Type genericType,
			final Annotation[] annotations, final MediaType mediaType, final MultivaluedMap<String, Object> httpHeaders,
			final OutputStream entityStream) throws IOException, WebApplicationException {

		final Mustache template = templateService.getTemplate(this.servletContext, "blogentry.mustache");
		final Map<String, Object> properties = new HashMap<>();
		properties.put("title", blogEntry.getTitle());
		properties.put("authorName", blogEntry.getAuthorName());
		properties.put("publishDate", blogEntry.getPublishDate());
		properties.put("commentsNumber", blogEntry.getCommentsNumber());
		properties.put("comments", blogEntry.getComments());
		// content is splitted into paragraphs here
		final String content = blogEntry.getContent();
		properties.put("paragraphs", content.split("   "));
		final String sendCommentFormAction = servletContext.getContextPath() + "/rest/blogentries/" + blogEntry.getId() + "/comments/";
		properties.put("sendCommentFormAction", sendCommentFormAction);
		
		try (final OutputStreamWriter writer = new OutputStreamWriter(entityStream)) {
			template.execute(writer, properties);
			writer.flush();
		}
	}

}
