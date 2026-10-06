#!/usr/bin/env bash
# Captures README screenshots of the sample app on a running emulator.
# CI has no stylus and adb cannot draw with pressure, so the sample opens each screen from the `scene` extra with
# pre-recorded, deterministic ink (a cursive signature and a damage sketch), so every capture is the same.
# Every capture is checked for the expected text and for a blank image.
#
#   bash scripts/screenshots.sh tablet   # pixel_tablet in landscape: the sketch pad with its tools, light and dark
#   bash scripts/screenshots.sh phone    # pixel_7: the signed delivery form and the export preview, light and dark
set -euo pipefail
source "$(dirname "$0")/screenshot-lib.sh"

device="${1:-phone}"

# The text each scene must show; the capture fails without it.
expected_text() {
  case "$1" in
    signature) echo "Delivery confirmation" ;;
    sketch) echo "Damage report" ;;
    export) echo "Export preview" ;;
  esac
}

suffix() {
  if [ "$1" = dark ]; then echo "-dark"; else echo ""; fi
}

install_sample
if [ "$device" = tablet ]; then
  ensure_landscape
  for mode in light dark; do
    set_night_mode "$mode"
    fresh_launch --es scene sketch
    capture "tablet-sketch$(suffix "$mode")" "$(expected_text sketch)"
  done
else
  for mode in light dark; do
    set_night_mode "$mode"
    for scene in signature export; do
      fresh_launch --es scene "$scene"
      capture "phone-$scene$(suffix "$mode")" "$(expected_text "$scene")"
    done
  done
fi
