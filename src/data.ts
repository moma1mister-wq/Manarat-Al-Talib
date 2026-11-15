import type {
  AppData,
  HisnChapter,
  PrayerEntry,
  PrayerSchedule,
  PriceItem,
  QuranVerse,
  Surah,
} from "./types";
import quranData from "./data/quran_full_6236.json";
import surahData from "./data/surahs.json";
import hisnData from "./data/hisn_almuslim_full.json";
import exerciseData from "./data/exercises.json";
import scheduleData from "./data/schedule.json";
import priceData from "./data/prices.json";

export const asset = (path: string) =>
  `${import.meta.env.BASE_URL}${path.replace(/^\/+/, "")}`;

export async function loadAppData(): Promise<AppData> {
  const quran = quranData as QuranVerse[];
  const surahs = surahData as Surah[];
  const hisn = hisnData as HisnChapter[];
  const exercises = exerciseData as AppData["exercises"];
  const schedule = scheduleData as Omit<AppData["schedule"][number], "id">[];
  const prices = priceData as PriceItem[];
  const pages = new Set(quran.map((verse) => verse.p));

  if (
    quran.length !== 6236 ||
    surahs.length !== 114 ||
    pages.size !== 604 ||
    Array.from({ length: 604 }, (_, index) => index + 1).some((page) => !pages.has(page))
  ) {
    throw new Error("بيانات القرآن غير مكتملة: المتوقع ٦٬٢٣٦ آية و١١٤ سورة.");
  }

  return {
    quran: quran.map((verse) => ({
      ...verse,
      t: verse.t.replace(/^\uFEFF/, ""),
    })),
    surahs,
    hisn,
    exercises,
    schedule: schedule.map((item, index) => ({ ...item, id: `seed-${index + 1}` })),
    prices,
  };
}

export const weekdays = [
  "الأحد",
  "الاثنين",
  "الثلاثاء",
  "الأربعاء",
  "الخميس",
  "الجمعة",
  "السبت",
];

export const exerciseCategories = [
  "الكل",
  "صدر",
  "ظهر",
  "أرجل",
  "كتف",
  "بطن",
  "كارديو",
  "إطالات",
  "وزن الجسم",
];

export const adhkarPresets = [
  "الكل",
  "أذكار الصباح",
  "أذكار المساء",
  "أذكار السفر",
  "أذكار النوم",
  "أذكار الاستيقاظ",
  "أذكار بعد الصلاة",
];

export const prayerMethods = {
  EGYPTIAN: { label: "الهيئة المصرية العامة للمساحة", fajr: 19.5, isha: 17.5, ishaOffset: 0 },
  UMM_AL_QURA: { label: "تقويم أم القرى — مكة المكرمة", fajr: 18.5, isha: 0, ishaOffset: 90 },
  MWL: { label: "رابطة العالم الإسلامي", fajr: 18, isha: 17, ishaOffset: 0 },
  KARACHI: { label: "جامعة العلوم الإسلامية — كراتشي", fajr: 18, isha: 18, ishaOffset: 0 },
  ISNA: { label: "الجمعية الإسلامية لأمريكا الشمالية", fajr: 15, isha: 15, ishaOffset: 0 },
} as const;

export type PrayerMethod = keyof typeof prayerMethods;

const degToRad = (degrees: number) => (degrees * Math.PI) / 180;
const radToDeg = (radians: number) => (radians * 180) / Math.PI;

function fixAngle(angle: number) {
  return ((angle % 360) + 360) % 360;
}

function fixHour(hour: number) {
  return ((hour % 24) + 24) % 24;
}

export function calculatePrayerSchedule(
  latitude: number,
  longitude: number,
  method: PrayerMethod,
  offsets: Record<string, number>,
  now: Date,
): PrayerSchedule {
  const year = now.getFullYear();
  let month = now.getMonth() + 1;
  let dayYear = year;
  if (month <= 2) {
    dayYear -= 1;
    month += 12;
  }
  const century = Math.floor(dayYear / 100);
  const correction = 2 - century + Math.floor(century / 4);
  const julianDate =
    Math.floor(365.25 * (dayYear + 4716)) +
    Math.floor(30.6001 * (month + 1)) +
    now.getDate() +
    correction -
    1524.5;
  const daysSinceEpoch = julianDate - 2451545;
  const meanAnomaly = fixAngle(357.529 + 0.98560028 * daysSinceEpoch);
  const meanLongitude = fixAngle(280.459 + 0.98564736 * daysSinceEpoch);
  const solarLongitude = fixAngle(
    meanLongitude +
      1.915 * Math.sin(degToRad(meanAnomaly)) +
      0.02 * Math.sin(degToRad(2 * meanAnomaly)),
  );
  const obliquity = 23.439 - 0.00000036 * daysSinceEpoch;
  const rightAscension =
    radToDeg(
      Math.atan2(
        Math.cos(degToRad(obliquity)) * Math.sin(degToRad(solarLongitude)),
        Math.cos(degToRad(solarLongitude)),
      ),
    ) / 15;
  const equationOfTime = meanLongitude / 15 - fixHour(rightAscension);
  const declination = radToDeg(
    Math.asin(Math.sin(degToRad(obliquity)) * Math.sin(degToRad(solarLongitude))),
  );
  const timezoneHours = -now.getTimezoneOffset() / 60;
  const noon = 12 + timezoneHours - longitude / 15 - equationOfTime;

  const sunAngleTime = (angle: number, beforeNoon: boolean) => {
    const numerator =
      -Math.sin(degToRad(angle)) -
      Math.sin(degToRad(declination)) * Math.sin(degToRad(latitude));
    const denominator =
      Math.cos(degToRad(declination)) * Math.cos(degToRad(latitude));
    const ratio = Math.max(-0.9999, Math.min(0.9999, numerator / denominator));
    const interval = radToDeg(Math.acos(ratio)) / 15;
    return noon + (beforeNoon ? -interval : interval);
  };

  const settings = prayerMethods[method];
  const asrAngle =
    -radToDeg(
      Math.atan(
        1 /
          (1 +
            Math.tan(degToRad(Math.abs(latitude - declination)))),
      ),
    );
  const rawTimes = [
    { key: "FAJR", name: "الفجر", hours: sunAngleTime(settings.fajr, true) },
    { key: "SUNRISE", name: "الشروق", hours: sunAngleTime(0.833, true) },
    { key: "DHUHR", name: "الظهر", hours: noon + 2 / 60 },
    { key: "ASR", name: "العصر", hours: sunAngleTime(asrAngle, false) },
    { key: "MAGHRIB", name: "المغرب", hours: sunAngleTime(0.833, false) },
    {
      key: "ISHA",
      name: "العشاء",
      hours:
        settings.ishaOffset > 0
          ? sunAngleTime(0.833, false) + settings.ishaOffset / 60
          : sunAngleTime(settings.isha, false),
    },
  ];

  const entries: PrayerEntry[] = rawTimes.map((entry) => {
    const rawMinutes = Math.trunc(fixHour(entry.hours) * 60) + (offsets[entry.key] ?? 0);
    const minutes = ((rawMinutes % 1440) + 1440) % 1440;
    return {
      key: entry.key,
      name: entry.name,
      time: minutes,
      time24: `${String(Math.floor(minutes / 60)).padStart(2, "0")}:${String(minutes % 60).padStart(2, "0")}`,
    };
  });
  const obligatory = entries.filter((entry) => entry.key !== "SUNRISE");
  const nowSeconds =
    now.getHours() * 3600 + now.getMinutes() * 60 + now.getSeconds();
  const nextIndex = obligatory.findIndex((entry) => entry.time * 60 > nowSeconds);
  const resolvedNextIndex = nextIndex === -1 ? 0 : nextIndex;
  const next = obligatory[resolvedNextIndex];
  const previous = obligatory[(resolvedNextIndex + obligatory.length - 1) % obligatory.length];
  const nextSeconds = next.time * 60;
  const previousSeconds = previous.time * 60;
  const remainingSeconds =
    nextSeconds > nowSeconds
      ? nextSeconds - nowSeconds
      : 86400 - nowSeconds + nextSeconds;
  const intervalSeconds =
    nextSeconds > previousSeconds
      ? nextSeconds - previousSeconds
      : 86400 - previousSeconds + nextSeconds;

  return { entries, next, previous, remainingSeconds, intervalSeconds };
}

export function formatArabicTime(time24: string) {
  const [hours, minutes] = time24.split(":").map(Number);
  const suffix = hours < 12 ? "ص" : "م";
  const hour12 = hours % 12 || 12;
  return `${hour12}:${String(minutes).padStart(2, "0")} ${suffix}`;
}

export function toArabicDigits(value: number | string) {
  return String(value).replace(/\d/g, (digit) => "٠١٢٣٤٥٦٧٨٩"[Number(digit)]);
}

export function normalizeArabic(value: string) {
  return value
    .replace(/[\u064B-\u065F\u0610-\u061A\u06D6-\u06ED\u0640]/g, "")
    .replace(/\u0670/g, "ا")
    .replace(/[أإآٱ]/g, "ا")
    .replace(/[ىئ]/g, "ي")
    .replace(/ؤ/g, "و")
    .replace(/ة/g, "ه")
    .replace(/\s+/g, " ")
    .trim();
}

export function getPageVerses(quran: QuranVerse[], page: number) {
  return quran.filter((verse) => verse.p === page);
}

export function getSurahName(surahs: Surah[], number: number) {
  return surahs[number - 1]?.nameAr ?? `سورة ${number}`;
}
