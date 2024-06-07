/*{
	"DESCRIPTION": "stmap",
	"CREDIT": "by tracyscott",
	"ISFVSN": "2.0",
	"CATEGORIES": [
		"VERTEX SDF"
	],
	"INPUTS": [
         {
            "NAME": "s",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": -5.0,
            "MAX": 5.0
         },
         {
            "NAME": "t",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": -5.0,
            "MAX": 5.0
         },
         {
            "NAME": "sscl",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": 0.1,
            "MAX": 10.0
         },
         {
            "NAME": "tscl",
            "TYPE": "float",
            "DEFAULT": 1.0,
            "MIN": 0.1,
            "MAX": 10.0
         }
	]
}*/

#version 330

uniform float fTime;
uniform float s;
uniform float t;
uniform float sscl;
uniform float tscl;

layout(location = 0) in vec3 position;
out vec3 tPosition;

#include <const.vert>
#include <uvwrap.vert>

void main() {
    vec2 st = uvwrap(position);
    st = vec2((st.x - s)*sscl, (st.y-t)*tscl);
    vec3 fragColor = vec3(st.x,st.y, 0.0);
    tPosition = vec3(clamp(fragColor.x, 0.0, 1.0), clamp(fragColor.y, 0.0, 1.0), clamp(fragColor.z, 0.0, 1.0));
}
