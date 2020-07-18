package clobits.wrappers;

import org.graalvm.word.PointerBase;
import org.graalvm.word.ComparableWord;
import clobits.wrappers.IWrapperNI;

public class WrapPointer implements IWrapperNI {
    PointerBase pointer;

    public boolean isNonNull() {
	return this.pointer.isNonNull();
    }

    public boolean isNull() {
	return this.pointer.isNull();
    }

    public boolean equal(ComparableWord val) {
	return this.pointer.equal(val);
    }
    
    public boolean notEqual(ComparableWord val) {
	return this.pointer.notEqual(val);
    }

    public long rawValue() {
	return this.pointer.rawValue();
    }
    
  public WrapPointer(PointerBase p) {
    this.pointer = p;
  }

  public PointerBase unwrap() {
    return this.pointer;
  }
}
