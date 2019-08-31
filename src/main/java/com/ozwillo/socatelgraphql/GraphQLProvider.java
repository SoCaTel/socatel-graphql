package com.ozwillo.socatelgraphql;

import com.ozwillo.socatelgraphql.fetcher.GenericDataFetchers;
import com.ozwillo.socatelgraphql.fetcher.PostDataFetchers;
import graphql.GraphQL;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

@Component
public class GraphQLProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphQLProvider.class);

    private GraphQL graphQL;

    private final PostDataFetchers postDataFetchers;
    private final GenericDataFetchers genericDataFetchers;
    private final ResourceLoader resourceLoader;

    public GraphQLProvider(PostDataFetchers postDataFetchers, GenericDataFetchers genericDataFetchers, ResourceLoader resourceLoader) {
        this.postDataFetchers = postDataFetchers;
        this.genericDataFetchers = genericDataFetchers;
        this.resourceLoader = resourceLoader;
    }

    @Bean
    public GraphQL graphQL() {
        return graphQL;
    }

    @PostConstruct
    public void init() {
        GraphQLSchema graphQLSchema = buildSchema();
        this.graphQL = GraphQL.newGraphQL(graphQLSchema).build();
    }

    private GraphQLSchema buildSchema() {
        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeRegistry = new TypeDefinitionRegistry();
        SchemaGenerator schemaGenerator = new SchemaGenerator();

        loadSchemas().forEach(schema -> typeRegistry.merge(schemaParser.parse(schema)));

        RuntimeWiring runtimeWiring = buildWiring();

        return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
    }

    private RuntimeWiring buildWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("Query")
                        .dataFetcher("postById", postDataFetchers.getPostByIdDataFetcher())
                        .dataFetcher("posts", postDataFetchers.getPostsDataFetcher())
                        .dataFetcher("postsByTopics", postDataFetchers.getPostsByTopicsDataFetcher())
                        .dataFetcher("searchByTopics", genericDataFetchers.searchByTopicsDataFetcher()))
                .scalar(ExtendedScalars.Date)
                .scalar(ExtendedScalars.DateTime)
                .build();
    }

    private List<File> loadSchemas() {
        List<File> files = new ArrayList<>();
        Resource[] resources;

        try {
            resources = ResourcePatternUtils
                    .getResourcePatternResolver(resourceLoader)
                    .getResources("classpath:schema/*.graphqls");

            for (Resource resource : resources) {
                files.add(resource.getFile());
            }
        } catch (IOException e) {
            LOGGER.error("An error occurred while loading GraphQL schemas : {}", e.getMessage());
        }

        return files;
    }
}
