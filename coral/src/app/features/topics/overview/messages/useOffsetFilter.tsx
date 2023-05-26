import { useEffect } from "react";
import { useSearchParams } from "react-router-dom";

const offsets = ["5", "25", "50"] as const;
type Offset = typeof offsets[number];

const NAME = "offset";
const defaultOffset: typeof offsets[0] = "5";

function isOffset(offset: string | null): offset is Offset {
  return Boolean(offset && offsets.includes(offset as Offset));
}

function useOffsetFilter(): [Offset, (offset: Offset) => void] {
  const [searchParams, setSearchParams] = useSearchParams();
  const offset = searchParams.get(NAME);

  useEffect(() => {
    if (!isOffset(offset)) {
      searchParams.set(NAME, defaultOffset);
      setSearchParams(searchParams);
    }
  }, [offset]);

  function getOffset(): Offset {
    return isOffset(offset) ? offset : defaultOffset;
  }

  function setOffset(offset: string): void {
    if (!isOffset(offset)) {
      searchParams.set(NAME, defaultOffset);
    } else {
      searchParams.set(NAME, offset);
    }
    setSearchParams(searchParams);
  }

  return [getOffset(), setOffset];
}

export { useOffsetFilter, offsets, isOffset, type Offset };
