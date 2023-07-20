# Development with local Klaw

## Basic setup

** ℹRequirements**

- [node](https://nodejs.org/en/) needs to be installed <br/>
  -> see [nvmrc](.nvmrc) or the `engines` definition in [package.json](package.json) for version).
- Coral uses [pnpm](https://pnpm.io/) (version 7) as a package manager. Read their official documentation [how to install](https://pnpm.io/installation) pnpm.

1. navigate to `/coral`
2. run `pnpm install`
3. run `pnpm add-precommit` the first time you install the repository to set the custom directory for our pre commit hooks.
4. [add api mock for `getAuth`](../docs/development-witout-api.md#necessary-api-mock-for-getauth)
5. run `pnpm dev-without-remote-api` to start the development mode.


## Necessary API mock for `getAuth`

Coral is only accessible for authenticated users. Without API, the call to `/getAuth` that gets the user, you'll only see a loading animation. 

Mocking the API response for that is necessary to access Coral:

1. go to [auth-user-api.ts](../src/domain/auth-user/auth-user-api.ts)
2. In function `getAuth`, return a hard coded AuthUser for this function:

```typescript

const testAuthUser: AuthUser = {
  canSwitchTeams: "false",
  teamId: "12345",
  teamname: "NightsWatch",
  username: "Jon Snow",
};

function getAuth(): Promise<AuthUser> {
  return Promise.resolve(testAuthUser);
}
```
🙇  Please remember removing the mock before opening a PR for review :)

## Mocking API responses general
If you want to work on UI that needs data from an enpdoint, you can mock the responses during the development process. This can be done by returning a resolved (or rejected) `Promise` instead of executing the API call during development. 

**Example: mocking the response for `getClusterInfoFromEnvironment`:**

- go the [`src/domain/cluster/cluster-api.ts`](../src/domain/cluster/cluster-api.ts)
- check the return type of the function (`KlawApiResponse<"getClusterInfoFromEnv">`) in our automatically generated api definition to create the right object: ([api.d.ts](../types/api.d.ts) to create the right object.
- return a promise with your data instead of calling the endpoint and comment out the actual endpoint call:

```typescript
const getClusterInfoFromEnvironment = async ({
envSelected,
envType,
}: {
envSelected: string;
envType: Environment["type"];
}): Promise<KlawApiResponse<"getClusterInfoFromEnv">> => {

const mock: KlawApiResponse<"getClusterInfoFromEnv"> = {
aivenCluster: true,
};

return Promise.resolve(mock);
};

```

🙇  Please remember removing the mock before opening a PR for review :)