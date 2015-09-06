#!/bin/sh
mvn -f module/daas-we/pom.xml clean install
mvn -f module/daas-we-sample/pom.xml clean install
