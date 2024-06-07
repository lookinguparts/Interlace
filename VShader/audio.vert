/*{
"DESCRIPTION": "audio",
"CREDIT": "by tracyscott",
"ISFVSN": "2.0",
"CATEGORIES": [
"TEXTURE SHADER"
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

uniform sampler2D textureSampler;
uniform sampler2D audioTexture;

layout(location = 0) in vec3 position;
out vec3 tPosition;

#include <const.vert>
#include <uvwrap.vert>

void main(){
    vec2 st = uvwrap(position);
    vec3 color = vec3(0.);

    //float x = st.x - 0.5;
    //float y = st.y - 0.5;
    //st.y = -x * sin(fTime) + y * cos(fTime);
    //st.x = x * cos(fTime) + y * sin(fTime);
    //st.y += 0.5;
    //st.x += 0.5;
    //color = texture(textureSampler, st).rgb;
    float mag = texture(audioTexture, vec2(st.x*0.5, 0.0)).r;
    if (st.y > mag) {
        color = vec3(0.0, 0.0, 0.0);
    } else {
        color = vec3(1.0, 1.0, 1.0);
    }
    tPosition = color;
}
