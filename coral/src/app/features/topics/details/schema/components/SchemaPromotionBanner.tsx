import { Banner, Box, Button } from "@aivenio/aquarium";
import illustration from "/src/app/images/topic-details-schema-Illustration.svg";

type SchemaPromotionBannerProps = {
  environment: string;
  promoteSchema: () => void;
};
function SchemaPromotionBanner({
  environment,
  promoteSchema,
}: SchemaPromotionBannerProps) {
  return (
    <Banner image={illustration} layout="vertical" title={""}>
      <Box element={"p"} marginBottom={"l1"}>
        This schema has not yet been promoted to the {environment} environment.
      </Box>
      <Button.Primary onClick={promoteSchema}>Promote</Button.Primary>
    </Banner>
  );
}

export { SchemaPromotionBanner };
