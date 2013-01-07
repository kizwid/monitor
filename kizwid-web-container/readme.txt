http://wiki.eclipse.org/Jetty/Howto/Configure_SSL#Configuring_Jetty_for_SSL

keytool -keystore keystore -alias jetty -genkey -keyalg RSA
openssl genrsa -des3 -out jetty.key
openssl req -new -x509 -key jetty.key -out jetty.crt
keytool -certreq -alias jetty -keystore keystore -file jetty.csr

(or openssl req -new -key jetty.key -out jetty.csr)

keytool -keystore keystore -import -alias jetty -file jetty.crt -trustcacerts

openssl pkcs12 -inkey jetty.key -in jetty.crt -export -out jetty.pkcs12

java -classpath $JETTY_HOME/lib/jetty-util-6.1-SNAPSHOT.jar:$JETTY_HOME/lib/jetty-6.1-SNAPSHOT.jar org.eclipse.jetty.security.PKCS12Import jetty.pkcs12 keystore

keytool -importkeystore -srckeystore jetty.pkcs12 -srcstoretype PKCS12 -destkeystore keystore


The default password for the keystore that is shipped with jetty is storepwd. But I would highly recommend you follow the steps listed in the http://docs.codehaus.org/display/JETTY/How+to+configure+SSL and create your own keystore
