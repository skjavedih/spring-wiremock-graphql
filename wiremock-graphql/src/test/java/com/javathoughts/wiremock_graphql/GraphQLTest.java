package com.javathoughts.wiremock_graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.recording.RecordSpec;
import com.github.tomakehurst.wiremock.recording.RecordingStatus;
import com.javathoughts.wiremock_graphql.WiremockGraphql1Application;
import com.javathoughts.wiremock_graphql.config.WireMockProxy;
import com.javathoughts.wiremock_graphql.dgs.GraphQLService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.javathoughts.wiremock_graphql.config.WireMockConfig;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles(value = "integration")
@SpringBootTest(classes = WiremockGraphql1Application.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
public class GraphQLTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WireMockConfig wireMockConfig;

    @Autowired
    private GraphQLService graphQLService;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    private final List<WireMockServer> servers = new ArrayList<>();

    Function<WireMockProxy, WireMockServer> getMockServer =
            (WireMockProxy proxy) ->
                    new WireMockServer(
                            WireMockConfiguration.options()
                                    .port(proxy.getPort())
                                    .notifier(new ConsoleNotifier(true)));

    @BeforeEach
    void startRecording() {
        List<WireMockProxy> proxies = wireMockConfig.getProxies();
        if (!CollectionUtils.isEmpty(proxies)) {
            for (WireMockProxy proxy : proxies) {
                WireMockServer wireMockServer = getMockServer.apply(proxy);
                wireMockServer.start();
                if (proxy.isRecording()) {
                    wireMockServer.startRecording(config(proxy.getUrl(), true));
                }
                servers.add(wireMockServer);
            }
        }
    }

    @AfterEach
    void stopRecording() {
        if (!CollectionUtils.isEmpty(servers)) {
            for (WireMockServer server : servers) {
                if (server.getRecordingStatus().getStatus().equals(RecordingStatus.Recording)) {
                    server.stopRecording();
                }
                server.stop();
            }
        }
    }

    @Test
    public void testGraphQLQuery() {
        // The GraphQL query to test
        String query = "{ getGreeting(name: \"Javed\") }";

        String response = graphQLService.executeGraphQLQuery(query);

        // Send request to WireMock (which will serve the mocked response)
        //ResponseEntity<String> response = restTemplate.postForEntity(GRAPHQL_ENDPOINT, query, String.class);

        System.out.println("GraphQL Response: " + response);
        // Verify the response contains the expected mocked data
        assertTrue(response.contains("Hello, Javed!"));
    }

//    @BeforeAll
//    public static void setupWireMock() {
//        // WireMock setup for testing with recorded responses
//        WireMock.configureFor("localhost", 8081);
//
//        // Serve the recorded response from __files/response-graphql-1.json
//        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/graphql"))
//                .willReturn(WireMock.aResponse()
//                        .withHeader("Content-Type", "application/json")
//                        .withBodyFile("body-graphql-Uf0lR.json"))); // The recorded response file
//    }


    private RecordSpec config(String recordingURL, boolean recordingEnabled) {
        return WireMock.recordSpec()
                .forTarget(recordingURL)
                .onlyRequestsMatching(RequestPatternBuilder.allRequests())
                .captureHeader("Accept")
                .makeStubsPersistent(recordingEnabled)
                .ignoreRepeatRequests()
                .matchRequestBodyWithEqualToJson(true, true)
                .build();
    }
}