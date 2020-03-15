package com.unciv.app.desktop.textRpg

import com.unciv.app.desktop.*

object Locations{
    const val academy = "Academy (Cyoria)"
    const val home = "Home (Cirin)"
}

class PreparationState(val player: Player) : State() {
    var preparationTurns = 10
    var location = "Home (Cirin)"

    override fun nextState(): State {

        val choices = when (location) {
            Locations.academy -> academyChoices()
            else -> homeChoices()
        }
        chooseAndActivateAction(choices)
        println("   ------    ")

        if (preparationTurns > 0) {
            println("It's a new day - What will you do?")
            return this
        } else {
            val enemy = Combatant("Cultist", 200, arrayListOf(getMagicMissile().apply { experience = 100 }))
            println("A ${enemy.name} attacks you!")
            return BattleState(player, enemy)
        }
    }

    fun academyChoices(): List<Action> {
        return listOf(Action("Go to class"){
            println("The teacher was really boring, but at least he taught you a Mana Shaping exercise.")
            player.abilities += Ability("Mana shaping exercise", arrayListOf("Mana Shaping"), listOf())
            preparationTurns--
            }.takeIf { location=="Academy (Cyoria)" && player.abilities.none { it.name=="Mana shaping exercise" } },

            Action("Practice"){
                val practiceActions = player.abilities.map { Action("Practice "+it.name){
                    println("You practice ${it.name} for a day.")
                    it.experience += 10
                    preparationTurns--
                } }
                chooseAndActivateAction(practiceActions)
            }).filterNotNull()
    }

    fun homeChoices(): List<Action> {
        return listOf(Action("Head to Cyoria"){
            location = Locations.academy
            println("You take the train from Cirin to Cyoria, and check in to your new academy housing -")
            println("  since the Academy is now authorized to teach you first-circle spells, security is tighter.")
        })
    }
}

class NewGameState():State(){
    override fun nextState(): State {
        println("The summer break is finally over, and today you start your third year at Cyoria's Royal Academy of Magical Arts.")
        println("After passing the examination last year, you are now considered a proper first-circle mage.")
        val player = Player()
        return PreparationState(player)
    }
}