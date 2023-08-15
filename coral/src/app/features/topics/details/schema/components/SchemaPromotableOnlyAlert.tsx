import { Alert, Box, BoxProps } from "@aivenio/aquarium";

type SchemaPromotableOnlyAlertProps = {
  marginBottom?: BoxProps<"div">["marginBottom"];
  isNewVersionRequest?: boolean;
};

const textVersion =
  "You can't create a new schema or new version for it here. Create your request in the lowest environment and then" +
  " promote it upwards.";

const textNewCreation =
  "Schemas are created in lower environments and promoted to higher environment. To add a schema to this topic, create a request in the lower environment and promote it to the higher one.";

function SchemaPromotableOnlyAlert({
  marginBottom,
  isNewVersionRequest = false,
}: SchemaPromotableOnlyAlertProps) {
  return (
    <Box
      marginBottom={marginBottom}
      data-testid={"schema-promotable-only-alert"}
    >
      <Alert type={"warning"}>
        <span>
          {isNewVersionRequest ? textVersion : textNewCreation}{" "}
          <a
            target="_blank"
            rel="noreferrer"
            href={
              "https://www.klaw-project.io/docs/Concepts/promotion#schema-promotion"
            }
          >
            Learn more
          </a>
          .
        </span>
      </Alert>
    </Box>
  );
}

export { SchemaPromotableOnlyAlert };
