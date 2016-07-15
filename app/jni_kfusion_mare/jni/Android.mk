
LOCAL_PATH := $(call my-dir)
ROOT_DIR=${LOCAL_PATH}/../../../
MARE_VERSION := 1.0.2
MARE_LIB_TYPE := release-cpu
MARE_DIR := $(HOME)/Qualcomm/MARE/$(MARE_VERSION)/arm-linux-androideabi
include ${MARE_DIR}/lib/MARE.mk

PEACH_DIRECTORY=${ROOT_DIR}/dependencies/slambench/
MORE_INCLUDE=${ROOT_DIR}/app/include

include $(CLEAR_VARS)

LOCAL_ARM_MODE := arm
LOCAL_ARM_NEON := true
LOCAL_CPP_FEATURES := rtti exceptions
LOCAL_SHARED_LIBRARIES := libmare


LOCAL_CFLAGS  += -O3 -fopenmp
LOCAL_LDFLAGS += -O3 -fopenmp

LOCAL_MODULE     :=  kfusion-mare
LOCAL_SRC_FILES  :=  $(PEACH_DIRECTORY)/kfusion/src/cpp/kernels.cpp jnikfusionmare.cpp
LOCAL_C_INCLUDES :=  $(PEACH_DIRECTORY)/kfusion/include/  $(PEACH_DIRECTORY)/kfusion/thirdparty/  $(MORE_INCLUDE)
LOCAL_LDLIBS     :=  -L${OPENNI_LIB_DIR} -L${OPENNI2_LIB_DIR} -llog -landroid -lGLESv2  -lGLESv1_CM

include $(BUILD_SHARED_LIBRARY)
