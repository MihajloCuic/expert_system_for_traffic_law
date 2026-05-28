import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { LucideAngularModule, Search, FileText, GitFork, LucideIconData } from 'lucide-angular';

type NavItem = { route: string; icon: LucideIconData; label: string };

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, LucideAngularModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <aside class="sidebar">
      <div class="sidebar-brand">
        <svg width="26" height="26" viewBox="0 0 120 120" fill="none"
             stroke="currentColor" stroke-width="6" style="color: white">
          <circle cx="60" cy="60" r="50"/>
          <path d="M 30 90 L 55 30" stroke-linecap="round"/>
          <path d="M 90 90 L 65 30" stroke-linecap="round"/>
          <line x1="60" y1="46" x2="60" y2="54" stroke-linecap="round"/>
          <line x1="60" y1="62" x2="60" y2="74" stroke-linecap="round"/>
        </svg>
        <div>
          <b>ESSP</b>
          <div class="sub">Ekspertni sistem · v1.0</div>
        </div>
      </div>

      <div class="sidebar-group">
        <div class="sidebar-group-title">Modul 1 · Sankcije</div>
        @for (item of sankcijeItems; track item.route) {
          <a class="sidebar-item"
             [routerLink]="['/', item.route]"
             routerLinkActive="active">
            <lucide-angular [img]="item.icon" [size]="16"/>
            <span>{{ item.label }}</span>
          </a>
        }
      </div>

      <div class="sidebar-group">
        <div class="sidebar-group-title">Modul 2 · Krivica</div>
        @for (item of krivicaItems; track item.route) {
          <a class="sidebar-item"
             [routerLink]="['/', item.route]"
             routerLinkActive="active">
            <lucide-angular [img]="item.icon" [size]="16"/>
            <span>{{ item.label }}</span>
          </a>
        }
      </div>
    </aside>
  `,
})
export class SidebarComponent {
  readonly sankcijeItems: NavItem[] = [
    { route: 'quick',     icon: Search,   label: 'Brza provera' },
    { route: 'violation', icon: FileText, label: 'Prijava prekršaja' },
  ];

  readonly krivicaItems: NavItem[] = [
    { route: 'fault', icon: GitFork, label: 'Procena udesa' },
  ];
}
