import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { LucideAngularModule, IdCard, Search, SearchX, ShieldCheck } from 'lucide-angular';

import { ButtonComponent } from '../../../shared/ui/button.component';
import { InputComponent }  from '../../../shared/ui/input.component';
import { CardComponent }   from '../../../shared/ui/card.component';
import { FieldComponent }  from '../../../shared/ui/field.component';
import { BadgeComponent }  from '../../../shared/ui/badge.component';
import { TagComponent }    from '../../../shared/ui/tag.component';

import { DriverRecord } from '../../../shared/types';
import { SankcijeService } from '../sankcije.service';

type LookupState =
  | { kind: 'empty' }
  | { kind: 'loading' }
  | { kind: 'notFound'; query: string }
  | { kind: 'found'; driver: DriverRecord };

@Component({
  selector: 'app-quick-check',
  standalone: true,
  imports: [
    LucideAngularModule,
    ButtonComponent, InputComponent, CardComponent, FieldComponent,
    BadgeComponent, TagComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="page">
      <div class="page-head">
        <div>
          <h1>Brza provera</h1>
          <div class="lede">Unesite broj vozačke dozvole za trenutni uvid u status vozača.</div>
        </div>
      </div>

      <div class="search-card">
        <app-field label="Broj vozačke dozvole" hint="9 cifara · bez razmaka">
          <div class="search-row">
            <app-input
              [mono]="true"
              placeholder="npr. 049837261"
              [value]="query()"
              (valueChange)="query.set($event)"
              (onEnter)="search()"
            />
            <app-button size="lg" [icon]="searchIcon" (onClick)="search()">
              {{ state().kind === 'loading' ? 'Učitavanje…' : 'Proveri' }}
            </app-button>
          </div>
        </app-field>
        <div class="hint-row">
          <lucide-angular [img]="shieldCheckIcon" [size]="14"/>
          <span>Provera se evidentira u službenom logu. Veza je TLS 1.3.</span>
        </div>
      </div>

      @switch (state().kind) {
        @case ('empty') {
          <div class="empty">
            <div class="icon"><lucide-angular [img]="idCardIcon" [size]="36" [strokeWidth]="1.25"/></div>
            <h3>Nema aktivnog rezultata</h3>
            <p>Unesite broj dozvole iznad i pritisnite Proveri.</p>
          </div>
        }
        @case ('loading') {
          <div class="empty">
            <div class="icon"><lucide-angular [img]="searchIcon" [size]="36" [strokeWidth]="1.25"/></div>
            <h3>Pretraga u toku…</h3>
          </div>
        }
        @case ('notFound') {
          @if (asNotFound(state()); as s) {
            <app-card>
              <div class="notfound">
                <div class="ico"><lucide-angular [img]="searchXIcon" [size]="22"/></div>
                <div>
                  <h3>Dozvola <span class="pp-mono">{{ s.query }}</span> nije pronađena.</h3>
                  <p>Proverite format (9 cifara) ili kontaktirajte centralnu evidenciju.</p>
                </div>
              </div>
            </app-card>
          }
        }
        @case ('found') {
          @if (asFound(state()); as s) {
            <app-card>
              @let d = s.driver;
              @let revoked = d.aktivniPoeni >= cap(d);
              @let pct = Math.min(100, (d.aktivniPoeni / cap(d)) * 100);

              <div class="driver-head">
                <div>
                  <div class="pp-overline">Vozač</div>
                  <h2 class="driver-name">{{ d.ime }} {{ d.prezime }}</h2>
                  <div class="driver-meta">
                    <app-tag>JMBG · {{ d.jmbg }}</app-tag>
                    <span class="muted">· rođen {{ d.rodj }}</span>
                  </div>
                </div>
                <div class="driver-badges">
                  <app-badge
                    [dot]="true"
                    [tone]="revoked ? 'red' : (d.aktivniPoeni > cap(d) * 0.6 ? 'amber' : 'green')"
                  >
                    {{ revoked ? 'Oduzimanje pokrenuto' : 'Aktivna dozvola' }}
                  </app-badge>
                  <app-badge tone="neutral">{{ d.dozvola.tip }}</app-badge>
                </div>
              </div>

              <hr class="divider"/>

              <div class="driver-body">
                <div class="kv">
                  <div class="k">Broj dozvole</div>     <div class="v mono">{{ d.dozvola.broj }}</div>
                  <div class="k">Tip dozvole</div>      <div class="v mono">{{ d.dozvola.tip }}</div>
                  <div class="k">Datum izdavanja</div>  <div class="v mono">{{ d.dozvola.izdato }}</div>
                  <div class="k">Važi do</div>          <div class="v mono">{{ d.dozvola.vazi }}</div>
                  <div class="k">Važeće kategorije</div>
                  <div class="v cats">
                    @for (k of d.dozvola.kategorije; track k) {
                      <app-tag>{{ k }}</app-tag>
                    }
                  </div>
                </div>

                <div>
                  <div class="pp-overline">Kazneni poeni</div>
                  <div class="gauge-row">
                    <div
                      class="gauge"
                      [style.background]="
                        'conic-gradient(' +
                        (revoked ? 'var(--signal-red)' : 'var(--warning-amber)') +
                        ' 0 ' + (pct * 3.6) + 'deg, var(--surface-3) ' + (pct * 3.6) + 'deg 360deg)'
                      "
                    >
                      <div class="gauge-inner">
                        <div class="gauge-num pp-mono"
                             [style.color]="revoked ? 'var(--signal-red-ink)' : 'var(--brand-navy-ink)'">
                          {{ d.aktivniPoeni }}
                        </div>
                        <div class="gauge-cap">od {{ cap(d) }}</div>
                      </div>
                    </div>
                    <div class="gauge-text">
                      @if (revoked) {
                        Prekoračen prag — oduzimanje<br/>vozačke dozvole je obavezno.
                      } @else {
                        {{ cap(d) - d.aktivniPoeni }} poena do praga oduzimanja
                        @if (d.dozvola.tip === 'PROBNA') { (probna dozvola) }.
                      }
                    </div>
                  </div>
                </div>
              </div>
            </app-card>
          }
        }
      }
    </div>
  `,
  styles: `
    .hint-row {
      margin-top: 12px;
      display: flex;
      gap: 12px;
      align-items: center;
      font-size: 12px;
      color: var(--ink-2);
    }

    .notfound { display: flex; gap: 12px; align-items: flex-start; }
    .notfound .ico { color: var(--warning-amber-ink); margin-top: 2px; }
    .notfound h3 { margin: 0; font-size: 16px; }
    .notfound p { margin: 4px 0 0; font-size: 13px; color: var(--ink-1); }
    .pp-mono { font-family: var(--font-mono); }

    .driver-head {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      gap: 16px;
    }
    .pp-overline {
      font-size: 11px;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.08em;
      color: var(--ink-2);
      margin-bottom: 8px;
    }
    .driver-name { margin: 0; font-size: 24px; color: var(--brand-navy-ink); font-weight: 600; }
    .driver-meta { display: flex; gap: 10px; margin-top: 8px; align-items: center; }
    .driver-meta .muted { font-size: 13px; color: var(--ink-2); }
    .driver-badges { display: flex; flex-direction: column; align-items: flex-end; gap: 6px; }

    .divider { border: 0; border-top: 1px solid var(--border-1); margin: 18px 0; }

    .driver-body { display: grid; grid-template-columns: 1.4fr 1fr; gap: 24px; }
    .v.cats { display: flex; gap: 4px; flex-wrap: wrap; }

    .gauge-row { display: flex; align-items: center; gap: 14px; }
    .gauge {
      width: 88px; height: 88px;
      border-radius: 50%;
      display: flex; align-items: center; justify-content: center;
    }
    .gauge-inner {
      width: 66px; height: 66px; border-radius: 50%;
      background: var(--surface-1);
      display: flex; flex-direction: column;
      align-items: center; justify-content: center;
    }
    .gauge-num { font-size: 22px; font-weight: 600; line-height: 1; }
    .gauge-cap { font-size: 10px; color: var(--ink-2); }
    .gauge-text { font-size: 13px; color: var(--ink-1); }
  `,
})
export class QuickCheckComponent {
  private readonly sankcije = inject(SankcijeService);

  readonly query = signal('');
  readonly state = signal<LookupState>({ kind: 'empty' });

  // Lucide icon refs exposed to the template
  readonly searchIcon      = Search;
  readonly searchXIcon     = SearchX;
  readonly idCardIcon      = IdCard;
  readonly shieldCheckIcon = ShieldCheck;

  // Re-exposed for use in template expressions
  readonly Math = Math;

  search(): void {
    const q = this.query().trim();
    if (!q) return;
    this.state.set({ kind: 'loading' });

    this.sankcije.getDriver(q).subscribe(driver => {
      if (driver) {
        this.state.set({ kind: 'found', driver });
      } else {
        this.state.set({ kind: 'notFound', query: q });
      }
    });
  }

  /** Threshold cap for the points gauge. */
  cap(d: DriverRecord): number {
    return d.dozvola.tip === 'PROBNA' ? 9 : 18;
  }

  // --- Type guards so the @switch arms get a narrowed state in templates ---

  asNotFound(s: LookupState): Extract<LookupState, { kind: 'notFound' }> | null {
    return s.kind === 'notFound' ? s : null;
  }
  asFound(s: LookupState): Extract<LookupState, { kind: 'found' }> | null {
    return s.kind === 'found' ? s : null;
  }
}
