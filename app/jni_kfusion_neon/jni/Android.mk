
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
ROOT_DIR=${LOCAL_PATH}/../../../
PEACH_DIRECTORY=${ROOT_DIR}/dependencies/slambench/

MORE_INCLUDE=${ROOT_DIR}/app/include


LOCAL_CFLAGS  += -O3
LOCAL_LDFLAGS += -O3

LOCAL_MODULE     :=  kfusion-neon
LOCAL_SRC_FILES  :=  $(PEACH_DIRECTORY)/kfusion/src/neon/kernels.cpp jnikfusionneon.cpp
LOCAL_LDLIBS     :=  -llog -landroid -lGLESv2  -lGLESv1_CM

ifeq ($(TARGET_ARCH),arm64)
    LOCAL_CFLAGS += -DHAVE_NEON=1
else
ifeq ($(TARGET_ARCH),arm)
LOCAL_ARM_NEON   := true
else
LOCAL_ARM_NEON   := true
endif
endif
LOCAL_C_INCLUDES :=  $(PEACH_DIRECTORY)/kfusion/src/neon/   $(PEACH_DIRECTORY)/kfusion/include/  $(MORE_INCLUDE)

include $(BUILD_SHARED_LIBRARY)
