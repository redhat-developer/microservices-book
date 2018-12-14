package com.redhat.examples.hellomicroprofile.rest;

import javax.ws.rs.*;
import java.net.*;


@Path("/api")
public class HelloRestController {

    @GET
    @Produces("text/plain")
    @Path("/hello")
    public String hello() {
        String hostname = null;
        try {
            hostname = InetAddress.getLocalHost()
                    .getHostAddress();
        } catch (UnknownHostException e) {
            hostname = "unknown";
        }
        return "Hello Microprofile from " + hostname;
    }

}
