import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./screens/audit-page.component').then((m) => m.AuditPageComponent)
  }
];
