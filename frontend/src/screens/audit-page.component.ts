import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-audit-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <main class="container">
      <h1>AuditAI</h1>
      <p>Submit a time log for auditing.</p>

      <form [formGroup]="form" (ngSubmit)="submit()">
        <textarea formControlName="timeLogContent" rows="10" placeholder="Paste time logs here"></textarea>
        <button type="submit" [disabled]="form.invalid || isSubmitting">Start processing</button>
      </form>

      <p *ngIf="lastCreatedId">Audit created: {{ lastCreatedId }}</p>
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
export class AuditPageComponent {
  isSubmitting = false;
  lastCreatedId: string | null = null;

  form = this.formBuilder.group({
    timeLogContent: ['', [Validators.required, Validators.maxLength(20000)]]
  });

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly http: HttpClient
  ) {}

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.isSubmitting = true;
    this.http.post<{ id: string }>('http://localhost:8080/api/audits', this.form.getRawValue()).subscribe({
      next: (response) => {
        this.lastCreatedId = response.id;
        this.form.reset();
      },
      error: (error) => {
        console.error('Failed to create audit', error);
      },
      complete: () => {
        this.isSubmitting = false;
      }
    });
  }
}
