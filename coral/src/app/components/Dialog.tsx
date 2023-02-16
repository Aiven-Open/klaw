import { Modal, ModalProps } from "src/app/components/Modal";
import { DialogType } from "@aivenio/aquarium/atoms/Dialog/Dialog";
import confirm from "@aivenio/aquarium/dist/src/icons/confirm";
import warningSign from "@aivenio/aquarium/dist/src/icons/warningSign";
import error from "@aivenio/aquarium/dist/src/icons/error";
import data from "@aivenio/aquarium/icons/list";
import { Box, Icon, Typography } from "@aivenio/aquarium";
import { ResolveIntersectionTypes } from "types/utils";

type DialogProps = ResolveIntersectionTypes<
  Omit<ModalProps, "isDialog" | "dialogTitle" | "close"> &
    Required<Pick<ModalProps, "secondaryAction">> & {
      type: DialogType;
    }
>;

const dialogTypeMap: Record<
  DialogType,
  { icon: typeof data; color: "info-70" | "secondary-70" | "error-70" }
> = {
  confirmation: {
    icon: confirm,
    color: "info-70",
  },
  warning: {
    icon: warningSign,
    color: "secondary-70",
  },
  danger: {
    icon: error,
    color: "error-70",
  },
};

function Dialog(props: DialogProps) {
  const { type, title } = props;

  const dialogTitle = (
    <Typography.LargeStrong
      htmlTag={"h1"}
      id={"modal-focus"}
      data-focusable
      color={dialogTypeMap[type].color}
    >
      <Box
        component={"span"}
        display={"flex"}
        alignItems={"center"}
        colGap={"l1"}
      >
        <Icon icon={dialogTypeMap[type].icon} />
        <span>{title}</span>
      </Box>
    </Typography.LargeStrong>
  );

  return (
    <Modal
      title={""}
      dialogTitle={dialogTitle}
      isDialog={true}
      primaryAction={props.primaryAction}
      secondaryAction={props.secondaryAction}
    >
      <>{props.children}</>
    </Modal>
  );
}

export { Dialog };
