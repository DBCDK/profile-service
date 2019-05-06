/*
 * Copyright (C) 2019 DBC A/S (http://dbc.dk/)
 *
 * This is part of profile-service
 *
 * profile-service is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * profile-service is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.profileservice;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.PostConstruct;
import javax.ejb.EJBException;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.UriBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@ApplicationScoped
@Singleton
@Startup
@Lock(LockType.READ)
public class Config {

    private static final Logger log = LoggerFactory.getLogger(Config.class);
    private final Map<String, String> env;

    private Client client;
    private UriBuilder openAgencyUrl;

    public Config() {
        env = System.getenv();
    }

    Config(Map<String, String> env) {
        this.env = env;
    }

    @PostConstruct
    public void init() {
        String userAgent = get("USER_AGENT").orElse("Unknown/0.1");
        log.debug("Using: {} as HttpUserAgent", userAgent);
        this.client = ClientBuilder.newBuilder()
                .register((ClientRequestFilter) (ClientRequestContext context) -> {
                    context.getHeaders().putSingle("User-Agent", userAgent);
                }).build();
        this.openAgencyUrl = UriBuilder.fromUri(get("OPEN_AGENCY_URL")
                .orElseThrow(required("OPEN_AGENCY_URL")));
    }

    public Client getClient() {
        return client;
    }

    public UriBuilder getOpenAgencyUrl() {
        return openAgencyUrl.clone();
    }

    private Optional<String> get(String key) {
        String value = env.get(key);
        if (value == null)
            return Optional.empty();
        return Optional.of(value);
    }

    private Supplier<RuntimeException> required(String name) {
        return () -> {
            return new EJBException("Required environment variable " + name + " is unset");
        };
    }

}
