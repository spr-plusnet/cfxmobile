#!/bin/bash
export GRAALVM_HOME=/opt/graalvm-ce-java11-21.1.0
export JAVA_HOME=/opt/graalvm-ce-java11-21.1.0
mvn -U clean gluonfx:build
