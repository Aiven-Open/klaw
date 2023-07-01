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

// `createMarkdown` is used to transform documentation on the api response
//  for example on TopicOverview. The `topicDocumentation` is a html string
//  that we transform to markdown.
async function createMarkdown(stringifiedHtml: string) {
  // see: https://unifiedjs.com/learn/recipe/remark-html/#how-to-turn-html-into-markdown
  // we're also using rehypeSanitize to sanitize the HTML
  if (unified) {
    const result = await unified()
      .use(rehypeParse)
      .use(rehypeSanitize)
      .use(rehypeRemark)
      .use(remarkStringify)
      .process(stringifiedHtml);
    return String(result) as MarkdownStringBrand;
  } else {
    return "" as MarkdownStringBrand;
  }
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
    const result = await unified()
      .use(remarkParse)
      .use(remarkRehype)
      .use(rehypeSanitize)
      .use(rehypeStringify)
      .process(markdownString);

    return String(result) as StringifiedHtmlBrand;
  } else {
    return "" as StringifiedHtmlBrand;
  }
}

export { createMarkdown, createStringifiedHtml };
export type { MarkdownString, StringifiedHtml };
