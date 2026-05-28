import { Routes } from '@angular/router';

import { ShellComponent } from './shell/shell.component';
import { QuickCheckComponent } from './features/sankcije/quick-check/quick-check.component';
import { ViolationFormComponent } from './features/sankcije/violation-form/violation-form.component';
import { ResultComponent } from './features/sankcije/result/result.component';
import { FaultComponent } from './features/krivica/fault.component';

export const routes: Routes = [
  {
    path: '',
    component: ShellComponent,
    children: [
      { path: '',          pathMatch: 'full', redirectTo: 'quick' },
      { path: 'quick',     component: QuickCheckComponent },
      { path: 'violation', component: ViolationFormComponent },
      { path: 'result',    component: ResultComponent },
      { path: 'fault',     component: FaultComponent },
      { path: '**',        redirectTo: 'quick' },
    ],
  },
];
