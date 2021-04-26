FROM openjdk:8-alpine as builder

LABEL maintainer="https://jbake.org/community/team.html"

ENV JBAKE_HOME=/opt/jbake

RUN mkdir -p ${JBAKE_HOME}
COPY . /usr/src/jbake

RUN set -o errexit -o nounset \
    && echo "Building JBake" \
    && cd /usr/src/jbake \
    && ./gradlew installDist \
    && cp -r jbake-dist/build/install/jbake/* $JBAKE_HOME \
    && rm -r ~/.gradle /usr/src/jbake

FROM openjdk:8-jre-alpine

ENV JBAKE_USER=jbake
ENV JBAKE_HOME=/opt/jbake
ENV PATH ${JBAKE_HOME}/bin:$PATH

RUN adduser -D -u 1000 -g "" ${JBAKE_USER} ${JBAKE_USER}

USER ${JBAKE_USER}

COPY --from=builder /opt/jbake /opt/jbake

WORKDIR /mnt/site

VOLUME ["/mnt/site"]

ENTRYPOINT ["jbake"]
CMD ["-b","-s"]

EXPOSE 8820
