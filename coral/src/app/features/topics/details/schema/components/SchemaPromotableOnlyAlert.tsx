import { Alert, Box, BoxProps } from "@aivenio/aquarium";

type SchemaPromotableOnlyAlertProps = {
  marginBottom?: BoxProps<"div">["marginBottom"];
};
function SchemaPromotableOnlyAlert({
  marginBottom,
}: SchemaPromotableOnlyAlertProps) {
  return (
    <Box
      marginBottom={marginBottom}
      data-testid={"schema-promotable-only-alert"}
    >
      <Alert type={"warning"}>
        <span>
          Users are not allowed to request a new schema in this environment. To
          add a schema, promote the schema from a lower environment.{" "}
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
