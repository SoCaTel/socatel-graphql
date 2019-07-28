package com.ozwillo.socatelgraphql.controller;

import com.ozwillo.socatelgraphql.domain.Service;
import com.ozwillo.socatelgraphql.repository.elastic.ServiceRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Optional;

@RestController
public class ElasticController {

    private final ServiceRepository serviceRepository;

    public ElasticController(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @GetMapping("/{name}")
    public Optional<Service> searchService(@PathVariable String name) {
        return serviceRepository.findByName(name);
    }
}
