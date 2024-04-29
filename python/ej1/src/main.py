from utils import *
from graph import *

if __name__ == "__main__":
    data = get_data()
    static_data = get_all_static_data()
    file = "../output-files/par"

    graph_positions([data[0]], [static_data[0]])
    graph_error([data[0]], [static_data[0]])


