package com.redhat.examples;

import com.fasterxml.jackson.databind.*;
import org.apache.camel.*;
import org.apache.camel.builder.*;
import org.apache.camel.model.dataformat.*;
import org.apache.camel.model.rest.*;
import org.apache.camel.util.toolbox.*;
import org.springframework.boot.context.properties.*;
import org.springframework.stereotype.*;
import org.springframework.stereotype.Component;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * A simple Camel route that triggers from a timer and calls a bean and prints to system out.
 * <p/>
 * Use <tt>@Component</tt> to make Camel auto detect this route when starting.
 */
@Component
@ConfigurationProperties(prefix="gateway")
public class MySpringBootRouter extends RouteBuilder {

    private String springbootsvcurl, microprofilesvcurl;

    private static final String REST_ENDPOINT=
            "http4:%s/api/greeting?httpClient.connectTimeout=1000&bridgeEndpoint=true&copyHeaders=true&connectionClose=true";

    @Override
    public void configure() {
        from("direct:microprofile").streamCaching()
                .to(String.format(REST_ENDPOINT, microprofilesvcurl))
                .log("Response from Microprofile microservice: ${body}")
                .convertBodyTo(String.class)
                .end();

        from("direct:springboot").streamCaching()
                .to(String.format(REST_ENDPOINT, springbootsvcurl))
                .log("Response from Spring Boot microservice: ${body}")
                .convertBodyTo(String.class)
                .end();

        rest()
            .get("/gateway").enableCORS(true)
            .route()
                .multicast(AggregationStrategies.flexible().accumulateInCollection(ArrayList.class))
                .parallelProcessing()
                    .to("direct:microprofile")
                    .to("direct:springboot")
                .end()
            .marshal().json(JsonLibrary.Jackson)
            .convertBodyTo(String.class)
        .endRest();
    }

    public void setMicroprofilesvcurl(String microprofilesvcurl) {
        this.microprofilesvcurl = microprofilesvcurl;
    }

    public void setSpringbootsvcurl(String springbootsvcurl) {
        this.springbootsvcurl = springbootsvcurl;
    }

}
