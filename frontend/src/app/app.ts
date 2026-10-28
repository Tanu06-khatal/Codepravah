import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { LanguageService } from './services/language.service';

/**
 * One knowledge-base entry for the AICTE Sahayak virtual assistant.
 * Matching is bilingual — keyword lists contain both English and Hindi cues
 * so the bot answers correctly no matter which language the user types in.
 */
interface BotEntry {
  keywords: string[];
  en: string;
  hi: string;
}

const BOT_KNOWLEDGE: BotEntry[] = [
  {
    keywords: ['hello', 'hi ', 'hii', 'hey', 'namaste', 'namaskar', 'नमस्ते', 'नमस्कार', 'नमस्कार', 'हाय', 'greeting'],
    en: 'Namaste! I can help you with APAAR ID verification, ABC credits, duplicate enrolment detection, intake rules, institute registration, student uploads and compliance reports. What would you like to know?',
    hi: 'नमस्ते! मी अपार आयडी सत्यापन, ABC क्रेडिट, दुहेरी नोंद तपासणी, प्रवेश नियम, संस्था नोंदणी, विद्यार्थी अपलोड व अनुपालन अहवाल याबद्दल मदत करू शकतो. तुम्हाला काय जाणून घ्यायचे आहे?'
  },
  {
    keywords: ['apaar', 'अपार', 'apaar id', '12-digit', '१२ अंकी'],
    en: 'APAAR ID (Automated Permanent Academic Account Registry) is a 12-digit lifelong academic identifier under NEP 2020. It links your DigiLocker, Academic Bank of Credits (ABC) and AICTE approval databases into one verifiable student identity.',
    hi: 'अपार आयडी (स्वयंचलित कायमस्वरूपी शैक्षणिक खाते नोंदणी) ही NEP 2020 अंतर्गत १२-अंकी आजीवन शैक्षणिक ओळख आहे. ती तुमचा डिजीलॉकर, क्रेडिट बँक (ABC) व AICTE मान्यता डेटाबेस एका सत्यापित विद्यार्थी ओळखीत जोडते.'
  },
  {
    keywords: ['credit', 'abc', 'क्रेडिट', 'cr ', 'क्रे'],
    en: 'ABC credits are Academic Bank of Credits units defined by UGC. 1 credit equals 15 learning hours, and a full B.Tech programme carries roughly 160-180 credits across 8 semesters. Credits are always expressed in plain "Credits" (not marks or grades) and are stored in your DigiLocker-linked ABC account.',
    hi: 'ABC क्रेडिट म्हणजे UGC ने परिभाषित क्रेडिट बँक एकके. १ क्रेडिट = १५ शिकण्याचे तास, आणि संपूर्ण बी.टेक अभ्यासक्रमासाठी सुमारे 160-180 क्रेडिट असतात. क्रेडिट हे नेहमी "क्रेडिट" एककात मोजले जातात आणि तुमच्या डिजीलॉकर-लिंक केलेल्या ABC खात्यात साठवले जातात.'
  },
  {
    keywords: ['duplicate', 'दुहेरी', 'conflict', 'dual'],
    en: 'The platform continuously checks every new enrolment against the national registry. If the same APAAR ID is found active at two institutions, a duplicate anomaly is raised instantly, both colleges are notified, and the AICTE dashboard flags the conflict for resolution (Verify to override or Reject to block the extra seat).',
    hi: 'हे मंच प्रत्येक नवीन नोंदणीची राष्ट्रीय नोंदणीपत्रकाशी सतत तपासणी करते. एकाच अपार आयडी दोन संस्थांमध्ये सक्रिय आढळल्यास तत्काळ दुहेरी विसंगती नोंदवली जाते, दोन्ही महाविद्यालयांना कळवले जाते आणि AICTE डॅशबोर्डवर संघर्ष चिन्हांकित होतो.'
  },
  {
    keywords: ['intake', 'प्रवेश क्षमता', 'sanctioned', 'exceed', 'over'],
    en: 'Colleges cannot admit students beyond their Sanctioned Approved Intake. Only limited supernumerary allowances exist: the 5% TFW (Tuition Fee Waiver) quota and up to 15% for NRI/Foreign National seats. Any excess admission triggers a real-time regulatory anomaly alert.',
    hi: 'महाविद्यालये मंजूर प्रवेश क्षमतेपेक्षा जास्त विद्यार्थी प्रवेश देऊ शकत नाहीत. फक्त मर्यादित अतिरिक्त कोटे आहेत: ५% TFW (शुल्कमाफी) कोटा व NRI/परदेशी नागरिक सीटसाठी १५% पर्यंत. जास्त प्रवेश झाल्यास वास्तविक-वेळ नियामक सूचना दिली जाते.'
  },
  {
    keywords: ['tfw', 'nri', 'supernumerary', 'quota', 'कोटा', 'foreign'],
    en: 'Statutory quotas tracked by this platform: TFW (Tuition Fee Waiver) supernumerary seats up to 5% of sanctioned intake, NRI (Non-Resident Indian) seats up to 15%, OCI and Foreign National allocations. All are audited separately from regular intake.',
    hi: 'या मंचावर मागोवा घेतल्या जाणाऱ्या वैधानिक कोटा: TFW (शुल्कमाफी) अतिरिक्त सीट ५% पर्यंत, NRI सीट १५% पर्यंत, OCI व परदेशी नागरिक वाटणी. सर्वाचे स्वतंत्र लेखापरीक्षण केले जाते.'
  },
  {
    keywords: ['digilocker', 'डिजीलॉकर', 'locker'],
    en: 'DigiLocker is the Government of India digital document wallet. Your APAAR ID, marksheets and ABC credit summary are issued into DigiLocker, which this platform uses to verify documents cryptographically instead of manual checking.',
    hi: 'डिजीलॉकर हे भारत सरकारचे डिजिटल दस्तऐवज वॉलेट आहे. तुमचा अपार आयडी, गुणपत्रिका व ABC क्रेडिट सारांश डिजीलॉकरमध्ये जारी होतात, आणि या मंचावर ती क्रिप्टोग्राफिक पद्धतीने सत्यापित केली जातात.'
  },
  {
    keywords: ['nad', 'depository', 'डिपॉझिटरी'],
    en: 'The National Academic Depository (NAD) is the DigiLocker-powered repository that stores authentic academic awards (degrees, marksheets, certificates) digitally. All verified records on this platform sync with NAD.',
    hi: 'राष्ट्रीय शैक्षणिक डिपॉझिटरी (NAD) हे डिजीलॉकर-आधारित साठे आहे जिथे प्रामाणिक शैक्षणिक प्रमाणपत्रे डिजिटलरित्या साठवली जातात. या मंचावरील सर्व सत्यापित नोंदी NAD सह सिंक होतात.'
  },
  {
    keywords: ['register', 'नोंदणी', 'institute', 'institution', 'संस्था', 'signup', 'sign up', 'onboard'],
    en: 'To register your institute: open the College Admin portal, choose "Request Access / Register", fill in the AICTE Permanent Institute ID, institution name, official principal/registrar email, passkey, affiliating university & state, and sanctioned intake. After submission you can sign in and import your students via Bulk Upload CSV.',
    hi: 'तुमची संस्था नोंदणी करण्यासाठी: महाविद्यालय प्रशासक पोर्टल उघडा, "प्रवेश मागणी / नोंदणी" निवडा, AICTE कायमस्वरूपी संस्था आयडी, संस्थेचे नाव, अधिकृत ईमेल, पासकी, संलग्नक विद्यापीठ व राज्य आणि मंजूर प्रवेश भरा. नंतर साइन इन करून बल्क अपलोड द्वारे विद्यार्थी आयात करा.'
  },
  {
    keywords: ['upload', 'csv', 'import', 'bulk', 'batch', 'अपलोड', 'आयात'],
    en: 'Bulk Upload: in the College dashboard click "Bulk Upload CSV", choose your .csv file (use the downloadable template: apaarId, name, dob, gender, course, academicYear, abcCredits, category, enrollmentNo, admissionDate, state), then click "Run Batch Validation". Records are validated against APAAR format and duplicates are flagged automatically.',
    hi: 'बल्क अपलोड: महाविद्यालय डॅशबोर्डवर "बल्क अपलोड CSV" क्लिक करा, तुमची .csv फाईल निवडा (नमुना साचा डाउनलोड करा: apaarId, name, dob, gender, course, academicYear, abcCredits, category, enrollmentNo, admissionDate, state), नंतर "बॅच सत्यापन सुरू करा" क्लिक करा. नोंदी स्वयंचलितपणे सत्यापित होतात.'
  },
  {
    keywords: ['report', 'excel', 'xlsx', 'download', 'export', 'अहवाल', 'डाउनलोड'],
    en: 'Compliance reports: the AICTE Super Admin dashboard can export the National Compliance Report (.xlsx) with intake variance, verification %, quota seats and audit status for every registered institution — generated live from the database. The PDF export prints the same data as a formatted document.',
    hi: 'अनुपालन अहवाल: AICTE महा प्रशासक डॅशबोर्डवरून राष्ट्रीय अनुपालन अहवाल (.xlsx) डाउनलोड करता येतो, ज्यात प्रत्येक नोंदणीकृत संस्थेचा प्रवेश फरक, सत्यापन %, कोटा सीट व ऑडिट स्थिती डेटाबेसमधून थेट तयार होते.'
  },
  {
    keywords: ['status', 'स्थिती', 'verified', 'pending', 'rejected', 'सत्यापित', 'प्रलंबित'],
    en: 'Record statuses: VERIFIED = student documents validated and seat approved. PENDING = DigiLocker validation queued. DUPLICATE = same APAAR ID active at another institution. REJECTED = enrolment cancelled by college/AICTE. College admins can Verify or Reject pending and duplicate records from the registry table.',
    hi: 'नोंद स्थिती: VERIFIED = कागदपत्रे सत्यापित व सीट मंजूर. PENDING = डिजीलॉकर सत्यापन प्रतीक्षारत. DUPLICATE = दुसऱ्या संस्थेत तोच अपार आयडी सक्रिय. REJECTED = प्रवेश रद्द. प्रशासक नोंदणी पत्रकातून सत्यापित करू शकतात.'
  },
  {
    keywords: ['consent', 'dpdp', 'privacy', 'संमती', 'गोपनीयता'],
    en: 'Under the DPDP Act 2023, a student\'s APAAR data is shared only after explicit statutory consent. The Student Portal requires the consent checkbox before fetching any profile, and every access is audit-logged.',
    hi: 'DPDP कायदा 2023 अंतर्गत विद्यार्थ्याचा अपार डेटा फक्त स्पष्ट वैधानिक संमतीनंतरच शेअर होतो. विद्यार्थी पोर्टलवर प्रोफाइल आणण्यापूर्वी संमती चेकबॉक्स आवश्यक आहे आणि प्रत्येक प्रवेश ऑडिट-लॉग केला जातो.'
  },
  {
    keywords: ['helpline', 'contact', 'phone', 'support', 'helpdesk', 'संपर्क', 'मदत'],
    en: 'AICTE APAAR Integration Helpline: (011) 29581333, 29581338, 29581342 — available 09:30 AM to 06:00 PM, Monday to Friday. You can also start a live video session from the home page support card.',
    hi: 'AICTE अपार एकीकरण हेल्पलाईन: (011) 29581333, 29581338, 29581342 — सोमवार तुक्के शुक्रवार सकाळी ९:३० ते संध्याकाळी ६:००. मुख्यपृष्ठावरील समर्थन कार्डावरून थेट व्हिडिओ सत्र देखील सुरू करू शकता.'
  },
  {
    keywords: ['qr', 'seal', 'hash', 'sha'],
    en: 'Every Verifiable Academic Credential Pass carries a cryptographic QR seal. Scanning it lets any verifier confirm the APAAR ID and Student ID dual imprint against the central ledger, with a SHA-256 hash integrity check.',
    hi: 'प्रत्येक सत्यापित शैक्षणिक प्रमाणपत्र पासवर क्रिप्टोग्राफिक QR सील असते. स्कॅन केल्यावर कोणताही सत्यापक केंद्रीय नोंदवहीविरुद्ध अपार आयडी व विद्यार्थी आयडीची दुहेरी छाप SHA-256 हॅश सह तपासू शकतो.'
  },
  {
    keywords: ['pass', 'card', 'id card', 'पास', 'कार्ड', 'certificate', 'प्रमाणपत्र'],
    en: 'The Student Portal generates an Official Verifiable Academic Credential Pass after APAAR verification. It shows your photo slot, full academic details, ABC credits, institution, verification timeline and the QR seal — and can be printed as a formal document.',
    hi: 'विद्यार्थी पोर्टल अपार सत्यापनानंतर अधिकृत सत्यापित शैक्षणिक प्रमाणपत्र पास तयार करतो. त्यात छायाचित्र, संपूर्ण शैक्षणिक तपशील, ABC क्रेडिट, संस्था, सत्यापन कालरेखा व QR सील असतात — आणि औपचारिक दस्तऐवज म्हणून प्रिंट करता येतो.'
  },
  {
    keywords: ['login', 'google', 'sign in', 'signin', 'लॉगिन', 'साइन इन'],
    en: 'Sign-in options: College admins can use "Continue with Google" with any Google account, or institutional credentials. Students need no login — they verify directly by APAAR ID. The AICTE Super Admin portal is restricted to regulatory officers.',
    hi: 'साइन-इन पर्याय: महाविद्यालय प्रशासक कोणत्याही Google खात्याने "Google द्वारे सुरू ठेवा" किंवा संस्थागत खाते वापरू शकतात. विद्यार्थ्यांना लॉगिनची गरज नाही — थेट अपार आयडीने सत्यापन होते.'
  },
  {
    keywords: ['language', 'hindi', 'हिंदी', 'भाषा', 'translate'],
    en: 'Use the language button in the top-right corner (English / हिन्दी) to switch the entire platform between English and Hindi instantly. Your preference is remembered on this device.',
    hi: 'वरील उजव्या कोपऱ्यातील भाषा बटण (English / हिन्दी) वापरून संपूर्ण मंच तत्काळ इंग्रजी-हिंदी दरम्यान बदला. तुमची पसंती या उपकरणावर लक्षात ठेवली जाते.'
  },
  {
    keywords: ['thank', 'धन्यवाद', 'thanks', 'शुक्रिया'],
    en: 'You are most welcome! Jai Hind. If you need anything else about APAAR verification or the AICTE platform, I am always here.',
    hi: 'आपले स्वागत आहे! जय हिंद. अपार सत्यापन किंवा AICTE मंचाबद्दल आणखी काही माहिती हवी असल्यास मी नेहमी उपलब्ध आहे.'
  },
  {
    keywords: ['help', 'sahay', 'मदत', 'what can you', 'सहाय्य'],
    en: 'I can answer questions about: APAAR ID, ABC credits, duplicate detection, intake & quota rules, DigiLocker/NAD, institute registration, bulk CSV upload, compliance reports, record statuses, DPDP consent, the QR credential pass, helpline numbers and language settings.',
    hi: 'मी याबद्दल उत्तरे देऊ शकतो: अपार आयडी, ABC क्रेडिट, दुहेरी नोंद तपासणी, प्रवेश व कोटा नियम, डिजीलॉकर/NAD, संस्था नोंदणी, बल्क CSV अपलोड, अनुपालन अहवाल, नोंद स्थिती, DPDP संमती, QR पास, हेल्पलाईन व भाषा सेटिंग्ज.'
  },
  {
    keywords: ['aicte', 'अखिल भारतीय', 'council'],
    en: 'AICTE (All India Council for Technical Education) is the statutory body under the Ministry of Education regulating technical education. This platform is its APAAR-based student data management and verification gateway operated by the Regulation Bureau.',
    hi: 'AICTE (अखिल भारतीय तकनीकी शिक्षा परिषद्) ही शिक्षा मंत्रालयाअंतर्गत तांत्रिक शिक्षणाची वैधानिक संस्था आहे. हे मंच तिचे अपार-आधारित विद्यार्थी डेटा व्यवस्थापन व सत्यापन गेटवे आहे.'
  },
  {
    keywords: ['nep', 'policy', 'नीति', 'नीती'],
    en: 'NEP 2020 (National Education Policy) mandates one nation, one student ID. APAAR implements it technically — a single lifelong academic identity connected to DigiLocker, ABC credits and every AICTE-approved institution.',
    hi: 'NEP 2020 (राष्ट्रीय शैक्षणिक धोरण) एक राष्ट्र, एक विद्यार्थी आयडी अनिवार्य करते. अपार आयडी ते तांत्रिकरित्या अंमलात आणते — एक आजीवन शैक्षणिक ओळख, डिजीलॉकर, ABC क्रेडिट व प्रत्येक AICTE-मान्यताप्राप्त संस्थेशी जोडलेली.'
  },
  {
    keywords: ['aadhaar', 'kyc', 'आधार', 'e-kyc'],
    en: 'Aadhaar e-KYC is the first step of APAAR verification — your 12-digit APAAR ID is cryptographically linked to your Aadhaar identity (only the last 4 digits are ever displayed for privacy).',
    hi: 'आधार ई-केवायसी ही अपार सत्यापनाची पहिली पायरी आहे — तुमचा अपार आयडी तुमच्या आधार ओळखीशी क्रिप्टोग्राफिकरित्या जोडलेला असतो (गोपनीयतेसाठी फक्त शेवटचे ४ अंक दाखवले जातात).'
  },
  {
    keywords: ['search', 'शोध', 'find', 'filter', 'फिल्टर'],
    en: 'Search tips: in the College registry type the student name (or part of it) or the 12-digit APAAR ID — results filter live and can be sorted by clicking any column header. The AICTE table searches by college name, code or state.',
    hi: 'शोध टिप्स: महाविद्यालय नोंदणी पत्रकात विद्यार्थ्याचे नाव किंवा १२-अंकी अपार आयडी टाइप करा — परिणाम तत्काळ फिल्टर होतात, स्तंभ शीर्षक क्लिक करून क्रमवारी लावता येते.'
  }
];

const BOT_FALLBACK = {
  en: 'I am trained on APAAR verification, ABC credits, duplicate detection, intake & quota rules, DigiLocker/NAD, institute registration, CSV uploads, reports, statuses and helpline info. Could you rephrase your question using one of those topics?',
  hi: 'मी अपार सत्यापन, ABC क्रेडिट, दुहेरी नोंद तपासणी, प्रवेश व कोटा नियम, डिजीलॉकर/NAD, संस्था नोंदणी, CSV अपलोड, अहवाल, स्थिती व हेल्पलाईन याबद्दल प्रशिक्षित आहे. कृपया या विषयांपैकी एक निवडून प्रश्न विचारा?'
};

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  currentDateStr: string = '';
  botOpen: boolean = false;
  botInput: string = '';
  currentUrl: string = '/';
  botMessages: Array<{ sender: 'bot' | 'user'; text?: string; key?: string }> = [
    { sender: 'bot', key: 'bot.greet' }
  ];

  constructor(public ls: LanguageService, private router: Router) {}

  ngOnInit() {
    this.router.events.subscribe(e => {
      if (e instanceof NavigationEnd) {
        this.currentUrl = e.urlAfterRedirects || e.url;
      }
    });
    this.updateTime();
    setInterval(() => this.updateTime(), 1000);
  }

  updateTime() {
    const now = new Date();
    this.currentDateStr = now.toLocaleString(this.ls.lang === 'hi' ? 'hi-IN' : 'en-IN', {
      weekday: 'long',
      month: '2-digit',
      day: '2-digit',
      year: 'numeric',
      hour: 'numeric',
      minute: 'numeric',
      second: 'numeric',
      hour12: true
    });
  }

  /** Portal currently being visited — used to hide every other portal from navigation. */
  get currentPortal(): 'student' | 'college' | 'aicte' | null {
    if (this.currentUrl.startsWith('/student')) return 'student';
    if (this.currentUrl.startsWith('/college')) return 'college';
    if (this.currentUrl.startsWith('/aicte')) return 'aicte';
    return null;
  }

  get isPortal(): boolean {
    return this.currentPortal !== null;
  }

  t(key: string): string {
    return this.ls.t(key);
  }

  toggleBot() {
    this.botOpen = !this.botOpen;
  }

  sendBotMessage(query?: string) {
    const text = (query || this.botInput || '').trim();
    if (!text) return;

    this.botMessages.push({ sender: 'user', text });
    if (!query) this.botInput = '';

    setTimeout(() => {
      const l = ' ' + text.toLowerCase() + ' ';

      // Score every knowledge entry: more keyword hits win.
      let best: BotEntry | null = null;
      let bestScore = 0;
      for (const entry of BOT_KNOWLEDGE) {
        let score = 0;
        for (const kw of entry.keywords) {
          if (l.includes(kw.toLowerCase())) score++;
        }
        if (score > bestScore) {
          bestScore = score;
          best = entry;
        }
      }

      const reply = best
        ? (this.ls.lang === 'hi' ? best.hi : best.en)
        : (this.ls.lang === 'hi' ? BOT_FALLBACK.hi : BOT_FALLBACK.en);

      this.botMessages.push({ sender: 'bot', text: reply });
    }, 450);
  }
}
