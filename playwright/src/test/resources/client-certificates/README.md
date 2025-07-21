# Client Certificate test-certificates

Regenerate all certificates by running:

```
bash generate.sh
```

## Java: Convert PEM Files to PKCS12

```
openssl pkcs12 -export -in server_cert.pem -inkey server_key.pem -out server_keystore.p12 -name myalias
```
