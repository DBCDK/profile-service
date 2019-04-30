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

import dk.dbc.badgerfish.BadgerFishReader;
import dk.dbc.openagency.OAProfileResponse;
import dk.dbc.openagency.Profile;
import dk.dbc.openagency.Profiles;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Locale;
import java.util.Map;
import javax.cache.annotation.CacheResult;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@Stateless
public class OpenAgencyCache {

    private static final Logger log = LoggerFactory.getLogger(OpenAgencyCache.class);

    @Inject
    Config config;

    @CacheResult(cacheName = "oaProfile",
                 exceptionCacheName = "oaProfileError",
                 cachedExceptions = {ClientErrorException.class,
                                     ServerErrorException.class,
                                     IOException.class})
    public Map<String, Profile> getProfilesFor(int agency) throws IOException {
        String agencyId = String.format(Locale.ROOT, "%06d", agency);
        log.info("Fetching profile for: {}", agencyId);
        URI uri = config.getOpenAgencyUrl()
                .queryParam("action", "openSearchProfile")
                .queryParam("agencyId", agencyId)
                .queryParam("profileVersion", 3)
                .queryParam("outputType", "json")
                .build();
        try (InputStream is = get(uri)) {
            OAProfileResponse response = BadgerFishReader.O.readValue(is, OAProfileResponse.class);
            log.trace("OAProfileResponse: {}", response);
            return Profiles.from(response, agencyId);
        } catch (IOException ex) {
            log.error("Error processing OpenAgency response for agency: {}: {}", agencyId, ex.getMessage());
            log.debug("Error processing OpenAgency response for agency: {}: ", agencyId, ex);
            throw ex;
        }
    }

    private InputStream get(URI uri) {
        try {
            log.debug("Fetching: {}", uri);
            InputStream is = config.getClient()
                    .target(uri)
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .get(InputStream.class);
            if (is == null)
                throw new ClientErrorException("No content from downstream", 500);
            return is;
        } catch (ClientErrorException | ServerErrorException ex) {
            log.error("Error fetching resource: {}: {}", uri, ex.getMessage());
            log.debug("Error fetching resource: ", ex);
            throw ex;
        }
    }
}
