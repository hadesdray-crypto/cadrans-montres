#!/usr/bin/env bash
# Execute DANS un appareil virtuel : fabrique l'appli et son parcours de test, les lance, ramene les captures d'ecran.
# Usage : tester-emulateur.sh <taille_police> <mode_sombre yes|no>
set -u
POLICE="${1:-1.0}"
SOMBRE="${2:-no}"
mkdir -p sortie
adb shell settings put system font_scale "$POLICE"
adb shell "cmd uimode night $SOMBRE" || true
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0
adb shell settings put global hide_error_dialogs 1
adb shell am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS >/dev/null 2>&1 || true
sleep 20

gradle assembleDebug assembleDebugAndroidTest --no-daemon --console=plain || { echo "ECHEC fabrication"; exit 1; }
adb install -r -g app/build/outputs/apk/debug/app-debug.apk || { echo "ECHEC installation"; exit 1; }
adb install -r -g app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk || { echo "ECHEC installation des tests"; exit 1; }
adb logcat -c
adb shell am instrument -w fr.cadrans.studio.test/androidx.test.runner.AndroidJUnitRunner > sortie/parcours.txt 2>&1
cat sortie/parcours.txt
adb logcat -d > sortie/logcat.txt
adb pull /sdcard/Android/data/fr.cadrans.studio/files/captures sortie/captures >/dev/null 2>&1 || echo "aucune capture trouvee"
ECHEC=0
if ! grep -q "^OK (" sortie/parcours.txt; then echo "ECHEC du parcours"; ECHEC=1; fi
if grep -E "FATAL EXCEPTION" sortie/logcat.txt | grep -q .; then echo "ECHEC : plantage dans le journal"; ECHEC=1; fi
exit $ECHEC