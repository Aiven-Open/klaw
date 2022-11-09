import { Flexbox } from "@aivenio/design-system";

const SideNavigation = () => {
  // The followin style is to illustrate sidenav height
  const style = { border: "1px solid red" };
  return (
    <Flexbox htmlTag="nav" style={style} height={"full"}>
      <ul>
        <a href="#">
          <li>navigation item 1</li>
        </a>
        <a href="#">
          <li>navigation item 2</li>
        </a>
        <a href="#">
          <li>navigation item 3</li>
        </a>
      </ul>
    </Flexbox>
  );
};

export default SideNavigation;
