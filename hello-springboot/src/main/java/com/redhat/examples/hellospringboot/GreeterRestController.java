/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.examples.hellospringboot;

import com.netflix.hystrix.contrib.javanica.annotation.*;
import io.opentracing.*;
import io.opentracing.contrib.spring.web.client.*;
import io.opentracing.contrib.tracerresolver.*;
import io.opentracing.propagation.*;
import io.opentracing.tag.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.context.properties.*;
import org.springframework.boot.web.client.*;
import org.springframework.context.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.*;

import javax.servlet.http.*;
import java.util.*;

@RestController
@RequestMapping("/api")
@ConfigurationProperties(prefix="greeting")
public class GreeterRestController {

    private RestTemplate template = new RestTemplateBuilder().build();

    private String saying, backendServiceHost;

    private int backendServicePort;

    @RequestMapping(value = "/greeting",
            method = RequestMethod.GET, produces = "text/plain")
    @HystrixCommand(fallbackMethod = "fallback")
    public String greeting(){
        Tracer tracer = TracerResolver.resolveTracer();

        template.setInterceptors(Collections.singletonList(new TracingRestTemplateInterceptor(tracer)));

        String backendServiceUrl =
                String.format(
                        "http://%s:%d/api/backend?greeting={greeting}",
                        backendServiceHost, backendServicePort);

        System.out.println("Sending to: " + backendServiceUrl);


        BackendDTO response = template.getForObject(
                backendServiceUrl, BackendDTO.class, saying);

        return response.getGreeting() + " at host: " +
                response.getIp();
    }

    public String fallback(){
        return saying + " at host "  +
                System.getenv("HOSTNAME") + " - (fallback)";
    }

    public void setSaying(String saying) {
        this.saying = saying;
    }

    public void setBackendServiceHost(String backendServiceHost) {
        this.backendServiceHost = backendServiceHost;
    }

    public void setBackendServicePort(int backendServicePort) {
        this.backendServicePort = backendServicePort;
    }
}