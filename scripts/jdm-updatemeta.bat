@echo off

if "%JAVA_HOME%" == "" goto noJavaHome
if "%JGM_HOME%" == "" goto noJgmHome
%JAVA_HOME%\bin\java -classpath %JGM_HOME%\lib\jgm-client.jar;%JAVA_HOME%\lib\tools.jar;%JGM_HOME%\lib\ant.jar;%JGM_HOME%\lib\commons-cli-1.0.jar;%JGM_HOME%\lib\commons-httpclient-2.0.jar;%JGM_HOME%\lib\commons-logging.jar;%JGM_HOME%\lib\log4j-1.2.8.jar;%JGM_HOME%\lib\xmllight.jar;%JGM_HOME%\lib\jaxrpc.jar;%JGM_HOME%\lib\axis.jar;%JGM_HOME%\lib\commons-discovery.jar;%JGM_HOME%\lib\saaj.jar;%JGM_HOME%\lib\wsdl4j.jar ve.usb.jgm.tools.admin.UpdateMetadata %*
goto end

:noJavaHome
    echo JAVA_HOME environment variable not set.
    echo Please set JAVA_HOME pointing to a valid JDK or JRE location
    goto end

:noJgmHome
    echo JGM_HOME environment variable not set.
    echo Please set JGM_HOME pointing to the Java Grid Machine installation directory
    goto end

:end