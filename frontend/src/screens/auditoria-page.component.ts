import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'app-auditoria-page',
  standalone: true,
  imports: [ReactiveFormsModule],
  template: `
    <main class="container">
      <h1>AuditAI</h1>
      <p>Envie um registro de ponto para auditoria.</p>

      <form [formGroup]="form" (ngSubmit)="submit()">
        <textarea formControlName="conteudoPonto" rows="10" placeholder="Cole os registros de ponto aqui"></textarea>
        <button type="submit" [disabled]="form.invalid">Iniciar processamento</button>
      </form>
    </main>
  `,
  styles: [`
    .container {
      max-width: 960px;
      margin: 40px auto;
      padding: 24px;
    }
    h1 {
      margin-top: 0;
    }
    form {
      display: grid;
      gap: 12px;
    }
    textarea {
      resize: vertical;
      border-radius: 8px;
      border: 1px solid #2d3748;
      background: #111827;
      color: #e5e7eb;
      padding: 12px;
    }
    button {
      width: fit-content;
      border: 0;
      border-radius: 8px;
      padding: 10px 16px;
      cursor: pointer;
      background: #2563eb;
      color: #fff;
    }
    button:disabled {
      opacity: 0.4;
      cursor: not-allowed;
    }
  `]
})
export class AuditoriaPageComponent {
  form = this.formBuilder.group({
    conteudoPonto: ['', [Validators.required, Validators.maxLength(20000)]]
  });

  constructor(private readonly formBuilder: FormBuilder) {}

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    console.log('Payload inicial', this.form.value);
  }
}
