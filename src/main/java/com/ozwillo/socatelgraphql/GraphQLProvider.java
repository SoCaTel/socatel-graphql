package com.ozwillo.socatelgraphql;

import com.ozwillo.socatelgraphql.fetcher.PostDataFetchers;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

@Component
public class GraphQLProvider {

    private GraphQL graphQL;

    private final PostDataFetchers postDataFetchers;

    public GraphQLProvider(PostDataFetchers postDataFetchers) {
        this.postDataFetchers = postDataFetchers;
    }

    @Bean
    public GraphQL graphQL() {
        return graphQL;
    }

    @PostConstruct
    public void init() throws IOException {
        ClassPathResource schemaResource = new ClassPathResource("schema/post.graphqls");
        byte[] sdl = FileCopyUtils.copyToByteArray(schemaResource.getInputStream());
        GraphQLSchema graphQLSchema = buildSchema(new String(sdl, StandardCharsets.UTF_8));
        this.graphQL = GraphQL.newGraphQL(graphQLSchema).build();
    }

    private GraphQLSchema buildSchema(String sdl) {
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(sdl);
        RuntimeWiring runtimeWiring = buildWiring();
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
    }

    private RuntimeWiring buildWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("Query")
                        .dataFetcher("postById", postDataFetchers.getPostByIdDataFetcher())
                        .dataFetcher("posts", postDataFetchers.getPostsDataFetcher()))
                .build();
    }
}
