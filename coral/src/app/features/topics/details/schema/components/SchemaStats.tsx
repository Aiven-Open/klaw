import { Box, Card, Grid } from "@aivenio/aquarium";
import StatsDisplay from "src/app/components/StatsDisplay";
import upperFirst from "lodash/upperFirst";
import { SchemaRequest } from "src/domain/schema-request";

type SchemaStatsProps = {
  version: number;
  id: number;
  compatibility: string;
  schemaType: SchemaRequest["schemaType"];
  isLoading: boolean;
};
function SchemaStats({
  isLoading,
  version,
  id,
  compatibility,
  schemaType,
}: SchemaStatsProps) {
  return (
    <Grid
      cols={"2"}
      rows={"1"}
      gap={"l2"}
      style={{ gridTemplateRows: "auto" }}
      marginTop={"l2"}
    >
      <Grid.Item xs={2}>
        <Card title="" fullWidth>
          <Box.Flex display="flex" gap={"l7"}>
            <StatsDisplay
              isLoading={isLoading}
              amount={version}
              entity={"Version no."}
            />
            <StatsDisplay isLoading={isLoading} amount={id} entity={"ID"} />
            <StatsDisplay
              isLoading={isLoading}
              chip={{
                status: "info",
                text: upperFirst(compatibility.toLowerCase()),
              }}
              entity={"Compatibility"}
            />
            <StatsDisplay
              isLoading={isLoading}
              chip={{
                status: "neutral",
                text: schemaType,
              }}
              entity={"Schema type"}
            />
          </Box.Flex>
        </Card>
      </Grid.Item>
    </Grid>
  );
}

export { SchemaStats };
