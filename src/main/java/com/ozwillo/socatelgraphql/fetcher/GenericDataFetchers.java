package com.ozwillo.socatelgraphql.fetcher;

import com.ozwillo.socatelgraphql.repository.GenericRepository;
import graphql.schema.DataFetcher;
import org.springframework.stereotype.Component;

@Component
public class GenericDataFetchers {

    private final GenericRepository genericRepository;

    public GenericDataFetchers(GenericRepository genericRepository) {
        this.genericRepository = genericRepository;
    }

    public DataFetcher searchByTopicsDataFetcher() {
        return dataFetchingEnvironment ->
                genericRepository.searchByTopics(dataFetchingEnvironment.getArgument("topics"));
    }
}
