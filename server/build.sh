#!/bin/bash

if [[ $* == *--help* ]]; then
  echo "Use -t flag to run tests after building. Be sure the JDBC_DATABASE_URL env variable is set to the URL of the PostgreSQL database, or else the tests cannot run."
  exit 0
else
  mvn -DskipTests clean dependency:list install

  if [[ $? -ne 0 ]]; then
    exit $?;
  fi

  status=0
  if [[ $* == *-t* ]]; then
    if [[ -z "${JDBC_DATABASE_URL}" ]]; then
      >&2 echo "Refusing to run tests. JDBC_DATABASE_URL env variable not set to the URL of the PostgreSQL database"
      exit 1
    else
      echo -e "Running Unit Tests...\n"
      mvn test
      status=$?
      echo -e "Running Integration Tests...\n"
      java -jar target/calendue-1.0-jar-with-dependencies.jar &
      sleep 10 &&
      newman run 'docs/Calendue.postman_collection.json' --environment 'docs/Development.postman_environment.json' --timeout 60000 &&
      if [ $? -ne 0 ]; then
        status=$?
      fi
      kill %1
    fi
  fi
fi
exit $status
