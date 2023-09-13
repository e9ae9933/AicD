#include <windows.h>
#include <cstdlib>
#include <cstdio>
//#include <sstream>
//#include <iostream>
#include <string>
#include <cstring>
#include "main.h"
#include "translations.h"

using namespace std;

void log(FILE* out,const char* s)
{
	if(out!=NULL)
		fprintf(out,"%s\n",s);
}

int theMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow)
{
	FILE* out=fopen("aicd_bootstrap.log","w");
	if(out==NULL)
		MessageBox(NULL,LOG_FAILED,WARNING,MB_OK|MB_ICONWARNING|MB_TASKMODAL); 
	log(out,"log test");
	//first, try load java
	int rt=WinExec("jre\\bin\\java -version",SW_HIDE);
	bool runInternal=(rt>31);
	if(!runInternal)
	{
		if(WinExec("java -version",SW_HIDE)<=31)
		{
			MessageBox(NULL,NO_JAVA,WARNING,MB_OK|MB_ICONWARNING|MB_TASKMODAL);
			return 1;
		}
	}
	#if false
	stringstream ss("");
	if(runInternal)
		ss<<"jre\\bin\\java ";
	else
		ss<<"java ";
	ss<<JVM_OPTIONS;
	ss<<" ";
	ss<<"-jar AicD-all.jar";
	ss<<" 2>1 > aicd.log";
	string cmd=ss.str();
	#endif
	char buf[1024]={};
	sprintf_s(buf,1023,"%s -jar AicD-all.jar 2>1 > aicd.log",runInternal?"jre\\bin\\java":"java");
	log(out,"this is cmd line:");
	log(out,buf);
	rt=WinExec(buf,SW_HIDE);
	sprintf_s(buf,1023,"process end with %d",rt);
	log(out,buf);
	if(out!=NULL)
		fclose(out);
	return 0;
}
