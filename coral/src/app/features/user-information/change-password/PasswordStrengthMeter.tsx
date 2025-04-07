import { Box, Typography } from "@aivenio/aquarium";
import { getFulfilledPasswordRules } from "src/app/features/user-information/change-password/password-requirement";
import { useEffect, useMemo, useState } from "react";

type Props = {
  password: string;
  active: boolean;
};

function PasswordStrengthMeter({ password, active }: Props) {
  const fulfilledRules = getFulfilledPasswordRules(password);

  const accessibleSummaryText = useMemo(() => {
    const summaryParts = fulfilledRules.reduce(
      (acc, rule) => {
        const text = `${rule.label}: ${rule.fulfilled ? "Met" : "Not met"}`;
        if (rule.fulfilled) {
          acc.met.push(text);
        } else {
          acc.notMet.push(text);
        }
        return acc;
      },
      { met: [] as string[], notMet: [] as string[] }
    );

    return [...summaryParts.met, ...summaryParts.notMet].join(". ");
  }, [fulfilledRules]);

  const [announcement, setAnnouncement] = useState("");

  useEffect(() => {
    if (password) {
      // Delay announcing to help screen readers catch the change
      const timeout = setTimeout(() => {
        setAnnouncement(accessibleSummaryText);
      }, 100);
      return () => clearTimeout(timeout);
    } else {
      setAnnouncement("");
    }
  }, [accessibleSummaryText, password]);

  return (
    <>
      <Box.Flex gap="2" aria-hidden="true">
        {fulfilledRules.map((rule, index) => (
          <Box key={index} flex="1">
            <Box
              backgroundColor={rule.fulfilled ? "success-60" : "grey-40"}
              height={"3"}
              width={"full"}
              marginBottom={"1"}
            />
            <Typography.Caption>{rule.label}</Typography.Caption>
          </Box>
        ))}
      </Box.Flex>
      {active && announcement && (
        <div role="alert" className="visually-hidden">
          {announcement}
        </div>
      )}
    </>
  );
}

export { PasswordStrengthMeter };
