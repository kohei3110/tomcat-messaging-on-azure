#!/bin/bash

if ! command -v java &> /dev/null
then
    echo "Java command not found. Installing Java..."
    apt update
    apt install -y msopenjdk-21
fi

export PATH=$PATH:$JAVA_HOME/bin
java -jar FileProcessor.jar