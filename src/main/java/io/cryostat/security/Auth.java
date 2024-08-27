/*
 * Copyright The Cryostat Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.cryostat.security;

import java.net.URI;

import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;

@Path("")
public class Auth {

    @POST
    @Path("/api/v4/logout")
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public RestResponse<Object> logout(@Context RoutingContext context) {
        return ResponseBuilder.create(RestResponse.Status.PERMANENT_REDIRECT)
                .location(URI.create("/oauth2/sign_out"))
                .build();
    }

    @POST
    @Path("/api/v4/auth")
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public AuthResponse login(@Context RoutingContext context, SecurityContext securityContext) {
        String user =
                securityContext.getUserPrincipal() != null
                        ? securityContext.getUserPrincipal().getName()
                        : context.request().getHeader("X-Forwarded-User");
        if (user == null) {
            user = "";
        }
        return new AuthResponse(user);
    }

    static record AuthResponse(String username) {}
}
