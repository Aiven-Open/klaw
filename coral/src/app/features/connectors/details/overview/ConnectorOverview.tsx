import { Box, PageHeader } from "@aivenio/aquarium";
import MonacoEditor from "@monaco-editor/react";
import { useConnectorDetails } from "src/app/features/connectors/details/ConnectorDetails";

const ConnectorOverview = () => {
  const { connectorOverview } = useConnectorDetails();

  return (
    <>
      <PageHeader title={"Connector configuration"} />

      <Box borderColor={"grey-20"} borderWidth={"1px"}>
        <MonacoEditor
          data-testid="topic-connector"
          height="300px"
          language="json"
          theme={"light"}
          value={connectorOverview.connectorInfo.connectorConfig}
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
      </Box>
    </>
  );
};

export { ConnectorOverview };
