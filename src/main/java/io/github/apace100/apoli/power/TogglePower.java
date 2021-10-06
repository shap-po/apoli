package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtElement;

public class TogglePower extends Power implements Active {

    private boolean isActive;

    public TogglePower(PowerType<?> type, LivingEntity entity) {
        this(type, entity, false);
    }

    public TogglePower(PowerType<?> type, LivingEntity entity, boolean activeByDefault) {
        super(type, entity);
        this.isActive = activeByDefault;
    }

    @Override
    public void onUse() {
        this.isActive = !this.isActive;
        PowerHolderComponent.sync(entity);
    }

    public boolean isActive() {
        return this.isActive && super.isActive();
    }

    @Override
    public NbtElement toTag() {
        return NbtByte.of(isActive);
    }

    @Override
    public void fromTag(NbtElement tag) {
        isActive = ((NbtByte)tag).byteValue() > 0;
    }

    private Key key;

    @Override
    public Key getKey() {
        return key;
    }

    @Override
    public void setKey(Key key) {
        this.key = key;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<TogglePower>(Apoli.identifier("toggle"),
            new SerializableData()
                .add("active_by_default", SerializableDataTypes.BOOLEAN, true)
                .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Active.Key()),
            data ->
                (type, player) -> {
                    TogglePower power = new TogglePower(type, player, data.getBoolean("active_by_default"));
                    power.setKey((Active.Key)data.get("key"));
                    return power;
                })
            .allowCondition();
    }
}
