#include "BufferStreams.h"

// ------------------------------------
// ------- BUFFER OUTPUT STREAM -------
// ------------------------------------
BufferOutputStream::BufferOutputStream(std::ofstream& stream) 
	: buffer(), position(0), stream(&stream) {}

void BufferOutputStream::write(char c) {
	if (position == BUFFER_SIZE) {
		stream->write(buffer.toCharPtr(), BUFFER_SIZE);
		position = 0;
	}
	buffer[position++] = c;
}

void BufferOutputStream::write(const char* c_arr, int size) {
	for (int i = 0; i < size; ++i)
		write(c_arr[i]);
}

void BufferOutputStream::flush() {
	if (position != 0) {
		stream->write(buffer.toCharPtr(), position);
		position = 0;
	}
}

long long BufferOutputStream::tellp() {
	long long result = stream->tellp();
	if (result == -1) return -1;
	result += position;
	return result;
}

void BufferOutputStream::open(std::ofstream& stream) {
	this->stream = &stream;
}

void BufferOutputStream::close() {
	flush();
	stream->close();
}

// -----------------------------------
// ------- BUFFER INPUT STREAM -------
// -----------------------------------
BufferInputStream::BufferInputStream(std::ifstream& stream) 
	: buffer(), position(-1), stream(&stream) {}

int BufferInputStream::read() {
	if (position == BUFFER_SIZE)
		position = -1;
	if (position == -1) {
		stream->read(buffer.toCharPtr(), BUFFER_SIZE);
		position = 0;
	}
	if (position == (int)stream->gcount())
		return -1;
	unsigned char result = buffer[position++];
	return result;
}

int BufferInputStream::read(char* c_arr, int size) {
	for (int i = 0; i < size; ++i) {
		int curr = read();
		if (curr == -1) return -1;
		else c_arr[i] = curr;
	}
	return 1;
}

void BufferInputStream::getline(char* c_arr, int max_size) {
	int cnt = 0;
	char curChar;
	while ((curChar = read()) != -1 &&
		curChar != '\n' &&
		cnt < max_size - 1) {
		c_arr[cnt++] = curChar;
	}
	c_arr[cnt] = '\0';
}

void BufferInputStream::seekg(unsigned long long pos) {
	stream->seekg(pos);
	position = -1;
}

long long BufferInputStream::tellg() {
	long long result = stream->tellg();
	if (result == -1)
		return -1;
	if (position != -1)
		result -= (buffer.size() - position);
	return result;
}

void BufferInputStream::open(std::ifstream& stream) {
	this->stream = &stream;
}

void BufferInputStream::close() {
	stream->close();
}
