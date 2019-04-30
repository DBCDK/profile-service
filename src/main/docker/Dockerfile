FROM docker.dbc.dk/payara5-micro

ENV CACHE_SUCCESS_MINUTES=60 \
    CACHE_ERROR_MINUTES=10 \
    USER_AGENT="profile-service/1.0"

LABEL CACHE_SUCCESS_MINUTES="How long (in minutest) to cache success results from openagency (default: 60)" \
    CACHE_ERROR_MINUTES="How long (in minutest) to cache failure results from openagency (default: 10)" \
    SCOPE="Name of environment (required)" \
    OPEN_AGENCY_URL="http:// address of open-agency service (required)" \
    PROFILES_ENABLED="comma seperated list of profiles that are allowed to scan" \
    USER_AGENT="name of user agent in http requests"

ADD target/docker/app.json target/${project.build.finalName}.war deployments/
