#include "Bitset.h"

Bitset::Bitset(int size)
	: mData(new byte[size / 8 + int((size % 8) > 0)])
	, mSize(size) {}
Bitset::Bitset(const Bitset& other) {
	this->mData = new byte[other.mSize / 8 + int((other.mSize % 8) > 0)];
	for (int i = 0; i < other.mSize / 8 + int((other.mSize % 8) > 0); ++i)
		mData[i] = other.mData[i];
	this->mSize = other.mSize;
}
Bitset& Bitset::operator=(const Bitset& other) {
	if (this != &other) {
		delete[] mData;
		this->mData = new byte[other.mSize / 8 + int((other.mSize % 8) > 0)];
		for (int i = 0; i < other.mSize / 8 + int((other.mSize % 8) > 0); ++i)
			mData[i] = other.mData[i];
		this->mSize = other.mSize;
	}
	return *this;
}
Bitset::~Bitset() {
	delete[] mData;
}

Bitset& Bitset::set(int idx) {
	const int byteIdx = idx / 8;
	const int bitIdx = 7 - (idx % 8);
	byte &el = mData[byteIdx];
	el = el | (1 << bitIdx);
	return *this;
}

Bitset& Bitset::clear(int idx) {
	const int byteIdx = idx / 8;
	const int bitIdx = 7 - (idx % 8);
	byte &el = mData[byteIdx];
	el = el & ~(1 << bitIdx);
	return *this;
}

bool Bitset::get(int idx) {
	const int byteIdx = idx / 8;
	const int bitIdx = 7 - idx % 8;
	const byte &el = mData[byteIdx];
	const bool bitVal = (1 << bitIdx) & el;
	return bitVal;
}

int Bitset::size() const {
	return mSize;
}

char* Bitset::toCharPtr() {
	return (char*)mData;
}