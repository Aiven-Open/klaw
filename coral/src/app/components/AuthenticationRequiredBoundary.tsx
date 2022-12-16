import { Box, Typography } from "@aivenio/aquarium";
import { Component, ReactElement } from "react";
import { isUnauthorizedError } from "src/services/api";

class AuthenticationRequiredBoundary extends Component<{
  children: ReactElement;
}> {
  public state = {
    hasError: false,
  };
  constructor(props: { children: ReactElement }) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(error: unknown) {
    if (isUnauthorizedError(error)) {
      return { hasError: true };
    }
  }

  render() {
    if (this.state.hasError) {
      window.location.assign("/login");
      return (
        <Box
          role="alertdialog"
          aria-labelledby={"authentication-required-heading"}
          aria-describedby={"authentication-required-text"}
          display={"flex"}
          flexDirection={"column"}
          paddingTop={"l6"}
          justifyContent={"center"}
          alignItems={"center"}
        >
          <Typography.Heading color={"secondary-100"}>
            <span id={"authentication-required-heading"}>
              Authentication session expired
            </span>
          </Typography.Heading>

          <Typography.LargeText>
            <span id={"authentication-required-text"}>
              Redirecting to login page.
            </span>
          </Typography.LargeText>
        </Box>
      );
    }

    return this.props.children;
  }
}

export default AuthenticationRequiredBoundary;
