import React from 'react';
import { act, create, type ReactTestRenderer } from 'react-test-renderer';

type RenderHookResult<T> = {
  result: { current: T };
  rerender: () => void;
  unmount: () => void;
};

// Minimal hook renderer for node-based unit tests.
export function renderHook<T>(hook: () => T): RenderHookResult<T> {
  const result = { current: undefined as unknown as T };

  const TestComponent = () => {
    result.current = hook();
    return null;
  };

  let renderer: ReactTestRenderer;

  act(() => {
    renderer = create(React.createElement(TestComponent));
  });

  return {
    result,
    rerender: () => {
      act(() => {
        renderer.update(React.createElement(TestComponent));
      });
    },
    unmount: () => {
      act(() => {
        renderer.unmount();
      });
    },
  };
}
