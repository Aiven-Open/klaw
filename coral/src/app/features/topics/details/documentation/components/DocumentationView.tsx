import ReactMarkdown from "react-markdown";
import rehypeRaw from "rehype-raw";
import { Box } from "@aivenio/aquarium";
import classes from "src/app/features/topics/details/documentation/components/documentation-view.module.css";

type DocumentationViewProps = {
  //⚠️ This component can only be used to
  // show stringified html our own backend
  // provides. We trust this source (only!)
  // to deliver sanitized, stringified html
  stringifiedHtml: string;
};

function DocumentationView({ stringifiedHtml }: DocumentationViewProps) {
  return (
    <Box
      component={"article"}
      padding={"l2"}
      borderColor={"grey-10"}
      borderWidth={"1px"}
      borderRadius={"4px"}
    >
      <ReactMarkdown
        className={classes.markdownEditor}
        rehypePlugins={[rehypeRaw]}
      >
        {stringifiedHtml}
      </ReactMarkdown>
    </Box>
  );
}

export { DocumentationView };
