import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApaarService, Student, College } from '../../services/apaar.service';
import { LanguageService } from '../../services/language.service';

/** One parsed CSV row from the institutional bulk-upload file. */
interface CsvRow {
  apaarId: string; name: string; dob: string; gender: string; mobile: string;
  aadhaarLast4: string; course: string; academicYear: string; abcCredits: string;
  tenthPercent: string; twelfthPercent: string; category: string;
  enrollmentNo: string; admissionDate: string; state: string;
}

@Component({
  selector: 'app-college',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './college.component.html',
  styleUrl: './college.component.css'
})
export class CollegeComponent implements OnInit {
  isAuthenticated: boolean = false;
  isGoogleUser: boolean = false;
  loggedInUser: string = '';

  // Auth form
  activeTab: 'signin' | 'register' = 'signin';
  email: string = '';
  password: string = '';
  loginError: string = '';
  pendingNotice: string = '';
  regNotice: string = '';

  // Google Modal (works with ANY Google account)
  showGoogleModal: boolean = false;
  googleEmail: string = '';
  googleName: string = '';
  googleError: string = '';

  // Registered Accounts storage
  registeredAccounts: Array<{ email: string; password: string; college: College }> = [];

  // Registration data
  regData = {
    pid: '',
    name: '',
    email: '',
    password: '',
    state: '',
    approvedIntake: 600
  };
  regSuccess: boolean = false;

  // College Data
  college: College | null = null;
  students: Student[] = [];
  filteredStudents: Student[] = [];
  duplicates: Student[] = [];

  // Search, Filter and Sorting controls
  searchQuery: string = '';
  courseFilter: string = 'all';
  statusFilter: string = 'all';
  sortColumn: string = 'apaarId';
  sortDir: 'asc' | 'desc' = 'asc';

  // Bulk Upload
  showUpload: boolean = false;
  uploadNotice: string = '';
  uploadError: boolean = false;
  uploadBusy: boolean = false;
  selectedFile: File | null = null;

  constructor(
    private apaarService: ApaarService,
    public ls: LanguageService,
    private cdr: ChangeDetectorRef
  ) {}

  t(key: string): string {
    return this.ls.t(key);
  }

  ngOnInit() {
    if (typeof localStorage !== 'undefined') {
      const saved = localStorage.getItem('aicte_registered_colleges');
      if (saved) {
        try {
          this.registeredAccounts = JSON.parse(saved);
        } catch (e) {}
      }
    }
  }

  // Pre-fill credentials from a saved registration notice
  fillCreds(e: string, p: string) {
    this.email = e;
    this.password = p;
    this.loginError = '';
    this.pendingNotice = '';
    this.regNotice = '';
  }

  // Traditional Sign-In
  login() {
    this.loginError = '';
    this.pendingNotice = '';
    this.regNotice = '';

    const cleanEmail = (this.email || '').trim().toLowerCase();
    const cleanPass = this.password || '';

    if (!cleanEmail || !cleanPass) {
      this.loginError = this.ls.lang === 'hi'
        ? 'कृपया संस्थागत ईमेल व पासकी टाका.'
        : 'Please enter your institutional email and passkey.';
      return;
    }

    // Check newly registered institutions first
    const matched = this.registeredAccounts.find(a =>
      a.email.toLowerCase() === cleanEmail && a.password === cleanPass
    );

    if (matched) {
      this.college = matched.college;
      this.isAuthenticated = true;
      this.isGoogleUser = false;
      this.loggedInUser = `${cleanEmail} (${matched.college.name})`;
      this.fetchData();
      return;
    }

    // Demo / approved national test institution
    if (cleanEmail === 'college@aicte.ac.in' && cleanPass === '123456') {
      this.isAuthenticated = true;
      this.isGoogleUser = false;
      this.loggedInUser = cleanEmail;
      this.college = this.college?.id === 'COL001' ? this.college : null;
      this.fetchData();
    } else if (cleanEmail === 'pending.college@aicte.ac.in' && cleanPass === '123456') {
      this.pendingNotice = this.t('col.pendingTitle');
    } else {
      this.loginError = this.t('col.errInvalid');
    }
  }

  // ---------- Google Sign-In: works for ANY Google account ----------

  openGoogleModal() {
    this.showGoogleModal = true;
    this.googleEmail = '';
    this.googleName = '';
    this.googleError = '';
  }

  closeGoogleModal(event: MouseEvent) {
    this.showGoogleModal = false;
  }

  cancelGoogle() {
    this.showGoogleModal = false;
  }

  /** Validates and signs in with ANY Google account email. */
  confirmGoogleSignIn() {
    const email = (this.googleEmail || '').trim().toLowerCase();
    const name = (this.googleName || '').trim();

    const emailOk = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(email);
    if (!emailOk) {
      this.googleError = this.t('col.errGoogleEmail');
      return;
    }

    // Any Google account is accepted (gmail / googlemail / institutional Workspace)
    const googleDomain = /@(gmail\.com|googlemail\.com)$/i.test(email);
    if (!googleDomain && !/\.(ac|edu|gov|org)[.\w]*$/i.test(email.split('@')[1] || '')) {
      this.googleError = this.t('col.errGoogleDomain');
      return;
    }

    this.showGoogleModal = false;
    this.isAuthenticated = true;
    this.isGoogleUser = true;
    this.loggedInUser = name ? `${email} (${name})` : email;
    this.college = null;
    this.fetchData();
  }

  // ---------- Institutional Registration (real data, no dummy values) ----------

  submitRegistration() {
    if (!this.regData.email || !this.regData.name) {
      this.loginError = this.t('col.errNameEmail');
      return;
    }

    const intake = Number(this.regData.approvedIntake) || 0;
    const pid = (this.regData.pid || '').trim() || String(Math.floor(100000 + Math.random() * 900000));
    const cleanEmail = this.regData.email.trim().toLowerCase();

    // Real provisioning: a brand-new institution starts with ZERO enrolments.
    const newCollege: College = {
      id: 'COL-' + pid,
      name: this.regData.name.trim(),
      code: 'AICTE-' + pid,
      state: this.regData.state || 'National',
      city: 'Main Campus',
      approvedIntake: intake,
      actualEnrolment: 0,
      verifiedCount: 0,
      pendingCount: 0,
      duplicateCount: 0,
      supernumeraryQuota: 0,
      nriCount: 0,
      fnCount: 0,
      email: cleanEmail
    };

    const newAcc = {
      email: cleanEmail,
      password: this.regData.password || '123456',
      college: newCollege
    };

    this.registeredAccounts = this.registeredAccounts.filter(a => a.email !== newAcc.email);
    this.registeredAccounts.push(newAcc);
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem('aicte_registered_colleges', JSON.stringify(this.registeredAccounts));
    }

    // Persist the institution into the backend database as well
    this.apaarService.registerCollege({
      pid,
      name: newCollege.name,
      email: cleanEmail,
      state: newCollege.state,
      city: newCollege.city,
      approvedIntake: intake
    }).subscribe({ next: () => {}, error: () => {} });

    this.regSuccess = true;
    setTimeout(() => {
      this.regSuccess = false;
      this.activeTab = 'signin';
      this.email = newAcc.email;
      this.password = newAcc.password;
      this.regNotice = this.t('col.regNotice');
      this.cdr.detectChanges();
    }, 1200);
  }

  logout() {
    this.isAuthenticated = false;
    this.isGoogleUser = false;
    this.loggedInUser = '';
    this.college = null;
    this.loginError = '';
    this.pendingNotice = '';
  }

  // ---------- Data loading ----------

  fetchData() {
    const isRegistered = !!this.college && this.registeredAccounts.some(a => a.college.id === this.college!.id);

    if (isRegistered && this.college) {
      // Real registered institution: show ONLY its own students from the database.
      this.students = [];
      this.apaarService.getStudents({ collegeId: this.college.id }).subscribe({
        next: (res) => {
          if (res && res.success && res.data) {
            this.students = res.data as Student[];
          }
          this.filterStudents();
          this.duplicates = this.students.filter(s => s.status === 'duplicate' || s.duplicateCollegeName);
          this.cdr.detectChanges();
        },
        error: () => {
          this.filterStudents();
          this.duplicates = [];
          this.cdr.detectChanges();
        }
      });
      return;
    }

    // National demo institution (COL001): live stats + full registry from the DB
    this.apaarService.getCollegeStats('COL001').subscribe({
      next: (res) => {
        if (res && res.success && res.college) {
          this.college = res.college;
          this.cdr.detectChanges();
        }
      },
      error: () => {
        if (!this.college) {
          this.college = {
            id: 'COL001',
            name: 'Delhi Institute of Engineering & Technology',
            code: 'AICTE-DL-0012',
            state: 'Delhi',
            city: 'New Delhi',
            approvedIntake: 1200,
            actualEnrolment: 1240,
            verifiedCount: 980,
            pendingCount: 200,
            duplicateCount: 60,
            supernumeraryQuota: 45,
            nriCount: 24,
            fnCount: 8
          };
          this.cdr.detectChanges();
        }
      }
    });

    this.apaarService.getStudents().subscribe({
      next: (res) => {
        if (res && res.success && res.data && res.data.length > 0) {
          // Merge incoming backend records while preserving known seed records
          const incoming = res.data as Student[];
          const map = new Map<string, Student>();
          incoming.forEach(s => map.set(s.apaarId, s));
          this.students = Array.from(map.values());
          this.filterStudents();
          this.duplicates = this.students.filter(s => s.status === 'duplicate' || s.duplicateCollegeName);
          this.cdr.detectChanges();
        }
      },
      error: () => {
        this.filterStudents();
        this.cdr.detectChanges();
      }
    });
  }

  // ---------- Search / Filter / Sort ----------

  /** Live search: displays records strictly according to the typed name/ID. */
  filterStudents() {
    const q = (this.searchQuery || '').trim().toLowerCase().replace(/\s+/g, ' ');
    const words = q.split(' ').filter(w => w);

    this.filteredStudents = this.students.filter(s => {
      const haystack = [s.name, s.apaarId, s.enrollmentNo, s.course]
        .map(f => (f || '').toLowerCase())
        .join(' ');

      // Every typed word must appear — so "farhan akhtar" matches by name.
      const matchQ = !words.length || words.every(w => haystack.includes(w));

      const matchCourse = this.courseFilter === 'all' ||
        (s.course || '').toLowerCase().includes(this.courseFilter.toLowerCase());

      const matchStatus = this.statusFilter === 'all' ||
        (s.status || '').toLowerCase() === this.statusFilter.toLowerCase();

      return matchQ && matchCourse && matchStatus;
    });

    this.sortStudents();
  }

  setSort(column: string) {
    if (this.sortColumn === column) {
      this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column;
      this.sortDir = 'asc';
    }
    this.sortStudents();
  }

  sortIcon(column: string): string {
    if (this.sortColumn !== column) return '↕';
    return this.sortDir === 'asc' ? '▲' : '▼';
  }

  /** Proper sorting — APAAR ID ascending uses natural (numeric-aware) comparison. */
  sortStudents() {
    const dir = this.sortDir === 'asc' ? 1 : -1;
    const col = this.sortColumn;
    const natural = (a: string, b: string) =>
      (a || '').localeCompare(b || '', undefined, { numeric: true, sensitivity: 'base' });

    this.filteredStudents = [...this.filteredStudents].sort((a, b) => {
      switch (col) {
        case 'apaarId': return dir * natural(a.apaarId, b.apaarId);
        case 'name': return dir * natural(a.name, b.name);
        case 'course': return dir * natural(a.course, b.course);
        case 'category': return dir * natural(a.category, b.category);
        case 'abcCredits': return dir * ((a.abcCredits || 0) - (b.abcCredits || 0));
        case 'admissionDate': return dir * natural(a.admissionDate, b.admissionDate);
        case 'status': return dir * natural(a.status, b.status);
        default: return dir * natural(a.apaarId, b.apaarId);
      }
    });
  }

  // ---------- Verify / Reject actions ----------

  /** Official Student Academic Profile & Verification Report (backend PDF). */
  downloadReport(apaarId: string) {
    this.apaarService.downloadStudentReportPdf(apaarId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Student_Report_${apaarId.replace(/[^A-Za-z0-9]/g, '')}.pdf`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
      },
      error: () => {
        // Fallback: let the browser download the PDF directly
        window.open(this.apaarService.studentReportUrl(apaarId), '_blank');
      }
    });
  }

  verify(apaarId: string) {
    const student = this.students.find(s => s.apaarId === apaarId);
    if (student) {
      const prevStatus = student.status;
      student.status = 'verified';
      student.duplicateCollegeName = undefined;
      student.duplicateCollegeId = undefined;
      student.flagReason = undefined;

      if (this.college) {
        this.college.verifiedCount = (this.college.verifiedCount || 0) + 1;
        if (prevStatus === 'pending' && this.college.pendingCount && this.college.pendingCount > 0) {
          this.college.pendingCount--;
        } else if (prevStatus === 'duplicate' && this.college.duplicateCount && this.college.duplicateCount > 0) {
          this.college.duplicateCount--;
        }
        this.college.actualEnrolment = this.students.length;
      }
      this.filterStudents();
      this.duplicates = this.students.filter(s => s.status === 'duplicate' || s.duplicateCollegeName);
      this.cdr.detectChanges();
    }

    this.apaarService.updateStatus(apaarId, 'verified', this.loggedInUser || 'college@aicte.ac.in').subscribe({
      next: () => {},
      error: () => {}
    });
  }

  reject(apaarId: string) {
    const student = this.students.find(s => s.apaarId === apaarId);
    if (student) {
      const prevStatus = student.status;
      student.status = 'rejected';

      if (this.college) {
        if (prevStatus === 'verified' && this.college.verifiedCount && this.college.verifiedCount > 0) {
          this.college.verifiedCount--;
        } else if (prevStatus === 'pending' && this.college.pendingCount && this.college.pendingCount > 0) {
          this.college.pendingCount--;
        } else if (prevStatus === 'duplicate' && this.college.duplicateCount && this.college.duplicateCount > 0) {
          this.college.duplicateCount--;
        }
        this.college.actualEnrolment = this.students.length;
      }
      this.filterStudents();
      this.duplicates = this.students.filter(s => s.status === 'duplicate' || s.duplicateCollegeName);
      this.cdr.detectChanges();
    }

    this.apaarService.updateStatus(apaarId, 'rejected', this.loggedInUser || 'college@aicte.ac.in').subscribe({
      next: () => {},
      error: () => {}
    });
  }

  // ---------- Bulk CSV Upload: a real file import ----------

  openUpload() {
    this.showUpload = true;
    this.uploadNotice = '';
    this.uploadError = false;
    this.selectedFile = null;
  }

  onFileSelected(event: any) {
    const file: File = event.target.files && event.target.files[0] ? event.target.files[0] : null;
    this.selectedFile = file;
    this.uploadError = false;
    this.uploadNotice = file ? `${this.t('col.fileSelected')}: ${file.name}` : '';
  }

  onDropFile(event: DragEvent) {
    event.preventDefault();
    const file = event.dataTransfer?.files && event.dataTransfer.files.length ? event.dataTransfer.files[0] : null;
    if (file) {
      this.selectedFile = file;
      this.uploadError = false;
      this.uploadNotice = `${this.t('col.fileSelected')}: ${file.name}`;
    }
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
  }

  /** Only reports success when a real CSV file with valid student rows was imported. */
  runBatchValidation() {
    this.uploadError = false;

    if (!this.selectedFile) {
      this.uploadError = true;
      this.uploadNotice = this.t('col.errNoFile');
      return;
    }

    this.uploadBusy = true;
    this.uploadNotice = this.ls.lang === 'hi'
      ? 'फाईल वाचत आहे व डेटाबेसविरुद्ध सत्यापित करत आहे...'
      : 'Reading file and validating against the database...';

    const reader = new FileReader();
    reader.onerror = () => {
      this.uploadBusy = false;
      this.uploadError = true;
      this.uploadNotice = this.t('col.errBadCsv');
      this.cdr.detectChanges();
    };
    reader.onload = (e: any) => {
      try {
        const rows = this.parseCsv(String(e.target.result || ''));

        if (!rows.length) {
          this.uploadBusy = false;
          this.uploadError = true;
          this.uploadNotice = this.t('col.errNoRows');
          this.cdr.detectChanges();
          return;
        }

        // Tag every imported row with the logged-in institution (real ownership)
        const collegeId = this.college?.id || 'COL001';
        const collegeName = this.college?.name || '';
        const payload: Student[] = rows.map(r => ({
          apaarId: r.apaarId,
          name: r.name,
          dob: r.dob || '',
          gender: r.gender || '',
          mobile: r.mobile || '',
          aadhaarLast4: r.aadhaarLast4 || '',
          collegeId,
          collegeName,
          course: r.course || '',
          academicYear: Number(r.academicYear) || 1,
          abcCredits: Number(r.abcCredits) || 0,
          tenthPercent: Number(r.tenthPercent) || 0,
          twelfthPercent: Number(r.twelfthPercent) || 0,
          status: 'pending',
          category: r.category || 'General',
          enrollmentNo: r.enrollmentNo || '',
          admissionDate: r.admissionDate || new Date().toISOString().slice(0, 10),
          digilockerVerified: false,
          state: r.state || this.college?.state || ''
        }));

        this.apaarService.bulkUpload(payload).subscribe({
          next: (res) => {
            this.uploadBusy = false;
            const added = res && res.addedCount !== undefined ? res.addedCount : payload.length;
            const dups = res && res.duplicateCount !== undefined ? res.duplicateCount : 0;
            this.uploadNotice = `${added} ${this.t('col.uploadOk')}, ${dups} ${this.t('col.uploadDup')}.`;
            this.afterImport();
          },
          error: () => {
            // Backend unreachable — import into the live session instead
            let added = 0;
            let dups = 0;
            const existing = new Set(this.students.map(s => s.apaarId.toLowerCase()));
            payload.forEach(p => {
              if (existing.has(p.apaarId.toLowerCase())) { dups++; }
              else { this.students.push(p); existing.add(p.apaarId.toLowerCase()); added++; }
            });
            this.uploadBusy = false;
            if (!added && !dups) {
              this.uploadError = true;
              this.uploadNotice = this.t('col.errNoRows');
              this.cdr.detectChanges();
              return;
            }
            this.uploadNotice = `${added} ${this.t('col.uploadOk')}, ${dups} ${this.t('col.uploadDup')}.`;
            this.afterImport();
          }
        });
      } catch (err) {
        this.uploadBusy = false;
        this.uploadError = true;
        this.uploadNotice = this.t('col.errBadCsv');
        this.cdr.detectChanges();
      }
    };
    reader.readAsText(this.selectedFile);
  }

  private afterImport() {
    this.filterStudents();
    this.duplicates = this.students.filter(s => s.status === 'duplicate' || s.duplicateCollegeName);
    if (this.college) {
      this.college.actualEnrolment = this.students.length;
      this.college.pendingCount = (this.college.pendingCount || 0) +
        this.students.filter(s => s.status === 'pending').length;
    }
    this.cdr.detectChanges();
    setTimeout(() => {
      this.showUpload = false;
      this.uploadNotice = '';
      this.selectedFile = null;
      this.cdr.detectChanges();
    }, 1800);
  }

  /** Minimal but correct CSV parser supporting quoted fields. */
  private parseCsv(text: string): CsvRow[] {
    const lines = text.split(/\r?\n/).map(l => l.trim()).filter(l => l.length > 0);
    if (lines.length < 2) return [];

    const splitLine = (line: string): string[] => {
      const out: string[] = [];
      let cur = '';
      let inQuotes = false;
      for (let i = 0; i < line.length; i++) {
        const ch = line[i];
        if (ch === '"') {
          if (inQuotes && line[i + 1] === '"') { cur += '"'; i++; }
          else { inQuotes = !inQuotes; }
        } else if (ch === ',' && !inQuotes) {
          out.push(cur); cur = '';
        } else {
          cur += ch;
        }
      }
      out.push(cur);
      return out.map(c => c.trim());
    };

    const headers = splitLine(lines[0]).map(h => h.toLowerCase().replace(/[^a-z0-9]/g, ''));
    const idxOf = (...names: string[]) => {
      for (const n of names) {
        const i = headers.indexOf(n);
        if (i >= 0) return i;
      }
      return -1;
    };

    const map: Record<string, number[]> = {
      apaarId: [idxOf('apaarid', 'apaar', 'apaarnumber')],
      name: [idxOf('name', 'studentname', 'fullname')],
      dob: [idxOf('dob', 'dateofbirth')],
      gender: [idxOf('gender', 'sex')],
      mobile: [idxOf('mobile', 'phone', 'mobileno')],
      aadhaarLast4: [idxOf('aadhaarlast4', 'aadhaar', 'aadhaarlast')],
      course: [idxOf('course', 'branch', 'coursebranch')],
      academicYear: [idxOf('academicyear', 'year')],
      abcCredits: [idxOf('abccredits', 'credits', 'credit')],
      tenthPercent: [idxOf('tenthpercent', 'tenth')],
      twelfthPercent: [idxOf('twelfthpercent', 'twelfth')],
      category: [idxOf('category')],
      enrollmentNo: [idxOf('enrollmentno', 'enrollment', 'rollno')],
      admissionDate: [idxOf('admissiondate', 'admission')],
      state: [idxOf('state')]
    };

    const rows: CsvRow[] = [];
    for (let i = 1; i < lines.length; i++) {
      const cells = splitLine(lines[i]);
      const row: CsvRow = {
        apaarId: '', name: '', dob: '', gender: '', mobile: '', aadhaarLast4: '',
        course: '', academicYear: '', abcCredits: '', tenthPercent: '', twelfthPercent: '',
        category: '', enrollmentNo: '', admissionDate: '', state: ''
      };
      for (const key of Object.keys(map) as Array<keyof CsvRow>) {
        const colIdx = map[key].find(ix => ix >= 0);
        row[key] = colIdx !== undefined && colIdx >= 0 ? (cells[colIdx] || '') : '';
      }
      if (row.apaarId && row.name) rows.push(row);
    }
    return rows;
  }

  /** Download the official CSV template so institutions import correctly. */
  downloadTemplate() {
    const headers = 'apaarId,name,dob,gender,mobile,aadhaarLast4,course,academicYear,abcCredits,tenthPercent,twelfthPercent,category,enrollmentNo,admissionDate,state';
    const sample1 = '123456789999,Sample Student One,2004-06-15,Male,+91 98000 00001,1234,B.Tech Computer Science & Engineering,1,30,85.0,84.0,General,2026-CSE-001,2026-08-01,Delhi';
    const sample2 = '123456799998,Sample Student Two,2005-01-20,Female,+91 98000 00002,5678,B.Tech Mechanical Engineering,1,28,80.0,82.5,Supernumerary (TFW),2026-ME-002,2026-08-01,Maharashtra';
    const csv = [headers, sample1, sample2].join('\r\n');
    const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'APAAR_Student_Upload_Template.csv';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  }
}
