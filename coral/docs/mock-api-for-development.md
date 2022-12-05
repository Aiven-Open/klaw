# Mocking an API for development

We're using [MSW](https://mswjs.io/) to mock our API in tests. We also have setup a [service-worker](src/services/api-mocks/browser.ts). This can be used to mock API calls in the component during development.

We recommend working with a [remote API](development-with-remote-api.md). But there may be reasons you want to work on Coral without setting up one. 

## How to mock an API

In this example, we assume you want to mock the [`team-api.ts`](../src/domain/team/team-api.ts).

The API call looks like this:

```ts

const getTeams = () => {
 return api
   .get<TeamNamesGetResponse>("/getAllTeamsSUOnly")
   .then(transformTeamNamesGetResponse);
};

```

Co-located to this file, you'll find [`team-api.msw.ts`](../src/domain/team/team-api.msw.ts). This is the MSW mock we're using in our tests. See how we use this mock in the [`SelectTeam.test.tsx`](../src/app/features/browse-topics/components/select-team/SelectTeam.test.tsx) test. 

**If there is no mock already, you can create a new one.** The mock will be useful for testing later!

💡 mocks for APIs are always co-located to the `-api.ts` file. The name is the same, with the `msw` addition. `team-api.ts` -> `team-api.ts`.

### Use mock to temporary replace API call
You can use this mock to replace the actual API call. This enables you to develop e.g a feature without having to use a remote API. Before committing please remove this part. It's only meant to be used for local development. 

We use the `getTeams` function in our `useGetTeams` hook. That is where you can add the MSW mock:

```tsx

function useGetTeams(): UseQueryResult<Team[]> {
// everything in useEffect is used to mock the api call
// please remove the whole useEffect block before commiting!
useEffect(() => {
const browserEnvWorker = window.msw;
   if (browserEnvWorker) {
     mockGetTeams({
       mswInstance: browserEnvWorker,
       response: {
         status: 200,
         data: //<YOUR_MOCKED_DATA>
       } 
     });
   }
}, []);

return useQuery<Team[], Error>(["topic-teams"], () => getTeams());
}

```

API calls to `/getAllTeamsSUOnly` will now return your mocked data instead of making a request. 🎉