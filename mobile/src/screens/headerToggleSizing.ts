export type WebBreakpoint = 'mobile' | 'narrow' | 'medium' | 'wide';

type HeaderToggleSizingInput = {
  isWeb: boolean;
  breakpoint: WebBreakpoint;
  numColumns: 1 | 2 | 3;
};

export type HeaderToggleSizing = {
  buttonSize: number;
  iconSize: number;
  emphasisIconSize: number;
  actionGap: number;
};

function clamp(value: number, min: number, max: number): number {
  return Math.max(min, Math.min(max, value));
}

export function getHeaderToggleSizing({
  isWeb,
  breakpoint,
  numColumns,
}: HeaderToggleSizingInput): HeaderToggleSizing {
  const baseByBreakpoint = isWeb
    ? breakpoint === 'wide'
      ? { buttonSize: 44, iconSize: 20, actionGap: 10 }
      : breakpoint === 'medium'
        ? { buttonSize: 42, iconSize: 19, actionGap: 8 }
        : { buttonSize: 40, iconSize: 18, actionGap: 6 }
    : { buttonSize: 38, iconSize: 18, actionGap: 8 };

  const modeScale = isWeb && numColumns === 1 ? 0.9 : isWeb && numColumns === 2 ? 0.92 : 1;

  const buttonSize = clamp(Math.round(baseByBreakpoint.buttonSize * modeScale), 36, 44);
  const iconSize = clamp(Math.round(baseByBreakpoint.iconSize * modeScale), 16, 20);
  const emphasisIconSize = clamp(iconSize + 2, 18, 22);
  const actionGap = clamp(Math.round(baseByBreakpoint.actionGap * (numColumns < 3 ? 0.9 : 1)), 6, 10);

  return {
    buttonSize,
    iconSize,
    emphasisIconSize,
    actionGap,
  };
}

export function calculateHeaderActionRowWidth(sizing: HeaderToggleSizing, actionCount: number = 3): number {
  if (actionCount <= 0) return 0;
  return (sizing.buttonSize * actionCount) + (sizing.actionGap * (actionCount - 1));
}
