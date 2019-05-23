package com.ozwillo.socatelgraphql.repository;

import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class PostRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostRepository.class);

    private RepositoryConnection repositoryConnection;

    public PostRepository(
            @Value("${graphdb.url}") String url,
            @Value("${graphdb.username}") String username,
            @Value("${graphdb.password}") String password) {
        HTTPRepository repository = new HTTPRepository(url);
        repository.setUsernameAndPassword(username, password);
        this.repositoryConnection = repository.getConnection();
    }

    public ArrayList<HashMap<String, String>> getPosts() {
        ArrayList<HashMap<String, String>> result = new ArrayList<>();
        try {
            TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL,
                    "PREFIX socatel: <http://www.everis.es/SoCaTel/ontology#>\n" +
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                    "PREFIX sioc: <http://rdfs.org/sioc/ns#>\n" +
                    "SELECT ?post ?identifier ?description ?num_likes ?num_replies\n" +
                    "WHERE {\n" +
                    "    ?post rdf:type socatel:Post ;\n" +
                    "    socatel:identifier ?identifier;\n" +
                    "    socatel:description ?description;\n" +
                    "    socatel:num_likes ?num_likes;\n" +
                    "    sioc:num_replies ?num_replies;\n" +
                    "} LIMIT 100");

            TupleQueryResult tupleQueryResult = tupleQuery.evaluate();
            while (tupleQueryResult.hasNext()) {
                BindingSet bindingSet = tupleQueryResult.next();
                HashMap<String, String> post = new HashMap<>();

                for (Binding binding : bindingSet) {
                    String name = binding.getName();
                    org.eclipse.rdf4j.model.Value value = binding.getValue();
                    post.put(name, value.stringValue());
                }
                result.add(post);
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
            TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(QueryLanguage.SPARQL,
                    "PREFIX socatel: <http://www.everis.es/SoCaTel/ontology#>\n" +
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                    "PREFIX sioc: <http://rdfs.org/sioc/ns#>\n" +
                    "SELECT ?description ?num_likes ?num_replies\n" +
                    "WHERE {\n" +
                    "    ?post rdf:type socatel:Post ;\n" +
                    "    socatel:identifier \"" + identifier + "\" ;\n" +
                    "    socatel:description ?description ;\n" +
                    "    socatel:num_likes ?num_likes ;\n" +
                    "    sioc:num_replies ?num_replies ;\n" +
                    "}");

            TupleQueryResult tupleQueryResult = tupleQuery.evaluate();

            if(tupleQueryResult.hasNext()) {

                LOGGER.debug("Got a result for {}", identifier);

                BindingSet bindingSet = tupleQueryResult.next();

                result = new HashMap<>();
                result.put("identifier", identifier);

                for (Binding binding : bindingSet) {
                    String name = binding.getName();
                    org.eclipse.rdf4j.model.Value value = binding.getValue();

                    result.put(name, value.stringValue());
                }
            }

            tupleQueryResult.close();
        } finally {
            repositoryConnection.close();
        }
        return result;
    }
}
