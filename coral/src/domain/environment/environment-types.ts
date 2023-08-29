import { KlawApiModel } from "types/utils";

type EnvironmentParams = KlawApiModel<"EnvParams">;
// KlawApiModel<"EnvModel">
// is only relevant when creating a new Environment and reserved to that usage
// we're not using that right now, that's why it's commented out
// type CreateEnvironment = KlawApiModel<"EnvModel">

// KlawApiModel<"EnvModelResponse">
// This represents the Environment in the Backend and is what we're using
// when talking about "Environment"
// we're redefining property types here to fit our need in app better
// transformEnvironmentApiResponse() is taking care of transforming
// the properties and makes sure the types are matching between BE and FE
type Environment = {
  name: KlawApiModel<"EnvModelResponse">["name"];
  id: KlawApiModel<"EnvModelResponse">["id"];
  type: KlawApiModel<"EnvModelResponse">["type"];
  // even though the openapi definition defines `params` as required
  // some endpoints don't have a `params` property,
  // so we need to make sure that we know where to
  // handle a potential missing params property
  params?: {
    // will be changed to Integer in BE at some point,
    // and we need it best as a number
    defaultPartitions?: number;
    defaultRepFactor?: number;
    maxPartitions?: number;
    maxRepFactor?: number;
    applyRegex?: EnvironmentParams["applyRegex"];
    topicRegex?: EnvironmentParams["topicRegex"];
    topicPrefix?: EnvironmentParams["topicPrefix"];
    topicSuffix?: EnvironmentParams["topicSuffix"];
  };
};

type EnvironmentInfo = KlawApiModel<"EnvIdInfo">;

const ALL_ENVIRONMENTS_VALUE = "ALL";

export type { Environment, EnvironmentInfo };
export { ALL_ENVIRONMENTS_VALUE };
