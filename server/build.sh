#!/bin/bash

if [[ $* == *--help* ]]; then
  echo "Use -t flag to run integration tests after building. Be sure the JDBC_DATABASE_URL env variable is set to the URL of the PostgreSQL database, or else the server will not start."
  exit 0
else
  mvn -DskipTests clean dependency:list install
  mvn test

  if [[ $* == *-t* ]]; then
    if [[ -z "${JDBC_DATABASE_URL}" ]]; then
      >&2 echo "Refusing to run integration tests. JDBC_DATABASE_URL env variable not set to the URL of the PostgreSQL database"
      exit 1
    else
      java -jar target/calendue-1.0-jar-with-dependencies.jar &
      sleep 10 &&
      newman run 'docs/Calendue.postman_collection.json' --environment 'docs/Development.postman_environment.json' --timeout 60000 &&
      kill %1
      exit 0
    fi
  fi
fi
