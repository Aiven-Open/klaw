import os, sys

def main(argv):
    s = argv
    print (''.join(e for e in s if e.isalnum()))
    
if __name__ == "__main__":
    main(sys.argv[1])
