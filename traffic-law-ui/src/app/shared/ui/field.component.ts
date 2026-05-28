import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  selector: 'app-field',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (label()) {
      <label class="pp-field-label">
        {{ label() }}
        @if (required()) { <span class="pp-field-required">*</span> }
      </label>
    }
    <ng-content/>
    @if (error()) {
      <span class="pp-field-error">{{ error() }}</span>
    } @else if (hint()) {
      <span class="pp-field-hint">{{ hint() }}</span>
    }
  `,
  styles: `
    :host { display: flex; flex-direction: column; gap: 6px; }
    .pp-field-label { font-size: 13px; font-weight: 500; color: var(--ink-0); }
    .pp-field-required { color: var(--signal-red); margin-left: 4px; }
    .pp-field-error { font-size: 12px; color: var(--signal-red-ink); }
    .pp-field-hint { font-size: 12px; color: var(--ink-2); }
  `,
})
export class FieldComponent {
  readonly label    = input<string>('');
  readonly hint     = input<string>('');
  readonly error    = input<string>('');
  readonly required = input<boolean>(false);
}
