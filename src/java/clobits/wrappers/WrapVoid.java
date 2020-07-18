package clobits.wrappers;

import org.graalvm.nativeimage.c.type.VoidPointer;
import org.graalvm.word.ComparableWord;
import clobits.wrappers.IWrapperNI;
import clobits.wrappers.IVoidPointer;
import clobits.wrappers.IVoidPointerYE;

public class WrapVoid implements IVoidPointerYE, IWrapperNI {
    IVoidPointer pointer;
    
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
    
    public WrapVoid(IVoidPointer p) {
      this.pointer = p;
    }

    public IVoidPointer unwrap() {
      return this.pointer;
    }
}
