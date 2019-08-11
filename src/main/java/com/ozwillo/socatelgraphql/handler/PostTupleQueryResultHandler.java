package com.ozwillo.socatelgraphql.handler;

import com.ozwillo.socatelgraphql.domain.Creator;
import com.ozwillo.socatelgraphql.domain.Location;
import com.ozwillo.socatelgraphql.domain.Owner;
import com.ozwillo.socatelgraphql.domain.Post;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import java.util.*;


public class PostTupleQueryResultHandler implements TupleQueryResultHandler {

    private RepositoryConnection repositoryConnection;

    private List<Post> postList;

    public PostTupleQueryResultHandler(RepositoryConnection repositoryConnection) {
        this.postList = new ArrayList<>();
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
        if(!bindingSet.hasBinding("post")) return; // Quick fix
        Post post = new Post();
        Location location = new Location();
        Owner owner = new Owner();
        Creator creator = new Creator();

        bindingSet.getBindingNames().forEach(bindingName -> {
            if(bindingName.startsWith("location_")) {
                location.mapper(bindingName, bindingSet.getValue(bindingName).stringValue());
            } else if (bindingName.startsWith("owner_")) {
                owner.mapper(bindingName, bindingSet.getValue(bindingName).stringValue());
            } else if(bindingName.startsWith("creator_")) {
                creator.mapper(bindingName, bindingSet.getValue(bindingName).stringValue());
            } else post.mapper(bindingName, bindingSet.getValue(bindingName).stringValue());
        });

        post.setLocation(location);
        post.setOwner(owner);
        post.setCreator(creator);

        postList.add(post);
    }

    public List<Post> getPostList() {
        return this.postList;
    }
}
