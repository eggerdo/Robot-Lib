PLATFORM=$NDK/platforms/android-8/arch-arm/
PREBUILT=$NDK/toolchains/arm-linux-androideabi-4.4.3/prebuilt/linux-x86
function build_r8b_test
{
bash configure --target-os=linux \
--prefix=$PREFIX \
--enable-cross-compile \
--extra-libs="-lgcc" \
--arch=arm \
--cc=$PREBUILT/bin/arm-linux-androideabi-gcc \
--cross-prefix=$PREBUILT/bin/arm-linux-androideabi- \
--nm=$PREBUILT/bin/arm-linux-androideabi-nm \
--sysroot=$PLATFORM \
--extra-cflags=" -O3 -fpic -DANDROID -DHAVE_SYS_UIO_H=1 -Dipv6mr_interface=ipv6mr_ifindex -fasm -Wno-psabi -fno-short-enums -fno-strict-aliasing -finline-limit=300 $OPTIMIZE_CFLAGS " \
--extra-cxxflags="-malign-double" \
--disable-shared \
--enable-static \
--extra-ldflags="-Wl,-rpath-link=$PLATFORM/usr/lib -L$PLATFORM/usr/lib -nostdlib -lc -lm -ldl -llog" \
--disable-everything \
--enable-demuxer=mov \
--enable-demuxer=h264 \
--disable-ffplay \
--enable-protocol=file \
--enable-avformat \
--enable-avcodec \
--enable-decoder=rawvideo \
--enable-decoder=mjpeg \
--enable-decoder=h263 \
--enable-decoder=mpeg4 \
--enable-decoder=h264 \
--enable-parser=h264 \
--enable-encoder=mpeg4 \
--enable-encoder=mpeg \
--enable-network \
--enable-protocol=rtp \
--enable-protocol=http \
--enable-protocol=tcp \
--enable-protocol=udp \
--enable-zlib \
--disable-avfilter \
--disable-avdevice \
--disable-fast-unaligned \
$ADDITIONAL_CONFIGURE_FLAG
sed -i 's/HAVE_LRINT 0/HAVE_LRINT 1/g' config.h
sed -i 's/HAVE_LRINTF 0/HAVE_LRINTF 1/g' config.h
sed -i 's/HAVE_ROUND 0/HAVE_ROUND 1/g' config.h
sed -i 's/HAVE_ROUNDF 0/HAVE_ROUNDF 1/g' config.h
sed -i 's/HAVE_TRUNC 0/HAVE_TRUNC 1/g' config.h
sed -i 's/HAVE_TRUNCF 0/HAVE_TRUNCF 1/g' config.h
make clean
make  -j4 install
$PREBUILT/bin/arm-linux-androideabi-ar d libavcodec/libavcodec.a inverse.o
$PREBUILT/bin/arm-linux-androideabi-ld -rpath-link=$PLATFORM/usr/lib -L$PLATFORM/usr/lib  -soname libffmpeg.so -shared -nostdlib  -z,noexecstack -Bsymbolic --whole-archive --no-undefined -o $PREFIX/libffmpeg.so libavcodec/libavcodec.a libavformat/libavformat.a libavutil/libavutil.a libswscale/libswscale.a -lc -lm -lz -ldl -llog  --warn-once  --dynamic-linker=/system/bin/linker $PREBUILT/lib/gcc/arm-linux-androideabi/4.4.3/libgcc.a
}

#arm v7vfpv3
CPU=armv7-a
OPTIMIZE_CFLAGS="-mfloat-abi=softfp -mfpu=vfpv3-d16 -marm -march=$CPU "
PREFIX=./android/$CPU
ADDITIONAL_CONFIGURE_FLAG=
build_r8b_test
