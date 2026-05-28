import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { LucideAngularModule, LucideIconData } from 'lucide-angular';

export type BtnVariant = 'primary' | 'secondary' | 'ghost' | 'danger';
export type BtnSize    = 'sm' | 'md' | 'lg';

@Component({
  selector: 'app-button',
  standalone: true,
  imports: [LucideAngularModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <button
      type="button"
      class="pp-btn"
      [class.lg]="size() === 'lg'"
      [class.sm]="size() === 'sm'"
      [class.primary]="variant() === 'primary'"
      [class.secondary]="variant() === 'secondary'"
      [class.ghost]="variant() === 'ghost'"
      [class.danger]="variant() === 'danger'"
      [disabled]="disabled()"
      (click)="onClick.emit()"
    >
      @if (icon(); as ic) {
        <lucide-angular [img]="ic" [size]="iconSize()"/>
      }
      <ng-content/>
      @if (iconRight(); as ir) {
        <lucide-angular [img]="ir" [size]="iconSize()"/>
      }
    </button>
  `,
  styles: `
    .pp-btn {
      font-family: var(--font-sans);
      font-size: 14px;
      font-weight: 500;
      height: 36px;
      padding: 0 14px;
      border-radius: var(--r-2);
      border: 1px solid transparent;
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      transition:
        background var(--d-fast) var(--ease-civic),
        color var(--d-fast),
        border-color var(--d-fast);
      white-space: nowrap;
    }
    .pp-btn.lg { height: 44px; font-size: 15px; padding: 0 18px; }
    .pp-btn.sm { height: 30px; font-size: 13px; padding: 0 10px; }

    .pp-btn.primary   { background: var(--brand-navy);  color: var(--ink-onbrand); }
    .pp-btn.primary:hover  { background: var(--brand-navy-hover); }
    .pp-btn.primary:active { background: var(--brand-navy-press); box-shadow: inset 0 1px 0 rgba(0,0,0,0.18); }

    .pp-btn.secondary { background: var(--surface-1); color: var(--brand-navy-ink); border-color: var(--border-2); }
    .pp-btn.secondary:hover { background: var(--surface-2); }

    .pp-btn.ghost     { background: transparent; color: var(--brand-navy-ink); }
    .pp-btn.ghost:hover { background: var(--surface-2); }

    .pp-btn.danger    { background: var(--signal-red); color: white; }
    .pp-btn.danger:hover { filter: brightness(0.94); }

    .pp-btn:disabled  { background: var(--ink-3); color: var(--surface-1); cursor: not-allowed; border-color: transparent; }
    .pp-btn:focus-visible { outline: none; box-shadow: var(--shadow-focus); }
  `,
})
export class ButtonComponent {
  readonly variant   = input<BtnVariant>('primary');
  readonly size      = input<BtnSize>('md');
  readonly icon      = input<LucideIconData | undefined>(undefined);
  readonly iconRight = input<LucideIconData | undefined>(undefined);
  readonly disabled  = input<boolean>(false);
  readonly onClick   = output<void>();

  iconSize(): number {
    return this.size() === 'lg' ? 18 : 16;
  }
}
