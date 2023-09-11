package com.lowdragmc.photon.client.emitter.data;

import com.lowdragmc.lowdraglib.gui.editor.annotation.Configurable;
import com.lowdragmc.lowdraglib.gui.editor.annotation.NumberRange;
import com.lowdragmc.lowdraglib.utils.Range;
import com.lowdragmc.photon.client.emitter.data.number.Constant;
import com.lowdragmc.photon.client.emitter.data.number.NumberFunction;
import com.lowdragmc.photon.client.emitter.data.number.NumberFunctionConfig;
import com.lowdragmc.photon.client.emitter.data.number.RandomConstant;
import com.lowdragmc.photon.client.emitter.data.number.curve.Curve;
import com.lowdragmc.photon.client.emitter.data.number.curve.CurveConfig;
import com.lowdragmc.photon.client.emitter.data.number.curve.RandomCurve;
import com.lowdragmc.photon.client.particle.LParticle;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Vector4f;

/**
 * @author KilaBash
 * @date 2023/5/31
 * @implNote UVAnimation
 */
@Environment(EnvType.CLIENT)
public class UVAnimationSetting extends ToggleGroup {
    public enum Animation {
        WholeSheet,
        SingleRow,
    }

    @Setter
    @Getter
    @Configurable(tips = "photon.emitter.config.uvAnimation.tiles")
    @NumberRange(range = {1, Integer.MAX_VALUE})
    protected Range tiles = new Range(1, 1);

    @Setter
    @Getter
    @Configurable(tips = "photon.emitter.config.uvAnimation.animation")
    protected Animation animation = Animation.WholeSheet;

    @Setter
    @Getter
    @Configurable(tips = "photon.emitter.config.uvAnimation.frameOverTime")
    @NumberFunctionConfig(types = {Constant.class, RandomConstant.class, Curve.class, RandomCurve.class}, min = 0, curveConfig = @CurveConfig(bound = {0, 4}, xAxis = "lifetime", yAxis = "frame over time"))
    protected NumberFunction frameOverTime = NumberFunction.constant(0);

    @Setter
    @Getter
    @Configurable(tips = "photon.emitter.config.uvAnimation.startFrame")
    @NumberFunctionConfig(types = {Constant.class, RandomConstant.class}, min = 0)
    protected NumberFunction startFrame = NumberFunction.constant(0);

    @Setter
    @Getter
    @Configurable(tips = "photon.emitter.config.uvAnimation.cycle")
    @NumberRange(range = {0, Integer.MAX_VALUE}, wheel = 1)
    protected float cycle = 1;

    public Vector4f getUVs(LParticle particle, float partialTicks) {
        var t = particle.getT(partialTicks);
        var cellU = 1f / tiles.getA().intValue();
        var cellV = 1f / tiles.getB().intValue();
        var currentFrame = this.startFrame.get(t, () -> particle.getMemRandom("startFrame")).floatValue();
        currentFrame += cycle * frameOverTime.get(t, () -> particle.getMemRandom("frameOverTime")).floatValue();
        float u0, v0, u1, v1;
        int cellSize;
        if (animation == Animation.WholeSheet) {
            cellSize = tiles.getA().intValue() * tiles.getB().intValue();
            int X = (int) (currentFrame % tiles.getA().intValue());
            int Y = (int) (currentFrame / tiles.getA().intValue());
            u0 = X * cellU;
            v0 = Y * cellV;
        } else {
            cellSize = tiles.getA().intValue();
            int X = (int) (currentFrame % cellSize);
            int Y = (int) (particle.getMemRandom("randomRow") * tiles.getB().intValue());
            u0 = X * cellU;
            v0 = Y * cellV;
        }
        u1 = u0 + cellU;
        v1 = v0 + cellV;
        return new Vector4f(u0, v0, u1, v1);
    }
}
