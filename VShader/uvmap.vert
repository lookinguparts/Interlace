/*{
	"DESCRIPTION": "uvmap",
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


void main() {
    vec3 fragColor = vec3((position.x - x) * xscl, (position.y-y) * yscl, (position.z-z) * zscl);

    tPosition = vec3(clamp(abs(fragColor.x), 0.0, 1.0), clamp(abs(fragColor.y), 0.0, 1.0), clamp(abs(fragColor.z), 0.0, 1.0));
}
