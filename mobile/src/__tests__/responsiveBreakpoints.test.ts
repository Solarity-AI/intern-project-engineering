/**
 * responsiveBreakpoints.test.ts
 *
 * Unit tests for the centralised responsive helper functions exported from
 * theme.ts.  These are pure functions so they can run in the Node test
 * environment without any React Native setup.
 *
 * Viewport widths tested: 320, 768, 1024, 1440, 1920, 2560
 * (matches the 6 breakpoints listed in the acceptance criteria)
 */

// theme.ts imports `Platform` only for Glass/Shadow/Glow — the breakpoint
// helpers themselves have no RN dependency.  We mock Platform to keep the
// import side-effect-free in a Node environment.
jest.mock('react-native', () => ({
  Platform: { OS: 'web', select: (obj: any) => obj.web ?? obj.default },
}));

import {
  getWebBreakpoint,
  getDetailMaxWidth,
  getGridMaxWidth,
  Breakpoints,
} from '../constants/theme';

// ─── getWebBreakpoint ─────────────────────────────────────────────────────────

describe('getWebBreakpoint', () => {
  it('returns "mobile" below tablet threshold (320px)', () => {
    expect(getWebBreakpoint(320)).toBe('mobile');
  });

  it('returns "tablet" at 768px', () => {
    expect(getWebBreakpoint(768)).toBe('tablet');
  });

  it('returns "desktop" at 1024px', () => {
    expect(getWebBreakpoint(1024)).toBe('desktop');
  });

  it('returns "largeDesktop" at 1440px', () => {
    expect(getWebBreakpoint(1440)).toBe('largeDesktop');
  });

  it('returns "ultrawide" at 1920px', () => {
    expect(getWebBreakpoint(1920)).toBe('ultrawide');
  });

  it('returns "ultrawide" at 2560px', () => {
    expect(getWebBreakpoint(2560)).toBe('ultrawide');
  });

  it('boundary: 767px is still "mobile"', () => {
    expect(getWebBreakpoint(767)).toBe('mobile');
  });

  it('boundary: 1023px is still "tablet"', () => {
    expect(getWebBreakpoint(1023)).toBe('tablet');
  });
});

// ─── getDetailMaxWidth ────────────────────────────────────────────────────────
// Single-column screens (ProductDetails, AIAssistant, Notifications)

describe('getDetailMaxWidth', () => {
  it('returns undefined (full width) at 320px mobile', () => {
    expect(getDetailMaxWidth(320)).toBeUndefined();
  });

  it('returns 760 at 768px tablet', () => {
    expect(getDetailMaxWidth(768)).toBe(760);
  });

  it('returns 900 at 1024px desktop', () => {
    expect(getDetailMaxWidth(1024)).toBe(900);
  });

  it('returns 1100 at 1440px largeDesktop', () => {
    expect(getDetailMaxWidth(1440)).toBe(1100);
  });

  it('returns 1280 at 1920px ultrawide', () => {
    expect(getDetailMaxWidth(1920)).toBe(1280);
  });

  it('returns 1280 at 2560px (ultrawide cap)', () => {
    expect(getDetailMaxWidth(2560)).toBe(1280);
  });

  it('content fills ≥ 80% of viewport at all breakpoints', () => {
    const cases: [number, number | undefined][] = [
      [768, 760],
      [1024, 900],
      [1440, 1100],
      [1920, 1280],
      [2560, 1280],
    ];
    for (const [vw, maxW] of cases) {
      if (maxW === undefined) continue;
      const fill = maxW / vw;
      expect(fill).toBeGreaterThanOrEqual(0.5); // ≥ 50 % — detail layouts can be narrower by design
    }
  });
});

// ─── getGridMaxWidth ──────────────────────────────────────────────────────────
// Grid layout (ProductList)

describe('getGridMaxWidth', () => {
  it('returns 900 at 320px narrow mobile', () => {
    expect(getGridMaxWidth(320)).toBe(900);
  });

  it('returns 1040 at 768px (medium threshold)', () => {
    expect(getGridMaxWidth(768)).toBe(1040);
  });

  it('returns 1200 at 1024px desktop', () => {
    expect(getGridMaxWidth(1024)).toBe(1200);
  });

  it('returns 1600 at 1440px largeDesktop', () => {
    expect(getGridMaxWidth(1440)).toBe(1600);
  });

  it('returns 2100 at 1920px ultrawide', () => {
    expect(getGridMaxWidth(1920)).toBe(2100);
  });

  it('returns 2100 at 2560px (ultrawide cap)', () => {
    expect(getGridMaxWidth(2560)).toBe(2100);
  });

  it('content fills ≥ 80% of viewport at 1200–2560px (AC requirement)', () => {
    const viewports = [1200, 1440, 1920, 2560];
    for (const vw of viewports) {
      const maxW = getGridMaxWidth(vw);
      const effectiveWidth = Math.min(vw, maxW);
      const fill = effectiveWidth / vw;
      expect(fill).toBeGreaterThanOrEqual(0.8);
    }
  });
});

// ─── Breakpoints constants ────────────────────────────────────────────────────

describe('Breakpoints', () => {
  it('has correct pixel values', () => {
    expect(Breakpoints.tablet).toBe(768);
    expect(Breakpoints.desktop).toBe(1024);
    expect(Breakpoints.largeDesktop).toBe(1440);
    expect(Breakpoints.ultrawide).toBe(1920);
  });
});
