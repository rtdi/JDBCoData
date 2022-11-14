FROM payara/micro:5.2022.2-jdk11

# indicate we are running for Salesforce consumers
# this will set all string max lengths to 255
ENV SALESFORCE_TOGGLE=true

COPY target/snowflake-odata.war ${DEPLOY_DIR}
COPY deploy.properties.template ${PAYARA_DIR}
COPY entrypoint.sh ${PAYARA_DIR}
COPY logging.properties ${PAYARA_DIR}

USER root
ADD requirements.txt .
RUN apk update && apk add python3 \
	&& python3 -m ensurepip --upgrade \
	&& python3 -m pip install -r requirements.txt
COPY utils /opt
USER payara

EXPOSE 8080
