package com.javathoughts.wiremock_graphql.dgs;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class GraphQLQueryRunner implements ApplicationRunner {

    private final GraphQLService graphQLService;

    public GraphQLQueryRunner(GraphQLService graphQLService) {
        this.graphQLService = graphQLService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Trigger the GraphQL query that will be recorded by WireMock
        String query = "{ getGreeting(name: \"John\") }";

        //String response = graphQLService.executeGraphQLQuery(query);

        // Print the response (just to see if it's working)
        //System.out.println("GraphQL Response: " + response);
    }
}