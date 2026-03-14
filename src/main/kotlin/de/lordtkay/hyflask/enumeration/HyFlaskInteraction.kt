package de.lordtkay.hyflask.enumeration

enum class HyFlaskInteraction(val id: String) {
    APPLY_EFFECT("HyFlask_ApplyEffect"),
    LEARN_EFFECT("HyFlask_LearnEffect"),
    FORGET_EFFECT("HyFlask_ForgetEffect"),
    REQUIRE_EFFECT("HyFlask_RequireEffect"),
    MODIFY_JUMP_HEIGHT("HyFlask_ModifyJumpHeight"),
    HAS_USES("HyFlask_HasUses"),
    MODIFY_USES("HyFlask_ModifyUses"),
    MODIFY_VISION("HyFlask_ModifyVision"),
    ;
}