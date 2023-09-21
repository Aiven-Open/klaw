import { Alert, Box, PageHeader, Skeleton, useToast } from "@aivenio/aquarium";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { DocumentationEditor } from "src/app/components/documentation/DocumentationEditor";
import { DocumentationView } from "src/app/components/documentation/DocumentationView";
import { useConnectorDetails } from "src/app/features/connectors/details/ConnectorDetails";
import { NoDocumentationBanner } from "src/app/features/connectors/details/documentation/components/NoDocumentationBanner";
import {
  ConnectorDocumentationMarkdown,
  updateConnectorDocumentation,
} from "src/domain/connector";
import { isDocumentationTransformationError } from "src/domain/helper/documentation-helper";
import { parseErrorMsg } from "src/services/mutation-utils";

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
      onError: () => {
        setSaving(false);
      },
    }
  );

  if (connectorIsRefetching) {
    return (
      <>
        <PageHeader title={"Documentation"} />
        <Box.Flex paddingTop={"l2"}>
          <div className={"visually-hidden"}>Loading documentation</div>
          <Skeleton />
        </Box.Flex>
      </>
    );
  }

  if (editMode) {
    return (
      <>
        <PageHeader title={"Edit documentation"} />
        <>
          {isError && (
            <Box.Flex marginBottom={"l1"} role="alert">
              <Alert type="error">
                The documentation could not be saved, there was an error: <br />
                {parseErrorMsg(error)}
              </Alert>
            </Box.Flex>
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

  if (
    isDocumentationTransformationError(connectorOverview.connectorDocumentation)
  ) {
    return (
      <>
        <PageHeader title={"Documentation"} />
        <Box.Flex role="alert">
          <Alert type="error">
            Something went wrong while trying to transform the documentation
            into the right format.
          </Alert>
        </Box.Flex>
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
      <Box.Flex paddingTop={"l2"}>
        <DocumentationView
          markdownString={connectorOverview.connectorDocumentation}
        />
      </Box.Flex>
    </>
  );
}

export { ConnectorDocumentation };
