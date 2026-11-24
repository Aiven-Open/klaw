# Directory structure

The structure is inspired in big parts by:

- [How To Structure React Projects From Beginner To Advanced](https://blog.webdevsimplified.com/2022-07/react-folder-structure/). See the "Advanced Folder Structure".
- [Elegant Frontend Architecture](https://michalzalecki.com/elegant-frontend-architecture/).

## Table of content

- [🏁️ Primary goals of this structure](#-primary-goals-of-this-structure)
- [🗂️ Folder and file naming](#-folder-and-file-naming)
- [💁 Outline folder structure](#-outline-folder-structure)
- [ℹ️ In-depth explanation](#ℹ-in-depth-explanation)
  - [First level: `app` folder](#first-level-app-folder)
    - [Second level: `app/assets`](#second-level-appassets)
    - [Second level: `app/components`](#second-level-appcomponents)
    - [Second level: `app/features`](#second-level-appfeatures)
    - [Second level: `app/hooks`](#second-level-apphooks)
    - [Second level: `app/pages`](#second-level-apppages)
  - [First level: `domain` folder](#first-level-domain-folder)
  - [First level: `services`](#first-level-services)

## 🏁️ Primary goals of this structure

- We want a strong separation between the frontend app and the backend language.
  Having a `domain` directory on the top level enables us to do so. Nothing inside of `app` should have any knowledge about anything related to the backend. Example: data model from incoming API data.

- We want a comprehensive approach, which enables us to move fast and the community to contribute.
  Choosing an approach based on documented, established pattern avoids re-inventing the wheel. It also enables us to provide a lot of background references for the community.

- We want to avoid spending too much time on "Which file is where?"
  By having only a `services` directory and no additional `lib` (or `utils`), we reduce the brain load.

## 🗂️ Folder and file naming

- folders are in `kebab-case`
- TypeScript files are in `kebab-case`
- React files are in `PascaleCases`
- Test files are mirroring the code file:
  - `ReactComponent.test.tsx`
  - `typescript-file.test.ts`
- Services files are in `kebab-cases` and include `service` in the file name
  - `useful-function.service.ts`

## 💁 Outline folder structure

```
.
└── src/
    ├── app/
    │   ├── assets/
    │   │   ├── scss/
    │   │   └── images/
    │   ├── components/
    │   │   ├── Form.tsx
    │   │   ├── Form.test.tsx
    │   │   ├── Pagination.tsx
    │   │   └── Pagination.test.tsx
    │   ├── features/
    │   │   ├── overarching-feature/
    │   │   │   ├── feature-one/
    │   │   │   │   ├── components/
    │   │   │   │   ├── hooks/
    │   │   │   │   └── FeatureOne.tsx
    │   │   │   ├── feature-two/
    │   │   │   │   ├── components/
    │   │   │   │   ├── hooks/
    │   │   │   │   ├── utils/
    │   │   │   │   └── FeatureOne.tsx
    │   │   └── topics/
    │   │       ├── components/
    │   │       │   ├── list
    │   │       │   └── select-environment
    │   │       ├── hooks/
    │   │       │   ├── list/
    │   │       │   ├── environment/
    │   │       └── Topics.tsx
    │   ├── layout/
    │   │   ├── LayoutForOneThing.tsx
    │   │   └── LayoutForOneThing.test.tsx
    │   ├── pages/
    │   │   ├── topics/
    │   │   │   └── index.tsx
    │   │   ├── users/
    │   │   │   └── index.tsx
    │   │   └── Login.tsx
    │   └── router.tsx
    ├── domain/
    │   ├── environment/
    │   │   ├── environment-api.ts
    │   │   ├── environment-types.ts
    │   │   └── index.ts
    │   ├── team/
    │   │   ├── team-api.ts
    │   │   ├── team-types.ts
    │   │   └── index.ts
    └── services/
        ├── feature-flags/
        │   ├── utils.test.ts
        │   └── utils.ts.ts
        ├── api.test.ts
        └── api.ts

```

## ℹ️ In-depth explanation

### First level: `app` folder

Contains everything related to the UI application. `app` consumes information from `domain` that we cater to the needs of the UI app.
Nothing in `app` knows anything from the content of `domain` other than the exported modules / interfaces in the `index.ts` (public API).

#### Second level: `app/assets`

Contains assets like scss and images.

#### Second level: `app/components`

Contains all UI elements in form of React components that we use in different places of the app. The components should be clean, and uncomplicated to reuse. They have to be agnostic from any business logic.

As long as we have single component files, they are all located at the root level. When as the number of components grows, we will think about a more detailed structure.

#### Second level: `app/features`

In this directory, we group similar code together based on one feature. The structure enables us to add or remove certain parts of the UI.

- We can separate features into over-arching topics (e.g. all features related to Cluster). This is something we will work out in more detail in the progress.
- We name the folders in `features` based on the feature.
- Every feature folder includes one component on the top level to export. This file is named the same as the features directory.
  -❗This root component should be the only one used in imports outside of this feature.

#### Second level: `app/hooks`

Cross-feature hooks. A hook file may also define and export the React context it consumes (e.g. `useRequestFormClose.ts` exports `RequestModalContext`), so that `features` never import from `layout` where the provider component sits. Global, app-wide providers (e.g. `AuthProvider`) live in `app/context-provider` instead.

#### Second level: `app/pages`

Contains every page of the application. The structure in this folder should mirror the structure of routing. If there is a link to a "dashboard" page in the web app, there should be a `dashboard/index.tsx` page inside `pages`. The files don't need to have a `Page` pre- or postfix since the directory already gives that information.

### First level: `domain` folder

Contains different domains we need to describe the UI application. They are specific to it. They are the concepts you would elaborate on when describing the Klaw UI app.
`domain` is where business logic lives and a layer between e.g. the data from the backend API and `app`. `domain` is
the only place that speaks with the backend and knows what data from the backend looks like.

- We name the folders in `domain` based on the model / concept they are implementing.
- Each `domain` has a main file called `{entity}-api.ts` which holds handler functions for API operations, which are used in the `app` folder
- Every folder includes a `index.ts` file to export modules / interfaces and act as a public API.

#### Naming convention for `domain` handlers

**`POST` - Creating a request: `request{entity}{action noun}`**
Only use `request` as a verb when the API handlers creates a request entity which needs to be approved, with the structure `request{entity}{action}`.

Example: `requestTopicCreation`, `requestConnectorDeletion`, `requestTopicPromotion`

**`POST` - Acting on a created request: `{action verb}{entity}Request`**
Only use `Request` as a noun when the API handlers fires an action against a created request entity, with the structure `{action}{entity}Request`.

Example: `deleteTopicRequest`, `approveTopicRequest`

**`POST` / `GET` - Immediate action: `{action verb}{entity}{optional category}`**
Do not use `request` for immediate actions (no request entity created), but the structure `{action}{entity}{optional category}`.

Example: `updateTopicDocumentation`, `getConnectorOverview`

### First level: `services`

Contains functionality we use in many areas to reduce coupling between different layers. `services` include utility functions as well as facades around external libraries. As a rule of thumb: We should not use a third-part library that is not related to the core packages like react outside of `services`. We implement them in a [Facade Pattern](https://blog.webdevsimplified.com/2022-07/facade-pattern/).

- We name files based on their exported functionality, e.g. `api.ts`.
- We create folders for the same responsibilities, e.g. for `test-utils/`.
