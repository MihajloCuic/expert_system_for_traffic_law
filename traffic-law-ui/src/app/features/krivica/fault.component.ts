import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { LucideAngularModule, Plus, Trash2, Send, RotateCcw } from 'lucide-angular';

import { ButtonComponent } from '../../shared/ui/button.component';
import { InputComponent }  from '../../shared/ui/input.component';
import { FieldComponent }  from '../../shared/ui/field.component';
import { SelectComponent } from '../../shared/ui/select.component';
import { ToggleComponent } from '../../shared/ui/toggle.component';
import { CardComponent }   from '../../shared/ui/card.component';

// ---- Form models ----

interface PForm {
  id: string;
  fullName: string;
  licensePlate: string;
  maneuver: string;
  speedKmH: string;
  distanceToVehicleAhead: string;
  underAlcoholInfluence: boolean;
  ignoredRedLight: boolean;
  hasLights: boolean;
}

interface AForm {
  participantId: string;
  type: string;
  dateStr: string;   // "YYYY-MM-DD"
  timeStr: string;   // "HH:MM:SS"
}

// ---- Response model ----

interface BlameEntry {
  participantId: string;
  percentage: number;
  type: 'EXCLUSIVE' | 'PREDOMINANT' | 'SHARED' | 'INDIRECT' | 'NONE';
  reasoning: string[];
}

// ---- Constants ----

const SIGNALIZATION_OPTIONS = [
  { value: 'NONE',          label: 'Bez signalizacije' },
  { value: 'STOP_SIGN',     label: 'Stop znak' },
  { value: 'YIELD_SIGN',    label: 'Znak „Ustupi prolaz"' },
  { value: 'TRAFFIC_LIGHT', label: 'Semafor' },
  { value: 'PRIORITY_ROAD', label: 'Prioritetni put' },
];

const VISIBILITY_OPTIONS = [
  { value: 'GOOD', label: 'Dobra' },
  { value: 'POOR', label: 'Loša' },
];

const ROAD_CONDITION_OPTIONS = [
  { value: 'DRY', label: 'Suvo' },
  { value: 'WET', label: 'Mokro' },
  { value: 'ICY', label: 'Zaleđeno' },
];

const MANEUVER_OPTIONS = [
  { value: 'STRAIGHT',                   label: 'Pravo' },
  { value: 'TURNING_LEFT',               label: 'Skretanje levo' },
  { value: 'TURNING_RIGHT',              label: 'Skretanje desno' },
  { value: 'MERGING_ONTO_PRIORITY_ROAD', label: 'Uključivanje na prioritetni put' },
  { value: 'OVERTAKING',                 label: 'Preticanje' },
  { value: 'STOPPED',                    label: 'Zaustavljeno' },
];

const ACTION_TYPE_OPTIONS = [
  { value: 'START_TURNING',      label: 'Početak skretanja' },
  { value: 'ENTER_INTERSECTION', label: 'Ulazak u raskrsnicu' },
  { value: 'BRAKE',              label: 'Kočenje' },
  { value: 'IMPACT',             label: 'Sudar' },
];

function defaultParticipant(n: number): PForm {
  return {
    id: `P${n}`,
    fullName: '',
    licensePlate: '',
    maneuver: 'STRAIGHT',
    speedKmH: '50',
    distanceToVehicleAhead: '0',
    underAlcoholInfluence: false,
    ignoredRedLight: false,
    hasLights: true,
  };
}

function defaultAction(): AForm {
  return { participantId: '', type: '', dateStr: '2024-01-01', timeStr: '10:00:00' };
}

function blameLabel(type: BlameEntry['type']): string {
  switch (type) {
    case 'EXCLUSIVE':   return 'Isključiva krivica';
    case 'PREDOMINANT': return 'Pretežna krivica';
    case 'SHARED':      return 'Deljena krivica';
    case 'INDIRECT':    return 'Posredna krivica';
    case 'NONE':        return 'Bez krivice';
  }
}

function actionTimestamp(a: AForm): string {
  const d = a.dateStr.trim() || '2024-01-01';
  const t = a.timeStr.trim() || '00:00:00';
  return `${d}T${t}`;
}

function extractFaultError(err: unknown): string {
  if (err && typeof err === 'object' && 'message' in err) {
    return `Greška pri analizi: ${(err as { message: string }).message}`;
  }
  return 'Greška pri analizi krivice.';
}

@Component({
  selector: 'app-fault',
  standalone: true,
  imports: [
    LucideAngularModule,
    ButtonComponent, InputComponent, FieldComponent,
    SelectComponent, ToggleComponent, CardComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="page">

      <!-- Page header -->
      <div class="page-head">
        <div>
          <h1>Analiza krivice u saobraćajnoj nesreći</h1>
          <div class="lede">Unesite podatke o sceni i učesnicima. Sistem određuje krivicu primenom Drools pravila.</div>
        </div>
        @if (result()) {
          <app-button variant="secondary" [icon]="resetIcon" (onClick)="reset()">Novo ispitivanje</app-button>
        }
      </div>

      @if (!result()) {

        <!-- Sekcija 1 — Scena nesreće -->
        <div class="section">
          <div class="section-head">
            <h2><span class="num">1</span> Scena nesreće</h2>
          </div>
          <div class="grid-2">
            <app-field label="Signalizacija">
              <app-select
                [options]="SIGNALIZATION_OPTIONS"
                [value]="signalization()"
                (valueChange)="signalization.set($event)"
              />
            </app-field>
            <app-field label="Vidljivost">
              <app-select
                [options]="VISIBILITY_OPTIONS"
                [value]="visibility()"
                (valueChange)="visibility.set($event)"
              />
            </app-field>
            <app-field label="Stanje puta">
              <app-select
                [options]="ROAD_CONDITION_OPTIONS"
                [value]="roadCondition()"
                (valueChange)="roadCondition.set($event)"
              />
            </app-field>
            <app-field label="Ograničenje brzine" hint="km/h">
              <app-input
                type="number"
                [value]="speedLimitKmH()"
                (valueChange)="speedLimitKmH.set($event)"
                placeholder="50"
              />
            </app-field>
          </div>
          <div class="toggle-row">
            <app-toggle
              [on]="intersection()"
              label="Raskrsnica"
              (onChange)="intersection.set($event)"
            />
          </div>
        </div>

        <!-- Sekcija 2 — Učesnici -->
        <div class="section">
          <div class="section-head">
            <h2><span class="num">2</span> Učesnici</h2>
            <span class="meta">{{ participants().length }} · minimum 2</span>
          </div>

          @for (p of participants(); track $index; let i = $index) {
            <div class="participant-card" [class.mt]="i > 0">
              <div class="participant-head">
                <span class="participant-num">{{ i + 1 }}</span>
                <span class="participant-label">{{ p.id || ('Učesnik ' + (i + 1)) }}</span>
                @if (participants().length > 2) {
                  <app-button variant="ghost" size="sm" [icon]="trashIcon" (onClick)="removeParticipant(i)">
                    Ukloni
                  </app-button>
                }
              </div>

              <div class="grid-3">
                <app-field label="ID učesnika" [required]="true">
                  <app-input
                    [value]="p.id"
                    (valueChange)="patchP(i, 'id', $event)"
                    placeholder="P1"
                  />
                </app-field>
                <app-field label="Ime vozača" [required]="true">
                  <app-input
                    [value]="p.fullName"
                    (valueChange)="patchP(i, 'fullName', $event)"
                    placeholder="Ime Prezime"
                  />
                </app-field>
                <app-field label="Registarski broj">
                  <app-input
                    [mono]="true"
                    [value]="p.licensePlate"
                    (valueChange)="patchP(i, 'licensePlate', $event)"
                    placeholder="NS-001-AA"
                  />
                </app-field>
              </div>

              <div class="grid-3 mt-12">
                <app-field label="Manevar">
                  <app-select
                    [options]="MANEUVER_OPTIONS"
                    [value]="p.maneuver"
                    (valueChange)="patchP(i, 'maneuver', $event)"
                  />
                </app-field>
                <app-field label="Brzina" hint="km/h">
                  <app-input
                    type="number"
                    [value]="p.speedKmH"
                    (valueChange)="patchP(i, 'speedKmH', $event)"
                    placeholder="50"
                  />
                </app-field>
                <app-field label="Distanca do vozila ispred" hint="m · 0 = nema vozila ispred">
                  <app-input
                    type="number"
                    [value]="p.distanceToVehicleAhead"
                    (valueChange)="patchP(i, 'distanceToVehicleAhead', $event)"
                    placeholder="0"
                  />
                </app-field>
              </div>

              <div class="toggle-row mt-14">
                <app-toggle
                  [on]="p.underAlcoholInfluence"
                  label="Pod uticajem alkohola"
                  (onChange)="patchP(i, 'underAlcoholInfluence', $event)"
                />
                <app-toggle
                  [on]="p.ignoredRedLight"
                  label="Ignorisao crveno svetlo"
                  (onChange)="patchP(i, 'ignoredRedLight', $event)"
                />
                <app-toggle
                  [on]="p.hasLights"
                  label="Ispravna svetla na vozilu"
                  (onChange)="patchP(i, 'hasLights', $event)"
                />
              </div>
            </div>
          }

          <div class="add-row">
            <app-button variant="secondary" size="sm" [icon]="plusIcon" (onClick)="addParticipant()">
              Dodaj učesnika
            </app-button>
          </div>
        </div>

        <!-- Sekcija 3 — CEP događaji (opciono) -->
        <div class="section">
          <div class="section-head">
            <h2><span class="num">3</span> CEP događaji</h2>
            <app-toggle
              [on]="showActions()"
              label="Uključi vremenski sled događaja"
              (onChange)="showActions.set($event)"
            />
          </div>

          @if (showActions()) {
            <div class="action-header-row">
              <span class="ah">ID učesnika</span>
              <span class="ah">Tip akcije</span>
              <span class="ah">Datum (YYYY-MM-DD)</span>
              <span class="ah">Vreme (HH:MM:SS)</span>
              <span></span>
            </div>

            @for (a of actions(); track $index; let i = $index) {
              <div class="action-row">
                <app-input
                  [value]="a.participantId"
                  (valueChange)="patchA(i, 'participantId', $event)"
                  placeholder="P1"
                />
                <app-select
                  [options]="ACTION_TYPE_OPTIONS"
                  [value]="a.type"
                  placeholder="— izaberi tip —"
                  (valueChange)="patchA(i, 'type', $event)"
                />
                <app-input
                  [mono]="true"
                  [value]="a.dateStr"
                  (valueChange)="patchA(i, 'dateStr', $event)"
                  placeholder="2024-01-01"
                />
                <app-input
                  [mono]="true"
                  [value]="a.timeStr"
                  (valueChange)="patchA(i, 'timeStr', $event)"
                  placeholder="10:00:00"
                />
                <div class="action-rm">
                  <app-button variant="ghost" size="sm" [icon]="trashIcon" (onClick)="removeAction(i)">
                    Ukloni
                  </app-button>
                </div>
              </div>
            }

            <div class="action-preview">
              @for (a of actions(); track $index) {
                @if (a.participantId && a.type) {
                  <span class="preview-chip">
                    {{ a.participantId }} · {{ a.type }} · {{ actionTimestamp(a) }}
                  </span>
                }
              }
            </div>

            <div class="add-row">
              <app-button variant="secondary" size="sm" [icon]="plusIcon" (onClick)="addAction()">
                Dodaj događaj
              </app-button>
            </div>
          } @else {
            <p class="hint-muted">Opcioni CEP unos — uključite za analizu hronološkog redosleda događaja.</p>
          }
        </div>

        <!-- Akcijska traka -->
        <div class="actionbar">
          <div class="summary">
            <b>{{ participantSummary() }}</b>
          </div>
          <app-button
            [icon]="sendIcon"
            [disabled]="submitting() || !canSubmit()"
            (onClick)="submit()"
          >
            {{ submitting() ? 'Analiza u toku…' : 'Pokreni analizu krivice' }}
          </app-button>
        </div>

        @if (error()) {
          <div class="error-row">{{ error() }}</div>
        }
      }

      <!-- Prikaz rezultata -->
      @if (result(); as blames) {
        <div class="result-section">
          <div class="result-head">
            <h2>Rezultat analize krivice</h2>
          </div>

          @if (blames.length === 0) {
            <div class="empty">
              <div class="empty-title">Nema rezultata</div>
              <div class="empty-sub">Backend nije vratio nijednog krivca — proverite unete podatke.</div>
            </div>
          } @else {
            <div class="blame-grid">
              @for (b of blames; track b.participantId) {
                <app-card>
                  <div class="blame-card">
                    <div class="blame-top">
                      <div class="blame-pct">
                        {{ b.percentage }}<span class="blame-sym">%</span>
                      </div>
                      <div class="blame-meta">
                        <div class="blame-pid">Učesnik {{ b.participantId }}</div>
                        <span class="blame-badge blame-{{ b.type }}">{{ blameLabel(b.type) }}</span>
                      </div>
                    </div>
                    @if (b.reasoning.length > 0) {
                      <div class="blame-reasoning">
                        @for (r of b.reasoning; track r) {
                          <div class="reasoning-item">{{ r }}</div>
                        }
                      </div>
                    }
                  </div>
                </app-card>
              }
            </div>
          }

          <div class="result-actions">
            <app-button variant="secondary" [icon]="resetIcon" (onClick)="reset()">
              Novo ispitivanje
            </app-button>
          </div>
        </div>
      }

    </div>
  `,
  styles: `
    .section {
      background: var(--surface-1);
      border: 1px solid var(--border-1);
      border-radius: var(--r-2);
      padding: 22px 24px;
      margin-bottom: 18px;
    }
    .section-head {
      display: flex;
      align-items: baseline;
      justify-content: space-between;
      margin-bottom: 18px;
      gap: 12px;
    }
    .section-head h2 {
      font-size: 16px;
      font-weight: 600;
      margin: 0;
      color: var(--brand-navy-ink);
      display: flex;
      align-items: center;
      gap: 10px;
    }
    .num {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 22px;
      height: 22px;
      border-radius: 50%;
      background: var(--brand-navy);
      color: white;
      font-family: var(--font-mono);
      font-size: 12px;
      font-weight: 600;
    }
    .meta { font-size: 12px; color: var(--ink-2); }

    .grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 16px 20px; }
    .grid-3 { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 16px 20px; }
    .mt-12  { margin-top: 12px; }
    .mt-14  { margin-top: 14px; }

    .toggle-row {
      display: flex;
      flex-wrap: wrap;
      gap: 16px 28px;
      margin-top: 16px;
    }

    .participant-card {
      background: var(--surface-2);
      border: 1px solid var(--border-1);
      border-radius: var(--r-2);
      padding: 18px 20px;
    }
    .participant-card.mt { margin-top: 12px; }
    .participant-head {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 16px;
    }
    .participant-num {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 20px;
      height: 20px;
      border-radius: 50%;
      background: var(--border-2);
      color: var(--ink-1);
      font-family: var(--font-mono);
      font-size: 11px;
      font-weight: 600;
      flex: none;
    }
    .participant-label {
      font-size: 14px;
      font-weight: 500;
      color: var(--ink-0);
      flex: 1;
      font-family: var(--font-mono);
    }

    .add-row { margin-top: 14px; }

    /* CEP tabela – 5 kolona */
    .action-header-row {
      display: grid;
      grid-template-columns: 1fr 1.4fr 1.2fr 1fr auto;
      gap: 8px 12px;
      padding: 0 0 6px 0;
      border-bottom: 1px solid var(--border-1);
      margin-bottom: 4px;
    }
    .ah {
      font-size: 11px;
      font-weight: 600;
      color: var(--ink-2);
      text-transform: uppercase;
      letter-spacing: 0.04em;
    }
    .action-row {
      display: grid;
      grid-template-columns: 1fr 1.4fr 1.2fr 1fr auto;
      gap: 8px 12px;
      align-items: center;
      padding: 8px 0;
      border-bottom: 1px solid var(--border-1);
    }
    .action-rm { justify-self: end; }

    .action-preview {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
      margin-top: 10px;
      min-height: 24px;
    }
    .preview-chip {
      font-size: 11px;
      font-family: var(--font-mono);
      color: var(--ink-1);
      background: var(--surface-2);
      border: 1px solid var(--border-1);
      border-radius: var(--r-1);
      padding: 2px 8px;
    }

    .hint-muted {
      margin: 0;
      padding: 12px 14px;
      background: var(--surface-sunken);
      border: 1px solid var(--border-1);
      border-radius: var(--r-2);
      font-size: 13px;
      color: var(--ink-2);
    }

    .actionbar {
      position: sticky;
      bottom: 0;
      background: var(--surface-1);
      border: 1px solid var(--border-1);
      border-radius: var(--r-2);
      padding: 12px 16px;
      display: flex;
      align-items: center;
      gap: 12px;
      box-shadow: var(--shadow-1);
      margin-top: 18px;
    }
    .summary { font-size: 13px; color: var(--ink-1); flex: 1; }
    .summary b { color: var(--ink-0); font-weight: 500; }

    .error-row {
      margin-top: 12px;
      padding: 10px 14px;
      background: var(--signal-red-soft);
      color: var(--signal-red-ink);
      border-radius: var(--r-2);
      font-size: 13px;
    }

    .result-section { margin-top: 4px; }
    .result-head {
      display: flex;
      align-items: baseline;
      justify-content: space-between;
      margin-bottom: 18px;
      gap: 16px;
    }
    .result-head h2 {
      font-size: 22px;
      font-weight: 600;
      margin: 0;
      color: var(--brand-navy-ink);
    }
    .result-actions { margin-top: 20px; }

    .blame-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
      gap: 14px;
    }
    .blame-card { display: flex; flex-direction: column; gap: 14px; }
    .blame-top  { display: flex; align-items: center; gap: 16px; }
    .blame-pct {
      font-size: 52px;
      font-weight: 700;
      line-height: 1;
      font-family: var(--font-mono);
      color: var(--brand-navy-ink);
      letter-spacing: -0.03em;
    }
    .blame-sym {
      font-size: 28px;
      font-weight: 400;
      color: var(--ink-2);
      letter-spacing: 0;
    }
    .blame-meta  { display: flex; flex-direction: column; gap: 6px; }
    .blame-pid   { font-size: 13px; font-family: var(--font-mono); color: var(--ink-1); }

    .blame-badge {
      display: inline-flex;
      align-items: center;
      min-height: 22px;
      padding: 3px 8px;
      border-radius: var(--r-1);
      font-size: 12px;
      font-weight: 500;
      line-height: 1.35;
    }
    .blame-EXCLUSIVE   { background: var(--signal-red-soft);    color: var(--signal-red-ink);    border: 1px solid oklch(85% 0.05 25); }
    .blame-PREDOMINANT { background: var(--warning-amber-soft); color: var(--warning-amber-ink); border: 1px solid oklch(88% 0.05 80); }
    .blame-SHARED      { background: oklch(97% 0.06 88);        color: oklch(44% 0.14 88);       border: 1px solid oklch(88% 0.07 88); }
    .blame-INDIRECT    { background: oklch(96% 0.02 300);       color: oklch(42% 0.12 300);      border: 1px solid oklch(86% 0.04 300); }
    .blame-NONE        { background: var(--success-green-soft); color: var(--success-green-ink); border: 1px solid oklch(86% 0.06 155); }

    .blame-reasoning { display: flex; flex-direction: column; gap: 4px; }
    .reasoning-item {
      font-size: 12px;
      font-family: var(--font-mono);
      color: var(--ink-1);
      padding: 4px 8px;
      background: var(--surface-3);
      border-radius: var(--r-1);
      line-height: 1.4;
      word-break: break-word;
    }

    .empty {
      background: var(--surface-1);
      border: 1px dashed var(--border-2);
      border-radius: var(--r-2);
      padding: 36px 24px;
      text-align: center;
    }
    .empty-title { font-size: 15px; font-weight: 500; color: var(--ink-1); margin-bottom: 6px; }
    .empty-sub   { font-size: 13px; color: var(--ink-2); }
  `,
})
export class FaultComponent {
  private readonly http = inject(HttpClient);

  readonly plusIcon  = Plus;
  readonly trashIcon = Trash2;
  readonly sendIcon  = Send;
  readonly resetIcon = RotateCcw;

  readonly actionTimestamp = actionTimestamp;

  readonly signalization = signal('NONE');
  readonly visibility    = signal('GOOD');
  readonly roadCondition = signal('DRY');
  readonly speedLimitKmH = signal('50');
  readonly intersection  = signal(false);

  readonly participants = signal<PForm[]>([
    defaultParticipant(1),
    defaultParticipant(2),
  ]);

  readonly showActions = signal(false);
  readonly actions     = signal<AForm[]>([]);

  readonly submitting = signal(false);
  readonly error      = signal<string | null>(null);
  readonly result     = signal<BlameEntry[] | null>(null);

  readonly SIGNALIZATION_OPTIONS   = SIGNALIZATION_OPTIONS;
  readonly VISIBILITY_OPTIONS      = VISIBILITY_OPTIONS;
  readonly ROAD_CONDITION_OPTIONS  = ROAD_CONDITION_OPTIONS;
  readonly MANEUVER_OPTIONS        = MANEUVER_OPTIONS;
  readonly ACTION_TYPE_OPTIONS     = ACTION_TYPE_OPTIONS;

  readonly blameLabel = blameLabel;

  readonly canSubmit = computed(() => {
    if (this.submitting()) return false;
    const ps = this.participants();
    if (ps.length < 2) return false;
    return ps.every(p => p.id.trim().length > 0 && p.fullName.trim().length > 0);
  });

  readonly participantSummary = computed(() => {
    const ps = this.participants();
    const ids = ps.map(p => p.id || '?').join(', ');
    return `${ps.length} učesnika: ${ids}`;
  });

  addParticipant() {
    this.participants.update(list => [...list, defaultParticipant(list.length + 1)]);
  }

  removeParticipant(i: number) {
    if (this.participants().length <= 2) return;
    this.participants.update(list => list.filter((_, idx) => idx !== i));
  }

  patchP(i: number, field: string, val: string | boolean) {
    this.participants.update(list =>
      list.map((p, idx) => idx === i ? { ...p, [field]: val } as PForm : p)
    );
  }

  addAction() {
    this.actions.update(list => [...list, defaultAction()]);
  }

  removeAction(i: number) {
    this.actions.update(list => list.filter((_, idx) => idx !== i));
  }

  patchA(i: number, field: string, val: string) {
    this.actions.update(list =>
      list.map((a, idx) => idx === i ? { ...a, [field]: val } as AForm : a)
    );
  }

  submit() {
    if (!this.canSubmit()) return;
    this.submitting.set(true);
    this.error.set(null);
    this.result.set(null);

    const now = new Date().toISOString().substring(0, 19);
    const ps  = this.participants();

    const payload = {
      accident: {
        id: `ACC-${Date.now()}`,
        occurredAt: now,
        scene: {
          signalization: this.signalization(),
          visibility:    this.visibility(),
          roadCondition: this.roadCondition(),
          speedLimitKmH: parseInt(this.speedLimitKmH(), 10) || 50,
          intersection:  this.intersection(),
        },
        participants: ps.map(p => ({
          id: p.id,
          driver: {
            id:               `D${p.id}`,
            fullName:          p.fullName,
            yearsOfExperience: 0,
            penaltyPoints:     0,
          },
          vehicle: {
            licensePlate: p.licensePlate,
            category:     'CAR',
            hasLights:    p.hasLights,
          },
          maneuver:                p.maneuver,
          speedKmH:                parseInt(p.speedKmH, 10)               || 0,
          distanceToVehicleAhead:  parseInt(p.distanceToVehicleAhead, 10) || 0,
          underAlcoholInfluence:   p.underAlcoholInfluence,
          ignoredRedLight:         p.ignoredRedLight,
        })),
        injuredPersons:    false,
        fatalOutcome:      false,
        materialDamageRsd: 0,
      },
      declaredFaults: [],
      actions: this.showActions()
        ? this.actions()
            .filter(a => a.participantId.trim() && a.type)
            .map(a => ({
              participant: { id: a.participantId },
              type:        a.type,
              timestamp:   actionTimestamp(a),
            }))
        : [],
    };

    this.http
      .post<{ blames: BlameEntry[] }>('http://localhost:8080/api/module2/determine-fault', payload)
      .subscribe({
        next: resp => {
          this.result.set(resp.blames);
          this.submitting.set(false);
        },
        error: err => {
          this.submitting.set(false);
          this.error.set(extractFaultError(err));
        },
      });
  }

  reset() {
    this.signalization.set('NONE');
    this.visibility.set('GOOD');
    this.roadCondition.set('DRY');
    this.speedLimitKmH.set('50');
    this.intersection.set(false);
    this.participants.set([defaultParticipant(1), defaultParticipant(2)]);
    this.showActions.set(false);
    this.actions.set([]);
    this.result.set(null);
    this.error.set(null);
    this.submitting.set(false);
  }
}