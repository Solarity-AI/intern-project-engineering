#!/bin/sh
# Parse DATABASE_URL (postgresql://user:pass@host:port/db) into JDBC format
if [ -n "$DATABASE_URL" ]; then
  DB_STRIPPED=${DATABASE_URL#postgresql://}
  DB_USERPASS=${DB_STRIPPED%%@*}
  DB_USER=${DB_USERPASS%%:*}
  DB_PASS=${DB_USERPASS#*:}
  DB_HOSTPORTDB=${DB_STRIPPED#*@}
  DB_HOSTPORT=${DB_HOSTPORTDB%%/*}
  DB_NAME=${DB_HOSTPORTDB#*/}
  case "$DB_HOSTPORT" in
    *:*) DB_HOST=${DB_HOSTPORT%%:*}; DB_PORT=${DB_HOSTPORT#*:} ;;
    *)   DB_HOST=$DB_HOSTPORT; DB_PORT=5432 ;;
  esac
  export JDBC_DATABASE_URL="jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}"
  export SPRING_DATASOURCE_USERNAME="$DB_USER"
  export SPRING_DATASOURCE_PASSWORD="$DB_PASS"
fi
exec java -Dserver.port=${PORT} -jar app.jar
