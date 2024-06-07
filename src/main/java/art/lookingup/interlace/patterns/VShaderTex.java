package art.lookingup.interlace.patterns;

import art.lookingup.util.EaseUtil;
import art.lookingup.util.GLUtil;
import art.lookingup.util.JavaUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import heronarts.glx.GLX;
import heronarts.glx.ui.component.UIButton;
import heronarts.glx.ui.component.UILabel;
import heronarts.glx.ui.vg.VGraphics;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.LXComponent;
import heronarts.lx.color.LXColor;
import heronarts.lx.command.LXCommand;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.MutableParameter;
import heronarts.lx.parameter.StringParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.studio.LXStudio;
import heronarts.lx.studio.ui.device.UIDevice;
import heronarts.lx.studio.ui.device.UIDeviceControls;
import heronarts.lx.utils.LXUtils;
import heronarts.glx.ui.UI2dContainer;
import heronarts.glx.ui.component.UISlider;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.List;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_POINTS;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.*;

/**
 * First attempt at using vertex shaders for volumetric rendering.
 */
@LXCategory(LXCategory.FORM)
public class VShaderTex extends LXPattern implements UIDeviceControls<VShaderTex> {
  public GL3 gl;

  StringParameter scriptName = new StringParameter("scriptName", "texture");
  StringParameter texName = new StringParameter("tName", "fractal5");
  CompoundParameter speed = new CompoundParameter("speed", 1f, 0f, 20f);

  // These parameters are loaded from the ISF Json declaration at the top of the shader
  LinkedHashMap<String, CompoundParameter> scriptParams = new LinkedHashMap<String, CompoundParameter>();
  // For each script based parameter, store the uniform location in the compiled shader.  We use this
  // to pass in the values for each frame.
  Map<String, Integer> paramLocations = new HashMap<String, Integer>();
  public final MutableParameter onReload = new MutableParameter("Reload");
  public final StringParameter error = new StringParameter("Error", null);
  private UIButton openButton;
  private UIButton texOpenButton;
  com.jogamp.opengl.util.texture.Texture glTexture;
  public int textureLoc = -3;

  public final int TEXTURE_SIZE = 512;

  public VShaderTex(LX lx) {
    super(lx);

    addParameter("scriptName", scriptName);
    addParameter("texName", texName);
    addParameter("speed", speed);


    VShader.initializeGLContext();

    texName.setValue("fractal5");
    reloadTexture(texName.getString());

    glInit();
  }

  private interface Buffer {
    int VERTEX = 0;
    int TBO = 1;
    int MAX = 2;
  }

  // Destination for transform feedback buffer when copied back from the GPU
  protected FloatBuffer tfbBuffer;
  // Staging buffer for vertex data to be copied to the VBO on the GPU
  protected FloatBuffer vertexBuffer;
  // Stores the buffer IDs for the buffer IDs allocated on the GPU.
  protected IntBuffer bufferNames = GLBuffers.newDirectIntBuffer(Buffer.MAX);
  // The shader's ID on the GPU.
  protected int shaderProgramId = -1;

  protected int fTimeLoc = -2;

  protected double totalTime = 0.0;

  protected JsonObject isfObj;

  protected EaseUtil easeUtil = new EaseUtil(0);

  float[] ledPositions;

  protected void updateLedPositions() {
    LXPoint[] points = model.points;
    float xMin = Float.MAX_VALUE;
    float xMax = Float.MIN_VALUE;
    float yMin = Float.MAX_VALUE;
    float yMax = Float.MIN_VALUE;
    float zMin = Float.MAX_VALUE;
    float zMax = Float.MIN_VALUE;
    for (LXPoint p : points) {
      xMin = Math.min(xMin, p.x);
      xMax = Math.max(xMax, p.x);
      yMin = Math.min(yMin, p.y);
      yMax = Math.max(yMax, p.y);
      zMin = Math.min(zMin, p.z);
      zMax = Math.max(zMax, p.z);
    }
    for (int i = 0; i < points.length; i++) {
      ledPositions[i * 3] = (points[i].x - xMin)/(xMax - xMin);
      ledPositions[i * 3 + 1] = (points[i].y - yMin)/(yMax - yMin);
      ledPositions[i * 3 + 2] = (points[i].z - zMin)/(zMax - zMin);
    }
  }

  /**
   * Allocate the CPU buffers for the input and the output.  Vertex setting should be moved to later if the
   * LXPoints are going to move around.
   * Also, reserve the OpenGL buffer IDs.
   * Load the shader, bind the output that feeds into the transform feedback buffer, and link the shader.
   */
  public void glInit() {
    LXPoint[] points = model.points;
    ledPositions = new float[points.length * 3];
    updateLedPositions();

    VShader.glDrawable.getContext().makeCurrent();
    gl = VShader.glDrawable.getGL().getGL3();
    vertexBuffer = GLBuffers.newDirectFloatBuffer(ledPositions);

    // This is just a destination, make it large enough to accept all the vertex data.  The vertex
    // shader always outputs the all the elements.  To return just some of the points, attach a
    // geometry shader and filter there.  You will also need to carry along the lxpoint index with
    // the vertex data in that scenario to match it up after the transform feedback.
    tfbBuffer = GLBuffers.newDirectFloatBuffer(vertexBuffer.capacity());
    if (glTexture != null) {
      // Set texture parameters for sampling
      glTexture.bind(gl);
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
    }
    gl.glGenBuffers(Buffer.MAX, bufferNames);
    VShader.glDrawable.getContext().release();

    reloadShader(scriptName.getString());
  }

  private List<String> newSliderKeys = new ArrayList<String>();
  private List<String> removeSliderKeys = new ArrayList<String>();

  public void reloadShader(String shaderName) {
    reloadShader(shaderName, true);
  }


  public void reloadShader(String shaderName, boolean clearSliders) {
    VShader.glDrawable.getContext().makeCurrent();
    if (shaderProgramId != -1)
      gl.glDeleteProgram(shaderProgramId);

    if (clearSliders) clearSliders();
    newSliderKeys.clear();
    removeSliderKeys.clear();

    shaderProgramId = gl.glCreateProgram();
    String shaderSource = "";

    try {
      shaderSource = GLUtil.loadShader(GLUtil.shaderDir(lx), shaderName + ".vert");
    } catch (Exception ex) {
      LX.log("Error loading shader: " + ex.getMessage());
    }

    //LX.log("Line: " + shaderSource);
    int endOfComment = shaderSource.indexOf("*/");
    int startOfComment = shaderSource.indexOf("/*");
    String jsonDef = shaderSource.substring(startOfComment + 2, endOfComment);
    //LX.log("JsonDef: " + jsonDef);
    isfObj = (JsonObject)new JsonParser().parse(jsonDef);
    JsonArray inputs = isfObj.getAsJsonArray("INPUTS");

    for (int k = 0; k < inputs.size(); k++) {
      JsonObject input = (JsonObject)inputs.get(k);
      String pName = input.get("NAME").getAsString();
      String pType = input.get("TYPE").getAsString(); // must be float for now
      float pDefault = input.get("DEFAULT").getAsFloat();
      float pMin = input.get("MIN").getAsFloat();
      float pMax =  input.get("MAX").getAsFloat();
      // Add the parameter
      if (clearSliders || (!clearSliders && !scriptParams.containsKey(pName))) {
        CompoundParameter cp = new CompoundParameter(pName, pDefault, pMin, pMax);
        scriptParams.put(pName, cp);
        addParameter(pName, cp);
        // LX.log("Found param: " + pName);
      }
      newSliderKeys.add(pName);
      // How to remove ones we haven't seen?
    }
    if (!clearSliders) {
      for (String key : scriptParams.keySet()) {
        if (!newSliderKeys.contains(key)) {
          removeSliderKeys.add(key);
        }
      }
      for (String key : removeSliderKeys) {
        removeParameter(key);
        scriptParams.remove(key);
      }
    }

    int shaderId = -1;
    try {
      shaderId = GLUtil.createShader(gl, shaderProgramId, shaderSource, GL_VERTEX_SHADER);
    } catch (Exception ex) {
      LX.log("Error creating shader: " + ex.getMessage());
    }

    // At this point, our only dependency is the integer shaderProgramId.
    gl.glTransformFeedbackVaryings(shaderProgramId, 1, new String[]{"tPosition"}, GL_INTERLEAVED_ATTRIBS);
    GLUtil.link(gl, shaderProgramId);

    /*
    Once the program is linked we can free the shaderId resources?
    https://stackoverflow.com/questions/9113154/proper-way-to-delete-glsl-shader
    Maybe there are some driver-specific surprises according to above?

    int vertexShaderId =
          createShader(gl4, programId, getVertexShaderTemplate(), GL4.GL_VERTEX_SHADER);
      int fragmentShaderId = createShader(gl4, programId, shaderBody, GL4.GL_FRAGMENT_SHADER);
      link(gl4, programId);

      // free native resources after link
      gl4.glDetachShader(programId, fragmentShaderId);
      gl4.glDetachShader(programId, vertexShaderId);
      gl4.glDeleteShader(fragmentShaderId);
      gl4.glDeleteShader(vertexShaderId);
     */

    // Now, find uniform locations.
    fTimeLoc = gl.glGetUniformLocation(shaderProgramId, "fTime");
    LX.log("Found fTimeLoc at: " + fTimeLoc);
    for (String scriptParam : scriptParams.keySet()) {
      int paramLoc = gl.glGetUniformLocation(shaderProgramId, scriptParam);
      paramLocations.put(scriptParam, paramLoc);
      //LX.log("Found " + scriptParam + " at: " + paramLoc);
    }
    if (glTexture != null) {
      textureLoc = gl.glGetUniformLocation(shaderProgramId, "textureSampler");
      LX.log("Found textureSampler at location: " + textureLoc);
    }
    VShader.glDrawable.getContext().release();
    // Notify the UI
    onReload.bang();
  }

  public void reloadTexture(String textureName) {
    LX.log("Loading texture: " + textureName);
    String texturesDir =  GLUtil.shaderDir(lx) + File.separator + "textures" + File.separator;
    BufferedImage textureImage = null;
    try {
      textureImage = ImageIO.read(new File(texturesDir + textureName + ".png"));
    } catch (IOException ioex) {
      LX.log("Error loading texture: " + textureName + " : " + ioex.getMessage());
    }
    /*
    if (textureImage != null && (textureImage.getWidth() != TEXTURE_SIZE || textureImage.getHeight() != TEXTURE_SIZE)) {
      textureImage = JavaUtil.resize(textureImage, TEXTURE_SIZE, TEXTURE_SIZE);
    }
     */

    VShader.glDrawable.getContext().makeCurrent();
    if (textureImage != null)
      glTexture = AWTTextureIO.newTexture(VShader.glDrawable.getGLProfile(), textureImage, false);

    if (glTexture != null && gl != null) {
      // Set texture parameters for sampling
      glTexture.bind(gl);
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
    }
    VShader.glDrawable.getContext().release();
  }


  @Override
  public void load(LX lx, JsonObject obj) {
    // Force-load the script name first so that slider parameter values can come after
    if (obj.has(LXComponent.KEY_PARAMETERS)) {
      JsonObject params = obj.getAsJsonObject(LXComponent.KEY_PARAMETERS);
      if (params.has("scriptName")) {
        this.scriptName.setValue(params.get("scriptName").getAsString());
      }
    }
    super.load(lx, obj);
  }

  /**
   * Run once per frame.  Copy the vertex data to the OpenGL buffer.
   * Tell OpenGL which buffer to use as the transform feedback buffer.
   * GL_RASTERIZER_DISCARD tells OpenGL to stop the pipeline after the vertex shader.
   *
   * @param deltaMs
   */
  public void glRun(double deltaMs) {
    totalTime += deltaMs/1000.0;
    VShader.glDrawable.getContext().makeCurrent();
    gl.glBindBuffer(GL_ARRAY_BUFFER, bufferNames.get(Buffer.VERTEX));
    gl.glBufferData(GL_ARRAY_BUFFER, vertexBuffer.capacity() * Float.BYTES, vertexBuffer, GL_STATIC_DRAW);
    int inputAttrib = gl.glGetAttribLocation(shaderProgramId, "position");
    gl.glEnableVertexAttribArray(inputAttrib);
    gl.glVertexAttribPointer(inputAttrib, 3, GL_FLOAT, false, 0, 0);

    gl.glBindBuffer(GL_ARRAY_BUFFER, bufferNames.get(Buffer.TBO));
    gl.glBufferData(GL_ARRAY_BUFFER, tfbBuffer.capacity() * Float.BYTES, tfbBuffer, GL_STATIC_READ);
    gl.glBindBufferBase(GL_TRANSFORM_FEEDBACK_BUFFER, 0, bufferNames.get(Buffer.TBO));

    gl.glEnable(GL_RASTERIZER_DISCARD);
    gl.glUseProgram(shaderProgramId);

    gl.glUniform1f(fTimeLoc, speed.getValuef() * (float)totalTime);
    for (String paramName : scriptParams.keySet()) {
      gl.glUniform1f(paramLocations.get(paramName), scriptParams.get(paramName).getValuef());
    }
    if (glTexture != null) {
      //logger.info("Attempting to bind the textureLoc to slot 0.");
      glTexture.enable(gl);
      glTexture.bind(gl);
      gl.glUniform1i(textureLoc, 0); // 0 is the texture unit
    }

    gl.glBeginTransformFeedback(GL_POINTS);
    {
      gl.glDrawArrays(GL_POINTS, 0, model.points.length);
    }
    gl.glEndTransformFeedback();
    gl.glFlush();

    gl.glGetBufferSubData(GL_TRANSFORM_FEEDBACK_BUFFER, 0, tfbBuffer.capacity() * Float.BYTES, tfbBuffer);

    gl.glUseProgram(0);
    gl.glDisable(GL_RASTERIZER_DISCARD);

    // For manually checking the first few output values in the transform feedback buffer.
    for (int i = 0; i < 21; i++) {
      //System.out.print(tfbBuffer.get(i) + ",");
    }
    //System.out.println();

    VShader.glDrawable.getContext().release();
  }

  @Override
  public void onParameterChanged(LXParameter p) {
    if (p == this.scriptName) {
      LX.log("scriptName parameter changed!");
      reloadShader(((StringParameter)p).getString());
    }
    if (p == this.texName) {
      LX.log("texture parameter changed!");
      reloadTexture(((StringParameter)p).getString());
    }
  }

  @Override
  public void onActive() {
    super.onActive();
    totalTime = 0f;
  }

  private void clearSliders() {
    for (String key : scriptParams.keySet()) {
      removeParameter(key);
    }
    scriptParams.clear();
  }

  public void run(double deltaMs) {
    glRun(deltaMs);
    LXPoint[] points = model.points;
    // TODO(tracy): At some low brightness threshold, we should introduce alpha transparency.
    // The alpha level should be scaled from 1 to 0 based on the range from 0 to threshold.
    // Hardcoding the threshold for now since it is some complicated UI work to fit it into
    // the dynamic parameter system.
    for (int i = 0; i < points.length; i++) {
      float red = tfbBuffer.get(i*3);
      float green = tfbBuffer.get(i*3 + 1);
      float blue = tfbBuffer.get(i*3 + 2);
      int color = LXColor.rgbf(red, green, blue);
      float bright = LXColor.luminosity(color)/100f;
      float threshold = 0.1f;
      if (bright < threshold) {
        float alpha = (bright/threshold);
        colors[points[i].index] = LXColor.rgba(LXColor.red(color),
          LXColor.green(color),
          LXColor.blue(color),
          (int)(255f * alpha));
      } else {
        colors[points[i].index] = color;
      }
    }
  }

  @Override
  public void buildDeviceControls(LXStudio.UI ui, UIDevice uiDevice, VShaderTex pattern) {
    final UILabel fileLabel = (UILabel)
      new UILabel(0, 0, 120, 18)
        .setLabel(pattern.scriptName.getString())
        .setBackgroundColor(LXColor.BLACK)
        .setBorderRounding(4)
        .setTextAlignment(VGraphics.Align.CENTER, VGraphics.Align.MIDDLE)
        .setTextOffset(0, -1)
        .addToContainer(uiDevice);

    pattern.scriptName.addListener(p -> {
      fileLabel.setLabel(pattern.scriptName.getString());
    });

    this.openButton = (UIButton) new UIButton(122, 0, 18, 18) {
      @Override
      public void onToggle(boolean on) {
        if (on) {
          ((GLX)lx).showOpenFileDialog(
            "Open Vertex Shader",
            "Vertex Shader",
            new String[] { "vert" },
            GLUtil.shaderDir(lx) + File.separator,
            (path) -> { onOpen(new File(path)); }
          );
        }
      }
    }
      .setIcon(ui.theme.iconOpen)
      .setMomentary(true)
      .setDescription("Open Shader")
      .addToContainer(uiDevice);


    // Texture selection:
    final UILabel texFileLabel = (UILabel)
      new UILabel(160, 0, 120, 18)
        .setLabel(pattern.texName.getString())
        .setBackgroundColor(LXColor.BLACK)
        .setBorderRounding(4)
        .setTextAlignment(VGraphics.Align.CENTER, VGraphics.Align.MIDDLE)
        .setTextOffset(0, -1)
        .addToContainer(uiDevice);

    pattern.texName.addListener(p -> {
      texFileLabel.setLabel(pattern.texName.getString());
    });

    this.texOpenButton = (UIButton) new UIButton(280, 0, 18, 18) {
      @Override
      public void onToggle(boolean on) {
        ((GLX)lx).showOpenFileDialog(
          "Open Texture",
          "PNG Texture",
          new String[] { "png" },
          GLUtil.shaderDir(lx) + File.separator + "textures" + File.separator,
          (path) -> { onOpenTexture(new File(path));}
        );
      }
    }
      .setIcon(ui.theme.iconOpen)
      .setMomentary(true)
      .setDescription("Open Texture")
      .addToContainer(uiDevice);


    final UIButton texResetButton = (UIButton) new UIButton(298, 0, 18, 18) {
      @Override
      public void onToggle(boolean on) {
        if (on) {
          lx.engine.addTask(() -> {
            LX.log("Reloading texture");
            reloadTexture(texName.getString());
          });
        }
      }
    }.setIcon(ui.theme.iconLoad)
      .setMomentary(true)
      .setDescription("Reload texture")
      .addToContainer(uiDevice);


    final UI2dContainer sliders = (UI2dContainer)
      UI2dContainer.newHorizontalContainer(uiDevice.getContentHeight() - 20, 2)
        .setPosition(0, 20)
        .addToContainer(uiDevice);

    final UILabel error = (UILabel)
      new UILabel(0, 20, uiDevice.getContentWidth(), uiDevice.getContentHeight() - 20)
        .setBreakLines(true)
        .setTextAlignment(VGraphics.Align.LEFT, VGraphics.Align.TOP)
        .addToContainer(uiDevice)
        .setVisible(false);

    // Add sliders to container on every reload
    pattern.onReload.addListener(p -> {
      sliders.removeAllChildren();
      new UISlider(UISlider.Direction.VERTICAL, 40, sliders.getContentHeight() - 14, speed)
        .addToContainer(sliders);
      for (CompoundParameter slider : pattern.scriptParams.values()) {
        new UISlider(UISlider.Direction.VERTICAL, 40, sliders.getContentHeight() - 14, slider)
          .addToContainer(sliders);
      }
      float contentWidth = LXUtils.maxf(320, sliders.getContentWidth());
      uiDevice.setContentWidth(contentWidth);
      //resetButton.setX(contentWidth - resetButton.getWidth());
      //this.openButton.setX(resetButton.getX() - 2 - this.openButton.getWidth());
      error.setWidth(contentWidth);
      fileLabel.setWidth(this.openButton.getX() - 2);
    }, true);

    pattern.error.addListener(p -> {
      String str = pattern.error.getString();
      boolean hasError = (str != null && !str.isEmpty());
      error.setLabel(hasError ? str : "");
      error.setVisible(hasError);
      sliders.setVisible(!hasError);
    }, true);

  }

  public void onOpen(final File openFile) {
    this.openButton.setActive(false);
    if (openFile != null) {
      LX lx = getLX();
      String baseFilename = openFile.getName().substring(0, openFile.getName().indexOf('.'));
      LX.log("Loading: " + baseFilename);

      lx.engine.addTask(() -> {
        LX.log("Running script name setting task");
        lx.command.perform(new LXCommand.Parameter.SetString(
          scriptName,
          baseFilename
        ));
      });
    }
  }
  public void onOpenTexture(final File openFile) {
    this.texOpenButton.setActive(false);
    if (openFile != null) {
      LX lx = getLX();
      String baseFilename = openFile.getName().substring(0, openFile.getName().indexOf('.'));
      LX.log("Loading texture: " + baseFilename);

      lx.engine.addTask(() -> {
        LX.log("Running texture name setting task");
        lx.command.perform(new LXCommand.Parameter.SetString(
          texName,
          baseFilename
        ));
      });
    }
  }

}
