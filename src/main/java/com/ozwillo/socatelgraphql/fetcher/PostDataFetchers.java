package com.ozwillo.socatelgraphql.fetcher;

import com.ozwillo.socatelgraphql.repository.PostRepository;
import graphql.schema.DataFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PostDataFetchers {

    @Autowired
    PostRepository postRepository;

    public DataFetcher getPostByIdDataFetcher() {
        return dataFetchingEnvironment -> {
            String identifier = dataFetchingEnvironment.getArgument("identifier");
            return postRepository.getPost(identifier);
        };
    }

    public DataFetcher getPostsDataFetcher() {
        return dataFetchingEnvironment -> postRepository.getPosts();
    }
}
