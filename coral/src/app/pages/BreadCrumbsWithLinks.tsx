import { Breadcrumbs, asCrumb } from "@aivenio/aquarium";
import { Link, LinkProps } from "react-router-dom";

const LinkCrumb = asCrumb<HTMLAnchorElement, LinkProps>(
  // asCrumb assserts a very specific return type which does not fit when passing a react-router-dom v6 LInk
  // This will be fixed in aquarium, and we can then remove the following two lines
  // eslint-disable-next-line @typescript-eslint/ban-ts-comment
  //@ts-ignore
  Link,
  "LinkCrumb"
);

export const BreadCrumbsWithLinks = ({
  breadcrumbs,
}: {
  breadcrumbs: { path: string; name: string }[];
}) => {
  const crumbs = breadcrumbs.map(({ path, name }) => {
    return (
      <LinkCrumb key={`${path}-${name}`} to={`/${path}`}>
        {name}
      </LinkCrumb>
    );
  });

  return <Breadcrumbs>{crumbs}</Breadcrumbs>;
};
