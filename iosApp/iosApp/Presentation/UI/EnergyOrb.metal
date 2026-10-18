//
// File        : EnergyOrb.metal
// Author      : Tran Van Hoai
// Date        : 18/10/26
//

#include <metal_stdlib>
#include <SwiftUI/SwiftUI_Metal.h>
using namespace metal;

#define PI 3.14159265359f
#define RIBBONS 8
#define STRANDS 64.0f
#define RING_WIDTH 0.9f
#define RING_STRENGTH 0.45f
#define LIMB_FADE 0.35f

static float3 toObject(float3 q, float yaw, float tilt) {
    float ct = cos(tilt), st = sin(tilt);
    float y  =  q.y * ct + q.z * st;
    float z1 = -q.y * st + q.z * ct;
    float cy = cos(yaw), sy = sin(yaw);
    float x  = q.x * cy - z1 * sy;
    float z  = q.x * sy + z1 * cy;
    return float3(x, y, z);
}

static float2 ribbonsAt(float3 p, float t, float R) {
    float lat = asin(clamp(p.y, -1.0f, 1.0f));
    float lon = atan2(p.x, p.z);
    float cl  = cos(lat);

    float total = 0.0f;
    float hot   = 0.0f;

    for (int i = 0; i < RIBBONS; i++) {
        float rf   = float(i);
        float base = rf / float(RIBBONS) * 2.0f * PI;

        float start = base
                    + 0.83f * lat
                    + 0.35f * sin(t * 0.6f + rf * 1.7f + lat * 2.2f)
                    + 0.40f * sin(t * 0.4f + rf * 2.0f);

        float w = 0.15f + 0.95f * (0.5f + 0.5f * sin(t * 0.5f + rf + lat * 2.0f));

        float u = fmod(lon - start, 2.0f * PI);
        if (u < 0.0f) u += 2.0f * PI;
        float f = u / w;
        if (f > 1.0f) continue;

        float mask = smoothstep(0.0f, 0.06f, f) * smoothstep(0.0f, 0.06f, 1.0f - f);
        float edge = pow(abs(f - 0.5f) * 2.0f, 3.0f);

        float strandPx = w * cl * R / STRANDS;
        float fade = smoothstep(0.4f, 1.6f, strandPx);
        float line = 0.5f + 0.5f * cos(f * STRANDS * 2.0f * PI);
        float fine = mix(0.45f, 0.15f + 1.1f * line * line, fade);

        float pole = 0.4f + 0.6f * sqrt(max(cl, 0.0f));
        float v = mask * fine * pole;

        total += v * (0.10f + 0.9f * edge);
        hot   += v * edge;
    }
    return float2(total, hot);
}

[[ stitchable ]] half4 energyOrb(float2 position, half4 color,
                                 float2 size, float time,
                                 half4 tint, float highlight) {
    float2 c = size * 0.5f;
    float R  = min(size.x, size.y) * 0.40f;
    float2 q = (position - c) / R;
    q.y = -q.y;
    float r2 = dot(q, q);
    float r  = sqrt(r2);
    float aa = 1.0f / R;

    float yaw  = time * 0.25f;
    float tilt = 0.35f + 0.15f * sin(time * 0.3f);

    float3 base  = float3(tint.rgb);
    float3 white = float3(1.0f);
    float3 E = float3(0.0f);

    if (r < 1.0f) {
        float z = sqrt(max(1.0f - r2, 0.0f));

        float2 fr = ribbonsAt(toObject(float3(q,  z), yaw, tilt), time, R);
        float2 bk = ribbonsAt(toObject(float3(q, -z), yaw, tilt), time, R);

        float limb = smoothstep(0.0f, LIMB_FADE, z);

        float bright = (fr.x + 0.4f * bk.x) * limb;
        float hot    = (fr.y + 0.4f * bk.y) * limb;

        float bodyFill = 0.10f;
        E += base * (bodyFill + 0.9f * bright);
        E += white * highlight * 0.8f * hot;
    }

    float d = (r - 1.0f) / (aa * RING_WIDTH);
    float ring = exp(-d * d);
    E += (base * 0.6f + white * highlight * 0.3f) * ring * RING_STRENGTH;

    float2 n = abs(position - c) / size;
    float border = 1.0f - smoothstep(0.36f, 0.5f, max(n.x, n.y));
    float halo = 0.08f * exp(-max(r - 1.0f, 0.0f) * 14.0f)
               * smoothstep(1.0f - aa, 1.0f + aa, r) * border;
    E += base * halo;

    float m = max(E.r, max(E.g, E.b));
    float a = min(m, 1.0f);
    float3 rgb = E / max(m, 1.0f);
    return half4(half3(rgb), half(a));
}
