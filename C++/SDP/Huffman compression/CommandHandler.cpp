#include "Huffman.h"

// ---------------------------------------
// ------- COMMANDS HELP FUNCTIONS -------
// ---------------------------------------
// checks if string contains '*' or '?'
bool contains_special_chars(const char* str) {
	if (!str) return false;
	if (!*str) return false;
	if (*str == '?' || *str == '*') return true;
	contains_special_chars(str + 1);
}

// string matching where the first string can contains wildcard characters
// https://www.geeksforgeeks.org/wildcard-character-matching/
bool match(const char* first, const char* second) {
	// If we reach at the end of both strings, we are done 
	if (*first == '\0' && *second == '\0')
		return true;

	// Make sure that the characters after '*' are present 
	// in second string. This function assumes that the first 
	// string will not contain two consecutive '*' 
	if (*first == '*' && *(first + 1) != '\0' && *second == '\0')
		return false;

	// If the first string contains '?', or current characters 
	// of both strings match 
	if (*first == '?' || *first == *second)
		return match(first + 1, second + 1);

	// If there is *, then there are two possibilities 
	// a) We consider current character of second string 
	// b) We ignore current character of second string. 
	if (*first == '*')
		return match(first + 1, second) || match(first, second + 1);
	return false;
}

// --------------------------------------------
// ------- COMMANDS EXECUTION FUNCTIONS -------
// --------------------------------------------

// compressing a single file
void compress_single_file(const fs::path& source_file_path) {
	// make new path to .lacho file
	fs::path compression_path = generate_compression_path(source_file_path);

	if (is_file_already_compressed(source_file_path, compression_path)) return;

	// input metadata
	std::vector<fs::path> file(1);
	file[0] = source_file_path;
	encode_metadata(file, compression_path);

	// compress the file
	compress_file(source_file_path, compression_path);
}


// compress directory and compress list help functions
// ---------------------------------------------------
// compressing the files in new '.lacho' file
void compress_files_new_file(const fs::path& compression_path, std::vector<fs::path>& file_paths) {
	// input metadata
	encode_metadata(file_paths, compression_path);

	// compress the files
	int file_num = file_paths.size();
	bool isFirstFile = true;
	for (int i = 0; i < file_num; ++i) {
		if (isFirstFile == false) writeFileSeparator(compression_path);		// separate the encoded files
		compress_file(file_paths[i], compression_path);
		isFirstFile = false;
	}
	encode_checksum(compression_path, 0);
}
// compressing the files first in temporary '.lacho.tmp' file then
// remove the existing original '.lacho' file
// and rename the temporary file with original file
void compress_files_tmp_file(const fs::path& compression_path,
	std::vector<fs::path>& file_paths, std::vector<bool>& compressed_files) {
	// create temporary file for the new compression
	fs::path compression_path_tmp = compression_path.string() + ".tmp";
	std::ofstream coding_file_tmp(compression_path_tmp, std::ios::binary | std::ios::out | std::ios::trunc);
	coding_file_tmp.close();

	// input metadata
	encode_metadata(file_paths, compression_path_tmp);

	// compress the files
	int file_num = file_paths.size();
	bool isFirstFile = true;
	for (int i = 0; i < file_num; ++i) {
		if (isFirstFile == false) writeFileSeparator(compression_path_tmp);		// separate the encoded files
		// if the file is already compressed, just write him in the new compressed path
		// if not - compress him
		if (compressed_files[i]) encode_same_data(file_paths[i], compression_path, compression_path_tmp);
		else compress_file(file_paths[i], compression_path_tmp);
		isFirstFile = false;
	}
	encode_checksum(compression_path_tmp, 0);

	// remove the old compression file and
	// rename the new compression file with its name
	fs::remove(compression_path);
	fs::rename(compression_path_tmp, compression_path);
}

// compressing directory
void compress_directory(const fs::path& source_file_path) {
	// make new path to .lacho file
	fs::path compression_path = generate_compression_path(source_file_path);

	// get file paths
	std::vector<fs::path> file_paths = get_file_paths(source_file_path);

	// get which files are already compressed in the archive
	// and which are not
	unsigned short file_num = file_paths.size();
	std::vector<bool> compressed_files(file_num);
	bool are_all_compressed = true;
	bool are_all_not_compressed = true;
	for (int i = 0; i < file_num; ++i) {
		if (is_file_already_compressed(file_paths[i], compression_path)) {
			compressed_files[i] = true;
			are_all_not_compressed = false;
		}
		else {
			are_all_compressed = false;
		}
	}

	// if all are already compressed its done
	if (are_all_compressed) return;

	// if all of them are NOT compressed make new compressed file and compress all of them
	// -----------------------------------------------------------------------------------
	// if there is at least one compressed file make .tmp file and
	// compress all of non-compressed files in it and
	// just write all of the already compressed files in it
	// then remove the original compressed file and rename the .tmp file with its name
	if (are_all_not_compressed)
		compress_files_new_file(compression_path, file_paths);
	else
		compress_files_tmp_file(compression_path, file_paths, compressed_files);
}


// compress array of sources
void compress_list(std::vector<fs::path>& list_of_sources) {
	int list_size = list_of_sources.size();
	// make new path to .lacho file
	// its either the name of the current folder or 'Archive'
	fs::path new_path = ((fs::current_path().filename().compare(""))
		? fs::current_path().filename() : "Archive");

	fs::path compression_path = generate_compression_path(new_path);

	std::vector<fs::path> file_paths = get_file_paths(list_of_sources);
	unsigned short file_num = file_paths.size();
	std::vector<bool> compressed_files(file_num);
	bool are_all_compressed = true;
	bool are_all_not_compressed = true;
	for (int i = 0; i < file_num; ++i) {
		if (is_file_already_compressed(file_paths[i], compression_path)) {
			compressed_files[i] = true;
			are_all_not_compressed = false;
		}
		else {
			are_all_compressed = false;
		}
	}

	// if all are already compressed its done
	if (are_all_compressed) return;

	// if all of them are NOT compressed make new compressed file and compress all of them
	// -----------------------------------------------------------------------------------
	// if there is at least one compressed file make .tmp file and
	// compress all of non-compressed files in it and
	// just write all of the already compressed files in it
	// then remove the original compressed file and rename the .tmp file with its name
	if (are_all_not_compressed)
		compress_files_new_file(compression_path, file_paths);
	else {
		compress_files_tmp_file(compression_path, file_paths, compressed_files);
	}
}



// decompress help functions
// -------------------------
// checks if a file is for decompression
bool is_for_decompression(std::vector<fs::path>& files, fs::path& file) {
	int files_size = files.size();
	for (int i = 0; i < files_size; ++i) {
		if (match(files[i].string().c_str(), file.string().c_str()) ||				// full path check
			match(files[i].string().c_str(), file.filename().string().c_str()) ||	// file with extension check
			match(files[i].string().c_str(), file.stem().string().c_str())) {		// file without extension check
			return true;
		}
	}
	return false;
}


// decompress certain files help function
// --------------------------------------
// returns array of the end positions of all the files for decompression
std::vector<long long> get_files_for_decompress_positions(const fs::path& source_file_path,
	std::vector<fs::path> files) {

	std::ifstream decoding_file;
	decoding_file.open(source_file_path, std::ios::binary | std::ios::in);
	BufferInputStream decoding_input(decoding_file);

	unsigned short files_size = files.size();
	std::vector<long long> files_for_decompress_positions(0);

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
		// convert filename to fs::path
		fs::path filename_to_path = filename;
		if (is_for_decompression(files, filename_to_path))
			files_for_decompress_positions.push_back(first_position);
		else
			files_for_decompress_positions.push_back(-1);
	}
	decoding_input.close();
	return files_for_decompress_positions;
}

// decompress certain files from archive
void decompress_certain_files(const fs::path& source_file_path, const fs::path& destination_file_path,
	std::vector<fs::path>& files) {
	// if the file don't exist or is empty
	if (!get_file_size(source_file_path) || !fs::exists(source_file_path)) return;
	// if the archive is corrupted don't decompress
	if (is_file_corrupted(source_file_path)) return;

	// container for starting positions of each file for decompression
	std::vector<long long> files_for_decompress_positions
		= get_files_for_decompress_positions(source_file_path, files);
	unsigned short files_for_decompress_size = files_for_decompress_positions.size();

	std::ifstream decoding_file;
	BufferInputStream decoding_input(decoding_file);
	for (int i = 0; i < files_for_decompress_size; ++i) {
		long long position = files_for_decompress_positions[i];
		if (position == -1) continue;

		// open the stream on each file
		// if we don't do that and try to decompress early the last file
		// then the stream will be closed and we would not be able to
		// move the seekg and read again
		decoding_file.open(source_file_path, std::ios::binary | std::ios::in);
		decoding_input.open(decoding_file);

		// decompress the file
		decoding_input.seekg(position);
		decompress_file(decoding_input, destination_file_path);

		// close the stream
		decoding_input.close();
	}
}

// decompressing all files from archive
void decompress_all_files(const fs::path& source_file_path, const fs::path& destination_file_path) {
	// if the file don't exist or is empty
	if (!get_file_size(source_file_path) || !fs::exists(source_file_path)) return;
	// if the archive is corrupted don't decompress
	if (is_file_corrupted(source_file_path)) return;

	std::ifstream decoding_file;
	decoding_file.open(source_file_path, std::ios::binary | std::ios::in);
	BufferInputStream decoding_input(decoding_file);

	// skip metadata
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

	// decompress all files
	for (int i = 0; i < num_files; ++i) {
		decompress_file(decoding_input, destination_file_path);
		if (decoding_input.read() == -1) break;
	}

	decoding_input.close();
}


// ------------------------
// ------- COMMANDS -------
// ------------------------
void compress_command(int& argc, char**& argv) {
	if (argc == 3) {
		// make path
		fs::path path = argv[2];
		if (!contains_special_chars(argv[2])) {
			if (fs::is_directory(path)) compress_directory(path);
			else compress_single_file(path);
		}
		else {
			// iterate directory and find matches
			std::vector<fs::path> files(0);
			for (const fs::directory_entry& entry : fs::recursive_directory_iterator(fs::current_path()))
				if (fs::is_directory(entry) == false)
					if (match(argv[2], entry.path().string().c_str()))
						files.push_back(entry.path());

			if (files.size() == 1) {
				if (fs::is_directory(files[0])) compress_directory(files[0]);
				else compress_single_file(files[0]);
			}
			else
				compress_list(files);
		}
	}
	else {
		std::vector<fs::path> files(0);
		for (int i = 2; i < argc; ++i) {
			if (!contains_special_chars(argv[i])) {
				files.push_back(argv[i]);
			}
			else {
				// iterate directory and find matches
				for (const fs::directory_entry& entry : fs::recursive_directory_iterator(fs::current_path()))
					if (fs::is_directory(entry) == false)
						if (match(argv[i], entry.path().string().c_str()))
							files.push_back(entry.path());
			}
		}
		compress_list(files);
	}
}

void decompress_command(int& argc, char**& argv) {
	if (argc == 3) {
		// decompress all files in the archive in current folder
		decompress_all_files(argv[2], "");
	}
	else if (argc == 4) {
		// decompress all files in the archive in the given path
		decompress_all_files(argv[2], argv[3]);
	}
	else {
		// decompress certain files in the archive
		std::vector<fs::path> files(0);
		for (int i = 3; i < argc - 1; ++i)
			files.push_back(argv[i]);

		decompress_certain_files(argv[2], argv[argc - 1], files);
	}
}

void info_command(int& argc, char**& argv) {
	if (argc > 3) {
		std::cout << "Too much arguments." << std::endl;
		return;
	}
	// create path
	fs::path path = argv[2];
	if (fs::exists(path) == false) {
		std::cout << "No such file." << std::endl;
	}
	else {
		get_info(path);
	}
}

void test_command(int& argc, char**& argv) {
	if (argc > 3) {
		std::cout << "Too much arguments." << std::endl;
		return;
	}
	// create path
	fs::path path = argv[2];
	if (fs::exists(path) == false) {
		std::cout << "No such file." << std::endl;
	}
	else {
		if (is_file_corrupted(path)) {
			std::cout << "The archive is damaged.\n";
			get_damaged_files(path);
		}
		else {
			std::cout << "The archive is NOT damaged.\n";
		}
	}
}

void command_handler(int& argc, char**& argv) {
	if (argc == 1) {
		std::cout << "Please write a command.\n";
		return;
	}
	else if (argc == 2) {
		std::cout << "Too few arguments.\n";
		return;
	}

	if (!strcmp(argv[1], "compress"))		 compress_command(argc, argv);
	else if (!strcmp(argv[1], "decompress")) decompress_command(argc, argv);
	else if (!strcmp(argv[1], "info"))		 info_command(argc, argv);
	else if (!strcmp(argv[1], "test"))		 test_command(argc, argv);
	else {
		std::cout << "Unknown command.\n";
		return;
	}
}
