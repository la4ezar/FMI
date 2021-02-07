#include "BitStreams.h"
#include "Code.hpp"

// ------------------------------------
// ------- BITSET OUTPUT STREAM -------
// ------------------------------------
BitOutputStream::BitOutputStream(std::ofstream& stream) : bitset(BITSET_SIZE), position(0), stream(&stream) {}

void BitOutputStream::flush() {
	while ((8 - (position % 8)) < 8)
		bitset.clear(position++);
	(*stream).write(bitset.toCharPtr(), position / 8);
	position = 0;
}

void BitOutputStream::add(Code& code) {
	int cnt = 0;
	while (cnt < code.significant_bits) {
		if (position == BITSET_SIZE)
			flush();
		if (code.code.get(cnt))
			bitset.set(position);
		else
			bitset.clear(position);
		++position;
		++cnt;
	}
}

void BitOutputStream::open(std::ofstream& stream) {
	this->stream = &stream;
}

void BitOutputStream::close() {
	flush();
	stream->close();
}
