import { unified } from "unified";
import rehypeParse from "rehype-parse";
import rehypeSanitize from "rehype-sanitize";
import rehypeRemark from "rehype-remark";
import remarkStringify from "remark-stringify";
import remarkParse from "remark-parse";
import remarkRehype from "remark-rehype";
import rehypeStringify from "rehype-stringify";

async function createMarkdown(stringifiedHtml: string) {
  console.log("creat markdown");
  if (unified) {
    const result = await unified()
      .use(rehypeParse)
      .use(rehypeSanitize)
      .use(rehypeRemark)
      .use(remarkStringify)
      .process(stringifiedHtml);
    return String(result);
  } else {
    return "";
  }
}

async function createStringifiedHtml(markdownString: string) {
  if (unified) {
    const result = await unified()
      .use(remarkParse)
      .use(remarkRehype)
      .use(rehypeSanitize)
      .use(rehypeStringify)
      .process(markdownString);

    return String(result);
  } else {
    return "";
  }
}

export { createMarkdown, createStringifiedHtml };
