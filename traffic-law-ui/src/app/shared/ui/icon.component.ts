import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { LucideAngularModule, LucideIconData } from 'lucide-angular';

/**
 * Wraps lucide-angular so feature code can stay declarative.
 *
 * Usage: `<app-icon [img]="SearchIcon" [size]="16"/>` where `SearchIcon`
 * is imported from `lucide-angular`. We hand the icon data object through
 * — string lookups by name are not exposed by lucide-angular.
 */
@Component({
  selector: 'app-icon',
  standalone: true,
  imports: [LucideAngularModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <lucide-angular
      [img]="img()"
      [size]="size()"
      [strokeWidth]="stroke()"
      [class]="className()"
    />
  `,
  styles: `
    :host { display: inline-flex; align-items: center; justify-content: center; flex: none; line-height: 0; }
    lucide-angular { color: currentColor; }
  `,
})
export class IconComponent {
  readonly img       = input.required<LucideIconData>();
  readonly size      = input<number>(18);
  readonly stroke    = input<number>(1.5);
  readonly className = input<string>('');
}
