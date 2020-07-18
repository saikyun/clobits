(ns clobits.wrappers)

(definterface IWrapper
  (unwrap []))

(gen-interface
 :name clobits.wrappers.IWrapperNI
 :extends [clobits.wrappers.IWrapper]
 :methods [[unwrap [] org.graalvm.word.PointerBase]])

(gen-interface
 :name clobits.wrappers.IVoidPointerYE
 :extends [])

(gen-interface
 :name clobits.wrappers.IVoidPointer
 :extends [org.graalvm.nativeimage.c.type.VoidPointer
           clobits.wrappers.IVoidPointerYE])

(gen-interface
 :name clobits.wrappers.IVoidPointerYE
 :extends [])

(gen-interface
 :name clobits.wrappers.IVoidPointer
 :extends [org.graalvm.nativeimage.c.type.VoidPointer
           clobits.wrappers.IVoidPointerYE])
