import {
  useEffect,
  useMemo,
  useRef,
  useState,
  type Dispatch,
  type FormEvent,
  type ReactNode,
  type SetStateAction,
} from "react";
import {
  ArrowDown,
  ArrowLeftRight,
  ArrowRight,
  AudioLines,
  Bookmark,
  BookmarkCheck,
  BookOpen,
  BookOpenCheck,
  CalendarDays,
  Check,
  CheckCheck,
  ChevronDown,
  ChevronLeft,
  ChevronRight,
  CircleHelp,
  Clock3,
  Compass,
  Dumbbell,
  FileText,
  Footprints,
  Heart,
  Home,
  List,
  MapPin,
  Menu,
  Minus,
  Moon,
  Pause,
  Play,
  Plus,
  RotateCcw,
  Search,
  Settings,
  Shield,
  Sparkles,
  Sun,
  Timer,
  Type,
  Volume2,
  X,
} from "lucide-react";
import {
  adhkarPresets,
  asset,
  calculatePrayerSchedule,
  exerciseCategories,
  formatArabicTime,
  getPageVerses,
  getSurahName,
  loadAppData,
  normalizeArabic,
  prayerMethods,
  toArabicDigits,
  weekdays,
  type PrayerMethod,
} from "./data";
import type {
  AppData,
  HisnChapter,
  HisnItem,
  NavKey,
  PriceItem,
  PrayerSchedule,
  ScheduleItem,
  Surah,
} from "./types";

type Store = {
  userName: string;
  darkMode: boolean;
  arabicRtl: boolean;
  sectionNumber: number;
  currentPage: number;
  bookmarkedPages: number[];
  mushafNight: boolean;
  quranFontSize: number;
  prayerMethod: PrayerMethod;
  latitude: number;
  longitude: number;
  prayerOffsets: Record<string, number>;
  schedule: ScheduleItem[];
  prices: PriceItem[];
  studySubject: string;
  studyMinutes: number;
  studyRemaining: number;
  studyRunning: boolean;
  notificationsEnabled: boolean;
  syncAlertWithNotification: boolean;
  notifications: Record<string, boolean>;
  alertSounds: Record<string, boolean>;
  alarmTimes: Record<string, string>;
  tones: Record<string, string>;
  adhkarCounts: Record<string, number>;
  adhkarAlarmEnabled: boolean;
  sebhaCount: number;
  sebhaTarget: number;
  hajjProgress: Record<string, boolean>;
};

type StoreSetter = Dispatch<SetStateAction<Store>>;
type ToneKey = "general" | "prayer" | "adhkar" | "quran" | "hajj" | "exercise" | "study";

const defaultTones: Record<ToneKey, string> = {
  general: "study_tone",
  prayer: "adhan",
  adhkar: "adhkar_tone",
  quran: "study_tone",
  hajj: "adhkar_tone",
  exercise: "exercise_tone",
  study: "study_tone",
};

const toneOptions = [
  { value: "adhan", label: "الأذان" },
  { value: "adhkar_tone", label: "نغمة الأذكار" },
  { value: "exercise_tone", label: "نغمة التمارين" },
  { value: "study_tone", label: "موعد في جنة عدن" },
];

const navItems: {
  key: NavKey;
  label: string;
  icon: typeof Home;
}[] = [
  { key: "home", label: "الرئيسية", icon: Home },
  { key: "schedule", label: "الجدول", icon: CalendarDays },
  { key: "quran", label: "المصحف", icon: BookOpen },
  { key: "exercises", label: "التمارين", icon: Dumbbell },
  { key: "adhkar", label: "الأذكار", icon: Sparkles },
  { key: "hajj", label: "الحج والعمرة", icon: Compass },
  { key: "settings", label: "الإعدادات", icon: Settings },
];

const notificationLabels: Record<ToneKey, string> = {
  general: "الإشعارات العامة",
  prayer: "الصلاة والأذان",
  adhkar: "الأذكار والسبحة",
  quran: "ورد القرآن",
  hajj: "الحج والعمرة",
  exercise: "التمارين",
  study: "المذاكرة والجدول",
};

const defaultStore = (data: AppData): Store => ({
  userName: "طالب العلم",
  darkMode: false,
  arabicRtl: true,
  sectionNumber: 1,
  currentPage: 1,
  bookmarkedPages: [],
  mushafNight: false,
  quranFontSize: 24,
  prayerMethod: "EGYPTIAN",
  latitude: 30.0595,
  longitude: 31.3137,
  prayerOffsets: {},
  schedule: data.schedule,
  prices: data.prices,
  studySubject: data.schedule[0]?.title ?? "وقت المذاكرة",
  studyMinutes: data.schedule[0]?.timerMinutes ?? 90,
  studyRemaining: (data.schedule[0]?.timerMinutes ?? 90) * 60,
  studyRunning: false,
  notificationsEnabled: true,
  syncAlertWithNotification: true,
  notifications: {
    prayer: true,
    adhkar: true,
    quran: true,
    hajj: true,
    exercise: true,
    study: true,
  },
  alertSounds: {
    prayer: true,
    adhkar: true,
    quran: true,
    hajj: true,
    exercise: true,
    study: true,
  },
  alarmTimes: {
    adhkar: "07:00",
    quran: "21:00",
    hajj: "17:30",
    exercise: "16:45",
    study: "08:00",
  },
  tones: defaultTones,
  adhkarCounts: {},
  adhkarAlarmEnabled: true,
  sebhaCount: 0,
  sebhaTarget: 33,
  hajjProgress: {},
});

function readStore(data: AppData) {
  const initial = defaultStore(data);
  try {
    const saved = window.localStorage.getItem("manarat-al-talib");
    if (!saved) return { store: initial, warning: "" };
    const parsed = JSON.parse(saved) as Partial<Store>;
    return {
      store: {
        ...initial,
        ...parsed,
        notifications: { ...initial.notifications, ...parsed.notifications },
        alertSounds: { ...initial.alertSounds, ...parsed.alertSounds },
        alarmTimes: { ...initial.alarmTimes, ...parsed.alarmTimes },
        tones: { ...defaultTones, ...parsed.tones },
        prayerOffsets: { ...initial.prayerOffsets, ...parsed.prayerOffsets },
        adhkarCounts: { ...initial.adhkarCounts, ...parsed.adhkarCounts },
        hajjProgress: { ...initial.hajjProgress, ...parsed.hajjProgress },
        schedule: Array.isArray(parsed.schedule) ? parsed.schedule : data.schedule,
        prices: Array.isArray(parsed.prices) ? parsed.prices : data.prices,
        bookmarkedPages: Array.isArray(parsed.bookmarkedPages)
          ? parsed.bookmarkedPages.filter((page) => page >= 1 && page <= 604)
          : [],
      },
      warning: "",
    };
  } catch (error) {
    console.error("Could not read saved preferences", error);
    return {
      store: initial,
      warning: "تعذر قراءة الإعدادات المحفوظة؛ تم تحميل الإعدادات الافتراضية.",
    };
  }
}

function App() {
  const [data, setData] = useState<AppData | null>(null);
  const [loadError, setLoadError] = useState("");

  useEffect(() => {
    let active = true;
    loadAppData()
      .then((loaded) => {
        if (active) setData(loaded);
      })
      .catch((error: unknown) => {
        console.error("App data failed to load", error);
        if (active) {
          setLoadError(
            error instanceof Error ? error.message : "تعذر تحميل بيانات التطبيق.",
          );
        }
      });
    return () => {
      active = false;
    };
  }, []);

  if (loadError) {
    return (
      <div className="boot-screen" dir="rtl">
        <div className="boot-card">
          <div className="brand-mark"><BookOpenCheck size={26} /></div>
          <h1>منارة الطالب</h1>
          <p>تعذر تشغيل البيانات المحلية. أعد تحميل التطبيق، وإذا استمرت المشكلة فتحقق من ملفات data.</p>
          <code>{loadError}</code>
          <button className="button button-primary" onClick={() => window.location.reload()}>إعادة المحاولة</button>
        </div>
      </div>
    );
  }

  if (!data) {
    return (
      <div className="boot-screen" dir="rtl">
        <div className="boot-card">
          <div className="brand-mark"><BookOpenCheck size={26} /></div>
          <h1>منارة الطالب</h1>
          <p>جاري تجهيز المصحف والأذكار والبيانات دون اتصال…</p>
          <div className="loading-bar"><span /></div>
        </div>
      </div>
    );
  }

  return <StudentApp data={data} />;
}

function StudentApp({ data }: { data: AppData }) {
  const boot = useMemo(() => readStore(data), [data]);
  const [store, setStore] = useState(boot.store);
  const [page, setPage] = useState<NavKey>("home");
  const [status, setStatus] = useState(boot.warning);
  const [now, setNow] = useState(() => new Date());
  const [showPrayerDetails, setShowPrayerDetails] = useState(false);
  const [customTones, setCustomTones] = useState<Partial<Record<ToneKey, string>>>({});
  const audioRef = useRef<HTMLAudioElement | null>(null);
  const lastAlarmRef = useRef("");

  const prayer = useMemo(
    () =>
      calculatePrayerSchedule(
        store.latitude,
        store.longitude,
        store.prayerMethod,
        store.prayerOffsets,
        now,
      ),
    [now, store.latitude, store.longitude, store.prayerMethod, store.prayerOffsets],
  );

  useEffect(() => {
    document.documentElement.dir = store.arabicRtl ? "rtl" : "ltr";
    document.documentElement.lang = store.arabicRtl ? "ar" : "en";
    document.documentElement.dataset.theme = store.darkMode ? "dark" : "light";
  }, [store.arabicRtl, store.darkMode]);

  useEffect(() => {
    try {
      window.localStorage.setItem("manarat-al-talib", JSON.stringify(store));
    } catch (error) {
      console.error("Could not persist preferences", error);
      setStatus("تعذر حفظ بعض التغييرات محلياً؛ تحقق من مساحة التخزين.");
    }
  }, [store]);

  useEffect(() => {
    const interval = window.setInterval(() => setNow(new Date()), 1000);
    return () => window.clearInterval(interval);
  }, []);

  const stopSound = () => {
    audioRef.current?.pause();
    audioRef.current = null;
  };

  const playTone = (key: ToneKey, preview = false) => {
    if (!preview && (!store.notificationsEnabled || !store.alertSounds[key])) return;
    const audio = new Audio(
      customTones[key] ?? asset(`data/audio/${store.tones[key] ?? defaultTones[key]}.mp3`),
    );
    stopSound();
    audioRef.current = audio;
    void audio.play().then(
      () => setStatus(`يتم تشغيل نغمة ${notificationLabels[key]}.`),
      (error: unknown) => {
        console.error("Audio playback failed", error);
        setStatus("تعذر تشغيل الصوت؛ قد يمنع النظام التشغيل أو لا يتوفر ملف النغمة.");
      },
    );
  };

  useEffect(() => {
    if (!store.studyRunning) return;
    const interval = window.setInterval(() => {
      setStore((current) => {
        if (current.studyRemaining <= 1) {
          return { ...current, studyRemaining: 0, studyRunning: false };
        }
        return { ...current, studyRemaining: current.studyRemaining - 1 };
      });
    }, 1000);
    return () => window.clearInterval(interval);
  }, [setStore, store.studyRunning]);

  useEffect(() => {
    if (store.studyRunning || store.studyRemaining !== 0) return;
    if (store.alertSounds.study && store.notificationsEnabled) playTone("study");
    if ("Notification" in window && Notification.permission === "granted") {
      new Notification("منارة الطالب", { body: `انتهى وقت مذاكرة ${store.studySubject}.` });
    }
    setStatus(`انتهى وقت مذاكرة ${store.studySubject}. أحسنت!`);
  }, [store.studyRemaining, store.studyRunning]);

  useEffect(() => {
    if (!store.notificationsEnabled) return;
    const minute = `${now.getFullYear()}-${now.getMonth()}-${now.getDate()}-${now.getHours()}:${now.getMinutes()}`;
    if (now.getSeconds() > 5 || lastAlarmRef.current === minute) return;
    const prayerDue = prayer.entries.find(
      (entry) => entry.key !== "SUNRISE" && entry.time24 ===
        `${String(now.getHours()).padStart(2, "0")}:${String(now.getMinutes()).padStart(2, "0")}`,
    );
    const fixedAlarm = Object.entries(store.alarmTimes).find(
      ([key, time]) =>
        store.notifications[key] &&
        time === `${String(now.getHours()).padStart(2, "0")}:${String(now.getMinutes()).padStart(2, "0")}`,
    );
    const key = prayerDue ? "prayer" : fixedAlarm?.[0];
    if (!key || !store.notifications[key]) return;
    lastAlarmRef.current = minute;
    if (store.alertSounds[key] && store.syncAlertWithNotification) playTone(key as ToneKey);
    if ("Notification" in window && Notification.permission === "granted") {
      new Notification("منارة الطالب", {
        body: key === "prayer" ? `حان وقت صلاة ${prayerDue?.name}` : `حان موعد ${notificationLabels[key as ToneKey]}.`,
      });
    }
  }, [now, prayer.entries, store]);

  const saveToneFile = (key: ToneKey, file?: File) => {
    if (!file) return;
    const url = URL.createObjectURL(file);
    setCustomTones((current) => {
      const old = current[key];
      if (old) URL.revokeObjectURL(old);
      return { ...current, [key]: url };
    });
    setStatus(`تم تعيين «${file.name}» كنغمة مخصصة لـ${notificationLabels[key]}.`);
  };

  const toggleBookmark = (target: number) => {
    setStore((current) => ({
      ...current,
      bookmarkedPages: current.bookmarkedPages.includes(target)
        ? current.bookmarkedPages.filter((pageNumber) => pageNumber !== target)
        : [...current.bookmarkedPages, target].sort((a, b) => a - b),
    }));
  };

  const startStudy = (subject: string, minutes: number) => {
    setStore((current) => ({
      ...current,
      studySubject: subject,
      studyMinutes: minutes,
      studyRemaining: minutes * 60,
      studyRunning: true,
    }));
    setPage("home");
  };

  return (
    <div className={`app-shell ${store.darkMode ? "is-dark" : ""}`}>
      <aside className="sidebar">
        <div className="sidebar-brand">
          <img src={asset("data/logo.png")} alt="" className="brand-logo" />
          <div>
            <strong>منارة الطالب</strong>
            <span>رفيق يومك وعبادتك</span>
          </div>
        </div>
        <div className="sidebar-caption">القائمة الرئيسية</div>
        <nav className="side-nav" aria-label="القائمة الرئيسية">
          {navItems.map(({ key, label, icon: Icon }) => (
            <button
              key={key}
              className={`nav-link ${page === key ? "active" : ""}`}
              onClick={() => setPage(key)}
              aria-current={page === key ? "page" : undefined}
            >
              <Icon size={19} strokeWidth={2} />
              <span>{label}</span>
              {key === "quran" && store.bookmarkedPages.length > 0 && (
                <small className="nav-badge">{store.bookmarkedPages.length}</small>
              )}
            </button>
          ))}
        </nav>
        <div className="sidebar-bottom">
          <div className="offline-indicator"><span /> يعمل دون اتصال</div>
          <span className="sidebar-version">الإصدار 1.0</span>
        </div>
      </aside>

      <div className="workspace">
        <header className="topbar">
          <button className="mobile-brand" onClick={() => setPage("home")} aria-label="الرئيسية">
            <img src={asset("data/logo.png")} alt="" />
            <strong>منارة الطالب</strong>
          </button>
          <div className="topbar-greeting">
            <span>{weekdays[now.getDay()]} • {now.toLocaleDateString("ar-EG", { day: "numeric", month: "long" })}</span>
            <strong>{navItems.find((item) => item.key === page)?.label}</strong>
          </div>
          <div className="topbar-actions">
            <div className="topbar-clock" title="الوقت المحلي">
              <Clock3 size={16} />
              <span>{now.toLocaleTimeString("ar-EG", { hour: "2-digit", minute: "2-digit" })}</span>
            </div>
            <button
              className="icon-button theme-quick-toggle"
              onClick={() => setStore((current) => ({ ...current, darkMode: !current.darkMode }))}
              aria-label={store.darkMode ? "تفعيل الوضع الفاتح" : "تفعيل الوضع الداكن"}
            >
              {store.darkMode ? <Sun size={18} /> : <Moon size={18} />}
            </button>
            <div className="user-avatar" title={store.userName}>{store.userName.slice(0, 1)}</div>
          </div>
        </header>

        <main className="main-content">
          {page === "home" && (
            <HomeScreen
              data={data}
              store={store}
              setStore={setStore}
              prayer={prayer}
              now={now}
              onNavigate={setPage}
              onOpenPrayer={() => setShowPrayerDetails(true)}
            />
          )}
          {page === "schedule" && (
            <ScheduleScreen data={data} store={store} setStore={setStore} startStudy={startStudy} />
          )}
          {page === "quran" && (
            <QuranScreen
              data={data}
              store={store}
              setStore={setStore}
              toggleBookmark={toggleBookmark}
            />
          )}
          {page === "exercises" && (
            <ExercisesScreen
              data={data}
              store={store}
              setStore={setStore}
              playTone={() => playTone("exercise", true)}
            />
          )}
          {page === "adhkar" && (
            <AdhkarScreen
              data={data}
              store={store}
              setStore={setStore}
              playTone={() => playTone("adhkar", true)}
            />
          )}
          {page === "hajj" && (
            <HajjScreen
              store={store}
              setStore={setStore}
              playTone={() => playTone("hajj", true)}
            />
          )}
          {page === "settings" && (
            <SettingsScreen
              data={data}
              store={store}
              setStore={setStore}
              setStatus={setStatus}
              playTone={playTone}
              stopSound={stopSound}
              saveToneFile={saveToneFile}
            />
          )}
        </main>
      </div>

      <nav className="mobile-nav" aria-label="التنقل">
        {navItems.filter((item) => item.key !== "hajj").map(({ key, label, icon: Icon }) => (
          <button
            key={key}
            className={page === key ? "active" : ""}
            onClick={() => setPage(key)}
            aria-label={label}
          >
            <Icon size={20} />
            <span>{label}</span>
          </button>
        ))}
        <button className={page === "hajj" ? "active" : ""} onClick={() => setPage("hajj")} aria-label="الحج والعمرة">
          <Compass size={20} />
          <span>المناسك</span>
        </button>
      </nav>

      {status && (
        <div className="status-toast" role="status">
          <span>{status}</span>
          <button onClick={() => setStatus("")} aria-label="إغلاق"><X size={15} /></button>
        </div>
      )}

      {showPrayerDetails && (
        <PrayerDialog
          prayer={prayer}
          store={store}
          setStore={setStore}
          now={now}
          onClose={() => setShowPrayerDetails(false)}
          onTestAdhan={() => playTone("prayer", true)}
        />
      )}
    </div>
  );
}

function PageHeading({
  icon: Icon,
  title,
  subtitle,
  action,
}: {
  icon: typeof Home;
  title: string;
  subtitle: string;
  action?: ReactNode;
}) {
  return (
    <div className="page-heading">
      <div className="page-title-icon"><Icon size={24} /></div>
      <div className="page-heading-text">
        <h1>{title}</h1>
        <p>{subtitle}</p>
      </div>
      {action && <div className="page-heading-action">{action}</div>}
    </div>
  );
}

function PrayerCountdown({
  prayer,
  now,
  onOpen,
}: {
  prayer: PrayerSchedule;
  now: Date;
  onOpen: () => void;
}) {
  const seconds = Math.max(0, prayer.remainingSeconds);
  const countdown = `${String(Math.floor(seconds / 3600)).padStart(2, "0")}:${String(Math.floor((seconds % 3600) / 60)).padStart(2, "0")}:${String(seconds % 60).padStart(2, "0")}`;
  const progress = Math.max(
    0,
    Math.min(100, 100 * (1 - seconds / Math.max(1, prayer.intervalSeconds))),
  );
  return (
    <button className="prayer-countdown-card" onClick={onOpen}>
      <div className="prayer-countdown-main">
        <div className="prayer-mint-icon"><Sun size={21} /></div>
        <div className="prayer-text">
          <span>الصلاة القادمة</span>
          <strong>صلاة {prayer.next.name}</strong>
          <small>{formatArabicTime(prayer.next.time24)} • {now.toLocaleDateString("ar-EG", { weekday: "long" })}</small>
        </div>
        <div className="prayer-time-left">
          <span>متبقي</span>
          <strong dir="ltr">{countdown}</strong>
        </div>
        <ChevronLeft size={18} className="prayer-arrow" />
      </div>
      <div className="progress-track"><span style={{ width: `${progress}%` }} /></div>
    </button>
  );
}

function HomeScreen({
  data,
  store,
  setStore,
  prayer,
  now,
  onNavigate,
  onOpenPrayer,
}: {
  data: AppData;
  store: Store;
  setStore: StoreSetter;
  prayer: PrayerSchedule;
  now: Date;
  onNavigate: (key: NavKey) => void;
  onOpenPrayer: () => void;
}) {
  const today = weekdays[now.getDay()];
  const todayItems = useMemo(
    () =>
      store.schedule
        .filter(
          (item) =>
            (item.sectionFilter === 0 || item.sectionFilter === store.sectionNumber) &&
            (item.dayOfWeek === today || item.dayOfWeek === "يومي"),
        )
        .sort((a, b) => a.startTime.localeCompare(b.startTime)),
    [store.schedule, store.sectionNumber, today],
  );
  const nextLesson = todayItems[0] ?? store.schedule.find((item) => item.title === store.studySubject);
  const percent = store.studyMinutes
    ? Math.max(0, Math.min(100, (1 - store.studyRemaining / (store.studyMinutes * 60)) * 100))
    : 0;
  const remainingLabel = `${String(Math.floor(store.studyRemaining / 3600)).padStart(2, "0")}:${String(Math.floor((store.studyRemaining % 3600) / 60)).padStart(2, "0")}:${String(store.studyRemaining % 60).padStart(2, "0")}`;
  const selectedSubject = store.studySubject || nextLesson?.title || "وقت المذاكرة";

  return (
    <div className="screen-stack">
      <section className="welcome-hero">
        <div className="welcome-copy">
          <span className="eyebrow"><Sparkles size={14} /> يوم مبارك وخطوات موفقة</span>
          <h1>مرحباً، {store.userName || "طالب العلم"} <span>✦</span></h1>
          <p>اجعل يومك منظماً، ووقتَك عامراً بالعلم والذكر.</p>
          <div className="welcome-pills">
            <span>السكشن {store.sectionNumber}</span>
            <span>{today}</span>
            <span>{data.quran.length.toLocaleString("ar-EG")} آية دون اتصال</span>
          </div>
        </div>
        <div className="welcome-decoration" aria-hidden="true">
          <div className="welcome-orbit"><BookOpenCheck size={42} /></div>
          <span className="sparkle s1">✦</span><span className="sparkle s2">✧</span>
        </div>
      </section>

      <PrayerCountdown prayer={prayer} now={now} onOpen={onOpenPrayer} />

      <div className="home-grid">
        <section className="panel study-panel">
          <div className="panel-heading">
            <div>
              <span className="section-kicker">التركيز اليوم</span>
              <h2>جلسة المذاكرة</h2>
            </div>
            <div className="panel-icon mint"><Timer size={20} /></div>
          </div>
          <div className="study-subject-row">
            <div>
              <small>المادة الحالية</small>
              <strong>{selectedSubject}</strong>
              <span>{nextLesson ? `${todayItems.length} محاضرة اليوم • ${nextLesson.location}` : "حدّد مادة من جدولك لبدء جلسة"}</span>
            </div>
            <button
              className="small-round-action"
              title="تبديل المادة"
              onClick={() => onNavigate("schedule")}
            >
              <ArrowLeftRight size={17} />
            </button>
          </div>
          <div className="timer-display-row">
            <div>
              <span className="timer-label">{store.studyRunning ? "الوقت المتبقي" : "مدة الجلسة"}</span>
              <strong className="timer-value" dir="ltr">{store.studyRunning ? remainingLabel : `${store.studyMinutes} دقيقة`}</strong>
            </div>
            <div className="timer-actions">
              <button
                className="icon-button soft"
                aria-label="تقليل ١٥ دقيقة"
                onClick={() => setStore((current) => {
                  const minutes = Math.max(15, current.studyMinutes - 15);
                  return { ...current, studyMinutes: minutes, studyRemaining: minutes * 60 };
                })}
                disabled={store.studyRunning}
              ><Minus size={16} /></button>
              <button
                className="icon-button soft"
                aria-label="زيادة ١٥ دقيقة"
                onClick={() => setStore((current) => {
                  const minutes = Math.min(180, current.studyMinutes + 15);
                  return { ...current, studyMinutes: minutes, studyRemaining: minutes * 60 };
                })}
                disabled={store.studyRunning}
              ><Plus size={16} /></button>
            </div>
          </div>
          <div className="progress-track"><span style={{ width: `${percent}%` }} /></div>
          <div className="study-buttons">
            {!store.studyRunning ? (
              <button
                className="button button-primary"
                onClick={() => setStore((current) => ({
                  ...current,
                  studyRemaining: current.studyRemaining > 0 ? current.studyRemaining : current.studyMinutes * 60,
                  studyRunning: true,
                }))}
              ><Play size={16} fill="currentColor" /> ابدأ المذاكرة</button>
            ) : (
              <button className="button button-primary" onClick={() => setStore((current) => ({ ...current, studyRunning: false }))}>
                <Pause size={16} fill="currentColor" /> إيقاف مؤقت
              </button>
            )}
            <button
              className="button button-outline"
              onClick={() => setStore((current) => ({ ...current, studyRunning: false, studyRemaining: current.studyMinutes * 60 }))}
            ><RotateCcw size={15} /> إعادة</button>
          </div>
        </section>

        <section className="panel today-panel">
          <div className="panel-heading">
            <div>
              <span className="section-kicker">نظرة سريعة</span>
              <h2>جدول اليوم</h2>
            </div>
            <button className="text-action" onClick={() => onNavigate("schedule")}>عرض الجدول <ChevronLeft size={15} /></button>
          </div>
          {todayItems.length ? (
            <div className="today-list">
              {todayItems.slice(0, 4).map((item) => (
                <div className="today-row" key={item.id}>
                  <button className="today-main" onClick={() => onNavigate("schedule")}>
                    <span className="today-time">{formatArabicTime(item.startTime)}</span>
                    <span className="timeline-dot" />
                    <span className="today-class">
                      <strong>{item.title}</strong>
                      <small>{item.location} • {formatArabicTime(item.endTime)}</small>
                    </span>
                  </button>
                  <button
                    className="mini-play"
                    aria-label={`ابدأ مذاكرة ${item.title}`}
                    onClick={() => setStore((current) => ({
                      ...current,
                      studySubject: item.title,
                      studyMinutes: item.timerMinutes || 90,
                      studyRemaining: (item.timerMinutes || 90) * 60,
                      studyRunning: true,
                    }))}
                  ><Play size={13} fill="currentColor" /></button>
                </div>
              ))}
            </div>
          ) : (
            <div className="empty-state compact-empty">
              <CalendarDays size={28} />
              <span>لا توجد محاضرات لهذا اليوم.</span>
              <button className="text-action" onClick={() => onNavigate("schedule")}>أضف موعداً <Plus size={14} /></button>
            </div>
          )}
        </section>
      </div>

      <section className="panel quick-panel">
        <div className="panel-heading">
          <div>
            <span className="section-kicker">أدواتك اليومية</span>
            <h2>وصول سريع</h2>
          </div>
        </div>
        <div className="quick-grid">
          <button className="quick-card quran-quick" onClick={() => onNavigate("quran")}>
            <span className="quick-icon"><BookOpen size={22} /></span>
            <strong>المصحف الشريف</strong>
            <small>آخر قراءة: صفحة {toArabicDigits(store.currentPage)} • ٦٠٤ صفحة</small>
            <span className="quick-arrow"><ChevronLeft size={16} /></span>
          </button>
          <button className="quick-card adhkar-quick" onClick={() => onNavigate("adhkar")}>
            <span className="quick-icon"><Sparkles size={22} /></span>
            <strong>الأذكار والسبحة</strong>
            <small>حصن المسلم • أذكار اليوم</small>
            <span className="quick-arrow"><ChevronLeft size={16} /></span>
          </button>
          <button className="quick-card hajj-quick" onClick={() => onNavigate("hajj")}>
            <span className="quick-icon"><Compass size={22} /></span>
            <strong>الحج والعمرة</strong>
            <small>مناسك مرتبة وقائمة متابعة</small>
            <span className="quick-arrow"><ChevronLeft size={16} /></span>
          </button>
          <button className="quick-card exercise-quick" onClick={() => onNavigate("exercises")}>
            <span className="quick-icon"><Dumbbell size={22} /></span>
            <strong>التمارين</strong>
            <small>{data.exercises.length} تمريناً بخطوات واضحة</small>
            <span className="quick-arrow"><ChevronLeft size={16} /></span>
          </button>
        </div>
      </section>
    </div>
  );
}

const emptyScheduleForm = (): Omit<ScheduleItem, "id"> => ({
  title: "",
  dayOfWeek: weekdays[new Date().getDay()],
  startTime: "08:00",
  endTime: "10:00",
  location: "",
  sectionFilter: 0,
  itemType: "LECTURE",
  recurrence: "WEEKLY",
  timerMinutes: 90,
});

function ScheduleScreen({
  data,
  store,
  setStore,
  startStudy,
}: {
  data: AppData;
  store: Store;
  setStore: StoreSetter;
  startStudy: (subject: string, minutes: number) => void;
}) {
  const [selectedDay, setSelectedDay] = useState("الكل");
  const [query, setQuery] = useState("");
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editId, setEditId] = useState<string | null>(null);
  const [form, setForm] = useState(emptyScheduleForm);
  const entries = useMemo(
    () =>
      store.schedule
        .filter((item) => item.sectionFilter === 0 || item.sectionFilter === store.sectionNumber)
        .filter((item) => selectedDay === "الكل" || item.dayOfWeek === selectedDay)
        .filter((item) => !query || item.title.toLowerCase().includes(query.toLowerCase()) || item.location.includes(query))
        .sort((a, b) => a.dayOfWeek.localeCompare(b.dayOfWeek) || a.startTime.localeCompare(b.startTime)),
    [query, selectedDay, store.schedule, store.sectionNumber],
  );

  const openNew = () => {
    setEditId(null);
    setForm(emptyScheduleForm());
    setDialogOpen(true);
  };

  const openEdit = (item: ScheduleItem) => {
    const { id: _id, ...values } = item;
    setEditId(item.id);
    setForm(values);
    setDialogOpen(true);
  };

  const saveForm = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const title = form.title.trim();
    if (!title) return;
    setStore((current) => {
      const nextItem: ScheduleItem = {
        ...form,
        title,
        id: editId ?? (globalThis.crypto?.randomUUID?.() ?? `lesson-${Date.now()}`),
      };
      return {
        ...current,
        schedule: editId
          ? current.schedule.map((item) => (item.id === editId ? nextItem : item))
          : [...current.schedule, nextItem],
      };
    });
    setDialogOpen(false);
  };

  return (
    <div className="screen-stack">
      <PageHeading
        icon={CalendarDays}
        title="الجدول الدراسي"
        subtitle="نظّم محاضراتك ومواعيد المذاكرة حسب اليوم والسكشن."
        action={<button className="button button-primary" onClick={openNew}><Plus size={17} /> إضافة موعد</button>}
      />
      <section className="schedule-banner">
        <div>
          <span className="eyebrow"><CalendarDays size={14} /> تنظيم الدراسة</span>
          <h2>خطوة صغيرة كل يوم تصنع فرقاً كبيراً.</h2>
          <p>اختر سكشنك، رتّب محاضراتك، وابدأ جلسة تركيز من أي موعد.</p>
        </div>
        <div className="section-selector" aria-label="اختيار السكشن">
          <span>السكشن الدراسي</span>
          {[1, 2].map((section) => (
            <button
              key={section}
              className={store.sectionNumber === section ? "selected" : ""}
              onClick={() => setStore((current) => ({ ...current, sectionNumber: section }))}
            >{section}</button>
          ))}
        </div>
      </section>
      <section className="panel schedule-panel">
        <div className="schedule-toolbar">
          <div className="day-chips">
            {["الكل", ...weekdays].map((day) => (
              <button key={day} className={`chip ${selectedDay === day ? "active" : ""}`} onClick={() => setSelectedDay(day)}>
                {day}
              </button>
            ))}
          </div>
          <label className="search-field schedule-search">
            <Search size={16} />
            <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="ابحث عن مادة أو مكان…" />
          </label>
        </div>
        {entries.length ? (
          <div className="schedule-list">
            {entries.map((item) => (
              <article className="schedule-item" key={item.id}>
                <div className="schedule-time">
                  <strong>{formatArabicTime(item.startTime)}</strong>
                  <span>{formatArabicTime(item.endTime)}</span>
                </div>
                <span className="schedule-timeline" />
                <div className="schedule-info">
                  <div className="schedule-title-line">
                    <h3>{item.title}</h3>
                    <span className={`type-badge ${item.itemType === "LAB" ? "lab" : ""}`}>
                      {item.itemType === "LAB" ? "معمل" : "محاضرة"}
                    </span>
                  </div>
                  <p><MapPin size={14} /> {item.location || "لم يحدد المكان"} <span>•</span> {item.dayOfWeek}</p>
                  <small>مؤقت المذاكرة: {item.timerMinutes} دقيقة</small>
                </div>
                <div className="schedule-item-actions">
                  <button className="icon-button soft" title="ابدأ مذاكرة هذه المادة" onClick={() => startStudy(item.title, item.timerMinutes)}>
                    <Play size={16} fill="currentColor" />
                  </button>
                  <button className="icon-button soft" title="تعديل الموعد" onClick={() => openEdit(item)}>
                    <Type size={16} />
                  </button>
                  <button
                    className="icon-button soft danger"
                    title="حذف الموعد"
                    onClick={() => setStore((current) => ({ ...current, schedule: current.schedule.filter((entry) => entry.id !== item.id) }))}
                  ><X size={16} /></button>
                </div>
              </article>
            ))}
          </div>
        ) : (
          <div className="empty-state"><CalendarDays size={34} /><strong>لا توجد مواعيد مطابقة.</strong><button className="button button-outline" onClick={openNew}>أضف موعداً جديداً</button></div>
        )}
        <div className="schedule-footer">
          <span>{entries.length} موعد{entries.length === 1 ? "" : "اً"} • تظهر مواعيد السكشن {store.sectionNumber} والمواعيد المشتركة</span>
          <button
            className="text-action danger-text"
            onClick={() => {
              if (window.confirm("هل تريد حذف جميع مواعيد الجدول؟ لا يمكن التراجع عن ذلك.")) {
                setStore((current) => ({ ...current, schedule: [] }));
              }
            }}
          >مسح الجدول</button>
        </div>
      </section>
      <section className="panel restore-panel">
        <div><strong>تحتاج إلى استعادة الجدول الأصلي؟</strong><small>استرجع المواعيد الافتراضية الموجودة في التطبيق.</small></div>
        <button className="button button-outline" onClick={() => setStore((current) => ({ ...current, schedule: data.schedule }))}>
          <RotateCcw size={15} /> استعادة الجدول الافتراضي
        </button>
      </section>

      {dialogOpen && (
        <div className="modal-backdrop" onMouseDown={(event) => event.target === event.currentTarget && setDialogOpen(false)}>
          <form className="modal-card" onSubmit={saveForm}>
            <div className="modal-heading">
              <div><span className="section-kicker">الجدول الدراسي</span><h2>{editId ? "تعديل موعد" : "إضافة موعد"}</h2></div>
              <button type="button" className="icon-button" onClick={() => setDialogOpen(false)} aria-label="إغلاق"><X size={18} /></button>
            </div>
            <label className="field-label">اسم المادة<input autoFocus required value={form.title} onChange={(event) => setForm((current) => ({ ...current, title: event.target.value }))} placeholder="مثال: تحليل الخوارزميات" /></label>
            <div className="form-grid">
              <label className="field-label">اليوم<select value={form.dayOfWeek} onChange={(event) => setForm((current) => ({ ...current, dayOfWeek: event.target.value }))}>{weekdays.map((day) => <option key={day}>{day}</option>)}</select></label>
              <label className="field-label">السكشن<select value={form.sectionFilter} onChange={(event) => setForm((current) => ({ ...current, sectionFilter: Number(event.target.value) }))}><option value={0}>مشترك</option><option value={1}>سكشن ١</option><option value={2}>سكشن ٢</option></select></label>
              <label className="field-label">من<input type="time" required value={form.startTime} onChange={(event) => setForm((current) => ({ ...current, startTime: event.target.value }))} /></label>
              <label className="field-label">إلى<input type="time" required value={form.endTime} onChange={(event) => setForm((current) => ({ ...current, endTime: event.target.value }))} /></label>
              <label className="field-label">المكان<input value={form.location} onChange={(event) => setForm((current) => ({ ...current, location: event.target.value }))} placeholder="القاعة أو المعمل" /></label>
              <label className="field-label">مدة المذاكرة<input type="number" min={15} max={180} step={15} value={form.timerMinutes} onChange={(event) => setForm((current) => ({ ...current, timerMinutes: Number(event.target.value) }))} /></label>
            </div>
            <div className="modal-actions"><button type="button" className="button button-outline" onClick={() => setDialogOpen(false)}>إلغاء</button><button type="submit" className="button button-primary"><Check size={16} /> حفظ الموعد</button></div>
          </form>
        </div>
      )}
    </div>
  );
}

type QuranIndexTab = "surah" | "juz" | "hizb" | "page" | "search";

function QuranScreen({
  data,
  store,
  setStore,
  toggleBookmark,
}: {
  data: AppData;
  store: Store;
  setStore: StoreSetter;
  toggleBookmark: (page: number) => void;
}) {
  const [indexOpen, setIndexOpen] = useState(false);
  const [indexTab, setIndexTab] = useState<QuranIndexTab>("surah");
  const [indexQuery, setIndexQuery] = useState("");
  const [pageInput, setPageInput] = useState("");
  const [writingOpen, setWritingOpen] = useState(false);
  const [writingVerse, setWritingVerse] = useState("");
  const [writingText, setWritingText] = useState("");
  const currentPage = Math.max(1, Math.min(604, store.currentPage));
  const pageVerses = useMemo(() => getPageVerses(data.quran, currentPage), [currentPage, data.quran]);
  const isBookmarked = store.bookmarkedPages.includes(currentPage);
  const surahRows = useMemo(
    () => data.surahs.filter((surah) => !indexQuery || `${surah.number} ${surah.nameAr} ${surah.nameEn}`.toLowerCase().includes(indexQuery.toLowerCase())),
    [data.surahs, indexQuery],
  );
  const juzRows = useMemo(
    () =>
      [...new Set(data.quran.map((verse) => verse.j))].map((number) => {
        const opening = data.quran.find((verse) => verse.j === number);
        return { number, page: opening?.p ?? 1, surah: opening?.s ?? 1 };
      }).filter((row) => !indexQuery || `الجزء ${row.number} ${row.number}`.includes(indexQuery)),
    [data.quran, indexQuery],
  );
  const hizbRows = useMemo(
    () =>
      [...new Set(data.quran.map((verse) => verse.h))].map((number) => {
        const opening = data.quran.find((verse) => verse.h === number);
        return { number, page: opening?.p ?? 1, surah: opening?.s ?? 1 };
      }).filter((row) => !indexQuery || `الحزب ${row.number} ${row.number}`.includes(indexQuery)),
    [data.quran, indexQuery],
  );
  const searchResults = useMemo(() => {
    const query = normalizeArabic(indexQuery);
    if (query.length < 2) return [];
    return data.quran
      .filter((verse) => normalizeArabic(verse.t).includes(query))
      .slice(0, 100);
  }, [data.quran, indexQuery]);
  const groupedVerses = useMemo(() => {
    const groups: { surah: number; verses: typeof pageVerses }[] = [];
    for (const verse of pageVerses) {
      const current = groups[groups.length - 1];
      if (!current || current.surah !== verse.s) groups.push({ surah: verse.s, verses: [verse] });
      else current.verses.push(verse);
    }
    return groups;
  }, [pageVerses]);

  const goToPage = (page: number) => {
    const target = Math.max(1, Math.min(604, page));
    setStore((current) => ({ ...current, currentPage: target }));
    setIndexOpen(false);
  };

  const currentTarget =
    pageVerses.find((verse) => `${verse.s}:${verse.a}` === writingVerse) ?? pageVerses[0];
  const targetWords = (currentTarget?.t ?? "")
    .split(/\s+/)
    .map((word) => normalizeArabic(word).replace(/[^\p{L}]/gu, ""))
    .filter(Boolean);
  const userWords = writingText.trim().split(/\s+/).filter(Boolean);
  const matchedCount = userWords.reduce((count, word, index) => {
    const expected = targetWords[index] ?? "";
    const actual = normalizeArabic(word).replace(/[^\p{L}]/gu, "");
    return count + Number(Boolean(expected) && actual === expected);
  }, 0);
  const accuracy = targetWords.length
    ? Math.round((matchedCount / targetWords.length) * 100)
    : 0;

  return (
    <div className={`screen-stack quran-screen ${store.mushafNight ? "mushaf-night-mode" : ""}`}>
      <PageHeading
        icon={BookOpen}
        title="المصحف الشريف"
        subtitle="مصحف المدينة المنورة — النص القرآني كاملاً، ٦٠٤ صفحة."
        action={<span className="offline-pill"><span /> متاح دون اتصال</span>}
      />
      <section className="mushaf-toolbar">
        <div className="mushaf-toolbar-main">
          <button className="button button-outline" onClick={() => { setIndexOpen(true); setIndexTab("surah"); setIndexQuery(""); }}>
            <List size={17} /> الفهرس والبحث
          </button>
          <div className="mushaf-current">
            <strong>صفحة {toArabicDigits(currentPage)}</strong>
            <span>الجزء {toArabicDigits(pageVerses[0]?.j ?? 1)} • {getSurahName(data.surahs, pageVerses[0]?.s ?? 1)}</span>
          </div>
          <button
            className={`icon-button ${isBookmarked ? "bookmark-active" : ""}`}
            onClick={() => toggleBookmark(currentPage)}
            title={isBookmarked ? "إزالة العلامة المرجعية" : "إضافة علامة مرجعية"}
          >{isBookmarked ? <BookmarkCheck size={18} /> : <Bookmark size={18} />}</button>
          <button
            className="icon-button"
            onClick={() => setStore((current) => ({ ...current, mushafNight: !current.mushafNight }))}
            title={store.mushafNight ? "الوضع النهاري" : "وضع القراءة الليلي"}
          >{store.mushafNight ? <Sun size={18} /> : <Moon size={18} />}</button>
        </div>
        <div className="mushaf-toolbar-actions">
          <button className="button button-mint" onClick={() => { setWritingOpen(true); setWritingVerse(pageVerses[0] ? `${pageVerses[0].s}:${pageVerses[0].a}` : ""); setWritingText(""); }}>
            <Type size={16} /> تدريب الكتابة
          </button>
          <button className="icon-button" title="تصغير الخط" onClick={() => setStore((current) => ({ ...current, quranFontSize: Math.max(17, current.quranFontSize - 1) }))}><Minus size={16} /></button>
          <button className="icon-button" title="تكبير الخط" onClick={() => setStore((current) => ({ ...current, quranFontSize: Math.min(38, current.quranFontSize + 1) }))}><Plus size={16} /></button>
        </div>
      </section>

      <section className="mushaf-reader">
        <div className="mushaf-edge">
          <div className="mushaf-page">
            <div className="mushaf-page-heading">
              <span>الجزء {toArabicDigits(pageVerses[0]?.j ?? 1)}</span>
              <strong>بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ</strong>
              <span>{getSurahName(data.surahs, pageVerses[0]?.s ?? 1)}</span>
            </div>
            <div className="mushaf-page-content" dir="rtl">
              {groupedVerses.map((group) => (
                <div className="mushaf-surah-block" key={`${group.surah}-${group.verses[0]?.a}`}>
                  {(group.verses[0]?.a === 1 || currentPage === 1) && (
                    <div className="mushaf-surah-title">{getSurahName(data.surahs, group.surah)}</div>
                  )}
                  <p className="mushaf-verse-flow" style={{ fontSize: `clamp(20px, ${store.quranFontSize / 16}vw, ${store.quranFontSize}px)` }}>
                    {group.verses.map((verse) => (
                      <span key={`${verse.s}-${verse.a}`}>
                        {verse.t}{" "}
                        <span className="ayah-marker" title={`الآية ${verse.a}`}>{toArabicDigits(verse.a)}</span>{" "}
                      </span>
                    ))}
                  </p>
                </div>
              ))}
            </div>
            <div className="mushaf-page-footer">
              <span>{getSurahName(data.surahs, pageVerses[0]?.s ?? 1)}</span>
              <strong>{toArabicDigits(currentPage)}</strong>
              <span>حزب {toArabicDigits(pageVerses[0]?.h ?? 1)}</span>
            </div>
          </div>
        </div>
        <div className="mushaf-page-controls">
          <button className="button button-outline" onClick={() => goToPage(currentPage - 1)} disabled={currentPage === 1}>
            <ChevronRight size={17} /> الصفحة السابقة
          </button>
          <label className="page-number-input">
            <span>صفحة</span>
            <input
              type="number"
              min={1}
              max={604}
              value={currentPage}
              aria-label="رقم الصفحة"
              onChange={(event) => {
                const next = Number(event.target.value);
                if (next >= 1 && next <= 604) setStore((current) => ({ ...current, currentPage: next }));
              }}
            />
            <span>من ٦٠٤</span>
          </label>
          <button className="button button-outline" onClick={() => goToPage(currentPage + 1)} disabled={currentPage === 604}>
            الصفحة التالية <ChevronLeft size={17} />
          </button>
        </div>
        <div className="mushaf-note">
          <BookOpenCheck size={15} /> النص القرآني من بيانات التطبيق الأصلية، مع حفظ آخر صفحة وعلاماتك المرجعية على هذا الجهاز.
        </div>
      </section>

      {indexOpen && (
        <div className="modal-backdrop" onMouseDown={(event) => event.target === event.currentTarget && setIndexOpen(false)}>
          <div className="modal-card quran-index-modal">
            <div className="modal-heading">
              <div><span className="section-kicker">مصحف المدينة</span><h2>الوصول والبحث في المصحف</h2></div>
              <button className="icon-button" onClick={() => setIndexOpen(false)} aria-label="إغلاق"><X size={18} /></button>
            </div>
            <label className="search-field index-search">
              <Search size={17} />
              <input value={indexQuery} onChange={(event) => setIndexQuery(event.target.value)} placeholder="ابحث بالكلمات أو اسم السورة أو الرقم…" autoFocus />
            </label>
            <div className="index-tabs">
              {([
                ["surah", "السورة"],
                ["juz", "الجزء"],
                ["hizb", "الحزب"],
                ["page", "الصفحة"],
                ["search", "بحث بالكلمات"],
              ] as [QuranIndexTab, string][]).map(([key, label]) => (
                <button className={`chip ${indexTab === key ? "active" : ""}`} key={key} onClick={() => setIndexTab(key)}>{label}</button>
              ))}
            </div>
            <div className="index-content">
              {indexTab === "surah" && (
                <div className="index-list">
                  {surahRows.map((surah) => (
                    <button className="index-row" key={surah.number} onClick={() => goToPage(surah.startPage)}>
                      <span className="index-number">{toArabicDigits(surah.number)}</span>
                      <span><strong>{surah.nameAr}</strong><small>{surah.isMakki ? "مكية" : "مدنية"} • {toArabicDigits(surah.ayahCount)} آية</small></span>
                      <small>ص {toArabicDigits(surah.startPage)}</small>
                      <ChevronLeft size={15} />
                    </button>
                  ))}
                </div>
              )}
              {indexTab === "juz" && (
                <div className="index-grid">
                  {juzRows.map((row) => (
                    <button className="division-card" key={row.number} onClick={() => goToPage(row.page)}>
                      <span>الجزء {toArabicDigits(row.number)}</span>
                      <small>{getSurahName(data.surahs, row.surah)} • صفحة {toArabicDigits(row.page)}</small>
                    </button>
                  ))}
                </div>
              )}
              {indexTab === "hizb" && (
                <div className="index-grid">
                  {hizbRows.map((row) => (
                    <button className="division-card" key={row.number} onClick={() => goToPage(row.page)}>
                      <span>الحزب {toArabicDigits(row.number)}</span>
                      <small>{getSurahName(data.surahs, row.surah)} • صفحة {toArabicDigits(row.page)}</small>
                    </button>
                  ))}
                </div>
              )}
              {indexTab === "page" && (
                <div className="page-index-panel">
                  <form className="page-jump-form" onSubmit={(event) => { event.preventDefault(); const target = Number(pageInput); if (target >= 1 && target <= 604) goToPage(target); }}>
                    <input type="number" min={1} max={604} value={pageInput} onChange={(event) => setPageInput(event.target.value)} placeholder="رقم الصفحة (١ - ٦٠٤)" />
                    <button className="button button-primary" type="submit">انتقال</button>
                  </form>
                  <div className="bookmark-list">
                    <strong><Bookmark size={15} /> صفحاتك المحفوظة</strong>
                    {store.bookmarkedPages.length ? store.bookmarkedPages.map((pageNumber) => (
                      <button key={pageNumber} className="bookmark-chip" onClick={() => goToPage(pageNumber)}>صفحة {toArabicDigits(pageNumber)} <X size={13} onClick={(event) => { event.stopPropagation(); toggleBookmark(pageNumber); }} /></button>
                    )) : <small>لم تحفظ صفحات بعد. اضغط علامة الكتاب أثناء القراءة.</small>}
                  </div>
                  <div className="page-index-grid">
                    {Array.from({ length: 604 }, (_, index) => index + 1).map((pageNumber) => (
                      <button key={pageNumber} className={`${pageNumber === currentPage ? "current" : ""} ${store.bookmarkedPages.includes(pageNumber) ? "saved" : ""}`} onClick={() => goToPage(pageNumber)} aria-label={`صفحة ${pageNumber}`}>{toArabicDigits(pageNumber)}</button>
                    ))}
                  </div>
                </div>
              )}
              {indexTab === "search" && (
                <div className="verse-results">
                  {indexQuery.trim().length < 2 ? (
                    <div className="empty-state"><Search size={30} /><span>اكتب كلمتين على الأقل للبحث في نص القرآن كاملاً.</span></div>
                  ) : searchResults.length ? searchResults.map((verse) => (
                    <button className="verse-result" key={`${verse.s}-${verse.a}`} onClick={() => goToPage(verse.p)}>
                      <span className="verse-result-meta">{getSurahName(data.surahs, verse.s)} • الآية {toArabicDigits(verse.a)} • صفحة {toArabicDigits(verse.p)}</span>
                      <span>{verse.t}</span>
                    </button>
                  )) : <div className="empty-state"><Search size={30} /><span>لا توجد آيات مطابقة لهذه الكلمات.</span></div>}
                  {searchResults.length === 100 && <small className="result-cap">تظهر أول ١٠٠ نتيجة. أضف كلمات أكثر لتضييق البحث.</small>}
                </div>
              )}
            </div>
            <div className="modal-footer-note">تتضمن البيانات الأصلية ١١٤ سورة و٦٬٢٣٦ آية على ٦٠٤ صفحات.</div>
          </div>
        </div>
      )}

      {writingOpen && (
        <div className="modal-backdrop" onMouseDown={(event) => event.target === event.currentTarget && setWritingOpen(false)}>
          <div className="modal-card writing-modal">
            <div className="modal-heading">
              <div><span className="section-kicker">مساعد الحفظ</span><h2>تدريب كتابة الآيات</h2></div>
              <button className="icon-button" onClick={() => setWritingOpen(false)} aria-label="إغلاق"><X size={18} /></button>
            </div>
            <p className="writing-instruction">اختر آية من الصفحة، ثم اكتبها من حفظك. تتم مقارنة الكلمات مع تجاهل التشكيل.</p>
            <label className="field-label">الآية
              <select value={writingVerse} onChange={(event) => { setWritingVerse(event.target.value); setWritingText(""); }}>
                {pageVerses.map((verse) => <option key={`${verse.s}-${verse.a}`} value={`${verse.s}:${verse.a}`}>{getSurahName(data.surahs, verse.s)} — الآية {toArabicDigits(verse.a)}</option>)}
              </select>
            </label>
            <div className="writing-target" dir="rtl">{currentTarget?.t}</div>
            <label className="field-label">اكتب الآية<textarea rows={4} value={writingText} onChange={(event) => setWritingText(event.target.value)} placeholder="ابدأ الكتابة هنا…" dir="rtl" /></label>
            <div className="writing-feedback">
              <div className="progress-track"><span style={{ width: `${accuracy}%` }} /></div>
              <strong>{toArabicDigits(accuracy)}٪ تطابق</strong>
              <span>{toArabicDigits(matchedCount)} من {toArabicDigits(targetWords.length)} كلمة</span>
            </div>
            {writingText && (
              <div className="writing-word-review" dir="rtl">
                {userWords.map((word, index) => {
                  const expected = targetWords[index] ?? "";
                  const actual = normalizeArabic(word).replace(/[^\p{L}]/gu, "");
                  return <span className={expected && actual === expected ? "correct" : "incorrect"} key={`${index}-${word}`}>{word}</span>;
                })}
              </div>
            )}
            <div className="modal-actions"><button className="button button-outline" onClick={() => setWritingText("")}><RotateCcw size={15} /> مسح</button><button className="button button-primary" onClick={() => setWritingOpen(false)}><Check size={16} /> إنهاء التدريب</button></div>
          </div>
        </div>
      )}
    </div>
  );
}

function ExercisesScreen({
  data,
  store,
  setStore,
  playTone,
}: {
  data: AppData;
  store: Store;
  setStore: StoreSetter;
  playTone: () => void;
}) {
  const [category, setCategory] = useState("الكل");
  const [query, setQuery] = useState("");
  const [activeExercise, setActiveExercise] = useState<number | null>(null);
  const [exerciseSeconds, setExerciseSeconds] = useState(0);
  const [running, setRunning] = useState(false);
  const [completed, setCompleted] = useState<number[]>([]);
  const playToneRef = useRef(playTone);
  useEffect(() => {
    playToneRef.current = playTone;
  }, [playTone]);
  const visibleExercises = data.exercises.filter(
    (exercise) =>
      (category === "الكل" || exercise.categoryAr === category) &&
      (!query || `${exercise.nameAr} ${exercise.targetMusclesAr}`.includes(query)),
  );

  useEffect(() => {
    if (!running) return;
    const interval = window.setInterval(() => {
      setExerciseSeconds((current) => {
        if (current <= 1) {
          setRunning(false);
          playToneRef.current();
          return 0;
        }
        return current - 1;
      });
    }, 1000);
    return () => window.clearInterval(interval);
  }, [running]);

  const beginExercise = (id: number, seconds: number) => {
    setActiveExercise(id);
    setExerciseSeconds(seconds);
    setRunning(true);
  };

  return (
    <div className="screen-stack">
      <PageHeading icon={Dumbbell} title="التمارين" subtitle="تمارين منزلية مصنّفة مع إرشادات ومؤقت لكل مجموعة." action={<button className="icon-button" onClick={playTone} title="تجربة نغمة التمارين"><Volume2 size={19} /></button>} />
      <section className="exercise-hero">
        <div className="exercise-hero-icon"><Dumbbell size={27} /></div>
        <div><span className="eyebrow">حركة ونشاط</span><h2>نشاط بسيط ينعش يومك الدراسي.</h2><p>اختر تمريناً، راجع خطواته، واضبط وقت الراحة بمؤقت مدمج.</p></div>
        <div className="exercise-count"><strong>{toArabicDigits(data.exercises.length)}</strong><span>تمرين</span></div>
      </section>
      <section className="panel exercise-panel">
        <div className="exercise-toolbar">
          <div className="day-chips">{exerciseCategories.map((item) => <button key={item} className={`chip ${category === item ? "active" : ""}`} onClick={() => setCategory(item)}>{item}</button>)}</div>
          <label className="search-field"><Search size={16} /><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="ابحث عن تمرين…" /></label>
        </div>
        <div className="exercise-grid">
          {visibleExercises.map((exercise, index) => {
            const isActive = activeExercise === exercise.id;
            const isDone = completed.includes(exercise.id);
            return (
              <article className={`exercise-card ${isActive ? "exercise-active" : ""}`} key={exercise.id}>
                <div className="exercise-card-top">
                  <span className="exercise-number">{toArabicDigits(exercise.id.toString().padStart(2, "0"))}</span>
                  <span className="category-badge">{exercise.categoryAr}</span>
                  <button className={`complete-mark ${isDone ? "done" : ""}`} title={isDone ? "إلغاء الإكمال" : "تحديد كمكتمل"} onClick={() => setCompleted((current) => current.includes(exercise.id) ? current.filter((id) => id !== exercise.id) : [...current, exercise.id])}>
                    {isDone ? <CheckCheck size={17} /> : <Check size={17} />}
                  </button>
                </div>
                <h3>{exercise.nameAr}</h3>
                <p className="exercise-muscles">{exercise.targetMusclesAr}</p>
                <div className="exercise-metrics">
                  <span><RotateCcw size={14} /> {toArabicDigits(exercise.setsCount)} مجموعات</span>
                  <span><Timer size={14} /> {exercise.repsOrDurationAr}</span>
                </div>
                {isActive && (
                  <div className="exercise-timer">
                    <span>وقت المجموعة</span>
                    <strong dir="ltr">{String(Math.floor(exerciseSeconds / 60)).padStart(2, "0")}:{String(exerciseSeconds % 60).padStart(2, "0")}</strong>
                    <div>
                      <button className="icon-button soft" onClick={() => setRunning((current) => !current)} aria-label={running ? "إيقاف المؤقت" : "استئناف المؤقت"}>{running ? <Pause size={15} /> : <Play size={15} />}</button>
                      <button className="icon-button soft" onClick={() => { setRunning(false); setExerciseSeconds(0); setActiveExercise(null); }} aria-label="إيقاف التمرين"><X size={15} /></button>
                    </div>
                  </div>
                )}
                <details className="exercise-details">
                  <summary>خطوات الأداء <ChevronDown size={15} /></summary>
                  <ol>{exercise.stepsAr.map((step) => <li key={step}>{step}</li>)}</ol>
                </details>
                <button className={`button ${isDone ? "button-success" : "button-primary"} exercise-start`} onClick={() => beginExercise(exercise.id, exercise.defaultTimerSeconds)}>
                  <Play size={15} fill="currentColor" /> {isActive ? "إعادة المؤقت" : "ابدأ المؤقت"}
                  <span>{toArabicDigits(exercise.defaultTimerSeconds)} ث</span>
                </button>
              </article>
            );
          })}
          {!visibleExercises.length && <div className="empty-state"><Dumbbell size={32} /><span>لا توجد تمارين تطابق بحثك.</span></div>}
        </div>
        <div className="exercise-bottom-note"><Heart size={15} /> استمع لجسمك وتوقف عند الشعور بألم غير معتاد.</div>
      </section>
    </div>
  );
}

type AdhkarMode = "adhkar" | "sebha" | "pdf" | "prices";

const emptyPriceForm = (): Omit<PriceItem, "id"> => ({
  title: "",
  category: "كتب ومراجع جامعية",
  description: "",
  priceEgp: 0,
  unit: "قطعة",
  pdfPageNumber: 1,
});

function AdhkarScreen({
  data,
  store,
  setStore,
  playTone,
}: {
  data: AppData;
  store: Store;
  setStore: StoreSetter;
  playTone: () => void;
}) {
  const [mode, setMode] = useState<AdhkarMode>("adhkar");
  const [preset, setPreset] = useState("الكل");
  const [selectedChapterId, setSelectedChapterId] = useState(data.hisn[0]?.id ?? 1);
  const [query, setQuery] = useState("");
  const [phrase, setPhrase] = useState("سبحان الله وبحمده، سبحان الله العظيم");
  const [sebhaDone, setSebhaDone] = useState(false);
  const [priceCategory, setPriceCategory] = useState("الكل");
  const [priceQuery, setPriceQuery] = useState("");
  const [priceDialogOpen, setPriceDialogOpen] = useState(false);
  const [editPriceId, setEditPriceId] = useState<string | null>(null);
  const [priceForm, setPriceForm] = useState(emptyPriceForm);
  const playPhrases = [
    "سبحان الله وبحمده، سبحان الله العظيم",
    "أستغفر الله العظيم وأتوب إليه",
    "لا إله إلا الله وحده لا شريك له",
    "اللهم صلِّ وسلم على نبينا محمد",
    "لا حول ولا قوة إلا بالله العلي العظيم",
  ];

  const presetMatches = (chapter: HisnChapter) => {
    const text = `${chapter.title} ${chapter.category}`;
    if (preset === "أذكار الصباح") return /الصباح|صباح/.test(text);
    if (preset === "أذكار المساء") return /المساء|مساء/.test(text);
    if (preset === "أذكار السفر") return /السفر|المسافر|الركوب|المركوب/.test(text);
    if (preset === "أذكار النوم") return /النوم|ليلا|الفزع/.test(text) && !/الاستيقاظ/.test(text);
    if (preset === "أذكار الاستيقاظ") return /الاستيقاظ/.test(text);
    if (preset === "أذكار بعد الصلاة") return /الصلاة|السلام/.test(text) && /بعد/.test(text);
    return true;
  };
  const visibleChapters = useMemo(
    () => data.hisn.filter(presetMatches),
    [data.hisn, preset],
  );
  const selectedChapter =
    visibleChapters.find((chapter) => chapter.id === selectedChapterId) ?? visibleChapters[0];
  const visibleItems = useMemo(() => {
    if (query.trim()) {
      return data.hisn.flatMap((chapter) =>
        chapter.items
          .filter((item) => `${chapter.title} ${chapter.category} ${item.text}`.includes(query))
          .map((item) => ({ chapter, item })),
      ).slice(0, 100);
    }
    return selectedChapter?.items.map((item) => ({ chapter: selectedChapter, item })) ?? [];
  }, [data.hisn, query, selectedChapter]);

  const countFor = (chapter: HisnChapter, item: HisnItem) =>
    store.adhkarCounts[`${chapter.id}:${item.id}`] ?? 0;
  const incrementCount = (chapter: HisnChapter, item: HisnItem) => {
    const key = `${chapter.id}:${item.id}`;
    const currentCount = countFor(chapter, item);
    const nextCount = currentCount >= item.count ? 0 : currentCount + 1;
    setStore((current) => ({ ...current, adhkarCounts: { ...current.adhkarCounts, [key]: nextCount } }));
  };

  const incrementSebha = () => {
    setSebhaDone(false);
    setStore((current) => ({
      ...current,
      sebhaCount: current.sebhaCount >= current.sebhaTarget - 1 ? 0 : current.sebhaCount + 1,
    }));
    if (store.sebhaCount >= store.sebhaTarget - 1) {
      setSebhaDone(true);
      if ("vibrate" in navigator) navigator.vibrate?.(80);
    }
  };

  const completedItems = data.hisn.reduce(
    (total, chapter) =>
      total + chapter.items.filter((item) => countFor(chapter, item) >= item.count).length,
    0,
  );
  const totalItems = data.hisn.reduce((total, chapter) => total + chapter.items.length, 0);
  const priceCategories = ["الكل", ...new Set(store.prices.map((item) => item.category))];
  const visiblePrices = store.prices.filter(
    (item) =>
      (priceCategory === "الكل" || item.category === priceCategory) &&
      (!priceQuery || `${item.title} ${item.description} ${item.category} ${item.unit}`.toLowerCase().includes(priceQuery.toLowerCase())),
  );
  const openPriceForm = (item?: PriceItem) => {
    if (item) {
      const { id: _id, ...values } = item;
      setEditPriceId(item.id);
      setPriceForm(values);
    } else {
      setEditPriceId(null);
      setPriceForm(emptyPriceForm());
    }
    setPriceDialogOpen(true);
  };
  const savePrice = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!priceForm.title.trim()) return;
    setStore((current) => {
      const item: PriceItem = {
        ...priceForm,
        title: priceForm.title.trim(),
        id: editPriceId ?? (globalThis.crypto?.randomUUID?.() ?? `price-${Date.now()}`),
      };
      return {
        ...current,
        prices: editPriceId
          ? current.prices.map((value) => (value.id === editPriceId ? item : value))
          : [...current.prices, item],
      };
    });
    setPriceDialogOpen(false);
  };

  return (
    <div className="screen-stack">
      <PageHeading
        icon={Sparkles}
        title="الأذكار وحصن المسلم"
        subtitle="الأذكار الأصلية من التطبيق، مع عدّاد تفاعلي وسبحة إلكترونية."
        action={<button className="icon-button" onClick={playTone} title="تجربة نغمة الأذكار"><Volume2 size={19} /></button>}
      />
      <section className="panel adhkar-header-panel">
        <div className="adhkar-header-row">
          <div className="adhkar-header-icon"><Sparkles size={25} /></div>
          <div><h2>واذكر ربك كثيراً</h2><p>أذكارك اليومية في مكان واحد، محفوظة على جهازك.</p></div>
          <div className="adhkar-progress-summary"><strong>{toArabicDigits(completedItems)} / {toArabicDigits(totalItems)}</strong><span>ذكر أُنجز</span></div>
        </div>
        <div className="adhkar-mode-tabs">
          <button className={mode === "adhkar" ? "active" : ""} onClick={() => setMode("adhkar")}><BookOpen size={16} /> الأذكار التفاعلية</button>
          <button className={mode === "sebha" ? "active" : ""} onClick={() => setMode("sebha")}><Sparkles size={16} /> السبحة الإلكترونية</button>
          <button className={mode === "pdf" ? "active" : ""} onClick={() => setMode("pdf")}><FileText size={16} /> كتاب حصن المسلم PDF</button>
          <button className={mode === "prices" ? "active" : ""} onClick={() => setMode("prices")}><List size={16} /> الأسعار والمستلزمات</button>
        </div>
      </section>

      {mode === "adhkar" && (
        <>
          <section className="panel adhkar-controls">
            <div className="day-chips">
              {adhkarPresets.map((item) => (
                <button key={item} className={`chip ${preset === item ? "active" : ""}`} onClick={() => {
                  setPreset(item);
                  setQuery("");
                  const first = data.hisn.find((chapter) => {
                    if (item === "الكل") return true;
                    const value = `${chapter.title} ${chapter.category}`;
                    if (item === "أذكار الصباح") return /الصباح|صباح/.test(value);
                    if (item === "أذكار المساء") return /المساء|مساء/.test(value);
                    if (item === "أذكار السفر") return /السفر|المسافر|الركوب|المركوب/.test(value);
                    if (item === "أذكار النوم") return /النوم|ليلا|الفزع/.test(value) && !/الاستيقاظ/.test(value);
                    if (item === "أذكار الاستيقاظ") return /الاستيقاظ/.test(value);
                    return /الصلاة|السلام/.test(value) && /بعد/.test(value);
                  });
                  if (first) setSelectedChapterId(first.id);
                }}>{item}</button>
              ))}
            </div>
            <label className="search-field adhkar-search"><Search size={16} /><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="ابحث في الأذكار…" /></label>
            {!query && (
              <label className="field-label chapter-picker">اختر باباً
                <select value={selectedChapter?.id ?? ""} onChange={(event) => setSelectedChapterId(Number(event.target.value))}>
                  {visibleChapters.map((chapter) => <option value={chapter.id} key={chapter.id}>{chapter.title}</option>)}
                </select>
              </label>
            )}
          </section>
          {query && <div className="search-summary">نتائج البحث: {toArabicDigits(visibleItems.length)} ذكر{visibleItems.length === 100 ? " (أول ١٠٠ نتيجة)" : ""}</div>}
          <div className="adhkar-list">
            {visibleItems.map(({ chapter, item }) => {
              const count = countFor(chapter, item);
              const done = count >= item.count;
              return (
                <article className={`panel dhikr-card ${done ? "dhikr-done" : ""}`} key={`${chapter.id}:${item.id}`}>
                  <div className="dhikr-card-meta"><span>{chapter.title}</span><span>{toArabicDigits(item.count)} {item.count === 1 ? "مرة" : "مرات"}</span></div>
                  <p className="dhikr-text" dir="rtl">{item.text}</p>
                  <div className="dhikr-card-footer">
                    <span className="dhikr-reference">{chapter.category}</span>
                    <div className="dhikr-counter">
                      <button className="counter-reset" onClick={() => setStore((current) => ({ ...current, adhkarCounts: { ...current.adhkarCounts, [`${chapter.id}:${item.id}`]: 0 } }))} aria-label="إعادة العداد"><RotateCcw size={14} /></button>
                      <button className={`dhikr-count-button ${done ? "done" : ""}`} onClick={() => incrementCount(chapter, item)} aria-label={`العدد ${count} من ${item.count}`}>
                        <span>{done ? <CheckCheck size={18} /> : <Plus size={16} />}</span>
                        <strong>{toArabicDigits(Math.min(count, item.count))}</strong>
                        <small>/ {toArabicDigits(item.count)}</small>
                      </button>
                    </div>
                  </div>
                </article>
              );
            })}
            {!visibleItems.length && <div className="panel empty-state"><Sparkles size={30} /><span>لا توجد أذكار مطابقة لهذا البحث أو الاختيار.</span></div>}
          </div>
          <div className="hisn-pdf-link">
            <span><FileText size={18} /> يضم الكتاب المرفق كامل حصن المسلم بصيغة PDF.</span>
            <button className="button button-outline" onClick={() => setMode("pdf")}>فتح الكتاب</button>
          </div>
        </>
      )}

      {mode === "sebha" && (
        <section className="panel sebha-panel">
          <div className="sebha-title"><div className="panel-icon purple"><Sparkles size={21} /></div><div><h2>السبحة الإلكترونية</h2><p>اختر ذكراً واضغط على الدائرة للتسبيح.</p></div></div>
          <label className="field-label sebha-phrase-label">الذكر
            <select value={phrase} onChange={(event) => { setPhrase(event.target.value); setStore((current) => ({ ...current, sebhaCount: 0 })); }}>
              {playPhrases.map((value) => <option key={value}>{value}</option>)}
            </select>
          </label>
          <div className={`sebha-bead ${sebhaDone ? "sebha-complete" : ""}`}>
            <span>{phrase}</span>
            <strong>{toArabicDigits(store.sebhaCount)}</strong>
            <small>من {toArabicDigits(store.sebhaTarget)}</small>
            <button onClick={incrementSebha} aria-label="سبّح">{sebhaDone ? <Check size={22} /> : <Plus size={23} />}</button>
          </div>
          <div className="sebha-actions">
            <label className="field-label">هدف التسبيح
              <select value={store.sebhaTarget} onChange={(event) => setStore((current) => ({ ...current, sebhaTarget: Number(event.target.value), sebhaCount: 0 }))}>
                {[33, 100, 500, 1000].map((target) => <option value={target} key={target}>{toArabicDigits(target)} مرة</option>)}
              </select>
            </label>
            <button className="button button-outline" onClick={() => setStore((current) => ({ ...current, sebhaCount: 0 }))}><RotateCcw size={15} /> تصفير العداد</button>
          </div>
          {sebhaDone && <div className="success-note"><CheckCheck size={17} /> أحسنت، أتممت وردك. تقبل الله منك.</div>}
        </section>
      )}

      {mode === "pdf" && (
        <section className="panel pdf-panel">
          <div className="pdf-heading"><div><h2>حصن المسلم</h2><p>الكتاب المرفق مع التطبيق — يعمل دون اتصال.</p></div><a className="button button-outline" href={asset("data/hisn_almuslim.pdf")} download="hisn-al-muslim.pdf">تنزيل PDF <ArrowRight size={15} /></a></div>
          <iframe title="كتاب حصن المسلم" src={asset("data/hisn_almuslim.pdf")} />
        </section>
      )}

      {mode === "prices" && (
        <>
          <section className="panel price-toolbar">
            <div className="day-chips">{priceCategories.map((category) => <button className={`chip ${priceCategory === category ? "active" : ""}`} key={category} onClick={() => setPriceCategory(category)}>{category}</button>)}</div>
            <label className="search-field"><Search size={16} /><input value={priceQuery} onChange={(event) => setPriceQuery(event.target.value)} placeholder="ابحث عن صنف أو فئة…" /></label>
            <button className="button button-primary" onClick={() => openPriceForm()}><Plus size={16} /> إضافة صنف</button>
          </section>
          <div className="price-list">
            {visiblePrices.map((item) => (
              <article className="panel price-card" key={item.id}>
                <div className="price-card-main">
                  <span className="category-badge">{item.category}</span>
                  <h3>{item.title}</h3>
                  <p>{item.description}</p>
                  <small>{item.unit}</small>
                </div>
                <strong className="price-value">{item.priceEgp.toLocaleString("ar-EG")} <small>ج.م</small></strong>
                <div className="price-actions">
                  <button className="icon-button soft" onClick={() => openPriceForm(item)} aria-label="تعديل الصنف"><Type size={15} /></button>
                  <button className="icon-button soft danger" onClick={() => {
                    if (window.confirm(`حذف «${item.title}» من قائمة الأسعار؟`)) {
                      setStore((current) => ({ ...current, prices: current.prices.filter((entry) => entry.id !== item.id) }));
                    }
                  }} aria-label="حذف الصنف"><X size={15} /></button>
                </div>
              </article>
            ))}
            {!visiblePrices.length && <div className="panel empty-state"><List size={30} /><span>لا توجد أصناف مطابقة. أضف صنفاً جديداً لقائمتك.</span></div>}
          </div>
          <div className="price-data-note"><CircleHelp size={15} /> الأسعار المبدئية من بيانات المشروع الأصلي، وقد تتغير؛ راجع البائع قبل الشراء.</div>
          {priceDialogOpen && (
            <div className="modal-backdrop" onMouseDown={(event) => event.target === event.currentTarget && setPriceDialogOpen(false)}>
              <form className="modal-card" onSubmit={savePrice}>
                <div className="modal-heading"><div><span className="section-kicker">الأسعار والمستلزمات</span><h2>{editPriceId ? "تعديل صنف" : "إضافة صنف"}</h2></div><button type="button" className="icon-button" onClick={() => setPriceDialogOpen(false)} aria-label="إغلاق"><X size={18} /></button></div>
                <label className="field-label">اسم الصنف<input required value={priceForm.title} onChange={(event) => setPriceForm((current) => ({ ...current, title: event.target.value }))} /></label>
                <div className="form-grid">
                  <label className="field-label">الفئة<input value={priceForm.category} onChange={(event) => setPriceForm((current) => ({ ...current, category: event.target.value }))} /></label>
                  <label className="field-label">السعر بالجنيه<input type="number" min={0} step="0.5" value={priceForm.priceEgp} onChange={(event) => setPriceForm((current) => ({ ...current, priceEgp: Number(event.target.value) }))} /></label>
                  <label className="field-label">الوحدة<input value={priceForm.unit} onChange={(event) => setPriceForm((current) => ({ ...current, unit: event.target.value }))} /></label>
                </div>
                <label className="field-label">الوصف<textarea rows={3} value={priceForm.description} onChange={(event) => setPriceForm((current) => ({ ...current, description: event.target.value }))} /></label>
                <div className="modal-actions"><button type="button" className="button button-outline" onClick={() => setPriceDialogOpen(false)}>إلغاء</button><button type="submit" className="button button-primary"><Check size={16} /> حفظ الصنف</button></div>
              </form>
            </div>
          )}
        </>
      )}
    </div>
  );
}

const umrahSteps = [
  { id: "ihram", title: "الإحرام والنية", detail: "الاغتسال والتطيب قبل الإحرام، ثم عقد نية العمرة من الميقات ولبس لباس الإحرام." },
  { id: "talbiyah", title: "التلبية", detail: "الإكثار من التلبية: لبيك اللهم عمرة، لبيك اللهم لبيك، لبيك لا شريك لك لبيك." },
  { id: "tawaf", title: "الطواف بالبيت", detail: "الطواف حول الكعبة سبعة أشواط، مبتدئاً من الحجر الأسود ومنتهياً إليه." },
  { id: "tawaf-prayer", title: "ركعتا الطواف", detail: "صلاة ركعتين بعد الطواف إن تيسر، دون مزاحمة أو مشقة." },
  { id: "sai", title: "السعي بين الصفا والمروة", detail: "السعي سبعة أشواط، يبدأ بالصفا وينتهي بالمروة." },
  { id: "hair", title: "الحلق أو التقصير", detail: "الحلق أو التقصير للرجل، والتقصير من أطراف الشعر للمرأة. وبذلك تتم العمرة." },
];

const hajjSteps = [
  { id: "hajj-ihram", title: "الإحرام من الميقات", detail: "الإحرام بالحج أو التمتع من الميقات المحدد، مع النية والتلبية." },
  { id: "tarwiyah", title: "يوم التروية — ٨ ذي الحجة", detail: "التوجه إلى منى والمبيت بها، مع أداء الصلوات قصراً دون جمع." },
  { id: "arafah", title: "الوقوف بعرفة — ٩ ذي الحجة", detail: "الوقوف بعرفة إلى الغروب، والإكثار من الدعاء والذكر." },
  { id: "muzdalifah", title: "المبيت بمزدلفة", detail: "التوجه بعد الغروب إلى مزدلفة، والصلاة والمبيت فيها بحسب الاستطاعة." },
  { id: "mina", title: "أعمال يوم النحر — ١٠ ذي الحجة", detail: "رمي جمرة العقبة، والهدي للمتمتع والقارن، والحلق أو التقصير، وطواف الإفاضة والسعي بحسب النسك." },
  { id: "days-tashreeq", title: "أيام التشريق — ١١ إلى ١٣", detail: "المبيت بمنى ورمي الجمرات الثلاث في أيام التشريق وفق الترتيب والوقت الشرعي." },
  { id: "farewell", title: "طواف الوداع", detail: "طواف الوداع عند مغادرة مكة، ويُعفى منه الحائض والنفساء." },
];

function HajjScreen({
  store,
  setStore,
  playTone,
}: {
  store: Store;
  setStore: StoreSetter;
  playTone: () => void;
}) {
  const [ritual, setRitual] = useState<"umrah" | "hajj">("umrah");
  const steps = ritual === "umrah" ? umrahSteps : hajjSteps;
  const doneCount = steps.filter((step) => store.hajjProgress[`${ritual}:${step.id}`]).length;
  const percent = Math.round((doneCount / steps.length) * 100);
  const toggleStep = (id: string) => {
    const key = `${ritual}:${id}`;
    setStore((current) => ({
      ...current,
      hajjProgress: { ...current.hajjProgress, [key]: !current.hajjProgress[key] },
    }));
  };

  return (
    <div className="screen-stack">
      <PageHeading icon={Compass} title="الحج والعمرة" subtitle="دليل موجز للمناسك مع قائمة متابعة شخصية وتنبيه مستقل." action={<button className="icon-button" onClick={playTone} title="تجربة نغمة الحج والعمرة"><Volume2 size={19} /></button>} />
      <section className="hajj-hero">
        <div className="hajj-hero-symbol"><Compass size={39} /></div>
        <span className="eyebrow">رحلة إيمانية</span>
        <h2>خطوات مرتبة لمناسك الحج والعمرة</h2>
        <p>تابع استعدادك ومناسكك خطوة بخطوة، واحفظ تقدّمك على هذا الجهاز.</p>
        <div className="hajj-stat-row">
          <div><strong>{toArabicDigits(doneCount)} / {toArabicDigits(steps.length)}</strong><span>خطوات مكتملة</span></div>
          <div className="hajj-progress"><div className="progress-track"><span style={{ width: `${percent}%` }} /></div><small>{toArabicDigits(percent)}٪</small></div>
        </div>
      </section>
      <div className="hajj-tabs">
        <button className={ritual === "umrah" ? "active" : ""} onClick={() => setRitual("umrah")}><Footprints size={18} /> مناسك العمرة</button>
        <button className={ritual === "hajj" ? "active" : ""} onClick={() => setRitual("hajj")}><Compass size={18} /> مناسك الحج</button>
      </div>
      <section className="panel hajj-steps-panel">
        <div className="panel-heading">
          <div><span className="section-kicker">{ritual === "umrah" ? "العمرة" : "الحج"}</span><h2>{ritual === "umrah" ? "مسار العمرة" : "محطات الحج"}</h2></div>
          <button className="text-action" onClick={() => setStore((current) => ({ ...current, hajjProgress: Object.fromEntries(Object.entries(current.hajjProgress).filter(([key]) => !key.startsWith(`${ritual}:`))) }))}><RotateCcw size={14} /> إعادة القائمة</button>
        </div>
        <div className="ritual-timeline">
          {steps.map((step, index) => {
            const done = store.hajjProgress[`${ritual}:${step.id}`] ?? false;
            return (
              <article className={`ritual-step ${done ? "done" : ""}`} key={step.id}>
                <button className="ritual-check" onClick={() => toggleStep(step.id)} aria-label={done ? "إلغاء الإكمال" : "وضع علامة مكتمل"}>{done ? <Check size={17} /> : toArabicDigits(index + 1)}</button>
                <div className="ritual-step-content"><div className="ritual-step-title"><h3>{step.title}</h3>{done && <span>مكتمل</span>}</div><p>{step.detail}</p></div>
              </article>
            );
          })}
        </div>
        <div className="hajj-disclaimer"><CircleHelp size={17} /><span>هذا ملخص تذكيري لا يغني عن تعلّم الأحكام التفصيلية من جهة موثوقة، ولا سيما عند اختلاف نوع النسك أو وجود عذر.</span></div>
      </section>
      <section className="panel hajj-alarm-panel">
        <div className="panel-icon gold"><Clock3 size={19} /></div>
        <div className="hajj-alarm-copy"><strong>تنبيه الحج والعمرة</strong><small>موعد يومي مستقل للتذكير بالمناسك والأدعية.</small></div>
        <input aria-label="موعد تنبيه الحج والعمرة" type="time" value={store.alarmTimes.hajj} onChange={(event) => setStore((current) => ({ ...current, alarmTimes: { ...current.alarmTimes, hajj: event.target.value } }))} />
        <label className="switch-control"><input type="checkbox" checked={store.notifications.hajj} onChange={(event) => setStore((current) => ({ ...current, notifications: { ...current.notifications, hajj: event.target.checked } }))} /><span /></label>
      </section>
    </div>
  );
}

function PrayerDialog({
  prayer,
  store,
  setStore,
  now,
  onClose,
  onTestAdhan,
}: {
  prayer: PrayerSchedule;
  store: Store;
  setStore: StoreSetter;
  now: Date;
  onClose: () => void;
  onTestAdhan: () => void;
}) {
  const method = prayerMethods[store.prayerMethod];
  const [locationStatus, setLocationStatus] = useState("");
  const [coordinates, setCoordinates] = useState({ latitude: String(store.latitude), longitude: String(store.longitude) });
  const progress = Math.max(0, Math.min(100, 100 * (1 - prayer.remainingSeconds / Math.max(1, prayer.intervalSeconds))));
  return (
    <div className="modal-backdrop" onMouseDown={(event) => event.target === event.currentTarget && onClose()}>
      <div className="modal-card prayer-modal">
        <div className="modal-heading">
          <div><span className="section-kicker">مواقيت اليوم</span><h2>الصلاة القادمة: {prayer.next.name}</h2></div>
          <button className="icon-button" onClick={onClose} aria-label="إغلاق"><X size={18} /></button>
        </div>
        <div className="prayer-detail-countdown">
          <span>متبقي على الصلاة القادمة</span>
          <strong dir="ltr">{String(Math.floor(prayer.remainingSeconds / 3600)).padStart(2, "0")}:{String(Math.floor((prayer.remainingSeconds % 3600) / 60)).padStart(2, "0")}:{String(prayer.remainingSeconds % 60).padStart(2, "0")}</strong>
          <small>{formatArabicTime(prayer.next.time24)} • {now.toLocaleDateString("ar-EG", { dateStyle: "full" })}</small>
          <div className="progress-track"><span style={{ width: `${progress}%` }} /></div>
        </div>
        <div className="prayer-times-list">
          {prayer.entries.map((entry) => (
            <div className={`prayer-time-row ${entry.key === prayer.next.key ? "upcoming" : ""}`} key={entry.key}>
              <span className="prayer-row-mark">{entry.key === prayer.next.key ? <Sun size={16} /> : <span />}</span>
              <strong>{entry.name}</strong>
              <span>{formatArabicTime(entry.time24)}</span>
              <div className="prayer-offset-control">
                <button className="icon-button soft" onClick={() => setStore((current) => ({ ...current, prayerOffsets: { ...current.prayerOffsets, [entry.key]: (current.prayerOffsets[entry.key] ?? 0) - 1 } }))} aria-label="تقديم دقيقة"><Minus size={13} /></button>
                <small>{toArabicDigits(store.prayerOffsets[entry.key] ?? 0)} د</small>
                <button className="icon-button soft" onClick={() => setStore((current) => ({ ...current, prayerOffsets: { ...current.prayerOffsets, [entry.key]: (current.prayerOffsets[entry.key] ?? 0) + 1 } }))} aria-label="تأخير دقيقة"><Plus size={13} /></button>
              </div>
            </div>
          ))}
        </div>
        <div className="prayer-details-controls">
          <label className="field-label">طريقة الحساب<select value={store.prayerMethod} onChange={(event) => setStore((current) => ({ ...current, prayerMethod: event.target.value as PrayerMethod }))}>{Object.entries(prayerMethods).map(([key, value]) => <option key={key} value={key}>{value.label}</option>)}</select></label>
          <label className="field-label">خط العرض<input type="number" step="0.0001" value={coordinates.latitude} onChange={(event) => setCoordinates((current) => ({ ...current, latitude: event.target.value }))} /></label>
          <label className="field-label">خط الطول<input type="number" step="0.0001" value={coordinates.longitude} onChange={(event) => setCoordinates((current) => ({ ...current, longitude: event.target.value }))} /></label>
        </div>
        {locationStatus && <div className="inline-status">{locationStatus}</div>}
        <div className="modal-actions">
          <button className="button button-outline" onClick={() => {
            if (!navigator.geolocation) { setLocationStatus("تحديد الموقع غير متاح في هذا الجهاز."); return; }
            navigator.geolocation.getCurrentPosition(
              (position) => {
                const latitude = position.coords.latitude;
                const longitude = position.coords.longitude;
                setCoordinates({ latitude: latitude.toFixed(4), longitude: longitude.toFixed(4) });
                setStore((current) => ({ ...current, latitude, longitude }));
                setLocationStatus("تم تحديث الموقع.");
              },
              (error) => setLocationStatus(`تعذر تحديد الموقع: ${error.message}`),
              { enableHighAccuracy: true, timeout: 10000 },
            );
          }}><MapPin size={15} /> موقعي الحالي</button>
          <button className="button button-outline" onClick={onTestAdhan}><Volume2 size={15} /> تجربة الأذان</button>
          <button className="button button-primary" onClick={() => {
            const latitude = Number(coordinates.latitude);
            const longitude = Number(coordinates.longitude);
            if (!Number.isFinite(latitude) || latitude < -90 || latitude > 90 || !Number.isFinite(longitude) || longitude < -180 || longitude > 180) {
              setLocationStatus("أدخل إحداثيات صحيحة: خط العرض من −٩٠ إلى ٩٠ والطول من −١٨٠ إلى ١٨٠.");
              return;
            }
            setStore((current) => ({ ...current, latitude, longitude }));
            setLocationStatus("تم حفظ الإحداثيات وطريقة الحساب.");
          }}><Check size={15} /> حفظ الإعدادات</button>
        </div>
        <small className="prayer-method-note">طريقة الحساب الحالية: {method.label} • العصر حسب المذهب الشافعي.</small>
      </div>
    </div>
  );
}

function SettingsScreen({
  data,
  store,
  setStore,
  setStatus,
  playTone,
  stopSound,
  saveToneFile,
}: {
  data: AppData;
  store: Store;
  setStore: StoreSetter;
  setStatus: (message: string) => void;
  playTone: (key: ToneKey, preview?: boolean) => void;
  stopSound: () => void;
  saveToneFile: (key: ToneKey, file?: File) => void;
}) {
  const [latitude, setLatitude] = useState(String(store.latitude));
  const [longitude, setLongitude] = useState(String(store.longitude));
  const alarmKeys: ToneKey[] = ["prayer", "adhkar", "quran", "hajj", "exercise", "study"];

  const requestNotifications = () => {
    if (!("Notification" in window)) {
      setStatus("الإشعارات غير مدعومة في هذا المتصفح.");
      return;
    }
    void Notification.requestPermission().then((permission) => {
      setStatus(permission === "granted" ? "تم السماح بإشعارات النظام." : "لم يتم السماح بإشعارات النظام؛ ستظل التنبيهات داخل التطبيق متاحة.");
    }).catch((error: unknown) => {
      console.error("Notification permission request failed", error);
      setStatus("تعذر طلب إذن الإشعارات.");
    });
  };

  const updateCoordinates = () => {
    const lat = Number(latitude);
    const lon = Number(longitude);
    if (!Number.isFinite(lat) || lat < -90 || lat > 90 || !Number.isFinite(lon) || lon < -180 || lon > 180) {
      setStatus("أدخل إحداثيات صحيحة: خط العرض من −٩٠ إلى ٩٠ والطول من −١٨٠ إلى ١٨٠.");
      return;
    }
    setStore((current) => ({ ...current, latitude: lat, longitude: lon }));
    setStatus("تم حفظ الإحداثيات.");
  };

  const saveCurrentLocation = () => {
    if (!navigator.geolocation) {
      setStatus("تحديد الموقع الجغرافي غير متاح على هذا الجهاز.");
      return;
    }
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const lat = position.coords.latitude;
        const lon = position.coords.longitude;
        setLatitude(lat.toFixed(4));
        setLongitude(lon.toFixed(4));
        setStore((current) => ({ ...current, latitude: lat, longitude: lon }));
        setStatus("تم تحديث موقع الصلاة الحالي.");
      },
      (error) => {
        console.error("Geolocation request failed", error);
        setStatus(`تعذر تحديد الموقع: ${error.message}`);
      },
      { enableHighAccuracy: true, timeout: 10000 },
    );
  };

  return (
    <div className="screen-stack">
      <PageHeading icon={Settings} title="الإعدادات" subtitle="المظهر، الموقع، الإشعارات والنغمات — تحفظ تلقائياً على جهازك." />
      <div className="settings-layout">
        <div className="settings-main">
          <section className="panel settings-card">
            <div className="settings-card-heading"><div className="panel-icon purple"><Settings size={19} /></div><div><h2>المظهر والحساب المحلي</h2><p>خصّص طريقة عرض التطبيق دون إنشاء حساب أو اتصال بالإنترنت.</p></div></div>
            <label className="field-label">الاسم المعروض<input value={store.userName} maxLength={40} onChange={(event) => setStore((current) => ({ ...current, userName: event.target.value }))} placeholder="اسمك" /></label>
            <div className="settings-row">
              <div><strong>الوضع الداكن</strong><small>تغيير ألوان التطبيق إلى مظهر داكن.</small></div>
              <label className="switch-control"><input type="checkbox" checked={store.darkMode} onChange={(event) => setStore((current) => ({ ...current, darkMode: event.target.checked }))} /><span /></label>
            </div>
            <div className="settings-row">
              <div><strong>اتجاه الواجهة</strong><small>العربية من اليمين إلى اليسار أو اتجاه يسار إلى يمين.</small></div>
              <div className="choice-chips">
                <button className={store.arabicRtl ? "selected" : ""} onClick={() => setStore((current) => ({ ...current, arabicRtl: true }))}>RTL</button>
                <button className={!store.arabicRtl ? "selected" : ""} onClick={() => setStore((current) => ({ ...current, arabicRtl: false }))}>LTR</button>
              </div>
            </div>
            <div className="settings-row">
              <div><strong>رقم السكشن الدراسي</strong><small>يحدد المواد التي تظهر في الصفحة الرئيسية والجدول.</small></div>
              <div className="choice-chips">
                {[1, 2].map((section) => <button key={section} className={store.sectionNumber === section ? "selected" : ""} onClick={() => setStore((current) => ({ ...current, sectionNumber: section }))}>سكشن {toArabicDigits(section)}</button>)}
              </div>
            </div>
          </section>

          <section className="panel settings-card">
            <div className="settings-card-heading"><div className="panel-icon mint"><MapPin size={19} /></div><div><h2>مواقيت الصلاة والموقع</h2><p>حساب محلي لمواقيت اليوم مع إمكانية ضبط الإحداثيات والطريقة.</p></div></div>
            <label className="field-label">طريقة حساب المواقيت<select value={store.prayerMethod} onChange={(event) => setStore((current) => ({ ...current, prayerMethod: event.target.value as PrayerMethod }))}>{Object.entries(prayerMethods).map(([key, method]) => <option value={key} key={key}>{method.label}</option>)}</select></label>
            <div className="form-grid">
              <label className="field-label">خط العرض<input type="number" step="0.0001" value={latitude} onChange={(event) => setLatitude(event.target.value)} /></label>
              <label className="field-label">خط الطول<input type="number" step="0.0001" value={longitude} onChange={(event) => setLongitude(event.target.value)} /></label>
            </div>
            <div className="location-actions"><button className="button button-outline" onClick={saveCurrentLocation}><MapPin size={15} /> استخدام موقعي</button><button className="button button-primary" onClick={updateCoordinates}><Check size={15} /> حفظ الإحداثيات</button></div>
            <small className="field-hint">الموقع الافتراضي: القاهرة. قد يتطلب استخدام GPS إذناً من النظام أو تشغيل التطبيق عبر localhost.</small>
          </section>

          <section className="panel settings-card">
            <div className="settings-card-heading"><div className="panel-icon gold"><AudioLines size={19} /></div><div><h2>الإشعارات والتنبيهات</h2><p>تحكم شامل ومستقل في التنبيهات لكل قسم.</p></div></div>
            <div className="master-notification">
              <div><strong>تفعيل الإشعارات والتنبيهات</strong><small>{store.notificationsEnabled ? "النظام يعمل للأقسام المحددة أدناه." : "جميع التنبيهات متوقفة."}</small></div>
              <label className="switch-control"><input type="checkbox" checked={store.notificationsEnabled} onChange={(event) => setStore((current) => ({ ...current, notificationsEnabled: event.target.checked }))} /><span /></label>
            </div>
            <div className="settings-row">
              <div><strong>مزامنة الصوت مع الإشعار</strong><small>تشغيل النغمة عند وقت التنبيه المحدد.</small></div>
              <label className="switch-control"><input type="checkbox" checked={store.syncAlertWithNotification} disabled={!store.notificationsEnabled} onChange={(event) => setStore((current) => ({ ...current, syncAlertWithNotification: event.target.checked }))} /><span /></label>
            </div>
            <div className="alarm-settings-list">
              {alarmKeys.map((key) => (
                <article className="alarm-setting-row" key={key}>
                  <div className="alarm-setting-title">
                    <strong>{notificationLabels[key]}</strong>
                    <label className="switch-control"><input type="checkbox" checked={store.notifications[key] ?? false} disabled={!store.notificationsEnabled} onChange={(event) => setStore((current) => ({ ...current, notifications: { ...current.notifications, [key]: event.target.checked } }))} /><span /></label>
                  </div>
                  <div className="alarm-setting-controls">
                    {key !== "prayer" && (
                      <label className="time-field">موعد يومي<input type="time" value={store.alarmTimes[key] ?? "08:00"} disabled={!store.notificationsEnabled || !store.notifications[key]} onChange={(event) => setStore((current) => ({ ...current, alarmTimes: { ...current.alarmTimes, [key]: event.target.value } }))} /></label>
                    )}
                    <label className="tone-select">النغمة<select value={store.tones[key] ?? defaultTones[key]} disabled={!store.notificationsEnabled} onChange={(event) => setStore((current) => ({ ...current, tones: { ...current.tones, [key]: event.target.value } }))}>{toneOptions.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}</select></label>
                    <label className="switch-sound"><input type="checkbox" checked={store.alertSounds[key] ?? false} disabled={!store.notificationsEnabled || !store.notifications[key]} onChange={(event) => setStore((current) => ({ ...current, alertSounds: { ...current.alertSounds, [key]: event.target.checked } }))} /><Volume2 size={16} /><span>صوت</span></label>
                    <button className="icon-button soft" title="تجربة النغمة" onClick={() => playTone(key, true)}><Play size={15} /></button>
                    <label className="upload-tone" title="اختيار نغمة مخصصة"><input type="file" accept="audio/*,.mp3,.m4a,.wav,.ogg" onChange={(event) => saveToneFile(key, event.target.files?.[0])} /><AudioLines size={15} /> تخصيص</label>
                  </div>
                </article>
              ))}
            </div>
            <div className="settings-actions">
              <button className="button button-outline" onClick={requestNotifications}><BellPermissionIcon /> السماح بإشعارات النظام</button>
              <button className="button button-danger-outline" onClick={() => { stopSound(); setStatus("تم إيقاف الصوت الحالي."); }}><X size={15} /> إيقاف الصوت</button>
            </div>
            <small className="field-hint">تظهر إشعارات النظام أثناء تشغيل التطبيق. تشغيل الصوت في الخلفية بعد إغلاق التطبيق يعتمد على نظام التشغيل.</small>
          </section>

          <section className="panel settings-card">
            <div className="settings-card-heading"><div className="panel-icon purple"><BookOpen size={19} /></div><div><h2>بيانات التطبيق</h2><p>المحتوى محفوظ محلياً ويمكن فتحه دون اتصال.</p></div></div>
            <div className="data-stat-grid">
              <div><strong>{data.quran.length.toLocaleString("ar-EG")}</strong><span>آية قرآنية</span></div>
              <div><strong>{toArabicDigits(data.surahs.length)}</strong><span>سورة</span></div>
              <div><strong>{toArabicDigits(data.hisn.length)}</strong><span>باباً في حصن المسلم</span></div>
              <div><strong>{toArabicDigits(data.exercises.length)}</strong><span>تمريناً</span></div>
            </div>
            <a className="download-link" href={asset("data/hisn_almuslim.pdf")} download="hisn-al-muslim.pdf"><FileText size={16} /> تنزيل كتاب حصن المسلم PDF</a>
          </section>
        </div>
        <aside className="settings-side">
          <div className="settings-side-card">
            <div className="settings-side-icon"><Shield size={22} /></div>
            <h3>بياناتك تبقى عندك</h3>
            <p>الإعدادات والعلامات المرجعية وعدّادات الذكر محفوظة محلياً على هذا الجهاز.</p>
            <div className="offline-indicator"><span /> دون اتصال</div>
          </div>
          <div className="settings-side-card purple-side">
            <div className="settings-side-icon"><BookOpenCheck size={22} /></div>
            <h3>منارة الطالب</h3>
            <p>نسخة ويب وسطح مكتب مبنية على بيانات المشروع الأصلي.</p>
            <span className="sidebar-version">الإصدار 1.0</span>
          </div>
        </aside>
      </div>
    </div>
  );
}

function BellPermissionIcon() {
  return <Volume2 size={15} />;
}

export default App;
