import { ChangeDetectionStrategy, Component, input, output, signal } from '@angular/core';

@Component({
  selector: 'app-input',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <input
      class="pp-input"
      [class.mono]="mono()"
      [class.error]="!!error()"
      [type]="type()"
      [placeholder]="placeholder()"
      [value]="value()"
      [attr.maxlength]="maxlength()"
      (input)="handleInput($event)"
      (keydown.enter)="onEnter.emit()"
    />
  `,
  styles: `
    .pp-input {
      height: 38px;
      padding: 0 12px;
      border: 1px solid var(--border-2);
      border-radius: var(--r-2);
      background: var(--surface-1);
      font-family: var(--font-sans);
      font-size: 14px;
      color: var(--ink-0);
      outline: none;
      width: 100%;
      transition: border-color var(--d-fast) var(--ease-civic), box-shadow var(--d-fast);
    }
    .pp-input.mono { font-family: var(--font-mono); }
    .pp-input.error { border-color: var(--signal-red); }
    .pp-input:hover { border-color: var(--border-3); }
    .pp-input:focus { border-color: var(--brand-navy); box-shadow: var(--shadow-focus); }
    .pp-input::placeholder { color: var(--ink-2); }
  `,
})
export class InputComponent {
  readonly mono        = input<boolean>(false);
  readonly type        = input<'text' | 'number' | 'date'>('text');
  readonly placeholder = input<string>('');
  readonly value       = input<string>('');
  readonly maxlength   = input<number | null>(null);
  readonly error       = input<string | null>(null);
  readonly valueChange = output<string>();
  readonly onEnter     = output<void>();

  handleInput(ev: Event) {
    const v = (ev.target as HTMLInputElement).value;
    this.valueChange.emit(v);
  }
}
