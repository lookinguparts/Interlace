package art.lookingup.util;

import art.lookingup.interlace.patterns.VShader;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL.GL_POINTS;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.*;
import static com.jogamp.opengl.GL2ES3.GL_RASTERIZER_DISCARD;

public class GLUtil {
  static public class VSGLContext {

    public VSGLContext(GL3 gl) {
      this.gl = gl;
    }

    public GL3 gl;
    private interface Buffer {
      int VERTEX = 0;
      int TBO = 1;
      int MAX = 2;
    }

    // Destination for transform feedback buffer when copied back from the GPU
    public FloatBuffer tfbBuffer;
    // Staging buffer for vertex data to be copied to the VBO on the GPU
    public FloatBuffer vertexBuffer;
    // Stores the buffer IDs for the buffer IDs allocated on the GPU.
    static public IntBuffer bufferNames = GLBuffers.newDirectIntBuffer(Buffer.MAX);
    // The shader's ID on the GPU.

    public int shaderProgramId = -1;
    public int fTimeLoc = -2;
    public int textureLoc = -3;
    public com.jogamp.opengl.util.texture.Texture glTexture;
    public double totalTime;
    public Map<String, Integer> paramLocations = new HashMap<String, Integer>();
    public LinkedHashMap<String, Float> scriptParams = new LinkedHashMap<String, Float>();

    public LX lx;
  }

  static public VSGLContext vsGLInit(LX lx, com.jogamp.opengl.util.texture.Texture glTexture, String scriptName) {
    return vsGLInit(lx, glTexture, scriptName, null);
  }

  // TODO(tracy): Load the TextureIO previous to this function.
  static public VSGLContext vsGLInit(LX lx, com.jogamp.opengl.util.texture.Texture glTexture, String scriptName,
                                     LinkedHashMap<String, Float> scriptParams) {

    VShader.initializeGLContext();
    GL3 gl = VShader.glDrawable.getGL().getGL3();

    VSGLContext vsGLContext = new VSGLContext(gl);

    if (scriptParams != null)
      vsGLContext.scriptParams = scriptParams;

    vsGLContext.lx = lx;
    LXPoint[] points = lx.getModel().points;
    float[] ledPositions = new float[points.length * 3];

    for (int i = 0; i < points.length; i++) {
      // Use the normalized u, v, w coordinates.
      ledPositions[i * 3] = points[i].xn;
      ledPositions[i * 3 + 1] = points[i].yn;
      ledPositions[i * 3 + 2] = points[i].zn;
    }

    vsGLContext.gl.getContext().makeCurrent();

    vsGLContext.vertexBuffer = GLBuffers.newDirectFloatBuffer(ledPositions);
    // This is just a destination, make it large enough to accept all the vertex data.  The vertex
    // shader always outputs the all the elements.  To return just some of the points, attach a
    // geometry shader and filter there.  You will also need to carry along the lxpoint index with
    // the vertex data in that scenario to match it up after the transform feedback.
    vsGLContext.tfbBuffer = GLBuffers.newDirectFloatBuffer(vsGLContext.vertexBuffer.capacity());

    gl.glGenBuffers(VSGLContext.Buffer.MAX, VSGLContext.bufferNames);

    if (glTexture != null) {
      vsGLContext.glTexture = glTexture;
      // Set texture parameters for sampling
      glTexture.bind(gl);
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
    }
    vsGLContext.gl.getContext().release();
    reloadShader(vsGLContext, scriptName);
    return vsGLContext;
  }


  // TODO(tracy): This is the shader reloader for textured shaders.  We will need to migrate to
  // new shader loading/compiling approach when we implement VTexShader.
  static public void reloadShader(VSGLContext spGLCtx, String shaderName) {

    spGLCtx.gl.getContext().makeCurrent();

    if (spGLCtx.shaderProgramId != -1)
      spGLCtx.gl.glDeleteProgram(spGLCtx.shaderProgramId);

    ShaderCode vertShader = ShaderCode.create(spGLCtx.gl, GL_VERTEX_SHADER, spGLCtx.getClass(), "shaders",
      null, shaderName, "vert", null, true);

    ShaderProgram shaderProgram = new ShaderProgram();
    shaderProgram.add(vertShader);
    shaderProgram.init(spGLCtx.gl);
    spGLCtx.shaderProgramId = shaderProgram.program();

    spGLCtx.gl.glTransformFeedbackVaryings(spGLCtx.shaderProgramId, 1, new String[]{"tPosition"}, GL_INTERLEAVED_ATTRIBS);
    shaderProgram.link(spGLCtx.gl, System.err);

    // Now, find uniform locations.
    spGLCtx.fTimeLoc = spGLCtx.gl.glGetUniformLocation(spGLCtx.shaderProgramId, "fTime");
    LX.log("Found fTimeLoc at: " + spGLCtx.fTimeLoc);

    for (String scriptParam : spGLCtx.scriptParams.keySet()) {
      int paramLoc = spGLCtx.gl.glGetUniformLocation(spGLCtx.shaderProgramId, scriptParam);
      spGLCtx.paramLocations.put(scriptParam, paramLoc);
      //LX.log("Found " + scriptParam + " at: " + paramLoc);
    }
    if (spGLCtx.glTexture != null) {
      spGLCtx.textureLoc = spGLCtx.gl.glGetUniformLocation(spGLCtx.shaderProgramId, "textureSampler");
      LX.log("Found textureSampler at location: " + spGLCtx.textureLoc);
    }
    spGLCtx.gl.getContext().release();
  }

  static public void glRun(LX lx, VSGLContext spGLCtx, double deltaMs, float speed) {
    glRun(lx, spGLCtx, deltaMs, speed, true);
  }

  static public void glUpdateTotalTime(VSGLContext vsGLCtx, double deltaMs) {
    vsGLCtx.totalTime += deltaMs / 1000.0;
  }

  static public void glRun(LX lx, VSGLContext vsGLCtx, double deltaMs, float speed, boolean incrementTime) {
    if (incrementTime)
      vsGLCtx.totalTime += deltaMs / 1000.0;

    vsGLCtx.gl.getContext().makeCurrent();
    vsGLCtx.gl.glBindBuffer(GL_ARRAY_BUFFER, VSGLContext.bufferNames.get(VSGLContext.Buffer.VERTEX));
    vsGLCtx.gl.glBufferData(GL_ARRAY_BUFFER, vsGLCtx.vertexBuffer.capacity() * Float.BYTES, vsGLCtx.vertexBuffer, GL_STATIC_DRAW);
    int inputAttrib = vsGLCtx.gl.glGetAttribLocation(vsGLCtx.shaderProgramId, "position");
    vsGLCtx.gl.glEnableVertexAttribArray(inputAttrib);
    vsGLCtx.gl.glVertexAttribPointer(inputAttrib, 3, GL_FLOAT, false, 0, 0);

    vsGLCtx.gl.glBindBuffer(GL_ARRAY_BUFFER, VSGLContext.bufferNames.get(VSGLContext.Buffer.TBO));
    vsGLCtx.gl.glBufferData(GL_ARRAY_BUFFER, vsGLCtx.tfbBuffer.capacity() * Float.BYTES, vsGLCtx.tfbBuffer, GL_STATIC_READ);
    vsGLCtx.gl.glBindBufferBase(GL_TRANSFORM_FEEDBACK_BUFFER, 0, VSGLContext.bufferNames.get(VSGLContext.Buffer.TBO));

    vsGLCtx.gl.glEnable(GL_RASTERIZER_DISCARD);
    vsGLCtx.gl.glUseProgram(vsGLCtx.shaderProgramId);

    vsGLCtx.gl.glUniform1f(vsGLCtx.fTimeLoc, speed * (float) vsGLCtx.totalTime);

    for (String paramName : vsGLCtx.scriptParams.keySet()) {
      vsGLCtx.gl.glUniform1f(vsGLCtx.paramLocations.get(paramName), vsGLCtx.scriptParams.get(paramName));
    }

    if (vsGLCtx.glTexture != null) {
      vsGLCtx.glTexture.enable(vsGLCtx.gl);
      vsGLCtx.glTexture.bind(vsGLCtx.gl);
      vsGLCtx.gl.glUniform1i(vsGLCtx.textureLoc, 0); // 0 is the texture unit
    }

    vsGLCtx.gl.glBeginTransformFeedback(GL_POINTS);
    {
      vsGLCtx.gl.glDrawArrays(GL_POINTS, 0, lx.getModel().points.length);
    }
    vsGLCtx.gl.glEndTransformFeedback();
    vsGLCtx.gl.glFlush();

    vsGLCtx.gl.glGetBufferSubData(GL_TRANSFORM_FEEDBACK_BUFFER, 0, vsGLCtx.tfbBuffer.capacity() * Float.BYTES, vsGLCtx.tfbBuffer);

    vsGLCtx.gl.glUseProgram(0);
    vsGLCtx.gl.glDisable(GL_RASTERIZER_DISCARD);

    vsGLCtx.gl.getContext().release();
  }

  static public void copyTFBufferToPoints(LX lx, int[] colors, VSGLContext vsGLCtx) {
    for (int i = 0; i < lx.getModel().points.length; i++) {
      colors[lx.getModel().points[i].index] = LXColor.rgbf(vsGLCtx.tfbBuffer.get(i * 3),
        vsGLCtx.tfbBuffer.get(i * 3 + 1),
        vsGLCtx.tfbBuffer.get(i * 3 + 2));
    }
  }

  static public void copyTFBufferToPoints(LX lx, int[] colors, VSGLContext vsGLCtx, LXColor.Blend blend) {
    vsGLCtx.gl.getContext().makeCurrent();
    for (int i = 0; i < lx.getModel().points.length; i++) {
      colors[lx.getModel().points[i].index] = LXColor.blend(colors[lx.getModel().points[i].index],
        LXColor.rgbf(vsGLCtx.tfbBuffer.get(i * 3), vsGLCtx.tfbBuffer.get(i * 3 + 1), vsGLCtx.tfbBuffer.get(i * 3 + 2)),
        blend);
    }
    vsGLCtx.gl.getContext().release();
  }

  //
  // The #include support is based on Titanic's End shader code.  I figured it would be better to be
  // compatible with whatever they are doing syntax-wise.  One slight difference is that we are using OpenGL 3
  // so that we can stay compatible with older Raspberry Pi's but the interface is the same for compiling shaders
  // and linking programs so there is effectively no difference.
  // TODO(tracy): I need to add some intermediate stateful static inner class here to properly track line numbers
  // across includes.  Also, failure modes should be more smoothed out with respect to Chromatik UI experience.
  // https://github.com/titanicsend/LXStudio-TE
  //
  //

  static public String loadFile(String shaderDir, String shader) {
    try {
      return new String(Files.readAllBytes(Paths.get(shaderDir + File.separator + shader)));
    } catch (IOException e) {
      LX.log("Error loading shader: " + shader + " from " + shaderDir + " : " + e.getMessage());
      return "";
    }
  }

  static public String loadShader(String shaderDir, String shaderFile)
    throws Exception {
    String shaderBody = loadFile(shaderDir, shaderFile);
    return preprocessShader(shaderDir, shaderBody);
  }

  static public String preprocessShader(String shaderDir, String shaderBody)
    throws Exception {
    int MAX_INCLUDE_DEPTH = 10;
    try {
      int depth = 0;
      while (true) {
        if (depth >= MAX_INCLUDE_DEPTH) {
          throw new RuntimeException("Exceeded maximum #include depth of " + MAX_INCLUDE_DEPTH);
        }
        String expandedShaderBody = expandIncludes(shaderDir, shaderBody);
        depth++;
        if (expandedShaderBody == null) {
          break;
        }
        shaderBody = expandedShaderBody;
      }
    } catch (Exception e) {
      throw new Exception("Shader Preprocessor Error. " + e.getMessage());
    }
    return shaderBody;
  }


  // Expand #include statements in the shader code.  Handles nested includes
  // up to MAX_INCLUDE_DEPTH (defaults to 10 levels.)
  static public String expandIncludes(String shaderDir, String input) throws IOException {
    boolean foundInclude = false;
    int lineCount = 0;

    StringBuilder output = new StringBuilder();
    BufferedReader reader = new BufferedReader(new StringReader(input));
    String line;
    while ((line = reader.readLine()) != null) {
      lineCount++;
      if (line.startsWith("#include")) {
        foundInclude = true;
        try {
          String filename = getFileName(shaderDir, line.substring("#include ".length(), line.length()));
          BufferedReader fileReader = new BufferedReader(new FileReader(filename));
          String fileLine;

          // restart line counter for include file
          output.append("#line 1 \n");
          while ((fileLine = fileReader.readLine()) != null) {
            output.append(fileLine).append("\n");
          }
          fileReader.close();
        } catch (Exception e) {
          throw new IOException("Line " + lineCount + " : " + line + "\n" + e.getMessage());
        }

        // reset line counter to main file count
        output.append("#line ").append(lineCount + 1).append("\n");
      } else {
        output.append(line).append("\n");
      }
    }
    reader.close();
    if (foundInclude) return output.toString();
    else return null;
  }

  private static String stringCleanup(String str) {
    // clean up delimiters
    if (str.startsWith("\"") && str.endsWith("\"")) {
      str = str.substring(1, str.length() - 1);
    }
    return str.trim();
  }

  // Convert an input token to a valid filename, removing any delimiters and
  // checking to see that the file actually exists.
  private static String getFileName(String shaderPath, String fName) {
    fName = stringCleanup(fName);

    // if name is enclosed in angle brackets, prefix with default resource path
    // to save repetitive typing
    if (fName.startsWith("<") && fName.endsWith(">")) {
      fName = fName.substring(1, fName.length() - 1);
      fName = shaderPath + File.separator + fName;
      // cleanup again in case there were spaces or more quotes
      fName = stringCleanup(fName);
    }

    // check to see if the file actually exists
    File f = new File(fName);
    if (!f.exists()) {
      throw new IllegalArgumentException("File " + fName + " not found.");
    }
    return fName;
  }

  public static int createShader(GL3 gl3, int programId, String shaderCode, int shaderType)
    throws Exception {
    int shaderId = gl3.glCreateShader(shaderType);
    if (shaderId == 0) {
      throw new Exception("Error creating shader. Shader id is zero.");
    }
    gl3.glShaderSource(shaderId, 1, new String[] {shaderCode}, null);
    gl3.glCompileShader(shaderId);
    validateStatus(gl3, shaderId, GL3.GL_COMPILE_STATUS);
    gl3.glAttachShader(programId, shaderId);
    return shaderId;
  }
  public static void link(GL3 gl3, int programId) {
    gl3.glLinkProgram(programId);
    // This is having issues on Mac with just vertex shaders.
    ///validateStatus(gl3, programId, GL3.GL_LINK_STATUS);

    gl3.glValidateProgram(programId);
    // This is having issues on Mac with just vertex shaders.
    //validateStatus(gl3, programId, GL3.GL_VALIDATE_STATUS);
  }


  private static void validateStatus(GL3 gl3, int id, int statusConstant) {
    boolean isShaderStatus = statusConstant == GL3.GL_COMPILE_STATUS;
    IntBuffer intBuffer = IntBuffer.allocate(1);
    if (isShaderStatus) {
      gl3.glGetShaderiv(id, statusConstant, intBuffer);
    } else {
      gl3.glGetProgramiv(id, statusConstant, intBuffer);
    }

    if (intBuffer.get(0) != 1) {
      if (isShaderStatus) {
        gl3.glGetShaderiv(id, GL3.GL_INFO_LOG_LENGTH, intBuffer);
      } else {
        gl3.glGetProgramiv(id, GL3.GL_INFO_LOG_LENGTH, intBuffer);
      }
      int size = intBuffer.get(0);
      String errorMessage = "";
      if (size > 0) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        if (isShaderStatus) {
          gl3.glGetShaderInfoLog(id, size, intBuffer, byteBuffer);
        } else {
          gl3.glGetProgramInfoLog(id, size, intBuffer, byteBuffer);
        }
        errorMessage = new String(byteBuffer.array());
      }
      LX.log("Error validating shader: " + errorMessage);
      throw new RuntimeException("Error producing shader!\n" + errorMessage);
    }
  }

  static public String shaderDir(LX lx) {
    return lx.getMediaPath() + File.separator + "VShader";
  }
}
