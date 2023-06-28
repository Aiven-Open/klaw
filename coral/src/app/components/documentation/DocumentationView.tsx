import ReactMarkdown from "react-markdown";
import { Box } from "@aivenio/aquarium";
import classes from "src/app/features/topics/details/documentation/components/documentation-view.module.css";

type DocumentationViewProps = {
  //⚠️ This component assumes that we're
  // handling markdown that is properly
  // sanitized
  markdownString: string;
};

function DocumentationView({ markdownString }: DocumentationViewProps) {
  return (
    <Box component={"article"} paddingTop={"l2"}>
      <ReactMarkdown className={classes.reactMarkdown}>
        {markdownString}
      </ReactMarkdown>
    </Box>
  );
}

export { DocumentationView };
