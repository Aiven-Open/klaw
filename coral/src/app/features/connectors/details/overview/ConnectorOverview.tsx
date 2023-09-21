import { Box, Label } from "@aivenio/aquarium";
import MonacoEditor from "@monaco-editor/react";
import { useConnectorDetails } from "src/app/features/connectors/details/ConnectorDetails";

const ConnectorOverview = () => {
  const { connectorOverview } = useConnectorDetails();
  // This is necessary to format the JSON renderd in MonacoEditor instead of rendering all in one line
  const parsedConnectorInfo = JSON.stringify(
    connectorOverview.connectorInfo,
    null,
    2
  );

  return (
    <Box.Flex marginTop={"l3"} marginBottom={"l2"}>
      <Label>Connector</Label>
      <Box.Flex borderColor={"grey-20"} borderWidth={"1px"}>
        <MonacoEditor
          data-testid="topic-connector"
          height="300px"
          language="json"
          theme={"light"}
          value={parsedConnectorInfo}
          loading={"Loading preview"}
          options={{
            ariaLabel: "Connector preview",
            readOnly: true,
            domReadOnly: true,
            renderControlCharacters: false,
            minimap: { enabled: false },
            folding: false,
            lineNumbers: "off",
            scrollBeyondLastLine: false,
          }}
        />
      </Box.Flex>
    </Box.Flex>
  );
};

export { ConnectorOverview };
