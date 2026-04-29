import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { routes } from './routes';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `<router-outlet></router-outlet>`
})
class AppComponent {}

bootstrapApplication(AppComponent, {
  providers: [provideRouter(routes), provideHttpClient()]
}).catch((err) => console.error(err));
