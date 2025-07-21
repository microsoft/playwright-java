cd "$(dirname "$0")"
CWD=$(pwd)

cd server
openssl pkcs12 -export -in server_cert.pem -inkey server_key.pem -out server_keystore.p12 -name myalias -passout pass:

cd "$CWD/client/trusted"
openssl pkcs12 -export -in cert.pem -inkey key.pem -out keystore.p12 -name myalias -passout pass:
