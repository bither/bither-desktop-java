#!/usr/bin/env bash
cd vanitygen/
make clean
make
cd ..
make clean
make
javac OclVanitygen.java
java OclVanitygen
