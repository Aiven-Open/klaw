import { useQuery } from "@tanstack/react-query";

export const useKlaw = ({
  getTopicsCall,
}: {
  getTopicsCall: (args: unknown) => Promise<unknown[]>;
}) => {
  const topics = useQuery({
    queryKey: ["getTopics"],
    queryFn: () =>
      getTopicsCall({
        organizationId: "1",
      }),
    keepPreviousData: true,
  });

  return { topics };
};
