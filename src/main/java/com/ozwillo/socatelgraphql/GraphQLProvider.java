package com.ozwillo.socatelgraphql;

import com.ozwillo.socatelgraphql.fetcher.GenericDataFetchers;
import com.ozwillo.socatelgraphql.fetcher.PostDataFetchers;
import com.ozwillo.socatelgraphql.fetcher.ServiceDataFetchers;
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
import org.springframework.util.FileCopyUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

@Component
public class GraphQLProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphQLProvider.class);

    private GraphQL graphQL;

    private final PostDataFetchers postDataFetchers;
    private final GenericDataFetchers genericDataFetchers;
    private final ServiceDataFetchers serviceDataFetchers;

    private final ResourceLoader resourceLoader;

    public GraphQLProvider(PostDataFetchers postDataFetchers, GenericDataFetchers genericDataFetchers, ServiceDataFetchers serviceDataFetchers, ResourceLoader resourceLoader) {
        this.postDataFetchers = postDataFetchers;
        this.genericDataFetchers = genericDataFetchers;
        this.serviceDataFetchers = serviceDataFetchers;
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

        loadSchemas().forEach(inputStream -> {
            try {
                byte[] sdl = FileCopyUtils.copyToByteArray(inputStream);
                typeRegistry.merge(schemaParser.parse(new String(sdl, StandardCharsets.UTF_8)));
            } catch (IOException e) {
                LOGGER.error("An error occured while trying to build GraphQL Schema {}", e.getMessage());
            }
        });

        RuntimeWiring runtimeWiring = buildWiring();

        return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
    }

    private RuntimeWiring buildWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("Query")
                        .dataFetcher("postById", postDataFetchers.getPostByIdDataFetcher())
                        .dataFetcher("posts", postDataFetchers.getPostsDataFetcher())
                        .dataFetcher("postsByTopics", postDataFetchers.getPostsByTopicsDataFetcher())
                        .dataFetcher("searchByTopics", genericDataFetchers.searchByTopicsDataFetcher())
                        .dataFetcher("services", serviceDataFetchers.services())
                        .dataFetcher("service", serviceDataFetchers.service())
                        .dataFetcher("servicesByTopics", serviceDataFetchers.servicesByTopics()))
                .scalar(ExtendedScalars.Date)
                .scalar(ExtendedScalars.DateTime)
                .build();
    }

    private List<InputStream> loadSchemas() {
        List<InputStream> inputStreams = new ArrayList<>();
        Resource[] resources;

        try {
            resources = ResourcePatternUtils
                    .getResourcePatternResolver(resourceLoader)
                    .getResources("classpath:schema/*.graphqls");

            for (Resource resource : resources) {
                inputStreams.add(resource.getInputStream());
            }
        } catch (IOException e) {
            LOGGER.error("An error occurred while loading GraphQL schemas : {}", e.getMessage());
        }

        return inputStreams;
    }
}
