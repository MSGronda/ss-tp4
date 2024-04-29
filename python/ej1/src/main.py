from utils import *
from graph import *

if __name__ == "__main__":
    data = get_data()
    file = "../output-files/par"

    graph_positions([data[0], data[2]])
    graph_error([data[0], data[2]])


