import { Box, Icon, Spacing, Typography } from "@aivenio/aquarium";
import link from "@aivenio/aquarium/icons/link";
import ClipBoard from "src/app/components/Clipboard";
import { Modal } from "src/app/components/Modal";
import { ClusterDetails } from "src/domain/cluster";

const getApplicationPropertiesString = ({
  clusterName,
  clusterId,
  kafkaFlavor,
  clusterType,
  protocol,
}: {
  clusterName: ClusterDetails["clusterName"];
  clusterId: ClusterDetails["clusterId"];
  kafkaFlavor: ClusterDetails["kafkaFlavor"];
  clusterType: ClusterDetails["clusterType"];
  protocol: ClusterDetails["protocol"];
}) => {
  if (clusterType === "KAFKA" && kafkaFlavor === "APACHE_KAFKA") {
    if (protocol === "SSL") {
      return `${clusterName.toLowerCase() + clusterId}.kafkassl.keystore.location=path/to/client.keystore.p12\n${clusterName.toLowerCase() + clusterId}.kafkassl.keystore.pwd=keystorePw\n${clusterName.toLowerCase() + clusterId}.kafkassl.key.pwd=keyPw\n${clusterName.toLowerCase() + clusterId}.kafkassl.truststore.location=path/to/client.truststore.jks\n${clusterName.toLowerCase() + clusterId}.kafkassl.truststore.pwd=truststorePw\n${clusterName.toLowerCase() + clusterId}.kafkassl.keystore.type=pkcs12\n${clusterName.toLowerCase() + clusterId}.kafkassl.truststore.type=JKS`;
    }
    if (protocol === "SASL_PLAIN" || protocol === "SASL_SSL_PLAIN_MECHANISM") {
      return `${clusterName.toLowerCase() + clusterId}.kafkasasl.jaasconfig.plain=org.apache.kafka.common.security.plain.PlainLoginModule\nrequired username='kwuser' password='kwuser-secret'`;
    }
    if (
      protocol === "SASL_SSL_SCRAM_MECHANISM_256" ||
      protocol === "SASL_SSL_SCRAM_MECHANISM_512"
    ) {
      return `${clusterName.toLowerCase() + clusterId}.kafkasasl.jaasconfig.scram=org.apache.kafka.common.security.scram.ScramLoginModule\nrequired username='kwuser' password='kwuser-secret'`;
    }
    if (protocol === "SASL_SSL_GSSAPI_MECHANISM") {
      return `${clusterName.toLowerCase() + clusterId}.kafkasasl.jaasconfig.gssapi=com.sun.security.auth.module.Krb5LoginModule\nrequired useKeyTab=true storeKey=true keyTab="/path/to/kafka_client.keytab"\n<principal=%22kafkaclient1@EXAMPLE.COM>"`;
    }
  }
  if (clusterType === "KAFKA" && kafkaFlavor === "AIVEN_FOR_APACHE_KAFKA") {
    if (protocol === "SSL") {
      return `${clusterName.toLowerCase() + clusterId}.kafkassl.keystore.location=path/to/client.keystore.p12\n${clusterName.toLowerCase() + clusterId}.kafkassl.keystore.pwd=keystorePw\n${clusterName.toLowerCase() + clusterId}.kafkassl.key.pwd=keyPw\n${clusterName.toLowerCase() + clusterId}.kafkassl.truststore.location=path/to/client.truststore.jks\n${clusterName.toLowerCase() + clusterId}.kafkassl.truststore.pwd=truststorePw\n${clusterName.toLowerCase() + clusterId}.kafkassl.keystore.type=pkcs12\n${clusterName.toLowerCase() + clusterId}.kafkassl.truststore.type=JKS\nklaw.clusters.accesstoken=yourAivenAccessToken`;
    }
    if (protocol === "SASL_PLAIN" || protocol === "SASL_SSL_PLAIN_MECHANISM") {
      return `${clusterName.toLowerCase() + clusterId}.kafkasasl.jaasconfig.plain=org.apache.kafka.common.security.plain.PlainLoginModule\nrequired username='kwuser' password='kwuser-secret'\nklaw.clusters.accesstoken=yourAivenAccessToken`;
    }
    if (
      protocol === "SASL_SSL_SCRAM_MECHANISM_256" ||
      protocol === "SASL_SSL_SCRAM_MECHANISM_512"
    ) {
      return `${clusterName.toLowerCase() + clusterId}.kafkasasl.jaasconfig.scram=org.apache.kafka.common.security.scram.ScramLoginModule\nrequired username='kwuser' password='kwuser-secret'\nklaw.clusters.accesstoken=yourAivenAccessToken`;
    }
    if (protocol === "SASL_SSL_GSSAPI_MECHANISM") {
      return `${clusterName.toLowerCase() + clusterId}.kafkasasl.jaasconfig.gssapi=com.sun.security.auth.module.Krb5LoginModule\nrequired useKeyTab=true storeKey=true keyTab="/path/to/kafka_client.keytab"\n<principal=%22kafkaclient1@EXAMPLE.COM>\nklaw.clusters.accesstoken=yourAivenAccessToken"`;
    }
  }

  if (clusterType === "SCHEMA_REGISTRY" && protocol === "SSL") {
    return `${clusterName.toLowerCase() + clusterId}.klaw.schemaregistry.credentials=username:password`;
  }

  if (clusterType === "KAFKA_CONNECT") {
    if (kafkaFlavor === "APACHE_KAFKA" && protocol === "SSL") {
      return `${clusterName.toLowerCase() + clusterId}.klaw.kafkaconnect.credentials=username:password`;
    }
    if (kafkaFlavor === "AIVEN_FOR_APACHE_KAFKA") {
      return `${clusterName.toLowerCase() + clusterId}.klaw.kafkaconnect.credentials=aivenUsername:aivenPassword`;
    }
    if (kafkaFlavor === "CONFLUENT_CLOUD") {
      return `${clusterName.toLowerCase() + clusterId}.klaw.confluentcloud.credentials=confluentApikey:confluentApisecret\n${clusterName.toLowerCase() + clusterId}.klaw.clusters.counfluentcloud.acls.api=/kafka/v3/clusters/${clusterId}/acls\n${clusterName.toLowerCase() + clusterId}.klaw.clusters.counfluentcloud.topics.api=/kafka/v3/clusters/${clusterId}/topics\n`;
    }
  }

  // Default case: no necessary application.properties to copy
  return "";
};

interface ClusterConnectHelpModalProps {
  onClose: () => void;
  modalData: {
    kafkaFlavor: ClusterDetails["kafkaFlavor"];
    protocol: ClusterDetails["protocol"];
    clusterType: ClusterDetails["clusterType"];
    clusterName: ClusterDetails["clusterName"];
    clusterId: ClusterDetails["clusterId"];
  };
}

const ClusterConnectHelpModal = ({
  onClose,
  modalData: { kafkaFlavor, protocol, clusterType, clusterName, clusterId },
}: ClusterConnectHelpModalProps) => {
  const applicationPropertiesString = getApplicationPropertiesString({
    clusterName,
    clusterId,
    kafkaFlavor,
    clusterType,
    protocol,
  });
  return (
    <Modal
      title={"Connect cluster to Klaw"}
      close={onClose}
      primaryAction={{ text: "Done", onClick: onClose }}
    >
      {applicationPropertiesString.length > 0 ? (
        <Spacing gap={"l2"}>
          <Typography.Default>
            Copy, paste, and replace placeholders in{" "}
            <pre style={{ display: "inline" }}>
              klaw/cluster-api/src/main/resources/application.properties
            </pre>
            .
            <br />
            <a
              href="https://www.klaw-project.io/docs/cluster-connectivity-setup/"
              style={{ display: "inline-block" }}
            >
              <Box.Flex alignItems="center" gap={"2"}>
                Learn more <Icon icon={link} />
              </Box.Flex>
            </a>
          </Typography.Default>
          <Box.Flex
            borderColor={"grey-10"}
            backgroundColor={"grey-0"}
            borderRadius={"4px"}
            borderWidth={"1px"}
            justifyContent={"space-between"}
            // flex-direction is set to row-reverse...
            //... so that the copy button is first to be selected when navigating with keyboard
            flexDirection="row-reverse"
            padding={"l1"}
          >
            <ClipBoard
              text={applicationPropertiesString}
              accessibleCopyDescription={
                "Copy application.properties to clipboard"
              }
              accessibleCopiedDescription={
                "Copied application.properties to clipboard"
              }
            />
            <pre>{applicationPropertiesString}</pre>
          </Box.Flex>
        </Spacing>
      ) : (
        <Typography.Default>
          <a
            href="https://www.klaw-project.io/docs/cluster-connectivity-setup/"
            style={{ display: "inline block" }}
          >
            <Box.Flex alignItems="center" gap={"2"}>
              Learn more about cluster connectivity setup <Icon icon={link} />
            </Box.Flex>
          </a>
        </Typography.Default>
      )}
    </Modal>
  );
};

export default ClusterConnectHelpModal;
