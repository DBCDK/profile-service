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
package dk.dbc.openagency;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class Profile implements Serializable {

    private static final long serialVersionUID = 1173310332199192210L;

    public boolean success;
    public String error;
    public List<String> search;
    public boolean includeOwnHoldings;
    public String filterQuery;

    public Profile(String error) {
        this.success = false;
        this.error = error;
    }

    public Profile(List<String> search, boolean includeOwnHoldings, String filterQuery) {
        this.success = true;
        this.search = search;
        this.includeOwnHoldings = includeOwnHoldings;
        this.filterQuery = filterQuery;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + ( this.success ? 1 : 0 );
        hash = 41 * hash + Objects.hashCode(this.error);
        hash = 41 * hash + Objects.hashCode(this.search);
        hash = 41 * hash + ( this.includeOwnHoldings ? 1 : 0 );
        hash = 41 * hash + Objects.hashCode(this.filterQuery);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        final Profile other = (Profile) obj;
        return this.success == other.success &&
               this.includeOwnHoldings == other.includeOwnHoldings &&
               Objects.equals(this.error, other.error) &&
               Objects.equals(this.filterQuery, other.filterQuery) &&
               Objects.equals(this.search, other.search);
    }

}
