#include "Buffer.h"

Buffer::Buffer() : mSize(BUFFER_SIZE) {
	buffer = new char[mSize + 1];
}
Buffer::~Buffer() {
	delete[] buffer;
}

char*& Buffer::toCharPtr() {
	return buffer;
}

char& Buffer::operator[](int ind) {
	return buffer[ind];
}

int Buffer::size() const {
	return mSize;
}
