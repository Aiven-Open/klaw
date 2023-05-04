import { createContext, ReactNode, useContext } from "react";
import { useQuery } from "@tanstack/react-query";
import { AuthUser, getAuth } from "src/domain/auth-user";
import Header from "src/app/layout/header/Header";
import { Box, GridItem } from "@aivenio/aquarium";

// for now, we don't do Authentication on Corals side,
// so we only have a AuthUser in the context
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
    <Box height={"l5"} backgroundColor={"primary-80"} paddingX={"l2"}>
      <Header />
    </Box>
  );
};

export { useAuthContext, AuthProvider };
