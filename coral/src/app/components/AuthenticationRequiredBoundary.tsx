import { Component, ReactElement } from "react";
import { isUnauthorizedError } from "src/services/api";
import { AuthenticationRequiredAlert } from "src/app/components/AuthenticationRequiredAlert";

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

      return <AuthenticationRequiredAlert />;
    }

    return this.props.children;
  }
}

export default AuthenticationRequiredBoundary;
