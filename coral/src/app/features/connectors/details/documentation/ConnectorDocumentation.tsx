import { Alert, Box, PageHeader, Skeleton, useToast } from "@aivenio/aquarium";
import { NoDocumentationBanner } from "src/app/features/connectors/details/documentation/components/NoDocumentationBanner";
import { useConnectorDetails } from "src/app/features/connectors/details/ConnectorDetails";
import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import {
  ConnectorDocumentationMarkdown,
  updateConnectorDocumentation,
} from "src/domain/connector";
import { parseErrorMsg } from "src/services/mutation-utils";
import { DocumentationEditor } from "src/app/components/documentation/DocumentationEditor";
import { DocumentationView } from "src/app/components/documentation/DocumentationView";

function ConnectorDocumentation() {
  const queryClient = useQueryClient();

  const [editMode, setEditMode] = useState(false);
  const [saving, setSaving] = useState(false);

  const { connectorOverview, connectorIsRefetching } = useConnectorDetails();

  const toast = useToast();

  const { mutate, isError, error } = useMutation(
    (markdownString: ConnectorDocumentationMarkdown) => {
      setSaving(true);
      return updateConnectorDocumentation({
        connectorName: connectorOverview.connectorInfo.connectorName,
        connectorIdForDocumentation:
          connectorOverview.connectorIdForDocumentation,
        connectorDocumentation: markdownString,
      });
    },
    {
      onSuccess: () => {
        queryClient.refetchQueries(["connector-overview"]).then(() => {
          toast({
            message: "Documentation successfully updated",
            position: "bottom-left",
            variant: "default",
          });
          setSaving(false);
          setEditMode(false);
        });
      },
      onError: () => setEditMode(false),
    }
  );

  if (connectorIsRefetching) {
    return (
      <>
        <PageHeader title={"Documentation"} />
        <Box paddingTop={"l2"}>
          <div className={"visually-hidden"}>Loading documentation</div>
          <Skeleton />
        </Box>
      </>
    );
  }

  if (editMode) {
    return (
      <>
        <PageHeader title={"Edit documentation"} />
        <>
          {isError && (
            <Box marginBottom={"l1"} role="alert">
              <Alert type="error">
                The documentation could not be saved, there was an error: <br />
                {parseErrorMsg(error)}
              </Alert>
            </Box>
          )}
          <DocumentationEditor
            documentation={connectorOverview.connectorDocumentation}
            save={(text) => mutate(text)}
            cancel={() => setEditMode(false)}
            isSaving={saving}
          />
        </>
      </>
    );
  }

  if (
    connectorOverview.connectorDocumentation === undefined ||
    connectorOverview.connectorDocumentation.length === 0
  ) {
    return (
      <>
        <PageHeader title={"Documentation"} />
        <NoDocumentationBanner addDocumentation={() => setEditMode(true)} />
      </>
    );
  }

  return (
    <>
      <PageHeader
        title={"Documentation"}
        primaryAction={{
          text: "Edit documentation",
          onClick: () => setEditMode(true),
        }}
      />
      <Box paddingTop={"l2"}>
        <DocumentationView
          markdownString={connectorOverview.connectorDocumentation}
        />
      </Box>
    </>
  );
}

export { ConnectorDocumentation };
