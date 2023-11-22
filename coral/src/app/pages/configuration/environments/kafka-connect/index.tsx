import { PageHeader } from "@aivenio/aquarium";
import KafkaConnectEnvironments from "src/app/features/configuration/environments/KafkaConnect/KafkaConnectEnvironments";

const KafkaConnectEnvironmentsPage = () => {
  return (
    <>
      <PageHeader title={"Kafka Connect Environments"} />
      <KafkaConnectEnvironments />
    </>
  );
};

export default KafkaConnectEnvironmentsPage;
