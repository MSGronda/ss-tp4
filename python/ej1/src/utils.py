import csv
import glob

TIME = 0
POSITION = 1
NAME = 2


def get_all_files() -> [str]:
    return glob.glob("../output-files/particle-movement-*.csv")


def get_particle_data(filename: str):
    time = []
    position = []

    with open(filename, 'r') as file:
        reader = csv.reader(file)

        for row in reader:
            time.append(float(row[0]))
            position.append(float(row[1]))

    return time, position, filename.replace('../output-files\particle-movement-', '').replace('.csv', '')


def get_data():
    data = []
    for filename in get_all_files():
        data.append(get_particle_data(filename))
    return data
