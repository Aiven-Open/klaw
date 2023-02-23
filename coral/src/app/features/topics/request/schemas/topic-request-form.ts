import z, { RefinementCtx } from "zod";
import isNumber from "lodash/isNumber";
import { UseFormReturn } from "react-hook-form";
import { useEffect } from "react";

const topicNameField = z
  .string()
  .min(1, { message: "Topic name can not be empty" })
  .max(255, { message: "Topic name length can not exceed 255 characters" });

const topicPartitionsField = z.string();

const replicationFactorField = z.string();

const environmentField = z.object({
  name: z.string(),
  id: z.string(),
  maxReplicationFactor: z.number().optional(),
  maxPartitions: z.number().optional(),
  defaultPartitions: z.number().optional(),
  defaultReplicationFactor: z.number().optional(),
  topicNamePrefix: z.string().optional(),
  topicNameSuffix: z.string().optional(),
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
    environment: { maxReplicationFactor },
    replicationfactor,
  } = val;
  if (
    isNumber(maxReplicationFactor) &&
    parseInt(replicationfactor, 10) > maxReplicationFactor
  ) {
    ctx.addIssue({
      code: z.ZodIssueCode.too_big,
      inclusive: true,
      maximum: maxReplicationFactor,
      type: "number",
      path: ["replicationfactor"],
      message: `${replicationfactor} can not be bigger than ${maxReplicationFactor}`,
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
    environment: { maxPartitions },
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
    environment: { topicNamePrefix, topicNameSuffix },
    topicname,
  } = val;
  if (topicNamePrefix !== undefined && !topicname.startsWith(topicNamePrefix)) {
    ctx.addIssue({
      code: z.ZodIssueCode.custom,
      fatal: true,
      message: `Topic name must start with "${topicNamePrefix}".`,
      path: ["topicname"],
    });
  }
  if (topicNameSuffix !== undefined && !topicname.endsWith(topicNameSuffix)) {
    ctx.addIssue({
      code: z.ZodIssueCode.custom,
      fatal: true,
      message: `Topic name must end with "${topicNameSuffix}".`,
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
        environmentMax: environment.maxPartitions,
        environmentDefault: environment.defaultPartitions,
        fallbackDefault: 2,
      });
      form.setValue("topicpartitions", nextTopicPartitions.toString(), {
        shouldValidate: false,
      });

      const nextReplicationFactorValue = findNextValue({
        currentValue: replicationFactor,
        environmentMax: environment.maxReplicationFactor,
        environmentDefault: environment.defaultReplicationFactor,
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
  environmentMax: z.infer<typeof environmentField>["maxPartitions"];
  environmentDefault: z.infer<typeof environmentField>["defaultPartitions"];
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
