# Development with local Klaw

Development against a real Klaw API will yield in better developer confidence of the functionality and developer experience compared to using a mocked API. 
We want to enable contributors to have that developer experience without the need to have a deployed Klaw running and use this as remote API. 

With the mode `local-api` we can run Klaw in a docker container. This can be used as a local api now. We created a small node proxy to help enable the best developer experience (we hope!)

## Run development server with local-api mode

ℹ️ environment settings are located in the file [`.env.local-api`](/coral/.env.local-api).

- go to directory [`proxy`](/coral/proxy)
- in `/proxy`, run `pnpm dev` 
  - check out the [documentation](/coral/proxy/README.md) for more scripts
- ❗️ the proxy runs on [`http://localhost:1337`](http://localhost:1337)
  - Currently, the correct redirect for login and authentication is not working. To authorize yourself:
    - Go to your [local Klaw](http://localhost:9097/login)
    - Login with your credentials
    - After you've been sucessfully logged in, go back to the proxy -> [`http://localhost:1337`](http://localhost:1337)
    

💡currently the proxy also does not redirect you to the login if your access expires. If you're getting related errors from your API, please login again like described 🙏