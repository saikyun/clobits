// This file is autogenerated -- probably shouldn't modify it by hand

package bindings.sdl_ni_generated;

import bindings.sdl_structs.ISDL_Event;
import clobits.all_targets.IWrapper;

public class WrapSDL_Event implements ISDL_Event, IWrapper {
  SDL_Event pointer;

  // used when sending data to native functions
  public SDL_Event unwrap() {
    return this.pointer;
  }

  public int type() {
    return this.pointer.type();
  }

  public WrapSDL_Event(SDL_Event pointer) {
    this.pointer = pointer;
  }
}
