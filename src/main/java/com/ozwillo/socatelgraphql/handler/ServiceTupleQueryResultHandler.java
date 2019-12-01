package com.ozwillo.socatelgraphql.handler;

import com.ozwillo.socatelgraphql.domain.*;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import java.util.*;


public class ServiceTupleQueryResultHandler implements TupleQueryResultHandler {

    private RepositoryConnection repositoryConnection;

    private List<Service> serviceList;

    public ServiceTupleQueryResultHandler(RepositoryConnection repositoryConnection) {
        this.serviceList = new ArrayList<>();
        this.repositoryConnection = repositoryConnection;
    }

    @Override
    public void handleBoolean(boolean value) throws QueryResultHandlerException {

    }

    @Override
    public void handleLinks(List<String> linkUrls) throws QueryResultHandlerException {
    }

    @Override
    public void startQueryResult(List<String> bindingNames) throws TupleQueryResultHandlerException {
    }

    @Override
    public void endQueryResult() throws TupleQueryResultHandlerException {
        repositoryConnection.close();
    }

    @Override
    public void handleSolution(BindingSet bindingSet) throws TupleQueryResultHandlerException {
        //TODO: find a SPARQL query solution to stop doing this check
        if(!bindingSet.hasBinding("service")) return; // Quick fix
        Service service = new Service();
        Location location = new Location();
        Creator creator = new Creator();

        bindingSet.getBindingNames().forEach(bindingName -> {
            if(bindingName.startsWith("location_")) {
                location.mapper(bindingName, bindingSet.getValue(bindingName).stringValue());
            } else if(bindingName.startsWith("creator_")) {
                creator.mapper(bindingName, bindingSet.getValue(bindingName).stringValue());
            } else service.mapper(bindingName, bindingSet.getValue(bindingName).stringValue());
        });

        service.setLocation(location);
        service.setCreator(creator);

        serviceList.add(service);
    }

    public List<Service> getServiceList() {
        return this.serviceList;
    }
}
