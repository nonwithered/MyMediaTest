#!/bin/bash

ANDROID_NDK_ROOT=$ANDROID_NDK_HOME
PREBUILT=$ANDROID_NDK_ROOT/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64
PLATFORM=$ANDROID_NDK_ROOT/platforms/android-26/arch-arm

mkdir build
cd build || exit

../submodule/FFmpeg/configure \
--prefix=./ffmpeg \
--cross-prefix="$PREBUILT"/bin/arm-linux-androideabi- \
--enable-cross-compile \
--arch=arm64 \
--target-os=linux \
--sysroot="$PLATFORM" \
--enable-static \
--disable-shared \
--disable-ffmpeg \
--disable-ffplay \
--disable-ffprobe \
--disable-stripping \
--disable-avdevice \
--disable-devices \
--disable-indevs \
--disable-outdevs \
--disable-debug \
--disable-asm \
--disable-yasm \
--disable-doc \
--enable-small \
--enable-dct \
--enable-dwt \
--enable-lsp \
--enable-mdct \
--enable-rdft \
--enable-fft \
--enable-version3 \
--enable-nonfree \
--disable-filters \
--disable-postproc \
--disable-bsfs \
--enable-bsf=aac_adtstoasc \
--enable-bsf=h264_mp4toannexb \
--disable-encoders \
--enable-encoder=pcm_s16le \
--enable-encoder=aac \
--enable-encoder=libvo_aacenc \
--disable-decoders \
--enable-decoder=aac \
--enable-decoder=mp3 \
--enable-decoder=pcm_s16le \
--disable-parsers \
--enable-parser=aac \
--disable-muxers \
--enable-muxer=flv \
--enable-muxer=wav \
--enable-muxer=adts \
--disable-demuxers \
--enable-demuxer=flv \
--enable-demuxer=wav \
--enable-demuxer=aac \
--disable-protocols \
--enable-protocol=rtmp \
--enable-protocol=file \
--disable-libfdk_aac \
--disable-libx264 \
--extra-cflags="-marm -march=armv7-a" \
--extra-ldflags="-marm -march=armv7-a" \

cd ..
