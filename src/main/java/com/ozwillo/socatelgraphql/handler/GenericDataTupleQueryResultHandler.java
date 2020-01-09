package com.ozwillo.socatelgraphql.handler;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryResultHandlerException;
import org.eclipse.rdf4j.query.TupleQueryResultHandler;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class GenericDataTupleQueryResultHandler implements TupleQueryResultHandler {

    private RepositoryConnection repositoryConnection;

    private List<HashMap<String, String>> dataList;

    public GenericDataTupleQueryResultHandler(RepositoryConnection repositoryConnection) {
        this.dataList = new ArrayList<>();
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
        if(!bindingSet.hasBinding("data")) return; // Quick fix
        HashMap<String, String> data = new HashMap<>();
        bindingSet.getBindingNames().forEach(bindingName -> {
            data.put(bindingName, bindingSet.getValue(bindingName).stringValue());
        });
        dataList.add(data);
    }

    public List<HashMap<String, String>>  getDataList() {
        return this.dataList;
    }
}
