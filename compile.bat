@echo off
title Compiling Editor . . .

:com
SET PATH=D:\Java\jdk1.7.0_75\bin
echo Compiling Editor . . .
javac -Xlint -d bin *.java
echo done.
pause
cls
goto com