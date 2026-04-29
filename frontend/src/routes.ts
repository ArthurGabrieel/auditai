import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./screens/auditoria-page.component').then((m) => m.AuditoriaPageComponent)
  }
];
