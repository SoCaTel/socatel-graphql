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
            String identifier = dataFetchingEnvironment.getArgument("id");
            return postRepository.getPost(identifier);
        };
    }

    public DataFetcher getPostsDataFetcher() {
        return dataFetchingEnvironment ->
                postRepository.getPosts(dataFetchingEnvironment.getArgument("creationDateFrom"),
                        dataFetchingEnvironment.getArgument("creationDateTo"),
                        dataFetchingEnvironment.getArgument("screenName"));
    }
}
