import { NativeSelect, NativeSelectProps, useToast } from "@aivenio/aquarium";
import { isValidElement, ReactElement, ReactNode, useEffect } from "react";
import { parseErrorMsg } from "src/services/mutation-utils";
import { isDevMode } from "src/services/is-dev-mode";

function isNativeSelectComponent(
  child: ReactNode
): child is ReactElement<NativeSelectProps> {
  return (
    isValidElement(child) &&
    /* eslint-disable-next-line  @typescript-eslint/no-explicit-any */
    (child.type as any)?.render?.displayName === NativeSelect.displayName
  );
}

type AsyncNativeSelectWrapperProps = {
  /**
   * `entity` is the entity which user filters by, e.g. "Team" or "Topic"
   * It is used in e.g. placeholder and toast notification for error cases,
   * like "Error loading <ENTITY>"
   */
  entity: string;
  isLoading: boolean;
  isError: boolean;
  error: unknown;
  children: ReactElement<NativeSelectProps>;
};

/** <AsyncNativeSelectWrapper> handles loading
 * and error states for a <NativeSelect> that uses
 * data that is fetched async (e.g. from API)
 * only takes one <NativeSelect /> as a child.
 */
function AsyncNativeSelectWrapper(props: AsyncNativeSelectWrapperProps) {
  const { entity, isLoading, isError, error, children } = props;

  const toast = useToast();

  // Type-guard to make sure we're only passing
  // <NativeSelect> as a child component
  useEffect(() => {
    if (!isNativeSelectComponent(children)) {
      const errorMessage =
        "Invalid child component. `AsyncNativeSelectWrapper` only accepts `NativeSelect` as a child.";

      if (isDevMode()) {
        throw new Error(errorMessage);
      } else {
        console.error(errorMessage);
      }
    }
  }, [children]);

  useEffect(() => {
    if (isError) {
      toast({
        message: `Error loading ${entity}: ${parseErrorMsg(error)}`,
        position: "bottom-left",
        variant: "default",
      });
    }
  }, [isError]);

  if (isError) {
    return (
      <NativeSelect
        valid={false}
        disabled={true}
        labelText={children.props.labelText}
        aria-label={`No ${entity}`}
        placeholder={`No ${entity}`}
        helperText={`${entity} could not be loaded.`}
      />
    );
  }

  if (isLoading) {
    return (
      <div data-testid={"async-select-loading"}>
        <NativeSelect.Skeleton />
      </div>
    );
  }

  return children;
}

export { AsyncNativeSelectWrapper };
