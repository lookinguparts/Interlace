import json
import math

# layout = generate_layout(fixture_name, strip_points, point_spacing, num_strips_per_sculpture, sculpture_diameter)

def generate_layout(fixture_name, num_points, point_spacing, num_strips_per_sculpture, sculpture_diameter):
    layout = {
        "label": fixture_name,
        "tag": fixture_name,
        "components": []
    }

    strip_num = 0
    for strip in range(num_strips_per_sculpture):
        xpos = (sculpture_diameter / 2) * math.sin(2 * math.pi * strip / num_strips_per_sculpture)
        zpos = (sculpture_diameter / 2) * math.cos(2 * math.pi * strip / num_strips_per_sculpture)
        component = {
            "type": "strip",
            "tag": "strip" + str(strip_num),
            "x": xpos,
            "y": 0,
            "z": zpos,
            "numPoints": num_points,
            "spacing": point_spacing,
            "direction": {
                "x": 0,
                "y": 1,
                "z": 0
            }
        }
        layout["components"].append(component)
        strip_num += 1

    return layout

# Get user input for the parameters
fixture_name = str(input("enter fixture name: "))
strip_points = int(input("enter the # points in a strip: "))
point_spacing = float(input("enter the spacing between points: "))
num_strips_per_sculpture = int(input("Enter the number of strips per sculpture: "))
sculpture_diameter = float(input("Enter the diameter of the sculpture: "))

# Generate the layout
layout = generate_layout(fixture_name, strip_points, point_spacing, num_strips_per_sculpture, sculpture_diameter)

# Save the layout to a JSON file
with open(fixture_name + ".lxf", "w") as file:
    json.dump(layout, file, indent=2)

print("LED strip layout file generated successfully.")