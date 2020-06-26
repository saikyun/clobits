#if IS_POLYGLOT
#include <polyglot.h>
#endif
#include <ncurses.h>
#include <stdlib.h>
#include <string.h>

void  _SHADOWING_silly( char  arg0);
char * _SHADOWING_string( int  size);
int  _SHADOWING_other_print_w(const void * value);
char * _SHADOWING_append_char( char * str,  int  pos,  char  c);
void  _SHADOWING_free( void * ptr);
void * _SHADOWING_malloc( int  size);
WINDOW * _SHADOWING_initscr();
int  _SHADOWING_delwin( WINDOW * win);
int  _SHADOWING_endwin();
int  _SHADOWING_printw( char * arg0);
int  _SHADOWING_refresh();
int  _SHADOWING_wrefresh( WINDOW * win);