import { Navigate, NavigateProps } from "react-router-dom";

type Props = Omit<NavigateProps, "to"> & {
  to: `/${string}`;
  useLegacy: boolean;
};

/**
 * CoralNavigate is a convenience component, which provides unified API
 * to do navigation either with React Router or by using window.location API
 *
 * Once the Reactification is complete, we should migrate all instances of
 * <CoralNavigate /> to use the React Router <Navigate />
 */
function CoralNavigate({ useLegacy, to, ...navigateProps }: Props) {
  if (useLegacy) {
    window.location.assign(to);
    return <></>;
  }
  return <Navigate to={to} {...navigateProps} />;
}

export default CoralNavigate;
