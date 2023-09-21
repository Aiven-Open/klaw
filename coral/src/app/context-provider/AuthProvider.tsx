import { createContext, ReactNode, useContext } from "react";
import { useQuery } from "@tanstack/react-query";
import { AuthUser, getAuth } from "src/domain/auth-user";
import { BasePage } from "src/app/layout/page/BasePage";
import { Box, Icon } from "@aivenio/aquarium";
import loading from "@aivenio/aquarium/icons/loading";

/** We don't do Authentication on Corals side
 * at the moment, so we only have a AuthUser
 * in the context
 * */
const AuthContext = createContext<AuthUser | undefined>(undefined);

const useAuthContext = () => useContext(AuthContext);

const AuthProvider = ({ children }: { children: ReactNode }) => {
  const { data: authUser, isLoading } = useQuery<AuthUser | undefined>(
    ["user-getAuth-data"],
    getAuth
  );

  if (!isLoading && authUser) {
    return (
      <AuthContext.Provider value={authUser}>{children}</AuthContext.Provider>
    );
  }

  return (
    <BasePage
      content={
        <Box.Flex paddingTop={"l2"} display={"flex"} justifyContent={"center"}>
          <div className={"visually-hidden"}>Loading Klaw</div>
          <Icon icon={loading} fontSize={"30px"} />
        </Box.Flex>
      }
    />
  );
};

export { useAuthContext, AuthProvider };
