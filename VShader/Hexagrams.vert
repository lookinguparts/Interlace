/*{
	"DESCRIPTION": "Hexagrams",
	"CREDIT": "by tracyscott",
	"ISFVSN": "2.0",
	"CATEGORIES": [
		"VERTEX SDF"
	],
	"INPUTS": [
         {
            "NAME": "rspeed",
            "TYPE": "float",
            "DEFAULT": 1.0,
            "MIN": -5.0,
            "MAX": 5.0
         },
         {
            "NAME": "layers",
            "TYPE": "float",
            "DEFAULT": 4.0,
            "MIN": 1.0,
            "MAX": 10.0
         },
         {
            "NAME": "brt",
            "TYPE": "float",
            "DEFAULT": 1.0,
            "MIN": 0.1,
            "MAX": 5.0
         },
         {
            "NAME": "pal",
            "TYPE": "float",
            "DEFAULT": 1.0,
            "MIN": 0.0,
            "MAX": 9.5
         },
          {
            "NAME": "pald",
            "TYPE": "float",
            "DEFAULT": 1.0,
            "MIN": 0.0,
            "MAX": 30.0
         },
         {
            "NAME": "s1",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": 0.0,
            "MAX": 1.0
         },
         {
            "NAME": "s2",
            "TYPE": "float",
            "DEFAULT": 0.2,
            "MIN": 0.0,
            "MAX": 1.0
         },
           {
            "NAME": "cspeed",
            "TYPE": "float",
            "DEFAULT": 1.0,
            "MIN": 0.0,
            "MAX": 10.0
         },
          {
            "NAME": "pw",
            "TYPE": "float",
            "DEFAULT": 1.0,
            "MIN": 0.0,
            "MAX": 5.0
         }
	]
}*/

#version 330

uniform float fTime;
uniform float rspeed;
uniform float layers;
uniform float brt;
uniform float pal;
uniform float pald;
uniform float s1;
uniform float s2;
uniform float cspeed;
uniform float pw;

layout(location = 0) in vec3 position;
out vec3 outColor;

#include <palettes.vert>
#include <sdf2d.vert>
#include <consts.vert>
#include <uvwrap.vert>


float Hash21(vec2 p) {
    p = fract(p*vec2(123.34, 456.12));
    p += dot(p, p+45.32);
    return fract(p.x*p.y);
}


float HexLayer(vec2 uv) {
    float hex = sdHexagram(uv, 0.1);
    hex = abs(hex);
    hex = smoothstep(s1, s2, hex);
    return clamp(pow(0.02 * brt / hex, pw), 0.0, 1.0);
}

void main(){
    vec2 uv = uvwrap(position);
    uv = uv - 0.5;
    vec3 col = vec3(0.);

    float t = fTime * .1;
    for (float i=0.; i<1.; i+= 1./layers) {
        float depth = fract(i + t);
        float scale = mix(6., .1, depth);
        float fade = depth*smoothstep(1, .9, depth);
        float bright = HexLayer((uv*Rot(fTime*depth*.1*rspeed))*scale)*fade;
        col += paletteN(bright*pald + i*fTime*cspeed, pal)*bright;
        //col += paletteN(bright*i*fTime*cspeed, pal)*bright;
        //col += bright;
    }

    outColor = clamp(col, 0.0, 1.0);
}
