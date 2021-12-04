import argparse
import xml.etree.ElementTree as ET

# Sample usage ----
# View help message:        python mxcat.py -h | less 
# Concatenate files:        python mxcat.py file*.mscx > catted.mscx
# Preview result:           python mxcat.py file*.mscx | less -S
# Numbered lines:           python mxcat.py file*.mscx | cat -n
# Search debug comments:    python mxcat.py out*.mscx --debug true | grep [DEBUG]
# -----------------

parser = argparse.ArgumentParser(description='Concatenate Musescore XML files and print on the standard output; mxcat behaves similarly to UNIX cat, where you may redirect output to another file. You can pipe to cat if you want access to cat-like options (such as -n, -v, and so on)')

parser.add_argument('names', metavar='F', type=str, nargs='+', help='Files to concatenate.')
parser.add_argument('--debug', metavar='Debug', type=bool, nargs='?', help='Print debug comments into output, which is grep-able with [DEBUG].', default=False)

# get arguments 
args = parser.parse_args()

# args.names with nargs + as list
files = args.names
debug = args.debug

# get header + metadata portion as lines
def get_headline(fname):
    ls = []
    tfile = open(fname, "r")
    broken = False
    for line in tfile:
        ls.append(line)
        if "</Part>" in line:
            broken = True
            break
    if not broken:
        raise SyntaxError("The mscx file is broken, no closing Part tag found.")
    return ls


def print_head(fname):
    larr = get_headline(fname)
    for line in larr:
        print(line)


def count_staff(fname):
    rootst = ET.parse(fname).getroot() # museScore
    count = 0
    for staff in rootst.findall('Score/Part/Staff'):
        count += 1
    if debug:
        print("<!--[DEBUG] Staff count:", count, "detected in", fname, "-->")
    return count

# header from first score:
root = ET.parse(files[0]).getroot()
print_head(files[0])

num_staffs = count_staff(files[0])

# Add opening tags for each staff
staff_data = []
staff_data.append("")
for num in range(1, num_staffs + 1):
    staff_data.append('<Staff id="' + str(num) + '">\n')

# bodies (extract each staff data from each file)
for f in files:
    if debug:
        print("<!--[DEBUG] Parsing body of", f, "-->")

    num_staffs = count_staff(f)
    
    root = ET.parse(f).getroot()
    for sf_id in range(1, num_staffs + 1):
        # 1, 2 for 2 staffs.
        for staff in root.findall('Score/Staff'):
            if str(sf_id) == str(staff.get("id")):
                strl = str(ET.tostring(staff), 'utf-8').split("\n")
                
                # Don't want first and last items (<Staff id=> and </Staff>)
                for item in strl:
                    if (not item.lstrip().startswith('<Staff id=')) and (not item.lstrip().startswith("</Staff>")):
                        staff_data[sf_id] += item + "\n"
    del root

# Add closing tag to end of each staff.
for val in range(1, num_staffs + 1):
    staff_data[val] += "\n</Staff>"

# print the staffs
for data in staff_data:
    print(data)

# print footer
print("</Score>\n</museScore>")
