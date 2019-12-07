package com.ozwillo.socatelgraphql.repository;

import com.ozwillo.socatelgraphql.domain.Service;
import com.ozwillo.socatelgraphql.handler.ServiceTupleQueryResultHandler;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder.prefix;
import static org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder.var;
import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;
import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.literalOf;

@Component
public class ServiceRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRepository.class);

    private static Prefix SOCATEL = prefix("socatel", iri("http://www.everis.es/SoCaTel/ontology#"));
    private static Prefix FOAF = prefix("foaf", iri("http://xmlns.com/foaf/0.1/"));
    private static Prefix GEO_ONT = prefix("geo-ont", iri("http://www.geonames.org/ontology#"));
    private static Prefix SKOS = prefix("skos", iri("http://www.w3.org/2004/02/skos/core#"));


    private RepositoryConnection repositoryConnection;

    private List<Projectable> projectables;

    public ServiceRepository(
            @Value("${graphdb.url}") String url,
            @Value("${graphdb.username}") String username,
            @Value("${graphdb.password}") String password) {
        HTTPRepository repository = new HTTPRepository(url);
        repository.setUsernameAndPassword(username, password);
        this.repositoryConnection = repository.getConnection();
        this.projectables = new ArrayList<>();

        projectables.addAll(
                Arrays.asList(
                        var("service"),
                        var("identifier"),
                        var("title"),
                        var("description"),
                        var("language"),
                        var("webLink"),
                        var("creator_name"),
                        var("location_name")));
    }

    public ArrayList<Service> getAllService(String language, Integer offset, Integer limit) {
        ArrayList<Service> serviceList = new ArrayList<>();

        Variable service = var("service");

        GraphPattern serviceGraphPattern = buildServiceGraphPattern(service, Optional.empty(), language);

        SelectQuery selectQuery = buildServiceSelectQuery(this.projectables)
                .where(serviceGraphPattern)
                .where(buildCreatorGraphPattern(service))
                .where(buildLocationGraphPattern(service))
                .offset(offset)
                .limit(limit);

        LOGGER.debug("Issuing SPARQL query :\n{}", selectQuery.getQueryString());
        try {
            TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, selectQuery.getQueryString());
            ServiceTupleQueryResultHandler serviceTupleQueryResultHandler = new ServiceTupleQueryResultHandler(repositoryConnection);

            tupleQuery.evaluate(serviceTupleQueryResultHandler);

            serviceList.addAll(serviceTupleQueryResultHandler.getServiceList());

            serviceTupleQueryResultHandler.endQueryResult();
        } catch (RepositoryException repositoryException) {
            LOGGER.error("An exception occurred on graphdb repository request {}", repositoryException.getMessage());
        } catch (MalformedQueryException malformedQueryException) {
            LOGGER.error("Something wrong in query {}", malformedQueryException.getMessage());
        }

        return serviceList;
    }

    public Optional<Service> getService(String identifier) {

        try {
            Variable service = var("service");

            SelectQuery selectQuery = buildServiceSelectQuery(this.projectables)
                    .where(buildServiceGraphPattern(service, Optional.of(identifier), ""))
                    .where(buildCreatorGraphPattern(service))
                    .where(buildLocationGraphPattern(service));

            LOGGER.debug("Issuing SPARQL query :\n{}", selectQuery.getQueryString());
            TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, selectQuery.getQueryString());
            ServiceTupleQueryResultHandler serviceTupleQueryResultHandler = new ServiceTupleQueryResultHandler(repositoryConnection);
            tupleQuery.evaluate(serviceTupleQueryResultHandler);
            return serviceTupleQueryResultHandler.getServiceList().isEmpty() ?
                    Optional.empty() :
                    Optional.of(serviceTupleQueryResultHandler.getServiceList().get(0));
        } catch (RepositoryException repositoryException) {
            LOGGER.error("An exception occurred on graphdb repository request {}", repositoryException.getMessage());
        } catch (MalformedQueryException malformedQueryException) {
            LOGGER.error("Something wrong in query {}", malformedQueryException.getMessage());
        }

        return Optional.empty();
    }

    public List<Service> servicesByTopics(List<String> topics, String language, Integer offset, Integer limit) {

        ServiceTupleQueryResultHandler serviceTupleQueryResultHandler = new ServiceTupleQueryResultHandler(repositoryConnection);

        Variable service = var("service");

        GraphPattern serviceGraphPattern = buildServiceGraphPattern(service, Optional.empty(), language);

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
            serviceGraphPattern = serviceGraphPattern.filter(Expressions.or(expressions.toArray(new Expression[expressions.size()])));
        }

        SelectQuery selectQuery = buildServiceSelectQuery(this.projectables)
                .where(serviceGraphPattern)
                .where(buildTopicCloseMatchGraphPattern(service))
                .where(buildCreatorGraphPattern(service))
                .where(buildLocationGraphPattern(service))
                .groupBy(this.projectables.toArray(new Groupable[this.projectables.size()]))
                .offset(offset)
                .limit(limit);

        LOGGER.debug("Issuing SPARQL query :\n{}", selectQuery.getQueryString());
        try {
            TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL, selectQuery.getQueryString());

            tupleQuery.evaluate(serviceTupleQueryResultHandler);

            serviceTupleQueryResultHandler.endQueryResult();
        } catch (RepositoryException repositoryException) {
            LOGGER.error("An exception occurred on graphdb repository request {}", repositoryException.getMessage());
        } catch (MalformedQueryException malformedQueryException) {
            LOGGER.error("Something wrong in query {}", malformedQueryException.getMessage());
        }

        return serviceTupleQueryResultHandler.getServiceList();
    }

    private GraphPattern buildServiceGraphPattern(Variable service, Optional<String> identifier, String language) {
        TriplePattern triplePattern = service.isA((SOCATEL.iri("Service")));
        identifier.ifPresent(s -> triplePattern.andHas(SOCATEL.iri("identifier"), s));
        triplePattern.andHas(SOCATEL.iri("identifier"), var("identifier"))
                .andHas(SOCATEL.iri("title"), var("title"))
                .andHas(SOCATEL.iri("description"), var("description"))
                .andHas(SOCATEL.iri("language"), language.isEmpty() || language.length() > 2 ? var("language") : literalOf(language))
                .andHas(SOCATEL.iri("webLink"), var("webLink"));

        return triplePattern;
    }

    private SelectQuery buildServiceSelectQuery(List<Projectable> projectables) {
        List<Projectable> groupConcatProjectable = new ArrayList<>(projectables);

        return Queries.SELECT()
                .prefix(SOCATEL, FOAF, GEO_ONT, SKOS)
                .select(groupConcatProjectable.toArray(new Projectable[groupConcatProjectable.size()]));
    }

    private GraphPattern buildCreatorGraphPattern(Variable service) {
        Variable creator = var("creator");
        return service.has(SOCATEL.iri("createdBy"), var("creator"))
                .and(creator.has(FOAF.iri("name"), var("creator_name"))).optional();
    }

    private GraphPattern buildLocationGraphPattern(Variable service) {
        Variable location = var("location");
        return service.has(SOCATEL.iri("location"), var("location"))
                .and(location.has(GEO_ONT.iri("name"), var("location_name"))).optional();
    }

    private GraphPattern buildTopicCloseMatchGraphPattern(Variable service) {
        Variable topic = var("topic");
        Variable matchedTopic = var("matchedTopic");

        return service.has(SOCATEL.iri("topic"), var("topic"))
                .and(topic.has(SKOS.iri("closeMatch"), matchedTopic))
                .and(topic.has(SKOS.iri("prefLabel | skos:altLabel"), var("label")))
                .and(matchedTopic.has(SKOS.iri("prefLabel | skos:altLabel"), var("matchedLabel"))).optional();
    }
}
