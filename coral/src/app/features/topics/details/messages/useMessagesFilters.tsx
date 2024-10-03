import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";

interface FilterErrors {
  partitionIdFilters: string | null;
  customOffsetFilters: string | null;
  rangeOffsetStartFilters: string | null;
  rangeOffsetEndFilters: string | null;
}

const defaultOffsets = ["5", "25", "50", "custom", "range"] as const;
type DefaultOffset = (typeof defaultOffsets)[number];

const fetchModeTypes = ["default", "custom", "range"] as const;
type TopicMessagesFetchModeTypes = (typeof fetchModeTypes)[number];

const NAMES = {
  defaultOffset: "defaultOffset",
  customOffset: "customOffset",
  rangeOffsetStart: "rangeOffsetStart",
  rangeOffsetEnd: "rangeOffsetEnd",
  partitionId: "partitionId",
};
const initialDefaultOffset: (typeof defaultOffsets)[0] = "5";

interface OffsetFilters {
  validateFilters: (totalNumberOfPartitions: number) => boolean;
  filterErrors: FilterErrors;
  getFetchingMode: () => TopicMessagesFetchModeTypes;
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
  rangeOffsetFilters: {
    rangeOffsetStart: string | null;
    setRangeOffsetStart: (rangeOffsetStart: string) => void;
    deleteRangeOffsetStart: () => void;
    rangeOffsetEnd: string | null;
    setRangeOffsetEnd: (rangeOffsetEnd: string) => void;
    deleteRangeOffsetEnd: () => void;
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

  const [filterErrors, setFilterErrors] = useState<FilterErrors>({
    partitionIdFilters: null,
    customOffsetFilters: null,
    rangeOffsetStartFilters: null,
    rangeOffsetEndFilters: null,
  });

  function validateFilters(totalNumberOfPartitions: number) {
    if (getFetchingMode() === "default") {
      return true;
    }

    const partitionIdFiltersError =
      getPartitionId() === "" || getPartitionId() === null
        ? "Please enter a partition ID"
        : Number(getPartitionId()) < 0
          ? "Partition ID cannot be negative"
          : Number(getPartitionId()) >= totalNumberOfPartitions
            ? "Invalid partition ID"
            : null;

    let customOffsetFiltersError = null;
    let rangeOffsetStartFiltersError = null;
    let rangeOffsetEndFiltersError = null;
    if (getFetchingMode() === "custom") {
      customOffsetFiltersError =
        getCustomOffset() === "" || getCustomOffset() === null
          ? "Please enter the number of recent offsets you want to view"
          : Number(getCustomOffset()) > 100
            ? "Entered value exceeds the view limit for offsets: 100"
            : null;
    } else {
      rangeOffsetStartFiltersError =
        getRangeOffsetStart() === "" || getRangeOffsetStart() === null
          ? "Please enter the start offset"
          : Number(getRangeOffsetStart()) < 0
            ? "Start offset cannot be negative."
            : null;
      rangeOffsetEndFiltersError =
        getRangeOffsetEnd() === "" || getRangeOffsetEnd() === null
          ? "Please enter the end offset"
          : Number(getRangeOffsetEnd()) < 0
            ? "End offset cannot be negative."
            : null;

      if (
        rangeOffsetStartFiltersError === null &&
        rangeOffsetEndFiltersError === null &&
        Number(getRangeOffsetStart()) > Number(getRangeOffsetEnd())
      ) {
        rangeOffsetStartFiltersError = "Start must me less than end.";
      }
    }

    setFilterErrors({
      partitionIdFilters: partitionIdFiltersError,
      customOffsetFilters: customOffsetFiltersError,
      rangeOffsetStartFilters: rangeOffsetStartFiltersError,
      rangeOffsetEndFilters: rangeOffsetEndFiltersError,
    });

    return (
      partitionIdFiltersError === null &&
      customOffsetFiltersError === null &&
      rangeOffsetStartFiltersError === null &&
      rangeOffsetEndFiltersError === null
    );
  }

  function getDefaultOffset(): DefaultOffset {
    return searchParams.get(NAMES.defaultOffset) as DefaultOffset;
  }

  function setDefaultOffset(defaultOffset: DefaultOffset): void {
    const oldValue = searchParams.get(NAMES.defaultOffset);
    if (!isDefaultOffset(defaultOffset)) {
      searchParams.set(NAMES.defaultOffset, initialDefaultOffset);
    } else {
      searchParams.set(NAMES.defaultOffset, defaultOffset);
    }
    setFilterErrors({
      partitionIdFilters: null,
      customOffsetFilters: null,
      rangeOffsetStartFilters: null,
      rangeOffsetEndFilters: null,
    });

    // If mode is changing from range to custom or vice versa, then no need to delete partition id
    if (
      (oldValue !== "custom" && oldValue !== "range") ||
      (defaultOffset !== "custom" && defaultOffset !== "range")
    ) {
      deletePartitionId();
    }

    deleteCustomOffset();
    deleteRangeOffsetStart();
    deleteRangeOffsetEnd();
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
        customOffsetFilters:
          "Please enter the number of recent offsets you want to view",
      }));
      deleteCustomOffset();
      return;
    }
    setFilterErrors((prev) => ({
      ...prev,
      customOffsetFilters:
        customOffset === ""
          ? "Please enter the number of recent offsets you want to view"
          : Number(customOffset) > 100
            ? "Entered value exceeds the view limit for offsets: 100"
            : null,
    }));
    searchParams.set(NAMES.customOffset, customOffset);
    deleteRangeOffsetStart();
    deleteRangeOffsetEnd();
    setSearchParams(searchParams);
  }

  function deleteCustomOffset() {
    searchParams.delete(NAMES.customOffset);
    setSearchParams(searchParams);
  }

  function getRangeOffsetStart(): string | null {
    return searchParams.get(NAMES.rangeOffsetStart);
  }

  function setRangeOffsetStart(rangeOffsetStart: string): void {
    if (getDefaultOffset() !== "range") {
      setDefaultOffset("range");
    }
    if (rangeOffsetStart.length === 0) {
      setFilterErrors((prev) => ({
        ...prev,
        rangeOffsetFilters: "Please enter the starting offset",
      }));
      deleteRangeOffsetStart();
      return;
    }
    setFilterErrors((prev) => ({
      ...prev,
      rangeOffsetFilters:
        rangeOffsetStart === "" ? "Please enter the starting offset" : null,
    }));
    searchParams.set(NAMES.rangeOffsetStart, rangeOffsetStart);
    deleteCustomOffset();
    setSearchParams(searchParams);
  }

  function deleteRangeOffsetStart() {
    searchParams.delete(NAMES.rangeOffsetStart);
    setSearchParams(searchParams);
  }

  function getRangeOffsetEnd(): string | null {
    return searchParams.get(NAMES.rangeOffsetEnd);
  }

  function setRangeOffsetEnd(rangeOffsetEnd: string): void {
    if (getDefaultOffset() !== "range") {
      setDefaultOffset("range");
    }
    if (rangeOffsetEnd.length === 0) {
      setFilterErrors((prev) => ({
        ...prev,
        rangeOffsetFilters: "Please enter the ending offset",
      }));
      deleteRangeOffsetEnd();
      return;
    }
    setFilterErrors((prev) => ({
      ...prev,
      rangeOffsetFilters:
        rangeOffsetEnd === "" ? "Please enter the ending offset" : null,
    }));
    searchParams.set(NAMES.rangeOffsetEnd, rangeOffsetEnd);
    deleteCustomOffset();
    setSearchParams(searchParams);
  }

  function deleteRangeOffsetEnd() {
    searchParams.delete(NAMES.rangeOffsetEnd);
    setSearchParams(searchParams);
  }

  function getPartitionId(): string | null {
    return searchParams.get(NAMES.partitionId);
  }

  function setPartitionId(partitionId: string): void {
    if (getDefaultOffset() !== "custom" && getDefaultOffset() !== "range") {
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

  function getFetchingMode(): TopicMessagesFetchModeTypes {
    if (getDefaultOffset() === "custom") {
      return "custom";
    }
    if (getDefaultOffset() === "range") {
      return "range";
    }

    return "default";
  }

  useEffect(() => {
    const toSetDefaultOffset = !isDefaultOffset(
      searchParams.get(NAMES.defaultOffset)
    )
      ? initialDefaultOffset
      : searchParams.get(NAMES.defaultOffset);
    searchParams.set(
      NAMES.defaultOffset,
      toSetDefaultOffset === null ? initialDefaultOffset : toSetDefaultOffset
    );
    setSearchParams(searchParams);
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
    rangeOffsetFilters: {
      rangeOffsetStart: getRangeOffsetStart(),
      setRangeOffsetStart,
      deleteRangeOffsetStart,
      rangeOffsetEnd: getRangeOffsetEnd(),
      setRangeOffsetEnd,
      deleteRangeOffsetEnd,
    },
    partitionIdFilters: {
      partitionId: getPartitionId(),
      setPartitionId,
      deletePartitionId,
    },
  };
}

export {
  defaultOffsets,
  isDefaultOffset,
  useMessagesFilters,
  type DefaultOffset,
  type FilterErrors,
  type TopicMessagesFetchModeTypes,
};
