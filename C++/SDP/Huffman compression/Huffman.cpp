#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <queue>
#include <utility>
#include <filesystem>
#include <cstring>

#include "Huffman.h"
#include "BufferStreams.h"
#include "BitStreams.h"
#include "Code.hpp"
#include "FilePosition.hpp"

namespace fs = std::experimental::filesystem;

const unsigned short EOF_MARKER = 256;

// ----------------------------
// ------- HUFFMAN TREE -------
// ----------------------------
// Huffman tree node
class Node {
	unsigned short character;
	unsigned int frequency;
	Node* left;
	Node* right;
public:
	// constructing node
	Node(unsigned short character, unsigned int frequency, Node* left, Node* right) {
		this->character = character;
		this->frequency = frequency;
		this->left = left;
		this->right = right;
	}

	bool isLeaf() const {
		return (left == nullptr && right == nullptr);
	}

	// getters
	unsigned short& getCharacter() {
		return character;
	}
	unsigned int getFrequency() const {
		return frequency;
	}
	Node*& getLeft() {
		return left;
	}
	Node*& getRight() {
		return right;
	}

	// setters
	void setCharacter(unsigned short& character) {
		this->character = character;
	}
	void setFrequency(unsigned int& frequency) {
		this->frequency = frequency;
	}
	void setLeft(Node* left) {
		this->left = left;
	}
	void setRight(Node* right) {
		this->right = right;
	}
};

// Deallocate memory
void freeTree(Node*& root) {
	if (root == nullptr) return;
	freeTree(root->getLeft());
	freeTree(root->getRight());
	delete root;
	root = nullptr;
}

// Compare class for the priority queue
// The lower is the frequency the higher is the priority
struct Comparator {
	bool operator()(Node* n1, Node* n2) {
		return n1->getFrequency() > n2->getFrequency();
	}
};

// count BYTE frequencies in file
void byteFrequencies(std::vector<unsigned int>& frequencies, const fs::path& file_path) {
	std::ifstream file;
	file.open(file_path, std::ios::binary | std::ios::in);
	if (!file.is_open()) return;

	// buffer input stream implementation
	BufferInputStream bis(file);
	int i = 0;
	while ((i = bis.read()) != -1)
		frequencies[(unsigned char)i]++;

	file.close();
}

// store tree nodes
void storeTreeNodes(std::priority_queue<Node*, std::vector<Node*>, Comparator>& queue,
	std::vector<unsigned int>& frequencies) {
	// putting the characters as tree nodes
	for (int i = 0; i < frequencies.size(); ++i) {
		if (frequencies[i] > 0)
			queue.push(new Node(i, frequencies[i], nullptr, nullptr));
	}

	queue.push(new Node(EOF_MARKER, 1, nullptr, nullptr));	// EOF marker
}

// build Huffman tree
Node* HuffmanTree(const fs::path& file_path) {
	std::vector<unsigned int> frequencies(256);
	std::priority_queue<Node*, std::vector<Node*>, Comparator> queue;

	byteFrequencies(frequencies, file_path);	// get frequencies
	storeTreeNodes(queue, frequencies);					// storing the tree nodes

	// building the tree
	while (queue.size() > 1) {
		Node* left = queue.top();
		queue.pop();
		Node* right = queue.top();
		queue.pop();

		// merging the two smallest nodes
		// the nodes that are not leafs will have the '\0' character
		//Node* parent = new Node('\0', left->getFrequency() + right->getFrequency(), left, right);
		//queue.push(parent);
		queue.push(new Node('\0', left->getFrequency() + right->getFrequency(), left, right));
	}
	return queue.top();
}


// -------------------------
// ------- CHECKSUMS -------
// -------------------------
// get the size of a file
unsigned long long get_file_size(const fs::path& file_path) {
	std::fstream file;
	file.open(file_path);
	if (file.is_open()) {
		file.seekg(0, std::ios::end);
		long long result = file.tellg();
		if (result == -1) return 0;
		return result;
	}
	return 0;
}

// generate 16 bit checksum
unsigned short generate_checksum(const fs::path& file_path, unsigned long long from,
	unsigned long long to) {
	unsigned long long cnt = 0;
	unsigned short result = 0;
	byte curChar = 0;
	std::ifstream file;
	file.open(file_path, std::ios::in | std::ios::binary);
	BufferInputStream bis(file);

	bis.seekg(from);

	while (bis.read((char*)& curChar, 1) != -1 &&
		cnt++ < to - from)
		result += curChar;
	file.close();

	return result;
}

// get the checksum encoded in the archive
// it is in the last two bytes
unsigned short get_checksum(const fs::path& file_path, unsigned int end_file_index) {
	std::ifstream file;
	file.open(file_path.filename(), std::ios::in | std::ios::binary);
	BufferInputStream bis(file);
	file.seekg(end_file_index - 2);
	unsigned short checksum = 0;
	file.read((char*)& checksum, 2);
	file.close();
	return checksum;
}

// get the original file checksum encoded in the archive
// it is four bytes before the end of the file
// and it is two bytes long
unsigned short get_original_file_checksum(const fs::path& file_path, unsigned int end_file_index) {
	std::ifstream file;
	file.open(file_path.filename(), std::ios::in | std::ios::binary);
	BufferInputStream bis(file);
	file.seekg(end_file_index - 4);
	unsigned short checksum = 0;
	file.read((char*)& checksum, 2);
	file.close();
	return checksum;
}

// checking the checksum written in the file
// with the actual checksum at this time
bool is_file_corrupted(const fs::path& file_path) {
	unsigned short file_checksum = get_checksum(file_path, get_file_size(file_path));
	byte first_byte = (file_checksum >> 8) & 0xff;
	byte second_byte = file_checksum & 0xff;
	// the actual checksum is the current checksum of the file without 
	// its last two bytes which are for the file checksum
	unsigned short actual_checksum = generate_checksum(file_path) - first_byte - second_byte;

	return (file_checksum != actual_checksum);
}


// --------------------
// ------- INFO -------
// --------------------
// generate level of compression
char generate_percent_of_compression(const fs::path& source_file_path, const fs::path& compressed_file_path,
	long long start_index) {
	unsigned long long original_file_size = get_file_size(source_file_path);
	unsigned long long compressed_file_size = get_file_size(compressed_file_path) - start_index;
	char result = 100 * (1 - (double(compressed_file_size) / double(original_file_size)));
	return result;
}

// get level of compression encoded in the archive
// it is five bytes before the end of the file and its 1 byte long
char get_percent_of_compression(const fs::path& file_path, unsigned int end_file_index) {
	std::ifstream file;
	file.open(file_path.filename(), std::ios::in | std::ios::binary);
	BufferInputStream bis(file);
	file.seekg(end_file_index - 5);
	char percent = 0;
	file.read((char*)& percent, 1);
	file.close();
	return percent;
}

// info help function
// make vector with all filenames and their end positions in the archive
std::vector<FilePosition> get_all_files_end_positions(const fs::path& compressed_file_path) {
	std::ifstream decoding_file;
	decoding_file.open(compressed_file_path, std::ios::binary | std::ios::in);
	BufferInputStream decoding_input(decoding_file);
	// read metadata
	unsigned short num_files;
	decoding_input.read((char*)& num_files, 2);

	std::vector<FilePosition> all_files_end_positions(num_files);
	int cnt = 0;

	char filename[1024];
	char curChar;
	decoding_input.read((char*)& curChar, 1);	// new line
	unsigned int first_position;
	unsigned int last_position;
	for (int i = 0; i < num_files; ++i) {
		decoding_input.getline(filename, 1024);
		decoding_input.read((char*)& first_position, 4);
		decoding_input.read((char*)& curChar, 1);	// space
		decoding_input.read((char*)& last_position, 4);
		all_files_end_positions[cnt].filename = filename;
		all_files_end_positions[cnt++].end_position = last_position;
	}
	decoding_input.close();

	return all_files_end_positions;
}

// get info(level of compression) function
void get_info(const fs::path& compressed_file_path) {
	// get all files end positions
	std::vector<FilePosition> all_files_end_positions
		= get_all_files_end_positions(compressed_file_path);

	std::ifstream decoding_file;
	decoding_file.open(compressed_file_path, std::ios::binary | std::ios::in);
	BufferInputStream decoding_input(decoding_file);
	int files_size = all_files_end_positions.size();

	short average_file_compression = 0;
	short current_file_compression;
	// output all files info
	for (int i = 0; i < files_size; ++i) {
		current_file_compression =
			(int)get_percent_of_compression(compressed_file_path, all_files_end_positions[i].end_position);

		std::cout << all_files_end_positions[i].filename
			//<< std::endl
			<< " "
			<< "percent of compression: "
			<< current_file_compression
			<< "%"
			<< std::endl;
		average_file_compression += current_file_compression;
	}
	std::cout << "Average archive compression: "
		<< average_file_compression / files_size
		<< "%"
		<< std::endl;

	// close the stream
	decoding_input.close();
}


// ------------------------
// ------- COMPRESS -------
// ------------------------
// If we have alphabet with N symbols, the longest path in huffman tree can be N-1,
// so the longest possible code is N-1
// In our case we have 257 symbol alphabet(ASCII table and one special symbol for EOF) , so the longest code can be 256bits,
// so I use 'Code' which is pair with first element the number of significant bits and second element the actual code
void encode(Node*& root, std::vector<Code>& coding, Code code) {
	if (root == nullptr) return;
	if (root->isLeaf())
		coding[root->getCharacter()] = code.significant_bits > 0 ? code : Code(Bitset().clear(0), 1);

	encode(root->getLeft(), coding, Code(code.code.clear(code.significant_bits), code.significant_bits + 1));
	encode(root->getRight(), coding, Code(code.code.set(code.significant_bits), code.significant_bits + 1));
}

// generate compression path for the '.lacho' file
fs::path generate_compression_path(const fs::path& file_path) {
	fs::path newpath = file_path.parent_path();		// path to the parent folder;
	if (newpath.string().compare("") != 0)			// if the path contain folders
		newpath += fs::path::preferred_separator;	// add line separator between the last folder
													// and the actual file;

	newpath += file_path.stem();					// the name without the extension;
	newpath += ".lacho";							// add my extension .lacho;
	return newpath;
}

// checks if original file has changed or is the same as in the archive
bool is_file_updated(const fs::path& source_file_path, const fs::path& compression_file_path) {
	if (fs::exists(compression_file_path) == false) return false;
	unsigned short compressed_file_checksum = generate_checksum(source_file_path);
	std::ifstream decoding_file;
	decoding_file.open(compression_file_path, std::ios::binary | std::ios::in);
	BufferInputStream decoding_input(decoding_file);

	unsigned short num_files;
	decoding_input.read((char*)& num_files, 2);

	char filename[1024];
	char curChar;
	decoding_input.read((char*)& curChar, 1);	// new line
	unsigned int first_position;
	unsigned int last_position;
	for (int i = 0; i < num_files; ++i) {
		decoding_input.getline(filename, 1024);
		decoding_input.read((char*)& first_position, 4);
		decoding_input.read((char*)& curChar, 1);	// space
		decoding_input.read((char*)& last_position, 4);
		if (source_file_path.string() == filename) {
			unsigned short original_file_checksum = get_original_file_checksum(compression_file_path, last_position);
			return original_file_checksum != compressed_file_checksum;
		}
	}
	decoding_input.close();
	return true;
}

// check if file is already compressed
bool is_file_already_compressed(const fs::path& source_file_path, const fs::path& compression_file_path) {
	if (fs::exists(compression_file_path) && !is_file_updated(source_file_path, compression_file_path))
		return true;
	return false;
}

// for single file/directory
// finds all file paths in the passed path
std::vector<fs::path> get_file_paths(const fs::path& source_file_path) {
	std::vector<fs::path> file_paths(0);
	if (fs::is_directory(source_file_path) == false) {
		file_paths.push_back(source_file_path);
		return file_paths;
	}
	for (const fs::directory_entry& entry : fs::recursive_directory_iterator(source_file_path))
		if (fs::is_directory(entry) == false)
			file_paths.push_back(entry.path());

	return file_paths;
}

// for list of directories/files
// finds all file paths in the passed array of paths
std::vector<fs::path> get_file_paths(std::vector<fs::path>& list_of_files) {
	int list_of_files_size = list_of_files.size();
	// create file paths
	std::vector<fs::path> file_paths(0);
	for (int i = 0; i < list_of_files_size; ++i) {
		// if its a single file
		if (fs::is_directory(list_of_files[i]) == false) {
			file_paths.push_back(list_of_files[i]);
		}
		else {
			// if its a directory							
			for (const fs::directory_entry& entry : fs::recursive_directory_iterator(list_of_files[i]))
				if (fs::is_directory(entry) == false)
					file_paths.push_back(entry.path());
		}
	}
	return file_paths;
}

void encode_metadata(std::vector<fs::path>& files, const fs::path& compression_path) {
	// create empty .lacho file
	std::ofstream coding_file(compression_path, std::ios::binary | std::ios::out | std::ios::trunc);
	BufferOutputStream coding_output(coding_file);

	// write num of files and new line
	unsigned short num_files = files.size();
	char new_line = '\n';
	coding_output.write((const char*)& num_files, 2);
	coding_output.write((const char*)& new_line, 1);

	// for every file write its name and new line then
	// 8 byte number for the index where it starts whitespace and 
	// 8 bytes for the index where this file ends
	// at the end of the compression of each file we will rewrite them
	unsigned int position = 0;
	char whitespace = ' ';
	for (int i = 0; i < num_files; ++i) {
		coding_output.write(files[i].string().c_str(), files[i].string().length());
		coding_output.write((const char*)& new_line, 1);
		coding_output.write((const char*)& position, 4);
		coding_output.write((const char*)& whitespace, 1);
		coding_output.write((const char*)& position, 4);
	}

	coding_output.close();
}

void encode_filename(const fs::path& source_file_path, const fs::path& destination_file_path) {
	std::ofstream coding_file;
	coding_file.open(destination_file_path, std::ios::binary | std::ios::out | std::ios::app);

	coding_file.write(source_file_path.string().c_str(), source_file_path.string().length());
	coding_file.write("\n", 1);
	coding_file.close();
}

void encode_encode_map(const fs::path& destination_file_path, std::vector<Code>& code) {
	std::ofstream coding_file;
	coding_file.open(destination_file_path, std::ios::binary | std::ios::out | std::ios::app);
	BufferOutputStream coding_output(coding_file);

	// count size of the map
	byte codeSize = -1;
	for (int i = 0; i < code.size(); ++i)
		if (code[i].significant_bits != 0)
			++codeSize;

	// write the size of the map
	coding_output.write((const char*)& codeSize, 1);
	byte strSize;
	for (unsigned short i = 0; i < code.size(); ++i) {
		if (code[i].significant_bits) {
			coding_output.write((const char*)& i, 2);
			coding_output.write((const char*)& code[i].significant_bits, 1);
			coding_output.write(code[i].code.toCharPtr(),
				code[i].significant_bits / 8 + int((code[i].significant_bits % 8) > 0));
		}
	}
	coding_output.write("\n", 1);
	coding_output.close();
}

void encode_data(const fs::path& source_file_path, const fs::path& destination_file_path, std::vector<Code>& code) {
	std::ofstream coding_file;
	coding_file.open(destination_file_path, std::ios::binary | std::ios::out | std::ios::app);
	BitOutputStream coding_bitset_output(coding_file);

	std::ifstream source_file;
	source_file.open(source_file_path, std::ios::binary | std::ios::in);
	BufferInputStream source_input(source_file);

	int curr = 0;
	while ((curr = source_input.read()) != -1)
		coding_bitset_output.add(code[(byte)curr]);

	// ADD EOF MARKER AT THE END OF THE FILE
	coding_bitset_output.add(code[EOF_MARKER]);

	// flush the remaining bits
	coding_bitset_output.flush();

	source_input.close();
	coding_bitset_output.close();
}

void encode_info(const fs::path& source_file_path, const fs::path& destination_file_path, unsigned int start_index) {
	char percentCompression = generate_percent_of_compression(source_file_path, destination_file_path, start_index);

	std::ofstream coding_file;
	coding_file.open(destination_file_path, std::ios::binary | std::ios::out | std::ios::app);

	coding_file.write((char*)& percentCompression, 1);
	coding_file.close();
}

void encode_original_file_checksum(const fs::path& source_file_path, const fs::path& destination_file_path) {
	unsigned short checksum = generate_checksum(source_file_path);
	std::ofstream coding_file;

	coding_file.open(destination_file_path, std::ios::binary | std::ios::out | std::ios::app);
	coding_file.write((char*)& checksum, 2);

	coding_file.close();
}

void encode_checksum(const fs::path& destination_file_path, unsigned int start_index) {
	unsigned short checksum = generate_checksum(destination_file_path, start_index);
	std::ofstream coding_file;

	coding_file.open(destination_file_path, std::ios::binary | std::ios::out | std::ios::app);
	coding_file.write((char*)& checksum, 2);

	coding_file.close();
}

void encode_positions(const fs::path& source_file_path, const fs::path& destination_file_path,
	unsigned int start_index, unsigned int end_index) {
	std::ifstream decoding_file;
	decoding_file.open(destination_file_path, std::ios::binary | std::ios::in);
	BufferInputStream decoding_input(decoding_file);

	unsigned short num_files;
	decoding_input.read((char*)& num_files, 2);
	char filename[1024];
	char curChar;
	decoding_input.read((char*)& curChar, 1);	// new line
	unsigned int first_position;
	unsigned int last_position;
	for (int i = 0; i < num_files; ++i) {
		decoding_input.getline(filename, 1024);
		if (source_file_path.string() == filename) {
			std::fstream file(destination_file_path, std::ios_base::binary | std::ios_base::out | std::ios_base::in);
			char whitespace = ' ';
			file.seekp(decoding_input.tellg(), std::ios::beg);
			file.write((char*)& start_index, 4);
			file.write((char*)& whitespace, 1);	// space
			file.write((char*)& end_index, 4);
			file.close();
			return;
		}
		decoding_input.read((char*)& first_position, 4);
		decoding_input.read((char*)& curChar, 1);	// space
		decoding_input.read((char*)& last_position, 4);

	}
	decoding_input.close();
}

void compress_file(const fs::path& source_file_path, const fs::path& destination_file_path) {
	unsigned int start_index = get_file_size(destination_file_path);

	Node* root = HuffmanTree(source_file_path);	// build tree
	std::vector<Code> code(257);				// create encoding map
	encode(root, code);							// encode

	// input the file name
	encode_filename(source_file_path, destination_file_path);

	// input map at the header file
	encode_encode_map(destination_file_path, code);

	// encode the file
	encode_data(source_file_path, destination_file_path, code);

	// input level of compression
	encode_info(source_file_path, destination_file_path, start_index);

	// input original file checksum
	encode_original_file_checksum(source_file_path, destination_file_path);

	// input compressed file checksum
	encode_checksum(destination_file_path, start_index);

	// rewrite metadata positions
	unsigned int end_index = get_file_size(destination_file_path);
	encode_positions(source_file_path, destination_file_path, start_index, end_index);

	// deallocate tree memory
	freeTree(root);
}

void writeFileSeparator(const fs::path& destination_file_path) {
	std::ofstream coding_file;
	coding_file.open(destination_file_path, std::ios::binary | std::ios::out | std::ios::app);

	coding_file.write("\n", 1);	// file separator

	coding_file.close();
}

void encode_same_data(const fs::path& source_file_path, const fs::path& compression_path, const fs::path& compression_path_tmp) {
	std::ifstream decoding_file;
	decoding_file.open(compression_path, std::ios::binary | std::ios::in);
	BufferInputStream decoding_input(decoding_file);

	std::ofstream decoding_file_tmp;
	decoding_file_tmp.open(compression_path_tmp, std::ios::binary | std::ios::out | std::ios::app);
	BufferOutputStream decoding_output(decoding_file_tmp);

	unsigned short num_files;
	decoding_input.read((char*)& num_files, 2);

	char filename[1024];
	char curChar;
	decoding_input.read((char*)& curChar, 1);	// new line
	unsigned int first_position;
	unsigned int last_position;
	for (int i = 0; i < num_files; ++i) {
		decoding_input.getline(filename, 1024);
		decoding_input.read((char*)& first_position, 4);
		decoding_input.read((char*)& curChar, 1);	// space
		decoding_input.read((char*)& last_position, 4);
		if (source_file_path.string() == filename) {
			decoding_input.seekg(first_position);
			while (decoding_input.tellg() < last_position && decoding_input.tellg() != -1) {
				curChar = decoding_input.read();
				decoding_output.write((const char*)& curChar, 1);
			}
			// rewrite the positions
			encode_positions(source_file_path, compression_path_tmp, first_position, last_position);
			break;
		}
	}

	decoding_output.close();
	decoding_input.close();
}


// --------------------------
// ------- DECOMPRESS -------
// --------------------------
// generates decompression path for the file that will be decompressed
fs::path generate_decompression_path(const fs::path& destination_file_path, std::string& original_filename) {
	fs::path result = destination_file_path;
	if (destination_file_path.compare(""))
		result += fs::path::preferred_separator;
	result += original_filename;
	return result;
}

std::string decode_filename(BufferInputStream& input_stream) {
	char filename[1024];
	input_stream.getline(filename, 1024);
	return filename;
}

Node*& decode_huffman_tree(BufferInputStream& decoding_input) {
	Node* root = new Node('\0', 0, nullptr, nullptr);
	Node* currentNode;
	byte mapSize;
	decoding_input.read((char*)& mapSize, 1);
	mapSize++;
	unsigned short curChar;
	byte numDigits;
	int cnt = 0;
	Bitset encoding(255);
	while (mapSize-- > 0) {
		currentNode = root;
		decoding_input.read((char*)& curChar, 2);
		decoding_input.read((char*)& numDigits, 1);
		decoding_input.read(encoding.toCharPtr(), numDigits / 8 + int((numDigits % 8) > 0));
		cnt = 0;
		while (cnt < numDigits) {
			if (!(encoding.get(cnt++))) {
				if (!currentNode->getLeft())
					currentNode->setLeft(new Node('\0', 0, nullptr, nullptr));
				currentNode = currentNode->getLeft();
			}
			else {
				if (!currentNode->getRight())
					currentNode->setRight(new Node('\0', 0, nullptr, nullptr));
				currentNode = currentNode->getRight();
			}
		}
		currentNode->setCharacter(curChar);
	}

	char c;
	decoding_input.read((char*)& c, 1);

	return root;
}

void decode_data(Node*& root, BufferInputStream& input, BufferOutputStream& output) {
	byte cur_byte;
	int cnt = 7;
	Node* currentNode = root;
	while (input.read((char*)& cur_byte, 1) != -1) {
		while (cnt >= 0) {
			if ((1 << cnt) & cur_byte)
				currentNode = currentNode->getRight();
			else
				currentNode = currentNode->getLeft();
			if (currentNode->isLeaf()) {
				if (currentNode->getCharacter() == EOF_MARKER)
					break;
				else {
					output.write((const char*)& currentNode->getCharacter(), 1);
					currentNode = root;
				}
			}
			--cnt;
		}
		if (currentNode->getCharacter() == EOF_MARKER && currentNode->isLeaf()) break;
		cnt = 7;
	}
}

void decompress_file(BufferInputStream& source_stream, const fs::path& destination_file_path) {
	// read original file name
	std::string filename = decode_filename(source_stream);

	// decompression file path, which is the destination file path + '\filename.extension'
	fs::path decompression_file_path = generate_decompression_path(destination_file_path, filename);

	// create the directories in the way
	fs::create_directories(decompression_file_path.parent_path());

	std::ofstream original_file;
	original_file.open(decompression_file_path, std::ios::out | std::ios::binary);
	BufferOutputStream decoding_output(original_file);

	// decode huffman tree
	Node* root = decode_huffman_tree(source_stream);

	// decode data
	decode_data(root, source_stream, decoding_output);

	// skip the precent of compression and checksum bytes
	long long c;
	source_stream.read((char*)& c, 5);

	// closing only the decompressed file and keep the '.lacho' file open
	// if there are more files to be decompressed
	decoding_output.close();

	// deallocate tree memory
	freeTree(root);
}


// -----------------------
// ------- DAMAGED -------
// -----------------------
// damaged help function
unsigned int get_metadata_end_position(const fs::path& compressed_file_path) {
	std::ifstream decoding_file;
	decoding_file.open(compressed_file_path, std::ios::binary | std::ios::in);
	BufferInputStream decoding_input(decoding_file);
	// read metadata
	unsigned short num_files;
	decoding_input.read((char*)& num_files, 2);

	char filename[1024];
	char curChar;
	decoding_input.read((char*)& curChar, 1);	// new line
	unsigned int first_position;
	unsigned int last_position;
	for (int i = 0; i < num_files; ++i) {
		decoding_input.getline(filename, 1024);
		decoding_input.read((char*)& first_position, 4);
		decoding_input.read((char*)& curChar, 1);	// space
		decoding_input.read((char*)& last_position, 4);
	}
	unsigned int result = decoding_input.tellg();
	decoding_input.close();
	return result;
}

// damaged
void get_damaged_files(const fs::path& compressed_file_path) {
	// get all files starting positions
	std::vector<FilePosition> all_files_end_positions
		= get_all_files_end_positions(compressed_file_path);

	std::ifstream decoding_file;
	decoding_file.open(compressed_file_path, std::ios::binary | std::ios::in);
	BufferInputStream decoding_input(decoding_file);
	int files_size = all_files_end_positions.size();

	unsigned int current_file_start_position = get_metadata_end_position(compressed_file_path);
	// output
	for (int i = 0; i < files_size; ++i) {
		unsigned short file_checksum
			= get_checksum(compressed_file_path, all_files_end_positions[i].end_position);

		unsigned short actual_checksum
			= generate_checksum(compressed_file_path, current_file_start_position, all_files_end_positions[i].end_position - 2);

		std::cout << all_files_end_positions[i].filename
			<< std::endl
			<< "is damaged: "
			<< ((file_checksum != actual_checksum) ? "True" : "False")
			<< std::endl;

		current_file_start_position = all_files_end_positions[i].end_position + 1;	// + 1 for the file separator byte
	}

	// close the stream
	decoding_input.close();
}



int main(int argc, char** argv) {

	command_handler(argc, argv);

	return 0;
}