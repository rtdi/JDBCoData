#!/bin/sh
set -e

/opt/configure.py --rootpath ${PAYARA_DIR}
source ${PAYARA_DIR}/deploy.properties

exec java -XX:MaxRAMPercentage=${MEM_MAX_RAM_PERCENTAGE} -Xss${MEM_XSS} -XX:+UseContainerSupport ${JVM_ARGS} -jar payara-micro.jar "$@"
