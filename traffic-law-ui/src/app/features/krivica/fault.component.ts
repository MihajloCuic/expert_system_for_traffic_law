import { ChangeDetectionStrategy, Component } from '@angular/core';

/**
 * Modul 2 placeholder — sidebar link mora da vodi negde, ali sama logika
 * Procene udesa nije implementirana u ovoj iteraciji. Modul 1 je primarni
 * fokus, Modul 2 dolazi kasnije (vidi CLAUDE.md §2.5).
 */
@Component({
  selector: 'app-fault',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="page stub-page">
      <h2>Procena udesa</h2>
      <p>Implementacija u sledećoj iteraciji.</p>
    </div>
  `,
})
export class FaultComponent {}
