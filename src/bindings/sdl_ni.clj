;; This file is autogenerated -- probably shouldn't modify it by hand
(clojure.core/ns bindings.sdl-ni (:require [clobits.patch-gen-class] [clobits.all-targets]) (:import org.graalvm.word.PointerBase org.graalvm.nativeimage.c.struct.CField org.graalvm.nativeimage.c.CContext org.graalvm.nativeimage.c.function.CFunction org.graalvm.nativeimage.c.function.CLibrary org.graalvm.nativeimage.c.struct.CFieldAddress org.graalvm.nativeimage.c.struct.CStruct org.graalvm.nativeimage.c.struct.AllowWideningCast org.graalvm.nativeimage.c.function.CFunction org.graalvm.word.WordFactory clobits.all_targets.IVoidPointer [org.graalvm.nativeimage.c.type CCharPointer VoidPointer]) (:gen-class))

(clojure.core/deftype Headers [] org.graalvm.nativeimage.c.CContext$Directives (getHeaderFiles [_] ["\"/Users/test/programmering/clojure/clobits/src/bindings/sdl.h\""]))

(clojure.core/gen-interface :name ^{org.graalvm.nativeimage.c.CContext bindings.sdl_ni.Headers, org.graalvm.nativeimage.c.function.CLibrary "bindings$sdl-ni", org.graalvm.nativeimage.c.struct.CStruct "SDL_Event"} bindings.sdl_ni_generated.SDL_Event :extends [org.graalvm.word.PointerBase bindings.sdl_structs.ISDL_Event] :methods [[^{org.graalvm.nativeimage.c.struct.CField "type"} type [] int]])

(clojure.core/gen-interface :name ^{org.graalvm.nativeimage.c.CContext bindings.sdl_ni.Headers, org.graalvm.nativeimage.c.function.CLibrary "bindings$sdl-ni", org.graalvm.nativeimage.c.struct.CStruct "SDL_Surface"} bindings.sdl_ni_generated.SDL_Surface :extends [org.graalvm.word.PointerBase bindings.sdl_structs.ISDL_Surface] :methods [[^{org.graalvm.nativeimage.c.struct.CField "format"} format [] bindings.sdl_ni_generated.SDL_PixelFormat]])

(clojure.core/gen-interface :name ^{org.graalvm.nativeimage.c.CContext bindings.sdl_ni.Headers, org.graalvm.nativeimage.c.function.CLibrary "bindings$sdl-ni", org.graalvm.nativeimage.c.struct.CStruct "SDL_PixelFormat"} bindings.sdl_ni_generated.SDL_PixelFormat :extends [org.graalvm.word.PointerBase bindings.sdl_structs.ISDL_PixelFormat] :methods [[^{org.graalvm.nativeimage.c.struct.CField "palette"} palette [] clobits.all_targets.IVoidPointer]])

(clobits.patch-gen-class/gen-class-native :name ^{org.graalvm.nativeimage.c.CContext bindings.sdl_ni.Headers, org.graalvm.nativeimage.c.function.CLibrary "bindings$sdl-ni"} bindings.sdl :methods [^{:static true, :native true} [^{org.graalvm.nativeimage.c.function.CFunction {:transition org.graalvm.nativeimage.c.function.CFunction$Transition/NO_TRANSITION, :value "_SHADOWING_GET_SDL_INIT_VIDEO"}} get_sdl_init_video [] int] ^{:static true, :native true} [^{org.graalvm.nativeimage.c.function.CFunction {:transition org.graalvm.nativeimage.c.function.CFunction$Transition/NO_TRANSITION, :value "_SHADOWING_GET_SDL_WINDOW_SHOWN"}} get_sdl_window_shown [] int] ^{:static true, :native true} [^{org.graalvm.nativeimage.c.function.CFunction {:transition org.graalvm.nativeimage.c.function.CFunction$Transition/NO_TRANSITION, :value "_SHADOWING_get_null"}} get_null [] clobits.all_targets.IVoidPointer] ^{:static true, :native true} [^{org.graalvm.nativeimage.c.function.CFunction {:transition org.graalvm.nativeimage.c.function.CFunction$Transition/NO_TRANSITION, :value "_SHADOWING_gen_title"}} gen_title [] org.graalvm.nativeimage.c.type.CCharPointer] ^{:static true, :native true} [^{org.graalvm.nativeimage.c.function.CFunction {:transition org.graalvm.nativeimage.c.function.CFunction$Transition/NO_TRANSITION, :value "_SHADOWING_create_rect"}} create_rect [int int int int] org.graalvm.nativeimage.c.type.VoidPointer] ^{:static true, :native true} [^{org.graalvm.nativeimage.c.function.CFunction {:transition org.graalvm.nativeimage.c.function.CFunction$Transition/NO_TRANSITION, :value "_SHADOWING_get_e"}} get_e [] bindings.sdl_ni_generated.SDL_Event] ^{:static true, :native true} [^{org.graalvm.nativeimage.c.function.CFunction {:transition org.graalvm.nativeimage.c.function.CFunction$Transition/NO_TRANSITION, :value "_SHADOWING_SDL_Init"}} init [int] int] ^{:static true, :native true} [^{org.graalvm.nativeimage.c.function.CFunction {:transition org.graalvm.nativeimage.c.function.CFunction$Transition/NO_TRANSITION, :value "_SHADOWING_SDL_PollEvent"}} poll_event [bindings.sdl_ni_generated.SDL_Event] int] ^{:static true, :native true} [^{org.graalvm.nativeimage.c.function.CFunction {:transition org.graalvm.nativeimage.c.function.CFunction$Transition/NO_TRANSITION, :value "_SHADOWING_SDL_Delay"}} delay [int] void] ^{:static true, :native true} [^{org.graalvm.nativeimage.c.function.CFunction {:transition org.graalvm.nativeimage.c.function.CFunction$Transition/NO_TRANSITION, :value "_SHADOWING_SDL_UpdateWindowSurface"}} update_window_surface [org.graalvm.nativeimage.c.type.VoidPointer] int] ^{:static true, :native true} [^{org.graalvm.nativeimage.c.function.CFunction {:transition org.graalvm.nativeimage.c.function.CFunction$Transition/NO_TRANSITION, :value "_SHADOWING_SDL_GetWindowSurface"}} get_window_surface [org.graalvm.nativeimage.c.type.VoidPointer] bindings.sdl_ni_generated.SDL_Surface] ^{:static true, :native true} [^{org.graalvm.nativeimage.c.function.CFunction {:transition org.graalvm.nativeimage.c.function.CFunction$Transition/NO_TRANSITION, :value "_SHADOWING_SDL_MapRGB"}} map_rgb [bindings.sdl_ni_generated.SDL_PixelFormat int int int] int] ^{:static true, :native true} [^{org.graalvm.nativeimage.c.function.CFunction {:transition org.graalvm.nativeimage.c.function.CFunction$Transition/NO_TRANSITION, :value "_SHADOWING_SDL_CreateWindow"}} create_window [org.graalvm.nativeimage.c.type.CCharPointer int int int int int] org.graalvm.nativeimage.c.type.VoidPointer] ^{:static true, :native true} [^{org.graalvm.nativeimage.c.function.CFunction {:transition org.graalvm.nativeimage.c.function.CFunction$Transition/NO_TRANSITION, :value "_SHADOWING_SDL_FillRect"}} fill_rect [bindings.sdl_ni_generated.SDL_Surface org.graalvm.nativeimage.c.type.VoidPointer int] int] ^{:static true, :native true} [^{org.graalvm.nativeimage.c.function.CFunction {:transition org.graalvm.nativeimage.c.function.CFunction$Transition/NO_TRANSITION, :value "_SHADOWING_SDL_Quit"}} quit [] void]])

