import { Range } from './types';

/** Format a plain number using Serbian thousands separator (`.`). */
export function fmtN(n: number): string {
  return new Intl.NumberFormat('sr-RS', { useGrouping: true })
    .format(n).replace(/ /g, '.');
}

/**
 * Render a Range as "min–max" (em-dash, no spaces) or a single value
 * when both ends match. Generic helper for non-currency ranges.
 */
export function fmtRange(r: Range): string {
  const [a, b] = r;
  return a === b ? fmtN(a) : `${fmtN(a)}–${fmtN(b)}`;
}

/** Render a RSD range with the "RSD" suffix appended once. */
export function fmtRsdRange(r: Range): string {
  return `${fmtRange(r)} RSD`;
}

/** Componentwise sum of two ranges: `[a,b] + [c,d] = [a+c, b+d]`. */
export function sumRange(a: Range, b: Range): Range {
  return [a[0] + b[0], a[1] + b[1]];
}
