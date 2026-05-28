import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { filter, map, startWith } from 'rxjs';

const ROUTE_LABEL: Record<string, [string, string]> = {
  quick:     ['Sankcije', 'Brza provera'],
  violation: ['Sankcije', 'Prijava prekršaja'],
  result:    ['Sankcije', 'Rezultat obrade'],
  fault:     ['Krivica',  'Procena udesa'],
};

@Component({
  selector: 'app-topbar',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <header class="topbar">
      <div class="crumbs">
        <span>ESSP</span>
        <span class="sep">/</span>
        <span>{{ crumbs()[0] }}</span>
        <span class="sep">/</span>
        <b>{{ crumbs()[1] }}</b>
      </div>
      <div class="spacer"></div>
    </header>
  `,
})
export class TopbarComponent {
  private readonly router = inject(Router);

  /** Re-resolve labels every time the URL changes. */
  private readonly currentUrl = toSignal(
    this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd),
      map(e => e.urlAfterRedirects),
      startWith(this.router.url),
    ),
    { initialValue: this.router.url },
  );

  readonly crumbs = computed<[string, string]>(() => {
    const url = this.currentUrl();
    const segment = url.replace(/^\//, '').split(/[?#]/)[0];
    return ROUTE_LABEL[segment] ?? ['', ''];
  });
}
