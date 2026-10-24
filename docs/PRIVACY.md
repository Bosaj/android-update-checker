# Privacy Policy — Update Radar

**Effective date:** 24 October 2026 · **App:** Update Radar (`ma.bosaj.updateradar`) · **Developer:** Oussama EL HADJI ([@Bosaj](https://github.com/Bosaj))

[English](#english) · [Français](#français) · [العربية](#العربية)

---

## English

Update Radar shows which apps on your phone have not been updated for a while and opens the store page where you can update them. It is built so that your data stays on your phone.

### What the app reads
To build the radar, the app asks Android for the apps that have a launcher icon. For each one it reads:
- the app's name, package name, version and icon;
- the dates of its first install and its last update;
- which store or installer put it on the phone.

It does this through Android's package-visibility rules: it only declares the launcher-app query. It does **not** use the `QUERY_ALL_PACKAGES` permission.

### What the app stores
On the phone only, in the app's private storage:
- your settings: thresholds, reminder choices, the apps you chose to ignore, and your language;
- the previous scan, which is used to find apps that updated between two scans.

### What leaves the phone
**Nothing.** The app has no `INTERNET` permission, so it cannot send data anywhere. It has no account, no ads, no analytics, no crash reporting and no third-party SDKs that collect data.

When you tap a store or web link, Android opens that store or your browser, and their own privacy policies apply from then on.

### Backups
If Android backup is on, your **settings** can be included in your own Google backup, and only when that backup is end-to-end encrypted. The scan of your apps is not backed up.

### Permissions
| Permission | Why |
|---|---|
| Notifications (`POST_NOTIFICATIONS`) | Optional reminders about stale apps. You can refuse or turn them off at any time. |
| Run at startup, wake lock, network state | Used by Android's WorkManager to run the reminder check on Wi-Fi. No data is sent. |

### Children
The app is not directed at children and collects no personal data from anyone.

### Your rights
All the data is on your phone. Clearing the app's storage or uninstalling the app deletes it completely. Because nothing is collected, there is nothing for us to access, export or delete on a server.

### Changes and contact
Changes to this policy are published in this file, with a new effective date. Questions: open an issue at <https://github.com/Bosaj/android-update-checker/issues>, or report a security problem privately through [GitHub security advisories](https://github.com/Bosaj/android-update-checker/security/advisories/new).

---

## Français

Update Radar montre les applications de votre téléphone qui n’ont pas été mises à jour depuis un moment et ouvre la page du store où vous pouvez les mettre à jour. Il est conçu pour que vos données restent sur votre téléphone.

**Ce que l’appli lit :** pour chaque application qui a une icône dans le lanceur : son nom, son nom de paquet, sa version, son icône, ses dates de première installation et de dernière mise à jour, et le store ou l’installateur qui l’a installée. L’appli n’utilise pas l’autorisation `QUERY_ALL_PACKAGES`.

**Ce que l’appli enregistre :** uniquement sur le téléphone, dans son stockage privé : vos réglages (seuils, rappels, applis ignorées, langue) et le scan précédent.

**Ce qui quitte le téléphone :** **rien.** L’appli n’a pas l’autorisation `INTERNET`. Elle n’a ni compte, ni publicité, ni statistiques, ni rapport de plantage, ni SDK tiers qui collecte des données. Quand vous touchez un lien vers un store ou une page web, la politique de confidentialité de ce store ou de votre navigateur s’applique.

**Sauvegardes :** si la sauvegarde Android est activée, seuls vos **réglages** peuvent être inclus dans votre propre sauvegarde Google, et seulement si elle est chiffrée de bout en bout.

**Autorisations :** notifications (rappels facultatifs, désactivables à tout moment) ; démarrage, maintien en éveil et état du réseau (utilisés par WorkManager pour la vérification en Wi-Fi, sans envoi de données).

**Enfants et droits :** l’appli ne s’adresse pas aux enfants et ne collecte aucune donnée personnelle. Vider le stockage de l’appli ou la désinstaller supprime toutes ses données.

**Contact :** <https://github.com/Bosaj/android-update-checker/issues>

---

## العربية

<div dir="rtl">

يعرض «رادار التحديثات» التطبيقات التي لم تُحدَّث على هاتفك منذ مدة، ويفتح صفحة المتجر التي يمكنك تحديثها منها. وقد صُمِّم لتبقى بياناتك على هاتفك.

**ما يقرؤه التطبيق:** لكل تطبيق له أيقونة في الشاشة الرئيسية: اسمه، واسم حزمته، وإصداره، وأيقونته، وتاريخا أول تثبيت وآخر تحديث، والمتجر أو المُثبِّت الذي ثبّته. لا يستخدم التطبيق إذن `QUERY_ALL_PACKAGES`.

**ما يحفظه التطبيق:** على الهاتف فقط، في مساحته الخاصة: إعداداتك (الحدود، والتذكيرات، والتطبيقات المتجاهَلة، واللغة) والفحص السابق.

**ما يغادر الهاتف:** **لا شيء.** ليس للتطبيق إذن `INTERNET`، ولا حساب، ولا إعلانات، ولا إحصاءات، ولا تقارير أعطال، ولا مكتبات خارجية تجمع البيانات. عند فتح رابط متجر أو صفحة ويب تنطبق سياسة خصوصية ذلك المتجر أو المتصفح.

**النسخ الاحتياطي:** إذا كان النسخ الاحتياطي في أندرويد مفعّلًا، قد تُضمَّن **إعداداتك** فقط في نسختك الاحتياطية على Google، وبشرط أن تكون مشفّرة من طرف إلى طرف.

**الأذونات:** الإشعارات (تذكيرات اختيارية يمكن إيقافها في أي وقت)؛ والتشغيل عند الإقلاع وإبقاء الجهاز نشطًا وحالة الشبكة (يستخدمها WorkManager للفحص عبر الواي فاي، دون إرسال أي بيانات).

**الأطفال والحقوق:** التطبيق غير موجّه للأطفال ولا يجمع أي بيانات شخصية. مسح بيانات التطبيق أو إلغاء تثبيته يحذف كل بياناته.

**التواصل:** <https://github.com/Bosaj/android-update-checker/issues>

</div>
