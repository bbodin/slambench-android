LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

ROOT_DIR=${LOCAL_PATH}/../../../

DEPENDENCIES=$(ROOT_DIR)/dependencies/

PEACH_DIRECTORY=$(ROOT_DIR)/dependencies/slambench/


MORE_INCLUDE=$(ROOT_DIR)/app/include

LOCAL_CFLAGS  += -O3
LOCAL_LDFLAGS += -O3

LOCAL_MODULE := kfusion-opencl

ifeq ($(TARGET_ARCH),arm64)
OPENCL_LIB_DIR:=$(DEPENDENCIES)/libopencl-stub/obj/local/arm64-v8a/
else
ifeq ($(TARGET_ARCH),arm)
OPENCL_LIB_DIR:=$(DEPENDENCIES)/libopencl-stub/obj/local/armeabi/
else
OPENCL_LIB_DIR:=$(DEPENDENCIES)/libopencl-stub/obj/local/$(TARGET_ARCH)/
endif
endif

ifeq ($(TARGET_ARCH),arm)

OPENNI_INCLUDE=$(DEPENDENCIES)/OpenNI/Include/
OPENNI2_INCLUDE=$(DEPENDENCIES)/OpenNI2/Include/
OPENNI_LIB_DIR=$(ROOT_DIR)/app/build/libs/armeabi-v7a/
OPENNI2_LIB_DIR=$(DEPENDENCIES)/OpenNI2/Packaging/AndroidBuild/libs/armeabi

LOCAL_CFLAGS  +=   -DDO_OPENNI -DDO_OPENNI15
LOCAL_LDFLAGS +=   -DDO_OPENNI -DDO_OPENNI15

LOCAL_SRC_FILES   :=  $(PEACH_DIRECTORY)/kfusion/src/opencl/kernels.cpp $(PEACH_DIRECTORY)/kfusion/src/opencl/common_opencl.cpp jnikfusionocl.cpp
LOCAL_C_INCLUDES  := $(PEACH_DIRECTORY)/kfusion/thirdparty/ $(PEACH_DIRECTORY)/kfusion/include/  $(MORE_INCLUDE)  $(OPENNI_INCLUDE) $(OPENNI2_INCLUDE)
LOCAL_LDLIBS      :=   -L${OPENNI_LIB_DIR} -L${OPENNI2_LIB_DIR} -L$(OPENCL_LIB_DIR)  -llog -landroid -lGLESv2  -lGLESv1_CM -lOpenCL -lOpenNI -lOpenNI2 -lusb -fuse-ld=bfd


else

ifeq ($(TARGET_ARCH),arm64)

OPENNI_INCLUDE=$(DEPENDENCIES)/OpenNI/Include/
OPENNI2_INCLUDE=$(DEPENDENCIES)/OpenNI2/Include/
OPENNI_LIB_DIR=$(ROOT_DIR)/app/build/libs/arm64-v8a/
OPENNI2_LIB_DIR=$(DEPENDENCIES)/OpenNI2/Packaging/AndroidBuild/libs/arm64-v8a/
LOCAL_CFLAGS  +=   -DDO_OPENNI -DDO_OPENNI15
LOCAL_LDFLAGS +=   -DDO_OPENNI -DDO_OPENNI15

LOCAL_SRC_FILES   :=  $(PEACH_DIRECTORY)/kfusion/src/opencl/kernels.cpp $(PEACH_DIRECTORY)/kfusion/src/opencl/common_opencl.cpp jnikfusionocl.cpp
LOCAL_C_INCLUDES  :=  $(PEACH_DIRECTORY)/kfusion/thirdparty/ $(PEACH_DIRECTORY)/kfusion/include/  $(MORE_INCLUDE)  $(OPENNI_INCLUDE) $(OPENNI2_INCLUDE)
LOCAL_LDLIBS      :=   -L${OPENNI_LIB_DIR} -L${OPENNI2_LIB_DIR} -L$(OPENCL_LIB_DIR)  -llog -landroid -lGLESv2  -lGLESv1_CM -lOpenCL -lOpenNI -lOpenNI2 -lusb -fuse-ld=bfd


else

OPENNI_LIB_DIR=$(ROOT_DIR)/app/build/libs/$(TARGET_ARCH)/
OPENNI2_LIB_DIR=$(DEPENDENCIES)/OpenNI2/Packaging/AndroidBuild/libs/$(TARGET_ARCH)/

LOCAL_SRC_FILES   :=  $(PEACH_DIRECTORY)/kfusion/src/opencl/kernels.cpp $(PEACH_DIRECTORY)/kfusion/src/opencl/common_opencl.cpp jnikfusionocl.cpp
LOCAL_C_INCLUDES  :=  $(PEACH_DIRECTORY)/kfusion/thirdparty/ $(PEACH_DIRECTORY)/kfusion/include/  $(MORE_INCLUDE)
LOCAL_LDLIBS      :=  -L$(OPENCL_LIB_DIR)  -llog -landroid -lGLESv2  -lGLESv1_CM -lOpenCL

endif

endif

include $(BUILD_SHARED_LIBRARY)
