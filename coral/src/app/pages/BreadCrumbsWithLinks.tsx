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

export const BreadCrumbsWithLinks = ({ paths }: { paths: string[] }) => {
  const crumbs = paths.map((item) => {
    return (
      <LinkCrumb key={item} to={`/${item.toLowerCase()}`}>
        {item}
      </LinkCrumb>
    );
  });

  return <Breadcrumbs>{crumbs}</Breadcrumbs>;
};
