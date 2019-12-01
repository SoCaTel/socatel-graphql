package com.ozwillo.socatelgraphql.fetcher;

import com.ozwillo.socatelgraphql.repository.ServiceRepository;
import graphql.schema.DataFetcher;
import org.springframework.stereotype.Component;

@Component
public class ServiceDataFetchers {

    private ServiceRepository serviceRepository;

    public ServiceDataFetchers(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public DataFetcher services() {
        return dataFetchingEnvironment ->
                serviceRepository.getAllService(
                        dataFetchingEnvironment.getArgument("language"),
                        dataFetchingEnvironment.getArgument("offset"),
                        dataFetchingEnvironment.getArgument("limit"));
    }

    public DataFetcher service() {
        return dataFetchingEnvironment ->
                serviceRepository.getService(dataFetchingEnvironment.getArgument("identifier"));
    }
}
