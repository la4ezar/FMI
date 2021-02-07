#pragma once
#include "Bitset.h"

// Pair with
// first element sequence of bits
// second element the actual significant bits in that code
struct Code {
	Bitset code;
	byte significant_bits;

	Code(Bitset code, byte significant_bits) : code(code), significant_bits(significant_bits) {}
	Code(int max_bits = 256) : code(max_bits), significant_bits(0) {}
	Code(const Code& other) {
		this->code = other.code;
		this->significant_bits = other.significant_bits;
	}
	Code& operator=(const Code& other) {
		if (this != &other) {
			this->code = other.code;
			this->significant_bits = other.significant_bits;
		}
		return *this;
	}
	~Code() = default;
};