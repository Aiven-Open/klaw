import { Box, Flexbox, Icon, Typography } from "@aivenio/aquarium";
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
      backgroundColor={"info-5"}
      padding={"l1"}
      aria-label={"Preview disclaimer"}
    >
      <Flexbox gap={"l1"}>
        <Icon
          aria-hidden={true}
          color={"info-50"}
          fontSize={20}
          icon={infoSign}
        />
        <Typography variant={"body-small"}>
          You are viewing a preview of the redesigned user interface. You are
          one of our early reviewers, and your{" "}
          <Link
            text={"feedback"}
            target={
              "https://github.com/aiven/klaw/issues/new?template=03_feature.md"
            }
            isRemote={true}
          />{" "}
          will help us improve the product. You can always go back to the{" "}
          <Link text={"old interface"} target={linkTarget} isRemote={false} />.
        </Typography>
      </Flexbox>
    </Box>
  );
}

export default PreviewBanner;
