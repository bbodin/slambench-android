
ROOT_DIR=$(shell pwd)



apk : local.properties dependencies/OpenNI dependencies/OpenNI2 dependencies/SensorKinect dependencies/libopencl-stub dependencies/slambench ${ROOT_DIR}/app/include/TooN ${ROOT_DIR}/app/include/CL
	./gradlew assembleDebug --stacktrace



dep : local.properties dependencies/OpenNI dependencies/OpenNI2 dependencies/SensorKinect dependencies/libopencl-stub dependencies/slambench ${ROOT_DIR}/app/include/TooN ${ROOT_DIR}/app/include/CL

studio :
	wget https://dl.google.com/dl/android/studio/ide-zips/2.1.2.0/android-studio-ide-143.2915827-linux.zip
	unzip android-studio-ide-143.2915827-linux.zip
	mkdir -p ~/.local/Android/
	mv android-studio ~/.local/Android/android-studio
	~/.local/Android/android-studio/bin/studio.sh

sdk :
	wget https://dl.google.com/android/android-sdk_r24.4.1-linux.tgz
	tar xzf android-sdk_r24.4.1-linux.tgz
	mv android-sdk-linux ~/.local/Android/Sdk
	~/.local/Android/Sdk/tools/android list sdk --all
	~/.local/Android/Sdk/tools/android update sdk -u -a -t "platform-tools,tools,android-22,build-tools-22.0.1,extra-google-m2repository,addon-google_apis-google-22"

ndk :
	wget http://dl.google.com/android/repository/android-ndk-r10e-linux-x86_64.zip
	unzip android-ndk-r10e-linux-x86_64.zip
	mv android-ndk-r10e ~/.local/Android/android-ndk-r10e

prop :
	@echo "sdk.dir=/home/`whoami`/.local/Android/Sdk" > ./local.properties
	@echo "ndk.dir=/home/`whoami`/.local/Android/Ndk" >> ./local.properties


local.properties :
	@echo "********************************************************************"
	@echo "The $@ is not found. Please produce this file. It should look like :"
	@echo "  sdk.dir=/home/`whoami`/.local/Android/Sdk"
	@echo "  ndk.dir=/home/`whoami`/.local/Android/Ndk"
	@echo "********************************************************************"
	@false


${ROOT_DIR}/app/include/CL:
	mkdir -p dependencies
	git clone https://github.com/KhronosGroup/OpenCL-Headers/ dependencies/OpenCL-Headers/
	mkdir ${ROOT_DIR}/app/include/CL/ && cd dependencies/OpenCL-Headers/ && cp *.h ${ROOT_DIR}/app/include/CL/
	rm dependencies/OpenCL-Headers/ -rf

${ROOT_DIR}/app/include/TooN: 
	mkdir -p dependencies
	git clone https://github.com/edrosten/TooN.git dependencies/TooN
	cd dependencies/TooN &&  git checkout 92241416d2a4874fd2334e08a5d417dfea6a1a3f
	cd dependencies/TooN && ./configure --prefix=${ROOT_DIR}/app/ --disable-lapack --enable-typeof=typeof && make install
	rm dependencies/TooN -rf
	rm ${ROOT_DIR}/app/lib -rf || true

dependencies/libopencl-stub : 
	mkdir -p dependencies
	git clone https://github.com/krrishnarraj/libopencl-stub.git $@
	cd $@  && git checkout b4f84459e3a3a14d6a18b5dabe0a6ae9cbef709e
	cd $@  && git apply ${ROOT_DIR}/patchs/libopencl-stub_SLAMBench.patch

dependencies/OpenNI :
	mkdir -p dependencies
	git clone https://github.com/OpenNI/OpenNI.git $@
	cd $@ && git checkout 1c1367c7863ebd61d95813d021a56df996eca039
	cd $@ && git apply ${ROOT_DIR}/patchs/OpenNI_SLAMBench.patch

dependencies/OpenNI2 :
	mkdir -p dependencies
	git clone https://github.com/OpenNI/OpenNI2.git $@
	cd $@ && git checkout 115cf06c6efea32304182d293eca16ca883c9150
	cd $@ && git apply  ${ROOT_DIR}/patchs/OpenNI2_SLAMBench.patch

dependencies/SensorKinect :
	mkdir -p dependencies
	git clone https://github.com/avin2/SensorKinect.git $@
	cd $@ && git checkout faf4994fceba82e6fbd3dad16f79e4399be0c184
	cd $@ && git apply  ${ROOT_DIR}/patchs/SensorKinect_SLAMBench.patch

dependencies/slambench : 
	mkdir -p dependencies
	git clone https://github.com/pamela-project/slambench.git $@
	cd $@ && git checkout android


clean :
	rm -rf dependencies build
	rm -rf .gradle/ .idea/ app/build/ app/include/CL/ app/include/TooN/	app/lib/
	rm -rf local.properties projectFilesBackup/ slambench-android.iml
