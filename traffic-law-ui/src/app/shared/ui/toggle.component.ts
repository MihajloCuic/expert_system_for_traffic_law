import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

@Component({
  selector: 'app-toggle',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="row">
      <div
        class="rail"
        [class.on]="on()"
        (click)="onChange.emit(!on())"
      >
        <div class="knob" [class.on]="on()"></div>
      </div>
      @if (label()) { <span class="label">{{ label() }}</span> }
      <span class="state" [class.on]="on()">{{ on() ? offLabels()[1] : offLabels()[0] }}</span>
    </div>
  `,
  styles: `
    .row { display: flex; align-items: center; gap: 12px; }

    .rail {
      width: 44px; height: 24px;
      background: var(--ink-3);
      border-radius: 12px;
      position: relative;
      cursor: pointer;
      transition: background var(--d-fast) var(--ease-civic);
      flex: none;
    }
    .rail.on { background: var(--brand-navy); }

    .knob {
      position: absolute;
      top: 2px;
      left: 2px;
      width: 20px; height: 20px;
      background: white;
      border-radius: 50%;
      box-shadow: 0 1px 2px rgba(0,0,0,0.15);
      transition: left var(--d-fast) var(--ease-civic);
    }
    .knob.on { left: 22px; }

    .label { font-size: 14px; color: var(--ink-0); }

    .state {
      font-family: var(--font-mono);
      font-size: 11px;
      padding: 2px 8px;
      border-radius: var(--r-1);
      background: var(--surface-3);
      color: var(--ink-1);
      font-weight: 600;
    }
    .state.on {
      background: var(--success-green-soft);
      color: var(--success-green-ink);
    }
  `,
})
export class ToggleComponent {
  readonly on        = input<boolean>(false);
  readonly label     = input<string>('');
  readonly offLabels = input<[string, string]>(['NE', 'DA']);
  readonly onChange  = output<boolean>();
}
