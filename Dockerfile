FROM payara/micro:5.2022.2-jdk11

COPY target/snowflake-odata.war $DEPLOY_DIR
