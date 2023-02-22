import { SchemaRequest } from "src/domain/schema-request";
import { Flexbox, Grid, GridItem, StatusChip } from "@aivenio/aquarium";
import MonacoEditor from "@monaco-editor/react";

type DetailsModalContentProps = {
  request?: SchemaRequest;
};

const Label = ({ children }: { children: React.ReactNode }) => (
  <dt className="inline-block mb-2 typography-small-strong text-grey-60">
    {children}
  </dt>
);
function SchemaRequestDetails(props: DetailsModalContentProps) {
  const { request } = props;
  if (!request) return null;
  return (
    <Grid htmlTag={"dl"} cols={"2"} rowGap={"6"}>
      <Flexbox direction={"column"}>
        <Label>Environment</Label>
        <dd>
          <StatusChip text={request.environmentName} />
        </dd>
      </Flexbox>
      <Flexbox direction={"column"}>
        <Label>Topic name</Label>
        <dd>{request.topicname}</dd>
      </Flexbox>

      <GridItem colSpan={"span-2"}>
        <Flexbox direction={"column"}>
          {/*//@TODO add editor preview*/}
          <Label>Schema</Label>
          <dd>
            <MonacoEditor
              data-testid="topic-schema"
              height="300px"
              language="json"
              theme={"light"}
              value={request.schemafull}
              loading={"Loading preview"}
              options={{
                ariaLabel: "Schema preview",
                readOnly: true,
                domReadOnly: true,
                renderControlCharacters: false,
                minimap: { enabled: false },
                folding: false,
                lineNumbers: "off",
                scrollBeyondLastLine: false,
                scrollbar: {
                  vertical: "hidden",
                },
              }}
            />
          </dd>
        </Flexbox>
      </GridItem>

      <GridItem colSpan={"span-2"}>
        <Flexbox direction={"column"}>
          <Label>Message for the approver</Label>
          <dd>{request.remarks || <i>No message</i>}</dd>
        </Flexbox>
      </GridItem>

      <Flexbox direction={"column"}>
        <Label>Requested by</Label>
        <dd>{request.username}</dd>
      </Flexbox>
      <Flexbox direction={"column"}>
        <Label>Requested on</Label>
        <dd>{request.requesttimestring} UTC</dd>
      </Flexbox>
    </Grid>
  );
}

export { SchemaRequestDetails };
