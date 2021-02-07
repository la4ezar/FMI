#pragma once

typedef unsigned char byte; // sizeof(byte) == 1
// Sequence of 0s and 1s
class Bitset {
	byte* mData;
	int mSize;
public:
	Bitset(int size = 8);
	Bitset(const Bitset& other);
	Bitset& operator=(const Bitset& other);
	~Bitset();

	Bitset& set(int idx);

	Bitset& clear(int idx);

	bool get(int idx);

	int size() const;

	char* toCharPtr();
};