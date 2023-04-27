import { createContext, ReactNode, useContext } from "react";
import { useQuery } from "@tanstack/react-query";
import { AuthUser, getAuth } from "src/domain/auth-user";
import { AuthenticationRequiredAlert } from "/src/app/components/AuthenticationRequiredAlert";

// for now, we don't do Authentication on Corals side,
// so we only have a AuthUser in the context
const AuthContext = createContext<AuthUser>({
  canSwitchTeams: "",
  teamId: "",
  teamname: "",
  username: "",
});

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
  } else {
    window.location.assign("/login");

    return (
      <>
        <h1>hello I'm redirected from AuthProvider</h1>
        <AuthenticationRequiredAlert />
      </>
    );
  }
};

export { useAuthContext, AuthProvider };
