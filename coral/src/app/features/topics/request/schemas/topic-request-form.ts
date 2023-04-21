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

const environmentField: z.ZodType<Environment> = z.object({
  name: z.string(),
  id: z.string(),
  type: z.string(),
  params: z.object({
    maxRepFactor: z.number().optional(),
    maxPartitions: z.number().optional(),
    defaultPartitions: z.number().optional(),
    defaultRepFactor: z.number().optional(),
    topicPrefix: z.array(z.string()).optional(),
    topicSuffix: z.array(z.string()).optional(),
  }),
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
  const {
    environment: {
      params: { maxRepFactor },
    },
    replicationfactor,
  } = val;
  if (
    isNumber(maxRepFactor) &&
    parseInt(replicationfactor, 10) > maxRepFactor
  ) {
    ctx.addIssue({
      code: z.ZodIssueCode.too_big,
      inclusive: true,
      maximum: maxRepFactor,
      type: "number",
      path: ["replicationfactor"],
      message: `${replicationfactor} can not be bigger than ${maxRepFactor}`,
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
  const {
    environment: {
      params: { maxPartitions },
    },
    topicpartitions,
  } = val;
  if (
    isNumber(maxPartitions) &&
    parseInt(topicpartitions, 10) > maxPartitions
  ) {
    ctx.addIssue({
      code: z.ZodIssueCode.too_big,
      inclusive: true,
      maximum: maxPartitions,
      type: "number",
      path: ["topicpartitions"],
      message: `${topicpartitions} can not be bigger than ${maxPartitions}`,
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
  const {
    environment: {
      params: { topicPrefix, topicSuffix },
    },
    topicname,
  } = val;

  const prefixToCheck = topicPrefix?.[0];
  if (prefixToCheck !== undefined && !topicname.startsWith(prefixToCheck)) {
    ctx.addIssue({
      code: z.ZodIssueCode.custom,
      fatal: true,
      message: `Topic name must start with "${prefixToCheck}".`,
      path: ["topicname"],
    });
  }

  const suffixToCheck = topicSuffix?.[0];
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
        environmentMax: environment.params.maxPartitions,
        environmentDefault: environment.params.defaultPartitions,
        fallbackDefault: 2,
      });
      form.setValue("topicpartitions", nextTopicPartitions.toString(), {
        shouldValidate: false,
      });

      const nextReplicationFactorValue = findNextValue({
        currentValue: replicationFactor,
        environmentMax: environment.params.maxRepFactor,
        environmentDefault: environment.params.defaultRepFactor,
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
  environmentMax: z.infer<typeof environmentField>["params"]["maxPartitions"];
  environmentDefault: z.infer<
    typeof environmentField
  >["params"]["defaultPartitions"];
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
