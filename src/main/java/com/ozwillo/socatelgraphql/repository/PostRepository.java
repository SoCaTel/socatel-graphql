package com.ozwillo.socatelgraphql.repository;

import com.ozwillo.socatelgraphql.domain.Post;
import com.ozwillo.socatelgraphql.handler.PostTupleQueryResultHandler;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expression;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.core.Prefix;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder.prefix;
import static org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder.var;
import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.*;

@Component
public class PostRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostRepository.class);

    private static Prefix SOCATEL = prefix("socatel", iri("http://www.everis.es/SoCaTel/ontology#"));
    private static Prefix SIOC = prefix("sioc", iri("http://rdfs.org/sioc/ns#"));
    private static Prefix RDF = prefix("rdf", iri("http://www.w3.org/1999/02/22-rdf-syntax-ns#"));
    private static Prefix XSD = prefix("xsd", iri("http://www.w3.org/2001/XMLSchema#"));
    private static Prefix GN = prefix("gn", iri("http://www.geonames.org/ontology#"));
    private static Prefix FOAF = prefix("foaf", iri("http://xmlns.com/foaf/0.1/"));

    private RepositoryConnection repositoryConnection;

    public PostRepository(
            @Value("${graphdb.url}") String url,
            @Value("${graphdb.username}") String username,
            @Value("${graphdb.password}") String password) {
        HTTPRepository repository = new HTTPRepository(url);
        repository.setUsernameAndPassword(username, password);
        this.repositoryConnection = repository.getConnection();
    }

    public ArrayList<Post> getPosts(String creationDateFrom, String creationDateTo, String screenName) {

        ArrayList<Post> postList = new ArrayList<>();

        GraphPattern postGraphPattern = buildPostGraphPattern(Optional.empty());

        List<Expression> expressions = new ArrayList<>();
        if (creationDateFrom != null) {
            expressions.add(Expressions.gte(var("creationDate"),
                    literalOfType(creationDateFrom, XSD.iri("dateTime"))));
        }

        if (creationDateTo != null) {
            expressions.add(Expressions.lte(var("creationDate"),
                    literalOfType(creationDateTo, XSD.iri("dateTime"))));
        }

        if (screenName != null) {
            expressions.add(Expressions.equals(var("creator_username"),
                    literalOf(screenName)));
        }

        if (!expressions.isEmpty()) {
            postGraphPattern = postGraphPattern.filter(Expressions.and(expressions.toArray(new Expression[expressions.size()])));
        }

        SelectQuery selectQuery = buildPostSelectQuery()
                .where(postGraphPattern)
                .where(buildLocationGraphPattern())
                .where(buildOwnerGraphPattern())
                .where(buildCreatorGraphPattern())
                .limit(100);

        LOGGER.debug("Issuing SPARQL query :\n{}", selectQuery.getQueryString());
        try {
            TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, selectQuery.getQueryString());
            PostTupleQueryResultHandler postTupleQueryResultHandler = new PostTupleQueryResultHandler(repositoryConnection);

            tupleQuery.evaluate(postTupleQueryResultHandler);

            postList.addAll(postTupleQueryResultHandler.getPostList());

            postTupleQueryResultHandler.endQueryResult();
        } catch (RepositoryException repositoryException) {
            LOGGER.error("An exception occurred on graphdb repository request {}", repositoryException.getMessage());
        } catch (MalformedQueryException malformedQueryException) {
            LOGGER.error("Something wrong in query {}", malformedQueryException.getMessage());
        }

        return postList;
    }

    public Post getPost(String identifier) {
        Post postResult = null;
        try {
            SelectQuery selectQuery = buildPostSelectQuery()
                    .where(buildPostGraphPattern(Optional.of(identifier)))
                    .where(buildLocationGraphPattern())
                    .where(buildOwnerGraphPattern())
                    .where(buildCreatorGraphPattern());

            LOGGER.debug("Issuing SPARQL query :\n{}", selectQuery.getQueryString());
            TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, selectQuery.getQueryString());

            PostTupleQueryResultHandler postTupleQueryResultHandler = new PostTupleQueryResultHandler(repositoryConnection);
            tupleQuery.evaluate(postTupleQueryResultHandler);
            postResult = postTupleQueryResultHandler.getPostList().get(0);
        } catch (RepositoryException repositoryException) {
            LOGGER.error("An exception occurred on graphdb repository request {}", repositoryException.getMessage());
        } catch (MalformedQueryException malformedQueryException) {
            LOGGER.error("Something wrong in query {}", malformedQueryException.getMessage());
        }
        return postResult;
        //TODO: return an error not found
    }

    private GraphPattern buildPostGraphPattern(Optional<String> identifier) {
        //TODO: check of optional data
        Variable post = var("post");
        TriplePattern triplePattern =  post.isA((SOCATEL.iri("Post")));
        identifier.ifPresent(s -> triplePattern.andHas(SOCATEL.iri("identifier"), s));
        triplePattern.andHas(SOCATEL.iri("identifier"), var("identifier"))
            .andHas(SOCATEL.iri("description"), var("description"))
            .andHas(SOCATEL.iri("creationDate"), var("creationDate"))
            .andHas(SOCATEL.iri("language"), var("language"))
            .andHas(SOCATEL.iri("num_likes"), var("num_likes"))
            .andHas(SIOC.iri("num_replies"), var("num_replies"))
            .andHas(SOCATEL.iri("location"), var("location"))
            .andHas(SIOC.iri("has_owner"), var("owner"))
            .andHas(SOCATEL.iri("createdBy"), var("creator"));

        return triplePattern;
    }

    private GraphPattern buildLocationGraphPattern() {
        Variable location = var("location");
        return location.has(GN.iri("name"), var("location_name"))
                .andHas(GN.iri("alternateName"), var("location_alternateName"))
                .andHas(GN.iri("countryCode"), var("location_countryCode"));
    }

    private GraphPattern buildOwnerGraphPattern() {
        Variable owner = var("owner");
        return owner.has(SOCATEL.iri("identifier"), var("owner_identifier"))
                .andHas(SOCATEL.iri("title"), var("owner_title"))
                .andHas(SOCATEL.iri("description"), var("owner_description"))
                .andHas(SOCATEL.iri("webLink"), var("owner_webLink"))
                .andHas(SOCATEL.iri("language"), var("owner_language"))
                .andHas(SOCATEL.iri("num_likes"), var("owner_num_likes"));
    }

    private GraphPattern buildCreatorGraphPattern() {
        Variable creator = var("creator");
        return creator.has(FOAF.iri("name"), var("creator_name"))
                .andHas(FOAF.iri("username"), var("creator_username"));
    }

    private SelectQuery buildPostSelectQuery() {
        return Queries.SELECT()
                .prefix(SOCATEL, RDF, SIOC, GN, FOAF)
                .select(var("post"), var("identifier"), var("description"),
                        var("creationDate"), var("language"), var("num_likes"),
                        var("num_replies"), var("location_name"), var("location_alternateName"),
                        var("location_countryCode"), var("owner_identifier"), var("owner_title"),
                        var("owner_description"), var("owner_webLink"), var("owner_language"),
                        var("owner_num_likes"), var("creator_name"), var("creator_username"));
    }
}