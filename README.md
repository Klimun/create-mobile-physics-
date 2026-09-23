# Create Mobile Physics v0.1.0

Minecraft **1.21.1** + **NeoForge**  
Create 6.0.10 uyumlu (opsiyonel)  
Mojo Launcher / Android için hafif fizik eklentisi

- Hot Air Balloon (yukarı kaldırma)
- Simple Thruster (redstone ile kuvvet)
- Performans config (telefon için limitler)

---

## Telefondan JAR almak (GitHub Actions)

### 1. GitHub hesabı aç (yoksa)
https://github.com

### 2. Yeni repo oluştur
- **New repository** → isim: `create-mobile-physics` (veya istediğin)
- Public yap
- **Create repository**

### 3. Dosyaları yükle
**Kolay yol (telefon):**
1. Bu zip’i indir
2. GitHub’da repo sayfasına gir
3. **Add file → Upload files**
4. Zip’in **içindeki tüm dosyaları** sürükle (klasörün kendisini değil, içindeki `src`, `gradle`, `build.gradle`, `.github` vb.)
5. **Commit changes**

### 4. Actions’ı çalıştır
1. Repo’da **Actions** sekmesine gir
2. Sol tarafta **Build Mod JAR** seç
3. **Run workflow** → **Run workflow** butonuna bas
4. 3–10 dakika bekle (yeşil tik gelsin)

### 5. JAR’ı indir
1. Bitince yeşil workflow’a tıkla
2. En altta **Artifacts** → **create_mobile_physics-jar**
3. İndir → zip aç → `create_mobile_physics-0.1.0.jar` çıkar

Bu jar’ı Mojo Launcher’da **1.21.1 NeoForge** instance’ının `mods` klasörüne at + Create 6.0.10 ile kullan.

---

## Gereksinimler (oyun)
- Minecraft 1.21.1
- NeoForge 21.1.x
- (Opsiyonel) Create 6.0.10

## Lisans
MIT
