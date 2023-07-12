import { unified } from "unified";
import rehypeParse from "rehype-parse";
import rehypeSanitize from "rehype-sanitize";
import rehypeRemark from "rehype-remark";
import remarkStringify from "remark-stringify";
import remarkParse from "remark-parse";
import remarkRehype from "remark-rehype";
import rehypeStringify from "rehype-stringify";

/**
 *  Our documentation is saved as stringified html at the moment.
 *  To provide backwards compatibility (including with our Angular
 *  app), we will keep this format as the saved on in coral.
 *  We still want to offer a markdown-only editor for adding/editing
 *  documentation in coral and keep the option to also use that as
 *  saved format later. That's why we need to handle the transformation
 *  between markdown - html on `domain` level. Outsidt of the domain
 *  directory, there's no information about stringified html.
 */

// The type MarkdownString does provide us a bit of type safety against slips,
// in case someone tries to pass a (not properly sanitized) string
// to DocumentView
type MarkdownStringBrand = string & {
  readonly __brand: unique symbol;
};

type MarkdownString = Awaited<ReturnType<typeof createMarkdown>>;

// very unique string to be able to identify documentation
// transformation errors that otherwise can't be determined
const documentationTransformError =
  "6e6c85af-71b4-4021-b7fe-bf99c81f2c6aZ-d3841163-ba74-4db4-b874-35b973d2a80e-32e866bd-99d0-4a64-ac15-5b73104ab0ff";
// `createMarkdown` is used to transform documentation on the api response
//  for example on TopicOverview. The `topicDocumentation` is a html string
//  that we transform to markdown.
async function createMarkdown(stringifiedHtml: string) {
  // see: https://unifiedjs.com/learn/recipe/remark-html/#how-to-turn-html-into-markdown
  // we're also using rehypeSanitize to sanitize the HTML
  if (unified) {
    try {
      const result = await unified()
        .use(rehypeParse)
        .use(rehypeSanitize)
        .use(rehypeRemark)
        .use(remarkStringify)
        .process(stringifiedHtml);
      return String(result) as MarkdownStringBrand;
    } catch (error) {
      console.error(error);
      return documentationTransformError as MarkdownStringBrand;
    }
  } else {
    return "" as MarkdownStringBrand;
  }
}

function documentationTransformationError(documentation: MarkdownStringBrand) {
  return documentation === documentationTransformError;
}
// The type MarkdownString does provide us a bit of type safety against slips,
// in case someone tries to pass a (not properly sanitized) html string
// to updateTopicDocumentation
type StringifiedHtmlBrand = string & {
  readonly __brand: unique symbol;
};

type StringifiedHtml = Awaited<ReturnType<typeof createStringifiedHtml>>;

// `createStringifiedHtml` is used to transform documentation from user input
//  for example from the topic documentation
//  the api function gets the input as markdown string, and we transform it
//  before sending it to the endpoint
async function createStringifiedHtml(markdownString: string) {
  if (unified) {
    // see: https://unifiedjs.com/learn/recipe/remark-html/#how-to-turn-markdown-into-html
    // we're also using rehypeSanitize to sanitize the HTML
    try {
      const result = await unified()
        .use(remarkParse)
        .use(remarkRehype)
        .use(rehypeSanitize)
        .use(rehypeStringify)
        .process(markdownString);

      return String(result) as StringifiedHtmlBrand;
    } catch (error) {
      throw Error(
        "Something went wrong while transforming the documentation into the right format to save."
      );
    }
  } else {
    return "" as StringifiedHtmlBrand;
  }
}

export {
  createMarkdown,
  createStringifiedHtml,
  documentationTransformationError,
};
export type { MarkdownString, StringifiedHtml };
