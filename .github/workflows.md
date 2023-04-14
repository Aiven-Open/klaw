# Overview

This document explains how and why we organize our workflows for a CI strategy using GitHub actions.

## Directories and file structure

Our directory structure is as follows:

- 📁 `/actions` contains reusable actions needed in workflows
- 📁 `/workflows` contains GitHub workflow files
- 📁 `/ISSUES_TEMPLATE` contains  templates for our issues

We have two types of files in the `/workflows` directory:
-  📄 starting with `job_` are reusable jobs that are used in different workflows.
  - they should (and can't) be used in isolation, as they don't take care of the code being checked out
-  📄 starting with `workflow_` indicate a collection of jobs that are automatically triggered by certain events or actions

## Pipelines 

We have two types of workflows triggered by Pull Requests:

1. When a Pull Request is opened, we run selected jobs based on the location and changes made in the code. We have two sets of jobs:
   - if changes are made inside the `/coral` directory, we run all [coral jobs](./workflows/jobs-coral.yaml)
   - if are made outside the `/coral` directory, we run all [maven jobs](./workflows/jobs-maven.yaml). This jobs are only run when the Pull Request is ready for review, excluding "Draft" PRs.
   
2. When a Pull Request is approved, the [`workflow-merge-to-main`](./workflows/workflow-merge-to-main.yaml) is triggered.
   - This workflow runs all [coral jobs](./workflows/jobs-coral.yaml) and [maven jobs](./workflows/jobs-maven.yaml)
   - After they are successful, it runs a job called `merge-to-main`. This job is defined in the [Branch protection rule](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/defining-the-mergeability-of-pull-requests/managing-a-branch-protection-rule)  for `main` and is and is necessary to enable merging on our default branch.

In addition, we have a workflow that is triggered when changes are made to the `openapi.yaml file`. We  check whether the changes affect the TypeScript file in `/coral`. If the changes do affect the TypeScript file, the `api.d.ts` file is automatically generated, and the TypeScript compiler is run. If the TypeScript compiler finds no errors, the file is committed. Otherwise the job fails.

## Drawbacks

We have identified two potential drawbacks with this strategy:

1. There will be a delay between approving a Pull Request and merging it because of the required job on approval. We can mitigate this in the future:
   - by adding automatic notification about a PR being ready to merge. 
   - consider auto-merging it in the future. Currently, we prefer manual merges to maintain more control over the process. 

2. Depending on the workflow, some job runs may be redundant and increase the time between giving a Pull Request for review and merging it.
  - We plan to iterate over our workflows to reduce redundancy and improve efficiency and will pay attention if this slows us down,
  - We are using the `merge-to-main` workflow as a temporary solution until we move to GitHub merge queues, when they become mores stable.


## Background of this strategy

Our goals for this CI strategy are to provide a good developer experience, ensure high confidence in our code, and support contributors by providing a smooth experience and ownership.

To achieve these goals, we use workflows adapted to our needs to provide timely feedback to developers by running only the necessary jobs based on the changes made in the code. We also use workflows to ensure that code changes are properly tested and reviewed before merging them into main. We also have specialized workflows to support our processes, such as automating validation of TypeScript types against openapi.yaml.

We protect `main` at all costs and threat it as a deployed production environment.

### Why "Protect `main` at all costs? 

We believe it is essential to ensure that main is always releasable. 

Users that download Klaw from `main` can have the confidence that the code will work as expected.


It also fosters an open-source community where outside contributors are welcome. Contributors will branch from `main` to make a Pull Request. So we want to make sure they always start working in a stable state.

Allowing a broken `main` branch may lead to conflicts. If a PR introduces an issue on `main`, there's a high changes the fix will be introduces by core contributors, because we have a faster communication lane. We wouldn't want the default branch to stay in a broken state longer then necessary and prioritize the fix.

Our strong protection avoids this situation and conflict. We want to ensure contributors to feel and take ownership of their changes right up until their code going 🚢 to "production". 






