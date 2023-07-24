# Development with remote API

## Table of content

* [First setup for remote API](#first-setup-for-remote-api)
    + [Create new vite mode for remote API development](#create-new-vite-mode-for-remote-api-development)
    + [Set up self-signed certificate (required when API runs on HTTPS)](#set-up-self-signed-certificate-required-when-api-runs-on-https)
* [How to run the project](#how-to-run-the-project)


## First setup for remote API

** ℹRequirements**

- [node](https://nodejs.org/en/) needs to be installed <br/>
  -> see [nvmrc](../.nvmrc) or the `engines` definition in [package.json](../package.json) for version).
- Coral uses [pnpm](https://pnpm.io/) (version 7) as a package manager. Read their official documentation [how to install](https://pnpm.io/installation) pnpm.

1. navigate to `/coral`
2. run `pnpm install`
3. run `pnpm add-precommit` the first time you install the repository to set the custom directory for our pre commit hooks.
4. Run development:
   4.1. If you have not setup a remote API mode, please follow [First setup](../docs/development-with-remote-api.
   md#first-setup)
   4.2. If you already have a setup, run `pnpm dev`
5. [Create new Vite mode for remote API development](#create-new-vite-mode-for-remote-api-development)
6. When your API runs on `https`: [Set up self-signed certificate](#set-up-self-signed-certificate-required-when-api-runs-on-https)

### Create new Vite mode for remote API development
Start by creating a new [Vite mode](https://vitejs.dev/guide/env-and-mode.html) called `.env.remote-api`. The easiest 
way is to create one is to run the commend below. Note, that you need to replace the `[KLAW API ORIGIN]` placeholder value with your Klaw API server origin.

```
cat << EOF > .env.remote-api
NODE_ENV=development
VITE_PROXY_TARGET=[KLAW API ORIGIN]
VITE_API_BASE_URL=https://127.0.0.1:5173/api
VITE_SERVER_CERTIFICATE_PATH=".cert/localhost.crt"
VITE_SERVER_CERTIFICATE_KEY_PATH=".cert/localhost.key"
EOF
```

### Set up self-signed certificate (required when API runs on HTTPS)

You can use Let's Encrypt [instructions](https://letsencrypt.org/docs/certificates-for-localhost/#making-and-trusting-your-own-certificates) for setting up self-signed certificates for local development. 

```
openssl req -x509 -out .cert/localhost.crt -keyout .cert/localhost.key \
    -newkey rsa:2048 -nodes -sha256 \
    -subj '/CN=localhost' -extensions EXT -config <( \
    printf "[dn]\nCN=localhost\n[req]\ndistinguished_name = dn\n[EXT]\nsubjectAltName=DNS:localhost,IP:127.0.0.1\nkeyUsage=digitalSignature\nextendedKeyUsage=serverAuth")
```

Install `.cert/localhost.crt` in your list of locally trusted roots. This is different dependent on your OS, you can look up a current way to do this online. Keep in mind that dependent on your OS and browser, you may have to refresh that certificate from time to time.    


## How to run the project

If all requirements are met, and you've done your first setup:

- `pnpm run dev`
- login to Klaw with your credentials on `<YOUR_LOCALHOST>:5173/login`

