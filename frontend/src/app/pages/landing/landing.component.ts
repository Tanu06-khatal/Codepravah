import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ApaarService } from '../../services/apaar.service';
import { LanguageService } from '../../services/language.service';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="landing-container">
      <!-- AICTE Header Banner -->
      <section class="hero-section">
        <div class="hero-inner">
          <div class="pill-badge">
            <span class="live-dot"></span>
            <span class="reg-badge">{{ t('land.badge') }}</span>
            <span>{{ t('land.badgeSub') }}</span>
          </div>

          <h1 class="hero-title">{{ t('land.heroTitle') }}</h1>
          <h2 class="hero-subtitle">{{ t('land.heroSub') }}</h2>
          <p class="hero-desc" [innerHTML]="t('land.heroDesc')"></p>

          <!-- 3 Portal Cards -->
          <div class="portals-grid">
            <div class="portal-card" (click)="navigate('/student')">
              <div class="portal-icon">🎓</div>
              <h3>{{ t('land.pStudent') }}</h3>
              <p>{{ t('land.pStudentDesc') }}</p>
              <div class="card-footer">{{ t('land.pStudentCta') }}</div>
            </div>

            <div class="portal-card amber-border" (click)="navigate('/college')">
              <div class="portal-icon amber-icon">🏛️</div>
              <h3>{{ t('land.pCollege') }}</h3>
              <p>{{ t('land.pCollegeDesc') }}</p>
              <div class="card-footer text-amber">{{ t('land.pCollegeCta') }}</div>
            </div>

            <div class="portal-card green-border" (click)="navigate('/aicte')">
              <div class="portal-icon green-icon">🛡️</div>
              <h3>{{ t('land.pAicte') }}</h3>
              <p>{{ t('land.pAicteDesc') }}</p>
              <div class="card-footer text-green">{{ t('land.pAicteCta') }}</div>
            </div>
          </div>

          <!-- Quick Search Box -->
          <div class="search-box-card gov-card">
            <form (ngSubmit)="searchApaar()" class="search-form">
              <input
                type="text"
                [(ngModel)]="searchId"
                name="searchId"
                [placeholder]="t('land.searchLabel')"
                class="search-input"
              />
              <button type="submit" class="gov-btn-saffron">{{ t('land.verifyNow') }}</button>
            </form>
          </div>
        </div>
      </section>

      <!-- Institutional Notice Board (Inspired by Screenshot) -->
      <section class="notice-section">
        <div class="notice-inner">
          <div class="notice-col-left gov-card">
            <div class="notice-header">
              <h3>{{ t('land.noticeTitle') }}</h3>
              <span class="clause-tag">{{ t('land.clause') }}</span>
            </div>
            <div class="notice-items">
              <div class="notice-item">
                <div>
                  <h4>{{ t('land.n1') }}</h4>
                  <p>{{ t('land.n1d') }}</p>
                </div>
                <button class="gov-btn-saffron small-btn" (click)="navigate('/college')">{{ t('land.uploadGateway') }}</button>
              </div>
              <div class="notice-item">
                <div>
                  <h4>{{ t('land.n2') }}</h4>
                  <p>{{ t('land.n2d') }}</p>
                </div>
                <button class="gov-btn-saffron small-btn" (click)="navigate('/college')">{{ t('land.intakeAudit') }}</button>
              </div>
              <div class="notice-item">
                <div>
                  <h4>{{ t('land.n3') }}</h4>
                  <p>{{ t('land.n3d') }}</p>
                </div>
                <button class="gov-btn-saffron small-btn" (click)="navigate('/aicte')">{{ t('land.inspectQuotas') }}</button>
              </div>
            </div>
          </div>

          <div class="notice-col-right gov-card">
            <div class="notice-header">
              <h3>{{ t('land.supportTitle') }}</h3>
            </div>
            <div class="support-card bg-amber-light">
              <h4>{{ t('land.helpline') }}</h4>
              <p class="phone-num">📞 (011) 29581333, 29581338, 29581342</p>
              <p class="sub-text">{{ t('land.helplineTime') }}</p>
            </div>
            <div class="support-card bg-blue-light">
              <h4>{{ t('land.videoSupport') }}</h4>
              <p class="sub-text">{{ t('land.videoSupportDesc') }}</p>
              <button class="gov-btn-primary full-btn" (click)="alertSupport()">{{ t('land.openSession') }}</button>
            </div>
          </div>
        </div>
      </section>

      <!-- National Statistics (Live from Database) -->
      <section class="stats-section">
        <div class="stats-inner">
          <div class="stat-card">
            <h2>{{ stats.institutions | number }}</h2>
            <p>{{ t('land.stInstitutions') }}</p>
            <span class="stat-badge green">{{ t('land.stOnboarded') }}</span>
          </div>
          <div class="stat-card">
            <h2>{{ stats.enrolments | number }}</h2>
            <p>{{ t('land.stStudents') }}</p>
            <span class="stat-badge blue">{{ t('land.stAy') }}</span>
          </div>
          <div class="stat-card">
            <h2>{{ stats.verifiedPct }}%</h2>
            <p>{{ t('land.stVerified') }}</p>
            <span class="stat-badge green">{{ t('land.stSynced') }}</span>
          </div>
          <div class="stat-card">
            <h2>{{ stats.duplicates | number }}</h2>
            <p>{{ t('land.stDuplicates') }}</p>
            <span class="stat-badge red">{{ t('land.stBlocked') }}</span>
          </div>
        </div>
      </section>
    </div>
  `,
  styles: [`
    .landing-container { min-height: 100vh; }
    .hero-section { background: linear-gradient(to bottom, #E9F3FC, #F8FAFC); padding: 50px 20px; text-align: center; }
    .hero-inner { max-width: 1100px; margin: 0 auto; }
    .pill-badge { display: inline-flex; align-items: center; gap: 8px; background: #fff; padding: 6px 16px; border-radius: 50px; font-size: 12px; font-weight: 600; color: #0F2C61; border: 1px solid #bfdbfe; margin-bottom: 20px; }
    .live-dot { width: 8px; height: 8px; background: #10B981; border-radius: 50%; }
    .reg-badge { background: #fef3c7; color: #92400e; padding: 2px 8px; border-radius: 4px; font-weight: 800; font-size: 10px; }
    .hero-title { font-size: 42px; font-weight: 900; color: #0F2C61; margin: 0; }
    .hero-subtitle { font-size: 26px; font-weight: 800; color: #D97706; margin: 10px 0 20px; }
    .hero-desc { font-size: 15px; color: #475569; max-width: 800px; margin: 0 auto 35px; line-height: 1.6; }
    .portals-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 20px; margin-bottom: 35px; }
    .portal-card { background: #fff; border: 2px solid #bfdbfe; border-radius: 14px; padding: 25px; text-align: left; cursor: pointer; transition: transform 0.2s, box-shadow 0.2s; box-shadow: 0 4px 12px rgba(15,44,97,0.06); }
    .portal-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(15,44,97,0.12); }
    .amber-border { border-color: #fde68a; }
    .green-border { border-color: #a7f3d0; }
    .portal-icon { font-size: 28px; margin-bottom: 12px; }
    .portal-card h3 { font-size: 18px; font-weight: 700; color: #0F2C61; margin: 0 0 8px; }
    .portal-card p { font-size: 12px; color: #64748b; margin: 0 0 16px; line-height: 1.5; }
    .card-footer { font-size: 12px; font-weight: 700; color: #0F2C61; border-top: 1px solid #f1f5f9; padding-top: 12px; }
    .text-amber { color: #d97706; }
    .text-green { color: #059669; }
    .search-box-card { max-width: 700px; margin: 0 auto; padding: 20px; }
    .search-form { display: flex; gap: 10px; }
    .search-input { flex: 1; padding: 12px 16px; border: 1px solid #cbd5e1; border-radius: 8px; font-family: monospace; font-size: 14px; }
    .notice-section { padding: 40px 20px; max-width: 1100px; margin: 0 auto; }
    .notice-inner { display: grid; grid-template-columns: 2fr 1fr; gap: 20px; }
    @media(max-width: 768px) { .notice-inner { grid-template-columns: 1fr; } }
    .notice-col-left, .notice-col-right { padding: 25px; }
    .notice-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #e2e8f0; padding-bottom: 12px; margin-bottom: 20px; }
    .notice-header h3 { font-size: 16px; font-weight: 700; color: #0F2C61; margin: 0; }
    .clause-tag { background: #eff6ff; color: #1e3a8a; padding: 4px 10px; border-radius: 4px; font-size: 11px; font-weight: 700; }
    .notice-item { display: flex; justify-content: space-between; align-items: center; padding: 14px; border: 1px solid #e2e8f0; border-radius: 8px; margin-bottom: 12px; gap: 10px; }
    .notice-item h4 { font-size: 13px; font-weight: 700; color: #0F2C61; margin: 0 0 4px; }
    .notice-item p { font-size: 11px; color: #64748b; margin: 0; }
    .small-btn { padding: 6px 12px; font-size: 11px; }
    .support-card { padding: 15px; border-radius: 8px; margin-bottom: 15px; }
    .bg-amber-light { background: #fffbeb; border: 1px solid #fef3c7; }
    .bg-blue-light { background: #eff6ff; border: 1px solid #dbeafe; }
    .phone-num { font-family: monospace; font-weight: 700; font-size: 12px; margin: 5px 0; }
    .sub-text { font-size: 11px; color: #64748b; margin: 0 0 10px; }
    .full-btn { width: 100%; font-size: 12px; }
    .stats-section { background: #fff; border-top: 1px solid #e2e8f0; border-bottom: 1px solid #e2e8f0; padding: 40px 20px; }
    .stats-inner { max-width: 1100px; margin: 0 auto; display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; text-align: center; }
    .stat-card { padding: 20px; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; }
    .stat-card h2 { font-size: 32px; font-weight: 900; color: #0F2C61; margin: 0 0 5px; }
    .stat-card p { font-size: 12px; font-weight: 600; color: #64748b; margin: 0 0 10px; }
    .stat-badge { font-size: 10px; font-weight: 700; padding: 3px 8px; border-radius: 4px; }
    .green { background: #d1fae5; color: #065f46; }
    .blue { background: #dbeafe; color: #1e40af; }
    .red { background: #fee2e2; color: #991b1b; }
  `]
})
export class LandingComponent implements OnInit {
  searchId: string = '';

  /** National statistics rendered from the actual database (with offline fallback). */
  stats = {
    institutions: 0,
    enrolments: 0,
    verified: 0,
    verifiedPct: '0.0',
    duplicates: 0
  };

  constructor(
    private router: Router,
    private apaarService: ApaarService,
    public ls: LanguageService,
    private cdr: ChangeDetectorRef
  ) {}

  t(key: string): string {
    return this.ls.t(key);
  }

  ngOnInit() {
    this.loadNationalStats();
  }

  /** Fetch live stats computed by the backend from the real database. */
  loadNationalStats() {
    this.apaarService.getNationalStats().subscribe({
      next: (res) => {
        if (res && res.success && res.stats) {
          const s = res.stats;
          this.stats = {
            institutions: s.institutions ?? 0,
            enrolments: s.totalEnrolments ?? 0,
            verified: s.verified ?? 0,
            verifiedPct: s.verifiedPercent ? Number(s.verifiedPercent).toFixed(1) : '0.0',
            duplicates: s.duplicates ?? 0
          };
          this.cdr.detectChanges();
        }
      },
      error: () => {
        // Offline fallback: compute from locally known college registry
        this.apaarService.getAllColleges().subscribe({
          next: (res) => {
            if (res && res.success && res.data) {
              this.computeFromColleges(res.data);
              this.cdr.detectChanges();
            }
          },
          error: () => {}
        });
      }
    });
  }

  private computeFromColleges(colleges: any[]) {
    const sum = (fn: (c: any) => number) => colleges.reduce((a, c) => a + (fn(c) || 0), 0);
    const enrolments = sum(c => c.actualEnrolment);
    const verified = sum(c => c.verifiedCount);
    this.stats = {
      institutions: colleges.length,
      enrolments,
      verified,
      verifiedPct: enrolments ? ((verified / enrolments) * 100).toFixed(1) : '0.0',
      duplicates: sum(c => c.duplicateCount)
    };
  }

  navigate(path: string) {
    this.router.navigateByUrl(path);
  }

  searchApaar() {
    if (this.searchId.trim()) {
      this.router.navigate(['/student'], { queryParams: { id: this.searchId.trim() } });
    }
  }

  alertSupport() {
    alert(this.ls.lang === 'hi'
      ? 'AICTE थेट व्हिडिओ समर्थन सुविधा डेस्कशी जोडले जात आहे...'
      : 'Connecting to AICTE Live Video Support Facilitation Desk...');
  }
}
