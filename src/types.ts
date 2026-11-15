export type NavKey =
  | "home"
  | "schedule"
  | "quran"
  | "exercises"
  | "adhkar"
  | "hajj"
  | "settings";

export interface QuranVerse {
  s: number;
  a: number;
  p: number;
  j: number;
  h: number;
  t: string;
}

export interface Surah {
  number: number;
  nameAr: string;
  nameEn: string;
  ayahCount: number;
  isMakki: boolean;
  startPage: number;
  endPage: number;
  juzStart: number;
}

export interface HisnItem {
  id: number;
  text: string;
  count: number;
}

export interface HisnChapter {
  id: number;
  title: string;
  category: string;
  items: HisnItem[];
}

export interface Exercise {
  id: number;
  nameAr: string;
  nameEn: string;
  categoryAr: string;
  targetMusclesAr: string;
  setsCount: number;
  repsOrDurationAr: string;
  defaultTimerSeconds: number;
  stepsAr: string[];
  animationPose: string;
}

export interface ScheduleItem {
  id: string;
  title: string;
  dayOfWeek: string;
  startTime: string;
  endTime: string;
  location: string;
  sectionFilter: number;
  itemType: string;
  recurrence: string;
  timerMinutes: number;
}

export interface PriceItem {
  id: string;
  title: string;
  category: string;
  description: string;
  priceEgp: number;
  unit: string;
  pdfPageNumber: number;
}

export interface AppData {
  quran: QuranVerse[];
  surahs: Surah[];
  hisn: HisnChapter[];
  exercises: Exercise[];
  schedule: ScheduleItem[];
  prices: PriceItem[];
}

export interface PrayerEntry {
  key: string;
  name: string;
  time: number;
  time24: string;
}

export interface PrayerSchedule {
  entries: PrayerEntry[];
  next: PrayerEntry;
  previous: PrayerEntry;
  remainingSeconds: number;
  intervalSeconds: number;
}
