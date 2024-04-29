from utils import *
from graph import *

if __name__ == "__main__":
    data = get_data()
    static_data = get_all_static_data()

    # graph_positions(data, static_data)
    graph_error(data, static_data)


