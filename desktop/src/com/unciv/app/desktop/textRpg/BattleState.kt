package com.unciv.app.desktop.textRpg

import com.unciv.app.desktop.*


class BattleState(val player: Player, val enemy: Combatant): State() {
    override fun nextState(): State {
        println("Your health: ${player.health}")

        val playerAttacks = player.abilities.filter { it.isAttack() }
        val chosenAttackIndex = chooseAction(playerAttacks.map { it.name })
        val chosenAttack = playerAttacks[chosenAttackIndex]
        val playerAttackDamage = chosenAttack.calculateDamage(player)
        enemy.health -= playerAttackDamage
        println("You use ${chosenAttack.name} for $playerAttackDamage damage! ${enemy.name} has ${enemy.health} health!")
        if (youWin()) {
            println("You defeated the ${enemy.name}!")
            return VictoryState()
        }

        val enemyAttack = enemy.abilities.filter { it.isAttack() }.random()
        val enemyAttackDamage = enemyAttack.calculateDamage(enemy)
        println("${enemy.name} used ${enemyAttack.name} for $enemyAttackDamage damage!")
        player.health -= enemyAttackDamage
        if (enemyWins()) {
            println("You died a miserable death!")
            println("  --------   ")
            if (player.addEncounter(Encounter.reset)) {
                listOf("You open your eyes, and and see a familiar ceiling.",
                        "You're back in your room. Were you saved? How long have you been here?",
                        "You go downstairs and ask your mother what happened, who attacked us? Is everyone okay?",
                        "She looks at you strangely, tells you you must have had an extremely odd dream, ",
                        "  and that you should get your things ready to head back to the Academy - you wouldn't want to miss the first day of school!",
                        "She seems to genuinely not know what you're talking about.",
                        "Was it all a dream, then? It seemed altogether too real for a dream.",
                        "Your dreams have always been vague, nonsensical, and prone to evaporate out of your memory soon after you woke up.",
                        "These felt exactly like your normal memories – no talking birds, floating pyramids,",
                        " three-eyed wolves and other surreal scenes your dreams usually contained.",
                        " And there was so much of it, too – surely a whole week worth of experiences is too much for a mere dream?",
                        "Wait, you recall you did learn some magic. You make a couple of sweeping gestures and words before cupping your hands in front of you." ,
                        "Yep, you definitely know the spell – you retained not just the memory of the casting procedure,",
                        " but also the control you developed with practice. ",
                        "You don't get things like that from a mere vision, even a prophetic one.",
                        "Is this... time travel? Or an illusion?",
                        "You're entirely unclear on what on earth is going on, but whatever it is, it's in the Academy.",
                        "You feel should probably practice your combat skills, if you don't want to get mauled like you did last time."
                ).forEach { println(it) }
            } else {
                println("You wake up at home again.")
            }

            player.health = player.maxHealth
            return PreparationState(player)
        }
        return this
    }

    private fun youWin() = enemy.health <= 0
    private fun enemyWins() = player.health <= 0
}