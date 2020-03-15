package com.unciv.app.desktop.textRpg

import com.unciv.app.desktop.*

class InvasionState(val player: Player): State(){
    override fun nextState(): State {
        when {
            Encounter.reset !in player.encounters -> {
                val texts = listOf(
                        "The sound of fireworks break you out of your thoughts. It was beautiful.",
                        " Most of them dissolved into quickly fading motes of light after the initial explosion, but a couple of them remained whole and consistently bright, more like flares than fireworks.",
                        " They arced through the sky before dipping down and falling back to earth like falling stars.",
                        " You frown. Weird. Shouldn't they be exploding by now?",

                        "The flare falling closest to you slams into the nearby academy residence building and detonates.",
                        " The explosion us so loud and so bright that you are momentarily blinded and deafened,",
                        " stumbling back and collapsing to your knees as the entire building shakes beneath your feet.",

                        "As the flares started dropping back to earth, you begin to panic. What the hell are you supposed to do!? ",
                        " Running away would be pointless since you don't know what the flares are targeting.",
                        " You could very well be running straight into the area of effect if you run blindly.",
                        " Wait a minute, why do *you* have to do anything?",
                        " There are a bunch of capable mages in the building, you should just notify them and have them handle it.",
                        " You break into a run, but as you run a man in a red cloak fires a Magic Missile at you!")
                for (text in texts) println(text)

                val enemy = Combatant("Mage", 200, arrayListOf(Ability("Energy Ray", arrayListOf("Mana Shaping"), listOf("Damage=20")).apply { experience = 1000 }))
                return BattleState(player, enemy)
            }

            player.addEncounter(Encounter.tookTrainBack) -> {
                listOf("If there's going to be an attack, the best way of avoiding it was, well, by avoiding it.",
                        "You decided you'd done everything you could and took the first train out of the city.",
                        "If anyone asked (though you doubted they would), you'll use his trusty 'alchemical accident' excuse.",
                        " You messed up a potion and breathed in some hallucinogenic fumes, only coming to your senses when you were already outside of Cyoria.",
                        " Yes, that's exactly what happened.",
                        "As the train sped away from Cyoria in the dead of a night, you suppressed your unease and feelings of guilt",
                        " for doing so little to warn anyone of the approaching attack.",
                        " What else could you have done? Nothing, that's what. Nothing at all.",
                        "    --------    ",
                        "Your eyes abruptly shot open and suddenly you were wide awake, not a trace of drowsiness in your mind.",
                        " In your own bed, back in Cirin.",
                        "You gape incredulously, your mouth opening and closing periodically. What, again?",
                        "You can't believe it, it happened again!? What the hell?",
                        " You was glad it happened last time, since it meant you weren't… you know, dead. ",
                        "But now? Now it was just freaky. Why was this happening to you?")
                        .forEach { println(it) }
            }

            else -> {
                println("Soon, the flares will probably be coming again. This time, you'll be ready.")
                var enemy:Combatant? = null
                val choices = listOf(Action("Try and repel the invaders") {
                    println("As night commes, the flares fall. Explosions rock the academy.")
                    println("There seem to be a multitude of enemies everywhere - orcs, wolves, and humans.")
                    println("It's entirely unclear where they are all coming fom, but it's obvious they're not here to talk.")
                    println("You bide your time, and attack one of the wolves that seems distracted.")

                    val wolfAbility = arrayListOf(Ability("Bite", arrayListOf("Attack"), listOf("Damage=5")).apply { experience = 100 },
                            Ability("Claw", arrayListOf("Attack"), listOf("Damage=5")).apply { experience = 100 })
                    enemy = Combatant("Wolf",100, wolfAbility)
                },
                Action("Take the train back to Cirin"){
                    println("You head back to Cirin before the attack starts, but this changes nothing.")
                    println("The next day, you wake up in your bed, as you did before.")
                    println("There's obviously something going on - something big - and running away from it isn't helping.")
                }
                ).filterNotNull()
                chooseAndActivateAction(choices)
                if(enemy!=null) return BattleState(player,enemy!!)
            }
        }

        return PreparationState(player)
    }

}