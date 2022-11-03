# Directory structure

The structure is inspired in big parts by:

- [How To Structure React Projects From Beginner To Advanced](https://blog.webdevsimplified.com/2022-07/react-folder-structure/). See the "Advanced Folder Structure".
- [Elegant Frontend Architecture](https://michalzalecki.com/elegant-frontend-architecture/).

## 🏁️ Primary goals of this structure

- We want a strong separation between the frontend app and the backend language.
  Having a `domain` directory on top level enables us to do so. Nothing inside of `app` should have any knowledge about anything related to backend. Example: data model from incoming API data.

- We want a comprehensive approach, which enables us to move fast and the community to contribute.
  Choosing an approach based on documented, established pattern avoids re-inventing the wheel. It also enables us to provide a lot of background references for the community.

- We want to avoid spending too much time on "Which file is where?"
  By having only a `services` directory and not additional `lib` (or `utils`), we reduce the brain load for this.

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
.
└── src/
    ├── app/
    │   ├── assets/
    │   │   ├── scss
    │   │   ├── images
    │   │   └── ...
    │   ├── components/
    │   │   ├── ComponentOne.tsx
    │   │   ├── ComponentOne.test.tsx
    │   │   ├── ComponentTwo.tsx
    │   │   ├── ComponentTwo.test.tsx
    │   │   └── index.ts
    │   ├── features/
    │   │   ├── overarching-topic/
    │   │   │   └── feature-one/
    │   │   │       ├── components
    │   │   │       ├── hooks
    │   │   │       ├── ...
    │   │   │       └── index.ts
    │   │   └── feature-two/
    │   │       ├── components
    │   │       ├── hooks
    │   │       ├── ...
    │   │       └── index.ts
    │   ├── hooks/
    │   │   ├── some-general-hook
    │   │   └── index.ts
    │   ├── layout/
    │   │   ├── LayoutForOneThing.tsx
    │   │   ├── LayoutForOneThing.test.tsx
    │   │   ├── ...
    │   │   ├── index.ts
    │   │── pages/
    │   │   ├── index.tsx
    │   │   ├── Login.tsx
    │   │   ├── Users.tsx
    │   │   └── ...
    │   └── router.tsx
    ├── domain/
    │   ├── name-of-domain-one/
    │   │   ├── api
    │   │   ├── types
    │   │   └── index.ts
    │   └── name-of-domain-two/
    │       ├── api
    │       ├── types
    │       └── index.ts
    └── services /
        ├── http-client/
        │   ├── some-file.tsx
        │   ├── some-file.test.tsx
        │   ├── another-file.tsx
        │   ├── another-file.test.tsx
        │   └── index.ts
        ├── string-formatting/
        │   └── some-file.tsx
        │   └── some-file.test.tsx
        └── index.ts

```

## ℹ️ In-depth explanation

## First level: `app` folder

Contains everything related to the UI application. `app` consumes information from `domain` that we cater for the needs of the UI app.
Nothing in `app` knows anything from content of `domain` other than the exported modules / interfaces in the `index.ts` (public API).

## First level: `domain` folder

Contains different domains wee need to describe the UI application. They are specific to it. They are the concepts you would elaborate on when describing the Klaw UI app.
`domain` is where business logic lives and a layer between e.g. the data from the backend api and `app`. `domain` is the only place that speaks with the backend and knows how data from the backend looks like.

- we name folder in `domain` based on the model / concept they are implementing
- every includes a `index.ts` file to export modules / interfaces and act as an public API.

## Second level: `app/assets`

Contains assets like scss and images.

#### `components`

Contains all UI elements in form of React components that we use in different places of the app. The components should be clean, and uncomplicated to reuse. They have to be agnostic from any business logic.

- top level `index.ts` file to export components and act as an public API.

## Second level: `app/features`

In this directory, we group similar code together based on one feature. The structure enables us to add or remove certain parts of the UI .

- We can separate features into over-arching topics (e.g. all features related to Cluster). This is something we will work out in more detail in the progress.
- We name folder in `features` based on the feature.
- Every feature folder includes a `index.ts` on top level to export modules / interfaces and act as an public API.

#### `pages`

Contains every page of the application, one file per page. The structure in this folder should mirror the structure of the web apps views and routing. If there is a link to a "dashboard" page in the web app, there should be a `Dashboard` page inside `pages`. The files don't need to have a `Page` pre- or postfix since the directory already gives that information.  

#### `services`

Contains functionality we use in many areas to reduce coupling between different layers. `services1 include utility functions as well as facades around external libraries. As a rule of thumb: We should not use a third-part library that is not related to the core packages like react outsite of `services`. We implement them in a [Facade Pattern](https://blog.webdevsimplified.com/2022-07/facade-pattern/).
