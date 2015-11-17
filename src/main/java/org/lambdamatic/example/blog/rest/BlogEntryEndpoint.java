/**
 * 
 */
package org.lambdamatic.example.blog.rest;

import static org.lambdamatic.mongodb.Projection.exclude;
import static org.lambdamatic.mongodb.Projection.include;

import java.util.Date;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Response.Status;

import org.lambdamatic.example.blog.domain.BlogEntry;
import org.lambdamatic.example.blog.domain.BlogEntryCollection;
import org.lambdamatic.example.blog.domain.BlogEntryComment;

/**
 * @author xcoulon
 *
 */
@RequestScoped
@Path("/blogentries")
@Produces({ "application/json", "text/html" })
public class BlogEntryEndpoint {

	@Inject
	private BlogEntryCollection blogEntryCollection;

	@GET
	@Path("/")
	public Response findAll() {
		final List<BlogEntry> blogentries = blogEntryCollection.all()
				.projection(b -> include(b.id, b.authorName, b.title, b.publishDate)).toList();
		final GenericEntity<List<BlogEntry>> result = new GenericEntity<List<BlogEntry>>(blogentries){};
		return Response.ok(result).build();
	}

	@GET
	@Path("/{id}")
	public Response findById(@PathParam("id") final String id) {
		final BlogEntry blogentry = blogEntryCollection.filter(b -> b.id.equals(id)).projection(b -> exclude(b.comments))
				.first();
		if (blogentry == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		return Response.ok(blogentry).build();
	}

	@GET
	@Path("/{id}/comments/")
	public Response getComment(@PathParam("id") String id) {
		final BlogEntry blogEntry = blogEntryCollection.filter(e -> e.id.equals(id)).projection(e -> include(e.comments)).first();
		final List<BlogEntryComment> comments = blogEntry.getComments();
		final GenericEntity<List<BlogEntryComment>> result = new GenericEntity<List<BlogEntryComment>>(comments){};
		return Response.ok(result).build();
	}

	@POST
	@Path("/{id}/comments/")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response addComment(@PathParam("id") String id, @FormParam("authorName") String author, @FormParam("message") String content, @Context final HttpServletRequest httpServletRequest) {
		final BlogEntryComment blogEntryComment = new BlogEntryComment(author, new Date(), content);
		blogEntryCollection.filter(e -> e.id.equals(id)).forEach(e -> {
			e.lastUpdate = new Date();
			e.commentsNumber++;
			e.comments.push(blogEntryComment);
		});
		return Response.seeOther(UriBuilder.fromResource(getClass()).path("/{id}").build(id)).build();
	}

}
