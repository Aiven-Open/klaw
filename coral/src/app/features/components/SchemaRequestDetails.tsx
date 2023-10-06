import { SchemaRequest } from "src/domain/schema-request";
import { Box, Grid, GridItem, StatusChip } from "@aivenio/aquarium";
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
      <Box.Flex direction={"column"}>
        <Label>Environment</Label>
        <dd>
          <StatusChip text={request.environmentName} />
        </dd>
      </Box.Flex>
      <Box.Flex direction={"column"}>
        <Label>Topic name</Label>
        <dd>{request.topicname}</dd>
      </Box.Flex>

      <GridItem colSpan={"span-2"}>
        <Label>Schema version</Label>
        <dd>{request.schemaversion}</dd>
      </GridItem>
      <GridItem colSpan={"span-2"}>
        <Box.Flex direction={"column"}>
          <Label>Schema preview</Label>
          <dd>
            <MonacoEditor
              data-testid="topic-schema"
              height="250px"
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
              }}
            />
          </dd>
        </Box.Flex>
      </GridItem>

      <GridItem colSpan={"span-2"}>
        <Box.Flex direction={"column"}>
          <Label>Message for approval</Label>
          <dd>{request.remarks || <i>No message</i>}</dd>
        </Box.Flex>
      </GridItem>

      <Box.Flex direction={"column"}>
        <Label>Requested by</Label>
        <dd>{request.requestor}</dd>
      </Box.Flex>
      <Box.Flex direction={"column"}>
        <Label>Requested on</Label>
        <dd>{request.requesttimestring} UTC</dd>
      </Box.Flex>
    </Grid>
  );
}

export { SchemaRequestDetails };
