import {
  Alert,
  Box,
  PageHeader,
  Skeleton,
  Typography,
  useToast,
} from "@aivenio/aquarium";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { DocumentationEditor } from "src/app/components/documentation/DocumentationEditor";
import { DocumentationView } from "src/app/components/documentation/DocumentationView";
import { useConnectorDetails } from "src/app/features/connectors/details/ConnectorDetails";
import {
  ConnectorDocumentationMarkdown,
  updateConnectorDocumentation,
} from "src/domain/connector";
import { isDocumentationTransformationError } from "src/domain/helper/documentation-helper";
import { parseErrorMsg } from "src/services/mutation-utils";
import { NoDocumentationBanner } from "src/app/features/components/documentation/components/NoDocumentationBanner";

const readmeDescription = (connectorOwner: boolean) => {
  const fixedText = `Readme provides essential information, guidelines, and explanations about the connector, helping team members understand its purpose and usage.`;
  const additionalTextConnectorOwner = `Edit the readme to update the information as the connector evolves.`;
  return (
    <Box component={Typography.SmallText} marginBottom={"l2"}>
      {fixedText} {connectorOwner ? additionalTextConnectorOwner : ""}
    </Box>
  );
};
function ConnectorDocumentation() {
  const queryClient = useQueryClient();

  const [editMode, setEditMode] = useState(false);
  const [saving, setSaving] = useState(false);

  const { connectorOverview, connectorIsRefetching } = useConnectorDetails();

  const isUserConnectorOwner = Boolean(
    connectorOverview.connectorInfo.connectorOwner
  );

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
            message: "Readme successfully updated",
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

  if (isUserConnectorOwner && connectorIsRefetching) {
    return (
      <>
        <PageHeader title={"Readme"} />
        <Box paddingTop={"l2"}>
          <div className={"visually-hidden"}>Loading readme</div>
          <Skeleton />
        </Box>
      </>
    );
  }

  if (isUserConnectorOwner && editMode) {
    return (
      <>
        <PageHeader title={"Edit readme"} />
        {readmeDescription(isUserConnectorOwner)}
        <>
          {isError && (
            <Box marginBottom={"l1"}>
              <Alert type="error">
                The readme could not be saved, there was an error: <br />
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
        <PageHeader title={"Readme"} />
        <NoDocumentationBanner
          addDocumentation={() => setEditMode(true)}
          isUserOwner={connectorOverview.connectorInfo.connectorOwner}
          entity={"connector"}
        />
      </>
    );
  }

  if (
    isUserConnectorOwner &&
    isDocumentationTransformationError(connectorOverview.connectorDocumentation)
  ) {
    return (
      <>
        <PageHeader title={"Readme"} />
        <Alert type="error">
          Something went wrong while trying to transform the readme into the
          right format.
        </Alert>
      </>
    );
  }

  return (
    <>
      <PageHeader
        title={"Readme"}
        primaryAction={
          isUserConnectorOwner
            ? {
                text: "Edit readme",
                onClick: () => setEditMode(true),
              }
            : undefined
        }
      />
      {readmeDescription(isUserConnectorOwner)}
      <Box paddingTop={"l2"}>
        <DocumentationView
          markdownString={connectorOverview.connectorDocumentation}
        />
      </Box>
    </>
  );
}

export { ConnectorDocumentation };
