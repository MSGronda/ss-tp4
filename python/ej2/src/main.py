from ej2.src.animation import animate, animate_parallel
from utils import *

if __name__ == "__main__":
    time, body_data = get_body_data(get_all_files()[0])
    properties = get_static_data(get_all_static_files()[0])

    animate_parallel(body_data, properties, 1080, 8)

