import { ChangeDetectionStrategy, Component, input } from '@angular/core';

export type BadgeTone = 'red' | 'amber' | 'green' | 'blue' | 'neutral';

@Component({
  selector: 'app-badge',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <span class="pp-badge" [attr.data-tone]="tone()">
      @if (dot()) { <span class="dot"></span> }
      <ng-content/>
    </span>
  `,
  styles: `
    .pp-badge {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      min-height: 22px;
      padding: 3px 8px;
      border-radius: var(--r-1);
      font-size: 12px;
      font-weight: 500;
      border: 1px solid var(--border-1);
      background: var(--surface-3);
      color: var(--ink-1);
      line-height: 1.35;
    }
    .pp-badge[data-tone="red"]    { background: var(--signal-red-soft);    color: var(--signal-red-ink);    border-color: oklch(85% 0.05 25); }
    .pp-badge[data-tone="amber"]  { background: var(--warning-amber-soft); color: var(--warning-amber-ink); border-color: oklch(88% 0.05 80); }
    .pp-badge[data-tone="green"]  { background: var(--success-green-soft); color: var(--success-green-ink); border-color: oklch(86% 0.06 155); }
    .pp-badge[data-tone="blue"]   { background: var(--info-blue-soft);     color: var(--info-blue-ink);     border-color: oklch(86% 0.04 245); }

    .dot { width: 6px; height: 6px; border-radius: 50%; background: var(--ink-2); }
    .pp-badge[data-tone="red"]    .dot { background: var(--signal-red); }
    .pp-badge[data-tone="amber"]  .dot { background: var(--warning-amber); }
    .pp-badge[data-tone="green"]  .dot { background: var(--success-green); }
    .pp-badge[data-tone="blue"]   .dot { background: var(--info-blue); }
  `,
})
export class BadgeComponent {
  readonly tone = input<BadgeTone>('neutral');
  readonly dot  = input<boolean>(false);
}
