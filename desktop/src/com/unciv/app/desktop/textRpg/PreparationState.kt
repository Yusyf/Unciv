package com.unciv.app.desktop.textRpg

import com.unciv.app.desktop.*

class PreparationState(val player: Combatant) : State() {
    var preparationTurns = 10

    override fun nextState(): State {
        if (preparationTurns > 0) {
            println("What will you do?")
            val choices = listOf(Action("Go to class"){
                println("The teacher was really boring, but at least he taught you a Mana Shaping exercise.")
                player.abilities += Ability("Mana shaping exercise", arrayListOf("Mana Shaping"), listOf())
                preparationTurns--
            }.takeIf { player.abilities.none { it.name=="Mana shaping exercise" } },
            Action("Practice"){
                val practiceActions = player.abilities.map { Action("Practice "+it.name){
                    println("You practice ${it.name} for a day.")
                    it.experience += 10
                    preparationTurns--
                } }
                chooseAndActivateAction(practiceActions)
            }).filterNotNull()
            chooseAndActivateAction(choices)
            println("   ------    ")
            return this
        } else {
            val enemy = Combatant("Cultist", 200, arrayListOf(getMagicMissile().apply { experience = 100 }))
            println("A ${enemy.name} attacks you!")
            return BattleState(player, enemy)
        }
    }
}