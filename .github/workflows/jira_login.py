import os, sys

def main(mode):
    user_data = os.environ['USER_DATA']
    user_data = user_data.split(',')
    if mode == "email":
        print (user_data[0].strip().replace("\n", ""))
    elif mode == "token":
        print (user_data[1].strip().replace("\n", ""))

if __name__ == "__main__":
    main(sys.argv[1])
