#pragma once
#include <iostream>
#include <vector>
#include <queue>
#include <filesystem>
#include "BufferStreams.h"

// PAIRS
#include "Code.hpp"
#include "FilePosition.hpp"


namespace fs = std::experimental::filesystem;



// ----------------------------
// ------- HUFFMAN TREE -------
// ----------------------------
// Huffman tree node
class Node;

// Deallocate memory
void freeTree(Node*& root);

// Compare class for the priority queue
// The lower is the frequency the higher is the priority
struct Comparator;

// count BYTE frequencies in file
void byteFrequencies(std::vector<unsigned int>& frequencies, const fs::path& file_path);
// store tree nodes
void storeTreeNodes(std::priority_queue<Node*, std::vector<Node*>, Comparator>& queue,
	std::vector<unsigned int>& frequencies);
// build Huffman tree
Node* HuffmanTree(const fs::path& file_path);


// -------------------------
// ------- CHECKSUMS -------
// -------------------------

unsigned long long get_file_size(const fs::path& file_path);

// generate 16 bit checksum
unsigned short generate_checksum(const fs::path& file_path, unsigned long long from = 0,
	unsigned long long to = ULLONG_MAX);

// get the checksum encoded in the archive
// it is in the last two bytes
unsigned short get_checksum(const fs::path& file_path, unsigned int end_file_index);

// get the original file checksum encoded in the archive
// it is four bytes before the end of the file
// and it is two bytes long
unsigned short get_original_file_checksum(const fs::path& file_path, unsigned int end_file_index);

// checking the checksum written in the file
// with the actual checksum at this time
bool is_file_corrupted(const fs::path& file_path);


// --------------------
// ------- INFO -------
// --------------------
// generate level of compression
char generate_percent_of_compression(const fs::path& source_file_path, const fs::path& compressed_file_path,
	long long start_index = 0);

// get level of compression encoded in the archive
// it is five bytes before the end of the file and its 1 byte long
char get_percent_of_compression(const fs::path& file_path, unsigned int end_file_index);

// info help function
// make vector with all filenames and their end positions in the archive
std::vector<FilePosition> get_all_files_end_positions(const fs::path& compressed_file_path);
// get info(level of compression) function
void get_info(const fs::path& compressed_file_path);


// ------------------------
// ------- COMPRESS -------
// ------------------------
// If we have alphabet with N symbols, the longest path in huffman tree can be N-1,
// so the longest possible code is N-1
// In our case we have 257 symbol alphabet(ASCII table and one special symbol for EOF) , so the longest code can be 256bits,
// so I use 'Code' which is pair with first element the number of significant bits and second element the actual code
void encode(Node*& root, std::vector<Code>& coding, Code code = Code());

// generate compression path for the '.lacho' file
fs::path generate_compression_path(const fs::path& file_path);

// checks if original file has changed or is the same as in the archive
bool is_file_updated(const fs::path& source_file_path, const fs::path& compression_file_path);

// check if file is already compressed
bool is_file_already_compressed(const fs::path& source_file_path, const fs::path& compression_file_path);

// for single file/directory
// finds all file paths in the passed path
std::vector<fs::path> get_file_paths(const fs::path& source_file_path);

// for list of directories/files
// finds all file paths in the passed array of paths
std::vector<fs::path> get_file_paths(std::vector<fs::path>& list_of_files);

void encode_metadata(std::vector<fs::path>& files, const fs::path& compression_path);

void encode_filename(const fs::path& source_file_path, const fs::path& destination_file_path);

void encode_encode_map(const fs::path& destination_file_path, std::vector<Code>& code);

void encode_data(const fs::path& source_file_path, const fs::path& destination_file_path, std::vector<Code>& code);
void encode_info(const fs::path& source_file_path, const fs::path& destination_file_path, unsigned int start_index = 0);

void encode_original_file_checksum(const fs::path& source_file_path, const fs::path& destination_file_path);

void encode_checksum(const fs::path& destination_file_path, unsigned int start_index);

void encode_positions(const fs::path& source_file_path, const fs::path& destination_file_path,
	unsigned int start_index, unsigned int end_index);

void compress_file(const fs::path& source_file_path, const fs::path& destination_file_path);

void writeFileSeparator(const fs::path& destination_file_path);
void encode_same_data(const fs::path& source_file_path, const fs::path& compression_path, const fs::path& compression_path_tmp);

// --------------------------
// ------- DECOMPRESS -------
// --------------------------
// generates decompression path for the file that will be decompressed
fs::path generate_decompression_path(const fs::path& destination_file_path, std::string& original_filename);

std::string decode_filename(BufferInputStream& input_stream);

Node*& decode_huffman_tree(BufferInputStream& decoding_input);

void decode_data(Node*& root, BufferInputStream& input, BufferOutputStream& output);

void decompress_file(BufferInputStream& source_stream, const fs::path& destination_file_path);


// -----------------------
// ------- DAMAGED -------
// -----------------------
// damaged help function
unsigned int get_metadata_end_position(const fs::path& compressed_file_path);

// damaged
void get_damaged_files(const fs::path& compressed_file_path);

void command_handler(int& argc, char**& argv);