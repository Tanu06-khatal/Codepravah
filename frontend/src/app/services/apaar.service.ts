import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Student {
  id?: number;
  apaarId: string;
  name: string;
  dob: string;
  gender: string;
  mobile: string;
  aadhaarLast4: string;
  collegeId: string;
  collegeName: string;
  course: string;
  academicYear: number;
  abcCredits: number;
  tenthPercent: number;
  twelfthPercent: number;
  status: string;
  category: string;
  enrollmentNo: string;
  admissionDate: string;
  digilockerVerified: boolean;
  state: string;
  photoUrl?: string;
  duplicateCollegeName?: string;
  duplicateCollegeId?: string;
  flagReason?: string;
}

export interface College {
  id: string;
  name: string;
  code: string;
  state: string;
  city: string;
  approvedIntake: number;
  actualEnrolment: number;
  verifiedCount: number;
  pendingCount: number;
  duplicateCount: number;
  supernumeraryQuota: number;
  nriCount: number;
  fnCount: number;
  email?: string;
}

@Injectable({
  providedIn: 'root',
})
export class ApaarService {
  private get baseUrl(): string {
    if (typeof window !== 'undefined') {
      const customUrl = (window as any).API_URL || localStorage.getItem('API_URL');
      if (customUrl) return customUrl;
      const host = window.location.hostname;
      if (host !== 'localhost' && host !== '127.0.0.1') {
        return 'https://codepravah.onrender.com/api';
      }
    }
    return 'http://localhost:8085/api';
  }

  constructor(private http: HttpClient) {}

  verifyApaar(apaarId: string): Observable<any> {
    return this.http.get(`${this.baseUrl}/verify/${encodeURIComponent(apaarId)}`);
  }

  updateStatus(apaarId: string, status: string, actorEmail: string = 'college@aicte.ac.in', role: string = 'COLLEGE_ADMIN'): Observable<any> {
    return this.http.post(`${this.baseUrl}/verify/${encodeURIComponent(apaarId)}/status`, {
      status,
      actorEmail,
      role,
    });
  }

  getCollegeStats(collegeId: string = 'COL001'): Observable<any> {
    return this.http.get(`${this.baseUrl}/college/stats?collegeId=${collegeId}`);
  }

  getStudents(params?: any): Observable<any> {
    let url = `${this.baseUrl}/college/students`;
    if (params) {
      const q = new URLSearchParams(params).toString();
      url += `?${q}`;
    }
    return this.http.get(url);
  }

  bulkUpload(students: Student[]): Observable<any> {
    return this.http.post(`${this.baseUrl}/college/bulk-upload`, students);
  }

  getAllColleges(): Observable<any> {
    return this.http.get(`${this.baseUrl}/aicte/colleges`);
  }

  /** Live national statistics computed from the actual database. */
  getNationalStats(): Observable<any> {
    return this.http.get(`${this.baseUrl}/aicte/stats`);
  }

  /** Persist a newly registered institution to the backend database. */
  registerCollege(payload: {
    pid: string; name: string; email: string; state: string; city?: string; approvedIntake: number;
  }): Observable<any> {
    return this.http.post(`${this.baseUrl}/college/register`, payload);
  }

  getAuditLogs(): Observable<any> {
    return this.http.get(`${this.baseUrl}/aicte/logs`);
  }

  downloadExcelReport(): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/aicte/export-excel`, { responseType: 'blob' });
  }

  /** Official National Compliance PDF report (backend-generated). */
  downloadNationalPdf(): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/aicte/export-pdf`, { responseType: 'blob' });
  }

  /** Official Student Academic Profile & Verification Report (PDF). */
  downloadStudentReportPdf(apaarId: string): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/college/student-report?apaarId=${encodeURIComponent(apaarId)}`, { responseType: 'blob' });
  }

  /** Absolute URL for direct browser navigation to a generated report. */
  studentReportUrl(apaarId: string): string {
    return `${this.baseUrl}/college/student-report?apaarId=${encodeURIComponent(apaarId)}`;
  }

  /** Resolve a backend static file path (e.g. /photos/x.jpg) against the API origin. */
  fileUrl(path?: string | null): string {
    if (!path) return '';
    if (path.startsWith('http')) return path;
    const apiBase = this.baseUrl;
    return apiBase.replace(/\/api\/?$/, '') + path;
  }
}
