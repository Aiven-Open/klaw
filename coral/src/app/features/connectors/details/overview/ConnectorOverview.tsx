import { Box, PageHeader } from "@aivenio/aquarium";
import MonacoEditor from "@monaco-editor/react";
import { useConnectorDetails } from "src/app/features/connectors/details/ConnectorDetails";
import { ConnectorPromotionBanner } from "src/app/features/connectors/details/components/ConnectorPromotionBanner";

const ConnectorOverview = () => {
  const { connectorOverview } = useConnectorDetails();
  const { promotionDetails } = connectorOverview;
  const { hasOpenClaimRequest, hasOpenRequest, connectorName, connectorOwner } =
    connectorOverview.connectorInfo;

  return (
    <>
      {connectorOwner && (
        <Box paddingTop={"l1"} paddingBottom={"l2"}>
          <ConnectorPromotionBanner
            connectorPromotionDetails={promotionDetails}
            hasOpenConnectorRequest={hasOpenRequest}
            hasOpenClaimRequest={hasOpenClaimRequest}
            connectorName={connectorName}
          />
        </Box>
      )}

      <PageHeader title={"Connector configuration"} />

      <Box borderColor={"grey-20"} borderWidth={"1px"}>
        <MonacoEditor
          data-testid="connector-editor"
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
