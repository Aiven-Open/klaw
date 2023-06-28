import { unified } from "unified";
import rehypeParse from "rehype-parse";
import rehypeSanitize from "rehype-sanitize";
import rehypeRemark from "rehype-remark";
import remarkStringify from "remark-stringify";

async function createMarkdown(stringifiedHtml: string) {
  const result = await unified()
    .use(rehypeParse)
    .use(rehypeSanitize)
    .use(rehypeRemark)
    .use(remarkStringify)
    .process(stringifiedHtml);
  return String(result);
}

export { createMarkdown };
