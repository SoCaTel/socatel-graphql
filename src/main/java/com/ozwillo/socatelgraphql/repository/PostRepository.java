package com.ozwillo.socatelgraphql.repository;

import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expression;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.core.Prefix;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder.*;
import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.*;
import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;

@Component
public class PostRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostRepository.class);

    private static Prefix SOCATEL = prefix("socatel", iri("http://www.everis.es/SoCaTel/ontology#"));
    private static Prefix SIOC = prefix("sioc", iri("http://rdfs.org/sioc/ns#"));
    private static Prefix RDF = prefix("rdf", iri("http://www.w3.org/1999/02/22-rdf-syntax-ns#"));
    private static Prefix XSD = prefix("xsd", iri("http://www.w3.org/2001/XMLSchema#"));

    private RepositoryConnection repositoryConnection;

    public PostRepository(
            @Value("${graphdb.url}") String url,
            @Value("${graphdb.username}") String username,
            @Value("${graphdb.password}") String password) {
        HTTPRepository repository = new HTTPRepository(url);
        repository.setUsernameAndPassword(username, password);
        this.repositoryConnection = repository.getConnection();
    }

    public ArrayList<HashMap<String, String>> getPosts(String creationDateFrom, String creationDateTo, String screenName) {

        ArrayList<HashMap<String, String>> result = new ArrayList<>();

        Variable post = var("post");
        GraphPattern graphPattern = post.isA((SOCATEL.iri("Post")))
                .andHas(SOCATEL.iri("identifier"), var("identifier"))
                .andHas(SOCATEL.iri("description"), var("description"))
                .andHas(SOCATEL.iri("creationDate"), var("creationDate"))
                .andHas(SOCATEL.iri("num_likes"), var("num_likes"))
                .andHas(SIOC.iri("num_replies"), var("num_replies"));

        // TODO does not seem to be parsed and pushed into the TS yet but it should be
        // .andHas(SIOC.iri("name"), var("screen_name"));

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
            expressions.add(Expressions.equals(var("screen_name"),
                    literalOf(screenName)));
        }

        if (!expressions.isEmpty()) {
            graphPattern = graphPattern.filter(Expressions.and(expressions.toArray(new Expression[expressions.size()])));
        }

        SelectQuery selectQuery = Queries.SELECT()
                .prefix(SOCATEL, RDF, SIOC)
                .select(var("post"), var("identifier"), var("description"),
                        var("creationDate"), var("num_likes"), var("num_replies"))

                // TODO does not seem to be parsed yet but it should be
                //, var("screen_name"))
                .where(graphPattern)
                .limit(100);

        LOGGER.debug("Issuing SPARQL query :\n{}", selectQuery.getQueryString());
        try {
            TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, selectQuery.getQueryString());

            TupleQueryResult tupleQueryResult = tupleQuery.evaluate();
            while (tupleQueryResult.hasNext()) {
                BindingSet bindingSet = tupleQueryResult.next();
                HashMap<String, String> postResult = new HashMap<>();

                for (Binding binding : bindingSet) {
                    postResult.put(binding.getName(), binding.getValue().stringValue());
                }
                result.add(postResult);
            }

            tupleQueryResult.close();
        } finally {
            repositoryConnection.close();
        }
        return result;
    }

    public Map<String, String> getPost(String identifier) {
        Map<String, String> result = null;
        try {

            Variable post = var("post");
            GraphPattern graphPattern = post.isA((SOCATEL.iri("Post")))
                    .andHas(SOCATEL.iri("identifier"), identifier)
                    .andHas(SOCATEL.iri("description"), var("description"))
                    .andHas(SOCATEL.iri("creationDate"), var("creationDate"))
                    .andHas(SOCATEL.iri("num_likes"), var("num_likes"))
                    .andHas(SIOC.iri("num_replies"), var("num_replies"));

            SelectQuery selectQuery = Queries.SELECT()
                    .prefix(SOCATEL, RDF, SIOC)
                    .select(var("post"), var("description"), var("creationDate"),
                            var("num_likes"), var("num_replies"))
                    .where(graphPattern);

            LOGGER.debug("Issuing SPARQL query :\n{}", selectQuery.getQueryString());
            TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, selectQuery.getQueryString());
            TupleQueryResult tupleQueryResult = tupleQuery.evaluate();

            if(tupleQueryResult.hasNext()) {

                LOGGER.debug("Got a result for {}", identifier);

                BindingSet bindingSet = tupleQueryResult.next();

                result = new HashMap<>();
                result.put("identifier", identifier);

                for (Binding binding : bindingSet) {
                    result.put(binding.getName(), binding.getValue().stringValue());
                }
            }

            tupleQueryResult.close();
        } finally {
            repositoryConnection.close();
        }
        return result;
    }
}
