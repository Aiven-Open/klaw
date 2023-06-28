import { Light as SyntaxHighlighter } from "react-syntax-highlighter";
import { a11yLight } from "react-syntax-highlighter/dist/esm/styles/hljs";
import { Box } from "@aivenio/aquarium";
import classes from "src/app/features/topics/details/documentation/components/documentation-editor.module.css";
import { Dispatch, SetStateAction } from "react";

type DocumentationEditorProps = {
  documentation: string;
  onChangeDocumentation: Dispatch<SetStateAction<string>>;
};

function DocumentationEditor({
  documentation,
  onChangeDocumentation,
}: DocumentationEditorProps) {
  return (
    <div className={classes.markdownEditor}>
      <Box
        borderColor={"grey-20"}
        borderWidth={"1px"}
        borderRadius={"2px"}
        className={classes.markdownSyntaxHighlight}
      >
        <textarea
          value={documentation}
          onChange={(event) => onChangeDocumentation(event?.target?.value)}
          className={classes.markdownTextarea}
        />
        <SyntaxHighlighter
          language={"markdown"}
          PreTag="div"
          style={a11yLight}
          wrapLongLines={true}
        >
          {documentation}
        </SyntaxHighlighter>
      </Box>
    </div>
  );
}

export { DocumentationEditor };
