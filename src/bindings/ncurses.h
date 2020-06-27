#if IS_POLYGLOT
#include <polyglot.h>
#endif
#include <ncurses.h>
#include <stdlib.h>
#include <string.h>

void  _SHADOWING_free( void * ptr);
void * _SHADOWING_malloc( int  size);
WINDOW * _SHADOWING_initscr();
int  _SHADOWING_delwin( WINDOW * win);
int  _SHADOWING_endwin();
int  _SHADOWING_printw( char * arg0);
int  _SHADOWING_refresh();
int  _SHADOWING_wrefresh( WINDOW * win);