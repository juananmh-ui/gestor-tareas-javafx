#!/bin/sh
set -eu

MAVEN_VERSION="3.9.11"
PROJECT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
MAVEN_DIR="$PROJECT_DIR/.mvn/apache-maven-$MAVEN_VERSION"
MAVEN_ARCHIVE="$PROJECT_DIR/.mvn/apache-maven-$MAVEN_VERSION-bin.tar.gz"
MAVEN_URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$MAVEN_VERSION/apache-maven-$MAVEN_VERSION-bin.tar.gz"
MAVEN_SHA512="bcfe4fe305c962ace56ac7b5fc7a08b87d5abd8b7e89027ab251069faebee516b0ded8961445d6d91ec1985dfe30f8153268843c89aa392733d1a3ec956c9978"
MAVEN_MARKER="$MAVEN_DIR/.distribution-sha512"

if [ -z "${JAVA_HOME:-}" ] || [ ! -x "$JAVA_HOME/bin/javac" ]; then
    if [ "$(uname -s)" = "Darwin" ] && [ -x /usr/libexec/java_home ]; then
        JAVA_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null || /usr/libexec/java_home)
    else
        JAVAC_PATH=$(command -v javac 2>/dev/null || true)
        if [ -z "$JAVAC_PATH" ]; then
            echo "ERROR: Se necesita un JDK 21 o posterior y no se ha encontrado javac." >&2
            exit 1
        fi
        JAVAC_PATH=$(readlink -f "$JAVAC_PATH" 2>/dev/null || printf '%s' "$JAVAC_PATH")
        JAVA_HOME=$(dirname "$(dirname "$JAVAC_PATH")")
    fi
    export JAVA_HOME
fi

JAVA_VERSION=$("$JAVA_HOME/bin/javac" -version 2>&1 | awk '{print $2}')
JAVA_MAJOR=${JAVA_VERSION%%.*}
if [ "$JAVA_MAJOR" = "1" ]; then
    JAVA_REST=${JAVA_VERSION#*.}
    JAVA_MAJOR=${JAVA_REST%%.*}
fi
case "$JAVA_MAJOR" in
    ''|*[!0-9]*)
        echo "ERROR: No se pudo determinar la versión del JDK." >&2
        exit 1
        ;;
esac
if [ "$JAVA_MAJOR" -lt 21 ]; then
    echo "ERROR: Se necesita JDK 21 o posterior; se ha encontrado $JAVA_VERSION." >&2
    exit 1
fi

CACHED_SHA512=""
if [ -f "$MAVEN_MARKER" ]; then
    CACHED_SHA512=$(cat "$MAVEN_MARKER")
fi

if [ ! -x "$MAVEN_DIR/bin/mvn" ] || [ "$CACHED_SHA512" != "$MAVEN_SHA512" ]; then
    echo "Preparando Apache Maven $MAVEN_VERSION..."
    mkdir -p "$PROJECT_DIR/.mvn"
    rm -rf -- "$MAVEN_DIR"

    if command -v curl >/dev/null 2>&1; then
        curl --fail --location --silent --show-error "$MAVEN_URL" --output "$MAVEN_ARCHIVE"
    elif command -v wget >/dev/null 2>&1; then
        wget -q "$MAVEN_URL" -O "$MAVEN_ARCHIVE"
    else
        echo "ERROR: Se necesita curl o wget para preparar Maven." >&2
        exit 1
    fi

    if command -v sha512sum >/dev/null 2>&1; then
        ACTUAL=$(sha512sum "$MAVEN_ARCHIVE" | awk '{print $1}')
    elif command -v shasum >/dev/null 2>&1; then
        ACTUAL=$(shasum -a 512 "$MAVEN_ARCHIVE" | awk '{print $1}')
    else
        echo "ERROR: Se necesita sha512sum o shasum para verificar Maven." >&2
        exit 1
    fi

    if [ "$ACTUAL" != "$MAVEN_SHA512" ]; then
        echo "ERROR: La suma SHA-512 de Maven no coincide." >&2
        exit 1
    fi

    tar -xzf "$MAVEN_ARCHIVE" -C "$PROJECT_DIR/.mvn"
    printf '%s' "$ACTUAL" > "$MAVEN_MARKER"
    rm -f "$MAVEN_ARCHIVE"
fi

exec "$MAVEN_DIR/bin/mvn" "$@"
