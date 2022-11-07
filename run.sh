#!/bin/bash

export SALESFORCE_TOGGLE=true

docker run -it --rm -p 8080:8080 \
-e KEY_VAULT_NAME -e KEY_VAULT_URI -e CONF_KEYVAULT=${KEY_VAULT_URI} -e AZURE_TENANT_ID -e AZURE_SUBSCRIPTION_ID \
-e AZURE_CLIENT_ID=${APP_ETL_CLIENTID} -e AZURE_CLIENT_SECRET=${APP_ETL_PASSWORD} \
-e ODATA_SNOWFLAKE_ACCOUNT -e ODATA_SNOWFLAKE_USER -e ODATA_SNOWFLAKE_PASSWORD -e ODATA_SNOWFLAKE_DB \
-e ODATA_CLIENT_USER -e ODATA_CLIENT_PASSWORD \
inergy/snowflake-odata

# undocumented problems:
# --lite results in exceptions
# --nocluster does not deploy applications
