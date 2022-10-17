# Notes about documentation

**TL;DR**
There are many things we may need to discuss at some point in the future, but are not relevant (so much) right now. One things we should start right away is think about and adding [low-level documentation](#Thoughts-about-low-level-documentation) about our code base. We can do that by adding markdown files close to the code (where we need them). This is a low effort start and we can iterate over that approach later, if we need to. 

## What we would need to do right now

First things first - what kind of work would this add for us:

- Check if there is available tooling to automate a few things better (I'll do that)
- Add things we feel we want to document about the code in markdown files in the repo
  - Examples could be: 
    - Naming approaches 
    - Directory structures
    - List of imporant part of tech stack with links to official documentation

That's it for now! We can iterate over that in the process and see where we want to make tweaks.


## Users and use cases for documenation

- external companies and developer
    - they use klaw as a product
        - intros and guides how to run it, deploy it etc.
- consumers of the API
    - external d/c that only use the API, not the FE
    - developer (internally and from the community) working on the FE (bc it's the API the frontend consumes!)
- developers (internally and from the community) working on the repo
    - in code documentation (low level user)


## Thoughts about low-level documentation

### Goals
- enable developers (internally and externally) to gain understanding about context and approaches by being explicit about it
- enable developers (internally and externally) to contribute with a more ease by reducing the amount of implicit knowledge you often need to move comfortably in an unknown code base
- this kind of documentation should be markdown, because it's known standard and it enables us later to use it for e.g. extracting content into a documentation site (it also enables us to start right away and establish a workflow from the beginning)
- markdown files should be as close to the code as possible
- add a short note / guideline on documenting comments and docs to a contribution


### Actionable things we can do
- have a guide how to contribute
- add documentation in the code review processes (e.g. in a definition of done etc)
- naming matters! meaningful variable names etc. are already documenting a lot of things (+ using meaningful names for that in documentation, too)
- well written tests are a good way to document code, so strucutring and describing (in FE tests) tests well will help a lot
- think about comments as useful tool to add more **context** to certain code parts
- identify areas where more in-depth documentation enables users to contribute


## Additonal
- a documentation (ha!) on "How and why we document" code should be part of the documentation
- this should include a common understanding of language we use in docs:
    - catch and remove condescending, inconsiderate or insensitive language
    - have a definition of what voice we use (active voice etc.)
- establish tooling to help us write documentation (e.g. alexjs, hemmingway.app, grammarly etc)
- check what can be automated, e.g linting on broken links, maybe linting for trademarks etc.


## List of possible tooling

### Automated
- `markdown-link-check` with script for pre-commit hook (can also do external links, but CI need more access to that)
- check https://earthly.dev/blog/markdown-lint/


### Manual checks
- write-good linter
- AlexJS linter
- Hemingway Editor

### Useful links for later
https://www.writethedocs.org/guide/docs-as-code/
