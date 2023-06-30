import ReactMarkdown from "react-markdown";
import { Box } from "@aivenio/aquarium";
import classes from "src/app/components/documentation/documentation-view.module.css";

type DocumentationViewProps = {
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
