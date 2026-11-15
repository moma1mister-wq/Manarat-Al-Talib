# منارة الطالب — الويب وسطح المكتب

نسخة ويب عربية متجاوبة ومغلّف سطح مكتب Electron مبنيان على بيانات مشروع Android الأصلي. تتضمن جدولاً دراسياً محلياً، مواقيت الصلاة وحساباً تنازلياً، المصحف كاملاً (٦٬٢٣٦ آية و٦٠٤ صفحات) مع الفهرسة والبحث ومساعد الكتابة، التمارين، حصن المسلم والسبحة، الحج والعمرة، قائمة الأسعار القابلة للبحث والتعديل، والإعدادات والنغمات.

تُحمّل بيانات القرآن والأذكار والتمارين من ملفات محلية، وتُحفظ الإعدادات والعلامات المرجعية والتقدم في التخزين المحلي. لا تتطلب نسخة الويب اتصالاً بخدمة خارجية.

## تشغيل نسخة الويب

**المتطلبات:** Node.js 22 أو أحدث وnpm.

```powershell
npm ci
npm run dev
```

لبناء النسخة الثابتة:

```powershell
npm run build
npm run preview
```

## تشغيل تطبيق Electron

```powershell
npm ci
npm run electron:dev
```

ولإنشاء مُثبّت Windows محلياً (يتطلب Windows):

```powershell
npm run electron:dist
```

ينتج الملف التنفيذي بصيغة `.exe` داخل مجلد `release`.

## GitHub Releases

كل دفع إلى الفرع `main` أو تشغيل يدوي لسير العمل `.github/workflows/windows-release.yml` يشغّل البناء على `windows-latest`. ينتج سير العمل مُثبّت NSIS، ويرفعه كـ `windows-exe-build` artifact، ثم ينشئ إصدار GitHub بأصل تنزيل ثابت `app-windows.exe`. بعد نجاح أول تشغيل، سيكون رابط التنزيل المباشر:

`https://github.com/moma1mister-wq/Manarat-Al-Talib/releases/latest/download/app-windows.exe`

يتطلب النشر صلاحية `contents: write` للرمز الافتراضي `GITHUB_TOKEN`.

## مشروع Android

يبقى مشروع Android الأصلي في `app/` وملفات Gradle كما هي؛ إعدادات البناء الخاصة به لم تتغير.
