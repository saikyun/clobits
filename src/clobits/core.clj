(ns clobits.core)

(def int-type {:ni/wrapper 'int
               :ni/type 'int
               :poly/wrapper '.asInt
               :poly/type 'int
               :primitive true})

(def void-pointer-type {"*" {:poly/type    'clobits.all_targets.IVoidPointerYE
                             :ni/type      'clobits.all_targets.IVoidPointer
                             :ni/wrapper   'clobits.wrappers.WrapVoid
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
                :poly/type nil
                :primitive false}
           :poly/type 'char
           :ni/type 'char
           :primitive true}    
   
   "void" (merge void-pointer-type
                 {:ni/type 'void
                  :primitive true})})
