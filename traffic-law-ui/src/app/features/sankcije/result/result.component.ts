import { ChangeDetectionStrategy, Component, computed, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import {
  LucideAngularModule,
  Printer, Plus, ShieldX, FileText, Lock, Ban, Hammer,
} from 'lucide-angular';

import { ButtonComponent } from '../../../shared/ui/button.component';
import { CardComponent }   from '../../../shared/ui/card.component';
import { BadgeComponent }  from '../../../shared/ui/badge.component';
import { TagComponent }    from '../../../shared/ui/tag.component';

import { Range, Violation } from '../../../shared/types';
import { fmtN, fmtRange, fmtRsdRange, sumRange } from '../../../shared/format';
import { SankcijeStateService } from '../sankcije-state.service';

const ZERO: Range = [0, 0];

@Component({
  selector: 'app-result',
  standalone: true,
  imports: [
    LucideAngularModule,
    ButtonComponent, CardComponent, BadgeComponent, TagComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (data(); as r) {
      <div class="page">
        <!-- Page header -->
        <div class="page-head">
          <div>
            <div class="pp-overline">Rezultat obrade · #PR-{{ caseId() }}</div>
            <h1>Sankcije izrečene</h1>
          </div>
          <div class="head-actions">
            <app-button variant="secondary" [icon]="printerIcon" (onClick)="onPrint()">Štampaj nalog</app-button>
            <app-button [icon]="plusIcon" (onClick)="onNew()">Nova prijava</app-button>
          </div>
        </div>

        <!-- Revocation banner -->
        @if (r.revoked) {
          <div class="banner">
            <div class="ico"><lucide-angular [img]="shieldXIcon" [size]="24"/></div>
            <div class="msg">
              @if (r.previouslyRevoked) {
                <h3>Vozačka dozvola je već oduzeta.</h3>
                <p>
                  Vozač <b>{{ r.driver.ime }} {{ r.driver.prezime }}</b>
                  je već pod oduzimanjem vozačke dozvole. Aktuelno stanje:
                  <b class="pp-mono">{{ r.totalPoints }}</b> aktivnih kaznenih
                  poena ({{ r.priorPoints }} ranije + {{ r.newPoints }} novih).
                </p>
              } @else {
                <h3>Oduzimanje vozačke dozvole.</h3>
                <p>
                  Vozač <b>{{ r.driver.ime }} {{ r.driver.prezime }}</b> ima ukupno
                  <b class="pp-mono">{{ r.totalPoints }}</b> aktivnih kaznenih poena
                  ({{ r.priorPoints }} ranije + {{ r.newPoints }} novih),
                  što prelazi prag {{ r.cap }} za
                  {{ r.driver.licType === 'PROBNA' ? 'probnu' : 'stalnu' }} dozvolu.
                </p>
              }
              <div class="law">Zakon o bezbednosti saobraćaja · čl. 199 st. 1</div>
            </div>
          </div>
        }

        <!-- Main grid -->
        <div class="result-grid">
          <div class="col">
            <!-- Sankcije table -->
            <app-card pad="0">
              <div class="table-head">
                <div>
                  <h3>Izrečene sankcije</h3>
                  <div class="caption">{{ r.sanctions.length }} stavki · raspon po zakonu</div>
                </div>
              </div>
              <table class="sanctions">
                <thead>
                  <tr>
                    <th>Prekršaj</th>
                    <th>Član</th>
                    <th class="right">Poeni</th>
                    <th class="right">Novčana kazna</th>
                  </tr>
                </thead>
                <tbody>
                  @for (s of r.sanctions; track s.code) {
                    <tr>
                      <td>
                        <div class="opis">{{ s.opis }}</div>
                        <div class="measures">
                          @if (s.zatvorDana) {
                            <app-badge tone="red">
                              <lucide-angular [img]="lockIcon" [size]="12"/>
                              Zatvor · {{ fmtRange(s.zatvorDana) }} dana
                            </app-badge>
                          }
                          @if (s.zabranaMeseci) {
                            <app-badge tone="amber">
                              <lucide-angular [img]="banIcon" [size]="12"/>
                              Zabrana upravljanja · {{ fmtRange(s.zabranaMeseci) }} meseci
                            </app-badge>
                          }
                          @if (s.koristanRadSati) {
                            <app-badge tone="blue">
                              <lucide-angular [img]="hammerIcon" [size]="12"/>
                              Društveno koristan rad · {{ fmtRange(s.koristanRadSati) }} sati
                            </app-badge>
                          }
                        </div>
                      </td>
                      <td><app-tag>{{ s.clan }}</app-tag></td>
                      <td class="right pp-mono">{{ s.poeni }}</td>
                      <td class="right pp-mono">{{ fmtRsdRange(s.kaznaRsd) }}</td>
                    </tr>
                  }
                </tbody>
                <tfoot>
                  <tr>
                    <td colspan="2">Ukupno</td>
                    <td class="right pp-mono">{{ r.newPoints }}</td>
                    <td class="right pp-mono">{{ fmtRsdRange(totals().fine) }}</td>
                  </tr>
                </tfoot>
              </table>
            </app-card>

            <!-- Dodatne mere · ukupno -->
            @if (hasAnyExtra()) {
              <div class="extra-stats">
                @if (totals().prison[1] > 0) {
                  <div class="stat red">
                    <lucide-angular [img]="lockIcon" [size]="20"/>
                    <div class="stat-body">
                      <div class="stat-label">Zatvor</div>
                      <div class="stat-val pp-mono">{{ fmtRange(totals().prison) }} dana</div>
                    </div>
                  </div>
                }
                @if (totals().ban[1] > 0) {
                  <div class="stat amber">
                    <lucide-angular [img]="banIcon" [size]="20"/>
                    <div class="stat-body">
                      <div class="stat-label">Zabrana upravljanja</div>
                      <div class="stat-val pp-mono">{{ fmtRange(totals().ban) }} dana</div>
                    </div>
                  </div>
                }
                @if (totals().work[1] > 0) {
                  <div class="stat blue">
                    <lucide-angular [img]="hammerIcon" [size]="20"/>
                    <div class="stat-body">
                      <div class="stat-label">Društveno koristan rad</div>
                      <div class="stat-val pp-mono">{{ fmtRange(totals().work) }} sati</div>
                    </div>
                  </div>
                }
              </div>
            }

            <!-- Vozač · zapisnik prijave -->
            <app-card>
              <h3 class="card-h">Vozač · zapisnik prijave</h3>
              <div class="kv">
                <div class="k">Ime i prezime</div>  <div class="v">{{ r.driver.ime }} {{ r.driver.prezime }}</div>
                <div class="k">JMBG</div>           <div class="v mono">{{ r.driver.jmbg }}</div>
                <div class="k">Vozačka dozvola</div>
                <div class="v">
                  @if (r.driver.licNum) {
                    <span class="mono">{{ r.driver.licNum }}</span>
                    <app-badge tone="neutral">{{ r.driver.licType }}</app-badge>
                  } @else {
                    <app-badge tone="red">Bez dozvole</app-badge>
                  }
                </div>
                <div class="k">Vozilo</div>         <div class="v mono">{{ r.driver.plate || '—' }}</div>
              </div>
            </app-card>
          </div>

          <div class="col">
            <!-- Stanje kaznenih poena -->
            <app-card>
              <h3 class="card-h">Stanje kaznenih poena</h3>
              <div class="gauge-row">
                <div
                  class="gauge"
                  [style.background]="
                    'conic-gradient(' +
                    (r.revoked ? 'var(--signal-red)' : 'var(--warning-amber)') +
                    ' 0 ' + (pointsPct() * 3.6) + 'deg, var(--surface-3) ' + (pointsPct() * 3.6) + 'deg 360deg)'
                  "
                >
                  <div class="gauge-inner">
                    <div class="gauge-num pp-mono"
                         [style.color]="r.revoked ? 'var(--signal-red-ink)' : 'var(--brand-navy-ink)'">
                      {{ r.totalPoints }}
                    </div>
                    <div class="gauge-cap">od {{ r.cap }}</div>
                  </div>
                </div>
                <div class="gauge-side">
                  <div>Prethodno: <span class="pp-mono">{{ r.priorPoints }}</span></div>
                  <div class="new-pts">Novih: <span class="pp-mono">+{{ r.newPoints }}</span></div>
                  <hr/>
                  <div><b>Ukupno: <span class="pp-mono">{{ r.totalPoints }}</span></b></div>
                </div>
              </div>
            </app-card>

            <!-- Status dozvole -->
            <app-card>
              <h3 class="card-h">Status dozvole</h3>
              @if (r.revoked) {
                <div class="status status-red">ODUZETA</div>
                <p>Mera oduzimanja stupa na snagu danom uručenja rešenja.</p>
              } @else {
                <div class="status status-green">AKTIVNA</div>
                <p>Dozvola ostaje na snazi. Sledeći prag: {{ r.cap }} poena.</p>
              }
            </app-card>

            <!-- Naredni koraci -->
            <app-card>
              <h3 class="card-h">Naredni koraci</h3>
              <ol class="steps">
                <li>Štampaj nalog za plaćanje i uruči vozaču.</li>
                <li>Snimi PDF prijave u službeni log.</li>
                @if (r.revoked) {
                  <li class="step-urgent">Pokreni postupak oduzimanja u roku od 24 h.</li>
                }
              </ol>
            </app-card>
          </div>
        </div>
      </div>
    } @else {
      <div class="page stub-page">
        <h2>Nema obrađene prijave.</h2>
        <p>Pošaljite prijavu prekršaja iz forme — rezultat se prikazuje ovde.</p>
      </div>
    }
  `,
  styles: `
    .head-actions { display: flex; gap: 10px; }

    .pp-overline {
      font-size: 11px; font-weight: 600; text-transform: uppercase;
      letter-spacing: 0.08em; color: var(--ink-2); margin-bottom: 8px;
    }
    .pp-mono { font-family: var(--font-mono); }

    /* Revocation banner */
    .banner {
      background: oklch(96% 0.03 25);
      border: 1px solid oklch(82% 0.08 25);
      color: var(--signal-red-ink);
      border-radius: var(--r-2);
      padding: 16px 18px;
      display: grid;
      grid-template-columns: 24px 1fr;
      gap: 14px;
      align-items: start;
      margin-bottom: 18px;
    }
    .banner .ico { color: var(--signal-red); margin-top: 1px; align-self: start; }
    .banner h3 { margin: 0 0 4px; font-size: 16px; color: var(--signal-red-ink); }
    .banner p { margin: 0; font-size: 13px; color: var(--ink-0); }
    .banner .law { margin-top: 8px; font-family: var(--font-mono); font-size: 11px; color: var(--ink-2); }

    /* Result grid */
    .result-grid { display: grid; grid-template-columns: 2fr 1fr; gap: 18px; }
    .col { display: flex; flex-direction: column; gap: 14px; }

    /* Sanctions table */
    .table-head {
      padding: 18px 22px 12px;
      display: flex; justify-content: space-between; align-items: baseline;
    }
    .table-head h3 { margin: 0; font-size: 16px; color: var(--brand-navy-ink); }
    .caption { font-size: 12px; color: var(--ink-2); margin-top: 4px; }

    .sanctions {
      width: 100%;
      border-collapse: collapse;
      font-size: 13px;
    }
    .sanctions thead th {
      text-align: left;
      font-size: 11px;
      text-transform: uppercase;
      letter-spacing: 0.06em;
      color: var(--ink-2);
      padding: 10px 22px;
      background: var(--surface-3);
      border-top: 1px solid var(--border-1);
      border-bottom: 1px solid var(--border-1);
    }
    .sanctions th.right, .sanctions td.right { text-align: right; }
    .sanctions tbody td { padding: 18px 22px 22px; border-bottom: 1px solid var(--border-1); vertical-align: top; }
    .opis { color: var(--ink-0); font-size: 14px; }
    .measures { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 12px; }
    .measures app-badge { display: inline-flex; }
    .sanctions tfoot td {
      padding: 12px 22px;
      background: var(--surface-2);
      font-weight: 600;
      color: var(--ink-0);
    }

    /* Extra-measure stat blocks */
    .extra-stats { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
    .stat {
      padding: 14px 16px;
      border-radius: var(--r-2);
      display: flex; gap: 12px; align-items: center;
      border: 1px solid var(--border-1);
    }
    .stat.red   { background: var(--signal-red-soft);    color: var(--signal-red-ink);    border-color: oklch(85% 0.05 25); }
    .stat.amber { background: var(--warning-amber-soft); color: var(--warning-amber-ink); border-color: oklch(88% 0.05 80); }
    .stat.blue  { background: var(--info-blue-soft);     color: var(--info-blue-ink);     border-color: oklch(86% 0.04 245); }
    .stat-label { font-size: 11px; text-transform: uppercase; letter-spacing: 0.06em; }
    .stat-val   { font-size: 16px; font-weight: 600; margin-top: 2px; }

    .card-h { margin: 0 0 14px; font-size: 16px; color: var(--brand-navy-ink); }

    .kv { display: grid; grid-template-columns: 140px 1fr; gap: 8px 12px; font-size: 14px; }
    .kv .k { color: var(--ink-1); }
    .kv .v { color: var(--ink-0); display: inline-flex; align-items: center; gap: 8px; }
    .kv .v.mono, .kv .v .mono { font-family: var(--font-mono); }

    /* Gauge */
    .gauge-row { display: flex; align-items: center; gap: 16px; }
    .gauge {
      width: 96px; height: 96px;
      border-radius: 50%;
      display: flex; align-items: center; justify-content: center;
    }
    .gauge-inner {
      width: 72px; height: 72px; border-radius: 50%;
      background: var(--surface-1);
      display: flex; flex-direction: column;
      align-items: center; justify-content: center;
    }
    .gauge-num { font-size: 24px; font-weight: 600; line-height: 1; }
    .gauge-cap { font-size: 10px; color: var(--ink-2); }
    .gauge-side { font-size: 13px; color: var(--ink-1); display: flex; flex-direction: column; gap: 4px; }
    .gauge-side hr { border: 0; border-top: 1px solid var(--border-1); margin: 4px 0; }
    .new-pts { color: var(--signal-red-ink); }

    /* Status dozvole */
    .status { font-size: 24px; font-weight: 700; margin-bottom: 6px; letter-spacing: -0.01em; }
    .status-red   { color: var(--signal-red); }
    .status-green { color: var(--success-green); }
    app-card p { margin: 0; font-size: 13px; color: var(--ink-1); }

    /* Steps */
    .steps { margin: 0; padding-left: 20px; font-size: 13px; color: var(--ink-0); }
    .steps li { margin-bottom: 6px; }
    .step-urgent { color: var(--signal-red-ink); font-weight: 500; }
  `,
})
export class ResultComponent implements OnInit {
  private readonly state  = inject(SankcijeStateService);
  private readonly router = inject(Router);

  readonly printerIcon  = Printer;
  readonly plusIcon     = Plus;
  readonly shieldXIcon  = ShieldX;
  readonly fileTextIcon = FileText;
  readonly lockIcon     = Lock;
  readonly banIcon      = Ban;
  readonly hammerIcon   = Hammer;

  readonly data = this.state.lastResult;

  /**
   * Authoritative totals from the backend (sticaj-prekršaja cap already
   * applied at Layer 5). We do NOT re-sum kaznaRsd locally — that ignores
   * the cap. We DO fall back to a local sum for koristanRadSati because
   * the backend's SanctionSummary doesn't carry it yet.
   */
  readonly totals = computed(() => {
    const r = this.data();
    if (!r) return { fine: ZERO, prison: ZERO, ban: ZERO, work: ZERO };
    // Backend totals carry the cap; use as-is for fine/prison/ban.
    let work: Range = ZERO;
    for (const s of r.sanctions) {
      if (s.koristanRadSati) work = sumRange(work, s.koristanRadSati);
    }
    return {
      fine:   r.totals.fine,
      prison: r.totals.prison,
      ban:    r.totals.ban,
      work,
    };
  });

  readonly pointsPct = computed(() => {
    const r = this.data();
    if (!r) return 0;
    return Math.min(100, (r.totalPoints / r.cap) * 100);
  });

  readonly hasAnyExtra = computed(() => {
    const t = this.totals();
    return t.prison[1] > 0 || t.ban[1] > 0 || t.work[1] > 0;
  });

  readonly caseId = computed(() => {
    // Random-ish but stable for this navigation
    const now = new Date();
    return `${now.getFullYear()}-${(Math.floor(Math.random() * 90000) + 10000)}`;
  });

  ngOnInit() {
    if (!this.data()) {
      this.router.navigate(['/violation']);
    }
  }

  // Template helpers
  fmtN = fmtN;
  fmtRange = fmtRange;
  fmtRsdRange = fmtRsdRange;

  onPrint() { window.print(); }
  onNew()   { this.state.clear(); this.router.navigate(['/violation']); }
}
