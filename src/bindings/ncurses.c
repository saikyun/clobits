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
int  _SHADOWING_printw( char * arg0) {

#if IS_POLYGLOT
char *arg0634;
if (polyglot_is_string(arg0)) {
 int length = polyglot_get_string_size(arg0);
 char str[length];
 polyglot_as_string(arg0, str, length, "UTF-8");
 arg0634 = str;
 } else {
 arg0634 = arg0;
 }
   return printw(arg0634);
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