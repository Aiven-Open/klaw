import { Box, Icon, Typography } from "@aivenio/aquarium";
import infoSign from "@aivenio/aquarium/dist/module/icons/infoSign";

function Link({
  text,
  target,
  isRemote,
}: {
  text: string;
  target: string;
  isRemote: boolean;
}) {
  const remoteAttrs = isRemote && {
    target: "_blank",
    rel: "noreferrer",
  };
  return (
    <a href={target} {...remoteAttrs}>
      <Typography color="primary-80" variant={"body-small"} htmlTag="span">
        {text}
      </Typography>
    </a>
  );
}

function PreviewBanner({ linkTarget }: { linkTarget: string }) {
  return (
    <Box
      component={"section"}
      display={"flex"}
      gap={"l1"}
      backgroundColor={"info-5"}
      padding={"l1"}
      marginBottom={"l1"}
      aria-label={"Preview disclaimer"}
    >
      <Typography.SmallText>
        <Icon icon={infoSign} color={"info-50"} style={{ marginTop: "5px" }} />
      </Typography.SmallText>
      <Typography.SmallText>
        You are viewing a preview of the redesigned user interface. You are one
        of our early reviewers, and your{" "}
        <Link
          text={"feedback"}
          target={
            "https://github.com/aiven/klaw/issues/new?template=03_feature.md"
          }
          isRemote={true}
        />{" "}
        will help us improve the product. You can always go back to the{" "}
        <Link text={"old interface"} target={linkTarget} isRemote={false} />.
      </Typography.SmallText>
    </Box>
  );
}

export default PreviewBanner;
