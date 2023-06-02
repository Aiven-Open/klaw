import { Box, Card, Grid, GridItem } from "@aivenio/aquarium";
import StatsDisplay from "src/app/features/topics/details/components/StatsDisplay";

type SchemaStatsProps = {
  version: number;
  id: number;
  compatibility: string;
};
function SchemaStats({ version, id, compatibility }: SchemaStatsProps) {
  return (
    <Grid
      cols={"2"}
      rows={"1"}
      gap={"l2"}
      style={{ gridTemplateRows: "auto" }}
      marginTop={"l2"}
    >
      <GridItem colSpan={"span-2"}>
        <Card title="" fullWidth>
          <Box.Flex display="flex" gap={"l7"}>
            <StatsDisplay amount={version} entity={"Version no."} />
            <StatsDisplay amount={id} entity={"ID"} />
            <StatsDisplay
              chip={{ status: "info", text: compatibility }}
              entity={"Compatibility"}
            />
          </Box.Flex>
        </Card>
      </GridItem>
    </Grid>
  );
}

export { SchemaStats };
