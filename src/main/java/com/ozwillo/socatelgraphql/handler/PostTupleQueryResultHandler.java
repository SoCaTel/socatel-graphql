package com.ozwillo.socatelgraphql.handler;

import com.ozwillo.socatelgraphql.domain.DTO.PostDTO;
import com.ozwillo.socatelgraphql.domain.Post;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryResultHandlerException;
import org.eclipse.rdf4j.query.TupleQueryResultHandler;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;


public class PostTupleQueryResultHandler implements TupleQueryResultHandler {

    private ModelMapper modelMapper;

    private RepositoryConnection repositoryConnection;

    private List<Post> postList;

    public PostTupleQueryResultHandler(RepositoryConnection repositoryConnection) {
        this.postList = new ArrayList<>();
        this.modelMapper = new ModelMapper();
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
        PostDTO postDTO = new PostDTO();

        postDTO.setIdentifier(bindingSet.getValue("identifier").stringValue());
        postDTO.setDescription(bindingSet.getValue("description").stringValue());
        postDTO.setCreationDate(bindingSet.getValue("creationDate").stringValue());
        postDTO.setLanguage(bindingSet.getValue("language").stringValue());
        postDTO.setNumLikes(Integer.valueOf(bindingSet.getValue("num_likes").stringValue()));
        postDTO.setNumReplies(Integer.valueOf(bindingSet.getValue("num_replies").stringValue()));
        postDTO.setLocationName(bindingSet.getValue("location_name").stringValue());
        postDTO.setLocationAlternateName(bindingSet.getValue("location_alternateName").stringValue());
        postDTO.setLocationCountryCode(bindingSet.getValue("location_countryCode").stringValue());
        postDTO.setOwnerIdentifier(bindingSet.getValue("owner_identifier").stringValue());
        postDTO.setOwnerTitle(bindingSet.getValue("owner_title").stringValue());
        postDTO.setDescription(bindingSet.getValue("owner_description").stringValue());
        postDTO.setOwnerWebLink(bindingSet.getValue("owner_webLink").stringValue());
        postDTO.setOwnerLanguage(bindingSet.getValue("owner_language").stringValue());
        postDTO.setOwnerNumLikes(Integer.valueOf(bindingSet.getValue("owner_num_likes").stringValue()));
        postDTO.setCreatorName(bindingSet.getValue("creator_name").stringValue());

        postList.add(this.modelMapper.map(postDTO, Post.class));
    }

    public List<Post> getPostList() {
        return this.postList;
    }
}
