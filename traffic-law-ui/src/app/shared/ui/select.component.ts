import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

export type SelectOption = { value: string; label: string };

@Component({
  selector: 'app-select',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <select
      class="pp-select"
      [value]="value()"
      (change)="handle($event)"
    >
      @if (placeholder()) {
        <option [value]="''" disabled>{{ placeholder() }}</option>
      }
      @for (o of options(); track o.value) {
        <option [value]="o.value">{{ o.label }}</option>
      }
    </select>
  `,
  styles: `
    .pp-select {
      height: 38px;
      padding: 0 36px 0 12px;
      border: 1px solid var(--border-2);
      border-radius: var(--r-2);
      background-color: var(--surface-1);
      background-image: url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%2358627a' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><polyline points='6 9 12 15 18 9'/></svg>");
      background-repeat: no-repeat;
      background-position: right 12px center;
      -webkit-appearance: none;
      appearance: none;
      font-family: var(--font-sans);
      font-size: 14px;
      color: var(--ink-0);
      width: 100%;
      cursor: pointer;
    }
    .pp-select:hover { border-color: var(--border-3); }
    .pp-select:focus { outline: none; border-color: var(--brand-navy); box-shadow: var(--shadow-focus); }
  `,
})
export class SelectComponent {
  readonly options     = input<SelectOption[]>([]);
  readonly value       = input<string>('');
  readonly placeholder = input<string>('');
  readonly valueChange = output<string>();

  handle(ev: Event) {
    const v = (ev.target as HTMLSelectElement).value;
    this.valueChange.emit(v);
  }
}
