//
// File        : EnergyOrbShader.kt
// Author      : Tran Van Hoai
// Date        : 21/10/26
//

package org.hinsun.edge.presentation.ui

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.toArgb

/**
 * Orb v1: AGSL port of iosApp `EnergyOrb.metal` / `EnergyOrbMetal`.
 * RuntimeShader needs Android 13 (API 33); older devices get the dotLottie orb v4 instead.
 */
@Composable
fun EnergyOrbShader(
    modifier: Modifier = Modifier,
    tint: Color = Color.hsv(216f, 0.85f, 1f),
    speed: Float = 5f,
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        EnergyOrbV4(modifier)
        return
    }

    val shader = remember { RuntimeShader(ENERGY_ORB_AGSL) }
    val highlight = if (isSystemInDarkTheme()) 0.6f else 1f
    var time by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(speed) {
        val start = withFrameNanos { it }
        while (true) withFrameNanos { time = (it - start) / 1e9f * speed }
    }

    Canvas(modifier.aspectRatio(1f)) {
        // Uniforms are set in the draw phase, so each frame redraws without recomposing.
        shader.setFloatUniform("size", size.width, size.height)
        shader.setFloatUniform("time", time)
        shader.setColorUniform("tint", tint.toArgb())
        shader.setFloatUniform("highlight", highlight)
        drawRect(ShaderBrush(shader))
    }
}

private const val ENERGY_ORB_AGSL = """
uniform float2 size;
uniform float time;
layout(color) uniform half4 tint;
uniform float highlight;

const float PI = 3.14159265359;
const int RIBBONS = 8;
const float STRANDS = 64.0;
const float RING_WIDTH = 0.9;
const float RING_STRENGTH = 0.45;
const float LIMB_FADE = 0.35;

float3 toObject(float3 q, float yaw, float tilt) {
    float ct = cos(tilt), st = sin(tilt);
    float y  =  q.y * ct + q.z * st;
    float z1 = -q.y * st + q.z * ct;
    float cy = cos(yaw), sy = sin(yaw);
    float x  = q.x * cy - z1 * sy;
    float z  = q.x * sy + z1 * cy;
    return float3(x, y, z);
}

float2 ribbonsAt(float3 p, float t, float R) {
    float lat = asin(clamp(p.y, -1.0, 1.0));
    float lon = atan(p.x, p.z);
    float cl  = cos(lat);

    float total = 0.0;
    float hot   = 0.0;

    for (int i = 0; i < RIBBONS; i++) {
        float rf   = float(i);
        float base = rf / float(RIBBONS) * 2.0 * PI;

        float start = base
                    + 0.83 * lat
                    + 0.35 * sin(t * 0.6 + rf * 1.7 + lat * 2.2)
                    + 0.40 * sin(t * 0.4 + rf * 2.0);

        float w = 0.15 + 0.95 * (0.5 + 0.5 * sin(t * 0.5 + rf + lat * 2.0));

        // GLSL mod() is floor-based, so u is already in [0, 2PI) unlike Metal's fmod.
        float u = mod(lon - start, 2.0 * PI);
        float f = u / w;
        if (f <= 1.0) {
            float mask = smoothstep(0.0, 0.06, f) * smoothstep(0.0, 0.06, 1.0 - f);
            float edge = pow(abs(f - 0.5) * 2.0, 3.0);

            float strandPx = w * cl * R / STRANDS;
            float fade = smoothstep(0.4, 1.6, strandPx);
            float line = 0.5 + 0.5 * cos(f * STRANDS * 2.0 * PI);
            float fine = mix(0.45, 0.15 + 1.1 * line * line, fade);

            float pole = 0.4 + 0.6 * sqrt(max(cl, 0.0));
            float v = mask * fine * pole;

            total += v * (0.10 + 0.9 * edge);
            hot   += v * edge;
        }
    }
    return float2(total, hot);
}

half4 main(float2 position) {
    float2 c = size * 0.5;
    float R  = min(size.x, size.y) * 0.40;
    float2 q = (position - c) / R;
    q.y = -q.y;
    float r2 = dot(q, q);
    float r  = sqrt(r2);
    float aa = 1.0 / R;

    float yaw  = time * 0.25;
    float tilt = 0.35 + 0.15 * sin(time * 0.3);

    float3 base  = float3(tint.rgb);
    float3 white = float3(1.0);
    float3 E = float3(0.0);

    if (r < 1.0) {
        float z = sqrt(max(1.0 - r2, 0.0));

        float2 fr = ribbonsAt(toObject(float3(q,  z), yaw, tilt), time, R);
        float2 bk = ribbonsAt(toObject(float3(q, -z), yaw, tilt), time, R);

        float limb = smoothstep(0.0, LIMB_FADE, z);

        float bright = (fr.x + 0.4 * bk.x) * limb;
        float hot    = (fr.y + 0.4 * bk.y) * limb;

        float bodyFill = 0.10;
        E += base * (bodyFill + 0.9 * bright);
        E += white * highlight * 0.8 * hot;
    }

    float d = (r - 1.0) / (aa * RING_WIDTH);
    float ring = exp(-d * d);
    E += (base * 0.6 + white * highlight * 0.3) * ring * RING_STRENGTH;

    float2 n = abs(position - c) / size;
    float border = 1.0 - smoothstep(0.36, 0.5, max(n.x, n.y));
    float halo = 0.08 * exp(-max(r - 1.0, 0.0) * 14.0)
               * smoothstep(1.0 - aa, 1.0 + aa, r) * border;
    E += base * halo;

    // Premultiplied output, same as the SwiftUI colorEffect.
    float m = max(E.r, max(E.g, E.b));
    float a = min(m, 1.0);
    float3 rgb = E / max(m, 1.0);
    return half4(half3(rgb), half(a));
}
"""
