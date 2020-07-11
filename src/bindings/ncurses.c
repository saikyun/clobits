// includes
#include "ncurses.h"

// inline-c


// fns
void  _SHADOWING_free( void * ptr) {
  free(ptr);
}
void * _SHADOWING_malloc( int  size) {
  return malloc(size);
}
WINDOW * _SHADOWING_initscr() {
  return initscr();
}
int  _SHADOWING_delwin( WINDOW * win) {
  return delwin(win);
}
int  _SHADOWING_endwin() {
  return endwin();
}
int  _SHADOWING_noecho() {
  return noecho();
}
int  _SHADOWING_curs_set( int  visibility) {
  return curs_set(visibility);
}
int  _SHADOWING_mvprintw( int  y,  int  x,  char * fmt) {

#if IS_POLYGLOT
 char *fmt497;
if (polyglot_is_string(fmt)) {
 int length = polyglot_get_string_size(fmt);
 char str[length + 1];
 polyglot_as_string(fmt, str, length + 1, "ascii");
 fmt497 = str;
 } else {
 fmt497 = fmt;
 }
   return mvprintw(y, x, fmt497);
#else
    return mvprintw(y, x, fmt);
#endif

}
int  _SHADOWING_clear() {
  return clear();
}
int  _SHADOWING_getmaxx( WINDOW * win) {
  return getmaxx(win);
}
int  _SHADOWING_getmaxy( WINDOW * win) {
  return getmaxy(win);
}
void  _SHADOWING_getmaxyx( WINDOW * win,  int  y,  int  x) {
  getmaxyx(win, y, x);
}
int  _SHADOWING_printw( char * arg0) {

#if IS_POLYGLOT
 char *arg0498;
if (polyglot_is_string(arg0)) {
 int length = polyglot_get_string_size(arg0);
 char str[length + 1];
 polyglot_as_string(arg0, str, length + 1, "ascii");
 arg0498 = str;
 } else {
 arg0498 = arg0;
 }
   return printw(arg0498);
#else
    return printw(arg0);
#endif

}
int  _SHADOWING_refresh() {
  return refresh();
}
int  _SHADOWING_wrefresh( WINDOW * win) {
  return wrefresh(win);
}