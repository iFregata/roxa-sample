javaw -Dfile.encoding=UTF-8 ^
-Dlog4j.configurationFile=./conf/log4j2.xml ^
-Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector ^
-Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory ^
-XX:+UseG1GC -Xms128M -Xmx128M ^
-jar roxa-sample-1.0.0-SNAPSHOT-fat.jar