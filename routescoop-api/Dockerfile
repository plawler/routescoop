FROM        ysihaoy/scala-play:2.12.3-2.6.2-sbt-0.13.15 as build

RUN         apk add --update nodejs

COPY        ["build.sbt", "/tmp/build/"]
COPY        ["project/plugins.sbt", "project/build.properties", "/tmp/build/project/"]
RUN         cd /tmp/build && \
            sbt compile && \
            rm -rf /tmp/build

# copy code
COPY        . /routescoop-api
WORKDIR     /routescoop-api
RUN         sbt stage
CMD         ["sbt"]

FROM        pelawler/openjdk-8-jre
COPY        --from=build routescoop-api/target/universal/stage /opt/app
EXPOSE      9000
CMD         /opt/app/bin/routescoop-api
