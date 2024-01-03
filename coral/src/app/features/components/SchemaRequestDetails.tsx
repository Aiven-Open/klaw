import { SchemaRequest } from "src/domain/schema-request";
import { Grid, StatusChip, Typography } from "@aivenio/aquarium";
import MonacoEditor from "@monaco-editor/react";

type DetailsModalContentProps = {
  request?: SchemaRequest;
};

const Label = ({ children }: { children: React.ReactNode }) => {
  return (
    <dt className="inline-block mb-2 typography-small-strong text-grey-60">
      {children}
    </dt>
  );
};

function SchemaRequestDetails(props: DetailsModalContentProps) {
  const { request } = props;
  if (!request) return null;
  return (
    <Grid htmlTag={"dl"} cols={"2"} rowGap={"6"}>
      <Grid.Item>
        <Label>Environment</Label>
        <dd>
          <StatusChip text={request.environmentName} />
        </dd>
      </Grid.Item>
      <Grid.Item>
        <Label>Topic name</Label>
        <dd>{request.topicname}</dd>
      </Grid.Item>

      <Grid.Item xs={2}>
        <Label>Schema version</Label>
        <dd>{request.schemaversion}</dd>
      </Grid.Item>
      {request.forceRegister && (
        <Grid.Item xs={2}>
          <Label>Force register applied</Label>
          <dd>
            {" "}
            <Typography.Small>
              Warning: This schema is being force registered. This will override
              standard validation process of the schema registry.{" "}
              <a
                href={
                  "https://www.klaw-project.io/docs/HowTo/schemas/Promote-a-schema/#how-does-force-register-work"
                }
              >
                Learn more
              </a>
            </Typography.Small>
          </dd>
        </Grid.Item>
      )}

      <Grid.Item xs={2}>
        <Grid.Item>
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
        </Grid.Item>
      </Grid.Item>

      <Grid.Item xs={2}>
        <Grid.Item>
          <Label>Message for approval</Label>
          <dd>{request.remarks || <i>No message</i>}</dd>
        </Grid.Item>
      </Grid.Item>

      <Grid.Item>
        <Label>Requested by</Label>
        <dd>{request.requestor}</dd>
      </Grid.Item>
      <Grid.Item>
        <Label>Requested on</Label>
        <dd>{request.requesttimestring} UTC</dd>
      </Grid.Item>
    </Grid>
  );
}

export { SchemaRequestDetails };
