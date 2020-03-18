package com.unciv.app.desktop.textRpg

import kotlin.random.Random

object MonsterGenerator {
    val run = Ability("Run", arrayListOf("Speed"), listOf("Energy=10", "Escape","CannotUseWhen=Burdened"))
    fun getHornRabbit(): Combatant {
        val rabbit = Combatant("Horn Rabbit")
        rabbit.abilities += Ability("Horn Attack", arrayListOf("Strength", "Body"), listOf("Damage=10"))
                .apply { experience = Random.nextInt(1000, 3000) }
        rabbit.abilities += run
        rabbit.corpseLoot += sequenceOf(Item("Horn Rabbit meat", "Food"),
                Item("Rabbit Horn", "Dagger","Equip=Hands"))
        return rabbit
    }

    fun getWolf(): Combatant {
        val wolf = Combatant("Wolf")
        wolf.abilities += Ability("Claw", arrayListOf("Strength", "Body"), listOf("Damage=10"))
                .apply { experience = Random.nextInt(1000, 3000) }
        wolf.abilities += Ability("Bite", arrayListOf("Strength", "Body"), listOf("Damage=10"))
                .apply { experience = Random.nextInt(1000, 3000) }
        wolf.abilities += run
        wolf.corpseLoot += Item("Wolf meat", "Food").times(2)
//        if(Random.nextInt(100) > 70)
//            wolf.corpseLoot += Item("Wolf skin", "Skin")

        return wolf
    }
}