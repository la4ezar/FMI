#pragma once
#include "Buffer.h"
#include <fstream>

// Buffered file output stream with auto flush on close
class BufferOutputStream {
	std::ofstream* stream;
	Buffer buffer;
	int position;
public:
	BufferOutputStream(std::ofstream& stream);

	void write(char c);

	void write(const char* c_arr, int size);

	void flush();

	long long tellp();

	void open(std::ofstream& stream);

	void close();
};

// Buffered file input stream
class BufferInputStream {
	std::ifstream* stream;
	Buffer buffer;
	int position;
public:
	BufferInputStream(std::ifstream& stream);
	int read();

	int read(char* c_arr, int size);

	void getline(char* c_arr, int max_size);

	void seekg(unsigned long long pos);

	long long tellg();

	void open(std::ifstream& stream);

	void close();
};
