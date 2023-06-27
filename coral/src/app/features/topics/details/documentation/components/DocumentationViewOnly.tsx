import ReactMarkdown from "react-markdown";
import rehypeRaw from "rehype-raw";
import { Box } from "@aivenio/aquarium";
import classes from "src/app/features/topics/details/documentation/components/documentation-view.module.css";

type DocumentationViewProps = {
  //⚠️ This component can only be used to
  // show stringified html our own backend
  // provides. We trust this source (only!)
  // to deliver sanitized, stringified html
  // This is why we don't use it for preview.
  stringifiedHtml: string;
};

function DocumentationViewOnly({ stringifiedHtml }: DocumentationViewProps) {
  return (
    <Box component={"article"} paddingTop={"l2"}>
      <ReactMarkdown
        className={classes.reactMarkdown}
        rehypePlugins={[rehypeRaw]}
      >
        {stringifiedHtml}
      </ReactMarkdown>
    </Box>
  );
}

export { DocumentationViewOnly };
