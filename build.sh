#!/bin/bash

#mvn clean install
mvn install
docker build . -t inergy/snowflake-odata
