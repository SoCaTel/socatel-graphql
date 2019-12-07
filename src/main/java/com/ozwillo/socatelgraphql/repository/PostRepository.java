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
import org.eclipse.rdf4j.sparqlbuilder.core.Groupable;
import org.eclipse.rdf4j.sparqlbuilder.core.Prefix;
import org.eclipse.rdf4j.sparqlbuilder.core.Projectable;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder.*;
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
    private static Prefix SKOS = prefix("skos", iri("http://www.w3.org/2004/02/skos/core#"));

    //TODO: singleton ??
    private RepositoryConnection repositoryConnection;

    private List<Projectable> projectables;

    public PostRepository(
            @Value("${graphdb.url}") String url,
            @Value("${graphdb.username}") String username,
            @Value("${graphdb.password}") String password) {
        HTTPRepository repository = new HTTPRepository(url);
        repository.setUsernameAndPassword(username, password);
        this.repositoryConnection = repository.getConnection();
        this.projectables = new ArrayList<>();

        projectables.addAll(Arrays.asList(
                var("post"),
                var("identifier"),
                var("description"),
                var("creationDate"),
                var("language"),
                var("num_likes"),
                var("num_replies"),
                var("location_name"),
                var("location_alternateName"),
                var("location_countryCode"),
                var("owner_identifier"),
                var("owner_title"),
                var("owner_description"),
                var("owner_webLink"),
                var("owner_language"),
                var("owner_num_likes"),
                var("owner_imageLink"),
                var("creator_name"),
                var("creator_username")));
    }

    public ArrayList<Post> getPosts(LocalDate creationDateFrom, LocalDate creationDateTo, String screenName, Integer offset, Integer limit) {

        ArrayList<Post> postList = new ArrayList<>();

        Variable post = var("post");

        GraphPattern postGraphPattern = buildPostGraphPattern(post, Optional.empty(), "");

        List<Expression> expressions = new ArrayList<>();
        if (creationDateFrom != null) {
            expressions.add(Expressions.gte(var("creationDate"),
                    literalOfType(creationDateFrom.format(DateTimeFormatter.ofPattern("YYYY-MM-dd")), XSD.iri("dateTime"))));
        }

        if (creationDateTo != null) {
            expressions.add(Expressions.lte(var("creationDate"),
                    literalOfType(creationDateTo.format(DateTimeFormatter.ofPattern("YYYY-MM-dd")), XSD.iri("dateTime"))));
        }

        if (screenName != null) {
            expressions.add(Expressions.equals(var("creator_username"),
                    literalOf(screenName)));
        }

        if (!expressions.isEmpty()) {
            postGraphPattern = postGraphPattern.filter(Expressions.and(expressions.toArray(new Expression[expressions.size()])));
        }

        SelectQuery selectQuery = buildPostSelectQuery(this.projectables)
                .where(postGraphPattern)
                .where(buildLocationGraphPattern(post))
                .where(buildOwnerGraphPattern(post))
                .where(buildCreatorGraphPattern(post))
                .offset(offset)
                .limit(limit);

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

    public Optional<Post> getPost(String identifier) {

        try {
            Variable post = var("post");

            SelectQuery selectQuery = buildPostSelectQuery(this.projectables)
                    .where(buildPostGraphPattern(post, Optional.of(identifier), ""))
                    .where(buildLocationGraphPattern(post))
                    .where(buildOwnerGraphPattern(post))
                    .where(buildCreatorGraphPattern(post));

            LOGGER.debug("Issuing SPARQL query :\n{}", selectQuery.getQueryString());
            TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, selectQuery.getQueryString());
            PostTupleQueryResultHandler postTupleQueryResultHandler = new PostTupleQueryResultHandler(repositoryConnection);
            tupleQuery.evaluate(postTupleQueryResultHandler);
            return postTupleQueryResultHandler.getPostList().isEmpty() ?
                    Optional.empty() :
                    Optional.of(postTupleQueryResultHandler.getPostList().get(0));
        } catch (RepositoryException repositoryException) {
            LOGGER.error("An exception occurred on graphdb repository request {}", repositoryException.getMessage());
        } catch (MalformedQueryException malformedQueryException) {
            LOGGER.error("Something wrong in query {}", malformedQueryException.getMessage());
        }

        return Optional.empty();
    }

    public List<Post> findPostsByTopics(List<String> topics, String language, Integer offset, Integer limit) {
        PostTupleQueryResultHandler postTupleQueryResultHandler = new PostTupleQueryResultHandler(repositoryConnection);

        List<Projectable> basicProjectablesPost =
                Arrays.asList(var("post"), var("identifier"), var("description"),
                var("creationDate"), var("language"), var("num_likes"), var("num_replies"));

        Variable post = var("post");

        GraphPattern postGraphPattern = buildPostGraphPattern(post, Optional.empty(), language);

        List<Expression> expressions = new ArrayList<>();

        topics = topics.stream().filter(topic -> !topic.isEmpty()).collect(Collectors.toList());

        if(!topics.isEmpty()) {
            String topicsRegex = String.join("|", topics);
            expressions.add(
                    Expressions.or(
                            Expressions.regex(Expressions.str(var("label")), literalOf(topicsRegex), literalOf("i")),
                            Expressions.regex(Expressions.str(var("matchedLabel")), literalOf(topicsRegex), literalOf("i"))));
        }

        if (!expressions.isEmpty()) {
            postGraphPattern = postGraphPattern.filter(Expressions.or(expressions.toArray(new Expression[expressions.size()])));
        }

        SelectQuery selectQuery = Queries.SELECT()
                .prefix(SOCATEL, RDF, SIOC, SKOS)
                .select(basicProjectablesPost.toArray(new Projectable[basicProjectablesPost.size()]))
                .where(postGraphPattern)
                .where(buildTopicCloseMatchGraphPattern(post))
                .groupBy(basicProjectablesPost.toArray(new Groupable[basicProjectablesPost.size()]))
                .offset(offset)
                .limit(limit);

        LOGGER.debug("Issuing SPARQL query :\n{}", selectQuery.getQueryString());
        try {
            TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, selectQuery.getQueryString());

            tupleQuery.evaluate(postTupleQueryResultHandler);

            postTupleQueryResultHandler.endQueryResult();
        } catch (RepositoryException repositoryException) {
            LOGGER.error("An exception occurred on graphdb repository request {}", repositoryException.getMessage());
        } catch (MalformedQueryException malformedQueryException) {
            LOGGER.error("Something wrong in query {}", malformedQueryException.getMessage());
        }

        return postTupleQueryResultHandler.getPostList();
    }

    private GraphPattern buildPostGraphPattern(Variable post, Optional<String> identifier, String language) {
        TriplePattern triplePattern = post.isA((SOCATEL.iri("Post")));
        identifier.ifPresent(s -> triplePattern.andHas(SOCATEL.iri("identifier"), s));
        triplePattern
                .andHas(SOCATEL.iri("identifier"), var("identifier"))
                .andHas(SOCATEL.iri("description"), var("description"))
                .andHas(SOCATEL.iri("creationDate"), var("creationDate"))
                .andHas(SOCATEL.iri("language"), language.isEmpty() || language.length() > 2 ? var("language") : literalOf(language))
                .andHas(SOCATEL.iri("num_likes"), var("num_likes"))
                .andHas(SIOC.iri("num_replies"), var("num_replies"));

        return triplePattern;
    }

    private GraphPattern buildLocationGraphPattern(Variable post) {
        Variable location = var("location");
        return post.has(SOCATEL.iri("location"), var("location"))
                .and(location.has(GN.iri("name"), var("location_name"))
                        .andHas(GN.iri("alternateName"), var("location_alternateName"))
                        .andHas(GN.iri("countryCode"), var("location_countryCode"))).optional();
    }

    private GraphPattern buildOwnerGraphPattern(Variable post) {
        Variable owner = var("owner");
        return post.has(SIOC.iri("has_owner"), var("owner"))
                .and(owner.has(SOCATEL.iri("identifier"), var("owner_identifier"))
                        .andHas(SOCATEL.iri("title"), var("owner_title"))
                        .andHas(SOCATEL.iri("description"), var("owner_description"))
                        .andHas(SOCATEL.iri("webLink"), var("owner_webLink"))
                        .andHas(SOCATEL.iri("num_likes"), var("owner_num_likes"))
                        .andHas(SOCATEL.iri("imageLink"), var("owner_imageLink"))
                        .andHas(SOCATEL.iri("language"), var("owner_language"))).optional();
    }

    private GraphPattern buildCreatorGraphPattern(Variable post) {
        Variable creator = var("creator");
        return post.has(SOCATEL.iri("createdBy"), var("creator"))
                .and(creator.has(FOAF.iri("name"), var("creator_name"))
                        .andHas(FOAF.iri("username"), var("creator_username"))).optional();
    }

    private SelectQuery buildPostSelectQuery(List<Projectable> projectables) {
        List<Projectable> grouConcatProjectable = new ArrayList<>(projectables);
        //Quick fix to optimise requests
        //grouConcatProjectable.add(Expressions.group_concat("\",\"", var("prefLabel")).distinct().as(var("topics")));

        return Queries.SELECT()
                .prefix(SOCATEL, RDF, SIOC, GN, FOAF, SKOS)
                .select(grouConcatProjectable.toArray(new Projectable[grouConcatProjectable.size()]));
    }

    private GraphPattern buildTopicGraphPattern(Variable post) {
        Variable topic = var("topic");

        return post.has(SOCATEL.iri("topic"), var("topic"))
            .and(topic.has(SKOS.iri("prefLabel"), var("prefLabel"))).optional();
    }

    private GraphPattern buildTopicCloseMatchGraphPattern(Variable post) {
        Variable topic = var("topic");
        Variable matchedTopic = var("matchedTopic");

        return post.has(SOCATEL.iri("topic"), var("topic"))
            .and(topic.has(SKOS.iri("closeMatch"), matchedTopic))
            .and(topic.has(SKOS.iri("prefLabel | skos:altLabel"), var("label")))
            .and(matchedTopic.has(SKOS.iri("prefLabel | skos:altLabel"), var("matchedLabel"))).optional();
    }
}
