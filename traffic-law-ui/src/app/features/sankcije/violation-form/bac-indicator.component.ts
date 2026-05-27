import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { classifyBac } from '../../../shared/catalogues';

const MAX_BAC = 2.0;
const TICKS = [0.21, 0.51, 0.81, 1.21];

/**
 * Live BAC tone-band indicator: classifies a numeric value into one of
 * the bands from CLAUDE.md §2.3 Section 4, renders a label and a
 * horizontal progress bar with threshold tick marks.
 */
@Component({
  selector: 'app-bac-indicator',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="panel" [attr.data-tone]="band().tone">
      <div class="row">
        <span class="reading pp-mono">
          @if (isValid()) { {{ formatted() }} } @else { 0,00 }
          <span class="permille">‰</span>
        </span>
        <span class="band">{{ band().label }}</span>
      </div>
      <div class="bar">
        <div class="bar-fill" [style.width.%]="fillPct()"></div>
        @for (t of ticks; track t) {
          <div class="tick" [style.left.%]="(t / max) * 100"></div>
        }
      </div>
      <div class="scale">
        <span>0,00</span>
        <span>1,00</span>
        <span>2,00 ‰</span>
      </div>
    </div>
  `,
  styles: `
    :host { display: block; }
    .panel {
      background: var(--surface-sunken);
      border: 1px solid var(--border-1);
      border-radius: var(--r-2);
      padding: 16px 18px;
      display: flex;
      flex-direction: column;
      gap: 10px;
    }
    .row { display: flex; justify-content: space-between; align-items: baseline; gap: 12px; }
    .reading {
      font-size: 22px;
      font-weight: 600;
      color: var(--brand-navy-ink);
    }
    .permille { margin-left: 2px; color: var(--ink-2); font-weight: 500; font-size: 14px; }

    .band {
      font-size: 13px;
      font-weight: 500;
      padding: 2px 10px;
      border-radius: var(--r-1);
      background: var(--surface-3);
      color: var(--ink-1);
    }
    .panel[data-tone="green"] .band { background: var(--success-green-soft); color: var(--success-green-ink); }
    .panel[data-tone="amber"] .band { background: var(--warning-amber-soft); color: var(--warning-amber-ink); }
    .panel[data-tone="red"]   .band { background: var(--signal-red-soft);    color: var(--signal-red-ink); }

    .bar {
      position: relative;
      height: 8px;
      background: var(--surface-3);
      border-radius: 4px;
      overflow: visible;
    }
    .bar-fill {
      position: absolute;
      top: 0; bottom: 0; left: 0;
      border-radius: 4px;
      transition: width var(--d-mid) var(--ease-civic), background var(--d-fast);
      background: var(--success-green);
    }
    .panel[data-tone="amber"] .bar-fill { background: var(--warning-amber); }
    .panel[data-tone="red"]   .bar-fill { background: var(--signal-red); }

    .tick {
      position: absolute;
      top: -3px;
      width: 1px;
      height: 14px;
      background: var(--ink-3);
    }

    .scale {
      display: flex;
      justify-content: space-between;
      font-family: var(--font-mono);
      font-size: 10px;
      color: var(--ink-2);
    }
  `,
})
export class BacIndicatorComponent {
  readonly value = input<number>(NaN);

  readonly max = MAX_BAC;
  readonly ticks = TICKS;

  readonly isValid  = computed(() => Number.isFinite(this.value()) && this.value() >= 0);
  readonly safeValue = computed(() => this.isValid() ? this.value() : 0);
  readonly band     = computed(() => classifyBac(this.safeValue()));
  readonly fillPct  = computed(() => Math.min(100, (this.safeValue() / MAX_BAC) * 100));
  readonly formatted = computed(() => this.safeValue().toFixed(2).replace('.', ','));
}
