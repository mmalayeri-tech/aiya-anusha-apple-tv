# Installing on the Xiaomi TV box (iPhone + remote only)

## One-time setup on the TV
1. Install **Downloader by AFTVnews** from the Play Store on the TV.
2. **Settings → System → About**: press OK on **Android TV OS build** 7 times to turn on developer mode.
3. **Settings → Apps → Security & restrictions → Unknown sources**: turn it on for **Downloader**.

If you did this for the LCARS launcher app, it's already set up.

## Get the APK onto the TV
**Option A, Send Files to TV (simplest, no GitHub):** install **Send Files to TV** on the TV and on the iPhone.
Save `AiyaAnushaAppleTV-debug.apk` to the iPhone's Files app, send it to the TV, and open it there.

**Option B, GitHub release + Downloader:** push this folder to a public GitHub repo and publish a release.
The workflow attaches `AiyaAnushaAppleTV-debug.apk`. Type that release link into Downloader
(use the Google TV app on the iPhone as a keyboard).

Choose **Install**, then **Done**. The app shows up in **Your apps** as *Aiya Anusha Apple TV*.

## After installing
- **AirPlay mirror** → **Install AirScreen** (one time) → **Start**. On the iPhone: Control Center → Screen Mirroring.
- **Send from iPhone** → scan the QR code with the iPhone camera. The iPhone must be on the same Wi-Fi.
- If the Apple TV app is available for your box in the Play Store, **Apple services → Get app** installs it.
  Apple TV+ then opens in the real app instead of the web view.
