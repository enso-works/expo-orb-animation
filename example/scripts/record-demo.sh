#!/usr/bin/env bash
# Records a showcase scene from a booted simulator/emulator and turns it into a README GIF.
#
#   EXPO_PUBLIC_SHOWCASE=orb npx expo start -c      # in another terminal, open the app
#   scripts/record-demo.sh ios orb                  # once the scene is on screen
#
# Needs ffmpeg and gifski (brew install ffmpeg gifski). Writes ../docs/<scene>-<platform>.gif.
set -euo pipefail

PLATFORM=${1:?usage: record-demo.sh <ios|android> <orb|intervals>}
SCENE=${2:?usage: record-demo.sh <ios|android> <orb|intervals>}
ADB=${ADB:-$HOME/Library/Android/sdk/platform-tools/adb}
OUT_DIR="$(cd "$(dirname "$0")/../../docs" && pwd)"
WORK=$(mktemp -d)
WIDTH=${WIDTH:-400}
FPS=${FPS:-20}

# Loop length of each scene in Showcase.tsx: three 4.6 s orb themes, one 7 s interval round.
# The scenes repeat forever, so two periods always contain one clean loop.
case "$SCENE" in
  orb) PERIOD=13.8 ;;
  intervals) PERIOD=7 ;;
  *) echo "unknown scene: $SCENE" >&2; exit 1 ;;
esac
RECORD_SECONDS=$(echo "($PERIOD * 2 + 3) / 1" | bc)

record() {
  if [ "$PLATFORM" = ios ]; then
    local raw="$WORK/raw.mp4"
    xcrun simctl io booted recordVideo --codec=h264 --force "$raw" >/dev/null 2>&1 &
    local pid=$!
    sleep "$RECORD_SECONDS"
    kill -INT "$pid"
    wait "$pid" || true
  else
    # Emulator encoders reject the full 1440x3120 and fall back to a squashed 720x1280,
    # and drop frames at large sizes, so record at half the native size.
    local raw="$WORK/raw.mp4" size
    size=$("$ADB" shell wm size | awk -F'[ x]' '/Physical/ { printf "%dx%d", $3 / 4 * 2, $4 / 4 * 2 }')
    "$ADB" shell screenrecord --size "$size" --bit-rate 16000000 --time-limit "$RECORD_SECONDS" /sdcard/showcase.mp4
    "$ADB" pull /sdcard/showcase.mp4 "$raw" >/dev/null
    "$ADB" shell rm /sdcard/showcase.mp4
  fi
  echo "$raw"
}

RAW=$(record)

# Square crop from the vertical centre, where the view sits.
CROP="crop=iw:iw:0:(ih-iw)/2"

# Loop between two dark frames one period apart: the orb's fade between themes,
# or the interval blob at its smallest. Simulators drop and bunch frames under
# load, so find the second point near where it should be instead of trusting
# the clock, then stretch the clip back to its real length.
read -r START LOOP < <(ffmpeg -v error -i "$RAW" \
  -vf "$CROP,scale=120:-1,signalstats,metadata=print:key=lavfi.signalstats.YAVG:file=-" -f null - 2>/dev/null |
  awk -v period="$PERIOD" '
    /pts_time/ { split($0, a, "pts_time:"); t = a[2]; if (t0 == "") t0 = t; t -= t0 }
    /YAVG/ { split($0, b, "="); n++; ts[n] = t; ys[n] = b[2] }
    END {
      for (i = 1; i <= n; i++) if (ts[i] >= 1 && ts[i] < 1 + period && (s == "" || ys[i] < ys[s])) s = i
      for (i = 1; i <= n; i++) if (ts[i] > ts[s] + period * 0.8 && ts[i] < ts[s] + period * 1.2 && (e == "" || ys[i] < ys[e])) e = i
      print ts[s], ts[e] - ts[s]
    }')

FRAMES="$WORK/frames"
mkdir -p "$FRAMES"
ffmpeg -v error -ss "$START" -t "$LOOP" -i "$RAW" \
  -vf "$CROP,setpts=(PTS-STARTPTS)*$PERIOD/$LOOP,fps=$FPS,scale=$WIDTH:-1:flags=lanczos" "$FRAMES/%04d.png"

OUT="$OUT_DIR/$SCENE-$PLATFORM.gif"
gifski --quiet --fps "$FPS" --quality "${QUALITY:-70}" --width "$WIDTH" -o "$OUT" "$FRAMES"/*.png
echo "$OUT ($(du -h "$OUT" | cut -f1), ${LOOP}s loop from ${START}s)"
rm -rf "$WORK"
