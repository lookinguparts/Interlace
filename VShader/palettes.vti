// http://dev.thi.ng/gradients/
vec3 palette(in float t, in vec3 a, in vec3 b, in vec3 c, in vec3 d)
{
    return a + b*cos(6.28318* (c*t + d));
}


vec3 palette0(float t) {
    vec3 a = vec3(0.5, 0.5, 0.5);
    vec3 b = vec3(0.5, 0.5, 0.5);
    vec3 c = vec3(1.0, 1.0, 1.0);
    vec3 d = vec3(0.263, 0.416, 0.557);
    return palette(t, a, b, c, d);
}

// orange green blue pink
//[[0.572 0.574 0.518] [0.759 0.171 0.358] [1.022 0.318 0.620] [3.138 5.671 -0.172]]
vec3 palette1(float t) {
    vec3 a = vec3(0.572, 0.574, 0.518);
    vec3 b = vec3(0.759, 0.171, 0.358);
    vec3 c = vec3(1.022, 0.318, 0.620);
    vec3 d = vec3(3.138, 5.5671, -0.172);
    return palette(t, a, b, c, d);
}

// neon green red purple
// [[0.846 0.430 0.206] [0.349 0.678 0.651] [0.690 1.319 0.654] [6.205 2.511 3.523]]
vec3 palette2(float t) {
    vec3 a = vec3(0.846, 0.430, 0.206);
    vec3 b = vec3(0.349, 0.678, 0.651);
    vec3 c = vec3(0.690, 1.319, 0.654);
    vec3 d = vec3(6.205, 2.511, 3.523);
    return palette(t, a, b, c, d);
}

// [[0.806 0.355 0.693] [0.802 0.464 0.260] [1.514 1.131 1.197] [1.015 0.738 3.202]]
vec3 palette3(float t) {
    vec3 a = vec3(0.806, 0.355, 0.693);
    vec3 b = vec3(0.802, 0.464, 0.260);
    vec3 c = vec3(1.514, 1.131, 1.197);
    vec3 d = vec3(1.015, 0.783, 3.202);
    return palette(t, a, b, c, d);
}

// yellow red yellow
//[[0.990 0.520 0.071] [0.063 0.800 0.918] [1.548 0.740 0.062] [1.261 5.091 5.773]]
vec3 palette4(float t) {
    vec3 a = vec3(0.990, 0.520, 0.071);
    vec3 b = vec3(0.063, 0.800, 0.918);
    vec3 c = vec3(1.548, 0.740, 0.062);
    vec3 d = vec3(1.261, 5.091, 5.773);
    return palette(t, a, b, c, d);
}

// many color
// [[0.481 0.619 0.755] [0.424 0.158 0.810] [3.136 1.650 2.155] [4.963 4.889 4.418]]
vec3 palette5(float t) {
    vec3 a = vec3(0.481, 0.619, 0.755);
    vec3 b = vec3(0.424, 0.158, 0.810);
    vec3 c = vec3(3.136, 1.650, 2.155);
    vec3 d = vec3(4.963, 4.889, 4.418);
    return palette(t, a, b, c, d);
}

// red cyan blue purple
//[[0.354 -0.322 0.578] [0.321 0.861 0.394] [1.197 1.258 0.758] [0.788 0.368 0.434]]
vec3 palette6(float t) {
    vec3 a = vec3(0.354, -0.322, 0.578);
    vec3 b = vec3(0.321, 0.861, 0.394);
    vec3 c = vec3(1.197, 1.258, 0.758);
    vec3 d = vec3(0.788, 0.368, 0.434);
    return palette(t, a, b, c, d);
}

// multi pastel
//[[0.768 0.748 0.828] [0.798 0.108 1.048] [3.108 0.798 2.008] [2.808 1.998 4.544]]
vec3 palette7(float t) {
    vec3 a = vec3(0.768, 0.748, 0.828);
    vec3 b = vec3(0.798, 0.108, 1.048);
    vec3 c = vec3(3.1, 0.798, 2.008);
    vec3 d = vec3(2.808, 1.998, 4.544);
    return palette(t, a, b, c, d);
}


// purple white blue green
//[[0.472 0.658 0.577] [0.837 0.606 0.653] [1.025 1.508 0.407] [2.753 4.488 2.828]]
vec3 palette8(float t) {
    vec3 a = vec3(0.472, 0.658, 0.577);
    vec3 b = vec3(0.837, 0.606, 0.653);
    vec3 c = vec3(1.025, 1.508, 0.407);
    vec3 d = vec3(2.753, 4.488, 2.828);
    return palette(t, a, b, c, d);
}

// orange green blue pink
//[[0.572 0.574 0.518] [0.759 0.171 0.358] [1.022 0.318 0.620] [3.138 5.671 -0.172]]
vec3 palette9(float t) {
    return clamp(vec3(t, t, t), 0.0, 1.0);
    //vec3 a = vec3(0.572, 0.574, 0.518);
    //vec3 b = vec3(0.759, 0.171, 0.358);
    //vec3 c = vec3(1.022, 0.318, 0.620);
    //vec3 d = vec3(3.138, 5.5671, -0.172);
    //return palette(t, a, b, c, d);
}



// which palette to use.
vec3 paletteN(in float t, in float pal_num) {
    pal_num = floor(pal_num);
    if (pal_num == 0.)
    return palette0(t);
    if (pal_num == 1.)
    return palette1(t);
    if (pal_num == 2.)
    return palette2(t);
    if (pal_num == 3.)
    return palette3(t);
    if (pal_num == 4.)
    return palette4(t);
    if (pal_num == 5.)
    return palette5(t);
    if (pal_num == 6.)
    return palette6(t);
    if (pal_num == 7.)
    return palette7(t);
    if (pal_num == 8.)
    return palette8(t);
    if (pal_num == 9.)
    return palette9(t);
    return palette0(t);
}
