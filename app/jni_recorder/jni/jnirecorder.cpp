
#include <string.h>
#include <jni.h>
#include <dlfcn.h>

#include <kernels.h>
#include <interface.h>
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

#define SSTR( x ) static_cast< std::ostringstream & >( \
        ( std::ostringstream() << std::dec << x ) ).str()


#ifdef __ANDROID__
#include <android/log.h>
#ifndef LOGI
#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "SLAMBenchRECORDER", __VA_ARGS__))
#define LOGW(...) ((void)__android_log_print(ANDROID_LOG_WARN, "SLAMBenchRECORDER", __VA_ARGS__))
#endif
#else
#define LOGI(...) printf(__VA_ARGS__);
#define LOGW(...) printf(__VA_ARGS__);
#endif






FILE* pFile;

uint2 computationSize;
uint2 inputSize;

uint16_t *inputDepth;
uchar3 *raw_rgb;
uchar4 *depthRender;
uchar4 *trackRender;
uchar4 *volumeRender;

DepthReader *reader;
float4 camera;
float3 init_pose;
uint frame = 0;
Configuration *global_config;

double timings[7];

#include <mygltest.h>

extern "C"
{
  JNIEXPORT jboolean JNICALL
    Java_project_pamela_slambench_jni_KFusion_recorderinit (JNIEnv * env,
							      jobject thiz,
							      jobjectArray
							      filename);
  JNIEXPORT jstring JNICALL
    Java_project_pamela_slambench_jni_KFusion_recorderprocess (JNIEnv *
								 env);
  JNIEXPORT jboolean JNICALL
    Java_project_pamela_slambench_jni_KFusion_recorderrelease (JNIEnv *
								 env);

  JNIEXPORT void JNICALL
    Java_project_pamela_slambench_jni_KFusion_recorderglinit (JNIEnv *);
  JNIEXPORT void JNICALL
    Java_project_pamela_slambench_jni_KFusion_recorderglresize (JNIEnv *,
								  jint width,
								  jint
								  height);
  JNIEXPORT void JNICALL
    Java_project_pamela_slambench_jni_KFusion_recorderglrender (JNIEnv *);

          JNIEXPORT jstring JNICALL
            Java_project_pamela_slambench_jni_KFusion_recorderinfo (JNIEnv *
        								 env);

}




JNIEXPORT jstring JNICALL
Java_project_pamela_slambench_jni_KFusion_recorderinfo (JNIEnv * env)
{

      std::ostringstream o;
      o << "Start infos" << std::endl;
      return  (env)->NewStringUTF(o.str().c_str());

}

JNIEXPORT void JNICALL
Java_project_pamela_slambench_jni_KFusion_recorderglrender (JNIEnv *)
{
  Draw ();

}


JNIEXPORT void JNICALL
Java_project_pamela_slambench_jni_KFusion_recorderglinit (JNIEnv *)
{

  Init ();

}

JNIEXPORT void JNICALL
Java_project_pamela_slambench_jni_KFusion_recorderglresize (JNIEnv *,
							      jint width,
							      jint height)
{

  Resize ();

}


JNIEXPORT jboolean JNICALL
Java_project_pamela_slambench_jni_KFusion_recorderrelease (JNIEnv * env)
{

  LOGI ("Start cpp mem release");
  if (raw_rgb)
    free (raw_rgb);
    raw_rgb = NULL;
  if (inputDepth)
    free (inputDepth);
  inputDepth = NULL;
  if (depthRender)
    free (depthRender);
  depthRender = NULL;
  if (trackRender)
    free (trackRender);
  trackRender = NULL;
  if (volumeRender)
    free (volumeRender);
  volumeRender = NULL;
  if (reader)
    delete (reader);
    reader = NULL;
  if (global_config)
    free (global_config);
    global_config = NULL;

  fclose(pFile);


  LOGI ("Finish record mem release");
  return true;

}

JNIEXPORT jstring JNICALL
Java_project_pamela_slambench_jni_KFusion_recorderprocess (JNIEnv * env)
{

  std::ostringstream o;
  Configuration & config = *global_config;

  if (reader->readNextDepthFrame (NULL,inputDepth))
    {

      frame++;

      o << "\rCurrent frame :" << frame << "     " << std::flush;

      int total = 0;

      total += fwrite(&(inputSize), sizeof(inputSize), 1, pFile);
      total += fwrite(inputDepth, sizeof(uint16_t), inputSize.x * inputSize.y,
                      pFile);
      total += fwrite(&(inputSize), sizeof(inputSize), 1, pFile);
      total += fwrite(raw_rgb, sizeof(uchar3), inputSize.x * inputSize.y,
                      pFile);

        LOGI ("No more frame\n");

         const float nearPlane = 0.4f;
        const float farPlane = 4.0f;
    	float rangeScale = 1 / (farPlane - nearPlane);

        for (int y = 0; y < inputSize.y; y++) {
		    int rowOffeset = y * inputSize.x;
		    for (unsigned int x = 0; x < inputSize.x; x++) {

			    unsigned int pos = rowOffeset + x;

			    if (inputDepth[pos]/1000.0f < nearPlane)
                    depthRender[pos] = make_uchar4(255, 255, 255, 0); // The forth value is a padding in order to align memory
		    	else {
				    if (inputDepth[pos]/1000.0f > farPlane)
                        depthRender[pos] = make_uchar4(0, 0, 0, 0); // The forth value is a padding in order to align memory
				    else {
				    	const float d = (inputDepth[pos]/1000.0f - nearPlane) * rangeScale;
                        depthRender[pos] = gs2rgb(d);
			    	}
			}
		}
	}


      return  (env)->NewStringUTF(o.str().c_str());

    }
  else
    {
      o << "\rLast frame :" << frame << "     " << std::flush;
      LOGI ("No more frame\n");






      return  (env)->NewStringUTF(o.str().c_str());

    }
}

std::vector < char *>
stringArrayJ2C (JNIEnv * env, jobjectArray array)
{
  std::vector < char *>vec;
  jsize stringCount = (*env).GetArrayLength (array);

  for (int i = 0; i < stringCount; i++)
    {
      jstring string = (jstring) (*env).GetObjectArrayElement (array, i);
      vec.push_back ((char *) (*env).GetStringUTFChars (string, NULL));
    }
  return vec;
}


jboolean
Java_project_pamela_slambench_jni_KFusion_recorderinit (JNIEnv * env,
							  jobject thiz,
							  jobjectArray
							  stringArray)
{
  LOGI ("Start android_main");

  std::vector < char *>vec = stringArrayJ2C (env, stringArray);
  for (unsigned int i = 0; i < vec.size (); i++)
    {
      LOGI ("Load argument %s", (vec[i]));
    }
  global_config = new Configuration (vec.size (), (char **) &(vec[(0)]));

  Configuration & config = *global_config;
  frame = 0;

  LOGI ("Configuration ready");
  // ========= CHECK ARGS =====================

  assert (config.compute_size_ratio > 0);
  assert (config.integration_rate > 0);
  assert (config.volume_size.x > 0);
  assert (config.volume_resolution.x > 0);

  LOGI ("Configuration checked");

  // ========= READER INITIALIZATION  =========

  if (is_file (config.input_file))
    {

      LOGI ("RAW File found\n");
      reader = new RawDepthReader (config.input_file, config.fps,
				   config.blocking_read);

    }
  else
    {
      LOGI ("try OpenNI2 drivers\n");
               reader = new OpenNIDepthReader("",config.fps,config.blocking_read);
       if(!(reader->cameraOpen)) {
           LOGI ("try OpenNI15 drivers\n");
          //This is for openni from a camera
          delete reader;
          reader = new OpenNI15DepthReader("",config.fps,config.blocking_read);
          }

      if(!(reader->cameraOpen)) {
	    LOGI("generation of OpenNI15 and OpeNI2 readers failed\n");
	    delete reader;
	    delete global_config;
	    reader=NULL;
	    return false;
      }
    }

  std::cout.precision (10);
  std::cerr.precision (10);

  init_pose = config.initial_pos_factor * config.volume_size;
  inputSize = reader->getinputSize ();
  std::cerr << "input Size is = " << inputSize.x << "," << inputSize.y
    << std::endl;


  LOGI ("Start memory allocation\n");


  //  =========  BASIC PARAMETERS  (input size / computation size )  =========

  computationSize = make_uint2 (inputSize.x,
				inputSize.y );
  camera = reader->getK () / config.compute_size_ratio;

  if (config.camera_overrided)
    camera = config.camera / config.compute_size_ratio;

  LOGI("CAMERA CPP= %f %f %f %f." , camera.x, camera.y,camera.z,camera.w);

  //  =========  BASIC BUFFERS  (input / output )  =========

  // Construction Scene reader and input buffer
  inputDepth =
    (uint16_t *) malloc (sizeof (uint16_t) * inputSize.x * inputSize.y);
  raw_rgb = (uchar3 *) malloc (sizeof (uchar3) * inputSize.x * inputSize.y);
  depthRender =
    (uchar4 *) malloc (sizeof (uchar4) * computationSize.x *
		       computationSize.y);
  trackRender =
    (uchar4 *) malloc (sizeof (uchar4) * computationSize.x *
		       computationSize.y);
  volumeRender =
    (uchar4 *) malloc (sizeof (uchar4) * computationSize.x *
		       computationSize.y);

  std::string target;
  int count = 0;
  target = "/sdcard/output" + SSTR(count) + ".raw";


  while ( access( target.c_str(), F_OK ) != -1)  {
    LOGI ("Try target %s", target.c_str()) ;
    count++;
    target = "/sdcard/output" + SSTR(count) + ".raw";
  }

  LOGI ("Target is %s", target.c_str()) ;

  pFile = fopen(target.c_str(), "wb");

  if (!pFile) {
    LOGI ("File opening failed.") ;
    return false;
  }

  return true;
}

