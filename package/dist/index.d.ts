import * as react_jsx_runtime from 'react/jsx-runtime';
import React from 'react';

interface TopicListProps<ParamsType extends Record<string, any>> {
    ariaLabel: string;
    params: ParamsType;
}
declare const TopicsTable: <ParamsType extends Record<string, any>>(props: TopicListProps<ParamsType>) => react_jsx_runtime.JSX.Element;

interface Sources {
    getTopics: <ReturnType extends {
        [key: string]: string;
        topicName: string;
    }, ParamType extends {
        [key: string]: number | string;
    }>(params: ParamType) => Promise<{
        topics: ReturnType[];
    }>;
}
interface KlawProviderProps {
    sources: Sources;
}
declare const KlawProvider: ({ children, sources, }: React.PropsWithChildren<KlawProviderProps>) => react_jsx_runtime.JSX.Element;

export { KlawProvider, TopicsTable };
