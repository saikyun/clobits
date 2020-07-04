import org.graalvm.nativeimage.c.type.VoidPointer;

public class WrapVoid {
  org.graalvm.nativeimage.c.type.VoidPointer pointer;

  public WrapVoid(org.graalvm.nativeimage.c.type.VoidPointer p) {
    this.pointer = p;
  }

  public static VoidPointer unwrap(WrapVoid wp) {
    return wp.pointer;
  }
}
