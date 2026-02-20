declare module 'react-test-renderer' {
  import type React from 'react';

  export type ReactTestRenderer = {
    update(element: React.ReactElement): void;
    unmount(): void;
  };

  export function create(element: React.ReactElement): ReactTestRenderer;
  export function act(callback: () => void | Promise<void>): void | Promise<void>;
}
