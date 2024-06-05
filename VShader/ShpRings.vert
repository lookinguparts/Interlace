/*{
	"DESCRIPTION": "ShpRings",
	"CREDIT": "by tracyscott",
	"ISFVSN": "2.0",
	"CATEGORIES": [
		"VERTEX SDF"
	],
	"INPUTS": [
         {
            "NAME": "shp",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": 0.0,
            "MAX": 3.5
         },
         {
            "NAME": "s1",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": -1.1,
            "MAX": 1.1
         },
         {
            "NAME": "s2",
            "TYPE": "float",
            "DEFAULT": 0.1,
            "MIN": -1.1,
            "MAX": 1.1
         },
         {
            "NAME": "r1",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": 0.0,
            "MAX": 2.0
         },
          {
            "NAME": "r2",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": 0.0,
            "MAX": 2.0
         },
          {
            "NAME": "h",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": 0.0,
            "MAX": 2.0
         },
         {
            "NAME": "thick",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": 0.0,
            "MAX": .3
         },
         {
            "NAME": "zoom",
            "TYPE": "float",
            "DEFAULT": 1.0,
            "MIN": -4.0,
            "MAX": 4.0
         },
          {
            "NAME": "brt",
            "TYPE": "float",
            "DEFAULT": 1.0,
            "MIN": .1,
            "MAX": 20.
         },
         {
            "NAME": "palval",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": 0.0,
            "MAX": 9.9
         },
          {
            "NAME": "pw",
            "TYPE": "float",
            "DEFAULT": 1.3,
            "MIN": 0.0,
            "MAX": 5.0
         },
          {
            "NAME": "rspeed",
            "TYPE": "float",
            "DEFAULT": 0.0,
            "MIN": 0.0,
            "MAX": 10.0
         }
	]
}*/

#version 330


uniform float fTime;
uniform float shp;
uniform float s1;
uniform float s2;
uniform float r1;
uniform float r2;
uniform float h;
uniform float thick;
uniform float zoom;
uniform float brt;
uniform float palval;
uniform float pw;
uniform float rspeed;

#include <palettes.vert>
#include <sdf2d.vert>
#include <const.vert>
#include <uvwrap.vert>

layout(location = 0) in vec3 position;
out vec3 tPosition;

float ring(vec2 p, float r1, float r2, float h) {
    //float d = sdUnevenCapsule(ruv3, r1, r2, h);
    //float d = sdVesica(ruv3, r1, h);
    //float d = sdEquilateralTriangle(ruv3, r1);
    //float d = sdBox(ruv3, vec2(r1,r2));
    float d = shapeN(shp, p, r1, r2, h);

    d = abs(d)-thick;
    d = smoothstep(s1, s2, d);
    d = pow(brt*0.01/d, pw);
    return clamp(d, 0., 1.);
}

mat2 Rot(float a) {
    float s=sin(a), c=cos(a);
    return mat2(c, -s, s, c);
}


void main() {
    vec2 uv = uvwrap(position);
    //vec2 uv = position.xy - 0.5;
    //vec2 uv = position.zy - 0.5;

    // uv above is 0 to 1, so move center to 0, 0
    uv = uv - 0.5;
    vec3 color = vec3(1., 1., 1.);

    float pal_d = length(uv);

    vec2 ruv = uv;
    ruv = ruv * Rot(fTime * rspeed * .1);
    ruv *= zoom;
 
    float bright = 0.;
    for (float i = 0.; i < 6.; i++) {
        vec2 iruv = Rot(i * PI/3.) * ruv;
        float d = ring(iruv + vec2(0., .5), r1, r2, h);
        bright += d;
    }

    color *= vec3(clamp(paletteN(pal_d + fTime * .5, palval)*bright, 0., 1.));

    tPosition = color;
}
