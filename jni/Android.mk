
LOCAL_PATH := $(call my-dir)
LIBS_PATH := ffmpeg-1.0/android/armv7-a

include $(CLEAR_VARS)
LOCAL_MODULE := ffmpeg-prebuilt
LOCAL_SRC_FILES := $(LIBS_PATH)/libffmpeg.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/$(LIBS_PATH)/include
LOCAL_EXPORT_LDLIBS := $(LIBS_PATH)/libffmpeg.so
LOCAL_PRELINK_MODULE := true
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := ardrone2
LOCAL_SRC_FILES := ardrone2.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/$(LIBS_PATH)/include
#LOCAL_C_INCLUDES += /data/ws_android/libjpeg-turbo
LOCAL_LDLIBS := -L$(NDK_PLATFORMS_ROOT)/$(TARGET_PLATFORM)/arch-arm/usr/lib -L$(LOCAL_PATH) -L$(LOCAL_PATH)/$(LIBS_PATH)
LOCAL_LDLIBS += -llog -ljnigraphics -lz -ldl -lgcc $(LOCAL_PATH)/$(LIBS_PATH)/libffmpeg.so 
#LOCAL_LDLIBS += /data/ws_android/libjpeg-turbo/libs/armeabi/libjpeg.so
LOCAL_SHARED_LIBRARY := ffmpeg-prebuilt
include $(BUILD_SHARED_LIBRARY)
