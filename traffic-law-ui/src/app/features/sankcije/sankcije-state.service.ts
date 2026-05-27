import { Injectable, signal } from '@angular/core';
import { ViolationResult, ViolationSubmission } from '../../shared/types';

/**
 * Carries the latest form submission + backend response across the
 * /violation → /result navigation. The data is intentionally not
 * persisted — refreshing the result page sends the user back to a
 * fresh /violation form, matching the no-drafts rule from CLAUDE.md.
 */
@Injectable({ providedIn: 'root' })
export class SankcijeStateService {
  readonly lastSubmission = signal<ViolationSubmission | null>(null);
  readonly lastResult     = signal<ViolationResult | null>(null);

  store(submission: ViolationSubmission, result: ViolationResult) {
    this.lastSubmission.set(submission);
    this.lastResult.set(result);
  }

  clear() {
    this.lastSubmission.set(null);
    this.lastResult.set(null);
  }
}
