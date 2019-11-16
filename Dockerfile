FROM mozilla/sbt

ARG PORT
ENV PORT=9090

COPY . /redis-proxy
WORKDIR redis-proxy

RUN sbt compile
CMD sbt run

EXPOSE 9090