#!/bin/bash

mvn clean install
docker build . -t inergy/snowflake-odata
