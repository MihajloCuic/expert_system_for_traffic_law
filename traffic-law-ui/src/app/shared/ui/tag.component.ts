import { ChangeDetectionStrategy, Component } from '@angular/core';

/**
 * Monospaced data chip — used for category codes, plates, JMBG.
 * Always neutral surface (no semantic colour variant by design).
 */
@Component({
  selector: 'app-tag',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<ng-content/>`,
  styles: `
    :host {
      display: inline-flex;
      align-items: center;
      min-height: 22px;
      padding: 3px 8px;
      background: var(--surface-3);
      color: var(--ink-0);
      border-radius: var(--r-1);
      font-family: var(--font-mono);
      font-size: 11px;
      font-weight: 500;
      letter-spacing: 0.02em;
      line-height: 1.35;
      word-break: break-word;
    }
  `,
})
export class TagComponent {}
