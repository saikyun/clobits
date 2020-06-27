// includes
#include "ncurses.h"

// inline-c

void  silly(char arg0) {
  printf("%c", arg0);
}


char* string(int size) {
  char* s = (char*)malloc(sizeof(char) * size);
  memset(s, 0, size);
  return s;
}


int other_print_w(const void *value) {
#if IS_POLYGLOT
  if (polyglot_is_string(value)) {
    int length = polyglot_get_string_size(value);
    char str[length];
    polyglot_as_string(value, str, length, "UTF-8");
    return printw(str);
  } else {
    return printw(value);
  }
#else
  return printw(value);
#endif
}


char* append_char(char *str, int pos, char c) {
  str[pos] = c;
  return str;
}


// fns
void  _SHADOWING_silly( char  arg0) {
  silly(arg0);
}
char * _SHADOWING_string( int  size) {
  return string(size);
}
int  _SHADOWING_other_print_w(const void * value) {
  return other_print_w(value);
}
char * _SHADOWING_append_char( char * str,  int  pos,  char  c) {
  return append_char(str, pos, c);
}
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
  return printw(arg0);
}
int  _SHADOWING_refresh() {
  return refresh();
}
int  _SHADOWING_wrefresh( WINDOW * win) {
  return wrefresh(win);
}