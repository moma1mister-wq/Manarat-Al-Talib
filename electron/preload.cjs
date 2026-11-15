const { contextBridge, ipcRenderer } = require("electron");

contextBridge.exposeInMainWorld("manaratDesktop", {
  showNotification: (title, body) =>
    ipcRenderer.invoke("notifications:show", title, body),
  saveCustomTone: (key, fileName, bytes) =>
    ipcRenderer.invoke("tones:save", key, fileName, bytes),
  loadCustomTones: () => ipcRenderer.invoke("tones:load"),
});
