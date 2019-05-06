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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
@SuppressFBWarnings(value = {"NP_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
public class Profiles {

    public static Map<String, Profile> from(OAProfileResponse response, String agencyId) {
        String ownCollectionIdenifer = agencyId + "-katalog";
        return response.openSearchProfileResponse.profile.stream()
                .map(ProfileTemp::new)
                .collect(Collectors.toMap(ProfileTemp::getName, p -> p.getProfile(ownCollectionIdenifer)));
    }

    @SuppressFBWarnings(value = {"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
    private static class ProfileTemp {

        private static final long serialVersionUID = 9173310341499192210L;

        private final String name;
        private final List<String> search;

        ProfileTemp(OAProfile profile) {
            name = profile.profileName;
            search = profile.source.stream()
                    .filter(source -> source.sourceSearchable)
                    .map(source -> source.sourceIdentifier)
                    .collect(Collectors.toSet()) // UNIQ
                    .stream()
                    .sorted()
                    .collect(Collectors.toList());
        }

        String getName() {
            return name;
        }

        List<String> getSearch() {
            return search;
        }

        Profile getProfile(String ownCollectionIdenifer) {
            // We know profiles don't have special characters ('"' or '\')
            // so simple quotes are enough for SolR
            boolean includeOwnHoldings = search.contains(ownCollectionIdenifer);
            String filterQuery = search.stream()
                    .map(c -> "rec.collectionIdentifier:\"" + c + "\"")
                    .collect(Collectors.joining(" OR "));
            if (includeOwnHoldings)
                filterQuery = filterQuery + " OR rec.holdingsAgencyId:\"" + ownCollectionIdenifer + "\"";
            return new Profile(search, includeOwnHoldings, filterQuery);
        }
    }
}
