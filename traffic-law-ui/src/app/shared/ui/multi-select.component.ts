import {
  ChangeDetectionStrategy, Component, ElementRef, HostListener,
  input, output, signal,
} from '@angular/core';
import { LucideAngularModule, Check } from 'lucide-angular';

export type MultiOption = { value: string; label: string };

@Component({
  selector: 'app-multi-select',
  standalone: true,
  imports: [LucideAngularModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="pp-multi" [class.open]="open()">
      <div class="control" (click)="toggleOpen()">
        @if (value().length === 0) {
          <span class="placeholder">{{ placeholder() }}</span>
        }
        @for (v of value(); track v) {
          <span class="chip">
            {{ labelOf(v) }}
            <span class="x" (click)="remove(v); $event.stopPropagation()">×</span>
          </span>
        }
      </div>
      @if (open()) {
        <div class="menu">
          @for (o of options(); track o.value) {
            @let checked = value().includes(o.value);
            <div class="row" [class.checked]="checked" (click)="toggle(o.value)">
              <span class="box" [class.on]="checked">
                @if (checked) { <lucide-angular [img]="checkIcon" [size]="12" [strokeWidth]="3"/> }
              </span>
              <span class="label">{{ o.label }}</span>
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: `
    :host { display: block; position: relative; }

    .control {
      min-height: 38px;
      border: 1px solid var(--border-2);
      border-radius: var(--r-2);
      background-color: var(--surface-1);
      background-image: url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%2358627a' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><polyline points='6 9 12 15 18 9'/></svg>");
      background-repeat: no-repeat;
      background-position: right 12px center;
      padding: 5px 36px 5px 8px;
      display: flex;
      flex-wrap: wrap;
      gap: 5px;
      align-items: center;
      cursor: pointer;
    }
    .pp-multi.open .control { border-color: var(--brand-navy); box-shadow: var(--shadow-focus); }

    .placeholder { color: var(--ink-2); font-size: 14px; padding: 0 4px; }

    .chip {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      height: 24px;
      padding: 0 4px 0 10px;
      background: var(--brand-navy);
      color: var(--ink-onbrand);
      border-radius: var(--r-1);
      font-family: var(--font-mono);
      font-size: 12px;
    }
    .chip .x {
      width: 16px; height: 16px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      color: rgba(255, 255, 255, 0.7);
      border-radius: 3px;
      cursor: pointer;
    }
    .chip .x:hover { background: rgba(255, 255, 255, 0.12); color: white; }

    .menu {
      position: absolute;
      top: calc(100% + 4px);
      left: 0; right: 0;
      background: var(--surface-1);
      border: 1px solid var(--border-1);
      border-radius: var(--r-2);
      box-shadow: var(--shadow-2);
      max-height: 240px;
      overflow-y: auto;
      z-index: 10;
      padding: 4px;
    }
    .row {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 8px 10px;
      border-radius: var(--r-1);
      cursor: pointer;
      font-size: 14px;
    }
    .row:hover { background: var(--surface-2); }
    .row.checked { background: var(--surface-2); }

    .box {
      width: 16px; height: 16px;
      border-radius: 3px;
      border: 1px solid var(--border-3);
      background: transparent;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      flex: none;
    }
    .box.on { border-color: var(--brand-navy); background: var(--brand-navy); color: white; }

    .label { flex: 1; }
  `,
})
export class MultiSelectComponent {
  private readonly host = (() => {
    // Constructor parameter trick avoids needing inject() for ElementRef
    // when we only use it inside the @HostListener handler.
    return null;
  })();

  readonly options     = input<MultiOption[]>([]);
  readonly value       = input<string[]>([]);
  readonly placeholder = input<string>('dodaj...');
  readonly valueChange = output<string[]>();

  readonly checkIcon = Check;
  readonly open = signal(false);

  constructor(private readonly el: ElementRef<HTMLElement>) {}

  @HostListener('document:mousedown', ['$event'])
  onDocumentClick(ev: MouseEvent) {
    if (!this.el.nativeElement.contains(ev.target as Node)) {
      this.open.set(false);
    }
  }

  toggleOpen() { this.open.update(o => !o); }

  toggle(v: string) {
    const cur = this.value();
    this.valueChange.emit(cur.includes(v) ? cur.filter(x => x !== v) : [...cur, v]);
  }

  remove(v: string) {
    this.valueChange.emit(this.value().filter(x => x !== v));
  }

  labelOf(v: string): string {
    return this.options().find(o => o.value === v)?.label ?? v;
  }
}
