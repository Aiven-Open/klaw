import { Alert, Box, BoxProps } from "@aivenio/aquarium";
import { Link } from "react-router-dom";

type OpenSchemaRequestAlertProps = {
  topicName: string;
  marginBottom?: BoxProps<"div">["marginBottom"];
};

function OpenSchemaRequestAlert({
  topicName,
  marginBottom,
}: OpenSchemaRequestAlertProps) {
  return (
    <Box marginBottom={marginBottom}>
      <Alert type={"warning"}>
        <span>
          {`A schema request for ${topicName} is already in progress.`}
        </span>{" "}
        <Link
          to={`/requests/schemas?status=CREATED&page=1&search=${topicName}`}
        >
          View request
        </Link>
        .
      </Alert>
    </Box>
  );
}

export { OpenSchemaRequestAlert };
