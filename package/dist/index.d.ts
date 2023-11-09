import * as _tanstack_react_query from '@tanstack/react-query';
import * as react_jsx_runtime from 'react/jsx-runtime';
import React from 'react';

declare const useKlaw: ({ getTopicsCall, }: {
    getTopicsCall: (args: unknown) => Promise<unknown[]>;
}) => {
    topics: _tanstack_react_query.UseQueryResult<unknown[], unknown>;
};

interface Topic {
    topicName: string;
    topicId: number;
}
interface TopicListProps {
    topics: Topic[];
    ariaLabel: string;
}
declare const TopicsTable: (props: TopicListProps) => react_jsx_runtime.JSX.Element;

declare const KlawProvider: ({ children }: React.PropsWithChildren) => react_jsx_runtime.JSX.Element;

export { KlawProvider, TopicsTable, useKlaw };
