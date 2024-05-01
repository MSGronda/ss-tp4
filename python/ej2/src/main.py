from ej2.src.animation import animate
from utils import *

if __name__ == "__main__":
    time, body_data = get_body_data(get_all_files()[0])
    properties = get_static_data(get_all_static_files()[0])

    animate(body_data, properties, 5E-9)