package com.ozwillo.socatelgraphql.handler;

import com.ozwillo.socatelgraphql.domain.Creator;
import com.ozwillo.socatelgraphql.domain.Location;
import com.ozwillo.socatelgraphql.domain.Owner;
import com.ozwillo.socatelgraphql.domain.Post;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryResultHandlerException;
import org.eclipse.rdf4j.query.TupleQueryResultHandler;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import java.util.ArrayList;
import java.util.List;


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
        Post post = new Post();

        Location location = new Location();
        location.setName(bindingSet.getValue("location_name").stringValue());
        location.setAlternateName(bindingSet.getValue("location_alternateName").stringValue());
        location.setCountryCode(bindingSet.getValue("location_countryCode").stringValue());

        Owner owner = new Owner();
        owner.setIdentifier(bindingSet.getValue("owner_identifier").stringValue());
        owner.setTitle(bindingSet.getValue("owner_title").stringValue());
        owner.setDescription(bindingSet.getValue("owner_description").stringValue());
        owner.setWebLink(bindingSet.getValue("owner_webLink").stringValue());
        owner.setLanguage(bindingSet.getValue("owner_language").stringValue());
        owner.setNumLikes(Integer.valueOf(bindingSet.getValue("owner_num_likes").stringValue()));

        Creator creator = new Creator();
        creator.setName(bindingSet.getValue("creator_name").stringValue());
        creator.setUsername(bindingSet.getValue("creator_username").stringValue());

        post.setIdentifier(bindingSet.getValue("identifier").stringValue());
        post.setDescription(bindingSet.getValue("description").stringValue());
        post.setCreationDate(bindingSet.getValue("creationDate").stringValue());
        post.setLanguage(bindingSet.getValue("language").stringValue());
        post.setNumLikes(Integer.valueOf(bindingSet.getValue("num_likes").stringValue()));
        post.setNumReplies(Integer.valueOf(bindingSet.getValue("num_replies").stringValue()));
        post.setLocation(location);
        post.setOwner(owner);
        post.setCreator(creator);

        postList.add(post);
    }

    public List<Post> getPostList() {
        return this.postList;
    }
}
