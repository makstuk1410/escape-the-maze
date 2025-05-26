#!/bin/bash

JFX=~/javafx-clean/javafx-sdk-24.0.1/lib

java \
--module-path "$JFX" \
--add-modules javafx.controls,javafx.fxml \
-Djava.library.path="$JFX" \
-jar target/EscapeTheMaze-1.0-SNAPSHOT-jar-with-dependencies.jar

