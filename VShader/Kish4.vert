/*{
	"DESCRIPTION": "Kish4",
	"CREDIT": "by tracyscott",
	"ISFVSN": "2.0",
	"CATEGORIES": [
		"VERTEX SDF"
	],
	"INPUTS": [
         {
            "NAME": "x1",
            "TYPE": "float",
            "DEFAULT": 1.0,
            "MIN": -5.0,
            "MAX": 5.0
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
    //vec2 uv = 2. * position.yz - 1.;// + 0.5; // + 0.5;
    vec2 uv = uvwrap(position);
    uv = uv - 0.5;
    
    vec3 fragColor = vec3(0.);

    vec2 uv0 = uv;

    vec3 finalColor = vec3(0.0);

    for (float i = 0.0; i < 2.0; i++) {
        uv *= 1.5;
        uv = fract(uv);
        uv -= 0.5;

        float d = length(uv) * exp(-length(uv));
        vec3 col = paletteN(length(uv0)  + i*4. + fTime, 3.0);

        d = sin(d*8. * x1 + fTime)/8.;
        d = abs(d);
        // d = smoothstep(0.0, 0.1,  d);
        //d = 0.01 / d;
        d = pow(0.01 /d, 1.2);

        finalColor += col * d;
    }

    tPosition = clamp(finalColor, 0., 1.);
}
