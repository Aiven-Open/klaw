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
      return <div>Redirecting to login...</div>;
    }

    return this.props.children;
  }
}

export default AuthenticationRequiredBoundary;
