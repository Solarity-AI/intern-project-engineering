import {
  calculateHeaderActionRowWidth,
  getHeaderToggleSizing,
} from '../screens/headerToggleSizing';

describe('wishlist header toggle sizing', () => {
  it('scales controls down for single and dual web column modes', () => {
    const wideThreeCol = getHeaderToggleSizing({
      isWeb: true,
      breakpoint: 'wide',
      numColumns: 3,
    });
    const narrowSingleCol = getHeaderToggleSizing({
      isWeb: true,
      breakpoint: 'narrow',
      numColumns: 1,
    });
    const mediumDualCol = getHeaderToggleSizing({
      isWeb: true,
      breakpoint: 'medium',
      numColumns: 2,
    });

    expect(narrowSingleCol.buttonSize).toBeLessThan(wideThreeCol.buttonSize);
    expect(mediumDualCol.buttonSize).toBeLessThan(wideThreeCol.buttonSize);
    expect(narrowSingleCol.iconSize).toBeLessThan(wideThreeCol.iconSize);
  });

  it('keeps sizes constrained to stable ranges', () => {
    const cases: Array<Parameters<typeof getHeaderToggleSizing>[0]> = [
      { isWeb: true, breakpoint: 'narrow', numColumns: 1 },
      { isWeb: true, breakpoint: 'medium', numColumns: 2 },
      { isWeb: true, breakpoint: 'wide', numColumns: 3 },
      { isWeb: false, breakpoint: 'mobile', numColumns: 1 },
      { isWeb: false, breakpoint: 'mobile', numColumns: 2 },
    ];

    for (const input of cases) {
      const sizing = getHeaderToggleSizing(input);
      expect(sizing.buttonSize).toBeGreaterThanOrEqual(36);
      expect(sizing.buttonSize).toBeLessThanOrEqual(44);
      expect(sizing.iconSize).toBeGreaterThanOrEqual(16);
      expect(sizing.iconSize).toBeLessThanOrEqual(20);
      expect(sizing.emphasisIconSize).toBeGreaterThanOrEqual(18);
      expect(sizing.emphasisIconSize).toBeLessThanOrEqual(22);
      expect(sizing.actionGap).toBeGreaterThanOrEqual(6);
      expect(sizing.actionGap).toBeLessThanOrEqual(10);
    }
  });

  it('keeps narrow single and dual mode header actions compact enough to avoid overflow regressions', () => {
    const narrowSingleCol = getHeaderToggleSizing({
      isWeb: true,
      breakpoint: 'narrow',
      numColumns: 1,
    });
    const narrowDualCol = getHeaderToggleSizing({
      isWeb: true,
      breakpoint: 'narrow',
      numColumns: 2,
    });

    expect(calculateHeaderActionRowWidth(narrowSingleCol)).toBeLessThanOrEqual(124);
    expect(calculateHeaderActionRowWidth(narrowDualCol)).toBeLessThanOrEqual(124);
  });

  it('keeps the destructive action icon visually one step larger than base icon', () => {
    const sizing = getHeaderToggleSizing({
      isWeb: true,
      breakpoint: 'medium',
      numColumns: 2,
    });

    expect(sizing.emphasisIconSize - sizing.iconSize).toBe(2);
  });
});
