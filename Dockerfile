FROM payara/micro:5.2022.2-jdk11

COPY target/snowflake-odata.war ${DEPLOY_DIR}
COPY entrypoint.sh deploy.properties.template ${PAYARA_DIR}

USER root
ADD requirements.txt .
RUN apk update && apk add python3 \
	&& python3 -m ensurepip --upgrade \
	&& python3 -m pip install -r requirements.txt
COPY utils /opt
USER payara

EXPOSE 8080
