import { Flexbox, Grid, GridItem, StatusChip } from "@aivenio/aquarium";
import { AclRequest } from "src/domain/acl/acl-types";

interface DetailsModalContentProps {
  aclRequest?: AclRequest;
}

const Label = ({ children }: { children: React.ReactNode }) => (
  <label className="inline-block mb-2 typography-small-strong text-grey-60">
    {children}
  </label>
);

const DetailsModalContent = ({ aclRequest }: DetailsModalContentProps) => {
  if (aclRequest === undefined) {
    return <div>Request not found.</div>;
  }

  const {
    topictype,
    environmentName = "Environment not found",
    topicname,
    acl_ssl,
    acl_ip,
    consumergroup = "Consumer group not found",
    remarks,
    requesttimestring = "Request time not found",
    username = "User not found",
    requestingTeamName = "Team name not found",
  } = aclRequest;

  const ips = acl_ip || [];
  const principals = acl_ssl || [];

  return (
    <Grid cols={"2"} rows={"6"} rowGap={"6"}>
      <GridItem>
        <Flexbox direction={"column"} width={"min"}>
          <Label>ACL type</Label>
          <div>
            <StatusChip
              status={topictype === "Producer" ? "info" : "success"}
              text={topictype}
            />
          </div>
        </Flexbox>
      </GridItem>
      <GridItem>
        <Flexbox direction={"column"}>
          <Label>Requesting team</Label>
          {requestingTeamName}
        </Flexbox>
      </GridItem>
      <GridItem>
        <Flexbox direction={"column"} width={"min"}>
          <Label>Environment</Label>
          <div>
            <StatusChip status={"neutral"} text={environmentName} />
          </div>
        </Flexbox>
      </GridItem>
      <GridItem>
        <Flexbox direction={"column"}>
          <Label>Topic</Label>
          {topicname}
        </Flexbox>
      </GridItem>
      <GridItem colSpan={"span-2"}>
        <Flexbox direction={"column"}>
          <Label>Principals/Usernames</Label>
          <Flexbox direction={"row"} gap={"2"}>
            {principals.map((principal) => (
              <StatusChip
                key={principal}
                status={"neutral"}
                text={`${principal} `}
              />
            ))}
          </Flexbox>
        </Flexbox>
      </GridItem>
      {ips.length > 0 && (
        <GridItem colSpan={"span-2"}>
          <Flexbox direction={"column"}>
            <Label>IP addresses</Label>
            <Flexbox direction={"row"} gap={"2"}>
              {ips.map((ip) => (
                <StatusChip key={ip} status={"neutral"} text={`${ip} `} />
              ))}
            </Flexbox>
          </Flexbox>
        </GridItem>
      )}
      <GridItem colSpan={"span-2"}>
        <Flexbox direction={"column"}>
          <Label>Consumer group</Label>
          {topictype === "Consumer" ? consumergroup : <i>Not applicable</i>}
        </Flexbox>
      </GridItem>
      <GridItem colSpan={"span-2"}>
        <Flexbox direction={"column"}>
          <Label>Message for the approver</Label>
          {remarks || <i>No message</i>}
        </Flexbox>
      </GridItem>
      <GridItem>
        <Flexbox direction={"column"}>
          <Label> Request by</Label>
          {username}
        </Flexbox>
      </GridItem>
      <GridItem>
        <Flexbox direction={"column"}>
          <Label> Requested on</Label>
          {requesttimestring} UTC
        </Flexbox>
      </GridItem>
    </Grid>
  );
};

export default DetailsModalContent;
