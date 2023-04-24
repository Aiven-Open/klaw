import z, { RefinementCtx } from "zod";
import isNumber from "lodash/isNumber";
import { UseFormReturn } from "react-hook-form";
import { useEffect } from "react";
import { Environment } from "src/domain/environment";

const topicNameField = z
  .string()
  .min(1, { message: "Topic name can not be empty" })
  .max(255, { message: "Topic name length can not exceed 255 characters" });

const topicPartitionsField = z.string();

const replicationFactorField = z.string();

const environmentParams = z.object({
  maxRepFactor: z.number().optional(),
  maxPartitions: z.number().optional(),
  defaultPartitions: z.number().optional(),
  defaultRepFactor: z.number().optional(),
  topicPrefix: z.array(z.string()).optional(),
  topicSuffix: z.array(z.string()).optional(),
});

const environmentField: z.ZodType<Environment> = z.object({
  name: z.string(),
  id: z.string(),
  type: z.string(),
  params: environmentParams.optional(),
});

const advancedConfigurationField = z.string().optional();

const formSchema = z
  .object({
    environment: environmentField,
    topicpartitions: topicPartitionsField,
    replicationfactor: replicationFactorField,
    topicname: topicNameField,
    advancedConfiguration: advancedConfigurationField,
    remarks: z.string(),
    description: z.string().min(1),
  })
  .superRefine(validateTopicPartitions)
  .superRefine(validateReplicationFactor)
  .superRefine(validateTopicName);

function validateReplicationFactor(
  val: {
    environment: z.infer<typeof environmentField>;
    replicationfactor: z.infer<typeof replicationFactorField>;
  },
  ctx: RefinementCtx
) {
  const { environment, replicationfactor } = val;
  if (
    environment.params?.maxRepFactor &&
    isNumber(environment.params?.maxRepFactor) &&
    parseInt(replicationfactor, 10) > environment.params.maxRepFactor
  ) {
    ctx.addIssue({
      code: z.ZodIssueCode.too_big,
      inclusive: true,
      maximum: environment.params.maxRepFactor,
      type: "number",
      path: ["replicationfactor"],
      message: `${replicationfactor} can not be bigger than ${environment.params.maxRepFactor}`,
    });
  }
}

function validateTopicPartitions(
  val: {
    environment: z.infer<typeof environmentField>;
    topicpartitions: z.infer<typeof topicPartitionsField>;
  },
  ctx: RefinementCtx
) {
  const { environment, topicpartitions } = val;
  if (
    environment.params?.maxPartitions &&
    isNumber(environment.params.maxPartitions) &&
    parseInt(topicpartitions, 10) > environment.params.maxPartitions
  ) {
    ctx.addIssue({
      code: z.ZodIssueCode.too_big,
      inclusive: true,
      maximum: environment.params.maxPartitions,
      type: "number",
      path: ["topicpartitions"],
      message: `${topicpartitions} can not be bigger than ${environment.params.maxPartitions}`,
    });
  }
}

function validateTopicName(
  val: {
    environment: z.infer<typeof environmentField>;
    topicname: z.infer<typeof topicNameField>;
  },
  ctx: RefinementCtx
) {
  const { environment, topicname } = val;

  if (topicname.length < 3) {
    ctx.addIssue({
      code: z.ZodIssueCode.custom,
      fatal: true,
      message: "Topic name must contain at least 3 characters.",
      path: ["topicname"],
    });
    return;
  }

  const defaultTopicNamePattern = /^[a-zA-Z0-9._-]{3,}$/;
  if (!defaultTopicNamePattern.test(topicname)) {
    ctx.addIssue({
      code: z.ZodIssueCode.custom,
      fatal: true,
      message:
        "Topic name can only contain letters, digits, period, underscore, hyphen.",
      path: ["topicname"],
    });
    return;
  }

  const prefixToCheck = environment.params?.topicPrefix?.[0];
  if (prefixToCheck !== undefined && !topicname.startsWith(prefixToCheck)) {
    ctx.addIssue({
      code: z.ZodIssueCode.custom,
      fatal: true,
      message: `Topic name must start with "${prefixToCheck}".`,
      path: ["topicname"],
    });
  }

  const suffixToCheck = environment.params?.topicSuffix?.[0];
  if (suffixToCheck !== undefined && !topicname.endsWith(suffixToCheck)) {
    ctx.addIssue({
      code: z.ZodIssueCode.custom,
      fatal: true,
      message: `Topic name must end with "${suffixToCheck}".`,
      path: ["topicname"],
    });
  }
}

const useExtendedFormValidationAndTriggers = (
  form: UseFormReturn<Schema>,
  { isInitialized }: { isInitialized: boolean }
) => {
  const [environment, topicName, topicPartitions, replicationFactor] =
    form.getValues([
      "environment",
      "topicname",
      "topicpartitions",
      "replicationfactor",
    ]);
  useEffect(() => {
    // When environment is updated, update partitions and replication factors to default is exists
    if (isInitialized && environment !== undefined) {
      const nextTopicPartitions = findNextValue({
        currentValue: topicPartitions,
        environmentMax: environment.params?.maxPartitions,
        environmentDefault: environment.params?.defaultPartitions,
        fallbackDefault: 2,
      });
      form.setValue("topicpartitions", nextTopicPartitions.toString(), {
        shouldValidate: false,
      });

      const nextReplicationFactorValue = findNextValue({
        currentValue: replicationFactor,
        environmentMax: environment.params?.maxRepFactor,
        environmentDefault: environment.params?.defaultRepFactor,
        fallbackDefault: 1,
      });
      form.setValue(
        "replicationfactor",
        nextReplicationFactorValue.toString(),
        {
          shouldValidate: false,
        }
      );

      if (topicName.length > 0) {
        form.trigger(["replicationfactor", "topicpartitions", "topicname"]);
      } else {
        form.trigger(["replicationfactor", "topicpartitions"]);
      }
    }
  }, [environment?.id]);

  useEffect(() => {
    form.trigger("replicationfactor");
  }, [replicationFactor]);

  useEffect(() => {
    if (isInitialized) {
      form.trigger("topicpartitions");
    }
  }, [topicPartitions]);

  useEffect(() => {
    if (isInitialized && topicName?.length > 0) {
      form.trigger("topicname");
    }
  }, [topicName]);
};

type FoobarArgs = {
  currentValue: string;
  environmentMax: z.infer<typeof environmentParams>["maxPartitions"];
  environmentDefault: z.infer<typeof environmentParams>["defaultPartitions"];
  fallbackDefault: number;
};
function findNextValue({
  currentValue,
  environmentMax,
  environmentDefault,
  fallbackDefault,
}: FoobarArgs): number {
  if (isNumber(environmentDefault)) {
    return environmentDefault;
  }

  const currentValueAsNumber = parseInt(currentValue, 10);
  // parseInt("", 10) = NaN
  if (!Number.isNaN(currentValueAsNumber)) {
    if (isNumber(environmentMax) && currentValueAsNumber > environmentMax) {
      return environmentMax;
    } else {
      return currentValueAsNumber;
    }
  }
  return fallbackDefault;
}

export type Schema = z.infer<typeof formSchema>;
export default formSchema;
export { useExtendedFormValidationAndTriggers };
