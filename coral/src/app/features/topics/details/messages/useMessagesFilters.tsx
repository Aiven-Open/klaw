import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";

interface FilterErrors {
  partitionIdFilters: string | null;
  customOffsetFilters: string | null;
}

const defaultOffsets = ["5", "25", "50", "custom"] as const;
type DefaultOffset = (typeof defaultOffsets)[number];

const NAMES = {
  defaultOffset: "defaultOffset",
  customOffset: "customOffset",
  partitionId: "partitionId",
};
const initialDefaultOffset: (typeof defaultOffsets)[0] = "5";

interface OffsetFilters {
  validateFilters: () => boolean;
  filterErrors: FilterErrors;
  getFetchingMode: () => "Custom" | "Default";
  defaultOffsetFilters: {
    defaultOffset: DefaultOffset;
    setDefaultOffset: (defaultOffset: DefaultOffset) => void;
    deleteDefaultOffset: () => void;
  };
  customOffsetFilters: {
    customOffset: string | null;
    setCustomOffset: (customOffset: string) => void;
    deleteCustomOffset: () => void;
  };
  partitionIdFilters: {
    partitionId: string | null;
    setPartitionId: (partitionId: string) => void;
    deletePartitionId: () => void;
  };
}

function isDefaultOffset(
  defaultOffset: string | null
): defaultOffset is DefaultOffset {
  return Boolean(
    defaultOffset && defaultOffsets.includes(defaultOffset as DefaultOffset)
  );
}

function useMessagesFilters(): OffsetFilters {
  const [searchParams, setSearchParams] = useSearchParams();
  const defaultOffset = searchParams.get(NAMES.defaultOffset);

  const [filterErrors, setFilterErrors] = useState<FilterErrors>({
    partitionIdFilters: null,
    customOffsetFilters: null,
  });

  function validateFilters() {
    if (getFetchingMode() === "Default") {
      return true;
    }

    const partitionIdFiltersError =
      getPartitionId() === "" || getPartitionId() === null
        ? "Please enter a partition ID"
        : null;
    const customOffsetFiltersError =
      getCustomOffset() === "" || getCustomOffset() === null
        ? "Please enter an offset"
        : Number(getCustomOffset()) > 100
        ? "Max offset: 100"
        : null;

    setFilterErrors({
      partitionIdFilters: partitionIdFiltersError,
      customOffsetFilters: customOffsetFiltersError,
    });

    return (
      partitionIdFiltersError === null && customOffsetFiltersError === null
    );
  }

  function getDefaultOffset(): DefaultOffset {
    return isDefaultOffset(defaultOffset)
      ? defaultOffset
      : initialDefaultOffset;
  }

  function setDefaultOffset(defaultOffset: DefaultOffset): void {
    if (!isDefaultOffset(defaultOffset)) {
      searchParams.set(NAMES.defaultOffset, initialDefaultOffset);
    } else {
      searchParams.set(NAMES.defaultOffset, defaultOffset);
    }
    setFilterErrors({
      partitionIdFilters: null,
      customOffsetFilters: null,
    });
    deletePartitionId();
    deleteCustomOffset();
    setSearchParams(searchParams);
  }

  function deleteDefaultOffset() {
    searchParams.delete("defaultOffset");
    setSearchParams(searchParams);
  }

  function getCustomOffset(): string | null {
    return searchParams.get(NAMES.customOffset);
  }

  function setCustomOffset(customOffset: string): void {
    if (getDefaultOffset() !== "custom") {
      setDefaultOffset("custom");
    }
    if (customOffset.length === 0) {
      setFilterErrors((prev) => ({
        ...prev,
        customOffsetFilters: "Please enter an offset",
      }));
      deleteCustomOffset();
      return;
    }
    setFilterErrors((prev) => ({
      ...prev,
      customOffsetFilters:
        customOffset === ""
          ? "Please enter an offset"
          : Number(customOffset) > 100
          ? "Max offset: 100"
          : null,
    }));
    searchParams.set(NAMES.customOffset, customOffset);
    setSearchParams(searchParams);
  }

  function deleteCustomOffset() {
    searchParams.delete(NAMES.customOffset);
    setSearchParams(searchParams);
  }

  function getPartitionId(): string | null {
    return searchParams.get(NAMES.partitionId);
  }

  function setPartitionId(partitionId: string): void {
    if (getDefaultOffset() !== "custom") {
      setDefaultOffset("custom");
    }
    if (partitionId.length === 0) {
      setFilterErrors((prev) => ({
        ...prev,
        partitionIdFilters: "Please enter a partition ID",
      }));
      deletePartitionId();
      return;
    }

    setFilterErrors((prev) => ({
      ...prev,
      partitionIdFilters:
        partitionId === "" ? "Please enter a partition ID" : null,
    }));
    searchParams.set(NAMES.partitionId, partitionId);
    setSearchParams(searchParams);
  }

  function deletePartitionId() {
    searchParams.delete(NAMES.partitionId);
    setSearchParams(searchParams);
  }

  function getFetchingMode() {
    if (defaultOffset === "custom") {
      return "Custom";
    }

    return "Default";
  }

  useEffect(() => {
    if (defaultOffset !== "custom" || defaultOffset === null) {
      searchParams.set(
        NAMES.defaultOffset,
        defaultOffset === null ? initialDefaultOffset : defaultOffset
      );
      setSearchParams(searchParams);
    }
  }, []);

  return {
    validateFilters,
    filterErrors,
    getFetchingMode: useMemo(() => getFetchingMode, []),
    defaultOffsetFilters: {
      defaultOffset: getDefaultOffset(),
      setDefaultOffset,
      deleteDefaultOffset,
    },
    customOffsetFilters: {
      customOffset: getCustomOffset(),
      setCustomOffset,
      deleteCustomOffset,
    },
    partitionIdFilters: {
      partitionId: getPartitionId(),
      setPartitionId,
      deletePartitionId,
    },
  };
}

export {
  useMessagesFilters,
  defaultOffsets,
  isDefaultOffset,
  type DefaultOffset,
  type FilterErrors,
};
