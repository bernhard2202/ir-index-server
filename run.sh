#!/bin/sh
mvn clean package
java -jar -Dlog4j.configuration=file:./config/log4j.properties ./target/indexserver-0.1.jar 
