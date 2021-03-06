package com.ozwillo.socatelgraphql.fetcher;

import com.ozwillo.socatelgraphql.repository.PostRepository;
import graphql.schema.DataFetcher;
import org.springframework.stereotype.Component;

@Component
public class PostDataFetchers {

    private final PostRepository postRepository;

    public PostDataFetchers(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public DataFetcher getPostByIdDataFetcher() {
        return dataFetchingEnvironment -> {
            String identifier = dataFetchingEnvironment.getArgument("identifier");
            return postRepository.getPost(identifier);
        };
    }

    public DataFetcher getPostsDataFetcher() {
        return dataFetchingEnvironment ->
                postRepository.getPosts(dataFetchingEnvironment.getArgument("creationDateFrom"),
                        dataFetchingEnvironment.getArgument("creationDateTo"),
                        dataFetchingEnvironment.getArgument("screenName"),
                        dataFetchingEnvironment.getArgument("offset"),
                        dataFetchingEnvironment.getArgument("limit"));
    }

    public DataFetcher getPostsByTopicsDataFetcher() {
        return dataFetchingEnvironment ->
                postRepository.findPostsByTopics(
                        dataFetchingEnvironment.getArgument("topics"),
                        dataFetchingEnvironment.getArgument("language"),
                        dataFetchingEnvironment.getArgument("offset"),
                        dataFetchingEnvironment.getArgument("limit"));
    }
}
