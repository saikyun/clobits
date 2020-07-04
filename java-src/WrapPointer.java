import org.graalvm.word.PointerBase;

import clobits.all_targets.IWrapper;

public class WrapPointer implements IWrapper {
  PointerBase pointer;

  public WrapPointer(PointerBase p) {
    this.pointer = p;
  }

  public PointerBase unwrap() {
    return this.pointer;
  }
}
