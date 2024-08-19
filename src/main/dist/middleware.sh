#!/bin/bash
JAVA=java

# if a JAVA_OPTS var was not supplied set a reasonable default
if [ "${JAVA_OPTS}z" == "z" ] ; then
  JAVA_OPTS="-server -XX:+UseG1GC -Xmx512m -XX:MaxMetaspaceSize=256m"
fi

# get path to this script
SCRIPT=$(readlink -f $0)

# get dir this script resides in 
SCRIPT_PARENT=`dirname ${SCRIPT}`

# change to where this script resides
cd ${SCRIPT_PARENT}

# print out environment for debugging purposes
#env

# make sure environemtn variables we require are set

# export APP_BIND_ADDRESS=0.0.0.0

# if [ "${APP_BIND_PORT}z" == "z" ] ; then
#   echo "Missing environment variable APP_BIND_PORT"
#   exit 1
# fi

if [ "${KEYCLOAK_BASE_URL}z" == "z" ] ; then
  echo "Missing environment variable KEYCLOAK_BASE_URL"
  exit 1
fi

if [ "${KEYCLOAK_REALM}z" == "z" ] ; then
  echo "Missing environment variable KEYCLOAK_REALM"
  exit 1
fi

if [ "${KEYCLOAK_CLIENT_ID}z" == "z" ] ; then
  echo "Missing environment variable KEYCLOAK_CLIENT_ID"
  exit 1
fi

if [ "${KEYCLOAK_CLIENT_SECRET}z" == "z" ] ; then
  echo "Missing environment variable KEYCLOAK_CLIENT_SECRET"
  exit 1
fi

# if [ "${KEYCLOAK_BASE_URL}z" == "z" ] ; then
#   echo "Missing environment variable KEYCLOAK_BASE_URL"
#   exit 1
# fi

# if [ "${KEYCLOAK_REALM}z" == "z" ] ; then
#   echo "Missing environment variable KEYCLOAK_REALM"
#   exit 1
# fi

# if [ "${KEYCLOAK_CLIENT_ID}z" == "z" ] ; then
#   echo "Missing environment variable KEYCLOAK_CLIENT_ID"
#   exit 1
# fi

# if [ "${LOGI_BACKEND_HOST}z" == "z" ] ; then
#   echo "Missing environment variable LOGI_BACKEND_HOST"
#   exit 1
# fi

# if [ "${LOGI_BACKEND_PORT}z" == "z" ] ; then
#   echo "Missing environment variable LOGI_BACKEND_PORT"
#   exit 1
# fi

# determine if we are running in the context of minikube specifically
# and if so we'll try to load ingress public cert into
# into the truststore so that connections to keycloak will be
# allowd via https using a self-signed cert
#
# first determine if we are on minikube or not
if [ "${K8S_NAMESPACE}z" == "devz" ] ; then
  # we are running on minikube, in the dev namespace
  # so load the public cert of ingress from
  # /secrets/dev-ingress-secret
  ENCODED_CERT_FILE="/secrets/dev-ingress-secret/tls.crt"
  DECODED_CERT_FILE="/tmp/tls.crt"

  # temporarily write the public cert of the ingress to a file
  cat ${ENCODED_CERT_FILE} > ${DECODED_CERT_FILE}
  keytool -import -cacerts -storepass ${CRED_STORE_PW} -alias authlocalcert -file ${DECODED_CERT_FILE} -noprompt
  # remove the temporarily decoded copy
  rm -f ${DECODED_CERT_FILE}  
fi

# read version from the VERSION file in this dir
# VERSION=$(cat VERSION)
# ${JAVA} ${JAVA_OPTS} -jar lib/vertx-api-mid-*.jar
${JAVA} ${JAVA_OPTS} -jar container-uber-jar.jar
