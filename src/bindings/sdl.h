#include <stdio.h>
#include <SDL2/SDL.h>

int  _SHADOWING_GET_SDL_INIT_VIDEO();
int  _SHADOWING_GET_SDL_WINDOW_SHOWN();
void * _SHADOWING_get_null();
char * _SHADOWING_gen_title();
SDL_Rect * _SHADOWING_create_rect( int  x,  int  y,  int  w,  int  h);
SDL_Event * _SHADOWING_get_e();
int  _SHADOWING_SDL_Init( Uint32  flags);
int  _SHADOWING_SDL_PollEvent( SDL_Event * event);
void  _SHADOWING_SDL_Delay( Uint32  ms);
int  _SHADOWING_SDL_UpdateWindowSurface( SDL_Window * window);
SDL_Surface * _SHADOWING_SDL_GetWindowSurface( SDL_Window * window);
Uint32  _SHADOWING_SDL_MapRGB(const SDL_PixelFormat * format,  Uint8  r,  Uint8  g,  Uint8  b);
SDL_Window * _SHADOWING_SDL_CreateWindow(const char * title,  int  x,  int  y,  int  w,  int  h,  Uint32  flags);
int  _SHADOWING_SDL_FillRect( SDL_Surface * dst, const SDL_Rect * rect,  Uint32  color);
void  _SHADOWING_SDL_Quit();