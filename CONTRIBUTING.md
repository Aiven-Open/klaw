# Contributing Guidelines

🎉 First off: Thank you for your interest in contributing to our project 🥳

Whether it's a bug report, new feature, correction, or additional documentation, we greatly value feedback and contributions from our community. GitHub Open Source guides have a great source about ways to [How to Contribute to Open Source](https://opensource.guide/how-to-contribute/).

Please read through this document before submitting any issues or pull requests. This ensures all parties have all the necessary information to respond to your bug report or contribution.

## Content

- [❤️ Code of Conduct](#-code-of-conduct)
- [Opening an issue](#opening-an-issue)
- [How to work on an issue](#how-to-work-on-an-issue)
- [How to make a pull request](#how-to-make-a-pull-request)
  - [Developer Certificate of Origin](#developer-certificate-of-origin)
  - [When is your pull request ready to be merged?](#-when-is-your-pull-request-ready-to-be-merged)
- [How to merge a pull request](#how-to-merge-a-pull-request)
- [Guideline commit messages](#guideline-commit-messages)
  - [ℹ️ Semantic prefixes for commit messages](#-semantic-prefixes-for-commit-messages)
  - [✍️ Writing a great commit message](#-writing-a-great-commit-message)

## ❤️ Code of Conduct

This project has adopted the [Contributor Covenant Code of Conduct](CODE_OF_CONDUCT.md). Before contributing, please take the time to read our COC. Everyone participating is expected to uphold this code. Please report unacceptable behavior to us!

For more information see the [Code of Conduct FAQ](https://www.contributor-covenant.org/faq/).


## Opening an issue
You should open an issue when you
- checked that there is no issue open already related to your topic
- want to report an error that you can't resolve by yourself
- want to propose a new feature
- want to discuss an idea to improve a higher-level topic, for example about community, documentation

__What information does an issue need?__
The more information an issue includes, the better! For example:

- If it's a bug, describe in detail how to reproduce it. You can add screenshots or screen recordings for visual bugs.
- If it's a feature, create a user story. What problem does that feature solve? What value does it add?


## How to work on an issue

- Comment on the issue to inform that you will work on it.
- If you have the rights: set yourself as an assignee and add the __in progress__ label.
- [Fork](https://docs.github.com/en/get-started/quickstart/fork-a-repo) the Klaw repository.
- On your fork, [create a branch](https://docs.github.com/en/desktop/contributing-and-collaborating-using-github-desktop/making-changes-in-a-branch/managing-branches#creating-a-branch) named after the issue you're working on.
- Make all of your changes 🧑‍💻
- For your commits, please see our [Guideline commit messages](#guideline-commit-messages).
- Prefer making small and self-contained commits. It helps doing reviews. 
- Check if it would be helpful to update documentation related to your change. If yes, please do so 🤗!
- Make sure you add tests for your changes.
- If you fix a bug, add a regression test.

- When you're finished and pushed all your changes to GitHub, you're ready to create a pull request 🎉


## How to make a pull request

GitHub has detailed documentation on [how to create a PR](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/proposing-changes-to-your-work-with-pull-requests/creating-a-pull-request-from-a-fork) if you want to have more information. If you're not sure what to do, please feel free to reach out to our community!

Be conscious of the scope of one PR. Sometimes it is tempting to get sidetracked for example by refactoring some unrelated code. We've all been there! But this should be a separate PR. Only add changes to your PR related and contribute to the issue you're solving.

### Developer Certificate of Origin

Klaw project requires contributors to adhere to the [Developer Certificate of Origin](https://developercertificate.org/), this means you must sign off your commits. We automatically check that all commit messages contain the `Signed-off-by:` line with your name and email.

#### How to sign off commits

To sign off commits you need to add the `-s` (or `--signoff`) flag to your commit command. For example:

```
git commit -s -m"docs: Update contribution guide with DCO guidelines"
```

#### How to fix not signed off commits after the fact

If you opened a Pull Request but not all commits were signed off, no worries, it can be easily fixed. If you had only one commit, go and amend it like follows:

```
git commit --amend --signoff

```

In case of having multiple commits, you can rebase and sign off, the following command with sign off the last 3 commits:

```
git rebase --signoff HEAD~3
```

Once you are done, you need to force push your changes. We recommend using `--force-with-lease` when force pushing:

```
git push --force-with-lease origin/your-branch-name
```

### ✅ When is your pull request ready to be merged?

Every pull request has to be reviewed before merging. At least one maintainer needs to approve it. This is meant to assure the quality of the product. It is also a great tool to give and receive feedback in the community and learn from each other.

Your pull request should meet the following criteria to be ready for review:

__1. Every PR has to be releasable__

- Every PR that is merged on `main` should be treated like we release it into production right away.
- The `main` branch should always be in a state where it can be deployed and used right away.

__2. A clear goal with a small scope__

- Make small and incremental PRs.
- The code changes relate to one specific topic.
- Every PR should have one specific goal (and if you add that goal in your description - all the better).
- Rather do multiple smaller PRs than one big one! PRs with a lot of changes are difficult to review. It's also more demanding to give constructive feedback to them.

__3. Add meaningful information__

- A descriptive title and detailed description of your changes help the reviewer gaining context.
- Include links to relevant issues.
- If you followed a recommended approach e.g. from an article, link it in the PR.
- Especially for UI changes it can be helpful to add screenshots or recordings of your changes
- If you have questions, don't hesitate to add them!

__4. Keep a clear git history in mind__

- If you do multiple commits, try to make every commit cover the scope of your changes.
- Your commit messages should follow our [guideline](#guideline-commit-messages).
- If you add changes after a review, don't force push in your existing PR, but add new commits. That way, reviewers can pick up the review again.

## How to merge a pull request

Pull request are merged by the maintainer that approved after review. An pull request author should never merge themself, even if they are maintainer. 

### Squash and merge

While working on a change, making small commits related to specific changes is a good practice. The commit history helps describe the process of building. It helps reviewers doing a good job! But they can clutter the Git history on the main branch a lot.

This is why we use "squash and merge" as merge method. When merging, the small commits are combined into one. It creates a cleaner merge history. It also makes it more straight forward to revert all part of the changes, if that is needed. 

The commit message for the pull request can be changed when merging. We recommend updating the automatically created text for this commit to create a meaningful squash commit message. They should follow our [Guideline commit messages](#guideline-commit-messages). 

## Guideline commit messages

We use the [Conventional Commits](https://www.conventionalcommits.org/) specification. It helps to create a meaningful and explicit git history.

### ℹ️ Semantic prefixes for commit messages

- **fix**: a commit that patches a bug.  
  Example: `fix: Removes circular dependency`

- **feat**: a commit that introduces a new feature.
  Example: `feat: Add sorting to user list`

- **docs**: a commit that changes something related to documentation.
  Example: `docs: Update contribution guide with guidelines for commit messages`

- **refactor**: a commit that refactors existing code.
  Example: `refactor: Update footer from kafkawize.io to klaw-project.io`


### ✍️ Writing a great commit message

A "great" commit message enables others to gain more context about a code change. While the `diff` is telling you **what** has changed, the commit message can tell you **why** it has changed.

For more information read this article: [How to Write a Git Commit Message](https://cbea.ms/git-commit/). We used it as a base for our rules.

```
<prefix>: <description>

[optional body]

```

#### 1. Add a short description as the first line
The first line (`<description>`) should be a short description of your change. Limit it to preferably 50 characters. It never should be longer than 72 characters.

**⛔️ Don't**

`Add CONTRIBUTING.md with first information about Code Of Conduct and a guideline for commit messages that includes our first rules and pattern we want to establish`

**️✅ Do**

`Add the first iteration for contribution guide`

#### 2. Use the "imperative mood" in the first line
"Imperative mood" means you form a sentence as if you were giving a command. You can image your commit message completing the sentence _"If applied, this commit will... <do your change>"_.

**⛔️ Don't**
`fix: Removed the newline that caused a linting error`
-> _"If applied, this commit will_ removed the newline that caused a linting error"

**️✅ Do**
`fix: Remove the newline that caused a linting error`
-> _"If applied, this commit will_ remove the newline that caused a linting error"


#### 3. Separate your description with a new line from the body
If you add a body, add an empty line between your description to separate it from the body. This makes the message more readable. It also makes `git log --oneline` or `git shortlog` more usable.


#### 4. Use an optional body to explain why not how.
You don't need to explain the code! The commit message has a changeset that contains this information. Use the body to explain _why_ you made a change. Not every commit needs to have a body. Often the code change itself is explanation enough.

**⛔️ Don't**
```
fix: Fix typo

Change "optoinal" "to optional"
```


**️✅ Do**
```fix: Remove word

Remove "just" from the description, because it can make people feel inadequate. 
```

#### 5. Wrap your body at 72 characters
Git does not wrap text, so you have to take care of margins. Editors and IDEs can help with that.
