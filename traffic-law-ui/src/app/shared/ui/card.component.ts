import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  selector: 'app-card',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<ng-content/>`,
  styles: `
    :host {
      display: block;
      background: var(--surface-1);
      border: 1px solid var(--border-1);
      border-radius: var(--r-2);
      padding: var(--card-pad, 22px);
    }
    :host([pad="0"])  { --card-pad: 0; }
    :host([pad="16"]) { --card-pad: 16px; }
    :host([pad="28"]) { --card-pad: 28px; }
  `,
})
export class CardComponent {
  /** "0" / "16" / "22" (default) / "28" — picked as host attribute. */
  readonly pad = input<string>('22');
}
