import { useState, useEffect } from "react";
import { createMarkdown } from "src/app/components/documentation/utils/topic-documentation-helper";

const useDocumentation = (
  stringifiedHtml: string | undefined
): {
  topicDocumentationMarkdownString: string | undefined;
} => {
  const [topicDocumentation, setTopicDocumentation] = useState<
    string | undefined
  >(undefined);

  useEffect(() => {
    // It would be more clear and responsibilities better split
    // to do that on API level, so outside from 'domain' we don't
    // even know that we handle stringified html, but that would
    // require to do that on all topicOverview entries and is
    // unnecessary load for user
    if (stringifiedHtml !== undefined && stringifiedHtml.trim().length > 0) {
      const docToTransform = stringifiedHtml;
      const transformDocumentationString = async () => {
        const documentation = await createMarkdown(docToTransform);
        setTopicDocumentation(documentation);
      };
      transformDocumentationString();
    }
  }, [stringifiedHtml]);
  return { topicDocumentationMarkdownString: topicDocumentation };
};

export { useDocumentation };
