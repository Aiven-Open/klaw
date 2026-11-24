import { isValidElement, ReactElement } from "react";
import { RouteObject } from "react-router-dom";
import RequestModal from "src/app/layout/request-modal/RequestModal";
import { requestModalRoute } from "src/app/layout/request-modal/request-modal-route";
import { Routes } from "src/services/router-utils/types";

describe("requestModalRoute", () => {
  const TestElement = <div data-testid="test-element">test element</div>;

  function getModalProps(routeObject: RouteObject) {
    const element = routeObject.element as ReactElement;
    expect(isValidElement(element)).toBe(true);
    expect(element.type).toBe(RequestModal);
    return element.props as {
      title: unknown;
      confirmTitle: string;
      children: unknown;
    };
  }

  it("sets the right path and wraps the element in a RequestModal", () => {
    const routeObject = requestModalRoute({
      path: Routes.TOPIC_REQUEST,
      element: TestElement,
      title: "Request topic",
      confirmTitle: "Cancel topic request?",
    });

    expect(routeObject.path).toEqual(Routes.TOPIC_REQUEST);
    const props = getModalProps(routeObject);
    expect(props.title).toEqual("Request topic");
    expect(props.confirmTitle).toEqual("Cancel topic request?");
    expect(props.children).toEqual(TestElement);
  });

  it("passes the route params to a title function", () => {
    const routeObject = requestModalRoute({
      path: Routes.TOPIC_SCHEMA_REQUEST,
      element: TestElement,
      title: ({ topicName }) => `Schema for ${topicName}`,
      confirmTitle: "Cancel schema request?",
    });

    const { title } = getModalProps(routeObject);

    expect(typeof title === "function" && title({ topicName: "abc" })).toEqual(
      "Schema for abc"
    );
  });
});
