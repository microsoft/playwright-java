# Client Certificate test-certificates

Regenerate all certificates by running:

```
bash generate.sh
```

## Java: Convert PEM Files to PKCS12


Java server understands only PKCS12 keys, after copying the certificates from Node.js Playwright we need to convert them.

```
bash generate_java.sh
```
