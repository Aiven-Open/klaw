import { Flexbox, Grid, GridItem, StatusChip } from "@aivenio/aquarium";
import MonacoEditor from "@monaco-editor/react";
import { ConnectorRequest } from "src/domain/connector";

type DetailsModalContentProps = {
  request?: ConnectorRequest;
};

const Label = ({ children }: { children: React.ReactNode }) => (
  <dt className="inline-block mb-2 typography-small-strong text-grey-60">
    {children}
  </dt>
);

function ConnectorRequestDetails(props: DetailsModalContentProps) {
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
        <Label>Connector name</Label>
        <dd>{request.connectorName}</dd>
      </Flexbox>

      <GridItem colSpan={"span-2"}>
        <Label>Connector description</Label>
        <dd>{request.description}</dd>
      </GridItem>
      <GridItem colSpan={"span-2"}>
        <Flexbox direction={"column"}>
          <Label>Connector configuration</Label>
          <dd>
            <MonacoEditor
              data-testid="kafka-connector"
              height="250px"
              language="json"
              theme={"light"}
              value={request.connectorConfig}
              loading={"Loading preview"}
              options={{
                ariaLabel: "Connector configuration",
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
        </Flexbox>
      </GridItem>

      <GridItem colSpan={"span-2"}>
        <Flexbox direction={"column"}>
          <Label>Message for approval</Label>
          <dd>{request.remarks || <i>No message</i>}</dd>
        </Flexbox>
      </GridItem>

      <Flexbox direction={"column"}>
        <Label>Requested by</Label>
        <dd>{request.requestor}</dd>
      </Flexbox>
      <Flexbox direction={"column"}>
        <Label>Requested on</Label>
        <dd>{request.requesttimestring} UTC</dd>
      </Flexbox>
    </Grid>
  );
}

export { ConnectorRequestDetails };
