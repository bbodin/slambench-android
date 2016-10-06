
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

/* Considered available :
 inputSize uint2
 raw_rgb      uchar3*       inputSize.x, inputSize.y
 trackRender  uchar3*       computationSize.x, computationSize.y
 depthRender  uchar3*       computationSize.x, computationSize.y
 volumeRender uchar3*       computationSize.x, computationSize.y
*/

// Shader sources

const GLchar* vertexSource = "#version 100\n"
    "attribute vec2 position;\n"
    "attribute vec2 texcoord;\n"
    "varying vec2 v_texCoord;\n"
    "void main() {\n"
    "   gl_Position = vec4(position, 0.0, 1.0);\n"
    "   v_texCoord = texcoord;\n"
    "}\n";

const GLchar* fragmentSource =  "#version 100\n"
    "precision mediump float;\n"
    "varying vec2 v_texCoord;\n"
    "uniform sampler2D s_texture;\n"
    "void main() {\n"
    "   gl_FragColor = texture2D( s_texture, v_texCoord );\n"
    "}\n";

GLuint shaderProgram;
GLuint textureId[4];
GLuint trackRenderTexture    = 0;
GLuint RGBRenderTexture      = 1;
GLuint depthRenderTexture    = 2;
GLuint VolumeRenderTexture   = 3;


static bool init_done = false;

static bool DeleteTextures() {
  glBindTexture ( GL_TEXTURE_2D, 0);
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
  glDeleteTextures(4, &textureId[0]);
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
  return true;
}
static bool CreateTextures( )
{
    glGenTextures(4, &textureId[0]);
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}

   // Use tightly packed data
   glPixelStorei ( GL_UNPACK_ALIGNMENT, 1 );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}

   // Bind the texture object
   glBindTexture ( GL_TEXTURE_2D, textureId[RGBRenderTexture] );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}

   // Load the texture
   glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
   glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
   glTexParameteri(  GL_TEXTURE_2D, GL_TEXTURE_WRAP_S,  GL_CLAMP_TO_EDGE);
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
   glTexParameteri(  GL_TEXTURE_2D, GL_TEXTURE_WRAP_T,  GL_CLAMP_TO_EDGE);
   if (raw_rgb) {
      glTexImage2D ( GL_TEXTURE_2D, 0, GL_RGB, inputSize.x, inputSize.y, 0, GL_RGB, GL_UNSIGNED_BYTE, raw_rgb );
      if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
    }

   // Bind the texture object
   glBindTexture ( GL_TEXTURE_2D, textureId[trackRenderTexture] );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}

   // Load the texture
   glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
   glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
   glTexParameteri(  GL_TEXTURE_2D, GL_TEXTURE_WRAP_S,  GL_CLAMP_TO_EDGE);
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
   glTexParameteri(  GL_TEXTURE_2D, GL_TEXTURE_WRAP_T,  GL_CLAMP_TO_EDGE);
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
   if (trackRender) {
      glTexImage2D ( GL_TEXTURE_2D, 0, GL_RGBA, computationSize.x, computationSize.y, 0, GL_RGBA, GL_UNSIGNED_BYTE, trackRender );
      if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
    }

   // Bind the texture object
   glBindTexture ( GL_TEXTURE_2D, textureId[depthRenderTexture] );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}

   // Load the texture
   glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
   glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
   glTexParameteri(  GL_TEXTURE_2D, GL_TEXTURE_WRAP_S,  GL_CLAMP_TO_EDGE);
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
   glTexParameteri(  GL_TEXTURE_2D, GL_TEXTURE_WRAP_T,  GL_CLAMP_TO_EDGE);
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
   if (depthRender) {
      glTexImage2D ( GL_TEXTURE_2D, 0, GL_RGBA, computationSize.x, computationSize.y, 0, GL_RGBA, GL_UNSIGNED_BYTE, depthRender );
      if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
    }

       // Bind the texture object
       glBindTexture ( GL_TEXTURE_2D, textureId[VolumeRenderTexture] );
     if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}

       // Load the texture
       glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
       glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
       glTexParameteri(  GL_TEXTURE_2D, GL_TEXTURE_WRAP_S,  GL_CLAMP_TO_EDGE);
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
       glTexParameteri(  GL_TEXTURE_2D, GL_TEXTURE_WRAP_T,  GL_CLAMP_TO_EDGE);
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
       if (volumeRender) {
          glTexImage2D ( GL_TEXTURE_2D, 0, GL_RGBA, computationSize.x, computationSize.y, 0, GL_RGBA, GL_UNSIGNED_BYTE, volumeRender );
         if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
        }
       glBindTexture ( GL_TEXTURE_2D, 0);
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
    return true;
}


GLuint vertexShader;
GLuint fragmentShader;

static int Init () {

   init_done = true;


 // Create and compile the vertex shader
    vertexShader = glCreateShader(GL_VERTEX_SHADER);
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
    glShaderSource(vertexShader, 1, &vertexSource, NULL);
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
    glCompileShader(vertexShader);
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}


 // Check the compile status
   GLint compiled;
   glGetShaderiv ( vertexShader, GL_COMPILE_STATUS, &compiled );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
   if ( !compiled )  {
      GLint infoLen = 0;
      glGetShaderiv ( vertexShader, GL_INFO_LOG_LENGTH, &infoLen );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}

      if ( infoLen > 1 ) {
         char* infoLog = (char*) malloc (sizeof(char) * infoLen );
         glGetShaderInfoLog ( vertexShader, infoLen, NULL, infoLog );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
	     LOGI("Error compiling shader:%s", infoLog );
         free ( infoLog );
      }
      glDeleteShader ( vertexShader );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
      return 1;
   }



    // Create and compile the fragment shader
    fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
    glShaderSource(fragmentShader, 1, &fragmentSource, NULL);
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
    glCompileShader(fragmentShader);
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}


     // Check the compile status
       glGetShaderiv ( fragmentShader, GL_COMPILE_STATUS, &compiled );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
       if ( !compiled )  {
          GLint infoLen = 0;
          glGetShaderiv ( fragmentShader, GL_INFO_LOG_LENGTH, &infoLen );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}

          if ( infoLen > 1 ) {
             char* infoLog = (char*) malloc (sizeof(char) * infoLen );
             glGetShaderInfoLog ( fragmentShader, infoLen, NULL, infoLog );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
    	     LOGI("Error compiling shader:%s", infoLog );
             free ( infoLog );
          }
          glDeleteShader ( fragmentShader );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
          return 1;
       }



    // Link the vertex and fragment shader into a shader program
    shaderProgram = glCreateProgram();
    if ( shaderProgram == 0 )
          return 1;

    glAttachShader(shaderProgram, vertexShader);
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
    glAttachShader(shaderProgram, fragmentShader);
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}

    glLinkProgram(shaderProgram);
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}

      // Check the link status
       glGetProgramiv ( shaderProgram, GL_LINK_STATUS, &compiled );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
       if ( !compiled )  {
          GLint infoLen = 0;
          glGetProgramiv ( shaderProgram, GL_INFO_LOG_LENGTH, &infoLen );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
          if ( infoLen > 1 ) {
             char* infoLog = (char*) malloc (sizeof(char) * infoLen );
             glGetProgramInfoLog ( shaderProgram, infoLen, NULL, infoLog );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
             LOGI("Error linking program:\n%s\n", infoLog );
             free ( infoLog );
          }
          glDeleteProgram ( shaderProgram );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return false;}
          return 1;
       }


    glClearColor ( 0.0f, 0.0f, 0.0f, 0.0f );

   return 0;



}


///
// Draw a triangle using the shader pair created in Init()
//

    static float vertices[] = {
    //  TOP-LEFT PICT
    //  Position  Texcoords
    -1.0f,  1.0f, 0.0f, 0.0f, // Top-left
     0.0f,  1.0f, 1.0f, 0.0f, // Top-center
     0.0f, -0.0f, 1.0f, 1.0f, // Mid-center
    -1.0f, -0.0f, 0.0f, 1.0f,  // Mid-left

    //  TOP-RIGHT PICT
    0.0f,  1.0f, 0.0f, 0.0f, // Top-center
    1.0f,  1.0f, 1.0f, 0.0f, // Top-right
    1.0f,  0.0f, 1.0f, 1.0f, // Mid-right
    0.0f, -0.0f, 0.0f, 1.0f, // Mid-center

    // BOTTOM-LEFT PICT
    -1.0f, -0.0f, 0.0f, 0.0f,  // Mid-left
     0.0f, -0.0f, 1.0f, 0.0f, // Mid-center
    0.0f, -1.0f, 1.0f, 1.0f, // Bottom-mid
    -1.0f, -1.0f, 0.0f, 1.0f,  // Bottom-left

    // BOTTOM-RIGHT PICT
    0.0f, -0.0f, 0.0f, 0.0f, // Mid-center
    1.0f,  0.0f, 1.0f, 0.0f, // Mid-right
    1.0f, -1.0f, 1.0f, 1.0f, // Bottom-right
    0.0f, -1.0f, 0.0f, 1.0f, // Bottom-mid

    };


     static GLushort indices1[] = { 0, 1, 2,  0, 2, 3 };
     static GLushort indices2[] = { 4, 5, 6,  4, 6, 7 };
     static GLushort indices3[] = { 8, 9,10,  8,10,11 };
     static GLushort indices4[] = {12,13,14, 12,14,15 };

static void Draw () {

    if (!init_done) Init();

    glClear ( GL_COLOR_BUFFER_BIT );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return;}
    glUseProgram(shaderProgram);


  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return;}

    GLint posAttrib = glGetAttribLocation(shaderProgram, "position");
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return;}
    glVertexAttribPointer(posAttrib, 2, GL_FLOAT, GL_FALSE,4*sizeof(float), vertices);
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return;}
    glEnableVertexAttribArray(posAttrib);
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return;}

    GLint texAttrib = glGetAttribLocation(shaderProgram, "texcoord");
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return;}
    glVertexAttribPointer(texAttrib, 2, GL_FLOAT, GL_FALSE,4*sizeof(float), &vertices[2]);
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return;}
    glEnableVertexAttribArray(texAttrib);
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return;}

    GLint samplerLoc = glGetUniformLocation (shaderProgram, "s_texture" );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return;}


     // Clear the screen to black
     glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return;}
     glClear(GL_COLOR_BUFFER_BIT);
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return;}

     if (!CreateTextures( )) {
        LOGI("OPENGL ERROR WITH TEXTURE");
        return;
     };



     glActiveTexture ( GL_TEXTURE0 );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return;}
     glBindTexture ( GL_TEXTURE_2D, textureId[RGBRenderTexture] );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return;}
     glUniform1i ( samplerLoc, 0 );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return;}

     glDrawElements ( GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, indices1 );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return;}

     glActiveTexture ( GL_TEXTURE0 );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return;}
          glBindTexture ( GL_TEXTURE_2D, textureId[trackRenderTexture] );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return;}
          glUniform1i ( samplerLoc, 0 );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return;}

     glDrawElements ( GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, indices2 );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return;}

          glActiveTexture ( GL_TEXTURE0 );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return;}
          glBindTexture ( GL_TEXTURE_2D, textureId[depthRenderTexture] );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return;}
          glUniform1i ( samplerLoc, 0 );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return;}

     glDrawElements ( GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, indices3 );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return;}

          glActiveTexture ( GL_TEXTURE0 );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return;}
          glBindTexture ( GL_TEXTURE_2D, textureId[VolumeRenderTexture] );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return;}
          glUniform1i ( samplerLoc, 0 );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return;}

     glDrawElements ( GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, indices4 );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return;}


     glBindTexture ( GL_TEXTURE_2D, 0 );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return;}

     if (!DeleteTextures()) {
        LOGI("Texture has not been release properly !");
     }

}

static int Resize() {

         glDeleteProgram ( shaderProgram );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return 0;}
         glDeleteShader  ( vertexShader );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return 0;}
         glDeleteShader  ( fragmentShader );
  if (glGetError() != GL_NO_ERROR) {LOGI("OPENGL ERROR");return 0;}

        init_done = false;
        return true;
}

