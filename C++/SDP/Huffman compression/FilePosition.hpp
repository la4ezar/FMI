#pragma once
#include <string>

// Pair with
// first element - filename
// second element - file end position in the archive
struct FilePosition {
	std::string filename;
	unsigned int end_position;

	FilePosition(std::string filename = "", unsigned int end_position = 0)
		: filename(filename), end_position(end_position) {}

	FilePosition(const FilePosition& other) {
		this->filename = other.filename;
		this->end_position = other.end_position;
	}

	FilePosition& operator=(const FilePosition& other) {
		if (this != &other) {
			this->filename = other.filename;
			this->end_position = other.end_position;
		}
		return *this;
	}
	~FilePosition() = default;
};