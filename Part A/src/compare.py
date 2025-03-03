import sys
import os
import re
from itertools import zip_longest
def get_number_from_name(file_name : str) -> int:
    ext_ind = file_name.find(".txt")
    return int(file_name[:ext_ind])
def tokens_from_out(fd):
    for x in fd:
        s_line = re.split('\t| ',x)
        token_o = s_line[2].strip('\n')
        if token_o:
           yield token_o
def tokens_from_wiki(fd):
    for x in fd:
        for word in re.split(r"[^a-zA-Z0-9_']+",x):
            if word :
                yield word
def main():
    data_folder = sys.argv[1]
    output_file = sys.argv[2]
    # print(f"{data_folder}\n{output_file}")
    files_in_data = os.scandir(data_folder)
    max_id = -sys.maxsize-1
    file_max = ""
    for file in files_in_data:
        file_id = get_number_from_name(file.name)
        if file_id > max_id:
            max_id = file_id
            file_max = file.name
    fd_wiki = open(data_folder + "/" + file_max,"r")
    fd_out = open(output_file,"r")
    for i, (token_w, token_o) in enumerate(zip_longest(tokens_from_wiki(fd_wiki),tokens_from_out(fd_out),fillvalue=None)):
        if token_w is None or token_o is None or token_o != token_w:
            print(f"\n\nFile not matching : {token_w} : {token_o}\n")
            sys.exit(-1)
        elif(token_o == token_w):
            print(f"{i:>5} {token_w} : {token_o}")
        else:
            print(f"\n\nFile not matching : {token_w} : {token_o}\n")
            sys.exit(-1)
    print(f"\n\nFile matching\n")
main()
