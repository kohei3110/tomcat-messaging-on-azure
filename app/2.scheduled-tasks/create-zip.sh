#!/bin/bash

javac FileProcessor.java
jar cfm FileProcessor.jar manifest.txt FileProcessor.class
zip archive.zip FileProcessor.jar run.sh