#!/bin/bash
# Installs the Android SDK in Claude Code cloud sessions so Gradle builds work.
# Gradle provisions the JDK 25 daemon itself (via api.foojay.io).
set -euo pipefail

if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
  exit 0
fi

SDK=/opt/android-sdk
CLT_ZIP=commandlinetools-linux-16111833_latest.zip

if [ ! -x "$SDK/cmdline-tools/latest/bin/sdkmanager" ]; then
  mkdir -p "$SDK/cmdline-tools"
  tmp=$(mktemp -d)
  curl -sSfL -o "$tmp/clt.zip" "https://dl.google.com/android/repository/$CLT_ZIP"
  unzip -q "$tmp/clt.zip" -d "$tmp"
  rm -rf "$SDK/cmdline-tools/latest"
  mv "$tmp/cmdline-tools" "$SDK/cmdline-tools/latest"
  rm -rf "$tmp"
fi

if [ ! -d "$SDK/platforms/android-37.0" ] || [ ! -d "$SDK/build-tools/37.0.0" ]; then
  yes | "$SDK/cmdline-tools/latest/bin/sdkmanager" --licenses >/dev/null 2>&1 || true
  "$SDK/cmdline-tools/latest/bin/sdkmanager" "platforms/android-37.0" "build-tools/37.0.0" "platform-tools" >/dev/null
fi

echo "sdk.dir=$SDK" > "$CLAUDE_PROJECT_DIR/local.properties"
if [ -n "${CLAUDE_ENV_FILE:-}" ]; then
  echo "export ANDROID_HOME=$SDK" >> "$CLAUDE_ENV_FILE"
fi
