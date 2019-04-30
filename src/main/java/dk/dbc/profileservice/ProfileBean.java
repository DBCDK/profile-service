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

import dk.dbc.openagency.Profile;
import java.io.IOException;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@Stateless
@Path("/profile")
public class ProfileBean {

    private static final Logger log = LoggerFactory.getLogger(ProfileBean.class);

    @EJB
    OpenAgencyCache openAgency;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{agencyId : [1-9][0-9]*}/{profile : .+}")
    public Profile profile(@PathParam("agencyId") int agencyId,
                           @PathParam("profile") String profile) {
        log.info("Requested: {}/{}", agencyId, profile);

        try {
            Map<String, Profile> profiles = openAgency.getProfilesFor(agencyId);
            Profile result = profiles.get(profile);
            if (result == null)
                result = new Profile("Unknown profile");
            return result;
        } catch (IOException ex) {
            throw new ServerErrorException("Error processing downstream response", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

}
