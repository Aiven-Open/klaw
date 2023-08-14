import { Alert, Box, Link, Typography } from "@aivenio/aquarium";

function PreviewBanner({ linkTarget }: { linkTarget: string }) {
  return (
    <Box
      marginBottom={"l1"}
      component={"section"}
      aria-label={"Preview disclaimer"}
    >
      <Alert type={"information"}>
        <Typography.Small>
          You are viewing a preview of the redesigned user interface. You are
          one of our early reviewers, and your{" "}
          <Link
            href={
              "https://github.com/aiven/klaw/issues/new?template=03_feature.md"
            }
            target={"_blank"}
            rel="noreferrer"
          >
            feedback
          </Link>{" "}
          will help us improve the product. You can always go back to the{" "}
          <Link href={linkTarget}>old interface</Link>.
        </Typography.Small>
      </Alert>
    </Box>
  );
}

export default PreviewBanner;
