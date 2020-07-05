package clobits.wrappers;

import org.graalvm.nativeimage.c.type.VoidPointer;
import org.graalvm.word.ComparableWord;
import clobits.all_targets.IWrapper;
import clobits.all_targets.IVoidPointer;
import clobits.all_targets.IVoidPointerYE;

public class WrapVoid implements IVoidPointerYE, IWrapper {
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

    public VoidPointer unwrap() {
      return this.pointer;
    }
}
