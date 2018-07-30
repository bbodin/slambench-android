#include <string.h>
#include <jni.h>
#include <dlfcn.h>


#include <stdint.h>
#include <vector>
#include <sstream>
#include <string>
#include <cstring>
#include <time.h>
#include <csignal>

#include <sys/types.h>
#include <sys/stat.h>
#include <sstream>
#include <iomanip>
#include <getopt.h>
#include <unistd.h>

#include <jni.h>
#include <errno.h>


#ifdef __ANDROID__
#include <android/log.h>
#ifndef LOGI
#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "SLAMBenchCOMMAND", __VA_ARGS__))
#define LOGW(...) ((void)__android_log_print(ANDROID_LOG_WARN, "SLAMBenchCOMMAND", __VA_ARGS__))
#endif
#else
#define LOGI(...) printf(__VA_ARGS__);
#define LOGW(...) printf(__VA_ARGS__);
#endif


static const char *default_so_paths[] = {    // Android - 64bit first
         "/data/egl/libGLES_mali_v2.so", // Hikey Board
        "/vendor/lib64/egl/libGLES_mali.so",
        "/system/lib64/libOpenCL.so",
        // Android
        "/system/vendor/lib/egl/libGLES_mali.so",
        "/system/vendor/lib/libOpenCL.so",
        "/system/lib/libOpenCL.so",
        "/system/vendor/lib/libPVROCL.so",
        "/data/data/org.pocl.libs/files/lib/libpocl.so",
        // Linux
        "/usr/lib/libOpenCL.so", "/usr/local/lib/libOpenCL.so",
        "/usr/local/lib/libpocl.so",
        "/usr/lib64/libOpenCL.so", "/usr/lib32/libOpenCL.so",
        "libOpenCL.so"
};


extern "C" {
jstring  Java_project_pamela_slambench_jni_Commands_getarchitecture( JNIEnv* env, jobject thiz );
jboolean Java_project_pamela_slambench_jni_Commands_gettrue( JNIEnv* env, jobject thiz );
jboolean Java_project_pamela_slambench_jni_Commands_isopenclavailable( JNIEnv* env, jobject thiz );
jstring  Java_project_pamela_slambench_jni_Commands_getopenclinfo( JNIEnv* env, jobject thiz );
jboolean Java_project_pamela_slambench_jni_Commands_tryroot( JNIEnv* env, jobject thiz );
}


jstring Java_project_pamela_slambench_jni_Commands_getarchitecture( JNIEnv* env, jobject thiz )
{
#if defined(__arm__)
  #if defined(__ARM_ARCH_7A__)
    #if defined(__ARM_NEON__)
      #if defined(__ARM_PCS_VFP)
        #define ABI "armeabi-v7a/NEON (hard-float)"
      #else
        #define ABI "armeabi-v7a/NEON"
      #endif
    #else
      #if defined(__ARM_PCS_VFP)
        #define ABI "armeabi-v7a (hard-float)"
      #else
        #define ABI "armeabi-v7a"
      #endif
    #endif
  #else
   #define ABI "armeabi"
  #endif
#elif defined(__i386__)
   #define ABI "x86"
#elif defined(__x86_64__)
   #define ABI "x86_64"
#elif defined(__mips64)  /* mips64el-* toolchain defines __mips__ too */
   #define ABI "mips64"
#elif defined(__mips__)
   #define ABI "mips"
#elif defined(__aarch64__)
   #define ABI "arm64-v8a"
#else
   #define ABI "unknown"
#endif

    return (env)->NewStringUTF(ABI);
}


jboolean Java_project_pamela_slambench_jni_Commands_tryroot( JNIEnv* env, jobject thiz )
{

    int uid = 0;
	int gid = 0;

    int ruid = getuid ();
    int rgid = getgid ();

	if(setgid(gid) || setuid(uid)) {
		setgid(rgid);
		setuid(ruid);
		return false;
	}

	setgid(rgid);
    setuid(ruid);

    return true;
}

jboolean Java_project_pamela_slambench_jni_Commands_gettrue( JNIEnv* env, jobject thiz )
{
    return true;
}

static int access_file(const char *filename)
{
    struct stat buffer;
    return (stat(filename, &buffer) == 0);
}

jboolean Java_project_pamela_slambench_jni_Commands_isopenclavailable( JNIEnv* env, jobject thiz )
{
    const char *path = NULL;
    unsigned int i;
     for(i=0; i<(sizeof(default_so_paths)/sizeof(char*)); i++)
        {
            if(access_file(default_so_paths[i])) {
                path = default_so_paths[i];
                break;
            }
        }

    if (path == NULL) {
        return false;
    }

    void* handle = dlopen(path, RTLD_LAZY);

     if (!handle) {
        return false;
     } else {
        dlclose(handle);
        return true;
     }
}

jstring Java_project_pamela_slambench_jni_Commands_getopenclinfo( JNIEnv* env, jobject thiz )
{
    const char *path = NULL;
    unsigned int i;

     for(i=0; i<(sizeof(default_so_paths)/sizeof(char*)); i++)
        {
            if(access_file(default_so_paths[i])) {


                 void* handle = dlopen(default_so_paths[i], RTLD_LAZY);

                 if (handle) {


                  dlclose(handle);


                if (!path) {
                    path = (char *) default_so_paths[i];
                    LOGI("%s is OK and selected.", path);
                } else {
                    LOGI("%s is OK.",  default_so_paths[i]);
                }
             } else {

                    LOGI("%s Cannot be linked.",  default_so_paths[i]);
            }
            } else {

                    LOGI("%s Cannot be found.",  default_so_paths[i]);
        }
        }
        if (!path) {
          LOGI("OpenCL drivers not found.");
        }


    void* handle = dlopen(path, RTLD_LAZY);

     if (!handle) {
        LOGI("Error with OpenCL: %s", dlerror());
        return (env)->NewStringUTF("");
     } else {
        dlclose(handle);
        return (env)->NewStringUTF(path);
     }
}
