import {
  Controller as _Controller,
  useFormContext,
  UseFormSetError,
} from "react-hook-form";
import Editor, { Monaco, useMonaco } from "@monaco-editor/react";
import { useQuery } from "@tanstack/react-query";
import isString from "lodash/isString";
import { Position, editor } from "monaco-editor";
import { useEffect, useRef } from "react";
import { getTopicAdvanvedConfigOptions } from "src/domain/topic/topic-api";
import { TopicAdvancedConfigurationOptions } from "src/domain/topic/topic-types";
import { Schema } from "src/app/features/topics/request/schemas/topic-request-form";
import { BorderBox, Box, Flexbox, Typography } from "@aivenio/aquarium";

type Props = {
  name: "advancedConfiguration";
};

function AdvancedConfiguration({ name }: Props) {
  const setModelMarkers = useRef<typeof editor.setModelMarkers>();
  const monacoEditor = useRef<editor.IStandaloneCodeEditor>();
  const { setValue, setError, control, getValues } = useFormContext<Schema>();
  const monaco = useMonaco();

  const { data: advancedConfigurationOptions } = useQuery({
    queryKey: ["getTopicAdvanvedConfigOptions"],
    queryFn: getTopicAdvanvedConfigOptions,
  });

  useEffect(
    () => setupAutocompleteAndTooltips(monaco, advancedConfigurationOptions),
    [monaco, advancedConfigurationOptions]
  );

  useEffect(() => {
    validateUnknownConfigurationKeys({
      value: getValues("advancedConfiguration"),
      advancedConfigurationOptions,
      setModelMarkers: setModelMarkers.current,
      monacoEditor: monacoEditor.current,
      setError,
    });
  }, [
    getValues("advancedConfiguration"),
    advancedConfigurationOptions,
    setModelMarkers,
    monacoEditor,
    setError,
  ]);

  function handleEditorValidation(markers: editor.IMarker[]) {
    if (markers.length > 0) {
      setError("advancedConfiguration", {
        message: markers[0].message,
        type: "custom",
      });
    }
  }

  function handleEditorMount(
    editor: editor.IStandaloneCodeEditor,
    monaco: Monaco
  ) {
    setModelMarkers.current = monaco.editor.setModelMarkers;
    monacoEditor.current = editor;
  }

  return (
    <Box component={Flexbox} gap={"l1"} direction={"column"}>
      <Typography.Subheading>
        Advanced Topic Configuration
      </Typography.Subheading>
      <Typography.Caption>
        Instead of hiding instructions, we could have those inline here. For the
        supported values refer to official{" "}
        <a
          href="https://kafka.apache.org/documentation/#topicconfigs"
          target="_blank"
          rel="noreferrer"
        >
          Apache Kafka Documentation
        </a>{" "}
      </Typography.Caption>
      <BorderBox borderColor="grey-20">
        <_Controller
          name={name}
          control={control}
          render={({ field: { name, value } }) => {
            return (
              <>
                <Editor
                  data-testid="advancedConfiguration"
                  height="300px"
                  defaultLanguage={"json"}
                  value={value}
                  defaultValue={"{\n}"}
                  onMount={handleEditorMount}
                  onChange={(value) => {
                    if (isString(value)) {
                      setValue(name, value, {
                        shouldValidate: true,
                        shouldTouch: true,
                        shouldDirty: true,
                      });
                    }
                  }}
                  onValidate={handleEditorValidation}
                  options={{
                    minimap: { enabled: false },
                    folding: false,
                    lineNumbers: "off",
                    scrollbar: {
                      vertical: "hidden",
                      horizontal: "hidden",
                      handleMouseWheel: false,
                    },
                  }}
                />
              </>
            );
          }}
        />
      </BorderBox>
    </Box>
  );
}

function setupAutocompleteAndTooltips(
  monaco: ReturnType<typeof useMonaco>,
  advancedConfigurationOptions: TopicAdvancedConfigurationOptions[] | undefined
) {
  if (monaco && advancedConfigurationOptions !== undefined) {
    const completionProvider = monaco.languages.registerCompletionItemProvider(
      "json",
      {
        provideCompletionItems: autocompleteOptionNames(
          advancedConfigurationOptions
        ),
      }
    );
    const hoverProvider = monaco.languages.registerHoverProvider("json", {
      provideHover: wordMatchesOptionName(advancedConfigurationOptions),
    });

    return () => {
      completionProvider.dispose();
      hoverProvider.dispose();
    };
  }
}

function getRange(model: editor.ITextModel, position: Position) {
  const word = model.getWordUntilPosition(position);
  return {
    startLineNumber: position.lineNumber,
    endLineNumber: position.lineNumber,
    startColumn: word.startColumn,
    endColumn: word.endColumn,
  };
}

const autocompleteOptionNames = (
  options: TopicAdvancedConfigurationOptions[]
) => {
  return (model: editor.ITextModel, position: Position) => {
    const range = getRange(model, position);
    return {
      suggestions: options
        .filter((option) => !model.getValue().includes(`"${option}"`))
        .map((option) => {
          return {
            label: `"${option.name}"`,
            range: range,
            kind: 9, //languages.CompletionItemKind.Property
            insertText: `"${option.name}": "",`,
          };
        }),
    };
  };
};

const wordMatchesOptionName = (
  options: TopicAdvancedConfigurationOptions[]
) => {
  return (model: editor.ITextModel, position: Position) => {
    const wordAtPosition = model.getWordAtPosition(position);
    if (wordAtPosition && isString(wordAtPosition.word)) {
      const configuration = options.find((o) => o.name === wordAtPosition.word);
      if (configuration !== undefined) {
        return {
          range: getRange(model, position),
          contents: [
            { value: `**${configuration.name}**` },
            ...(configuration.documentation
              ? [
                  { value: configuration.documentation?.text },
                  {
                    value: configuration.documentation?.link,
                  },
                ]
              : []),
          ],
        };
      }
    }
  };
};

function getUnknownConfigKeys(
  value: string,
  options: TopicAdvancedConfigurationOptions[]
): string[] {
  try {
    const valueAsObject = JSON.parse(value);
    const supportedKeys = new Set(options.map((o) => o.name));
    return Object.keys(valueAsObject).filter(
      (name) => !supportedKeys.has(name)
    );
  } catch {
    return [];
  }
}

function findMatchesFromModel(
  keys: string[],
  model: editor.IModel
): { configName: string; matches: editor.FindMatch[] }[] {
  return keys
    .map((configName) => ({
      configName,
      matches: model.findMatches(
        configName, // searchString
        true, // searchOnlyEditableRange
        false, // isRegex
        true, // matchCase
        null, // wordSeparators
        true // captureMatches
      ),
    }))
    .filter(({ matches }) => matches.length > 0);
}

function unknownConfigKeyIntoMarker(
  configKey: string,
  match: editor.FindMatch
): editor.IMarkerData {
  return {
    severity: 8, // https://microsoft.github.io/monaco-editor/api/enums/monaco.MarkerSeverity.html#Error
    message: `"${configKey}" is an unknown topic configuration`,
    startLineNumber: match.range.startLineNumber,
    startColumn: match.range.startColumn,
    endLineNumber: match.range.endLineNumber,
    endColumn: match.range.endColumn,
  };
}

type ValidateUnknownConfigurationKeysArgs = {
  value: string | undefined;
  advancedConfigurationOptions: TopicAdvancedConfigurationOptions[] | undefined;
  setModelMarkers: typeof editor.setModelMarkers | undefined;
  monacoEditor: editor.IStandaloneCodeEditor | undefined;
  setError: UseFormSetError<Schema>;
};

const validateUnknownConfigurationKeys = ({
  value,
  advancedConfigurationOptions,
  setModelMarkers,
  monacoEditor,
}: ValidateUnknownConfigurationKeysArgs) => {
  if (
    value !== undefined &&
    advancedConfigurationOptions !== undefined &&
    setModelMarkers !== undefined &&
    monacoEditor !== undefined
  ) {
    const unknownConfigKeys = getUnknownConfigKeys(
      value,
      advancedConfigurationOptions
    );
    const model = monacoEditor.getModel();
    if (model) {
      const markers = findMatchesFromModel(unknownConfigKeys, model)
        .map(({ configName, matches }) =>
          matches.map((match) => unknownConfigKeyIntoMarker(configName, match))
        )
        .reduce((acc, curr) => [...acc, ...curr], []);
      setModelMarkers(model, "custom-component-validation", markers);
    }
  }
};

export default AdvancedConfiguration;
