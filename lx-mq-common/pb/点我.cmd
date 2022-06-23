@echo off
rem @Echo Off&Setlocal Enabledelayedexpansion

for /f "delims=" %%i in ('dir /b *.proto') do (
	if "grpc.proto" neq "%%i" (
       protoc.exe --java_out=../src/main/java %%i
	   rem protoc.exe --go_out=../ %%i
	)
	
)
rem protoc.exe --go_out=../ grpc.proto --go-grpc_out=../ grpc.proto


pause