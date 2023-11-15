import { createContext, ReactNode, useContext } from "react";
import { TopicApiResponse } from "src/domain/topic/topic-types";
import { Environment } from "src/domain/environment";
import { KlawApiResponse } from "types/utils";

type ApiConfig = {
  getTopics: (params: {
    pageNo: string;
    env: string;
    teamId?: number;
    topicnamesearch?: string;
  }) => Promise<TopicApiResponse>;
  getAllEnvironmentsForTopicAndAcl: () => Promise<Environment[]>;
  getAllEnvironmentsForConnector: () => Promise<Environment[]>;
  getTeams: () => Promise<KlawApiResponse<"getAllTeamsSU">>;
};

const ApiContext = createContext<ApiConfig | undefined>(undefined);

const useApiConfig = () => {
  const context = useContext(ApiContext);
  if (!context) {
    throw new Error("useApiConfig must be used within an ApiProvider!");
  }
  return context;
};

function ApiProvider({
  config,
  children,
}: {
  config: ApiConfig;
  children: ReactNode;
}) {
  return <ApiContext.Provider value={config}>{children}</ApiContext.Provider>;
}

export { useApiConfig, ApiProvider };
export type { ApiConfig };
