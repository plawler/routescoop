FROM        ysihaoy/scala-play:2.11.8-2.5.4-sbt-0.13.11 as build

COPY        ["build.sbt", "/tmp/build/"]
COPY        ["project/plugins.sbt", "project/build.properties", "/tmp/build/project/"]
RUN         cd /tmp/build && \
            activator compile && \
            rm -rf /tmp/build

# copy code
COPY        . /routescoop-web
WORKDIR     /routescoop-web
RUN         activator stage
CMD         ["activator"]

FROM        pelawler/openjdk-8-jre
COPY        --from=build routescoop-web/target/universal/stage /opt/app
EXPOSE      9000
CMD         /opt/app/bin/routescoop-web
