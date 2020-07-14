(ns clobits.core)

(def int-type {#_#_:ni/wrapper 'int
               :ni/type 'int
               :poly/wrapper '.asInt
               :poly/type 'int
               :primitive true})

(def void-pointer-type {"*" {:interface    'clobits.all_targets.IVoidPointerYE
                             :poly/type    'clobits.all_targets.IVoidPointerYE
                             :ni/interface    'clobits.all_targets.IVoidPointer
                             :ni/type      'org.graalvm.nativeimage.c.type.VoidPointer
                             :ni/wrapper   'clobits.wrappers.WrapVoid
                             :ni/java-wrapper "new clobits.wrappers.WrapVoid"
                             :ni/unwrap    '.unwrap
                             :poly/wrapper 'wrap-pointer
                             :poly/unwrap  '.unwrap
                             :primitive false}})

(def default-typing
  {"int"    int-type
   "Uint32" int-type
   "Uint16" int-type
   "Sint16" int-type
   "Uint8"  int-type
   
   "char" {"*" {:ni/type   'org.graalvm.nativeimage.c.type.CCharPointer
                :ni/wrapper 'clobits.wrappers.WrapPointer
                :ni/unwrap '.unwrap
                :poly/type nil
                :primitive false}
           :poly/type 'char
           :ni/type 'char
           :primitive true}    
   
   "void" (merge void-pointer-type
                 {:ni/type 'void
                  :primitive true})})
