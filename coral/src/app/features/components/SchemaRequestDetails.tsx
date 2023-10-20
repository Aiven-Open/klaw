import { SchemaRequest } from "src/domain/schema-request";
import {
  Grid,
  GridItem,
  InlineIcon,
  StatusChip,
  Typography,
} from "@aivenio/aquarium";
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
      <GridItem>
        <Label>Environment</Label>
        <dd>
          <StatusChip text={request.environmentName} />
        </dd>
      </GridItem>
      <GridItem>
        <Label>Topic name</Label>
        <dd>{request.topicname}</dd>
      </GridItem>

      <GridItem>
        <Label>Schema version</Label>
        <dd>{request.schemaversion}</dd>
      </GridItem>
      {request.forceRegister && (
        <GridItem>
          <Label>Force register</Label>
          <dd>
            {" "}
            <Typography.Small>
              Warning: This schema is being force registered. This will override
              standard validation process of the schema registry.{" "}
              <a
                target="_blank"
                rel="noreferrer"
                href={
                  "https://www.klaw-project.io/docs/HowTo/schemas/Promote-a-schema/#how-does-force-register-work"
                }
              >
                Learn more
              </a>
            </Typography.Small>
          </dd>
        </GridItem>
      )}

      <GridItem colSpan={"span-2"}>
        <GridItem>
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
        </GridItem>
      </GridItem>

      <GridItem colSpan={"span-2"}>
        <GridItem>
          <Label>Message for approval</Label>
          <dd>{request.remarks || <i>No message</i>}</dd>
        </GridItem>
      </GridItem>

      <GridItem>
        <Label>Requested by</Label>
        <dd>{request.requestor}</dd>
      </GridItem>
      <GridItem>
        <Label>Requested on</Label>
        <dd>{request.requesttimestring} UTC</dd>
      </GridItem>
    </Grid>
  );
}

export { SchemaRequestDetails };
