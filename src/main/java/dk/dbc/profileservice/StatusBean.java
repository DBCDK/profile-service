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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@Stateless
@Path("status")
public class StatusBean {
    private static final Logger log = LoggerFactory.getLogger(StatusBean.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Status getStatus() {
        log.info("getStatus called");
        return new Status();
    }

    @SuppressFBWarnings()
    public static class Status {

        public boolean success;

        public String error;

        public Status() {
            this.success = true;
            this.error = null;
        }

        public Status(String error) {
            this.success = false;
            this.error = error;
        }

    }

}
