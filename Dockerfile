FROM ubuntu:focal

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-8-jdk

RUN apt-get install -y --no-install-recommends \
    libwoff1 \
    libopus0 \
    libwebp6 \
    libwebpdemux2 \
    libenchant1c2a \
    libgudev-1.0-0 \
    libsecret-1-0 \
    libhyphen0 \
    libgdk-pixbuf2.0-0 \
    libegl1 \
    libnotify4 \
    libxslt1.1 \
    libevent-2.1-7 \
    libgles2 \
    libxcomposite1 \
    libatk1.0-0 \
    libatk-bridge2.0-0 \
    libepoxy0 \
    libgtk-3-0 \
    libharfbuzz-icu0

RUN apt-get install -y --no-install-recommends \
    libgstreamer-gl1.0-0 \
    libgstreamer-plugins-bad1.0-0 \
    gstreamer1.0-plugins-good \
    gstreamer1.0-libav

RUN apt-get install -y --no-install-recommends \
    libnss3 \
    libxss1 \
    libasound2 \
    fonts-noto-color-emoji \
    libxtst6

RUN apt-get install -y --no-install-recommends \
    libdbus-glib-1-2 \
    libxt6

RUN apt-get install -y --no-install-recommends \
    ffmpeg

RUN apt-get install -y --no-install-recommends \
    xvfb

RUN apt-get install -y --no-install-recommends \
    maven curl unzip && rm -rf /var/lib/apt/lists/*

COPY . /tmp/playwright/

RUN cd /tmp/playwright && mvn install -DskipTests && \
    sh scripts/download_driver.sh && \
    rm -rf /tmp/playwright 
