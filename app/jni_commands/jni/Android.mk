LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_CFLAGS  += -O3
LOCAL_LDFLAGS += -O3

LOCAL_MODULE    := jnicommands
LOCAL_SRC_FILES := jnicommands.cpp
LOCAL_C_INCLUDES:=
LOCAL_LDLIBS    := -llog -landroid

include $(BUILD_SHARED_LIBRARY)
