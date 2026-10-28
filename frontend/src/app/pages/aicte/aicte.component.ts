import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApaarService, College } from '../../services/apaar.service';
import { LanguageService } from '../../services/language.service';

@Component({
  selector: 'app-aicte',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './aicte.component.html',
  styleUrl: './aicte.component.css'
})
export class AicteComponent implements OnInit {
  colleges: College[] = [];
  filteredColleges: College[] = [];
  searchCollege: string = '';
  filterState: string = 'all';
  filterCompliance: string = 'all';
  sortColumn: string = '';
  sortDir: 'asc' | 'desc' = 'asc';
  auditLogs: any[] = [];
  anomalies: Array<{ type: string; severity: string; description: string; action: string }> = [];
  exportToast: string = '';
  maxStateVal: number = 1;

  /** KPI values rendered live from the actual database */
  kpi = {
    institutions: 0,
    enrolments: 0,
    verified: 0,
    verifiedPct: '0.0',
    duplicates: 0,
    pending: 0,
    statesCovered: 0,
    underAudit: 0
  };

  /** Statutory quota distribution (live from registry) */
  categoryData: Array<{ label: string; key: string; count: number; color: string }> = [];

  /** State-wise chart data computed from the actual database */
  stateData: Array<{ state: string; total: number; verified: number; approved: number; pending: number }> = [];

  constructor(
    private apaarService: ApaarService,
    public ls: LanguageService,
    private cdr: ChangeDetectorRef
  ) {}

  t(key: string): string {
    return this.ls.t(key);
  }

  ngOnInit() {
    this.initColleges();
    this.loadLiveStats();
    this.loadAuditLogs();
  }

  private initColleges() {
    this.colleges = [];

    this.apaarService.getAllColleges().subscribe({
      next: (res) => {
        if (res && res.success && res.data) {
          this.colleges = res.data as College[];

          // Include institutions registered from this device (persisted offline)
          if (typeof localStorage !== 'undefined') {
            const saved = localStorage.getItem('aicte_registered_colleges');
            if (saved) {
              try {
                const registered = JSON.parse(saved);
                if (Array.isArray(registered)) {
                  registered.forEach((item: any) => {
                    if (item.college && !this.colleges.some(c => c.id === item.college.id)) {
                      this.colleges.unshift(item.college);
                    }
                  });
                }
              } catch (e) {}
            }
          }

          this.applyFilters();
          this.cdr.detectChanges();
        }
      },
      error: () => {
        this.filteredColleges = [];
        this.cdr.detectChanges();
      }
    });
  }

  /** National KPIs, state chart and anomaly stream — computed from the live database. */
  private loadLiveStats() {
    this.apaarService.getNationalStats().subscribe({
      next: (res) => {
        if (res && res.success && res.stats) {
          const s = res.stats;
          this.kpi = {
            institutions: s.institutions ?? 0,
            enrolments: s.totalEnrolments ?? 0,
            verified: s.verified ?? 0,
            verifiedPct: s.verifiedPercent ? Number(s.verifiedPercent).toFixed(1) : '0.0',
            duplicates: s.duplicates ?? 0,
            pending: s.pending ?? 0,
            statesCovered: s.statesCovered ?? 0,
            underAudit: s.collegesUnderAudit ?? 0
          };

          const cat = s.categoryBreakdown || {};
          this.categoryData = [
            { label: this.t('aic.catGeneral'), key: 'general', count: Number(cat.general ?? 0), color: '#1e3a8a' },
            { label: this.t('aic.catTfw'), key: 'tfw', count: Number(cat.tfw ?? 0), color: '#d97706' },
            { label: this.t('aic.catNri'), key: 'nri', count: Number(cat.nri ?? 0), color: '#7c3aed' },
            { label: this.t('aic.catFn'), key: 'fn', count: Number(cat.fn ?? 0), color: '#0f766e' }
          ];
          const maxCat = Math.max(1, ...this.categoryData.map(c => c.count));
          this.categoryData.forEach(c => (c.count = c.count));

          this.stateData = (s.stateBreakdown || []).map((x: any) => ({
            state: x.state,
            total: x.total,
            verified: x.verified,
            approved: x.approved,
            pending: x.pending
          }));
          this.maxStateVal = Math.max(1, ...this.stateData.map(d => Math.max(d.total, d.approved)));
          this.anomalies = s.anomalies || [];
          this.cdr.detectChanges();
        }
      },
      error: () => {
        // Offline fallback: compute from college registry
        this.apaarService.getAllColleges().subscribe({
          next: (res) => {
            if (res && res.success && res.data) {
              this.computeStatsLocally(res.data);
              this.cdr.detectChanges();
            }
          },
          error: () => {}
        });
      }
    });
  }

  private computeStatsLocally(colleges: any[]) {
    const sum = (fn: (c: any) => number) => colleges.reduce((a, c) => a + (fn(c) || 0), 0);
    const enrolments = sum(c => c.actualEnrolment);
    const verified = sum(c => c.verifiedCount);
    this.kpi = {
      institutions: colleges.length,
      enrolments,
      verified,
      verifiedPct: enrolments ? ((verified / enrolments) * 100).toFixed(1) : '0.0',
      duplicates: sum(c => c.duplicateCount),
      pending: sum(c => c.pendingCount),
      statesCovered: new Set(colleges.map(c => c.state)).size,
      underAudit: colleges.filter(c => c.actualEnrolment > c.approvedIntake).length
    };
    const stateMap = new Map<string, { total: number; verified: number; approved: number; pending: number }>();
    colleges.forEach(c => {
      const agg = stateMap.get(c.state) || { total: 0, verified: 0, approved: 0, pending: 0 };
      agg.total += c.actualEnrolment || 0;
      agg.verified += c.verifiedCount || 0;
      agg.approved += c.approvedIntake || 0;
      agg.pending += c.pendingCount || 0;
      stateMap.set(c.state, agg);
    });
    this.stateData = Array.from(stateMap.entries()).map(([state, v]) => ({ state, ...v }));
    this.maxStateVal = Math.max(1, ...this.stateData.map(d => Math.max(d.total, d.approved)));
    this.categoryData = [];
  }

  private loadAuditLogs() {
    this.apaarService.getAuditLogs().subscribe({
      next: (res) => {
        if (res && res.success && res.data && res.data.length > 0) {
          this.auditLogs = res.data;
          this.cdr.detectChanges();
        }
      },
      error: () => {
        this.auditLogs = [];
      }
    });
  }

  /** College search: displays strictly according to the typed college name/code/state. */
  filterColleges() {
    this.applyFilters();
  }

  private applyFilters() {
    const q = (this.searchCollege || '').trim().toLowerCase().replace(/\s+/g, ' ');
    const words = q.split(' ').filter(w => w);
    this.filteredColleges = this.colleges.filter(c => {
      const stateOk = this.filterState === 'all' || c.state === this.filterState;
      const compliant = c.actualEnrolment <= c.approvedIntake;
      const complianceOk =
        this.filterCompliance === 'all' ||
        (this.filterCompliance === 'approved' && compliant) ||
        (this.filterCompliance === 'audit' && !compliant);
      if (!stateOk || !complianceOk) return false;
      const haystack = [c.name, c.code, c.state, c.city].map(f => (f || '').toLowerCase()).join(' ');
      return !words.length || words.every(w => haystack.includes(w));
    });

    if (this.sortColumn) this.sortData(this.sortColumn, true);
  }

  get states(): string[] {
    return Array.from(new Set(this.colleges.map(c => c.state).filter(Boolean))).sort();
  }

  sortBy(column: string) {
    if (this.sortColumn === column) {
      this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column;
      this.sortDir = 'asc';
    }
    this.sortData(column, false);
  }

  private sortData(column: string, silent: boolean) {
    const dir = this.sortDir === 'asc' ? 1 : -1;
    this.filteredColleges.sort((a, b) => {
      const va = (a as any)[column];
      const vb = (b as any)[column];
      if (typeof va === 'number' && typeof vb === 'number') return (va - vb) * dir;
      return String(va || '').localeCompare(String(vb || '')) * dir;
    });
    if (!silent) this.cdr.detectChanges();
  }

  downloadExcel() {
    this.apaarService.downloadExcelReport().subscribe({
      next: (blob) => {
        this.saveBlob(blob, 'AICTE_National_Compliance_Report_2026.xlsx');
        this.showToast('✅ ' + this.t('aic.toastExcel'));
      },
      error: () => {
        // Fallback client-side CSV compliance export
        const csvContent = this.generateComplianceCsv();
        const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' });
        this.saveBlob(blob, 'AICTE_National_Compliance_Report_2026.csv');
        this.showToast('✅ ' + this.t('aic.toastCsv'));
      }
    });
  }

  /** Official formatted PDF generated by the platform (falls back to print view). */
  downloadPdf() {
    this.apaarService.downloadNationalPdf().subscribe({
      next: (blob) => {
        this.saveBlob(blob, 'AICTE_National_Compliance_Report_2026.pdf');
        this.showToast('✅ ' + this.t('aic.toastPdf'));
      },
      error: () => {
        this.showToast('ℹ️ ' + this.t('aic.toastPdfFallback'));
        setTimeout(() => window.print(), 600);
      }
    });
  }

  private saveBlob(blob: Blob, filename: string) {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  }

  private generateComplianceCsv(): string {
    const headers = [
      'College Code', 'College Name', 'City', 'State',
      'Approved Intake', 'Actual Enrolment', 'Intake Variance',
      'Supernumerary (TFW)', 'NRI Count', 'FN Count',
      'Verified Count', 'Verification %', 'Regulatory Status'
    ];

    const rows = this.colleges.map(c => {
      const variance = c.actualEnrolment - c.approvedIntake;
      const pct = ((c.verifiedCount / (c.actualEnrolment || 1)) * 100).toFixed(1) + '%';
      const status = c.actualEnrolment > c.approvedIntake ? 'Under Audit' : 'Approved';
      return [
        `"${c.code}"`, `"${c.name}"`, `"${c.city}"`, `"${c.state}"`,
        c.approvedIntake, c.actualEnrolment,
        variance > 0 ? `+${variance}` : variance,
        c.supernumeraryQuota, c.nriCount, c.fnCount, c.verifiedCount,
        `"${pct}"`, `"${status}"`
      ].join(',');
    });

    return [headers.join(','), ...rows].join('\r\n');
  }

  private showToast(msg: string) {
    this.exportToast = msg;
    this.cdr.detectChanges();
    setTimeout(() => {
      this.exportToast = '';
      this.cdr.detectChanges();
    }, 4000);
  }

  printReport() {
    window.print();
  }
}
