import { KlawApiModel } from "types/utils";

type PromotionStatus =
  | KlawApiModel<"PromotionStatus">
  | KlawApiModel<"ConnectorPromotionStatus">;

export type { PromotionStatus };
