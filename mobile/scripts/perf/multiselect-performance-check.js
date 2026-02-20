#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

const args = new Set(process.argv.slice(2));
const strictMode = args.has('--strict');
const jsonMode = args.has('--json');
const helpMode = args.has('--help') || args.has('-h');

const rootDir = path.resolve(__dirname, '..', '..');

const files = {
  productListScreen: 'src/screens/ProductListScreen.tsx',
  wishlistScreen: 'src/screens/WishlistScreen.tsx',
  selectableProductCard: 'src/components/SelectableProductCard.tsx',
  selectableWishlistCard: 'src/components/SelectableWishlistCard.tsx',
};

if (helpMode) {
  console.log(
    [
      'Multiselect Performance Check',
      '',
      'Usage:',
      '  node scripts/perf/multiselect-performance-check.js [--strict] [--json]',
      '',
      'Options:',
      '  --strict   Exit with code 1 if any finding exists',
      '  --json     Print machine-readable JSON output',
      '',
      'Default mode only reports findings and always exits 0.',
    ].join('\n')
  );
  process.exit(0);
}

function readFile(relativePath) {
  const absolutePath = path.join(rootDir, relativePath);
  return fs.readFileSync(absolutePath, 'utf8');
}

function lineForIndex(source, index) {
  return source.slice(0, index).split('\n').length;
}

function findMatches(source, pattern) {
  const matches = [];
  pattern.lastIndex = 0;
  let match = pattern.exec(source);
  while (match) {
    matches.push({
      index: match.index,
      line: lineForIndex(source, match.index),
      text: match[0],
      groups: match.slice(1),
    });
    match = pattern.exec(source);
  }
  return matches;
}

function classifyRisk(score) {
  if (score >= 50) return 'HIGH';
  if (score >= 20) return 'MEDIUM';
  return 'LOW';
}

function pushFindings(collection, source, relativeFile, regex, metadata) {
  const matches = findMatches(source, regex);
  matches.forEach((m) => {
    collection.push({
      id: metadata.id,
      title: metadata.title,
      reason: metadata.reason,
      weight: metadata.weight,
      file: relativeFile,
      line: m.line,
      sample: m.text,
    });
  });
}

function run() {
  const sources = Object.fromEntries(
    Object.entries(files).map(([key, relPath]) => [key, readFile(relPath)])
  );

  const findings = [];

  pushFindings(
    findings,
    sources.productListScreen,
    files.productListScreen,
    /extraData=\{selectionTick\}/g,
    {
      id: 'FLATLIST_EXTRA_DATA_TICK',
      title: 'FlatList uses selectionTick as extraData',
      reason: 'Selection toggles force visible cells to re-render across the list.',
      weight: 16,
    }
  );

  pushFindings(
    findings,
    sources.wishlistScreen,
    files.wishlistScreen,
    /extraData=\{selectionTick\}/g,
    {
      id: 'FLATLIST_EXTRA_DATA_TICK',
      title: 'FlatList uses selectionTick as extraData',
      reason: 'Selection toggles force visible cells to re-render across the list.',
      weight: 16,
    }
  );

  pushFindings(
    findings,
    sources.productListScreen,
    files.productListScreen,
    /`\$\{id\}-\$\{isSelectionMode \? 1 : 0\}-\$\{selected \? 1 : 0\}-\$\{selectionTick\}`/g,
    {
      id: 'DYNAMIC_FORCE_KEY',
      title: 'Dynamic item key includes selectionTick',
      reason: 'Key churn causes remounts instead of cheap updates, adding interaction latency.',
      weight: 20,
    }
  );

  pushFindings(
    findings,
    sources.wishlistScreen,
    files.wishlistScreen,
    /`\$\{id\}-\$\{isSelectionMode \? 1 : 0\}-\$\{selected \? 1 : 0\}-\$\{selectionTick\}`/g,
    {
      id: 'DYNAMIC_FORCE_KEY',
      title: 'Dynamic item key includes selectionTick',
      reason: 'Key churn causes remounts instead of cheap updates, adding interaction latency.',
      weight: 20,
    }
  );

  pushFindings(
    findings,
    sources.productListScreen,
    files.productListScreen,
    /removeClippedSubviews=\{false\}/g,
    {
      id: 'NO_CLIPPING_OPTIMIZATION',
      title: 'FlatList clipping disabled',
      reason: 'Disabling clipping increases memory/paint work on larger lists.',
      weight: 8,
    }
  );

  pushFindings(
    findings,
    sources.wishlistScreen,
    files.wishlistScreen,
    /removeClippedSubviews=\{false\}/g,
    {
      id: 'NO_CLIPPING_OPTIMIZATION',
      title: 'FlatList clipping disabled',
      reason: 'Disabling clipping increases memory/paint work on larger lists.',
      weight: 8,
    }
  );

  pushFindings(
    findings,
    sources.selectableProductCard,
    files.selectableProductCard,
    /Animated\.loop\(/g,
    {
      id: 'SELECTION_SHAKE_LOOP',
      title: 'Per-card infinite shake animation',
      reason: 'Running looped animations on many selected cards can saturate render threads.',
      weight: 12,
    }
  );

  pushFindings(
    findings,
    sources.selectableWishlistCard,
    files.selectableWishlistCard,
    /Animated\.loop\(/g,
    {
      id: 'SELECTION_SHAKE_LOOP',
      title: 'Per-card infinite shake animation',
      reason: 'Running looped animations on many selected cards can saturate render threads.',
      weight: 12,
    }
  );

  const productDelayMatches = findMatches(
    sources.selectableProductCard,
    /delayLongPress=\{(\d+)\}/g
  );
  productDelayMatches.forEach((m) => {
    const value = Number(m.groups[0]);
    if (Number.isFinite(value) && value >= 400) {
      findings.push({
        id: 'LONG_PRESS_DELAY_HIGH',
        title: 'Long-press delay may feel laggy',
        reason: `delayLongPress is ${value}ms. Values this high are often perceived as sluggish.`,
        weight: 10,
        file: files.selectableProductCard,
        line: m.line,
        sample: m.text,
      });
    }
  });

  const wishlistDelayMatches = findMatches(
    sources.selectableWishlistCard,
    /delayLongPress=\{(\d+)\}/g
  );
  wishlistDelayMatches.forEach((m) => {
    const value = Number(m.groups[0]);
    if (Number.isFinite(value) && value >= 400) {
      findings.push({
        id: 'LONG_PRESS_DELAY_HIGH',
        title: 'Long-press delay may feel laggy',
        reason: `delayLongPress is ${value}ms. Values this high are often perceived as sluggish.`,
        weight: 10,
        file: files.selectableWishlistCard,
        line: m.line,
        sample: m.text,
      });
    }
  });

  if (!/memo\(/.test(sources.selectableProductCard)) {
    findings.push({
      id: 'CARD_NOT_MEMOIZED',
      title: 'SelectableProductCard is not memoized',
      reason: 'Non-memoized cards re-render more often when parent list state changes.',
      weight: 10,
      file: files.selectableProductCard,
      line: 1,
      sample: 'export const SelectableProductCard',
    });
  }

  const score = findings.reduce((sum, item) => sum + item.weight, 0);
  const risk = classifyRisk(score);
  const status = findings.length === 0 ? 'PASS' : strictMode ? 'FAIL' : 'WARN';

  const report = {
    status,
    strictMode,
    risk,
    score,
    findingCount: findings.length,
    findings,
    analyzedFiles: Object.values(files),
  };

  if (jsonMode) {
    console.log(JSON.stringify(report, null, 2));
  } else {
    console.log('Multiselect Performance Check');
    console.log(`Status: ${status}`);
    console.log(`Risk: ${risk} (score=${score})`);
    console.log(`Findings: ${findings.length}`);
    console.log('');

    if (findings.length === 0) {
      console.log('No lag-risk patterns detected.');
    } else {
      findings.forEach((item, index) => {
        console.log(
          `${index + 1}. ${item.title} [${item.id}]`
        );
        console.log(`   ${item.file}:${item.line}`);
        console.log(`   ${item.reason}`);
      });
      console.log('');
      console.log('Tip: Run with --json for machine-readable output.');
    }
  }

  if (strictMode && findings.length > 0) {
    process.exit(1);
  }
}

run();
