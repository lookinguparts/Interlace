package art.lookingup.vstrip;

public class Point3D {
  public Point3D(float x, float y, float z) {
    this.x = x; this.y = y; this.z = z;
  }

  public Point3D(Point3D p) {
    x = p.x;
    y = p.y;
    z = p.z;
  }

  public void scale(float x, float y, float z) {
    this.x *= x; this.y *= y; this.z *= z;
  }

  public void rotate(float angle) {
    float xNew = x * (float)Math.cos(angle) - y * (float)Math.sin(angle);
    float yNew = x * (float)Math.sin(angle) + y * (float)Math.cos(angle);
    x = xNew;
    y = yNew;
  }

  public float computePolarAngle() {
    return (float)Math.atan2(z, x);
  }

  public void translate(float x, float y, float z) {
    this.x += x;
    this.y += y;
    this.z += z;
  }

  public void rotateZAxis(float angle) {
    float newX = x * (float)Math.cos(angle) + y * (float)Math.sin(angle);
    float newY = - x * (float)Math.sin(angle) + y * (float)Math.cos(angle);
    x = newX;
    y = newY;
  }

  public void rotateYAxis(float angle) {
    float newX = x * (float)Math.cos(angle) - z * (float)Math.sin(angle);
    float newZ = x * (float)Math.sin(angle) + z * (float)Math.cos(angle);
    x = newX;
    z = newZ;
  }

  public void rotateXAxis(float angle) {
    float newY = y * (float)Math.cos(angle) + z * (float)Math.sin(angle);
    float newZ = -y * (float)Math.sin(angle) + z * (float)Math.cos(angle);
    y = newY;
    z = newZ;
  }

  public void projectXYPlane() {
    z = 0f;
  }

  public void projectXZPlane() {
    y = 0f;
  }

  public void projectYZPlane() {
    x = 0f;
  }

  public float length() {
    return (float)Math.sqrt(x*x + y*y + z*z);
  }

  public float distanceTo(Point3D p) {
    return (float)Math.sqrt((x-p.x)*(x-p.x) + (y-p.y)*(y-p.y) + (z-p.z)*(z-p.z));
  }

  public float dotProduct(Point3D p) {
    float dotProduct = x * p.x + y * p.y + z * p.z;
    return dotProduct;
  }

  public float angle(Point3D p) {
    return (float)Math.acos(dotProduct(p) / (length() * p.length()));
  }

  public void normalize() {
    float len = length();
    x /= len;
    y /= len;
    z /= len;
  }

  public void add(Point3D p) {
    x += p.x;
    y += p.y;
    z += p.z;
  }

  public void subtract(Point3D p) {
    x -= p.x;
    y -= p.y;
    z -= p.z;
  }

  public float x;
  public float y;
  public float z;
}
