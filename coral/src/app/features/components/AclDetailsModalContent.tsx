import { Box, Grid, GridItem, StatusChip } from "@aivenio/aquarium";
import { AclRequest } from "src/domain/acl/acl-types";

interface DetailsModalContentProps {
  request?: AclRequest;
}

const Label = ({ children }: { children: React.ReactNode }) => (
  <dt className="inline-block mb-2 typography-small-strong text-grey-60">
    {children}
  </dt>
);

const TopicDetailsModalContent = ({ request }: DetailsModalContentProps) => {
  if (request === undefined) {
    return <div>Request not found.</div>;
  }

  const {
    aclType,
    environmentName = "Environment not found",
    topicname,
    acl_ssl,
    acl_ip,
    consumergroup = "Consumer group not found",
    remarks,
    requesttimestring = "Request time not found",
    requestor = "User not found",
    requestingTeamName = "Team name not found",
  } = request;

  return (
    <Grid htmlTag={"dl"} cols={"2"} rows={"6"} rowGap={"6"}>
      <Box direction={"column"} width={"min"}>
        <Label>ACL type</Label>
        <dd>
          <StatusChip
            status={aclType === "PRODUCER" ? "info" : "success"}
            text={aclType}
          />
        </dd>
      </Box>
      <Box direction={"column"}>
        <Label>Requesting team</Label>
        <dd>{requestingTeamName}</dd>
      </Box>

      <Box direction={"column"} width={"min"}>
        <Label>Environment</Label>
        <dd>
          <StatusChip status={"neutral"} text={environmentName} />
        </dd>
      </Box>
      <Box direction={"column"}>
        <Label>Topic</Label>
        <dd>{topicname}</dd>
      </Box>

      <GridItem colSpan={"span-2"}>
        <Box direction={"column"}>
          <Label>Principals/Usernames</Label>
          <Box flexDirection={"row"} gap={"2"} component={"ul"}>
            {acl_ssl.map((principal) => (
              <li key={principal}>
                <dd>
                  <StatusChip status={"neutral"} text={principal} />
                </dd>
              </li>
            ))}
          </Box>
        </Box>
      </GridItem>

      {acl_ip.length > 0 && (
        <GridItem colSpan={"span-2"}>
          <Box direction={"column"}>
            <Label>IP addresses</Label>
            <Box flexDirection={"row"} gap={"2"} component={"ul"}>
              {acl_ip.map((ip) => (
                <li key={ip}>
                  <dd>
                    <StatusChip status={"neutral"} text={ip} />
                  </dd>
                </li>
              ))}
            </Box>
          </Box>
        </GridItem>
      )}

      <GridItem colSpan={"span-2"}>
        <Box direction={"column"}>
          <Label>Consumer group</Label>
          <dd>
            {
              // If consumergroup is "-na-", it means the request was made for an Aiven cluster
              // Which does not user consumergroup
              // So we return Not applicable
              aclType === "CONSUMER" || consumergroup === "-na-" ? (
                consumergroup
              ) : (
                <i>Not applicable</i>
              )
            }
          </dd>
        </Box>
      </GridItem>

      <GridItem colSpan={"span-2"}>
        <Box direction={"column"}>
          <Label>Message for the approver</Label>
          <dd>{remarks || <i>No message</i>}</dd>
        </Box>
      </GridItem>

      <Box direction={"column"}>
        <Label>Requested by</Label>
        <dd>{requestor}</dd>
      </Box>
      <Box direction={"column"}>
        <Label>Requested on</Label>
        <dd>{requesttimestring} UTC</dd>
      </Box>
    </Grid>
  );
};

export default TopicDetailsModalContent;
