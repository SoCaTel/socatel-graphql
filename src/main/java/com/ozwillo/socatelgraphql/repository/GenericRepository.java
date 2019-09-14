package com.ozwillo.socatelgraphql.repository;

import com.ozwillo.socatelgraphql.handler.GenericDataTupleQueryResultHandler;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder.prefix;
import static org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder.var;
import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;
import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.literalOf;

@Component
public class GenericRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericRepository.class);

    private static Prefix SOCATEL = prefix("socatel", iri("http://www.everis.es/SoCaTel/ontology#"));
    private static Prefix SKOS = prefix("skos", iri("http://www.w3.org/2004/02/skos/core#"));

    private RepositoryConnection repositoryConnection;

    public GenericRepository(
            @Value("${graphdb.url}") String url,
            @Value("${graphdb.username}") String username,
            @Value("${graphdb.password}") String password) {
        HTTPRepository repository = new HTTPRepository(url);
        repository.setUsernameAndPassword(username, password);
        this.repositoryConnection = repository.getConnection();
    }

    public List<HashMap<String, String>> searchByTopics(List<String> topics) {
        GenericDataTupleQueryResultHandler genericDataTupleQueryResultHandler = new GenericDataTupleQueryResultHandler(repositoryConnection);

        List<Projectable> basicProjectablesPost =
                Arrays.asList(var("data"), var("identifier"), var("description"), var("type"), var("webLink"));

        Variable data = var("data");
        Variable topicVar = var("topic");
        Variable matchedTopic = var("matchedTopic");

        GraphPattern graphPattern = data.isA(var("type"))
                .andHas(SOCATEL.iri("identifier"), var("identifier"))
                .andHas(SOCATEL.iri("webLink"), var("webLink"))
                .andHas(SOCATEL.iri("topic"), topicVar)
                .and(topicVar.has(SKOS.iri("closeMatch*"), matchedTopic))
                .and(topicVar.has(SKOS.iri("prefLabel | skos:altLabel"), var("label")))
                .and(matchedTopic.has(SKOS.iri("prefLabel | skos:altLabel"), var("matchedLabel")));

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
            graphPattern = graphPattern.filter(Expressions.or(expressions.toArray(new Expression[expressions.size()])));
        }

        SelectQuery selectQuery = Queries.SELECT()
                .prefix(SOCATEL, SKOS)
                .select(basicProjectablesPost.toArray(new Projectable[basicProjectablesPost.size()]))
                .where(graphPattern)
                .groupBy(basicProjectablesPost.toArray(new Groupable[basicProjectablesPost.size()]));

        LOGGER.debug("Issuing SPARQL query :\n{}", selectQuery.getQueryString());
        try {
            TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, selectQuery.getQueryString());

            tupleQuery.evaluate(genericDataTupleQueryResultHandler);
        } catch (RepositoryException repositoryException) {
            LOGGER.error("An exception occurred on graphdb repository request {}", repositoryException.getMessage());
        } catch (MalformedQueryException malformedQueryException) {
            LOGGER.error("Something wrong in query {}", malformedQueryException.getMessage());
        }

        return genericDataTupleQueryResultHandler.getDataList();
    }

}
