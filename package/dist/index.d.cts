import * as react_jsx_runtime from 'react/jsx-runtime';
import React from 'react';

interface TopicListProps {
    ariaLabel: string;
    params: unknown;
}
declare const TopicsTable: (props: TopicListProps) => react_jsx_runtime.JSX.Element;

interface Sources {
    getTopics: (params: unknown) => Promise<{
        topics: {
            topicName: string;
        }[];
    }>;
}
interface KlawProviderProps {
    sources: Sources;
}
declare const KlawProvider: ({ children, sources, }: React.PropsWithChildren<KlawProviderProps>) => react_jsx_runtime.JSX.Element;

export { KlawProvider, TopicsTable };
