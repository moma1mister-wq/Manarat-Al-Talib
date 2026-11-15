const fs = require("node:fs/promises");
const path = require("node:path");
const {
  app,
  BrowserWindow,
  Menu,
  Notification,
  Tray,
  ipcMain,
  nativeImage,
  shell,
} = require("electron");

const toneMimeTypes = {
  ".mp3": "audio/mpeg",
  ".m4a": "audio/mp4",
  ".wav": "audio/wav",
  ".ogg": "audio/ogg",
  ".webm": "audio/webm",
};
const toneKeys = new Set(["prayer", "adhkar", "quran", "exercise", "study"]);
const maxToneBytes = 20 * 1024 * 1024;
const runsInTray = process.platform === "win32" && app.isPackaged;

let mainWindow;
let tray;
let isQuitting = false;

app.commandLine.appendSwitch("autoplay-policy", "no-user-gesture-required");
if (process.platform === "win32") app.setAppUserModelId("com.manarataltalib.desktop");

function showMainWindow() {
  if (!mainWindow) return;
  if (mainWindow.isMinimized()) mainWindow.restore();
  mainWindow.show();
  mainWindow.focus();
}

function createTray() {
  const iconPath = path.join(app.getAppPath(), "dist", "data", "logo.png");
  const icon = nativeImage.createFromPath(iconPath);
  if (icon.isEmpty()) throw new Error(`Could not load tray icon: ${iconPath}`);

  tray = new Tray(icon.resize({ width: 16, height: 16 }));
  tray.setToolTip("منارة الطالب — التذكيرات تعمل");
  tray.setContextMenu(
    Menu.buildFromTemplate([
      { label: "إظهار منارة الطالب", click: showMainWindow },
      { type: "separator" },
      {
        label: "إنهاء البرنامج",
        click: () => {
          isQuitting = true;
          app.quit();
        },
      },
    ]),
  );
  tray.on("click", showMainWindow);
  tray.on("double-click", showMainWindow);
}

function createWindow() {
  const startHidden = runsInTray && process.argv.includes("--background");
  const window = new BrowserWindow({
    width: 1440,
    height: 960,
    minWidth: 375,
    minHeight: 680,
    show: !startHidden,
    backgroundColor: "#f3f2f8",
    autoHideMenuBar: true,
    title: "منارة الطالب",
    webPreferences: {
      preload: path.join(__dirname, "preload.cjs"),
      contextIsolation: true,
      nodeIntegration: false,
      sandbox: true,
      backgroundThrottling: false,
    },
  });
  mainWindow = window;

  const devServer = process.env.VITE_DEV_SERVER_URL;
  if (devServer) {
    window.loadURL(devServer);
  } else {
    window.loadFile(path.join(__dirname, "..", "dist", "index.html"));
  }

  window.webContents.setWindowOpenHandler(({ url }) => {
    if (url.startsWith("https:")) shell.openExternal(url);
    return { action: "deny" };
  });

  window.on("close", (event) => {
    if (!runsInTray || isQuitting) return;
    event.preventDefault();
    window.hide();
  });
  window.on("closed", () => {
    if (mainWindow === window) mainWindow = undefined;
  });
}

function showSystemNotification(title, body) {
  if (!Notification.isSupported()) return false;
  new Notification({
    title,
    body,
    silent: true,
    icon: path.join(app.getAppPath(), "dist", "data", "logo.png"),
  }).show();
  return true;
}

function validateNotificationText(value, label, maxLength) {
  if (typeof value !== "string" || value.trim().length === 0) {
    throw new TypeError(`${label} must be a non-empty string.`);
  }
  return value.trim().slice(0, maxLength);
}

function toBuffer(value) {
  if (Buffer.isBuffer(value)) return Buffer.from(value);
  if (value instanceof ArrayBuffer) return Buffer.from(value);
  if (ArrayBuffer.isView(value)) {
    return Buffer.from(value.buffer, value.byteOffset, value.byteLength);
  }
  throw new TypeError("Custom alert sound data must be an ArrayBuffer or typed array.");
}

function toneDirectory() {
  return path.join(app.getPath("userData"), "tones");
}

async function saveCustomTone(key, fileName, value) {
  if (!toneKeys.has(key)) throw new TypeError("Unsupported alert sound category.");
  if (typeof fileName !== "string") throw new TypeError("A file name is required.");

  const extension = path.extname(path.basename(fileName)).toLowerCase();
  const mimeType = toneMimeTypes[extension];
  if (!mimeType) throw new TypeError("Choose an MP3, M4A, WAV, OGG, or WEBM audio file.");

  const bytes = toBuffer(value);
  if (bytes.length === 0 || bytes.length > maxToneBytes) {
    throw new RangeError("Custom alert sounds must be between 1 byte and 20 MB.");
  }

  const directory = toneDirectory();
  await fs.mkdir(directory, { recursive: true });
  const existingFiles = await fs.readdir(directory);
  for (const existingFile of existingFiles) {
    const existingExtension = path.extname(existingFile).toLowerCase();
    if (
      path.basename(existingFile, existingExtension) === key &&
      Object.hasOwn(toneMimeTypes, existingExtension)
    ) {
      await fs.rm(path.join(directory, existingFile), { force: true });
    }
  }

  const destination = path.join(directory, `${key}${extension}`);
  await fs.writeFile(destination, bytes);
  return `data:${mimeType};base64,${bytes.toString("base64")}`;
}

async function loadCustomTones() {
  const directory = toneDirectory();
  let files;
  try {
    files = await fs.readdir(directory);
  } catch (error) {
    if (error && typeof error === "object" && error.code === "ENOENT") return {};
    throw error;
  }

  const tones = {};
  for (const fileName of files) {
    const extension = path.extname(fileName).toLowerCase();
    const key = path.basename(fileName, extension);
    const mimeType = toneMimeTypes[extension];
    if (!toneKeys.has(key) || !mimeType) continue;

    const bytes = await fs.readFile(path.join(directory, fileName));
    if (bytes.length === 0 || bytes.length > maxToneBytes) {
      throw new RangeError(`Saved custom alert sound is invalid: ${fileName}`);
    }
    tones[key] = `data:${mimeType};base64,${bytes.toString("base64")}`;
  }
  return tones;
}

ipcMain.handle("notifications:show", (_event, title, body) =>
  showSystemNotification(
    validateNotificationText(title, "Notification title", 120),
    validateNotificationText(body, "Notification body", 500),
  ),
);
ipcMain.handle("tones:save", (_event, key, fileName, bytes) =>
  saveCustomTone(key, fileName, bytes),
);
ipcMain.handle("tones:load", () => loadCustomTones());

const hasSingleInstanceLock = app.requestSingleInstanceLock();
if (!hasSingleInstanceLock) {
  app.quit();
} else {
  app.on("second-instance", showMainWindow);
  app.on("before-quit", () => {
    isQuitting = true;
  });
  app.whenReady().then(() => {
    if (runsInTray) {
      createTray();
      app.setLoginItemSettings({ openAtLogin: true, args: ["--background"] });
    }
    createWindow();
    app.on("activate", () => {
      if (BrowserWindow.getAllWindows().length === 0) createWindow();
      else showMainWindow();
    });
  });
}

app.on("window-all-closed", () => {
  if (process.platform !== "darwin") app.quit();
});
