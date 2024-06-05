/*{
	"DESCRIPTION": "Kish3",
	"CREDIT": "by tracyscott",
	"ISFVSN": "2.0",
	"CATEGORIES": [
		"VERTEX SDF"
	],
	"INPUTS": [
         {
            "NAME": "x1",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": -0.1,
            "MAX": 1.1
         },
         {
            "NAME": "y1",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": -0.1,
            "MAX": 1.1
         }
	]
}*/

#version 330

uniform float fTime;
uniform float x1;
uniform float y1;

layout(location = 0) in vec3 position;
out vec3 tPosition;

#include <palettes.vert>
#include <sdf2d.vert>
#include <const.vert>
#include <uvwrap.vert>


void main(){
    vec2 uv = uvwrap(position);
    uv = uv - 0.5;
    vec3 fragColor = vec3(0.);

    vec2 uv0 = uv;

    uv *= 2.0;
    uv = fract(uv);
    uv -= 0.5;

    float d = length(uv);
    vec3 col = paletteN(length(uv0) + fTime, 3.0);

    d = sin(d*8. + fTime)/8.;
    d = abs(d);
    // d = smoothstep(0.0, 0.1,  d);
    d = 0.02 / d;

    col *= d;

    //d = clamp(d, 0., 1.);
    fragColor = col;
    tPosition = clamp(fragColor, 0., 1.);
}
