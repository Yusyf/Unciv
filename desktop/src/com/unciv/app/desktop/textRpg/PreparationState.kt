package com.unciv.app.desktop.textRpg

import com.unciv.app.desktop.*

object Locations{
    const val academy = "Academy (Cyoria)"
    const val home = "Home (Cirin)"
}

class PreparationState(val player: Player) : State() {
    var preparationTurns = 5
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
            if(Encounter.reset !in player.encounters){
                val texts = listOf(
                "The sound of fireworks break you out of your thoughts. It was beautiful." ,
                        " Most of them dissolved into quickly fading motes of light after the initial explosion, but a couple of them remained whole and consistently bright, more like flares than fireworks." ,
                        " They arced through the sky before dipping down and falling back to earth like falling stars." ,
                        " You frown. Weird. Shouldn't they be exploding by now?",

                "The flare falling closest to you slams into the nearby academy residence building and detonates." ,
                        " The explosion us so loud and so bright that you are momentarily blinded and deafened," ,
                        " stumbling back and collapsing to your knees as the entire building shakes beneath your feet.",

                "As the flares started dropping back to earth, you begin to panic. What the hell are you supposed to do!? " ,
                        " Running away would be pointless since you don't know what the flares are targeting." ,
                        " You could very well be running straight into the area of effect if you run blindly." ,
                        " Wait a minute, why *you* have to do anything?" ,
                        " There are a bunch of capable mages in the building, you should just notify them and have them handle it." ,
                        " You break into a run, but as you run a man in a red cloak fires a Magic Missile at you!")
                for(text in texts) println(text)
            }
            val enemy = Combatant("Mage", 200, arrayListOf(getMagicMissile().apply { experience = 100 }))
            return BattleState(player, enemy)
        }
    }

    fun academyChoices(): List<Action> {
        return listOf(Action("Go to class"){
            classChoice()
            preparationTurns--
            },

            Action("Practice"){
                val practiceActions = player.abilities.map { Action("Practice "+it.name){
                    println("You practice ${it.name} for a day.")
                    it.experience += 10
                    preparationTurns--
                } }
                chooseAndActivateAction(practiceActions)
            })
    }

    fun classChoice(){
        when(preparationTurns) {
            5 -> {
                println("Essential Invocations. We learned the difference between structured and unstructured magic," +
                        " and learned the 'torch' spell and how it differs from the light-emitting shaping exercise.")
                if (player.abilities.none { it.name == "Mana shaping exercise" })
                    player.abilities += Ability("Torch", arrayListOf("Light emitting"), listOf())
            }
            4 -> println("Advanced Runes. This year, we're starting to learn about imbuing runes, and the effects of such.")
            3 -> {
                println("Warding. The teacher's textbook is more like a manual for a professional warder than a student's textbook,")
                println(" and it's clear that although she knows the subject material, she doesn't know how to teach. At all.")
            }
            2 -> {
                println("Mana shaping. We learnt a new mana shaping exercise - spinning a pencil, but switching between the X and Z axes.")
                println("It's much more difficult than I expected.")
                if (player.abilities.none { it.name == "Mana shaping exercise" })
                    player.abilities += Ability("Mana shaping exercise", arrayListOf("Mana Shaping"), listOf())
            }
            1 -> {
                println("Alchemy. We made a type of glue out of ground Borer husks.")
            }
        }
    }

    fun homeChoices(): List<Action> {
        return listOf(Action("Head to Cyoria"){
            location = Locations.academy
            println("You take the train from Cirin to Cyoria, and check in to your new academy housing -")
            println("  since the Academy is now authorized to teach you first-circle spells, security is tighter.")
            preparationTurns--
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