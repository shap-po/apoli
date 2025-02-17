package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.*;
import net.minecraft.advancement.criterion.PlayerHurtEntityCriterion;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.InteractionEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InteractionEntity.class)
public abstract class InteractionEntityMixin extends Entity {

    private InteractionEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @WrapWithCondition(method = "handleAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/criterion/PlayerHurtEntityCriterion;trigger(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;FFZ)V"))
    private boolean apoli$cacheDamageData(PlayerHurtEntityCriterion criterion, ServerPlayerEntity player, Entity entity, DamageSource source, float dealt, float taken, boolean blocked, @Share("damageSource") LocalRef<DamageSource> damageSourceRef, @Share("damageDealt") LocalFloatRef damageDealtRef, @Share("damageTaken") LocalFloatRef damageTakenRef) {

        damageSourceRef.set(source);

        damageDealtRef.set(dealt);
        damageTakenRef.set(taken);

        return true;

    }

    @Inject(method = "handleAttack", at = @At(value = "RETURN", ordinal = 0))
    private void apoli$invokeHitActions(Entity attacker, CallbackInfoReturnable<Boolean> cir, @Share("damageSource") LocalRef<DamageSource> damageSourceRef, @Share("damageDealt") LocalFloatRef damageDealtRef, @Share("damageTaken") LocalFloatRef damageTakenRef) {

        DamageSource damageSource = damageSourceRef.get();

        float damageDealt = damageDealtRef.get();
        float damageTaken = damageTakenRef.get();

        PowerHolderComponent.withPowers(this, ActionWhenHitPower.class, p -> p.doesApply(attacker, damageSource, damageTaken), p -> p.whenHit(attacker));
        PowerHolderComponent.withPowers(attacker, ActionOnHitPower.class, p -> p.doesApply(this, damageSource, damageDealt), p -> p.onHit(this));

        PowerHolderComponent.withPowers(this, SelfActionWhenHitPower.class, p -> p.doesApply(damageSource, damageTaken), SelfActionWhenHitPower::whenHit);
        PowerHolderComponent.withPowers(this, AttackerActionWhenHitPower.class, p -> p.doesApply(damageSource, damageTaken), p -> p.whenHit(attacker));

        PowerHolderComponent.withPowers(attacker, SelfActionOnHitPower.class, p -> p.doesApply(this, damageSource, damageDealt), SelfActionOnHitPower::onHit);
        PowerHolderComponent.withPowers(attacker, TargetActionOnHitPower.class, p -> p.doesApply(this, damageSource, damageDealt), p -> p.onHit(this));

    }

}
