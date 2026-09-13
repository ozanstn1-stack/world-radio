# 📻 World Radio Globe

Android cihazlar için canlı dünya radyolarını 3B Küre ve 1:1 Gerçek Dünya Haritası üzerinden keşfetme ve dinleme uygulaması.

---

## 📲 Obtainium ile Otomatik Güncelleme Kurulumu

Bu uygulama, telefonunuzdaki **[Obtainium](https://github.com/ImranR98/Obtainium)** uygulamasıyla doğrudan entegre çalışır. Yeni bir güncelleme/release yayınlandığında Obtainium otomatik olarak algılar ve tek dokunuşla güncellemenizi sağlar.

### Kurulum Adımları:
1. Android telefonunuzda **Obtainium** uygulamasını açın.
2. Sağ alt köşedeki **+ (Add App / Uygulama Ekle)** butonuna dokunun.
3. **App Source URL** alanına GitHub deponuzun linkini yapıştırın:
   ```text
   https://github.com/KULLANICI_ADINIZ/REPO_ADINIZ
   ```
4. **Add (Ekle)** butonuna basın.
5. Obtainium depodaki son Release'i ve APK dosyasını (`RadioGlobe-v*.apk` veya `RadioGlobe-release.apk`) otomatik olarak bulur ve indirip yükler.

---

## 🚀 Yeni Sürüm (Release + APK) Yayınlama

GitHub Actions iş akışı (`.github/workflows/release.yml`) otomatik olarak yapılandırılmıştır. Yeni bir APK sürümü yayınlamak için 2 kolay yönteminiz vardır:

### Yöntem 1: GitHub Web Arayüzünden Tek Tıkla (Önerilen)
1. GitHub reponuzda **Actions** sekmesine gidin.
2. Sol taraftan **Build & Publish Release APK** iş akışını seçin.
3. Sağ taraftaki **Run workflow** butonuna tıklayın:
   - Sürüm etiketini belirleyin (örneğin: `v1.0.1`, `v1.1.0` vb.).
   - **Run workflow** butonuna basın.
4. Yaklaşık 2-3 dakika içinde APK derlenir, imzalanır ve GitHub Releases bölümünde yeni sürüm oluşturulur.
5. Telefonunuzdaki Obtainium bu yeni sürümü otomatik olarak bildirir.

### Yöntem 2: Git Tag ile (Terminal / Komut Satırı)
Herhangi bir yeni tag push ettiğinizde otomatik olarak release oluşturulur:
```bash
git tag v1.0.1
git push origin v1.0.1
```

---

## ⚙️ Özel İmzalama Anahtarı (Opsiyonel)
GitHub Actions iş akışımız, deponuzda özel bir anahtar tanımlı değilse otomatik olarak güvenli bir release anahtarı oluşturur ve APK'yı imzalar.

Kendi özel `.jks` imzalama anahtarınızı kullanmak isterseniz:
1. GitHub reponuzda **Settings > Secrets and variables > Actions** bölümüne gidin.
2. Aşağıdaki secret'ları ekleyin:
   - `KEYSTORE_BASE64`: `.jks` dosyanızın base64 formatındaki metni (`base64 -w 0 your-key.jks`)
   - `STORE_PASSWORD`: Keystore şifreniz
   - `KEY_PASSWORD`: Anahtar şifreniz
