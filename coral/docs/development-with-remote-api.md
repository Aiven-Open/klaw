# Development with remote API

Development against a real Klaw API will yield in better developer confidence of the functionality and developer experience compared to using a mocked API. The remote API can be a shared staging server, or even a production system. All command below are expected to be run in the `coral` directory.

## Create new vite mode for remote API development
Start by creating a new [vite mode](https://vitejs.dev/guide/env-and-mode.html) called `.env.remote-api`. The easiest way is to create one is to run the commend below. Note, that you need to replace the `[KLAW API ORIGIN]` placeholder value with your Klaw API server origin.
```
cat << EOF > .env.remote-api
NODE_ENV=development
VITE_PROXY_TARGET=[KLAW API ORIGIN]
VITE_API_BASE_URL=https://127.0.0.1:5173/api
VITE_SERVER_CERTIFICATE_PATH=".cert/localhost.crt"
VITE_SERVER_CERTIFICATE_KEY_PATH=".cert/localhost.key"
EOF
```

## Set up self-signed certificate [required when API runs on HTTPS]

Let's Encrypt [instructions](https://letsencrypt.org/docs/certificates-for-localhost/#making-and-trusting-your-own-certificates) for setting up self-signed certificates for local development. 

```
openssl req -x509 -out .cert/localhost.crt -keyout .cert/localhost.key \
    -newkey rsa:2048 -nodes -sha256 \
    -subj '/CN=localhost' -extensions EXT -config <( \
    printf "[dn]\nCN=localhost\n[req]\ndistinguished_name = dn\n[EXT]\nsubjectAltName=DNS:localhost,IP:127.0.0.1\nkeyUsage=digitalSignature\nextendedKeyUsage=serverAuth")
```

Install `.cert/localhost.crt` in your list of locally trusted roots. You can find  

## Run development server with remote-api mode

- `pnpm run dev`
- login to Klaw with your credentials on `<YOUR_LOCALHOST>:5173/login`
💡currently coral does not redirect you to the login if your access expires. If you're getting related errors from your API, please login again!  