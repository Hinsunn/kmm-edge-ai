//
// File        : EnergyOrbV2.metal
// Author      : Tran Van Hoai
// Date        : 21/10/26
//

#include <metal_stdlib>
#include <SwiftUI/SwiftUI_Metal.h>
using namespace metal;

// Smoke swirling inside a glass ball. Each pixel marches through the sphere;
// smoke is a stack of very thin noise iso-layers, so sheets glow where seen
// edge-on (silky folds) and stay translucent face-on. Density hugs the wall,
// and periodic gusts push sheets into the middle ("blown into the ball").
#define STEPS 24

static float hash12(float2 p) {
    float3 p3 = fract(float3(p.xyx) * 0.1031f);
    p3 += dot(p3, p3.yzx + 33.33f);
    return fract((p3.x + p3.y) * p3.z);
}

static float hash3(float3 p) {
    p = fract(p * 0.3183099f + 0.1f);
    p *= 17.0f;
    return fract(p.x * p.y * p.z * (p.x + p.y + p.z));
}

static float noise3(float3 x) {
    float3 i = floor(x);
    float3 f = fract(x);
    f = f * f * (3.0f - 2.0f * f);
    return mix(mix(mix(hash3(i + float3(0, 0, 0)), hash3(i + float3(1, 0, 0)), f.x),
                   mix(hash3(i + float3(0, 1, 0)), hash3(i + float3(1, 1, 0)), f.x), f.y),
               mix(mix(hash3(i + float3(0, 0, 1)), hash3(i + float3(1, 0, 1)), f.x),
                   mix(hash3(i + float3(0, 1, 1)), hash3(i + float3(1, 1, 1)), f.x), f.y), f.z);
}

static float3 rotY(float3 p, float a) {
    float c = cos(a), s = sin(a);
    return float3(p.x * c + p.z * s, p.y, -p.x * s + p.z * c);
}

static float3 rotX(float3 p, float a) {
    float c = cos(a), s = sin(a);
    return float3(p.x, p.y * c - p.z * s, p.y * s + p.z * c);
}

static float smoke(float3 p, float t) {
    float rad = length(p);

    // Swirl: the ball's content spins, faster near the wall, twisting with height.
    float3 s = rotY(p, t * 0.55f + p.y * 1.4f + rad * 1.2f);
    s = rotX(s, 0.5f + 0.3f * sin(t * 0.37f));

    // Turbulent displacement, then thin iso-layers -> silk sheets.
    float d = noise3(s * 1.7f + float3(0, -t * 0.6f, 0));
    // Layers roughly parallel to the glass (radius dominates), deformed by noise,
    // so they are thin face-on and bright where they turn edge-on.
    float n = rad * 5.0f + 0.9f * noise3(s * 2.0f + d * 1.5f + float3(t * 0.25f, 0, 0))
            + 0.4f * noise3(s * 5.0f + d * 2.0f - float3(0, t * 0.5f, 0))   // wisps
            + 0.18f * noise3(s * 11.0f + float3(t * 0.7f, 0, 0));         // fine filaments
    float sheet = exp(-pow((fract(n) - 0.5f) / 0.045f, 2.0f));

    // Smoke hugs the glass; gusts push it inward in a region that wanders.
    float gust = smoothstep(0.62f, 0.92f, noise3(p * 1.2f + float3(0, 0, t * 0.35f)));
    float inner = mix(0.74f, 0.25f, gust);
    float wall  = smoothstep(inner, 0.98f, rad) * (1.0f - smoothstep(0.985f, 1.0f, rad));
    return sheet * wall
         * (0.35f + 0.65f * d)
         // Large drifting clumps: dense white on one side, clear on another.
         * smoothstep(0.3f, 0.6f, noise3(p * 1.1f + float3(t * 0.3f, -t * 0.2f, 7.0f)));
}

[[ stitchable ]] half4 energyOrbV2(float2 position, half4 color,
                                   float2 size, float time, half4 tint) {
    float2 c = size * 0.5f;
    float R  = min(size.x, size.y) * 0.38f;
    float2 q = (position - c) / R;
    q.y = -q.y;
    float r2 = dot(q, q);
    float r  = sqrt(r2);
    float px = 1.0f / R;
    float t  = time;

    float E = 0.0f;
    if (r < 1.0f) {
        float zs = sqrt(1.0f - r2);
        float dz = 2.0f * zs / float(STEPS);
        // Per-pixel jitter hides step banding (reads as fine smoke grain).
        float z  = -zs + dz * hash12(position);
        float acc = 0.0f;
        for (int i = 0; i < STEPS; i++) {
            float3 p = float3(q, z);
            // Wall-parallel sheets only glow where seen edge-on.
            float edgeOn = pow(1.0f - abs(z) / max(length(p), 1e-3f), 2.4f);
            // Back half slightly dimmer for depth.
            acc += smoke(p, t) * (0.002f + edgeOn) * (z > 0.0f ? 1.0f : 0.7f);
            z += dz;
        }
        E += acc * dz * 95.0f;
    }

    // Crisp glass outline + soft inner glow hugging it.
    float edge = (r - 0.995f) / (1.8f * px);
    E += 0.25f * exp(-edge * edge);
    if (r < 1.0f) E += 0.05f * exp(-(1.0f - r) / 0.025f);

    float a = 1.0f - exp(-E * 1.6f);
    return half4(tint.rgb * half(a), half(a));
}
