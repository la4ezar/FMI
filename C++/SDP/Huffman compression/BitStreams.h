#pragma once
#include "Bitset.h"
#include "Buffer.h"
#include "Code.hpp"
#include <fstream>

const unsigned short BITSET_SIZE = 8 * BUFFER_SIZE; // 1kb

// Buffered file output stream based on bitset with auto flush on close
class BitOutputStream {
	std::ofstream* stream;
	Bitset bitset;
	int position;
public:
	BitOutputStream(std::ofstream& stream);

	void flush();

	void add(Code& code);

	void open(std::ofstream& stream);

	void close();
};

