import csv
import glob

TIME = 0
POSITION = 1


def get_all_files() -> [str]:
    return glob.glob("../output-files/particle-movement-*.csv")


def get_all_static_files() -> [str]:
    return glob.glob("../output-files/static-data-*.csv")


def get_particle_data(filename: str):
    time = []
    position = []

    with open(filename, 'r') as file:
        reader = csv.reader(file)

        for row in reader:
            time.append(float(row[0]))
            position.append(float(row[1]))

    return time, position


def get_static_data(file_name: str):
    data = {}

    with open(file_name, mode='r') as file:
        csv_reader = csv.reader(file)
        for row in csv_reader:
            key = row[0]

            if row[1].isdigit():
                value = int(row[1])
            elif is_float(row[1]):
                value = float(row[1])
            else:
                value = row[1]

            data[key] = value
    return data


def is_float(string):
    try:
        float(string)
        return True
    except ValueError:
        return False


def get_data():
    data = []
    for filename in get_all_files():
        data.append(get_particle_data(filename))
    return data


def get_all_static_data():
    data = []
    for file in get_all_static_files():
        data.append(get_static_data(file))
    return data
