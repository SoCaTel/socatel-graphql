package com.ozwillo.socatelgraphql.repository.elastic;

import com.ozwillo.socatelgraphql.domain.Service;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.elasticsearch.jest.JestProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class ServiceRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRepository.class);

    @Value("${es.indices.organizations}")
    private String organizationsIndices;

    private final JestProperties jestProperties;

    public ServiceRepository(JestProperties jestProperties) {
        this.jestProperties = jestProperties;
    }

    public Optional<Service> findByName(String name) {
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig
                .Builder(jestProperties.getUris().get(0))
                .defaultCredentials(jestProperties.getUsername(), jestProperties.getPassword())
                .multiThreaded(true)
                .defaultMaxTotalConnectionPerRoute(2)
                .maxTotalConnection(20)
                .build());
        JestClient client = factory.getObject();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("organisation_name", name));

        Search search = new Search.Builder(searchSourceBuilder.toString())
                .addIndex(organizationsIndices)
                .build();

        SearchResult result;
        try {
            result = client.execute(search);
        } catch (IOException e) {
            LOGGER.error("Error while searching in ES", e);
            return Optional.empty();
        }

        LOGGER.debug(result.getJsonString());
        List<Service> services = result.getSourceAsObjectList(Service.class, false);
        if (!services.isEmpty())
            return Optional.of(result.getSourceAsObjectList(Service.class, false).get(0));
        else
            return Optional.empty();
    }
}
