#pragma once

const unsigned short BUFFER_SIZE = 1024; // 1kb

class Buffer {
	char* buffer;
	int mSize;
public:
	Buffer();
	~Buffer();

	char*& toCharPtr();

	char& operator[](int ind);

	int size() const;
};