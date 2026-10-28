import { Routes } from '@angular/router';
import { LandingComponent } from './pages/landing/landing.component';
import { StudentComponent } from './pages/student/student.component';
import { CollegeComponent } from './pages/college/college.component';
import { AicteComponent } from './pages/aicte/aicte.component';

export const routes: Routes = [
  { path: '', component: LandingComponent },
  { path: 'student', component: StudentComponent },
  { path: 'college', component: CollegeComponent },
  { path: 'aicte', component: AicteComponent },
  { path: '**', redirectTo: '' }
];
