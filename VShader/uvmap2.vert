/*{
	"DESCRIPTION": "uvmap2",
	"CREDIT": "by tracyscott",
	"ISFVSN": "2.0",
	"CATEGORIES": [
		"VERTEX SDF"
	],
	"INPUTS": [
         {
            "NAME": "x",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": -1.0,
            "MAX": 1.0
         },
         {
            "NAME": "y",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": -1.0,
            "MAX": 1.0
         },
         {
            "NAME": "z",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": -1.0,
            "MAX": 1.0
         },
         {
            "NAME": "xscl",
            "TYPE": "float",
            "DEFAULT": 1.0,
            "MIN": 0.0,
            "MAX": 10.0
         },
         {
            "NAME": "yscl",
            "TYPE": "float",
            "DEFAULT": 1.0,
            "MIN": 0.0,
            "MAX": 10.0
         },
         {
            "NAME": "zscl",
            "TYPE": "float",
            "DEFAULT": 1.0,
            "MIN": 0.0,
            "MAX": 10
         }
	]
}*/

#version 330

uniform float fTime;
uniform float x;
uniform float y;
uniform float z;
uniform float xscl;
uniform float yscl;
uniform float zscl;

layout(location = 0) in vec3 position;
out vec3 tPosition;

// Pi, the ratio of a circle's circumference to its diameter.
const float M_PI = 3.14159265358979323846264338327950288;
const float PI = M_PI;

// Pi divided by two (pi/2)
const float M_PI_2 = 1.57079632679489661923132169163975144;

// Pi divided by four  (pi/4)
const float M_PI_4 = 0.785398163397448309615660845819875721;

// The reciprocal of pi (1/pi)
const float M_1_PI = 0.318309886183790671537767526745028724;


vec2 map_coordinates_to_texture_space(vec3 position) {
    // Map y coordinates to v in texture space
   float v = position.y;

    // Map x and z coordinates to u (theta angle) in texture space
    float theta = atan(position.z, position.x);
    if (theta < 0) {
        theta = theta + 2.0 * M_PI;
    }
    float u = theta / (2.0 * M_PI);
    
    return vec2(u, v);
}


void main() {
    vec3 uposition = vec3((position.x - 0.5)*2.0, position.y, (position.z - 0.5)*2.0);
    vec2 uv = map_coordinates_to_texture_space(uposition);
    uv = vec2((uv.x - x)*xscl, (uv.y-y)*yscl);
    vec3 fragColor = vec3(uv.x,uv.y, 0.0);
    tPosition = vec3(clamp(fragColor.x, 0.0, 1.0), clamp(fragColor.y, 0.0, 1.0), clamp(fragColor.z, 0.0, 1.0));
}
