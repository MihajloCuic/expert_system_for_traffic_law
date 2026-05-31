import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { LucideAngularModule, AlertTriangle, Send } from 'lucide-angular';

import { ButtonComponent }       from '../../../shared/ui/button.component';
import { InputComponent }        from '../../../shared/ui/input.component';
import { FieldComponent }        from '../../../shared/ui/field.component';
import { SelectComponent }       from '../../../shared/ui/select.component';
import { MultiSelectComponent }  from '../../../shared/ui/multi-select.component';
import { ToggleComponent }       from '../../../shared/ui/toggle.component';

import {
  CATEGORIES, VEHICLE_TYPES, VIOLATIONS, SPEEDING_OPTIONS, parseSerbianDecimal,
} from '../../../shared/catalogues';
import { LicenceType, ViolationSubmission } from '../../../shared/types';
import { SankcijeService } from '../sankcije.service';
import { SankcijeStateService } from '../sankcije-state.service';

import { BacIndicatorComponent } from './bac-indicator.component';

@Component({
  selector: 'app-violation-form',
  standalone: true,
  imports: [
    LucideAngularModule,
    ButtonComponent, InputComponent, FieldComponent,
    SelectComponent, MultiSelectComponent, ToggleComponent,
    BacIndicatorComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="page">
      <div class="page-head">
        <div>
          <h1>Prijava prekršaja</h1>
          <div class="lede">
            Popunite obavezna polja. Po slanju, sistem vraća sankcije, kaznene poene i status dozvole.
          </div>
        </div>
      </div>

      <!-- Section 1 — Podaci o vozaču -->
      <div class="section">
        <div class="section-head">
          <h2><span class="num">1</span> Podaci o vozaču</h2>
          <span class="meta">Obavezno</span>
        </div>
        <div class="grid-3">
          <app-field label="Ime">
            <app-input [value]="ime()" (valueChange)="ime.set($event)" placeholder="Marko"/>
          </app-field>
          <app-field label="Prezime">
            <app-input [value]="prezime()" (valueChange)="prezime.set($event)" placeholder="Marković"/>
          </app-field>
          <app-field label="JMBG" hint="13 cifara">
            <app-input
              [mono]="true"
              [value]="jmbg()"
              (valueChange)="jmbg.set($event)"
              placeholder="0205988710326"
              [maxlength]="13"
            />
          </app-field>
        </div>
      </div>

      <!-- Section 2 — Vozačka dozvola -->
      <div class="section">
        <div class="section-head">
          <h2><span class="num">2</span> Vozačka dozvola</h2>
          <app-toggle
            [on]="hasLicence()"
            label="Vozač poseduje vozačku dozvolu"
            (onChange)="hasLicence.set($event)"
          />
        </div>

        @if (hasLicence()) {
          <div class="grid-2">
            <app-field label="Broj dozvole" hint="9 cifara">
              <app-input
                [mono]="true"
                [value]="licNum()"
                (valueChange)="licNum.set($event)"
                placeholder="049837261"
                [maxlength]="9"
              />
            </app-field>
            <app-field label="Tip dozvole">
              <app-select
                [options]="LIC_TYPE_OPTIONS"
                [value]="licType()"
                (valueChange)="licType.set($any($event))"
              />
            </app-field>
            <app-field label="Važeće kategorije" class="span-2">
              <app-multi-select
                [options]="CAT_OPTIONS"
                [value]="cats()"
                (valueChange)="cats.set($event)"
                placeholder="izaberi kategorije..."
              />
            </app-field>
            <app-field label="Datum izdavanja">
              <app-input
                type="date"
                [value]="licIssued()"
                (valueChange)="licIssued.set($event)"
              />
            </app-field>
          </div>
        } @else {
          <div class="no-licence">
            <div class="ico"><lucide-angular [img]="alertIcon" [size]="22"/></div>
            <div>
              <h3>Vozač bez vozačke dozvole.</h3>
              <p>Sistem će dodati prekršaj čl. 195 — vožnja bez položenog vozačkog ispita.</p>
            </div>
          </div>
        }
      </div>

      <!-- Section 3 — Vozilo -->
      <div class="section">
        <div class="section-head">
          <h2><span class="num">3</span> Vozilo</h2>
          <span class="meta">Iz registra ili ručno</span>
        </div>
        <div class="grid-2">
          <app-field label="Tip vozila">
            <app-select
              [options]="VEHICLE_OPTIONS"
              [value]="vehicleType()"
              (valueChange)="vehicleType.set($event)"
            />
          </app-field>
          <app-field label="Registarska oznaka" hint="Format BG-1234-AŠ">
            <app-input
              [mono]="true"
              [value]="plate()"
              (valueChange)="plate.set($event)"
              placeholder="BG-1234-AŠ"
            />
          </app-field>
        </div>
      </div>

      <!-- Section 4 — Alkotest -->
      <div class="section">
        <div class="section-head">
          <h2><span class="num">4</span> Alkotest</h2>
          <span class="meta">Promili (‰)</span>
        </div>
        <div class="grid-2">
          <app-field label="Nivo alkohola u krvi" hint="Format 0,42 — koristi zarez">
            <app-input
              [value]="bac()"
              (valueChange)="bac.set($event)"
              placeholder="0,00"
            />
          </app-field>
          <app-bac-indicator [value]="bacNumeric()"/>
        </div>
      </div>

      <!-- Section 5 — Prekršaji -->
      <div class="section">
        <div class="section-head">
          <h2><span class="num">5</span> Prekršaji</h2>
          <span class="meta">{{ violations().length }} izabrano</span>
        </div>
        <app-field label="Vrste prekršaja">
          <app-multi-select
            [options]="VIOLATION_OPTIONS"
            [value]="violations()"
            (valueChange)="violations.set($event)"
            placeholder="izaberi prekršaje..."
          />
        </app-field>
      </div>

      <!-- Section 6 — Prekoračenje brzine -->
      <div class="section">
        <div class="section-head">
          <h2><span class="num">6</span> Prekoračenje brzine</h2>
          <span class="meta">{{ speeding() ? 'izabrano' : 'opciono' }}</span>
        </div>
        <app-field
          label="Opseg prekoračenja"
          hint="Izaberite tačan opseg po članu ZOBS-a; ako nema prekoračenja, ostavite prazno."
        >
          <app-select
            [options]="SPEEDING_OPTS"
            [value]="speeding()"
            (valueChange)="speeding.set($event)"
            placeholder="— nema prekoračenja brzine —"
          />
        </app-field>
        @if (vehicleType() === 'BUS' && speeding()) {
          <p class="hint-info">
            Za autobus se automatski primenjuje i posebna tarifa
            (ZOBS čl. 45 par. 1 tač. 4) — backend će je dodati uz izabrani opseg.
          </p>
        }
      </div>

      <!-- Section 7 — Okolnosti -->
      <div class="section">
        <div class="section-head">
          <h2><span class="num">7</span> Okolnosti</h2>
          <app-toggle
            [on]="causedAccident()"
            label="Vozač je izazvao saobraćajnu nezgodu"
            (onChange)="causedAccident.set($event)"
          />
        </div>
        @if (causedAccident()) {
          <p class="hint-info">
            Sve sankcije iz ove prijave biće eskalovane prema članovima 329–334
            Zakona o bezbednosti saobraćaja (kombinacija prekršaja + nezgoda).
          </p>
        } @else {
          <p class="hint-info muted">
            Bez nezgode: sankcije se izriču po osnovnom članu prekršaja,
            bez eskalacije.
          </p>
        }
      </div>

      <!-- Sticky action bar -->
      <div class="actionbar">
        <div class="summary">
          <b>{{ summary() }}</b>
        </div>
        <app-button
          [icon]="sendIcon"
          [disabled]="submitting() || !canSubmit()"
          (onClick)="submit()"
        >
          {{ submitting() ? 'Slanje…' : 'Pošalji prijavu' }}
        </app-button>
      </div>

      @if (error()) {
        <div class="error-row">{{ error() }}</div>
      }
    </div>
  `,
  styles: `
    .section { background: var(--surface-1); border: 1px solid var(--border-1); border-radius: var(--r-2); padding: 22px 24px; margin-bottom: 18px; }
    .section-head { display: flex; align-items: baseline; justify-content: space-between; margin-bottom: 18px; gap: 12px; }
    .section-head h2 { font-size: 16px; font-weight: 600; margin: 0; color: var(--brand-navy-ink); display: flex; align-items: center; gap: 10px; }
    .section-head .num { display: inline-flex; align-items: center; justify-content: center; width: 22px; height: 22px; border-radius: 50%; background: var(--brand-navy); color: white; font-family: var(--font-mono); font-size: 12px; font-weight: 600; }
    .section-head .meta { font-size: 12px; color: var(--ink-2); }

    .grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 16px 20px; }
    .grid-3 { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 16px 20px; }
    .span-2 { grid-column: span 2; }

    .no-licence {
      display: flex; gap: 12px;
      background: var(--warning-amber-soft);
      border: 1px solid oklch(88% 0.05 80);
      border-radius: var(--r-2);
      padding: 16px 18px;
      color: var(--warning-amber-ink);
    }
    .no-licence .ico { color: var(--warning-amber); flex: 0 0 24px; margin-top: 1px; }
    .no-licence h3 { margin: 0 0 4px; font-size: 15px; }
    .no-licence p  { margin: 0; font-size: 13px; color: var(--ink-0); }

    .actionbar {
      position: sticky; bottom: 0;
      background: var(--surface-1);
      border: 1px solid var(--border-1);
      border-radius: var(--r-2);
      padding: 12px 16px;
      display: flex; align-items: center; gap: 12px;
      box-shadow: var(--shadow-1);
      margin-top: 18px;
    }
    .actionbar .summary { font-size: 13px; color: var(--ink-1); flex: 1; }
    .actionbar .summary b { color: var(--ink-0); font-weight: 500; }

    .error-row {
      margin-top: 12px;
      padding: 10px 14px;
      background: var(--signal-red-soft);
      color: var(--signal-red-ink);
      border-radius: var(--r-2);
      font-size: 13px;
    }

    .hint-info {
      margin: 0;
      padding: 12px 14px;
      background: var(--surface-sunken);
      border: 1px solid var(--border-1);
      border-radius: var(--r-2);
      font-size: 13px;
      color: var(--warning-amber-ink);
    }
    .hint-info.muted {
      color: var(--ink-2);
    }
  `,
})
export class ViolationFormComponent {
  private readonly sankcije = inject(SankcijeService);
  private readonly state    = inject(SankcijeStateService);
  private readonly router   = inject(Router);

  // Lucide icons
  readonly alertIcon = AlertTriangle;
  readonly sendIcon  = Send;

  // Form fields (signal-based, no FormBuilder ceremony for this size)
  readonly ime         = signal('');
  readonly prezime     = signal('');
  readonly jmbg        = signal('');
  readonly hasLicence  = signal(true);
  readonly licNum      = signal('');
  readonly licType     = signal<LicenceType>('STALNA');
  readonly cats        = signal<string[]>([]);
  readonly licIssued   = signal('');
  readonly vehicleType    = signal('CAR');
  readonly plate          = signal('');
  readonly bac            = signal('');
  readonly violations     = signal<string[]>([]);
  readonly speeding       = signal<string>('');
  readonly causedAccident = signal(false);

  // Server interaction
  readonly submitting = signal(false);
  readonly error      = signal<string | null>(null);

  // Catalogues -> select options
  readonly LIC_TYPE_OPTIONS = [
    { value: 'STALNA', label: 'STALNA' },
    { value: 'PROBNA', label: 'PROBNA' },
  ];
  readonly CAT_OPTIONS       = CATEGORIES.map(c => ({ value: c, label: c }));
  readonly VEHICLE_OPTIONS   = VEHICLE_TYPES.map(v => ({ value: v.code, label: v.label }));
  readonly VIOLATION_OPTIONS = VIOLATIONS.map(v => ({ value: v.code, label: v.label }));
  /** Prvi red ("") je placeholder = nema prekoračenja brzine. */
  readonly SPEEDING_OPTS     = [
    { value: '', label: '— nema prekoračenja brzine —' },
    ...SPEEDING_OPTIONS.map(o => ({ value: o.code, label: o.label })),
  ];

  readonly bacNumeric = computed(() => parseSerbianDecimal(this.bac()));

  readonly summary = computed(() => {
    const n = this.violations().length
            + (!this.hasLicence() ? 1 : 0)
            + (this.speeding() ? 1 : 0);
    const fullName = [this.ime(), this.prezime()].filter(Boolean).join(' ') || '—';
    const plateOrDash = this.plate() || '—';
    return `${n} prekršaja · vozač ${fullName} · ${plateOrDash}`;
  });

  readonly canSubmit = computed(() => {
    if (!this.ime() || !this.prezime() || !this.jmbg()) return false;
    if (this.jmbg().length !== 13) return false;
    if (this.hasLicence() && !this.licNum()) return false;
    if (!this.vehicleType()) return false;
    // Nothing to sanction: no licence violation, no BAC, no picked violations,
    // no speeding tier selected.
    if (this.violations().length === 0
        && this.hasLicence()
        && this.bacNumeric() <= 0.20
        && !this.speeding()) {
      return false;
    }
    return true;
  });

  /** Cast helper for template typing of select event payloads. */
  $any(v: unknown) { return v as LicenceType; }

  submit() {
    if (!this.canSubmit() || this.submitting()) return;
    this.submitting.set(true);
    this.error.set(null);

    const payload: ViolationSubmission = {
      ime: this.ime().trim(),
      prezime: this.prezime().trim(),
      jmbg: this.jmbg().trim(),
      hasLicence: this.hasLicence(),
      licNum:    this.hasLicence() ? this.licNum().trim() : undefined,
      licType:   this.hasLicence() ? this.licType() : undefined,
      cats:      this.hasLicence() ? this.cats() : undefined,
      licIssued: this.hasLicence() ? (this.licIssued() || undefined) : undefined,
      vehicleType: this.vehicleType(),
      plate: this.plate().trim(),
      bac: this.bac(),
      causedAccident: this.causedAccident(),
      violations: this.violations(),
      speeding: this.speeding() || undefined,
    };

    this.sankcije.submitViolation(payload).subscribe({
      next: result => {
        this.state.store(payload, result);
        this.submitting.set(false);
        this.router.navigate(['/result']);
      },
      error: err => {
        this.submitting.set(false);
        this.error.set(extractError(err));
      },
    });
  }
}

function extractError(err: unknown): string {
  if (err && typeof err === 'object' && 'message' in err) {
    return `Greška pri slanju: ${(err as { message: string }).message}`;
  }
  return 'Greška pri slanju prijave.';
}
